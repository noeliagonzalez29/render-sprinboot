package com.proyecto.ProyectoConectacare.service;

import com.proyecto.ProyectoConectacare.model.Usuario;

import java.util.List;

public interface UsuarioService {
    Usuario createUsuario(Usuario usuario);
    Usuario updateUsuario(String id, Usuario usuario);
    Usuario getUsuarioById(String id);
    List<Usuario> getAllUsuarios();
    void deleteUsuario(String id);
}
