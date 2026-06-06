package com.transportista.despacho.controller;

import com.transportista.despacho.model.GuiaDespacho;
import com.transportista.despacho.service.GuiaDespachoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/guias")
public class GuiaDespachoController {

    private final GuiaDespachoService guiaService;

    @Autowired
    public GuiaDespachoController(GuiaDespachoService guiaService) {
        this.guiaService = guiaService;
    }

    @PostMapping
    public ResponseEntity<GuiaDespacho> crearGuia(
            @RequestParam("transportista") String transportista,
            @RequestParam("archivo") MultipartFile archivo) {
        GuiaDespacho guia = guiaService.crearGuia(transportista, archivo);
        return new ResponseEntity<>(guia, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/subir")
    public ResponseEntity<GuiaDespacho> subirGuiaAS3(
            @PathVariable Long id,
            @RequestParam("bucketName") String bucketName) throws IOException {
        GuiaDespacho guia = guiaService.subirGuiaAS3(id, bucketName);
        return new ResponseEntity<>(guia, HttpStatus.OK);
    }

    @GetMapping("/{id}/descargar")
    public ResponseEntity<ByteArrayResource> descargarGuia(
            @PathVariable Long id,
            @RequestParam("bucketName") String bucketName,
            @RequestParam("permiso") String permiso) throws IOException {
        try {
            byte[] data = guiaService.descargarGuia(id, bucketName, permiso);
            ByteArrayResource resource = new ByteArrayResource(data);
            return ResponseEntity.ok()
                    .contentLength(data.length)
                    .header("Content-type", "application/octet-stream")
                    .header("Content-disposition", "attachment; filename=\"guia_" + id + ".pdf\"")
                    .body(resource);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<GuiaDespacho> actualizarGuia(
            @PathVariable Long id,
            @RequestParam("bucketName") String bucketName,
            @RequestParam("archivo") MultipartFile archivo) throws IOException {
        GuiaDespacho guia = guiaService.actualizarGuia(id, bucketName, archivo);
        return new ResponseEntity<>(guia, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarGuia(
            @PathVariable Long id,
            @RequestParam("bucketName") String bucketName) {
        guiaService.eliminarGuia(id, bucketName);
        return new ResponseEntity<>("Guía eliminada correctamente", HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<GuiaDespacho>> consultarGuias(
            @RequestParam("transportista") String transportista,
            @RequestParam("fecha") String fecha) {
        List<GuiaDespacho> guias = guiaService.consultarPorTransportistaYFecha(transportista, fecha);
        return new ResponseEntity<>(guias, HttpStatus.OK);
    }
}