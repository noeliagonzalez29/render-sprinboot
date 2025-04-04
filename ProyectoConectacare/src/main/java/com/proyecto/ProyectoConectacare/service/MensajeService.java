package com.proyecto.ProyectoConectacare.service;

import com.proyecto.ProyectoConectacare.model.Mensaje;

import java.util.List;

public interface MensajeService {
    Mensaje sendMensaje(Mensaje mensaje);
    List<Mensaje> getMensajesByConversacion(String usuario1Id, String usuario2Id);
}
