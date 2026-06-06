package com.transportista.despacho.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "GUIA_DESPACHO")
public class GuiaDespacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NUMERO_GUIA", nullable = false, unique = true)
    private String numeroGuia;

    @Column(name = "TRANSPORTISTA", nullable = false)
    private String transportista;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "ESTADO")
    private String estado;

    @Column(name = "RUTA_EFS")
    private String rutaEfs;

    @Column(name = "RUTA_S3")
    private String rutaS3;
}