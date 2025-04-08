package com.proyecto.ProyectoConectacare.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;


import java.util.List;
import java.util.Objects;


public class Usuario {

    private String id;

    @NotEmpty(message = "El nombre no puede estar vacío")
    private String nombre;

    private String apellido;
    @NotEmpty(message = "El telefono no puede estar vacío")
    private String telefono;

    @NotEmpty(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;


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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
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

    public String getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(String disponibilidad) {
        this.disponibilidad = disponibilidad;
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

    public List<String> getHabilidades() {
        return habilidades;
    }

    public void setHabilidades(List<String> habilidades) {
        this.habilidades = habilidades;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
