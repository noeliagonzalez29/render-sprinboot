package com.proyecto.ProyectoConectacare.dto;


import jakarta.validation.constraints.NotBlank;


import java.util.List;

/**
 * Representa el objeto de transferencia de datos para una entidad cliente.
 * Extiende la clase RegistroDTO para incluir propiedades adicionales específicas de un cliente.
 * Esta clase se utiliza normalmente para transferir datos relacionados con el cliente entre las capas de la aplicación.
 */
public class ClienteDTO extends RegistroDTO{

    @NotBlank
    private String nombre;
    private String apellido;
    private String direccion;
    private List<String> necesidades;

    public ClienteDTO() {
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public List<String> getNecesidades() {
        return necesidades;
    }

    public void setNecesidades(List<String> necesidades) {
        this.necesidades = necesidades;
    }


}
