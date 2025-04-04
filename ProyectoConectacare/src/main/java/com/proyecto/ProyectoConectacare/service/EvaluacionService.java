package com.proyecto.ProyectoConectacare.service;

import com.proyecto.ProyectoConectacare.model.Evaluacion;

import java.util.List;

public interface EvaluacionService {
    Evaluacion createEvaluacion(Evaluacion evaluacion);
    Evaluacion getEvaluacionById(String id);
    List<Evaluacion> getEvaluacionesByTrabajadorId(String trabajadorId);
}
