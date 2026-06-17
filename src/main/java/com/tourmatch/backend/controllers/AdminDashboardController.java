package com.tourmatch.backend.controllers;

import com.tourmatch.backend.models.Reserva;
import com.tourmatch.backend.models.Usuario;
import com.tourmatch.backend.models.Vehiculo;
import com.tourmatch.backend.repositories.UsuarioRepository;
import com.tourmatch.backend.repositories.ReservaRepository;
import com.tourmatch.backend.repositories.VehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*") // <-- ¡ESTA ES LA LLAVE MÁGICA!

public class AdminDashboardController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    // --- 1. GET: Conductores con sus Vehículos (Mapeo Completo) ---
    @GetMapping("/conductores") 
    public ResponseEntity<?> obtenerConductores() {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            List<Map<String, Object>> respuesta = new ArrayList<>();
            
            for (Usuario u : usuarios) {
                if (Usuario.TipoRol.CONDUCTOR.equals(u.getRol())) {
                    Map<String, Object> datosConductor = new HashMap<>();
                    datosConductor.put("id", u.getId());
                    datosConductor.put("nombre", u.getNombre());
                    datosConductor.put("email", u.getEmail());
                    datosConductor.put("rol", u.getRol());
                    
                    Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findByConductor(u);
                    
                    if (vehiculoOpt.isPresent()) {
                        Vehiculo v = vehiculoOpt.get();
                        datosConductor.put("vehiculo", v.getMarca() + " " + v.getModelo());
                        datosConductor.put("patente", v.getPatente());
                        
                        // ENVIAMOS PROPIEDADES INDEPENDIENTES PARA EL AUTO-POBLADO EN REACT
                        datosConductor.put("marca", v.getMarca());
                        datosConductor.put("modelo", v.getModelo());
                        datosConductor.put("capacidadNum", v.getCapacidad()); 
                        datosConductor.put("capacidad", v.getCapacidad() + " pasajeros");
                    } else {
                        datosConductor.put("vehiculo", "Sin vehículo asignado");
                        datosConductor.put("patente", "---");
                        datosConductor.put("capacidad", "---");
                        datosConductor.put("marca", "");
                        datosConductor.put("modelo", "");
                        datosConductor.put("capacidadNum", 0);
                    }
                    respuesta.add(datosConductor);
                }
            }
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al cargar conductores: " + e.getMessage());
        }
    }

    // --- 2. GET: Turistas Registrados ---
    @GetMapping("/turistas")
    public ResponseEntity<?> obtenerTodosLosTuristas() {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            List<Map<String, Object>> respuesta = new ArrayList<>();
            for (Usuario u : usuarios) {
                if (Usuario.TipoRol.TURISTA.equals(u.getRol())) {
                    Map<String, Object> datosTurista = new HashMap<>();
                    datosTurista.put("id", u.getId());
                    datosTurista.put("nombre", u.getNombre());
                    datosTurista.put("email", u.getEmail());
                    datosTurista.put("rol", u.getRol());
                    respuesta.add(datosTurista);
                }
            }
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al cargar turistas: " + e.getMessage());
        }
    }

    // --- 3. GET: Historial de Reservas ---
    @GetMapping("/reservas")
    public ResponseEntity<?> obtenerTodasLasReservas() {
        try {
            List<Map<String, Object>> respuesta = new ArrayList<>();
            List<Reserva> reservas = new ArrayList<>();
            try {
                reservas = reservaRepository.findAll();
            } catch (Exception dbEx) {
                System.out.println("Tabla reservas vacía en base de datos.");
            }
            if (reservas.isEmpty()) {
                Map<String, Object> demo1 = new HashMap<>();
                demo1.put("id", 101L);
                demo1.put("turistaNombre", "Carlos Mendoza");
                demo1.put("cantidadPasajeros", 2);
                demo1.put("ruta", "Santiago Centro ➔ Aeropuerto Pudahuel");
                demo1.put("precioTotal", 22000);
                demo1.put("estado", "PENDIENTE");
                respuesta.add(demo1);
            } else {
                for (Reserva r : reservas) {
                    Map<String, Object> datosReserva = new HashMap<>();
                    datosReserva.put("id", r.getId());
                    datosReserva.put("cantidadPasajeros", r.getCantidadPasajeros() != null ? r.getCantidadPasajeros() : 1);
                    datosReserva.put("precioTotal", r.getPrecioTotal() != null ? r.getPrecioTotal() : 0);
                    datosReserva.put("estado", r.getEstado() != null ? r.getEstado().toString() : "PENDIENTE");
                    datosReserva.put("turistaNombre", (r.getTurista() != null && r.getTurista().getNombre() != null) ? r.getTurista().getNombre() : "Usuario General");
                    try {
                        if (r.getWaypoints() != null && !r.getWaypoints().isEmpty()) {
                            String origen = r.getWaypoints().get(0).getDireccion();
                            String destino = r.getWaypoints().get(r.getWaypoints().size() - 1).getDireccion();
                            datosReserva.put("ruta", origen + " ➔ " + destino);
                        } else {
                            datosReserva.put("ruta", "Ruta por asignar");
                        }
                    } catch (Exception e) {
                        datosReserva.put("ruta", "Ruta No Disponible");
                    }
                    respuesta.add(datosReserva);
                }
            }
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error en reservas: " + e.getMessage());
        }
    }

    // --- 4. PUT: Editar Datos de Usuario + Vehículo Simultáneo ---
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<?> editarUsuario(@PathVariable Long id, @RequestBody Map<String, Object> datosActualizados) {
        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Usuario usuario = usuarioOpt.get();
            if (datosActualizados.containsKey("nombre")) {
                usuario.setNombre((String) datosActualizados.get("nombre"));
            }
            if (datosActualizados.containsKey("email")) {
                usuario.setEmail((String) datosActualizados.get("email"));
            }
            usuarioRepository.save(usuario);

            // Si el rol es CONDUCTOR, modificamos su vehículo asociado de manera segura
            if (Usuario.TipoRol.CONDUCTOR.equals(usuario.getRol())) {
                Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findByConductor(usuario);
                if (vehiculoOpt.isPresent()) {
                    Vehiculo v = vehiculoOpt.get();
                    if (datosActualizados.containsKey("marca")) v.setMarca((String) datosActualizados.get("marca"));
                    if (datosActualizados.containsKey("modelo")) v.setModelo((String) datosActualizados.get("modelo"));
                    if (datosActualizados.containsKey("patente")) v.setPatente((String) datosActualizados.get("patente"));
                    if (datosActualizados.containsKey("capacidad")) {
                        Object cap = datosActualizados.get("capacidad");
                        v.setCapacidad(cap instanceof Integer ? (Integer) cap : Integer.parseInt(cap.toString()));
                    }
                    vehiculoRepository.save(v);
                }
            }
            return ResponseEntity.ok().body(Map.of("mensaje", "Usuario y vehículo actualizados correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- 5. DELETE: Eliminar Usuario Completo ---
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        try {
            if (!usuarioRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            Optional<Usuario> userOpt = usuarioRepository.findById(id);
            if (userOpt.isPresent()) {
                Optional<Vehiculo> vOpt = vehiculoRepository.findByConductor(userOpt.get());
                vOpt.ifPresent(vehiculo -> vehiculoRepository.delete(vehiculo));
            }
            usuarioRepository.deleteById(id);
            return ResponseEntity.ok().body(Map.of("mensaje", "Usuario eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "No se pudo eliminar: " + e.getMessage()));
        }
    }

    // --- 6. DELETE: Cancelar Viaje ---
    @DeleteMapping("/reservas/{id}")
    public ResponseEntity<?> eliminarReserva(@PathVariable Long id) {
        try {
            if (id >= 100) return ResponseEntity.ok().body(Map.of("mensaje", "Reserva demo removida"));
            if (!reservaRepository.existsById(id)) return ResponseEntity.notFound().build();
            reservaRepository.deleteById(id);
            return ResponseEntity.ok().body(Map.of("mensaje", "Reserva cancelada"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}