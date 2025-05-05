package com.proyecto.ProyectoConectacare.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Esta clase es un manejador de excepciones centralizado para gestionar excepciones globalmente
 * en una aplicación Spring Boot. Extiende {@link ResponseEntityExceptionHandler}
 * para proporcionar un manejo personalizado de varios tipos de excepciones.
 *
 * Utiliza la anotación @ControllerAdvice para garantizar que las excepciones lanzadas en los controladores
 * se capturen y se devuelvan las entidades de respuesta apropiadas. La clase
 * proporciona manejo de excepciones comunes, como discrepancias en los argumentos de los métodos,
 * solicitudes HTTP ilegibles, métodos HTTP no compatibles, errores de validación y excepciones genéricas.
 *
 * Cada manejador construye una respuesta de error personalizada, representada por la clase
 * {@code HttpErrorCustomizado}, para proporcionar retroalimentación significativa al cliente.
 */
@ControllerAdvice
public class GestorCentralizadoExcepciones extends ResponseEntityExceptionHandler {

    /**
     * Gestiona escenarios donde el cuerpo de la solicitud HTTP entrante no se puede analizar en un objeto.
     * Normalmente se invoca cuando la carga útil JSON tiene un formato incorrecto o no coincide con el esperado.
     *
     * @param ex: la excepción lanzada cuando el objeto en la solicitud HTTP no se puede analizar.
     * @param headers: las cabeceras HTTP de la solicitud.
     * @param status: el código de estado HTTP asociado con el error.
     * @param request: la solicitud web durante la cual se lanzó la excepción.
     * @return: una {@code ResponseEntity} que contiene un objeto de error personalizado con el mensaje:
     * "No se puede analizar el objeto JSON" y un estado HTTP de 400 (Solicitud incorrecta).
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        HttpErrorCustomizado httpErrorCustomizado = new HttpErrorCustomizado("No se puede parsear el objeto JSON");
        return ResponseEntity.badRequest().body(httpErrorCustomizado);
    }

    /**
     * Gestiona solicitudes que utilizan métodos HTTP no compatibles.
     * Este método se activa cuando un cliente envía una solicitud utilizando un método HTTP
     * no compatible con el endpoint. Se devuelve un mensaje de error personalizado
     * que informa al cliente que no existe un endpoint para atender la solicitud.
     *
     * @param ex: la excepción lanzada cuando se utiliza un método HTTP no compatible.
     * @param headers: las cabeceras HTTP de la solicitud.
     * @param status: el código de estado HTTP asociado al error.
     * @param request: la solicitud web durante la cual se lanzó la excepción.
     * @return: una {@code ResponseEntity} que contiene un objeto de error personalizado con el mensaje
     * "No existe un endpoint para atender esta petición." y un estado HTTP 405 (Método no permitido).
     */
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                         HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        HttpErrorCustomizado httpErrorCustomizado = new HttpErrorCustomizado("No existe end-point para atender esta petición.");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(httpErrorCustomizado);
    }

    /**
     * Gestiona excepciones causadas por discrepancias en el tipo de argumento del método.
     * Esta excepción suele ocurrir cuando el argumento entrante en una solicitud no coincide
     * con el tipo esperado para un parámetro del método.
     *
     * @param ex la excepción lanzada cuando hay una discrepancia entre el tipo del parámetro del método
     * y el tipo del argumento recibido.
     * @return una {@code ResponseEntity} que contiene un objeto de error personalizado con un mensaje detallado
     * que especifica el valor y el tipo recibidos, y el tipo esperado; un estado HTTP de 400 (Solicitud incorrecta).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String tipoRequerido = ex.getRequiredType().getSimpleName();
        String tipoEntrante = ex.getValue().getClass().getSimpleName();
        HttpErrorCustomizado httpErrorCustomizado = new HttpErrorCustomizado("El valor [" + ex.getValue() +
                "] es de tipo [" + tipoEntrante + "]. Se requiere un tipo [" + tipoRequerido + "]");
        return ResponseEntity.badRequest().body(httpErrorCustomizado);
    }

    /**
     * Gestiona excepciones de tipo {@code PresentationException} y devuelve una respuesta de error personalizada.
     * Este método se activa cuando se lanza una {@code PresentationException}, creando un objeto {@code HttpErrorCustomizado} basado en el mensaje de la excepción y devolviéndolo junto con el estado HTTP especificado en la excepción.
     *
     * @param ex la {@code PresentationException} que se gestionará, que contiene los detalles del error y el código de estado HTTP correspondiente.
     * @return una {@code ResponseEntity} que contiene el objeto de error personalizado y el estado HTTP asociado a la excepción.
     */
    @ExceptionHandler(PresentationException.class)
    public ResponseEntity<Object> handlePresentationException(PresentationException ex) {
        HttpErrorCustomizado httpErrorCustomizado = new HttpErrorCustomizado(ex.getMessage());
        return new ResponseEntity<>(httpErrorCustomizado, ex.getHttpStatus());
    }

    /**
     * Gestiona excepciones genéricas que no son detectadas específicamente por otros controladores.
     * Este método se invoca cuando se produce una excepción no gestionada, lo que crea una respuesta de error personalizada que indica un error del servidor.
     *
     * @param ex la excepción lanzada durante el procesamiento de la aplicación.
     * @return una {@code ResponseEntity} que contiene un mensaje de error personalizado en el cuerpo y un estado HTTP de 500 (Error interno del servidor).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        HttpErrorCustomizado httpErrorCustomizado = new HttpErrorCustomizado("Se ha producido un error en el servidor.");
        return ResponseEntity.internalServerError().body(httpErrorCustomizado);
    }
    /**
     * Gestiona errores de validación para argumentos de método anotados con restricciones de validación.
     * Este método se activa cuando se lanza una {@code MethodArgumentNotValidException},
     * normalmente cuando falla una restricción de validación en un parámetro de método.
     *
     * @param ex: la excepción que contiene detalles sobre los fallos de validación.
     * @param headers: las cabeceras HTTP de la solicitud.
     * @param status: el código de estado HTTP asociado con el error de validación.
     * @param request: la solicitud web durante la cual se lanzó la excepción.
     * @return: una {@code ResponseEntity} que contiene un mensaje de error personalizado y un estado HTTP de 400 (Solicitud incorrecta).
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        String mensajeError = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("error", mensajeError);
        return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
    }
}
