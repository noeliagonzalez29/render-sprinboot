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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(FirebaseFiltroAutenticacion.class); // Usa un logger

    // Lista de prefijos para rutas públicas generales (docs, etc.)
    private final List<String> PREFIJOS_RUTAS_PUBLICAS_GENERALES = List.of(
            "/api/public",
            "/swagger-ui",
            "/swagger-resources",
            "/v3/api-docs",
            "/webjars"
    );

    // Rutas específicas y sus métodos que deben ser públicos para ESTE FILTRO
    private static final String RUTA_VERIFICAR_EMAIL = "/usuarios/email-existe"; // Asume que está bajo el context path /api si tienes uno global

    // Rutas de creación de perfil (SÍ necesitan token de Firebase, pero el usuario puede no existir aún en BD local)
    private static final String RUTA_REGISTRO_CLIENTE = "/usuarios/cliente";
    private static final String RUTA_REGISTRO_TRABAJADOR = "/usuarios/trabajador";

    private final UsuarioService usuarioService;

    public FirebaseFiltroAutenticacion(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        String path = request.getRequestURI().substring(request.getContextPath().length());


        String method = request.getMethod();

        // 1. Manejar rutas públicas que no necesitan token de Firebase
        if (esRutaPublica(path, method)) {
            logger.debug("Ruta pública (filtro Firebase): {} {}");
            chain.doFilter(request, response);
            return;
        }

        // 2. Para todas las demás rutas, se espera un token de Firebase
        String token = obtenerToken(request);
        if (token == null) {
            logger.warn("Token de Firebase no encontrado en la cabecera Authorization para: {} {}");
            enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token de autenticación de Firebase requerido");
            return;
        }

        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            String uid = decodedToken.getUid();
            String emailFromToken = decodedToken.getEmail(); // Email del token

            // Poner información del token en el request para los controladores
            request.setAttribute("firebaseUserId", uid);
            request.setAttribute("firebaseUserEmail", emailFromToken);

            boolean esRutaDeCreacionDePerfil =
                    (RUTA_REGISTRO_CLIENTE.equals(path) && "POST".equalsIgnoreCase(method)) ||
                            (RUTA_REGISTRO_TRABAJADOR.equals(path) && "POST".equalsIgnoreCase(method));

            UsernamePasswordAuthenticationToken authToken;

            if (esRutaDeCreacionDePerfil) {
                logger.debug("Ruta de creación de perfil detectada para UID: {}. Token de Firebase es válido. No se busca en BD local aún.");
                // Para la creación de perfil, solo necesitamos que el token de Firebase sea válido.
                // El controlador se encargará de crear el usuario en la BD local.
                // Creamos una autenticación simple con el UID. El rol se asignará en el controlador.
                authToken = new UsernamePasswordAuthenticationToken(uid, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_PRE_REGISTRO"))); // Un rol temporal o específico
            } else {
                // Para todas las OTRAS RUTAS AUTENTICADAS, el usuario DEBE existir en nuestra BD local
                // y tener un rol definido.
                Optional<Usuario> optUsuario = Optional.ofNullable(usuarioService.getUsuarioById(uid));
                if (optUsuario.isEmpty()) {
                    logger.warn("Token de Firebase válido para UID: {} pero usuario no encontrado en BD local para ruta: {} {}");
                    enviarError(response, HttpServletResponse.SC_FORBIDDEN, "Usuario (UID del token) no encontrado en la base de datos local.");
                    return;
                }
                Usuario usuario = optUsuario.get();
                String rolName = usuario.getRol().name(); // Asegúrate que usuario.getRol() no sea null
                GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + rolName);
                authToken = new UsernamePasswordAuthenticationToken(uid, null, List.of(authority));
                request.setAttribute("userRole", rolName); // Rol de la BD local
            }

            request.setAttribute("userId", uid); // UID del usuario autenticado
            SecurityContextHolder.getContext().setAuthentication(authToken);
            chain.doFilter(request, response);

        } catch (FirebaseAuthException e) {
            logger.warn("Token de Firebase inválido o error al verificar: " + e.getMessage());
            enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token de Firebase inválido o expirado");
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

    // Método para verificar rutas públicas explícitas
    private boolean esRutaPublica(String path, String method) {
        // Ruta de verificación de email
        if (RUTA_VERIFICAR_EMAIL.equals(path) && "GET".equalsIgnoreCase(method)) {
            return true;
        }
        // Prefijos generales (Swagger, etc.)
        for (String prefijoPublico : PREFIJOS_RUTAS_PUBLICAS_GENERALES) {
            if (path.startsWith(prefijoPublico)) {
                return true;
            }
        }
        return false;
    }

    private void enviarError(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + mensaje + "\"}");
    }
}