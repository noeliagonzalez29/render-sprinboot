package com.proyecto.ProyectoConectacare.dto;

import com.proyecto.ProyectoConectacare.model.EstadoSolicitud;

import java.util.Date;
import java.util.List;

/**
 * Representa un objeto de transferencia de datos (DTO) para una solicitud de empleo que incluye los datos del trabajador.
 * Esta clase se utiliza para encapsular y gestionar la información relacionada con una solicitud específica
 * enviada por un trabajador para un anuncio específico, junto con sus datos correspondientes.
 *
 * La clase contiene propiedades como:
 * - Los identificadores únicos de la solicitud, el anuncio y el trabajador.
 * - La fecha de envío de la solicitud.
 * - Datos personales y cualificaciones del trabajador, como nombre, apellidos, disponibilidad,
 * habilidades, formación y experiencia.
 * - El estado actual de la solicitud mediante la enumeración EstadoSolicitud.
 * - Indicadores que indican si el proceso de solicitud se ha completado o si existe una evaluación.
 *
 * Esta clase facilita la transferencia de datos entre las capas de la aplicación y sirve como objeto intermediario para operaciones como la creación, recuperación y gestión de solicitudes de empleo con información del trabajador.
 */
public class SolicitudConTrabajadorDTO {
    private String id;
    private String anuncioId;
    private String trabajadorId;
    private Date fechaSolicitud;
    private String trabajadorNombre;
    private String apellido;
    private String disponibilidad;
    private List<String> habilidades;
    private String estudios;
    private String experiencia;
    private EstadoSolicitud estado;
    private boolean completado;
    private boolean evaluacionExistente;

    public boolean isCompletado() {
        return completado;
    }

    public void setCompletado(boolean completado) {
        this.completado = completado;
    }

    public boolean isEvaluacionExistente() {
        return evaluacionExistente;
    }

    public void setEvaluacionExistente(boolean evaluacionExistente) {
        this.evaluacionExistente = evaluacionExistente;
    }

    public SolicitudConTrabajadorDTO() {
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
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

    public String getTrabajadorNombre() {
        return trabajadorNombre;
    }

    public void setTrabajadorNombre(String trabajadorNombre) {
        this.trabajadorNombre = trabajadorNombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(String disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    public List<String> getHabilidades() {
        return habilidades;
    }

    public void setHabilidades(List<String> habilidades) {
        this.habilidades = habilidades;
    }

    public String getEstudios() {
        return estudios;
    }

    public void setEstudios(String estudios) {
        this.estudios = estudios;
    }

    public String getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(String experiencia) {
        this.experiencia = experiencia;
    }
}
