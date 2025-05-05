package com.proyecto.ProyectoConectacare.service;

import com.proyecto.ProyectoConectacare.dto.SolicitudConTrabajadorDTO;
import com.proyecto.ProyectoConectacare.model.EstadoSolicitud;
import com.proyecto.ProyectoConectacare.model.Solicitud;

import java.util.List;

/**
 * La interfaz SolicitudService proporciona operaciones para gestionar y recuperar entidades de "Solicitud".
 * Una "Solicitud" representa una solicitud realizada por un cliente para un servicio o tarea específica. Este servicio
 * incluye métodos para crear, recuperar, actualizar y gestionar el estado de las solicitudes.
 */
public interface SolicitudService {
    Solicitud createSolicitud(Solicitud solicitud);
    Solicitud getSolicitudById(String id);
    List<Solicitud> getSolicitudesByAnuncioId(String anuncioId);
    List<Solicitud> getSolicitudesByTrabajadorId(String trabajadorId);
    List<SolicitudConTrabajadorDTO> getSolicitudesByClienteId(String clienteId);
    Solicitud actualizarEstadoSolicitud(String solicitudId, EstadoSolicitud nuevoEstado);
    Solicitud marcarComoCompletado(String solicitudId);
}
