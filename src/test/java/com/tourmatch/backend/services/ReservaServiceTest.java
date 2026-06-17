package com.tourmatch.backend.services;

import com.tourmatch.backend.dtos.ReservaDTO;
import com.tourmatch.backend.models.Reserva;
import com.tourmatch.backend.models.Usuario;
import com.tourmatch.backend.models.Waypoint;
import com.tourmatch.backend.repositories.ReservaRepository;
import com.tourmatch.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ReservaService reservaService;

    private Usuario turistaSimulado;
    private Usuario conductorSimulado;
    private Reserva reservaSimulada;

    @BeforeEach
    void setUp() {
        // Preparamos a un Turista
        turistaSimulado = new Usuario();
        turistaSimulado.setId(1L);
        turistaSimulado.setNombre("Carlos Turista");
        turistaSimulado.setEmail("turista@test.com");
        turistaSimulado.setRol(Usuario.TipoRol.TURISTA);

        // Preparamos a un Conductor
        conductorSimulado = new Usuario();
        conductorSimulado.setId(2L);
        conductorSimulado.setNombre("Pedro Conductor");
        conductorSimulado.setEmail("conductor@test.com");
        conductorSimulado.setRol(Usuario.TipoRol.CONDUCTOR);

        // Preparamos una Reserva PENDIENTE
        reservaSimulada = new Reserva();
        reservaSimulada.setId(100L);
        reservaSimulada.setPrecioTotal(15000.0);
        reservaSimulada.setCantidadPasajeros(2);
        reservaSimulada.setEstado(Reserva.EstadoReserva.PENDIENTE);
        reservaSimulada.setTurista(turistaSimulado);
        reservaSimulada.setFechaViaje(LocalDateTime.now());
        
        // Agregamos Waypoints a la reserva para que pase la validación
        List<Waypoint> waypoints = new ArrayList<>();
        Waypoint origen = new Waypoint();
        origen.setDireccion("Plaza de Armas");
        waypoints.add(origen);
        reservaSimulada.setWaypoints(waypoints);
    }

    // ==========================================
    // PRUEBAS PARA CREAR RESERVA
    // ==========================================

    @Test
    void crearReservaConUsuario_TuristaValidoYWaypoints_CreaReserva() {
        // Arrange
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(turistaSimulado));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaSimulada);

        // Act
        Reserva resultado = reservaService.crearReservaConUsuario(reservaSimulada, turistaSimulado.getEmail());

        // Assert
        assertNotNull(resultado);
        assertEquals(Reserva.EstadoReserva.PENDIENTE, resultado.getEstado());
        assertEquals("Carlos Turista", resultado.getTurista().getNombre());
        verify(reservaRepository, times(1)).save(any(Reserva.class));
    }

    @Test
    void crearReservaConUsuario_SinWaypoints_LanzaExcepcion() {
        // Arrange
        reservaSimulada.setWaypoints(new ArrayList<>()); // Vaciamos los waypoints
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(turistaSimulado));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reservaService.crearReservaConUsuario(reservaSimulada, turistaSimulado.getEmail());
        });

        assertEquals("Una ruta personalizada requiere mínimo un origen y un destino.", exception.getMessage());
        verify(reservaRepository, never()).save(any(Reserva.class)); // Verificamos que no se guarde basura en la BD
    }

    // ==========================================
    // PRUEBAS PARA OBTENER VIAJES (LISTAS)
    // ==========================================

    @Test
    void obtenerViajesDisponibles_ExistenPendientes_RetornaListaDTO() {
        // Arrange
        List<Reserva> listaReservas = new ArrayList<>();
        listaReservas.add(reservaSimulada); // Agregamos una pendiente
        
        Reserva reservaAceptada = new Reserva();
        reservaAceptada.setEstado(Reserva.EstadoReserva.ACEPTADA);
        listaReservas.add(reservaAceptada); // Agregamos una aceptada (debería ser ignorada por el bucle)

        when(reservaRepository.findAll()).thenReturn(listaReservas);

        // Act
        List<ReservaDTO> resultado = reservaService.obtenerViajesDisponibles(4);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size()); // Solo debe devolver 1 (la pendiente)
        assertEquals("PENDIENTE", resultado.get(0).estado());
        assertEquals("Carlos Turista", resultado.get(0).nombreTurista());
    }

    // ==========================================
    // PRUEBAS PARA ACEPTAR RESERVA (CONDUCTOR)
    // ==========================================

    @Test
    void aceptarReserva_ViajePendienteYConductorValido_CambiaEstadoAAceptada() {
        // Arrange
        when(reservaRepository.findById(anyLong())).thenReturn(Optional.of(reservaSimulada));
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(conductorSimulado));
        
        // Simulamos que al guardar, devuelve la reserva pero ya con el estado ACEPTADA
        Reserva reservaGuardada = new Reserva();
        reservaGuardada.setEstado(Reserva.EstadoReserva.ACEPTADA);
        reservaGuardada.setConductor(conductorSimulado);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaGuardada);

        // Act
        Reserva resultado = reservaService.aceptarReserva(100L, conductorSimulado.getEmail());

        // Assert
        assertNotNull(resultado);
        assertEquals(Reserva.EstadoReserva.ACEPTADA, resultado.getEstado());
        assertEquals("Pedro Conductor", resultado.getConductor().getNombre());
    }

    @Test
    void aceptarReserva_ViajeYaProcesado_LanzaExcepcion() {
        // Arrange
        reservaSimulada.setEstado(Reserva.EstadoReserva.CANCELADA); // Le cambiamos el estado
        when(reservaRepository.findById(anyLong())).thenReturn(Optional.of(reservaSimulada));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reservaService.aceptarReserva(100L, conductorSimulado.getEmail());
        });

        assertEquals("Este viaje ya fue procesado o cancelado.", exception.getMessage());
        verify(usuarioRepository, never()).findByEmail(anyString()); // Verifica que ni siquiera busca al conductor si ya falló
    }
}