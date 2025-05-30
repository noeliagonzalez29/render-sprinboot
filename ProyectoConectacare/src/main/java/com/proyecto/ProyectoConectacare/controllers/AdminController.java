package com.proyecto.ProyectoConectacare.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.proyecto.ProyectoConectacare.dto.UsuarioCreadAdmDTO;
import com.proyecto.ProyectoConectacare.exception.PresentationException;
import com.proyecto.ProyectoConectacare.model.Rol;
import com.proyecto.ProyectoConectacare.model.Usuario;
import com.proyecto.ProyectoConectacare.service.AdminService;
import com.proyecto.ProyectoConectacare.service.AnuncioService;
import com.proyecto.ProyectoConectacare.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsable del control de las funciones administrativas relacionadas con la gestión de usuarios,
 * las estadísticas.
 */
@RestController
@RequestMapping("/admin")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final AdminService adminService;
    private final FirebaseAuth firebaseAuth;
    private final UsuarioService usuarioService;
    private final AnuncioService anuncioService;

    public AdminController(AdminService adminService, FirebaseAuth firebaseAuth, UsuarioService usuarioService, AnuncioService anuncioService) {
        this.adminService = adminService;
        this.anuncioService = anuncioService;
        this.firebaseAuth = firebaseAuth;
        this.usuarioService = usuarioService;
    }


    /**
     * Gestiona la solicitud para obtener una lista de todos los usuarios del sistema.
     * Este método verifica el token proporcionado para la autenticación y comprueba si el usuario solicitante tiene el rol de administrador (ADMINISTRADOR) necesario para acceder a la lista.
     *
     * @param token: el token de autorización proporcionado en el encabezado de la solicitud; debe
     * comenzar con "Bearer" seguido del token real.
     * @return: una ResponseEntity que contiene una lista de objetos "Usuario" si la autenticación y la autorización son exitosas.
     * @throws: FirebaseAuthException si hay un error durante la verificación del token.
     * @throws: PresentationException si se deniega el acceso debido a permisos insuficientes.
     */
    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> obtenerUsuarios(@RequestHeader("Authorization") String token) throws FirebaseAuthException {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);

        Usuario usuarioActual = usuarioService.getUsuarioById(decodedToken.getUid());


        if (usuarioActual == null || usuarioActual.getRol() != Rol.ADMINISTRADOR) {
            throw new PresentationException("Acceso denegado", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(usuarioService.getAllUsuarios());
    }

    /**
     * Elimina un usuario identificado por el ID proporcionado si la solicitud está autorizada por un usuario administrador. Este método verifica el token de autenticación de Firebase del usuario y comprueba si el solicitante tiene privilegios de administrador antes de eliminarlo del sistema.
     *
     * @param id: el identificador del usuario que se eliminará.
     * @param token: el token de autorización utilizado para validar y autorizar la solicitud.
     * @return: una {@link ResponseEntity} sin contenido si el usuario se elimina correctamente, o una {@link ResponseEntity} con las respuestas de error correspondientes si la solicitud falla.
     * @throws: FirebaseAuthException si no se puede verificar el token de Firebase.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable String id, @RequestHeader("Authorization") String token) throws FirebaseAuthException {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);

        Usuario usuarioActual = usuarioService.getUsuarioById(decodedToken.getUid());
        if (usuarioActual == null || usuarioActual.getRol() != Rol.ADMINISTRADOR) {
            throw new PresentationException("Acceso denegado", HttpStatus.FORBIDDEN);
        }
        // Obtener el usuario que se desea eliminar
        Usuario usuarioAEliminar = usuarioService.getUsuarioById(id);
        if (usuarioAEliminar == null) {
            return ResponseEntity.notFound().build();
        }
        // Primero eliminar del sistema de autenticación
        firebaseAuth.deleteUser(usuarioAEliminar.getId());
        usuarioService.deleteUsuario(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene estadísticas generales del sistema si el usuario autenticado tiene rol de administrador.
     *
     * @param token el token de autorización en el encabezado de la solicitud, que debe incluir el prefijo "Bearer ".
     * @return un mapa que contiene varias estadísticas, incluyendo ingresos totales, inicios de sesión, registros nuevos, y el número total de anuncios.
     * @throws FirebaseAuthException si hay un error al verificar el token de autenticación con Firebase.
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(@RequestHeader("Authorization") String token) throws FirebaseAuthException {

        if (token.startsWith("Bearer ")) token = token.substring(7);
        FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
        Usuario usuarioActual = usuarioService.getUsuarioById(decodedToken.getUid());
        if (usuarioActual == null || usuarioActual.getRol() != Rol.ADMINISTRADOR) {
            throw new PresentationException("Acceso denegado", HttpStatus.FORBIDDEN);
        }
        int inicios = adminService.contarInicioSesion();
        int registros = adminService.contarRegistros();
        int anuncios = anuncioService.contarTotalAnuncios();
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("iniciosSesion", inicios);    // Total de logins
        estadisticas.put("registros", registros);         // Nuevos registros
        estadisticas.put("totalAnuncios", anuncios);

        return ResponseEntity.ok(estadisticas);
    }


    /**
     * Obtiene el usuario actual a partir del token de autorización proporcionado.
     *
     * @param token el token de autorización en el encabezado "Authorization"
     * @return una ResponseEntity que contiene el usuario asociado al token. Si el token es inválido, se lanza una excepción.
     */
    @GetMapping("/yo")
    public ResponseEntity<Usuario> obtenerMiUsuario(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            FirebaseToken decoded = firebaseAuth.verifyIdToken(token);
            String uid = decoded.getUid();

            Usuario usuario = usuarioService.getUsuarioById(uid);
            return ResponseEntity.ok(usuario);
        } catch (FirebaseAuthException e) {
            throw new PresentationException("Token inválido", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/estadisticas/descargar-csv")
    public ResponseEntity<byte[]> descargarEstadisticasCSVBytes(
            @RequestHeader("Authorization") String token) throws FirebaseAuthException {

        logger.info("Solicitud recibida para descargar CSV de estadísticas (byte[]).");

        // 1. Autenticación y Autorización (Estilo Manual)
        if (token == null || !token.startsWith("Bearer ")) {
            throw new PresentationException("Token de autorización ausente o mal formado", HttpStatus.UNAUTHORIZED);
        }
        token = token.substring(7);
        FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
        Usuario usuarioActual = usuarioService.getUsuarioById(decodedToken.getUid());

        if (usuarioActual.getRol() != Rol.ADMINISTRADOR) {
            logger.warn("Intento de acceso no autorizado a descarga CSV por usuario: {}", usuarioActual.getEmail());
            throw new PresentationException("Acceso denegado. Se requiere rol de ADMINISTRADOR.", HttpStatus.FORBIDDEN);
        }
        logger.debug("Usuario administrador {} autorizado para descarga CSV.", usuarioActual.getEmail());

        // 2. Obtener la lista de usuarios
        List<Usuario> usuarios;
        try {
            usuarios = adminService.obtenerUsuarios();
            logger.info("Obtenidos {} usuarios para incluir en el CSV (byte[]).", usuarios.size());
        } catch (PresentationException e) {
            logger.error("Error (PresentationException) al obtener la lista de usuarios para el CSV: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al obtener usuarios para CSV: {}", e.getMessage(), e);
            throw new PresentationException("Error interno al obtener datos de usuarios.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {

            adminService.generarCSVBytes(usuarios);
            logger.info("Bytes del CSV generados correctamente ({} bytes).");
        } catch (PresentationException e) {
            // Error específico de negocio DENTRO del servicio generarCSVBytes
            logger.error("❌ Error (PresentationException) durante la generación del contenido CSV: {}", e.getMessage());
            throw e; // Relanzar para manejo global
        } catch (Exception e) {
            logger.error("❌ Error inesperado durante la generación del CSV como bytes: {}", e.getMessage(), e);
            throw new PresentationException("Error inesperado al generar el informe CSV.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 4. Preparar Encabezados HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        String fechaActual = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nombreArchivo = String.format("estadisticas_usuarios_%s.csv", fechaActual);
        headers.setContentDispositionFormData("attachment", nombreArchivo);

        logger.debug("Encabezados HTTP para descarga CSV (byte[]) configurados.");


        return new ResponseEntity<>( headers, HttpStatus.OK);
    }
@PostMapping("crear-usuario")
public ResponseEntity<Usuario> crearUsuario( @RequestBody UsuarioCreadAdmDTO usuarioNuevoDTO,
                                             @RequestHeader("Authorization") String token)throws FirebaseAuthException{

    if (token.startsWith("Bearer ")) {
        token = token.substring(7);
    }

    FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
    Usuario usuarioActual = usuarioService.getUsuarioById(decodedToken.getUid());

    if (usuarioActual == null || usuarioActual.getRol() != Rol.ADMINISTRADOR) {
        throw new PresentationException("Acceso denegado", HttpStatus.FORBIDDEN);
    }


    UserRecord.CreateRequest request = new UserRecord.CreateRequest()
            .setEmail(usuarioNuevoDTO.getEmail())
            .setPassword(usuarioNuevoDTO.getPassword());

    UserRecord userRecord = firebaseAuth.createUser(request);


    Usuario nuevoUsuario = new Usuario();
    nuevoUsuario.setId(userRecord.getUid());
    nuevoUsuario.setEmail(usuarioNuevoDTO.getEmail());
    nuevoUsuario.setNombre(usuarioNuevoDTO.getNombre());
    nuevoUsuario.setRol(usuarioNuevoDTO.getRol());

    usuarioService.createUsuario(nuevoUsuario);

    return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
}
}
