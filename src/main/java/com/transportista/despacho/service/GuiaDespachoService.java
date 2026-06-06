package com.transportista.despacho.service;

import com.transportista.despacho.model.GuiaDespacho;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface GuiaDespachoService {
    GuiaDespacho crearGuia(String transportista, MultipartFile archivo);
    GuiaDespacho subirGuiaAS3(Long id, String bucketName) throws IOException;
    byte[] descargarGuia(Long id, String bucketName, String permiso) throws IOException;
    GuiaDespacho actualizarGuia(Long id, String bucketName, MultipartFile archivo) throws IOException;
    void eliminarGuia(Long id, String bucketName);
    List<GuiaDespacho> consultarPorTransportistaYFecha(String transportista, String yyyyMM);
}