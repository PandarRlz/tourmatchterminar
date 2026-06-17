package com.tourmatch.backend.dtos;

public class AuthResponse {
    private String token;
    private String rol;
    private String nombre;
    private String mensaje;

    // Constructor, Getters y Setters
    public AuthResponse(String token, String rol, String nombre, String mensaje) {
        this.token = token;
        this.rol = rol;
        this.nombre = nombre;
        this.mensaje = mensaje;
    }

    public String getToken() { return token; }
    public String getRol() { return rol; }
    public String getNombre() { return nombre; }
    public String getMensaje() { return mensaje; }
}