package com.tourmatch.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 🚀 BALA DE PLATA CONTRA CORS: Inyectamos los permisos antes de cualquier validación
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");

        // Si es una petición de verificación (OPTIONS), le damos luz verde y cortamos la validación aquí
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return; 
        }

        // 1. Obtener el header de autorización
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;
        String rol = null; // valor nulo por defecto

        // 2. Comprobar si el header empieza con "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // Quitar "Bearer "
            try {
                email = jwtUtil.extractEmail(token);
                // Extraemos el rol usando el método seguro de JwtUtil
                rol = jwtUtil.extractRole(token);
            } catch (Exception e) {
                System.out.println("Error extrayendo datos del token: " + e.getMessage());
            }
        }

        // 3. Validar el token y autenticar al usuario con su ROL REAL
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(token)) {
                // Si no se encontró rol en el token, asignamos un rol por defecto simple
                String authority = (rol != null && !rol.isBlank()) ? "ROLE_" + rol : "ROLE_USER";
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        email, null, Collections.singletonList(new SimpleGrantedAuthority(authority)));

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        // 4. Continuar con el siguiente filtro
        filterChain.doFilter(request, response);
    }
}