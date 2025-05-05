package com.proyecto.ProyectoConectacare.service;

import com.proyecto.ProyectoConectacare.model.Mensaje;

import java.util.List;

/**
 * La interfaz MensajeService define las operaciones para gestionar y recuperar entidades de "Mensaje".
 * Un "Mensaje" suele representar un mensaje intercambiado entre dos usuarios en una conversación.
 * Este servicio proporciona métodos para enviar y recuperar mensajes basados ​​en una conversación.
 */
public interface MensajeService {
    Mensaje mandarMensaje(Mensaje mensaje);
    List<Mensaje> getMensajesByConversacion(String usuario1Id, String usuario2Id);
}
