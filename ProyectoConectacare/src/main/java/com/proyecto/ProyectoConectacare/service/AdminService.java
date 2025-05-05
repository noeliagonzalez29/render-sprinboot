package com.proyecto.ProyectoConectacare.service;

import com.proyecto.ProyectoConectacare.model.Usuario;

import java.io.IOException;
import java.util.List;

/**
 * La interfaz AdminService proporciona métodos para administrar e interactuar con los usuarios del sistema.
 * Está diseñada para realizar operaciones administrativas como recuperar datos de usuarios y eliminarlos.
 */
public interface AdminService {
    List<Usuario> obtenerUsuarios();
    void eliminarUsuario(String id);
    int contarInicioSesion();
    int contarRegistros();
    byte[] generarCSVBytes(List<Usuario> usuarios);


}
