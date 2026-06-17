package com.tourmatch.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore; // ◄--- AGREGA ESTE IMPORT
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "waypoints")
public class Waypoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    @Column(nullable = false)
    private Integer orden;

    @ManyToOne
    @JoinColumn(name = "reserva_id", nullable = false)
    @JsonIgnore // ◄--- ¡AGREGA ESTO AQUÍ! Evita el bucle infinito al enviar datos al Conductor
    private Reserva reserva;
}