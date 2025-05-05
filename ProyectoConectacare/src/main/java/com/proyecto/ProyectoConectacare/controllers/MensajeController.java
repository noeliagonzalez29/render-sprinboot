package com.proyecto.ProyectoConectacare.controllers;

import com.proyecto.ProyectoConectacare.model.Mensaje;
import com.proyecto.ProyectoConectacare.service.MensajeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Clase controladora para gestionar operaciones relacionadas con mensajes.
 * Proporciona puntos finales para enviar un mensaje y recuperar conversaciones.
 */
@RestController
@RequestMapping("/mensajes")
public class MensajeController {
    private final MensajeService mensajeService;

    public MensajeController(MensajeService mensajeService) {
        this.mensajeService = mensajeService;
    }

    /**
     * Gestiona el envío de un mensaje procesándolo y devolviendo la respuesta.
     *
     * @param mensaje: el mensaje que se enviará, con detalles como remitente, destinatario, contenido y marca de tiempo.
     * @return: una entidad de respuesta que contiene el mensaje enviado y un estado HTTP de CREADO.
     */
    @PostMapping
    public ResponseEntity<Mensaje> enviarMensaje(@RequestBody Mensaje mensaje) {
        return new ResponseEntity<>(mensajeService.mandarMensaje(mensaje), HttpStatus.CREATED);
    }

    /**
     * Recupera la lista de mensajes intercambiados entre dos usuarios según sus ID.
     *
     * @param usuario1Id el ID del primer usuario que participó en la conversación.
     * @param usuario2Id el ID del segundo usuario que participó en la conversación.
     * @return una lista de mensajes que representan la conversación entre los dos usuarios.
     */
    @GetMapping("/conversacion")
    public List<Mensaje> obtenerConversacion(
            @RequestParam String usuario1Id,
            @RequestParam String usuario2Id) {
        return mensajeService.getMensajesByConversacion(usuario1Id, usuario2Id);
    }
}
