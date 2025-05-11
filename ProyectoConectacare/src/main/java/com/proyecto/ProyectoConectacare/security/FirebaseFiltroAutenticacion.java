package com.proyecto.ProyectoConectacare.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.proyecto.ProyectoConectacare.model.Usuario;
import com.proyecto.ProyectoConectacare.service.UsuarioService;
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
import java.util.Optional;

/**
 * La clase FirebaseFiltroAutenticacion es un filtro personalizado que extiende OncePerRequestFilter para
 * gestionar la autenticación basada en Firebase en una configuración de Spring Security.
 *
 * Responsabilidades:
 * - Valida las solicitudes HTTP entrantes comprobando si requieren autenticación.
 * - Omite la autenticación en rutas públicas predefinidas.
 * - Extrae el token de Firebase del encabezado de autorización.
 * - Decodifica y verifica el token de Firebase mediante FirebaseAuth para autenticar a los usuarios.
 * - Configura SecurityContext con los datos del usuario para su posterior procesamiento en endpoints seguros.
 *
 * Características principales:
 * - Rutas públicas: Se permiten solicitudes a endpoints específicos, como la documentación de Swagger o el registro de usuarios.
 * Se permiten endpoints sin autenticación.
 * - Autenticación de Firebase: Las solicitudes con un token de Firebase válido se autentican y los datos del usuario (por ejemplo, ID de usuario, rol) se extraen del token y se configuran en los atributos de la solicitud.
 * * - Contexto de seguridad: Tras una autenticación exitosa, el SecurityContextHolder se rellena
 * con un UsernamePasswordAuthenticationToken que contiene el UID (identificador único) del usuario.
 * - Gestión de errores personalizada: Los tokens inválidos o faltantes generan una respuesta no autorizada con un
 * mensaje de error JSON.
 *
 * Notas:
 * - La clase utiliza FirebaseAuth (inicializado en FirebaseConfig) para la verificación de tokens.
 * - Las rutas consideradas públicas se configuran en la lista rutasPublicas y en el método esRutaPublica.
 * - Las solicitudes sin el encabezado Authorization o con tokens inválidos se rechazan inmediatamente
 * con un mensaje de error correspondiente.
 *
 * Uso:
 * - Se usa habitualmente en la configuración de la cadena de filtros de Spring Security.
 */
@Component
public class FirebaseFiltroAutenticacion extends OncePerRequestFilter {

    private final List<String> rutasPublicas = List.of(
            "/api/public",
            "/swagger-ui",
            "/swagger-resources",
            "/v3/api-docs",
            "/webjars",
            "/usuarios/email-existe"
    );
    private static final String RUTA_REGISTRO_CLIENTE = "/usuarios/cliente";
    private static final String RUTA_REGISTRO_TRABAJADOR = "/usuarios/trabajador";
    private final UsuarioService usuarioService;

    public FirebaseFiltroAutenticacion(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {String path = request.getRequestURI().substring(request.getContextPath().length());
        String method = request.getMethod();

        if (esRutaPublica(request)) {
            chain.doFilter(request, response);
            return;
        }

        String token = obtenerToken(request);
        if (token == null) {
            enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token requerido");
            return;
        }

        try {
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(token);
            String uid = decoded.getUid();
            request.setAttribute("firebaseUserId", uid);
            boolean esRutaDeCreacionDePerfil =
                    (RUTA_REGISTRO_CLIENTE.equals(path) && "POST".equalsIgnoreCase(method)) ||
                            (RUTA_REGISTRO_TRABAJADOR.equals(path) && "POST".equalsIgnoreCase(method));

            if (esRutaDeCreacionDePerfil) {
                // Para la creación de perfil, solo necesitamos que el token de Firebase sea válido.
                // No necesitamos que el usuario ya exista en nuestra BD.
                // Creamos una autenticación simple con el UID y un rol genérico o sin roles si aún no se define.
                // O, si el rol viene en los claims del token de Firebase (si lo configuras), podrías usarlo.
                // Por ahora, solo autenticamos el UID.
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(uid, null, Collections.emptyList()); // Sin roles específicos aquí aún
                SecurityContextHolder.getContext().setAuthentication(authToken);
                chain.doFilter(request, response);
                return; // Importante: salimos después de manejar el caso de creación de perfil
            }
            Optional<Usuario> optUsuario = Optional.ofNullable(usuarioService.getUsuarioById(uid));
            if (optUsuario.isEmpty()) {
                enviarError(response, HttpServletResponse.SC_FORBIDDEN, "Usuario no encontrado");
                return;
            }
            Usuario usuario = optUsuario.get();

            // Obtener rol desde la entidad
            String rolName = usuario.getRol().name();
            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + rolName);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(uid, null, List.of(authority));

            // Atributos en request
            request.setAttribute("userId", uid);
            request.setAttribute("userRole", rolName);

            SecurityContextHolder.getContext().setAuthentication(authToken);
            chain.doFilter(request, response);

        } catch (FirebaseAuthException e) {
            enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token inválido: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado en el filtro de autenticación de Firebase: ", e);
            enviarError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno del servidor durante la autenticación");
        }
    }

    private String obtenerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private boolean esRutaPublica(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        boolean rutaPublicaGet = rutasPublicas.stream()
                .anyMatch(ruta -> path.startsWith(ruta) && "GET".equalsIgnoreCase(method));

        boolean esRegistro = path.startsWith("/usuarios/cliente") || path.startsWith("/usuarios/trabajador");
        return rutaPublicaGet || esRegistro;
    }

    private void enviarError(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + mensaje + "\"}");
    }
}