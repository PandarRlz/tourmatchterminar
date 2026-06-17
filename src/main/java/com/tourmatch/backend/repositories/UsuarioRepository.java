package com.tourmatch.backend.repositories;

import com.tourmatch.backend.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Con esta simple línea, Spring Boot crea automáticamente la consulta SQL 
    // para buscar un usuario por su correo. ¡Magia pura!
    Optional<Usuario> findByEmail(String email);
}