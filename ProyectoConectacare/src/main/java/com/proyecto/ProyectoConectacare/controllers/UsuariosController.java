package com.proyecto.ProyectoConectacare.controllers;

import com.google.firebase.auth.*;
import com.proyecto.ProyectoConectacare.dto.ClienteDTO;
import com.proyecto.ProyectoConectacare.dto.TrabajadorDTO;
import com.proyecto.ProyectoConectacare.exception.PresentationException;
import com.proyecto.ProyectoConectacare.model.Evento;
import com.proyecto.ProyectoConectacare.model.Rol;
import com.proyecto.ProyectoConectacare.model.Usuario;
import com.proyecto.ProyectoConectacare.service.LogEstadisticaService;
import com.proyecto.ProyectoConectacare.service.UsuarioService;
import com.proyecto.ProyectoConectacare.service.impl.AdminServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar operaciones relacionadas con los usuarios, como el registro, la recuperación y la actualización de usuarios en el sistema. Gestiona los roles de usuario Cliente y Trabajador.
 * Utiliza Firebase para la autenticación y Firestore para el almacenamiento de datos.
 */
@RestController
@RequestMapping("/usuarios")
public class UsuariosController {
    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);
    private final UsuarioService usuarioService;
    private final FirebaseAuth firebaseAuth;
    private final LogEstadisticaService logEstadisticasService;

    public UsuariosController(UsuarioService usuarioService, FirebaseAuth firebaseAuth, LogEstadisticaService logEstadisticasService) {
        this.usuarioService = usuarioService;
        this.firebaseAuth = firebaseAuth;
        this.logEstadisticasService = logEstadisticasService;
    }


    /**
     * Registra un nuevo cliente utilizando los datos del cliente y el token de autorización proporcionados.
     * El método extrae y valida el token de Firebase proporcionado, crea un nuevo usuario
     * en la base de datos de Firestore y le asigna el rol CLIENTE.
     *
     * @param clienteDTO Objeto que contiene los datos del cliente, como nombre, correo electrónico, dirección, etc.
     *
     * @return Una ResponseEntity que contiene el objeto Usuario creado y el código de estado HTTP 201 (CREATED),
     * o una excepción con el estado HTTP correspondiente en caso de errores durante el registro.
     */
    @PostMapping("/cliente")
    public ResponseEntity<Usuario> registrarCliente(@Valid @RequestBody ClienteDTO clienteDTO,  HttpServletRequest request)  {

        try {

            String uid = (String) request.getAttribute("firebaseUserId"); // UID verificado por el filtro

            if (uid == null) {
                // Esto no debería pasar si el filtro funcionó y la ruta es autenticada
                throw new PresentationException("UID de Firebase no encontrado en la solicitud", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Verificar si ya existe un usuario con este UID en tu BD (doble chequeo o si permites actualizar)
            if (usuarioService.getUsuarioById(uid) != null) {
                throw new PresentationException("El perfil para este usuario (UID) ya existe en nuestros sistemas.", HttpStatus.CONFLICT);
            }

            // Crear objeto Usuario para Firestore usando el UID obtenido.
            Usuario usuario = new Usuario();
            usuario.setId(uid);
            usuario.setEmail(clienteDTO.getEmail());
            usuario.setRol(Rol.CLIENTE);
            usuario.setNombre(clienteDTO.getNombre());
            usuario.setApellido(clienteDTO.getApellido());
            usuario.setDireccion(clienteDTO.getDireccion());
            usuario.setNecesidades(clienteDTO.getNecesidades());

            //  Guardar en Firestore
            Usuario usuarioGuardado = usuarioService.createUsuario(usuario);
            return new ResponseEntity<>(usuarioGuardado, HttpStatus.CREATED);
        } catch (PresentationException e) {

            throw e;
        }catch (Exception e) { // Captura general para otros errores inesperados

            logger.error("Error inesperado al registrar cliente: ", e);
            throw new PresentationException("Error interno al registrar el perfil del cliente", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gestiona el registro de un nuevo usuario "Trabajador". Extrae y verifica
     * el token de Firebase proporcionado, crea el objeto Usuario correspondiente y
     * lo guarda en la base de datos.
     *
     * @param trabajadorDTO El objeto de transferencia de datos que contiene los datos del usuario, como nombre, correo electrónico,
     * disponibilidad, estudios, habilidades y experiencia
     * @return Una ResponseEntity que contiene el objeto Usuario creado y un estado HTTP de CREADO.
     * Si se produce algún error durante el proceso, se genera una excepción con el código de estado correspondiente.
     */
    @PostMapping("/trabajador")
    public ResponseEntity<Usuario> registrarTrabajador(@Valid @RequestBody TrabajadorDTO trabajadorDTO,  HttpServletRequest request){

        try {
            String uid = (String) request.getAttribute("firebaseUserId");
            if (uid == null) {
                throw new PresentationException("UID de Firebase no encontrado en la solicitud", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (usuarioService.getUsuarioById(uid) != null) {
                throw new PresentationException("El perfil para este usuario (UID) ya existe en nuestros sistemas.", HttpStatus.CONFLICT);
            }

            // 2. Crear objeto Usuario para Firestore
            Usuario usuario = new Usuario();
            usuario.setId(uid);
            usuario.setEmail(trabajadorDTO.getEmail());
            usuario.setRol(Rol.TRABAJADOR);
            usuario.setNombre(trabajadorDTO.getNombre());
            usuario.setApellido(trabajadorDTO.getApellido());
            usuario.setDisponibilidad(trabajadorDTO.getDisponibilidad());
            usuario.setEstudios(trabajadorDTO.getEstudios());
            usuario.setHabilidades(trabajadorDTO.getHabilidades());
            usuario.setExperiencia(trabajadorDTO.getExperiencia());


            // 3. Guardar en Firestore
            Usuario usuarioGuardado = usuarioService.createUsuario(usuario);
            return new ResponseEntity<>(usuarioGuardado, HttpStatus.CREATED);
        } catch (PresentationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al registrar trabajador: ", e);
            throw new PresentationException("Error interno al registrar el perfil del trabajador", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Recupera un usuario por su identificador único.
     *
     * @param id el identificador único del usuario a recuperar
     * @return una ResponseEntity que contiene el usuario si se encuentra, o un estado HTTP apropiado si no
     */
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable String id) {
        Usuario usuario = usuarioService.getUsuarioById(id);

        return ResponseEntity.ok(usuario);
    }

    /**
     * Actualiza la información del usuario según el ID proporcionado y los campos de actualización.
     *
     * Este método valida el token de autorización y garantiza que un usuario solo pueda
     * actualizar su propio perfil. También restringe las actualizaciones a campos específicos
     * según el rol del usuario.
     *
     * @param id El identificador único del usuario que se actualizará.
     * @param updates Un mapa que contiene los campos y valores que se actualizarán para el usuario.
     * @param token El token de autorización del encabezado de la solicitud.
     * @return Una ResponseEntity que contiene la información actualizada del usuario.
     * @throws FirebaseAuthException Si hay un error al validar el token.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(
            @PathVariable String id,
            @RequestBody Map<String, Object> updates, @RequestHeader("Authorization") String token
            ) throws FirebaseAuthException {


        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
        if (!decodedToken.getUid().equals(id)) {
            throw new PresentationException("No autorizado", HttpStatus.FORBIDDEN);
        }

        // Actualizar solo campos permitidos según el rol
        Usuario usuarioActualizado = usuarioService.updateUsuario(id, updates);
        return ResponseEntity.ok(usuarioActualizado);
    }


    /**
     * Recupera la información del usuario actual basándose en el token de autorización proporcionado.
     *
     * @param token: el token de autorización del encabezado de la solicitud, utilizado para verificar e identificar al usuario.
     * @return: una ResponseEntity que contiene la información del usuario si el token es válido.
     * @throws: PresentationException si el token no es válido o el proceso de verificación falla.
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

            logEstadisticasService.registrarEvento(usuario, Evento.INICIO_SESION);
            return ResponseEntity.ok(usuario);
        } catch (FirebaseAuthException e) {
            throw new PresentationException("Token inválido", HttpStatus.UNAUTHORIZED);
        }
    }
    /**
     * Verifica si un correo electrónico ya está registrado en Firebase Authentication.
     *
     * @param email Correo electrónico a verificar
     * @return true si ya existe, false si no
     */

    @GetMapping("/email-existe") // Endpoint ahora público
    public ResponseEntity<Map<String, Object>> verificarEmail(@RequestParam String email) {
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
            // Si no lanza excepción, el email existe
            return ResponseEntity.ok(Map.of("existe", true));
        } catch (FirebaseAuthException e) {
            // Comprueba los códigos de error exactos que devuelve tu versión del SDK de Admin de Firebase
            if ("NOT_FOUND".equalsIgnoreCase(String.valueOf(e.getErrorCode())) ||
                    "user-not-found".equalsIgnoreCase(String.valueOf(e.getErrorCode())) ||
                    "USER_NOT_FOUND".equalsIgnoreCase(String.valueOf(e.getErrorCode()))) {
                // Email no existe
                return ResponseEntity.ok(Map.of("existe", false));
            }
            // Otro error de Firebase al intentar buscar el email
            System.err.println("Error verificando email con Firebase Admin SDK: " + e.getMessage() + " (Código: " + e.getErrorCode() + ")");
            // Devolver un error genérico para no exponer detalles internos
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", true, "mensaje", "Error al verificar el email"));
        } catch (Exception e) {
            // Captura cualquier otra excepción inesperada
            System.err.println("Error inesperado verificando email: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

}
