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

@RestController
@RequestMapping("/anuncios")
public class AnuncioController {
    private final AnuncioService anuncioService;
    private final FirebaseAuth firebaseAuth;
    public AnuncioController(AnuncioService anuncioService, FirebaseAuth firebaseAuth) {
        this.anuncioService = anuncioService;
        this.firebaseAuth = firebaseAuth;
    }

    @PostMapping
    public ResponseEntity<Anuncio> crearAnuncio(@RequestHeader("Authorization") String token, @RequestBody Anuncio anuncio) {
        try {
            // Verifica token
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

    @GetMapping("/{id}")
    public Anuncio obtenerAnuncio(@PathVariable String id) {
        return anuncioService.getAnuncioById(id);
    }

    @GetMapping
    public ResponseEntity<List<Anuncio>> obtenerTodos() {
        List<Anuncio> anuncios = anuncioService.getAllAnuncios(); // devuelve todos sin filtrar por cliente
        return new ResponseEntity<>(anuncios, HttpStatus.OK);
    }

    @GetMapping("/cliente/{clienteId}")
    public List<Anuncio> obtenerAnunciosCliente(@PathVariable String clienteId) {
        return anuncioService.getAnunciosByClienteId(clienteId);
    }
}
