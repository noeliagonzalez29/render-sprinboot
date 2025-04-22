package com.proyecto.ProyectoConectacare.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class FirebaseFiltroAutenticacion extends OncePerRequestFilter {

    private final List<String> rutasPublicas = List.of(
            //"/usuarios/cliente",
           // "/usuarios/trabajador",
           // "/usuarios/trabajador/**",
            "/api/public",
            "/swagger-ui",
            "/swagger-resources",
            "/v3/api-docs",
            "/webjars"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (esRutaPublica(request)) {
            chain.doFilter(request, response);
            return;
        }

        String token = obtenerToken(request);
        if (token == null) {
            enviarError(response, "Token requerido");
            return;
        }

        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);

            String uid = decodedToken.getUid();
            String role = (String) decodedToken.getClaims().get("role");
            request.setAttribute("userId", uid); //para poder acceder al controlador
            request.setAttribute("userRole", role);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(uid, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);


            chain.doFilter(request, response);

        } catch (FirebaseAuthException e) {
            enviarError(response, "Token inválido: " + e.getMessage());
        }

    }

    private String obtenerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer "))
                ? header.substring(7)
                : null;
    }

    private boolean esRutaPublica(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        /*
        // Permitir rutas públicas por path
        boolean rutaCoincide = rutasPublicas.stream()
                .anyMatch(ruta -> path.startsWith(ruta));
        */
        // Permitir solo GET en rutas públicas (Swagger, etc.)
        boolean rutaPublicaGet = rutasPublicas.stream()
                .anyMatch(ruta -> path.startsWith(ruta) && "GET".equalsIgnoreCase(method));

        // Permitir POST/PUT en endpoints de registro (configurados en SecurityConfig)
        boolean esRegistro = path.startsWith("/usuarios/cliente") || path.startsWith("/usuarios/trabajador");

        return rutaPublicaGet || esRegistro;
    }

    private void enviarError(HttpServletResponse response, String mensaje) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + mensaje + "\"}");
    }
}