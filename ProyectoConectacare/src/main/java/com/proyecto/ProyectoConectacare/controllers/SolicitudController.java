package com.proyecto.ProyectoConectacare.controllers;

import com.proyecto.ProyectoConectacare.exception.PresentationException;
import com.proyecto.ProyectoConectacare.model.Solicitud;
import com.proyecto.ProyectoConectacare.service.SolicitudService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/solicitudes")
public class SolicitudController {
    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PostMapping
    public ResponseEntity<Solicitud> crearSolicitud(@Valid @RequestBody Solicitud solicitud) {
        if(!anuncioService.existeAnuncio(solicitud.getAnuncioId())) {
            throw new PresentationException("El anuncio no existe", HttpStatus.BAD_REQUEST);
        }

        solicitud.setFechaSolicitud(new Date()); // Fecha actual del servidor
        Solicitud nuevaSolicitud = solicitudService.createSolicitud(solicitud);
        return new ResponseEntity<>(nuevaSolicitud, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Solicitud> obtenerSolicitud(@PathVariable String id) {
        return ResponseEntity.ok(solicitudService.getSolicitudById(id));
    }

    @GetMapping("/anuncio/{anuncioId}")
    public List<Solicitud> obtenerSolicitudesPorAnuncio(@PathVariable String anuncioId) {
        return solicitudService.getSolicitudesByAnuncioId(anuncioId);
    }

    @GetMapping("/trabajador/{trabajadorId}")
    public List<Solicitud> obtenerSolicitudesPorTrabajador(@PathVariable String trabajadorId) {
        return solicitudService.getSolicitudesByTrabajadorId(trabajadorId);
    }
}
