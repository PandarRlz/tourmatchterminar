package com.tourmatch.backend.services;

import com.tourmatch.backend.dtos.AuthResponse;
import com.tourmatch.backend.dtos.LoginRequest;
import com.tourmatch.backend.dtos.RegistroRequest;
import com.tourmatch.backend.models.Usuario;
import com.tourmatch.backend.repositories.UsuarioRepository;
import com.tourmatch.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegistroRequest registroRequest;
    private LoginRequest loginRequest;
    private Usuario usuarioSimulado;

    @BeforeEach
    void setUp() {
        // Datos de prueba preparados antes de cada test
        registroRequest = new RegistroRequest();
        registroRequest.setNombre("Juan Perez");
        registroRequest.setEmail("juan@test.com");
        registroRequest.setPassword("123456");
        registroRequest.setRol("CONDUCTOR");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("juan@test.com");
        loginRequest.setPassword("123456");

        usuarioSimulado = new Usuario();
        usuarioSimulado.setId(1L);
        usuarioSimulado.setNombre("Juan Perez");
        usuarioSimulado.setEmail("juan@test.com");
        usuarioSimulado.setPassword("hash_123456");
        usuarioSimulado.setRol(Usuario.TipoRol.CONDUCTOR);
    }

    // ==========================================
    // PRUEBAS PARA REGISTRO
    // ==========================================

    @Test
    void registrar_UsuarioValido_RetornaAuthResponse() {
        // Arrange (Preparar)
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hash_123456");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSimulado);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("token_falso_123");

        // Act (Ejecutar)
        AuthResponse response = authService.registrar(registroRequest);

        // Assert (Comprobar)
        assertNotNull(response);
        assertEquals("token_falso_123", response.getToken());
        assertEquals("CONDUCTOR", response.getRol());
        verify(usuarioRepository, times(1)).save(any(Usuario.class)); // Verifica que se guardó
    }

    @Test
    void registrar_EmailYaExiste_LanzaExcepcion() {
        // Arrange: Simulamos que la BD ya tiene ese correo
        when(usuarioRepository.findByEmail(registroRequest.getEmail())).thenReturn(Optional.of(usuarioSimulado));

        // Act & Assert: Verificamos que explote con el mensaje correcto
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.registrar(registroRequest);
        });

        assertEquals("Error: El correo electrónico ya está en uso", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class)); // Verifica que NUNCA se guardó
    }

    // ==========================================
    // PRUEBAS PARA LOGIN
    // ==========================================

    @Test
    void login_CredencialesCorrectas_RetornaAuthResponse() {
        // Arrange
        when(usuarioRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(usuarioSimulado));
        when(passwordEncoder.matches(loginRequest.getPassword(), usuarioSimulado.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("token_falso_123");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("token_falso_123", response.getToken());
        assertEquals("Bienvenido, Juan Perez", response.getMensaje());
    }

    @Test
    void login_PasswordIncorrecto_LanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(usuarioSimulado));
        when(passwordEncoder.matches(loginRequest.getPassword(), usuarioSimulado.getPassword())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Error: Contraseña incorrecta", exception.getMessage());
        verify(jwtUtil, never()).generateToken(anyString(), anyString()); // No debe generar token
    }
}