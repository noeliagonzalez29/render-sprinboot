package com.proyecto.ProyectoConectacare.service.impl;

import com.google.cloud.firestore.Firestore;
import com.proyecto.ProyectoConectacare.model.Evento;
import com.proyecto.ProyectoConectacare.model.LogEstadisticas;
import com.proyecto.ProyectoConectacare.model.Usuario;
import com.proyecto.ProyectoConectacare.service.LogEstadisticaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LogEstadisticaServiceImpl  implements LogEstadisticaService {
    private static final Logger logger = LoggerFactory.getLogger(LogEstadisticaServiceImpl.class);
    private final Firestore db;
    private final String COLECCION_LOGS = "logEstadisticas";

    public LogEstadisticaServiceImpl(Firestore db) {
        this.db = db;
    }
    @Override
    public void registrarEvento(Usuario usuario, Evento tipoEvento) {
        if (usuario == null || usuario.getId() == null) {
            logger.warn("Intento de registrar evento '{}' para usuario nulo o sin ID.", tipoEvento);
            return; // No hacer nada si no hay usuario válido
        }
        try {
            LogEstadisticas log = new LogEstadisticas();
            log.setId(usuario.getId());
            log.setEmail(usuario.getEmail());
            log.setNombre(usuario.getNombre()); // O concatenar nombre y apellido
            log.setEvento(tipoEvento);


            // Guardar de forma asíncrona
            db.collection(COLECCION_LOGS).add(log);
            logger.info("Evento '{}' registrado para usuario ID: {}", tipoEvento, usuario.getId());

        } catch (Exception e) {
            // Loguear el error pero no lanzar excepción para no afectar flujo principal
            logger.error("❌ Error al registrar evento '{}' para usuario ID {}: {}", tipoEvento, usuario.getId(), e.getMessage(), e);
        }
    }

}
