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

/**
 * Clase controladora para la gestión de solicitudes.
 * Proporciona puntos finales para crear, recuperar y actualizar solicitudes.
 * Con autenticación y validación a través de Firebase.
 */
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

    /**
     * Crea una nueva Solicitud y la asocia con el ID del trabajador autenticado.
     *
     * @param token: el token de autorización proporcionado en el encabezado de la solicitud, que se espera siga el formato del token de portador.
     * @param solicitud: el objeto de Solicitud que se creará, proporcionado en el cuerpo de la solicitud.
     * @return: una ResponseEntity que contiene el objeto de Solicitud creado y un estado HTTP CREADO.
     * @throws: PresentationException si el token proporcionado no es válido.
     */
    @PostMapping
    public ResponseEntity<Solicitud> crearSolicitud(@RequestHeader("Authorization") String token,@RequestBody Solicitud solicitud) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
            String trabajadorId = decodedToken.getUid();

            solicitud.setTrabajadorId(trabajadorId);
            solicitud = solicitudService.createSolicitud(solicitud);

            return new ResponseEntity<>(solicitud, HttpStatus.CREATED);

        } catch (FirebaseAuthException e) {
            throw new PresentationException("Token inválido", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Recupera una solicitud basándose en su identificador único.
     *
     * @param id el identificador único de la solicitud a recuperar.
     * @return una ResponseEntity que contiene la solicitud si se encuentra, o una respuesta de error apropiada si no se encuentra.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Solicitud> obtenerSolicitud(@PathVariable String id) {
        return ResponseEntity.ok(solicitudService.getSolicitudById(id));
    }

    /**
     * Recupera una lista de solicitudes asociadas a un anuncio específico.
     *
     * @param anuncioId el ID del anuncio para el que se recuperarán las solicitudes.
     * @return una lista de objetos Solicitud asociados al ID del anuncio especificado.
     */
    @GetMapping("/anuncio/{anuncioId}")
    public List<Solicitud> obtenerSolicitudesPorAnuncio(@PathVariable String anuncioId) {
        return solicitudService.getSolicitudesByAnuncioId(anuncioId);
    }

    /**
     * Recupera una lista de solicitudes asociadas a un ID de trabajador específico.
     *
     * @param trabajadorId el identificador único del trabajador cuyas solicitudes se recuperarán.
     * @return una lista de solicitudes asociadas al trabajador especificado.
     */
    @GetMapping("/trabajador/{trabajadorId}")
    public List<Solicitud> obtenerSolicitudesPorTrabajador(@PathVariable String trabajadorId) {
        return solicitudService.getSolicitudesByTrabajadorId(trabajadorId);
    }

    /**
     * Recupera una lista de solicitudes asociadas al trabajador autenticado según el token de autorización proporcionado.
     *
     * @param token: el token de autorización en el encabezado "Authorization". Debe ser un token Bearer.
     * @return ResponseEntity: contiene una lista de objetos Solicitud si el token es válido.
     * Se lanza una excepción si el token no es válido.
     */
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
    /**
     * Recupera una lista de solicitudes de trabajo relacionadas con los anuncios del cliente actual
     * según el token de autorización proporcionado.
     *
     * @param token: el token de autorización JWT con el prefijo "Bearer", que se utiliza
     * para autenticar al cliente y extraer su ID de usuario.
     * @return: una lista de objetos SolicitudConTrabajadorDTO que representan las solicitudes de trabajo
     * asociadas con los anuncios del cliente.
     * @throws: una excepción PresentationException si el token proporcionado no es válido o la autenticación falla.
     */
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

    /**
     * Actualiza el estado de una solicitud específica por su ID.
     *
     * @param id el identificador único de la solicitud que se actualizará
     * @param body un mapa que contiene el nuevo estado en la clave "estado"
     * @return una ResponseEntity que contiene el objeto de solicitud actualizado
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<Solicitud> actualizarEstado(@PathVariable String id, @RequestBody Map<String, String> body) {
        String estadoStr = body.get("estado");
        EstadoSolicitud nuevoEstado = EstadoSolicitud.valueOf(estadoStr);
        Solicitud actualizada = solicitudService.actualizarEstadoSolicitud(id, nuevoEstado);
        return ResponseEntity.ok(actualizada);
    }
    /**
     * Marca una solicitud específica como completada.
     *
     * @param id El identificador único de la solicitud que se marcará como completada.
     * @param token El token de autenticación proporcionado en el encabezado de la solicitud para verificar la identidad del usuario.
     * @return Una ResponseEntity que contiene el objeto "solicitud" actualizado si la operación se realiza correctamente.
     * @throws PresentationException Si hay un problema con la solicitud, como un error de autenticación, acceso no autorizado o datos no válidos.
     */
    @PutMapping("/{id}/completar")
    public ResponseEntity<Solicitud> marcarComoCompletado(
            @PathVariable String id,
            @RequestHeader("Authorization") String token) {

        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token.replace("Bearer ", ""));
            Solicitud solicitud = solicitudService.getSolicitudById(id);

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
