package com.proyecto.ProyectoConectacare.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.proyecto.ProyectoConectacare.dto.SolicitudConTrabajadorDTO;
import com.proyecto.ProyectoConectacare.exception.PresentationException;
import com.proyecto.ProyectoConectacare.model.EstadoSolicitud;
import com.proyecto.ProyectoConectacare.model.Solicitud;
import com.proyecto.ProyectoConectacare.service.AnuncioService;
import com.proyecto.ProyectoConectacare.service.SolicitudService;
import com.proyecto.ProyectoConectacare.service.impl.AnuncioServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

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

    @GetMapping("/mias")
    public ResponseEntity<List<Solicitud>> obtenerMisSolicitudes(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
            String trabajadorId = decodedToken.getUid(); // 🔐 UID real del trabajador autenticado

            List<Solicitud> solicitudes = solicitudService.getSolicitudesByTrabajadorId(trabajadorId);
            return new ResponseEntity<>(solicitudes, HttpStatus.OK);

        } catch (FirebaseAuthException e) {
            throw new PresentationException("Token inválido", HttpStatus.UNAUTHORIZED);
        }
    }
    @GetMapping("/cliente")
    public List<SolicitudConTrabajadorDTO> obtenerSolicitudesParaMisAnuncios(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            FirebaseToken decoded = firebaseAuth.verifyIdToken(token);
            String uid = decoded.getUid();
            return solicitudService.getSolicitudesByClienteId(uid);
        } catch (FirebaseAuthException e) {
            throw new PresentationException("Token inválido", HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Solicitud> actualizarEstado(@PathVariable String id, @RequestBody Map<String, String> body) {
        String estadoStr = body.get("estado");
        EstadoSolicitud nuevoEstado = EstadoSolicitud.valueOf(estadoStr); // Ej: "ACEPTADA"
        Solicitud actualizada = solicitudService.actualizarEstadoSolicitud(id, nuevoEstado);
        return ResponseEntity.ok(actualizada);
    }
    @PutMapping("/{id}/completar")
    public ResponseEntity<Solicitud> marcarComoCompletado(
            @PathVariable String id,
            @RequestHeader("Authorization") String token) {

        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token.replace("Bearer ", ""));
            Solicitud solicitud = solicitudService.getSolicitudById(id);
            // Validación reforzada
            if (solicitud.getClienteId() == null) {
                throw new PresentationException("Solicitud no tiene cliente asociado", HttpStatus.BAD_REQUEST);
            }
            // Verificar que el cliente es dueño de la solicitud
            if (!solicitud.getClienteId().equals(decodedToken.getUid())) {
                throw new PresentationException("No autorizado", HttpStatus.FORBIDDEN);
            }

            return ResponseEntity.ok(solicitudService.marcarComoCompletado(id));

        } catch (FirebaseAuthException e) {
            throw new PresentationException("Error de autenticación", HttpStatus.UNAUTHORIZED);
        }
    }
}
