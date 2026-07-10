package com.transportista.despacho.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.transportista.despacho.config.RabbitConfig;
import com.transportista.despacho.dto.GuiaMensajeDTO;
import com.transportista.despacho.model.GuiaDespacho;
import com.transportista.despacho.repository.GuiaDespachoRepository;
import com.transportista.despacho.repository.S3Repository;

@Service
public class GuiaDespachoServiceImpl implements GuiaDespachoService {

    private static final Logger log = LoggerFactory.getLogger(GuiaDespachoServiceImpl.class);

    private final GuiaDespachoRepository guiaRepo;
    private final S3Repository s3Repository;
    private final RabbitTemplate rabbitTemplate;

    private static final String EFS_BASE_PATH = "/app/efs/";

    @Autowired
    public GuiaDespachoServiceImpl(GuiaDespachoRepository guiaRepo, S3Repository s3Repository,
            RabbitTemplate rabbitTemplate) {
        this.guiaRepo = guiaRepo;
        this.s3Repository = s3Repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public GuiaDespacho crearGuia(String transportista, MultipartFile archivo) {
        String numeroGuia = UUID.randomUUID().toString();
        String rutaEfs = EFS_BASE_PATH + numeroGuia + ".pdf";

        // Guardar archivo en EFS
        File destino = new File(rutaEfs);
        destino.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(destino)) {
            fos.write(archivo.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar guía en EFS", e);
        }

        GuiaDespacho guia = new GuiaDespacho();
        guia.setNumeroGuia(numeroGuia);
        guia.setTransportista(transportista);
        guia.setFechaCreacion(LocalDateTime.now());
        guia.setEstado("CREADA");
        guia.setRutaEfs(rutaEfs);

        guia = guiaRepo.save(guia);

        publicarGuiaEnCola(guia);

        return guia;
    }

    private void publicarGuiaEnCola(GuiaDespacho guia) {
        GuiaMensajeDTO mensaje = new GuiaMensajeDTO(
                guia.getId(),
                guia.getNumeroGuia(),
                guia.getTransportista(),
                guia.getFechaCreacion(),
                guia.getEstado());
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE_NAME,
                    RabbitConfig.ROUTING_KEY_GUIAS,
                    mensaje);
            log.info("Guía {} publicada en {}", guia.getNumeroGuia(), RabbitConfig.COLA_GUIAS);
        } catch (Exception e) {
            log.error("Fallo al publicar la guía {} en {}. Reenviando a {}",
                    guia.getNumeroGuia(), RabbitConfig.COLA_GUIAS, RabbitConfig.COLA_GUIAS_ERROR, e);
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE_NAME,
                    RabbitConfig.ROUTING_KEY_ERROR,
                    mensaje);
        }
    }

    @Override
    public GuiaDespacho subirGuiaAS3(Long id, String bucketName) throws IOException {
        GuiaDespacho guia = guiaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Guía no encontrada: " + id));

        String yyyyMM = guia.getFechaCreacion().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String claveS3 = yyyyMM + "/" + guia.getTransportista() + "/" + guia.getNumeroGuia() + ".pdf";

        File archivo = new File(guia.getRutaEfs());
        s3Repository.uploadFile(bucketName, claveS3, archivo);

        guia.setRutaS3(claveS3);
        guia.setEstado("SUBIDA");
        return guiaRepo.save(guia);
    }

    @Override
    public byte[] descargarGuia(Long id, String bucketName, String permiso) throws IOException {
        if (!"ADMIN".equals(permiso)) {
            throw new SecurityException("Permiso insuficiente para descargar esta guía");
        }

        GuiaDespacho guia = guiaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Guía no encontrada: " + id));

        if (guia.getRutaS3() == null) {
            throw new RuntimeException("La guía aún no ha sido subida a S3");
        }

        return s3Repository.downloadFile(bucketName, guia.getRutaS3());
    }

    @Override
    public GuiaDespacho actualizarGuia(Long id, String bucketName, MultipartFile archivo) throws IOException {
        GuiaDespacho guia = guiaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Guía no encontrada: " + id));

        if (guia.getRutaS3() == null) {
            throw new RuntimeException("La guía aún no ha sido subida a S3");
        }

        // Reemplazar en EFS
        File nuevo = new File(guia.getRutaEfs());
        try (FileOutputStream fos = new FileOutputStream(nuevo)) {
            fos.write(archivo.getBytes());
        }

        // Reemplazar en S3 (misma clave)
        s3Repository.uploadFile(bucketName, guia.getRutaS3(), nuevo);

        guia.setEstado("ACTUALIZADA");
        return guiaRepo.save(guia);
    }

    @Override
    public void eliminarGuia(Long id, String bucketName) {
        GuiaDespacho guia = guiaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Guía no encontrada: " + id));

        if (guia.getRutaS3() != null) {
            s3Repository.deleteObject(bucketName, guia.getRutaS3());
        }

        // Eliminar de EFS
        File archivoEfs = new File(guia.getRutaEfs());
        if (archivoEfs.exists()) {
            archivoEfs.delete();
        }

        guia.setEstado("ELIMINADA");
        guiaRepo.save(guia);
    }

    @Override
    public List<GuiaDespacho> consultarPorTransportistaYFecha(String transportista, String yyyyMM) {
        LocalDateTime inicio = LocalDateTime.parse(yyyyMM + "01T00:00:00",
                DateTimeFormatter.ofPattern("yyyyMMdd'T'HH:mm:ss"));
        LocalDateTime fin = inicio.plusMonths(1).minusSeconds(1);
        return guiaRepo.findByTransportistaAndFechaCreacionBetween(transportista, inicio, fin);
    }
}