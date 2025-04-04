package com.proyecto.ProyectoConectacare.model;

import lombok.Data;

import java.util.Date;

@Data
public class Anuncio {
    private String id;
    private String titulo;
    private String descripcion;
    private Date fechaPublicacion;
    private String clienteId; // Referencia al id del cliente que publica el anuncio

    public Anuncio() {
    }
}
