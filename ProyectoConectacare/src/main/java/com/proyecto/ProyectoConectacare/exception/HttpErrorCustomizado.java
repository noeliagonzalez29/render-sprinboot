package com.proyecto.ProyectoConectacare.exception;

public class HttpErrorCustomizado {
    private String error;

    public HttpErrorCustomizado(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
