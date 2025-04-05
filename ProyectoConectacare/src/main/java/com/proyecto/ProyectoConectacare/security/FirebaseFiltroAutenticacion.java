package com.proyecto.ProyectoConectacare.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class FirebaseFiltroAutenticacion extends OncePerRequestFilter {

    private final List<String> rutasPublicas = List.of(
            "/api/public",
            "/api/auth/registro",  // Ruta de registro pública
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
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
            request.setAttribute("userId", decodedToken.getUid()); //para poder acceder al controlador
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(decodedToken.getUid(), null, Collections.emptyList());
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
        return rutasPublicas.stream()
                .anyMatch(ruta -> request.getRequestURI().startsWith(ruta));
    }

    private void enviarError(HttpServletResponse response, String mensaje) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + mensaje + "\"}");
    }
}