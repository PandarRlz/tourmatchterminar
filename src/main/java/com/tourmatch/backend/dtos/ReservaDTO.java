package com.tourmatch.backend.dtos;

import java.util.List;

public record ReservaDTO(
    Long id,
    String fechaViaje,
    Double precioTotal,        // 100% (Lo que paga el Turista)
    Double gananciaConductor,  // 85% (Lo que gana el Conductor)
    Double comisionApp,        // 15% (La comisión de la plataforma)
    Integer cantidadPasajeros,
    String estado,
    String nombreTurista, 
    List<WaypointDTO> waypoints 
) {
    public record WaypointDTO(
        Long id,
        String direccion, 
        Integer orden     
    ) {}
}