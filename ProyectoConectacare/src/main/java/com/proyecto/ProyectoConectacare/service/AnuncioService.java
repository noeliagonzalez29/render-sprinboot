package com.proyecto.ProyectoConectacare.service;

import com.proyecto.ProyectoConectacare.model.Anuncio;

import java.util.List;

public interface AnuncioService {
    Anuncio crearAnuncio(String clienteId, Anuncio anuncio);
    Anuncio getAnuncioById(String id);
    List<Anuncio> getAllAnuncios();
    List<Anuncio> getAnunciosByClienteId(String clienteId);
}
