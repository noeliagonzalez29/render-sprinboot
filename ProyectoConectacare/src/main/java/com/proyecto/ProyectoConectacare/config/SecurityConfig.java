package com.proyecto.ProyectoConectacare.config;


import com.google.cloud.storage.HttpMethod;
import com.proyecto.ProyectoConectacare.security.FirebaseFiltroAutenticacion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
                        .requestMatchers(String.valueOf(HttpMethod.POST), "/usuarios/cliente").permitAll()
                        .requestMatchers(String.valueOf(HttpMethod.POST), "/usuarios/trabajador").permitAll()
                        .requestMatchers(
                                "/api/public/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
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
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}