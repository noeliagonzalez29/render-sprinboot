package com.proyecto.ProyectoConectacare.model;

import lombok.Data;

import java.util.Date;

@Data
public class Solicitud {
    private String id;
    private String anuncioId;
    private String trabajadorId;
    private Date fechaSolicitud;
    private EstadoSolicitud estado;

    public Solicitud() {
    }
}
