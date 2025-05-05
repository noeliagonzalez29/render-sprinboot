package com.proyecto.ProyectoConectacare.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.proyecto.ProyectoConectacare.exception.PresentationException;
import com.proyecto.ProyectoConectacare.model.Anuncio;
import com.proyecto.ProyectoConectacare.service.AnuncioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

/**
 * Clase controladora responsable de gestionar las solicitudes HTTP relacionadas con los recursos de "Anuncio".
 * Proporciona puntos de conexión para crear, recuperar y obtener Anuncios específicos o todos los Anuncios.
 * La clase utiliza la autenticación de Firebase para la verificación de tokens y delega la lógica principal
 * al Servicio de Anuncios para las operaciones con datos.
 */
@RestController
@RequestMapping("/anuncios")
public class AnuncioController {
    private final AnuncioService anuncioService;
    private final FirebaseAuth firebaseAuth;
    public AnuncioController(AnuncioService anuncioService, FirebaseAuth firebaseAuth) {
        this.anuncioService = anuncioService;
        this.firebaseAuth = firebaseAuth;
    }

    /**
     * Crea un nuevo recurso "Anuncio" asociado al usuario autenticado.
     *
     * @param token: el token de autorización, que debe incluir el prefijo "Bearer" para autenticar la solicitud.
     * @param anuncio: el objeto "Anuncio" que contiene los datos que se crearán.
     * @return: una {@code ResponseEntity} que contiene el objeto "Anuncio" creado y un estado HTTP de CREADO.
     * @throws: una PresentationException si el token proporcionado no es válido o la autenticación falla.
     */
    @PostMapping
    public ResponseEntity<Anuncio> crearAnuncio(@RequestHeader("Authorization") String token, @RequestBody Anuncio anuncio) {
        try {

            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            FirebaseToken decoded = firebaseAuth.verifyIdToken(token);
            String uid = decoded.getUid();

             anuncio = anuncioService.crearAnuncio(uid,anuncio);
            return new ResponseEntity<>(anuncio, HttpStatus.CREATED);
        } catch (FirebaseAuthException e) {
            throw new PresentationException("Token inválido", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Recupera un recurso "Anuncio" por su identificador único.
     *
     * @param id: el identificador único del "Anuncio" que se recuperará.
     * @return: el objeto "Anuncio" correspondiente al identificador especificado.
     */
    @GetMapping("/{id}")
    public Anuncio obtenerAnuncio(@PathVariable String id) {
        return anuncioService.getAnuncioById(id);
    }

    /**
     * Recupera todos los recursos de "Anuncio" sin filtrar por cliente.
     *
     * @return una {@code ResponseEntity} que contiene una lista de todos los objetos de "Anuncio" y un estado HTTP correcto.
     */
    @GetMapping
    public ResponseEntity<List<Anuncio>> obtenerTodos() {
        List<Anuncio> anuncios = anuncioService.getAllAnuncios(); // devuelve todos sin filtrar por cliente
        return new ResponseEntity<>(anuncios, HttpStatus.OK);
    }

    /**
     * Recupera una lista de recursos "Anuncio" asociados a un cliente específico.
     *
     * @param clienteId: el identificador único del cliente cuyos recursos "Anuncio" se recuperarán.
     * @return: una lista de objetos "Anuncio" relacionados con el cliente especificado.
     */
    @GetMapping("/cliente/{clienteId}")
    public List<Anuncio> obtenerAnunciosCliente(@PathVariable String clienteId) {
        return anuncioService.getAnunciosByClienteId(clienteId);
    }
}
