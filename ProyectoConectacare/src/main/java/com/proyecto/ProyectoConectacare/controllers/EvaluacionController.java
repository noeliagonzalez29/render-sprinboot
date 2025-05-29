package com.proyecto.ProyectoConectacare.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.proyecto.ProyectoConectacare.dto.EvaluacionDTO;
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

/**
 * Este controlador gestiona las solicitudes HTTP relacionadas con la gestión de las entidades "Evaluación".
 * Proporciona puntos finales para crear, obtener y listar evaluaciones.
 * La autenticación y la autorización se aplican mediante la verificación de tokens de Firebase.
 */
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
    /**
     * Crea una nueva evaluación para una solicitud de trabajo completada, garantizando que el usuario esté autenticado
     * y autorizado para evaluar el trabajo correspondiente.
     *
     * @param evaluacion: el objeto de evaluación que contiene los detalles que se crearán.
     * @param token: el token de autorización del usuario que envía la solicitud.
     * @return: una ResponseEntity que contiene la evaluación creada y el código de estado HTTP.
     */
    @PostMapping
    public ResponseEntity<Evaluacion> crearEvaluacion(
            @RequestBody Evaluacion evaluacion,
            @RequestHeader("Authorization") String token) {

        // Verificar autenticación
        FirebaseToken decodedToken = null;
        try {
            decodedToken = firebaseAuth.verifyIdToken(token.replace("Bearer ", ""));
        } catch (FirebaseAuthException e) {
           // throw new RuntimeException(e);
            throw new PresentationException("Token inválido o expirado", HttpStatus.UNAUTHORIZED);
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

        Evaluacion evaluacionCreada = evaluacionService.createEvaluacion(evaluacion);
        return new ResponseEntity<>(evaluacionCreada, HttpStatus.CREATED);
    }
    /**
     * Recupera una evaluación por su identificador único.
     *
     * @param id el identificador único de la evaluación a recuperar
     * @return la evaluación correspondiente al identificador proporcionado
     */
    @GetMapping("/{id}")
    public Evaluacion obtenerEvaluacion(@PathVariable String id) {
        return evaluacionService.getEvaluacionById(id);
    }

    /**
     * Recupera la lista de evaluaciones de un trabajador específico según su identificador único.
     *
     * @param trabajadorId: el identificador único del trabajador cuyas evaluaciones se recuperarán.
     * @return: una lista de evaluaciones asociadas con el trabajador especificado.
     */
    @GetMapping("/trabajador/{trabajadorId}")
    public List<EvaluacionDTO> obtenerEvaluacionesTrabajador(@PathVariable String trabajadorId) {
        return evaluacionService.getEvaluacionesByTrabajadorId(trabajadorId);
    }
    /**
     * Recupera la lista de evaluaciones asociadas al trabajador autenticado.
     * Utiliza el token de autorización para determinar la identidad del trabajador autenticado.
     *
     * @param token: el token de autorización proporcionado por el usuario, generalmente con el formato "Bearer <token>"
     * @return: una ResponseEntity que contiene la lista de evaluaciones vinculadas al trabajador autenticado y el código de estado HTTP
     * @throws: PresentationException si el token no es válido o no está autorizado
     */
    @GetMapping("/mias")
    public ResponseEntity<List<EvaluacionDTO>> obtenerMisEvaluaciones(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            FirebaseToken decoded = firebaseAuth.verifyIdToken(token);
            String trabajadorId = decoded.getUid();

            List<EvaluacionDTO> evaluaciones = evaluacionService.getEvaluacionesByTrabajadorId(trabajadorId);
            return new ResponseEntity<>(evaluaciones, HttpStatus.OK);
        } catch (FirebaseAuthException e) {
            throw new PresentationException("Token inválido", HttpStatus.UNAUTHORIZED);
        }
    }
    @GetMapping("/evaluada/{solicitudId}")
    public ResponseEntity<Boolean> estaEvaluada(@PathVariable String solicitudId) {
        boolean evaluada = evaluacionService.existeEvaluacionPorSolicitud(solicitudId);
        return ResponseEntity.ok(evaluada);
    }

}
