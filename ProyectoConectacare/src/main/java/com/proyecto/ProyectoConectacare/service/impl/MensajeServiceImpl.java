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

    /**
     * Envía un mensaje guardándolo en la base de datos. El método asigna un ID único al mensaje,
     * establece la marca de tiempo de su envío y lo almacena en la colección especificada.
     *
     * @param mensaje El objeto del mensaje que se enviará. Debe contener el ID del remitente, el ID del destinatario y el contenido del mensaje.
     * @return El objeto del mensaje con el ID generado y la marca de tiempo establecida.
     * @throws PresentationException Si se produce un error al guardar el mensaje.
     */
    @Override
    public Mensaje mandarMensaje(Mensaje mensaje) {
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

    /**
     * Recupera una lista de mensajes intercambiados entre dos usuarios en una conversación específica,
     * ordenados por fecha de envío.
     *
     * @param usuario1Id El identificador único del primer usuario.
     * @param usuario2Id El identificador único del segundo usuario.
     * @return Una lista de objetos {@code Mensaje} que representan los mensajes de la conversación.
     * @throws PresentationException Si se produce un error al recuperar los mensajes.
     */
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

    /**
     * Genera un ID de conversación único ordenando alfabéticamente dos ID de usuario y concatenándolos con un guion bajo.
     *
     * @param id1 El primer ID de usuario que se incluirá en el ID de conversación.
     * @param id2 El segundo ID de usuario que se incluirá en el ID de conversación.
     * @return Un ID de conversación único formado por la combinación de los dos ID de usuario en orden alfabético, separados por un guion bajo.
     */
    private String generarIdConversacion(String id1, String id2) {
        //  ID único ordenando los IDs alfabéticamente
        return id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1;
    }


}
