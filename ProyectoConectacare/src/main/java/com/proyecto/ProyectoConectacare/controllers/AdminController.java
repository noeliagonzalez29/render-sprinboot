package com.proyecto.ProyectoConectacare.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.proyecto.ProyectoConectacare.exception.PresentationException;
import com.proyecto.ProyectoConectacare.model.Rol;
import com.proyecto.ProyectoConectacare.model.Usuario;
import com.proyecto.ProyectoConectacare.service.AdminService;
import com.proyecto.ProyectoConectacare.service.AuditService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final AuditService auditService;
    private final FirebaseAuth firebaseAuth;

    public AdminController(AdminService adminService, AuditService auditService, FirebaseAuth firebaseAuth) {
        this.adminService = adminService;
        this.auditService = auditService;
        this.firebaseAuth = firebaseAuth;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> obtenerUsuarios(@RequestHeader("Authorization") String token) throws FirebaseAuthException {
        // Validar token y que el usuario sea admin
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
        Usuario usuarioActual = adminService.obtenerUsuarios().stream()
                .filter(u -> u.getId().equals(decodedToken.getUid()))
                .findFirst()
                .orElse(null);
        if (usuarioActual == null || usuarioActual.getRol() != Rol.ADMINISTRADOR) {
            throw new PresentationException("Acceso denegado", HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(adminService.obtenerUsuarios());
    }

    @DeleteMapping("/usuario/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable String id, @RequestHeader("Authorization") String token) throws FirebaseAuthException {
        // Validar que el usuario autenticado es admin
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
        Usuario usuarioActual = adminService.obtenerUsuarios().stream()
                .filter(u -> u.getId().equals(decodedToken.getUid()))
                .findFirst()
                .orElse(null);
        if (usuarioActual == null || usuarioActual.getRol() != Rol.ADMINISTRADOR) {
            throw new PresentationException("Acceso denegado", HttpStatus.FORBIDDEN);
        }

        adminService.eliminarUsuario(id);
        // Registrar la acción
        auditService.registrarAccion(usuarioActual.getId(), "ELIMINAR_USUARIO", "Eliminó el usuario con ID: " + id);
        return ResponseEntity.noContent().build();
    }
}
