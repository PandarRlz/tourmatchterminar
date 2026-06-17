package com.tourmatch.backend.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_viaje", nullable = false)
    private LocalDateTime fechaViaje;

    @Column(name = "precio_total", nullable = false)
    private Double precioTotal;

    @Column(nullable = false)
    private Integer cantidadPasajeros;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado;

    @ManyToOne
    @JoinColumn(name = "turista_id", nullable = false)
    private Usuario turista;

    @ManyToOne
    @JoinColumn(name = "conductor_id")
    private Usuario conductor;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Waypoint> waypoints;

    public enum EstadoReserva {
        PENDIENTE, ACEPTADA, COMPLETADA, CANCELADA
    }
}