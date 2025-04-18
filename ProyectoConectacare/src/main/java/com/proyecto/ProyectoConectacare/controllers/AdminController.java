package com.proyecto.ProyectoConectacare.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.proyecto.ProyectoConectacare.exception.PresentationException;
import com.proyecto.ProyectoConectacare.model.Rol;
import com.proyecto.ProyectoConectacare.model.Usuario;
import com.proyecto.ProyectoConectacare.service.AdminService;
import com.proyecto.ProyectoConectacare.service.AnuncioService;
import com.proyecto.ProyectoConectacare.service.AuditService;
import com.proyecto.ProyectoConectacare.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final FirebaseAuth firebaseAuth;
    private final UsuarioService usuarioService;
    private final AnuncioService anuncioService;

    public AdminController(AdminService adminService ,FirebaseAuth firebaseAuth,UsuarioService usuarioService, AnuncioService anuncioService) {
        this.adminService = adminService;
        this.anuncioService = anuncioService;
        this.firebaseAuth = firebaseAuth;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> obtenerUsuarios(@RequestHeader("Authorization") String token) throws FirebaseAuthException {
        if (token.startsWith("Bearer ")) {
            token=  token.substring(7);
        }
        FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);

        Usuario usuarioActual = usuarioService.getUsuarioById(decodedToken.getUid());


        if (usuarioActual == null || usuarioActual.getRol() != Rol.ADMINISTRADOR) {
            throw new PresentationException("Acceso denegado", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(usuarioService.getAllUsuarios());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable String id, @RequestHeader("Authorization") String token) throws FirebaseAuthException {
        if (token.startsWith("Bearer ")) {
            token=  token.substring(7);
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
    @GetMapping("/estadisticas")
    public Map<String, Integer> obtenerEstadisticas(@RequestHeader("Authorization") String token)throws FirebaseAuthException {

        if (token.startsWith("Bearer ")) token = token.substring(7);
        FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
        Usuario usuarioActual = usuarioService.getUsuarioById(decodedToken.getUid());
        if (usuarioActual == null || usuarioActual.getRol() != Rol.ADMINISTRADOR) {
            throw new PresentationException("Acceso denegado", HttpStatus.FORBIDDEN);
        }
        Map<String, Integer> estadisticas = new HashMap<>();
        estadisticas.put("ingresos", 120);         // Por ejemplo, ingresos en total
        estadisticas.put("iniciosSesion", 450);    // Total de logins
        estadisticas.put("registros", 80);         // Nuevos registros
        estadisticas.put("totalAnuncios", anuncioService.contarTotalAnuncios());

        return estadisticas;
    }



    @GetMapping("/yo")
    public ResponseEntity<Usuario> obtenerMiUsuario(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            FirebaseToken decoded = firebaseAuth.verifyIdToken(token);
            String uid = decoded.getUid();
            System.out.println("🔐 UID Firebase al login: " + uid);
            Usuario usuario = usuarioService.getUsuarioById(uid);
            return ResponseEntity.ok(usuario);
        } catch (FirebaseAuthException e) {
            throw new PresentationException("Token inválido", HttpStatus.UNAUTHORIZED);
        }
    }

}
