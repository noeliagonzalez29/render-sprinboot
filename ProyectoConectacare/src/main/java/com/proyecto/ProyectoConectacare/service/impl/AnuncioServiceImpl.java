package com.proyecto.ProyectoConectacare.service.impl;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.proyecto.ProyectoConectacare.exception.PresentationException;
import com.proyecto.ProyectoConectacare.model.Anuncio;
import com.proyecto.ProyectoConectacare.service.AnuncioService;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Data
public class AnuncioServiceImpl implements AnuncioService {
    private static final String COLECCION = "anuncios";
    private final Firestore db;

    public AnuncioServiceImpl(Firestore db) {
        this.db = db;
    }

    @Override
    public Anuncio createAnuncio(Anuncio anuncio) {
        try {
            DocumentReference docRef = db.collection(COLECCION).document();
            anuncio.setId(docRef.getId());
            docRef.set(anuncio).get();
            return anuncio;
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al crear anuncio", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Anuncio getAnuncioById(String id) {
        try {
            DocumentSnapshot document = db.collection(COLECCION).document(id).get().get();
            if (document.exists()) {
                return document.toObject(Anuncio.class);
            }
            throw new PresentationException("Anuncio no encontrado", HttpStatus.NOT_FOUND);
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al obtener anuncio", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<Anuncio> getAllAnuncios() {
        try {
            return db.collection(COLECCION).get().get().getDocuments()
                    .stream()
                    .map(doc -> doc.toObject(Anuncio.class))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al obtener anuncios", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<Anuncio> getAnunciosByClienteId(String clienteId) {
        try {
            return db.collection(COLECCION)
                    .whereEqualTo("clienteId", clienteId)
                    .get().get().getDocuments()
                    .stream()
                    .map(doc -> doc.toObject(Anuncio.class))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al obtener anuncios", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
