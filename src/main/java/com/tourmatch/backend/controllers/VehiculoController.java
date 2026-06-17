package com.tourmatch.backend.controllers;

import com.tourmatch.backend.models.Vehiculo;
import com.tourmatch.backend.models.Usuario;
import com.tourmatch.backend.repositories.UsuarioRepository;
import com.tourmatch.backend.repositories.VehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/vehiculos")
@CrossOrigin(origins = "*") // <-- ¡ESTA ES LA LLAVE MÁGICA!

public class VehiculoController {

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarVehiculo(@RequestBody Vehiculo vehiculo, Authentication authentication) {
        try {
            String emailConductor = authentication.getName();
            Usuario conductor = usuarioRepository.findByEmail(emailConductor)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado en la sesión actual."));

            if (vehiculoRepository.findByConductor(conductor).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("mensaje", "El usuario ya posee un vehículo registrado."));
            }

            if (vehiculoRepository.findByPatente(vehiculo.getPatente()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("mensaje", "La patente ingresada ya se encuentra registrada en el sistema."));
            }

            vehiculo.setConductor(conductor);

            if (vehiculo.getCapacidad() > 4) {
                vehiculo.setTipo(Vehiculo.CategoriaVehiculo.XL);
            } else {
                vehiculo.setTipo(Vehiculo.CategoriaVehiculo.BASICO);
            }

            vehiculoRepository.save(vehiculo);
            return ResponseEntity.ok(Map.of("mensaje", "Vehículo registrado exitosamente."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Ocurrió un error interno en el servidor al procesar la solicitud."));
        }
    }

    @GetMapping("/mi-vehiculo")
    public ResponseEntity<?> obtenerMiVehiculo(Authentication authentication) {
        try {
            String emailConductor = authentication.getName();
            Usuario conductor = usuarioRepository.findByEmail(emailConductor)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado en la sesión actual."));

            Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findByConductor(conductor);
            
            if (vehiculoOpt.isPresent()) {
                Vehiculo v = vehiculoOpt.get();
                return ResponseEntity.ok(Map.of(
                    "id", v.getId(),
                    "patente", v.getPatente(),
                    "marca", v.getMarca(),
                    "modelo", v.getModelo(),
                    "capacidad", v.getCapacidad(),
                    "tipo", v.getTipo().toString()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error al consultar la información vehicular."));
        }
    }
}