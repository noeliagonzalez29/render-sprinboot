package com.proyecto.ProyectoConectacare.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;


import java.util.Date;


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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTrabajadorId() {
        return trabajadorId;
    }

    public void setTrabajadorId(String trabajadorId) {
        this.trabajadorId = trabajadorId;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public int getEstrellas() {
        return estrellas;
    }

    public void setEstrellas(int estrellas) {
        this.estrellas = estrellas;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Date getFechaEvaluacion() {
        return fechaEvaluacion;
    }

    public void setFechaEvaluacion(Date fechaEvaluacion) {
        this.fechaEvaluacion = fechaEvaluacion;
    }
}
