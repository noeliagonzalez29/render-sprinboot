package com.proyecto.ProyectoConectacare.config;



import com.proyecto.ProyectoConectacare.security.FirebaseFiltroAutenticacion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Clase responsable de configurar los ajustes de seguridad de la aplicación Spring Boot.
 * Configura la autenticación, la autorización y diversas funciones relacionadas con la seguridad.
 *
 * Una instancia de esta clase configura el objeto HttpSecurity y establece una cadena de filtros.
 * utilizada para proteger las solicitudes HTTP.
 *
 * Funcionalidades clave:
 * - Configura el Intercambio de Recursos entre Orígenes (CORS) para permitir el acceso controlado desde orígenes específicos.
 * - Desactiva la protección contra la Falsificación de Solicitudes entre Sitios (CSRF) para permitir solicitudes de API REST sin estado.
 * - Configura la gestión de sesiones sin estado para evitar la creación y el uso de sesiones del lado del servidor.
 * - Define reglas de acceso para rutas HTTP, incluyendo endpoints públicos y autenticados.
 * - Incorpora un filtro de autenticación personalizado que utiliza Firebase para la autenticación basada en tokens.
 *
 * Beans:
 * - `SecurityFilterChain`: Configura la cadena de filtros de seguridad utilizada para interceptar y procesar solicitudes HTTP. * - `CorsConfigurationSource`: Proporciona una configuración CORS personalizada para definir orígenes, métodos y encabezados permitidos.
 *
 * Dependencias:
 * - Esta clase depende del componente `FirebaseFiltroAutenticacion` para gestionar la autenticación basada en Firebase
 * y extraer información del usuario de los tokens de ID.
 *
 * Autorización de ruta:
 * - Las rutas de acceso público incluyen los endpoints en `/api/public/**`, las rutas de documentación de Swagger
 * y ciertos endpoints de registro.
 * - Los endpoints de administración (`/admin/**`) y otras rutas autenticadas requieren una autenticación válida.
 *
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final FirebaseFiltroAutenticacion firebaseFilter;

    public SecurityConfig(FirebaseFiltroAutenticacion firebaseFilter) {
        this.firebaseFilter = firebaseFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Configuración CORS (asumiendo que tienes un bean CorsConfigurationSource)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configuración CSRF
                .csrf(csrf -> csrf.disable())

                // Configuración de sesión
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Autorización de requests
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/usuarios/cliente").authenticated()
                        .requestMatchers(HttpMethod.POST, "/usuarios/trabajador").authenticated()
                        .requestMatchers(
                                "/api/public/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/usuarios/email-existe").permitAll()
                        .requestMatchers("/admin/**").authenticated()
                        .anyRequest().authenticated()
                )

                // Añadir filtro personalizado
                .addFilterBefore(firebaseFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Si usas una configuración CORS personalizada
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://proyectoconectacare.web.app",
                "https://proyectoconectacare.firebaseapp.com",
                "http://localhost:4200" ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}