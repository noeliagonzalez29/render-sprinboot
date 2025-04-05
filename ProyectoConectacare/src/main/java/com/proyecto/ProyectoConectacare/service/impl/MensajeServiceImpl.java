package com.proyecto.ProyectoConectacare.service.impl;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.proyecto.ProyectoConectacare.exception.PresentationException;
import com.proyecto.ProyectoConectacare.model.Mensaje;
import com.proyecto.ProyectoConectacare.service.MensajeService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MensajeServiceImpl implements MensajeService {
    private static final String COLECCION = "mensajes";
    private final Firestore db;

    public MensajeServiceImpl(Firestore db) {
        this.db = db;
    }

    @Override
    public Mensaje sendMensaje(Mensaje mensaje) {
        try {
            DocumentReference docRef = db.collection(COLECCION).document();
            mensaje.setId(docRef.getId());
            mensaje.setFechaEnvio(new Date());  // Firebase puede poner la fecha automáticamente
            docRef.set(mensaje).get();
            return mensaje;
        } catch (Exception e) {
            throw new PresentationException("Error enviando mensaje", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<Mensaje> getMensajesByConversacion(String usuario1Id, String usuario2Id) {
        try {
            // Simplificamos la consulta usando un identificador único de conversación
            String conversacionId = generarIdConversacion(usuario1Id, usuario2Id);

            return db.collection(COLECCION)
                    .whereEqualTo("conversacionId", conversacionId)
                    .orderBy("fecha")
                    .get().get().getDocuments()
                    .stream()
                    .map(doc -> doc.toObject(Mensaje.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new PresentationException("Error obteniendo mensajes", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String generarIdConversacion(String id1, String id2) {
        // Creamos un ID único ordenando los IDs alfabéticamente
        return id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1;
    }


}
