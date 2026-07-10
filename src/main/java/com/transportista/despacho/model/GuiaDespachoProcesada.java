package com.transportista.despacho.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "GUIA_DESPACHO_PROCESADA")
public class GuiaDespachoProcesada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "GUIA_ID", nullable = false)
    private Long guiaId;

    @Column(name = "NUMERO_GUIA", nullable = false)
    private String numeroGuia;

    @Column(name = "TRANSPORTISTA", nullable = false)
    private String transportista;

    @Column(name = "FECHA_CREACION_GUIA", nullable = false)
    private LocalDateTime fechaCreacionGuia;

    @Column(name = "ESTADO_GUIA")
    private String estadoGuia;

    @Column(name = "FECHA_PROCESAMIENTO", nullable = false)
    private LocalDateTime fechaProcesamiento;
}