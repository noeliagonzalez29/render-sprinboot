package com.proyecto.ProyectoConectacare.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.proyecto.ProyectoConectacare.exception.PresentationException;
import com.proyecto.ProyectoConectacare.model.Evaluacion;
import com.proyecto.ProyectoConectacare.model.Solicitud;
import com.proyecto.ProyectoConectacare.service.EvaluacionService;
import com.proyecto.ProyectoConectacare.service.SolicitudService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/evaluaciones")
public class EvaluacionController {
    private final EvaluacionService evaluacionService;
    private final FirebaseAuth firebaseAuth;
    private final SolicitudService solicitudService;
    public EvaluacionController(EvaluacionService evaluacionService, FirebaseAuth firebaseAuth, SolicitudService solicitudService) {
        this.evaluacionService = evaluacionService;
        this.firebaseAuth = firebaseAuth;
        this.solicitudService = solicitudService;
    }

    @PostMapping
    public ResponseEntity<Evaluacion> crearEvaluacion(
            @RequestBody Evaluacion evaluacion,
            @RequestHeader("Authorization") String token) {

        // Verificar autenticación
        FirebaseToken decodedToken = null;
        try {
            decodedToken = firebaseAuth.verifyIdToken(token.replace("Bearer ", ""));
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }

        // Obtener la solicitud relacionada
        Solicitud solicitud = solicitudService.getSolicitudById(evaluacion.getSolicitudId());

        // Validaciones
        if (!solicitud.getClienteId().equals(decodedToken.getUid())) {
            throw new PresentationException("No autorizado", HttpStatus.FORBIDDEN);
        }

        if (!solicitud.isCompletado()) {
            throw new PresentationException("El trabajo debe estar completado primero", HttpStatus.BAD_REQUEST);
        }

        evaluacion.setClienteId(decodedToken.getUid());
        evaluacion.setTrabajadorId(solicitud.getTrabajadorId());

        return new ResponseEntity<>(evaluacionService.createEvaluacion(evaluacion), HttpStatus.CREATED);
    }
    @GetMapping("/{id}")
    public Evaluacion obtenerEvaluacion(@PathVariable String id) {
        return evaluacionService.getEvaluacionById(id);
    }

    @GetMapping("/trabajador/{trabajadorId}")
    public List<Evaluacion> obtenerEvaluacionesTrabajador(@PathVariable String trabajadorId) {
        return evaluacionService.getEvaluacionesByTrabajadorId(trabajadorId);
    }
    @GetMapping("/mias")
    public ResponseEntity<List<Evaluacion>> obtenerMisEvaluaciones(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            FirebaseToken decoded = firebaseAuth.verifyIdToken(token);
            String trabajadorId = decoded.getUid();

            List<Evaluacion> evaluaciones = evaluacionService.getEvaluacionesByTrabajadorId(trabajadorId);
            return new ResponseEntity<>(evaluaciones, HttpStatus.OK);
        } catch (FirebaseAuthException e) {
            throw new PresentationException("Token inválido", HttpStatus.UNAUTHORIZED);
        }
    }

}
