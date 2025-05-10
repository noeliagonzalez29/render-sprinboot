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
import jakarta.validation.Valid;
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
     * @param token Token de autorización con el formato "Bearer <token>" utilizado para validar
     * e identificar al usuario en Firebase.
     * @return Una ResponseEntity que contiene el objeto Usuario creado y el código de estado HTTP 201 (CREATED),
     * o una excepción con el estado HTTP correspondiente en caso de errores durante el registro.
     */
    @PostMapping("/cliente")
    public ResponseEntity<Usuario> registrarCliente(@Valid @RequestBody ClienteDTO clienteDTO,  @RequestHeader("Authorization") String token)  {

        try {

            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            // Verificar el token y extraer el UID.
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
            String uid = decodedToken.getUid();
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
        } catch (FirebaseAuthException e) {

            if (e.getErrorCode().equals("email-already-exists")) {
                throw new PresentationException("El email ya está registrado", HttpStatus.BAD_REQUEST);
            }
            throw new PresentationException("Error al registrar usuario", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gestiona el registro de un nuevo usuario "Trabajador". Extrae y verifica
     * el token de Firebase proporcionado, crea el objeto Usuario correspondiente y
     * lo guarda en la base de datos.
     *
     * @param trabajadorDTO El objeto de transferencia de datos que contiene los datos del usuario, como nombre, correo electrónico,
     * disponibilidad, estudios, habilidades y experiencia.
     * @param token El token de autorización proporcionado en el encabezado de la solicitud HTTP, utilizado para verificar la sesión del usuario y recuperar el UID de Firebase.
     * @return Una ResponseEntity que contiene el objeto Usuario creado y un estado HTTP de CREADO.
     * Si se produce algún error durante el proceso, se genera una excepción con el código de estado correspondiente.
     */
    @PostMapping("/trabajador")
    public ResponseEntity<Usuario> registrarTrabajador(@Valid @RequestBody TrabajadorDTO trabajadorDTO, @RequestHeader("Authorization") String token){

        try {
            // 1. Extraer el token y quitar "Bearer " si es necesario.
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            // 2. Verificar el token y extraer el UID.
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
            String uid = decodedToken.getUid();

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
        } catch (FirebaseAuthException e) {
            if (e.getErrorCode().equals("email-already-exists")) {
                throw new PresentationException("El email ya está registrado", HttpStatus.BAD_REQUEST);
            }
            throw new PresentationException("Error al registrar usuario", HttpStatus.INTERNAL_SERVER_ERROR);
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
                    "USER_NOT_FOUND".equalsIgnoreCase(String.valueOf(e.getErrorCode()))) { // Algunas variaciones comunes
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
