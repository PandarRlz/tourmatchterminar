package com.tourmatch.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            // 🌟 MODIFICADO: Vinculamos explícitamente el Bean de CORS que definimos abajo
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) 
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) 
            .authorizeHttpRequests(auth -> auth
                // Permitimos las peticiones de diagnóstico OPTIONS del navegador
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                .requestMatchers("/api/auth/**").permitAll() // Login y Registro públicos
                .requestMatchers("/api/admin/**").authenticated() 
                .anyRequest().authenticated() 
            );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 🌟 NUEVO BEAN: Abre la puerta de forma explícita a peticiones externas (CORS)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permite conexiones desde cualquier origen (Vercel, Localhost, etc.)
        configuration.setAllowedOrigins(List.of("*")); 
        
        // Habilita explícitamente todos los métodos necesarios para la app
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Permite cualquier tipo de cabecera (incluyendo tu Token de autorización)
        configuration.setAllowedHeaders(List.of("*"));
        
        // Expone las cabeceras si el cliente las requiere
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica esta política de manera global a todos los endpoints de la API
        source.registerCorsConfiguration("/**", configuration); 
        
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}