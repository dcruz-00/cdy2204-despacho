package com.transportista.despacho.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuiaMensajeDTO implements Serializable {

    private Long guiaId;
    private String numeroGuia;
    private String transportista;
    private LocalDateTime fechaCreacion;
    private String estado;
}