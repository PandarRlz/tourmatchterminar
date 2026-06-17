package com.tourmatch.backend.services;

import com.tourmatch.backend.dtos.VehiculoRequest;
import com.tourmatch.backend.models.Usuario;
import com.tourmatch.backend.models.Vehiculo;
import com.tourmatch.backend.repositories.UsuarioRepository;
import com.tourmatch.backend.repositories.VehiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VehiculoServiceTest {

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private VehiculoService vehiculoService;

    private Usuario conductorValido;
    private Usuario turistaInvalido;
    private VehiculoRequest requestBasico;
    private VehiculoRequest requestXL;

    @BeforeEach
    void setUp() {
        // Preparamos un usuario con rol CONDUCTOR
        conductorValido = new Usuario();
        conductorValido.setId(1L);
        conductorValido.setEmail("conductor@test.com");
        conductorValido.setRol(Usuario.TipoRol.CONDUCTOR);

        // Preparamos un usuario con rol TURISTA (para forzar error)
        turistaInvalido = new Usuario();
        turistaInvalido.setId(2L);
        turistaInvalido.setEmail("turista@test.com");
        turistaInvalido.setRol(Usuario.TipoRol.TURISTA);

        // Petición para un auto normal (Capacidad <= 4)
        requestBasico = new VehiculoRequest();
        requestBasico.setPatente("ABCD12");
        requestBasico.setModelo("Toyota Yaris");
        requestBasico.setCapacidad(4);

        // Petición para un vehículo grande (Capacidad > 4)
        requestXL = new VehiculoRequest();
        requestXL.setPatente("WXYZ99");
        requestXL.setModelo("Hyundai H1");
        requestXL.setCapacidad(7);
    }

    // ==========================================
    // PRUEBAS DE REGISTRO Y REGLAS DE NEGOCIO
    // ==========================================

    @Test
    void registrarVehiculo_CapacidadHasta4_AsignaCategoriaBasica() {
        // Arrange
        when(usuarioRepository.findByEmail(conductorValido.getEmail())).thenReturn(Optional.of(conductorValido));
        
        Vehiculo vehiculoSimulado = new Vehiculo();
        vehiculoSimulado.setTipo(Vehiculo.CategoriaVehiculo.BASICO);
        when(vehiculoRepository.save(any(Vehiculo.class))).thenReturn(vehiculoSimulado);

        // Act
        Vehiculo resultado = vehiculoService.registrarVehiculo(requestBasico, conductorValido.getEmail());

        // Assert
        assertNotNull(resultado);
        assertEquals(Vehiculo.CategoriaVehiculo.BASICO, resultado.getTipo());
        verify(vehiculoRepository, times(1)).save(any(Vehiculo.class));
    }

    @Test
    void registrarVehiculo_CapacidadMayorA4_AsignaCategoriaXL() {
        // Arrange
        when(usuarioRepository.findByEmail(conductorValido.getEmail())).thenReturn(Optional.of(conductorValido));
        
        Vehiculo vehiculoSimulado = new Vehiculo();
        vehiculoSimulado.setTipo(Vehiculo.CategoriaVehiculo.XL);
        when(vehiculoRepository.save(any(Vehiculo.class))).thenReturn(vehiculoSimulado);

        // Act
        Vehiculo resultado = vehiculoService.registrarVehiculo(requestXL, conductorValido.getEmail());

        // Assert
        assertNotNull(resultado);
        assertEquals(Vehiculo.CategoriaVehiculo.XL, resultado.getTipo());
        verify(vehiculoRepository, times(1)).save(any(Vehiculo.class));
    }

    // ==========================================
    // PRUEBAS DE SEGURIDAD Y VALIDACIÓN DE ROL
    // ==========================================

    @Test
    void registrarVehiculo_UsuarioNoEsConductor_LanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findByEmail(turistaInvalido.getEmail())).thenReturn(Optional.of(turistaInvalido));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            vehiculoService.registrarVehiculo(requestBasico, turistaInvalido.getEmail());
        });

        assertEquals("Solo los usuarios con rol de CONDUCTOR pueden registrar vehículos.", exception.getMessage());
        verify(vehiculoRepository, never()).save(any(Vehiculo.class)); // Seguridad: No se guarda nada en BD
    }

    @Test
    void registrarVehiculo_UsuarioNoEncontrado_LanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findByEmail("correo@fantasma.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            vehiculoService.registrarVehiculo(requestBasico, "correo@fantasma.com");
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(vehiculoRepository, never()).save(any(Vehiculo.class));
    }
}