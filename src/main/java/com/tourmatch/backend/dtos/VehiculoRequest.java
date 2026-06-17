package com.tourmatch.backend.dtos;

import lombok.Data;

@Data
public class VehiculoRequest {
    private String patente;
    private String modelo;
    private Integer capacidad;
}