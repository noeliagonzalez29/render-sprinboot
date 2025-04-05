package com.proyecto.ProyectoConectacare.model;

import com.google.cloud.firestore.annotation.ServerTimestamp;


import java.util.Date;


public class Mensaje {
    private String id;
    private String remitenteId;   // Puede ser el id del cliente o trabajador
    private String destinatarioId;
    private String contenido;
    @ServerTimestamp
    private Date fechaEnvio;

    public Mensaje() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRemitenteId() {
        return remitenteId;
    }

    public void setRemitenteId(String remitenteId) {
        this.remitenteId = remitenteId;
    }

    public String getDestinatarioId() {
        return destinatarioId;
    }

    public void setDestinatarioId(String destinatarioId) {
        this.destinatarioId = destinatarioId;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public Date getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(Date fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }
}
