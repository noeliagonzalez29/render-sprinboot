package com.proyecto.ProyectoConectacare.dto;

import jakarta.validation.constraints.NotEmpty;

public class AdminDTO extends RegistroDTO{
    @NotEmpty(message = "El nombre no puede estar vacío")
    private String nombre;

    public AdminDTO() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
