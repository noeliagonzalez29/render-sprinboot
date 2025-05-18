package com.proyecto.ProyectoConectacare.service.impl;

import com.google.cloud.firestore.*;
import com.proyecto.ProyectoConectacare.exception.PresentationException;
import com.proyecto.ProyectoConectacare.model.Evaluacion;
import com.proyecto.ProyectoConectacare.model.Usuario;
import com.proyecto.ProyectoConectacare.service.EvaluacionService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class EvaluacionServiceImpl implements EvaluacionService {
    private static final String COLECCION = "evaluaciones";
    private static final String COLECCION_CHATS = "chats";
    private final Firestore db;

    public EvaluacionServiceImpl(Firestore db) {
        this.db = db;
    }

    /**
     * Crea una nueva Evaluación y la almacena en la base de datos.
     *
     * @param evaluacion el objeto de Evaluación que se creará y guardará.
     * @return la Evaluación recién creada con su ID generado.
     * @throws PresentationException si hay un error durante el proceso de creación.
     */
    @Override
    public Evaluacion createEvaluacion(Evaluacion evaluacion) {
        try {
            DocumentReference docRef = db.collection(COLECCION).document();
            evaluacion.setId(docRef.getId());
            docRef.set(evaluacion).get();
            marcarChatComoEvaluadoTrasCreacion(evaluacion.getClienteId(), evaluacion.getTrabajadorId(), evaluacion.getSolicitudId());
            return evaluacion;
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al crear evaluación", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private void marcarChatComoEvaluadoTrasCreacion(String clienteId, String trabajadorId, String solicitudIdRelacionada) {
        if (clienteId == null || trabajadorId == null) {
            System.err.println("ClienteId o TrabajadorId es null. No se puede marcar el chat para la solicitud: " + solicitudIdRelacionada);
            return;
        }
        try {
            // Recrear el ID del chat como lo haces en el frontend (ordenado)
            String chatId = Arrays.stream(new String[]{clienteId, trabajadorId})
                    .sorted()
                    .collect(Collectors.joining("_"));

            DocumentReference chatRef = db.collection(COLECCION_CHATS).document(chatId);
            Map<String, Object> updates = new HashMap<>();
            updates.put("evaluadoPorCliente", true);
            updates.put("solicitudEvaluadaId", solicitudIdRelacionada);

            System.out.println("Servicio: Chat " + chatId + " marcado como evaluadoPorCliente para solicitud " + solicitudIdRelacionada);

        } catch (Exception e) {
            System.err.println("Error en servicio al marcar el chat para solicitud " + solicitudIdRelacionada + " como evaluado: " + e.getMessage());

        }
    }
    /**
     * Recupera un objeto de evaluación de la base de datos mediante su identificador único.
     *
     * @param id: el identificador único de la evaluación que se recuperará.
     * @return: el objeto de evaluación correspondiente al identificador especificado.
     * @throws: PresentationException si no se encuentra la evaluación o si se produce un error durante el proceso de recuperación.
     */
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

    /**
     * Recupera una lista de objetos de evaluación asociados a un trabajadorId específico.
     *
     * @param trabajadorId: el identificador único del trabajador cuyas evaluaciones se recuperarán.
     * @return: una lista de objetos de evaluación correspondientes al trabajadorId especificado.
     * @throws: PresentationException si se produce un error durante el proceso de recuperación.
     */
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

    @Override
    public boolean existeEvaluacionPorSolicitud(String solicitudId) {
        // Consulta directa en Firestore para evitar traer todas las evaluaciones
        CollectionReference evaluacionesRef =db.collection(COLECCION);
        Query query = evaluacionesRef.whereEqualTo("solicitudId", solicitudId).limit(1);

        try {
            return !query.get().get().isEmpty(); // Si hay al menos 1 documento, retorna true
        } catch (Exception e) {
            throw new RuntimeException("Error al verificar evaluación", e);
        }
    }

    @Override
    public List<Evaluacion> getAllEvaluaciones() {
        try {
            return db.collection(COLECCION).get().get().getDocuments()
                    .stream()
                    .map(doc -> doc.toObject(Evaluacion.class))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al obtener las evaluaciones", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
