package com.proyecto.ProyectoConectacare.model;

import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.Data;

import java.util.Date;

@Data
public class Mensaje {
    private String id;
    private String remitenteId;   // Puede ser el id del cliente o trabajador
    private String destinatarioId;
    private String contenido;
    @ServerTimestamp
    private Date fechaEnvio;

    public Mensaje() {
    }
}
