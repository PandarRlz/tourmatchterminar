package com.tourmatch.backend.services;

import com.tourmatch.backend.models.Reserva;
import com.tourmatch.backend.dtos.ReservaDTO;
import com.tourmatch.backend.models.Usuario;
import com.tourmatch.backend.models.Waypoint;
import com.tourmatch.backend.repositories.ReservaRepository;
import com.tourmatch.backend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public Reserva crearReservaConUsuario(Reserva reserva, String emailTurista) {
        Usuario turista = usuarioRepository.findByEmail(emailTurista)
                .orElseThrow(() -> new RuntimeException("Turista no encontrado"));
        reserva.setTurista(turista);

        if (reserva.getWaypoints() != null && !reserva.getWaypoints().isEmpty()) {
            for (Waypoint wp : reserva.getWaypoints()) {
                wp.setReserva(reserva);
            }
        } else {
            throw new RuntimeException("Una ruta personalizada requiere mínimo un origen y un destino.");
        }

        reserva.setEstado(Reserva.EstadoReserva.PENDIENTE);
        return reservaRepository.save(reserva);
    }

    public List<Reserva> obtenerReservasPorUsuario(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return reservaRepository.findAll().stream()
                .filter(r -> r.getTurista() != null && r.getTurista().getId().equals(usuario.getId()))
                .toList();
    }

    /**
     * 🌟 SOLUCIÓN ANTIBLOQUEO CON REGLA DE NEGOCIO FINANCIERA (15% / 85%)
     * Mapea las reservas pendientes aplicando los desgloses económicos directamente en el backend.
     */
    public List<ReservaDTO> obtenerViajesDisponibles(int capacidadConductor) {
        List<ReservaDTO> listaMapeada = new ArrayList<>();
        List<Reserva> todasLasReservas = reservaRepository.findAll();

        for (Reserva r : todasLasReservas) {
            try {
                // 1. Solo procesamos los que están PENDIENTES
                if (r.getEstado() != Reserva.EstadoReserva.PENDIENTE) {
                    continue; 
                }

                // 2. Extraer el nombre del turista de forma segura
                String nombrePasajero = "Pasajero";
                if (r.getTurista() != null) {
                    nombrePasajero = r.getTurista().getNombre();
                }

                // 3. Extraer los waypoints de forma ultra segura
                List<ReservaDTO.WaypointDTO> waypointDtos = new ArrayList<>();
                if (r.getWaypoints() != null) {
                    for (Waypoint wp : r.getWaypoints()) {
                        waypointDtos.add(new ReservaDTO.WaypointDTO(
                            wp.getId(),
                            wp.getDireccion(),
                            wp.getOrden()
                        ));
                    }
                }

                // 4. Formatear las variables base
                String fechaStr = (r.getFechaViaje() != null) ? r.getFechaViaje().toString() : "";
                Integer pasajeros = (r.getCantidadPasajeros() != null) ? r.getCantidadPasajeros() : 1;
                String estadoStr = (r.getEstado() != null) ? r.getEstado().name() : "PENDIENTE";

                // =========================================================
                // 🚀 REGLA DE NEGOCIO EN ACCIÓN: DISTRIBUCIÓN DE INGRESOS
                // =========================================================
                Double precioBase = (r.getPrecioTotal() != null) ? r.getPrecioTotal() : 0.0;
                
                Double comisionTourMatch = precioBase * 0.15;      // 15% para nuestra comisión
                Double gananciaParaConductor = precioBase * 0.85;  // 85% neto para el conductor
                // =========================================================

                // 5. Construimos el DTO (Java Record) pasando los parámetros en orden exacto
                ReservaDTO dto = new ReservaDTO(
                    r.getId(),
                    fechaStr,
                    precioBase,              // El pasajero paga el 100%
                    gananciaParaConductor,   // El conductor recibe el 85%
                    comisionTourMatch,       // La aplicación retiene el 15%
                    pasajeros,
                    estadoStr,
                    nombrePasajero,
                    waypointDtos
                );

                listaMapeada.add(dto);

            } catch (Exception e) {
                // Evita que un registro corrupto bote la API completa
                System.err.println("Error procesando reserva ID: " + r.getId() + " -> " + e.getMessage());
            }
        }

        return listaMapeada;
    }

@Transactional
    public Reserva aceptarReserva(Long reservaId, String emailConductor) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        if (reserva.getEstado() != Reserva.EstadoReserva.PENDIENTE) {
            throw new RuntimeException("Este viaje ya fue procesado o cancelado.");
        }

        Usuario conductor = usuarioRepository.findByEmail(emailConductor)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

        // 🛑 NUEVO ESCUDO: Verificar si el conductor ya tiene un viaje en curso
        boolean tieneViajeEnCurso = reservaRepository.findAll().stream()
                .anyMatch(r -> r.getConductor() != null && 
                               r.getConductor().getId().equals(conductor.getId()) && 
                               r.getEstado() == Reserva.EstadoReserva.ACEPTADA);

        if (tieneViajeEnCurso) {
            throw new RuntimeException("Ya tienes un viaje en curso. Debes finalizarlo antes de aceptar uno nuevo.");
        }

        reserva.setConductor(conductor);
        reserva.setEstado(Reserva.EstadoReserva.ACEPTADA);

        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva finalizarReserva(Long reservaId, String emailConductor) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        // Validamos que el viaje lo esté terminando el conductor asignado
        if (reserva.getConductor() == null || !reserva.getConductor().getEmail().equals(emailConductor)) {
            throw new RuntimeException("No tienes permiso para finalizar este viaje.");
        }

        if (reserva.getEstado() != Reserva.EstadoReserva.ACEPTADA) {
            throw new RuntimeException("Solo se puede finalizar un viaje que ya está aceptado.");
        }

        reserva.setEstado(Reserva.EstadoReserva.COMPLETADA);
        return reservaRepository.save(reserva);   
    }

    public List<ReservaDTO> obtenerMisViajesEnCurso(String emailConductor) {
        Usuario conductor = usuarioRepository.findByEmail(emailConductor)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

        return reservaRepository.findAll().stream()
                .filter(r -> r.getConductor() != null && 
                             r.getConductor().getId().equals(conductor.getId()) && 
                             r.getEstado() == Reserva.EstadoReserva.ACEPTADA)
                .map(r -> new ReservaDTO(
                    r.getId(),
                    r.getFechaViaje() != null ? r.getFechaViaje().toString() : "",
                    r.getPrecioTotal(),
                    r.getPrecioTotal() * 0.85,
                    r.getPrecioTotal() * 0.15,
                    r.getCantidadPasajeros(),
                    r.getEstado().name(),
                    r.getTurista().getNombre(),
                    r.getWaypoints() != null ? r.getWaypoints().stream()
                        .map(wp -> new ReservaDTO.WaypointDTO(wp.getId(), wp.getDireccion(), wp.getOrden()))
                        .toList() : new ArrayList<>()
                ))
                .toList();
    }
}