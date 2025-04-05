package com.proyecto.ProyectoConectacare.service.impl;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.proyecto.ProyectoConectacare.exception.PresentationException;
import com.proyecto.ProyectoConectacare.model.Solicitud;
import com.proyecto.ProyectoConectacare.service.SolicitudService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class SolicitudServiceImpl implements SolicitudService {
    private static final String COLECCION = "solicitudes";
    private final Firestore db;

    public SolicitudServiceImpl(Firestore db) {
        this.db = db;
    }

    @Override
    public Solicitud createSolicitud(Solicitud solicitud) {
        try {
            DocumentReference docRef = db.collection(COLECCION).document();
            solicitud.setId(docRef.getId());
            docRef.set(solicitud).get();
            return solicitud;
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al crear solicitud", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Solicitud getSolicitudById(String id) {
        try {
            DocumentSnapshot document = db.collection(COLECCION).document(id).get().get();
            if (document.exists()) {
                return document.toObject(Solicitud.class);
            }
            throw new PresentationException("Solicitud no encontrada", HttpStatus.NOT_FOUND);
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al obtener solicitud", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<Solicitud> getSolicitudesByAnuncioId(String anuncioId) {
        try {
            return db.collection(COLECCION)
                    .whereEqualTo("anuncioId", anuncioId)
                    .get().get().getDocuments()
                    .stream()
                    .map(doc -> doc.toObject(Solicitud.class))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al obtener solicitudes", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<Solicitud> getSolicitudesByTrabajadorId(String trabajadorId) {
        try {
            return db.collection(COLECCION)
                    .whereEqualTo("trabajadorId", trabajadorId)
                    .get().get().getDocuments()
                    .stream()
                    .map(doc -> doc.toObject(Solicitud.class))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al obtener solicitudes", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
