package com.tourmatch.backend.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // ☢️ OPCIÓN NUCLEAR: Se ejecuta ANTES que Spring Security
public class SimpleCORSFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        String origin = request.getHeader("Origin");
        
        // Lista de orígenes permitidos
        String[] allowedOrigins = {
            "https://tourmatch-frontend.vercel.app",
            "http://localhost:3000",
            "http://localhost:5173"
        };
        
        // Si el origen es uno de los permitidos, lo autorizamos
        for (String allowedOrigin : allowedOrigins) {
            if (allowedOrigin.equals(origin)) {
                response.setHeader("Access-Control-Allow-Origin", origin);
                break;
            }
        }
        
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT, PATCH");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept, x-requested-with, Cache-Control");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // Si el navegador pregunta si puede pasar (OPTIONS), le decimos que SÍ y cortamos la petición ahí mismo
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            // Si es un GET, POST, PUT o DELETE normal, lo dejamos seguir su camino hacia Spring Security
            chain.doFilter(req, res);
        }
    }
}