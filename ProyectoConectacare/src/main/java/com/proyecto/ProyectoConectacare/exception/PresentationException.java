package com.proyecto.ProyectoConectacare.exception;

import org.springframework.http.HttpStatus;

/**
 * La clase {@code PresentationException} es una excepción personalizada que se utiliza para representar errores
 * que ocurren durante la capa de presentación de una aplicación Spring Boot. Esta excepción
 * extiende la {@code RuntimeException}, lo que permite que se lance sin estar declarada explícitamente
 * en la firma del método.
 *
 * Esta clase de excepción está diseñada para encapsular tanto un mensaje de error como un estado HTTP asociado. Se puede utilizar en escenarios donde una aplicación necesita comunicar
 * un código de estado de respuesta específico y un mensaje de error al cliente de forma estandarizada.
 *
 * Características principales:
 * - Asocia un mensaje de error con un estado HTTP.
 * - Proporciona constructores para crear excepciones utilizando una instancia de {@code HttpStatus}
 * o un código de estado entero.
 * - Facilita la gestión de errores al permitir la gestión centralizada de excepciones mediante un controlador de excepciones global. *
 * Normalmente, esta excepción se puede usar en combinación con un controlador de excepciones centralizado (p. ej., {@code @ControllerAdvice}) para devolver respuestas significativas
 * a los clientes en caso de errores.
 */
public class PresentationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private HttpStatus httpStatus;

    /**
     * Construye una nueva {@code PresentationException} con el mensaje de error especificado
     * y el estado HTTP.
     *
     * @param mensaje el mensaje detallado que describe la excepción
     * @param httpStatus el estado HTTP asociado con esta excepción
     */
    public PresentationException(String mensaje, HttpStatus httpStatus) {
        super(mensaje);
        this.httpStatus = httpStatus;
    }

    /**
     * Construye una nueva {@code PresentationException} con el mensaje de error especificado
     * y el código de estado HTTP.
     *
     * @param mensaje el mensaje detallado que describe la excepción
     * @param httpStatusCode el código de estado HTTP asociado con esta excepción
     */
    public PresentationException(String mensaje, int httpStatusCode) {
        super(mensaje);
        this.httpStatus = HttpStatus.valueOf(httpStatusCode);
    }

    /**
     * Recupera el estado HTTP asociado a esta excepción.
     *
     * @return el estado HTTP encapsulado en esta excepción.
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
