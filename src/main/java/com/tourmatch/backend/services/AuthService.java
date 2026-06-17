package com.tourmatch.backend.services;

import com.tourmatch.backend.dtos.AuthResponse;
import com.tourmatch.backend.dtos.LoginRequest;
import com.tourmatch.backend.dtos.RegistroRequest;
import com.tourmatch.backend.models.Usuario;
import com.tourmatch.backend.repositories.UsuarioRepository;
import com.tourmatch.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Lógica para registrar un usuario nuevo
     */
    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        // 1. Validaciones
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new RuntimeException("Email y password son obligatorios");
        }

        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Error: El correo electrónico ya está en uso");
        }

        // 2. Crear Usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(request.getNombre());
        nuevoUsuario.setEmail(request.getEmail());
        nuevoUsuario.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // 3. Asignar ROL
        try {
            String rolInput = (request.getRol() != null) ? request.getRol().toUpperCase() : "TURISTA";
            nuevoUsuario.setRol(Usuario.TipoRol.valueOf(rolInput));
        } catch (IllegalArgumentException e) {
            nuevoUsuario.setRol(Usuario.TipoRol.TURISTA);
        }

        // 4. Guardar en Base de Datos
        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        // 5. Generar Token
        String token = jwtUtil.generateToken(usuarioGuardado.getEmail(), usuarioGuardado.getRol().name());
        
        // RETORNO CON TODOS LOS DATOS PARA EL FRONTEND
        return new AuthResponse(
            token, 
            usuarioGuardado.getRol().name(), 
            usuarioGuardado.getNombre(), 
            "Usuario registrado exitosamente"
        );
    }

    /**
     * Lógica para validar credenciales e iniciar sesión
     */
    public AuthResponse login(LoginRequest request) {
        // 1. Buscar usuario
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado"));

        // 2. Validar contraseña
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Error: Contraseña incorrecta");
        }

        // 3. Generar Token
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol().name());
        
        // RETORNO CON TODOS LOS DATOS PARA EL FRONTEND
        // Aquí pasamos explícitamente el ROL y el NOMBRE de la DB
        return new AuthResponse(
            token, 
            usuario.getRol().name(), 
            usuario.getNombre(), 
            "Bienvenido, " + usuario.getNombre()
        );
    }
}