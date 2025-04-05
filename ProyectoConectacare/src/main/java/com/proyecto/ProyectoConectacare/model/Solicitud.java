package com.proyecto.ProyectoConectacare.model;

import com.google.cloud.firestore.annotation.ServerTimestamp;


import java.util.Date;


public class Solicitud {
    private String id;
    private String anuncioId;
    private String trabajadorId;
    @ServerTimestamp
    private Date fechaSolicitud;
    private EstadoSolicitud estado;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAnuncioId() {
        return anuncioId;
    }

    public void setAnuncioId(String anuncioId) {
        this.anuncioId = anuncioId;
    }

    public String getTrabajadorId() {
        return trabajadorId;
    }

    public void setTrabajadorId(String trabajadorId) {
        this.trabajadorId = trabajadorId;
    }

    public Date getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(Date fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public Solicitud() {
    }
}
