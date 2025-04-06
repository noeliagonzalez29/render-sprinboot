package com.proyecto.ProyectoConectacare.service.impl;

import com.proyecto.ProyectoConectacare.model.Usuario;
import com.proyecto.ProyectoConectacare.service.AdminService;
import com.proyecto.ProyectoConectacare.service.UsuarioService;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class AdminServiceImpl implements AdminService {
    private final UsuarioService usuarioService;

    public AdminServiceImpl(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }
    @Override
    public List<Usuario> obtenerUsuarios() {
        return usuarioService.getAllUsuarios();
    }

    @Override
    public void eliminarUsuario(String id) {
        usuarioService.deleteUsuario(id);
    }
}
