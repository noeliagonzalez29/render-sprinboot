package com.proyecto.ProyectoConectacare.service;

import com.proyecto.ProyectoConectacare.dto.SolicitudConTrabajadorDTO;
import com.proyecto.ProyectoConectacare.model.EstadoSolicitud;
import com.proyecto.ProyectoConectacare.model.Solicitud;

import java.util.List;

public interface SolicitudService {
    Solicitud createSolicitud(Solicitud solicitud);
    Solicitud getSolicitudById(String id);
    List<Solicitud> getSolicitudesByAnuncioId(String anuncioId);
    List<Solicitud> getSolicitudesByTrabajadorId(String trabajadorId);
    List<SolicitudConTrabajadorDTO> getSolicitudesByClienteId(String clienteId);
    Solicitud actualizarEstadoSolicitud(String solicitudId, EstadoSolicitud nuevoEstado);
    Solicitud marcarComoCompletado(String solicitudId);
}
