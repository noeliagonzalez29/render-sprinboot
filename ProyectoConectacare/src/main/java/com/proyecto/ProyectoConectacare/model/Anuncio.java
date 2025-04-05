package com.proyecto.ProyectoConectacare.model;

import com.google.cloud.firestore.annotation.ServerTimestamp;

import java.util.Date;


public class Anuncio {
    private String id;
    private String titulo;
    private String descripcion;

    @ServerTimestamp
    private Date fechaPublicacion;
    private String clienteId; // Referencia al id del cliente que publica el anuncio

    public Anuncio() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Date getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(Date fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }
}
