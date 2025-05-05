package com.proyecto.ProyectoConectacare.service;

import com.proyecto.ProyectoConectacare.model.Usuario;

import java.util.List;
import java.util.Map;

/**
 * La interfaz UsuarioService define las operaciones para gestionar e interactuar con las entidades "Usuario".
 * Un "Usuario" representa a un usuario en el sistema, que puede ser un cliente o un trabajador.
 * Este servicio proporciona métodos para crear, actualizar, recuperar, listar y eliminar usuarios.
 */
public interface UsuarioService {
    Usuario createUsuario(Usuario usuario);
    Usuario updateUsuario(String id, Map<String, Object> updates);
    Usuario getUsuarioById(String id);
    List<Usuario> getAllUsuarios();
    void deleteUsuario(String id);
}
