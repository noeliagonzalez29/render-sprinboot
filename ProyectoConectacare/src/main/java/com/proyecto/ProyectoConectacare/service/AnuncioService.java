package com.proyecto.ProyectoConectacare.service;

import com.proyecto.ProyectoConectacare.model.Anuncio;

import java.util.List;

/**
 * La interfaz AnuncioService define las operaciones para gestionar y recuperar entidades "Anuncio".
 * Un "Anuncio" suele representar un anuncio asociado a un cliente específico.
 * Este servicio proporciona métodos para crear, recuperar y contabilizar anuncios, así como para recuperarlos según los identificadores de cliente.
 */
public interface AnuncioService {
    Anuncio crearAnuncio(String clienteId, Anuncio anuncio);
    Anuncio getAnuncioById(String id);
    List<Anuncio> getAllAnuncios();
    List<Anuncio> getAnunciosByClienteId(String clienteId);
    int contarTotalAnuncios();
}
