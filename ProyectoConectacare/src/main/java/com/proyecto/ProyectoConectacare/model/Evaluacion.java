package com.proyecto.ProyectoConectacare.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.Date;

@Data
public class Evaluacion {
    private String id;
    private String trabajadorId;
    private String clienteId;
    @Min(value = 1, message = "La calificación debe ser al menos 1")
    @Max(value = 5, message = "La calificación no puede ser mayor que 5")
    private int estrellas; // Calificación de 1 a 5
    private String comentario;
    private Date fechaEvaluacion;

    public Evaluacion() {
    }
}
