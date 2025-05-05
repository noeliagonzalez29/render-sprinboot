package com.proyecto.ProyectoConectacare.model;

import com.google.cloud.firestore.annotation.ServerTimestamp;


import java.util.Date;


/**
 * La clase Solicitud representa una solicitud o aplicación realizada por un trabajador a un anuncio.
 * Está diseñada para gestionar la interacción entre clientes y trabajadores dentro de un sistema.
 *
 * Esta clase incluye información como:
 * - El ID de la solicitud.
 * - El ID del anuncio asociado.
 * - El ID del trabajador que creó la solicitud.
 * - El ID del cliente propietario del anuncio.
 * - La marca de tiempo que indica cuándo se creó la solicitud.
 * - El estado actual de la solicitud, representado por la enumeración EstadoSolicitud.
 * - Un indicador que indica si la solicitud se ha completado.
 *
 * Se proporcionan métodos para acceder y modificar estos atributos.
 */
public class Solicitud {
    private String id;
    private String anuncioId;
    private String trabajadorId;
    @ServerTimestamp
    private Date fechaSolicitud;
    private EstadoSolicitud estado;
    private String clienteId;
    private boolean completado;
    public String getClienteId() {
        return clienteId;
    }
    public boolean isCompletado() {
        return completado;
    }

    public void setCompletado(boolean completado) {
        this.completado = completado;
    }
    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

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
