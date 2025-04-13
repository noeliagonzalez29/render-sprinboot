package com.proyecto.ProyectoConectacare.service.impl;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.proyecto.ProyectoConectacare.exception.PresentationException;
import com.proyecto.ProyectoConectacare.model.Evaluacion;
import com.proyecto.ProyectoConectacare.service.EvaluacionService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class EvaluacionServiceImpl implements EvaluacionService {
    private static final String COLECCION = "evaluaciones";
    private final Firestore db;

    public EvaluacionServiceImpl(Firestore db) {
        this.db = db;
    }

    @Override
    public Evaluacion createEvaluacion(Evaluacion evaluacion) {
        try {
            DocumentReference docRef = db.collection(COLECCION).document();
            evaluacion.setId(docRef.getId());
            docRef.set(evaluacion).get();
            return evaluacion;
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al crear evaluación", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Evaluacion getEvaluacionById(String id) {
        try {
            DocumentSnapshot document = db.collection(COLECCION).document(id).get().get();
            if (document.exists()) {
                return document.toObject(Evaluacion.class);
            }
            throw new PresentationException("Evaluación no encontrada", HttpStatus.NOT_FOUND);
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al obtener evaluación", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<Evaluacion> getEvaluacionesByTrabajadorId(String trabajadorId) {
        try {
            return db.collection("evaluaciones")
                    .whereEqualTo("trabajadorId", trabajadorId)
                    .get().get().getDocuments()
                    .stream()
                    .map(doc -> {
                        Evaluacion ev = doc.toObject(Evaluacion.class);
                        ev.setId(doc.getId());
                        return ev;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new PresentationException("Error al obtener valoraciones", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
