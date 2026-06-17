package com.tourmatch.backend.dtos;

import lombok.Data;

@Data
public class RegistroRequest {
    private String nombre;
    private String email;
    private String password;
    private String rol; // "TURISTA" o "CONDUCTOR"
}