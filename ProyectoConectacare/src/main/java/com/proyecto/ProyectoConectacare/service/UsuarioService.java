package com.proyecto.ProyectoConectacare.service;

import com.proyecto.ProyectoConectacare.model.Usuario;

import java.util.List;
import java.util.Map;

public interface UsuarioService {
    Usuario createUsuario(Usuario usuario);
    Usuario updateUsuario(String id, Map<String, Object> updates);
    Usuario getUsuarioById(String id);
    List<Usuario> getAllUsuarios();
    void deleteUsuario(String id);
}
