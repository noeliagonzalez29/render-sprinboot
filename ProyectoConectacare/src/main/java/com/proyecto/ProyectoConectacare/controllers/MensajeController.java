package com.proyecto.ProyectoConectacare.controllers;

import com.proyecto.ProyectoConectacare.model.Mensaje;
import com.proyecto.ProyectoConectacare.service.MensajeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mensajes")
public class MensajeController {
    private final MensajeService mensajeService;

    public MensajeController(MensajeService mensajeService) {
        this.mensajeService = mensajeService;
    }

    @PostMapping
    public ResponseEntity<Mensaje> enviarMensaje(@RequestBody Mensaje mensaje) {
        return new ResponseEntity<>(mensajeService.sendMensaje(mensaje), HttpStatus.CREATED);
    }

    @GetMapping("/conversacion")
    public List<Mensaje> obtenerConversacion(
            @RequestParam String usuario1Id,
            @RequestParam String usuario2Id) {
        return mensajeService.getMensajesByConversacion(usuario1Id, usuario2Id);
    }
}
