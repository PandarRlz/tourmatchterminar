package com.tourmatch.backend.controllers;

import com.tourmatch.backend.dtos.AuthResponse;
import com.tourmatch.backend.dtos.LoginRequest;
import com.tourmatch.backend.dtos.RegistroRequest;
import com.tourmatch.backend.services.AuthService;
import jakarta.validation.Valid; // ◄--- NUEVO IMPORT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // <-- ¡ESTA ES LA LLAVE MÁGICA!

public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/registro")
    // MODIFICADO: Agregamos @Valid para que se ejecuten las anotaciones del DTO
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistroRequest request) { 
        try {
            AuthResponse response = authService.registrar(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Credenciales incorrectas: " + e.getMessage());
        }
    }
}