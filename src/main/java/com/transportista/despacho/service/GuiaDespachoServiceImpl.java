package com.transportista.despacho.service;

import com.transportista.despacho.model.GuiaDespacho;
import com.transportista.despacho.repository.GuiaDespachoRepository;
import com.transportista.despacho.repository.S3Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class GuiaDespachoServiceImpl implements GuiaDespachoService {

    private final GuiaDespachoRepository guiaRepo;
    private final S3Repository s3Repository;

    private static final String EFS_BASE_PATH = "/app/efs/";

    @Autowired
    public GuiaDespachoServiceImpl(GuiaDespachoRepository guiaRepo, S3Repository s3Repository) {
        this.guiaRepo = guiaRepo;
        this.s3Repository = s3Repository;
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

        return guiaRepo.save(guia);
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