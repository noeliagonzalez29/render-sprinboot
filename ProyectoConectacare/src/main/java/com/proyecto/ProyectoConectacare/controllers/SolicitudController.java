package com.proyecto.ProyectoConectacare.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.proyecto.ProyectoConectacare.exception.PresentationException;
import com.proyecto.ProyectoConectacare.model.Solicitud;
import com.proyecto.ProyectoConectacare.service.AnuncioService;
import com.proyecto.ProyectoConectacare.service.SolicitudService;
import com.proyecto.ProyectoConectacare.service.impl.AnuncioServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/solicitudes")
public class SolicitudController {
    private final SolicitudService solicitudService;
    private final AnuncioServiceImpl anuncioService; //esto es para que no se creen si no existen
    private final FirebaseAuth firebaseAuth;
    public SolicitudController(SolicitudService solicitudService, AnuncioServiceImpl anuncioService,FirebaseAuth firebaseAuth) {
        this.solicitudService = solicitudService;
        this.anuncioService = anuncioService;
        this.firebaseAuth = firebaseAuth;
    }

    @PostMapping
    public ResponseEntity<Solicitud> crearSolicitud(@RequestHeader("Authorization") String token,@RequestBody Solicitud solicitud) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
            String trabajadorId = decodedToken.getUid(); // ✅ UID del trabajador

            solicitud.setTrabajadorId(trabajadorId);
            solicitud = solicitudService.createSolicitud(solicitud);

            return new ResponseEntity<>(solicitud, HttpStatus.CREATED);

        } catch (FirebaseAuthException e) {
            throw new PresentationException("Token inválido", HttpStatus.UNAUTHORIZED);
        }
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
