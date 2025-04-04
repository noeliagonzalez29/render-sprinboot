package com.proyecto.ProyectoConectacare.service;

import com.proyecto.ProyectoConectacare.model.Solicitud;

import java.util.List;

public interface SolicitudService {
    Solicitud createSolicitud(Solicitud solicitud);
    Solicitud getSolicitudById(String id);
    List<Solicitud> getSolicitudesByAnuncioId(String anuncioId);
    List<Solicitud> getSolicitudesByTrabajadorId(String trabajadorId);
}
