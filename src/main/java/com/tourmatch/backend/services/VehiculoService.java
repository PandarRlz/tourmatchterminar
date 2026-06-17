package com.tourmatch.backend.services;

import com.tourmatch.backend.dtos.VehiculoRequest;
import com.tourmatch.backend.models.Usuario;
import com.tourmatch.backend.models.Vehiculo;
import com.tourmatch.backend.repositories.UsuarioRepository;
import com.tourmatch.backend.repositories.VehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VehiculoService {

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Vehiculo registrarVehiculo(VehiculoRequest request, String emailConductor) {
        // 1. Buscamos al usuario por el token
        Usuario conductor = usuarioRepository.findByEmail(emailConductor)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Validamos que su rol sea el correcto
        if (conductor.getRol() != Usuario.TipoRol.CONDUCTOR) {
            throw new RuntimeException("Solo los usuarios con rol de CONDUCTOR pueden registrar vehículos.");
        }

        // 3. Armamos el vehículo
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPatente(request.getPatente());
        vehiculo.setModelo(request.getModelo());
        vehiculo.setCapacidad(request.getCapacidad());
        vehiculo.setConductor(conductor);

        // 4. Lógica automática de categorización
        if (request.getCapacidad() > 4) {
            vehiculo.setTipo(Vehiculo.CategoriaVehiculo.XL);
        } else {
            vehiculo.setTipo(Vehiculo.CategoriaVehiculo.BASICO);
        }

        return vehiculoRepository.save(vehiculo);
    }
}