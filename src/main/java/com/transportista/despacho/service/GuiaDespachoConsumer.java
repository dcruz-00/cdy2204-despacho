package com.transportista.despacho.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.transportista.despacho.config.RabbitConfig;
import com.transportista.despacho.dto.GuiaMensajeDTO;
import com.transportista.despacho.model.GuiaDespachoProcesada;
import com.transportista.despacho.repository.GuiaDespachoProcesadaRepository;

@Component
public class GuiaDespachoConsumer {

    private static final Logger log = LoggerFactory.getLogger(GuiaDespachoConsumer.class);

    private final GuiaDespachoProcesadaRepository procesadaRepo;

    @Autowired
    public GuiaDespachoConsumer(GuiaDespachoProcesadaRepository procesadaRepo) {
        this.procesadaRepo = procesadaRepo;
    }

    @RabbitListener(queues = RabbitConfig.COLA_GUIAS)
    public void recibirGuia(GuiaMensajeDTO mensaje) {
        log.info("Mensaje recibido de {}: guía {}", RabbitConfig.COLA_GUIAS, mensaje.getNumeroGuia());

        GuiaDespachoProcesada procesada = new GuiaDespachoProcesada();
        procesada.setGuiaId(mensaje.getGuiaId());
        procesada.setNumeroGuia(mensaje.getNumeroGuia());
        procesada.setTransportista(mensaje.getTransportista());
        procesada.setFechaCreacionGuia(mensaje.getFechaCreacion());
        procesada.setEstadoGuia(mensaje.getEstado());
        procesada.setFechaProcesamiento(LocalDateTime.now());

        procesadaRepo.save(procesada);
        log.info("Guía {} guardada en GUIA_DESPACHO_PROCESADA", mensaje.getNumeroGuia());
    }
}