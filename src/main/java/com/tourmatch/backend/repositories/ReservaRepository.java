package com.tourmatch.backend.repositories;

import com.tourmatch.backend.models.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    
    // 💰 Sumar todas las ganancias de la plataforma
    @Query("SELECT SUM(r.comisionPlataforma) FROM Reserva r WHERE r.estado = 'COMPLETADA'")
    Double obtenerTotalComisionesPlataforma();
}