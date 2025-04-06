package com.proyecto.ProyectoConectacare.service;

import com.proyecto.ProyectoConectacare.model.Usuario;

import java.util.List;

public interface AdminService {
    List<Usuario> obtenerUsuarios();
    void eliminarUsuario(String id);
}
