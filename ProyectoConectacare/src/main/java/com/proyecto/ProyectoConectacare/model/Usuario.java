package com.proyecto.ProyectoConectacare.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class Usuario {

    private String id;

    @NotEmpty(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotEmpty(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    @NotEmpty(message = "La contraseña es obligatoria")
    private String password;

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;

    // Campos opcionales según el tipo de usuario:
    // Para Cliente:
    private String direccion;
    private List<String> necesidades;

    // Para Trabajador:
    private String disponibilidad;
    private String estudios;
    private String experiencia;
    private List<String> habilidades;

    public Usuario() {
    }
}
