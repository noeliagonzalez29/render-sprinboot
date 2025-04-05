package com.proyecto.ProyectoConectacare.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.proyecto.ProyectoConectacare.dto.ClienteDTO;
import com.proyecto.ProyectoConectacare.dto.TrabajadorDTO;
import com.proyecto.ProyectoConectacare.exception.PresentationException;
import com.proyecto.ProyectoConectacare.model.Rol;
import com.proyecto.ProyectoConectacare.model.Usuario;
import com.proyecto.ProyectoConectacare.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuariosController {
    private final UsuarioService usuarioService;
    private final FirebaseAuth firebaseAuth;

    public UsuariosController(UsuarioService usuarioService, FirebaseAuth firebaseAuth) {
        this.usuarioService = usuarioService;
        this.firebaseAuth = firebaseAuth;
    }


    @PostMapping("/cliente")
    public ResponseEntity<Usuario> registrarCliente(@Valid @RequestBody ClienteDTO clienteDTO)  {

        // 1. Crear usuario en Firebase Authentication
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(clienteDTO.getEmail())
                .setPassword(clienteDTO.getPassword());

        try {
            UserRecord userRecord = firebaseAuth.createUser(request);
            // 2. Crear objeto Usuario para Firestore
            Usuario usuario = new Usuario();
            usuario.setId(userRecord.getUid());
            usuario.setEmail(userRecord.getEmail());
            usuario.setRol(Rol.CLIENTE);
            usuario.setNombre(clienteDTO.getNombre());
            usuario.setApellido(clienteDTO.getApellido());
            usuario.setDireccion(clienteDTO.getDireccion());
            usuario.setNecesidades(clienteDTO.getNecesidades());

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

    @PostMapping("/trabajador")
    public ResponseEntity<Usuario> registrarTrabajador(@Valid @RequestBody TrabajadorDTO trabajadorDTO){

        // 1. Crear usuario en Firebase Authentication
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(trabajadorDTO.getEmail())
                .setPassword(trabajadorDTO.getPassword());

        try {
            UserRecord userRecord = firebaseAuth.createUser(request);
            // 2. Crear objeto Usuario para Firestore
            Usuario usuario = new Usuario();
            usuario.setId(userRecord.getUid());
            usuario.setEmail(userRecord.getEmail());
            usuario.setRol(Rol.TRABAJADOR);
            usuario.setNombre(trabajadorDTO.getNombre());
            usuario.setApellido(trabajadorDTO.getApellido());
            usuario.setDisponibilidad(trabajadorDTO.getDisponibilidad());
            usuario.setEstudios(trabajadorDTO.getEstudios());
            usuario.setHabilidades(trabajadorDTO.getHabilidades());

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

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable String id) {
        Usuario usuario = usuarioService.getUsuarioById(id);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioService.getAllUsuarios();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(
            @PathVariable String id,
            @RequestBody Map<String, Object> updates,
            @RequestHeader("Authorization") String token) throws FirebaseAuthException {

        // Validar token y que el usuario actualice solo su perfil
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
        if (!decodedToken.getUid().equals(id)) {
            throw new PresentationException("No autorizado", HttpStatus.FORBIDDEN);
        }

        // Actualizar solo campos permitidos según el rol
        Usuario usuarioActualizado = usuarioService.updateUsuario(id, updates);
        return ResponseEntity.ok(usuarioActualizado);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable String id) {
        usuarioService.deleteUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
