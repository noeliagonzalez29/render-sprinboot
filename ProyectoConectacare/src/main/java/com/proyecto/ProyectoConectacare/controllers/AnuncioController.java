package com.proyecto.ProyectoConectacare.controllers;

import com.proyecto.ProyectoConectacare.model.Anuncio;
import com.proyecto.ProyectoConectacare.service.AnuncioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/anuncios")
public class AnuncioController {
    private final AnuncioService anuncioService;

    public AnuncioController(AnuncioService anuncioService) {
        this.anuncioService = anuncioService;
    }

    @PostMapping
    public ResponseEntity<Anuncio> crearAnuncio(@RequestBody Anuncio anuncio) {
        anuncio.setFechaPublicacion(new Date());
        return new ResponseEntity<>(anuncioService.createAnuncio(anuncio), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public Anuncio obtenerAnuncio(@PathVariable String id) {
        return anuncioService.getAnuncioById(id);
    }

    @GetMapping
    public List<Anuncio> listarAnuncios() {
        return anuncioService.getAllAnuncios();
    }

    @GetMapping("/cliente/{clienteId}")
    public List<Anuncio> obtenerAnunciosCliente(@PathVariable String clienteId) {
        return anuncioService.getAnunciosByClienteId(clienteId);
    }
}
