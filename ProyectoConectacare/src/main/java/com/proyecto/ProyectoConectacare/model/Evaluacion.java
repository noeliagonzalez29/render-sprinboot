package com.proyecto.ProyectoConectacare.model;

import com.google.cloud.firestore.annotation.ServerTimestamp;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;


import java.util.Date;


/**
 * La clase "Evaluación" representa una reseña o evaluación de un servicio prestado por un trabajador a un cliente.
 * Incluye varios atributos como el ID del trabajador, el ID del cliente, la calificación y un comentario opcional.
 * Además, registra la fecha de la evaluación y el ID de la solicitud de servicio asociada.
 *
 * Esta clase está diseñada para gestionar la recopilación y gestión de datos de retroalimentación,
 * garantizando restricciones en las calificaciones y la gestión de marcas de tiempo para las evaluaciones.
 *
 * Atributos:
 * - id: Identificador único de la evaluación.
 * - trabajadorId: Identificador del trabajador evaluado.
 * - clienteId: Identificador del cliente que proporciona la evaluación.
 * - estrellas: Calificación otorgada por el cliente, con un límite de 1 a 5.
 * - comentario: Comentario o retroalimentación opcional proporcionado por el cliente.
 * - fechaEvaluación: Marca de tiempo que indica cuándo se registró la evaluación.
 * - solicitudId: Identificador de la solicitud de servicio asociada.
 */
public class Evaluacion {
    private String id;
    private String trabajadorId;
    private String clienteId;
    @Min(value = 1, message = "La calificación debe ser al menos 1")
    @Max(value = 5, message = "La calificación no puede ser mayor que 5")
    private int estrellas; // Calificación de 1 a 5
    private String comentario;
    @ServerTimestamp
    private Date fechaEvaluacion;
    private String solicitudId; // Nuevo campo

    public Evaluacion() {
    }
    public String getSolicitudId() {
        return solicitudId;
    }

    public void setSolicitudId(String solicitudId) {
        this.solicitudId = solicitudId;
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
