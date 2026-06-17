package com.tourmatch.backend.security;

import com.tourmatch.backend.models.Usuario;
import com.tourmatch.backend.models.Usuario.TipoRol; // Importamos el enum correcto
import com.tourmatch.backend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataSetupConfig implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; 

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "tourmatchadmin@gmail.com";
        
        // 1. Buscamos si ya existe el administrador en Neon.tech
        Optional<Usuario> adminOpt = usuarioRepository.findByEmail(adminEmail);
        
        // 2. Si no existe en la base de datos, lo registramos de inmediato
        if (adminOpt.isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setEmail(adminEmail);
            
            // Encriptamos la clave usando el PasswordEncoder para cumplir con Spring Security
            admin.setPassword(passwordEncoder.encode("admintourm123"));
            
            // Asignamos el rol ADMIN del enum TipoRol que acabamos de actualizar
            admin.setRol(TipoRol.ADMIN); 

            // Guardamos el registro de forma definitiva
            usuarioRepository.save(admin);
            System.out.println(">> [DATABASE] ¡Usuario Administrador creado exitosamente con credenciales iniciales!");
        } else {
            System.out.println(">> [DATABASE] El Usuario Administrador ya existe en el sistema.");
        }
    }
}