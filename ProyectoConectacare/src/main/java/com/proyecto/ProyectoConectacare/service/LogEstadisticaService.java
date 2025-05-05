package com.proyecto.ProyectoConectacare.service;

import com.proyecto.ProyectoConectacare.model.Evento;
import com.proyecto.ProyectoConectacare.model.Usuario;

public interface LogEstadisticaService {

    void registrarEvento(Usuario usuario, Evento tipoEvento);
}
