package com.proyecto.ProyectoConectacare.service.impl;

import com.proyecto.ProyectoConectacare.service.AuditService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Service
public class AuditServiceImpl implements AuditService {
    @Override
    public void registrarAccion(String usuarioId, String accion, String detalle) {
        //para ver en consola. Añadir que escriba en un archivo o base de datos
        System.out.println("[" + LocalDateTime.now() + "] Usuario: " + usuarioId
                + " - Acción: " + accion + " - Detalle: " + detalle);
    }
}
