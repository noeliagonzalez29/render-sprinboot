package com.proyecto.ProyectoConectacare.exception;

/**
 * La clase {@code HttpErrorCustomizado} se utiliza para encapsular mensajes de error HTTP
 * en un formato estandarizado. Se utiliza principalmente para crear respuestas de error personalizadas
 * en mecanismos de gestión de excepciones dentro de una aplicación Spring Boot.
 *
 * Esta clase proporciona una sola propiedad, {@code error}, que almacena el mensaje de error.
 * Las instancias de esta clase se crean típicamente con un mensaje de error específico
 * y se devuelven como parte del cuerpo de la respuesta HTTP en caso de error.
 */
public class HttpErrorCustomizado {
    private String error;

    public HttpErrorCustomizado(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
