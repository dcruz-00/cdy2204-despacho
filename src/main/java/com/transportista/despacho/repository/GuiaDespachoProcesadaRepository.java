package com.transportista.despacho.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.transportista.despacho.model.GuiaDespachoProcesada;

@Repository
public interface GuiaDespachoProcesadaRepository extends JpaRepository<GuiaDespachoProcesada, Long> {
}