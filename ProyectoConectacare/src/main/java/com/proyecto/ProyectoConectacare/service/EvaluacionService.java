package com.proyecto.ProyectoConectacare.service;

import com.proyecto.ProyectoConectacare.dto.EvaluacionDTO;
import com.proyecto.ProyectoConectacare.model.Evaluacion;


import java.util.List;

/**
 * La interfaz EvaluacionService define las operaciones para gestionar y recuperar entidades "Evaluación".
 * Una "Evaluación" suele representar una calificación o reseña proporcionada por un cliente para un trabajador específico.
 * Este servicio proporciona métodos para crear evaluaciones y recuperarlas por ID de evaluación o ID de trabajador.
 */
public interface EvaluacionService {
    Evaluacion createEvaluacion(Evaluacion evaluacion);
    Evaluacion getEvaluacionById(String id);
    List<EvaluacionDTO> getEvaluacionesByTrabajadorId(String trabajadorId);
    boolean existeEvaluacionPorSolicitud(String solicitudId);
    List<Evaluacion> getAllEvaluaciones();
}
