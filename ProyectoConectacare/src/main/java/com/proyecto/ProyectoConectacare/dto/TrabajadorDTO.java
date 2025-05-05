package com.proyecto.ProyectoConectacare.dto;


import jakarta.validation.constraints.NotBlank;


import java.util.List;

/**
 * Representa el objeto de transferencia de datos (DTO) de una entidad de trabajador.
 * Extiende la clase RegistroDTO para incluir propiedades adicionales específicas de los trabajadores.
 * Esta clase se utiliza para encapsular y gestionar la información de los trabajadores, normalmente para transferir datos entre capas de la aplicación.
 *
 * Las propiedades incluyen:
 * - Datos personales como nombre, apellidos y teléfono.
 * - Disponibilidad (representada como disponibilidad).
 * - Una lista de habilidades del trabajador.
 * - Formación académica (estudios).
 * - Experiencia laboral (experiencia).
 *
 * Esta clase proporciona métodos getter y setter para gestionar estas propiedades.
 */
public class TrabajadorDTO extends RegistroDTO {

    @NotBlank
    private String nombre;
    private String apellido;

    private String telefono;

    private String disponibilidad;
    private List<String> habilidades;
    private String estudios;
    private String experiencia;

    public TrabajadorDTO() {
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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
