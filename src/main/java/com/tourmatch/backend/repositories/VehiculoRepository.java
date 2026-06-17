package com.tourmatch.backend.repositories;

import com.tourmatch.backend.models.Vehiculo;
import com.tourmatch.backend.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    Optional<Vehiculo> findByConductor(Usuario conductor);
    Optional<Vehiculo> findByPatente(String patente);
}