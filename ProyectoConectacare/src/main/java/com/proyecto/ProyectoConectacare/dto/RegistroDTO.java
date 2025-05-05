package com.proyecto.ProyectoConectacare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Representa un objeto de transferencia de datos (DTO) para registrar un usuario.
 * Esta clase se utiliza normalmente para encapsular los datos de registro de usuarios,
 * sirviendo como una forma simple y concisa de transferir los detalles de registro entre
 * diferentes capas de la aplicación.
 *
 * La clase incluye campos básicos para correo electrónico y contraseña, junto con
 * anotaciones de validación para aplicar restricciones a los valores.
 */
public class RegistroDTO {
    @NotBlank(message = "El email es obligatorio")
    @Email
    private String email;


    private String password;

    public RegistroDTO() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
