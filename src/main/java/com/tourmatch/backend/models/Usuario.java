package com.tourmatch.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email; // ◄--- NUEVO IMPORT
import jakarta.validation.constraints.NotBlank; // ◄--- NUEVO IMPORT
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    // MODIFICADO: Añadimos validación en la persistencia del modelo
    @NotBlank(message = "El correo no puede estar vacío")
    @Email(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "Formato de correo inválido")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRol rol; 
    
    public enum TipoRol {
        TURISTA, CONDUCTOR, ADMIN 
    }
}