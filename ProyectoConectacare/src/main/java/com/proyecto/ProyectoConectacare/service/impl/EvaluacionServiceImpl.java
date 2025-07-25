package com.proyecto.ProyectoConectacare.service.impl;

import com.google.cloud.firestore.*;
import com.proyecto.ProyectoConectacare.dto.EvaluacionDTO;
import com.proyecto.ProyectoConectacare.exception.PresentationException;
import com.proyecto.ProyectoConectacare.model.Evaluacion;
import com.proyecto.ProyectoConectacare.model.Usuario;
import com.proyecto.ProyectoConectacare.service.EvaluacionService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
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
        DocumentReference docRef = null; // Para tener acceso en el catch si es necesario
        try {
            docRef = db.collection(COLECCION).document();
            evaluacion.setId(docRef.getId());
            docRef.set(evaluacion).get(); // Espera a que la evaluación se cree

            System.out.println("DEBUG Servicio: Evaluación creada con ID: " + evaluacion.getId());

            // Llamar a marcar el chat DESPUÉS de confirmar la creación de la evaluación
            marcarChatComoEvaluadoTrasCreacion(evaluacion.getClienteId(), evaluacion.getTrabajadorId(), evaluacion.getSolicitudId());

            return evaluacion;
        } catch (InterruptedException | ExecutionException e) {
            // Es importante loguear qué operación falló
            String operation = (docRef != null && evaluacion.getId() != null) ? "marcar chat" : "crear evaluación";
            System.err.println("ERROR CRÍTICO en createEvaluacion durante " + operation + ": " + e.getMessage());
            e.printStackTrace();
            throw new PresentationException("Error durante la creación de la evaluación o tareas posteriores", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) { // Otra captura genérica por si acaso
            System.err.println("ERROR INESPERADO en createEvaluacion: " + e.getMessage());
            e.printStackTrace();
            throw new PresentationException("Error inesperado durante el proceso de evaluación", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private void marcarChatComoEvaluadoTrasCreacion(String clienteId, String trabajadorId, String solicitudIdRelacionada) {
        if (clienteId == null || trabajadorId == null) {
            System.err.println("ERROR CRÍTICO en Servicio: ClienteId o TrabajadorId es null. No se puede marcar el chat para la solicitud: " + solicitudIdRelacionada);
            return;
        }
        System.out.println("DEBUG Servicio: Intentando marcar chat. ClienteID: " + clienteId + ", TrabajadorID: " + trabajadorId + ", SolicitudID: " + solicitudIdRelacionada);

        try {
            String chatId = Arrays.stream(new String[]{clienteId, trabajadorId})
                    .sorted()
                    .collect(Collectors.joining("_"));
            System.out.println("DEBUG Servicio: ChatID construido: " + chatId);

            DocumentReference chatRef = db.collection(COLECCION_CHATS).document(chatId);
            Map<String, Object> updates = new HashMap<>();
            updates.put("evaluadoPorCliente", true); // Booleano
            updates.put("solicitudEvaluadaId", solicitudIdRelacionada);


            chatRef.update(updates).get();



        } catch (ExecutionException e) {
            System.err.println("ERROR CRÍTICO en Servicio (ExecutionException) al marcar el chat para solicitud " + solicitudIdRelacionada + " como evaluado: " + e.getMessage());

        } catch (InterruptedException e) {
            System.err.println("ERROR CRÍTICO en Servicio (InterruptedException) al marcar el chat para solicitud " + solicitudIdRelacionada + " como evaluado: " + e.getMessage());

        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO en Servicio (Exception Genérica) al marcar el chat para solicitud " + solicitudIdRelacionada + " como evaluado: " + e.getMessage());

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
    public List<EvaluacionDTO> getEvaluacionesByTrabajadorId(String trabajadorId) {
        try {
            List<QueryDocumentSnapshot> docs = db.collection("evaluaciones")
                    .whereEqualTo("trabajadorId", trabajadorId)
                    .get().get().getDocuments();

            List<EvaluacionDTO> evaluacionesDTO = new ArrayList<>();

            for (QueryDocumentSnapshot doc : docs) {
                Evaluacion ev = doc.toObject(Evaluacion.class);
                String clienteId = ev.getClienteId();

                DocumentSnapshot clienteDoc = db.collection("usuarios").document(clienteId).get().get();
                String nombreCliente = clienteDoc.exists() && clienteDoc.contains("nombre")
                        ? clienteDoc.getString("nombre")
                        : "Desconocido";

                EvaluacionDTO dto = new EvaluacionDTO();
                dto.setId(doc.getId());
                dto.setComentario(ev.getComentario());
                dto.setEstrellas(ev.getEstrellas());
                dto.setFechaEvaluacion(ev.getFechaEvaluacion());
                dto.setNombreCliente(nombreCliente);

                evaluacionesDTO.add(dto);
            }

            return evaluacionesDTO;

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

    @Override
    public Map<String, Object> getValoracionMedia() {
        List<Evaluacion> todasLasEvaluaciones = this.getAllEvaluaciones(); // Reutilizamos el método que ya tienes

        Map<String, Object> resultado = new HashMap<>();

        if (todasLasEvaluaciones == null || todasLasEvaluaciones.isEmpty()) {
            resultado.put("mediaEstrellas", 0.0); // O null si prefieres, pero 0.0 es más seguro para el cálculo en front
            resultado.put("totalValoraciones", 0);
            return resultado;
        }

        double sumaEstrellas = 0;
        for (Evaluacion evaluacion : todasLasEvaluaciones) {
            // Es importante asegurarse que 'estrellas' no sea null si es un Integer o int primitivo.
            // Si 'estrellas' en tu clase Evaluacion es un 'int', no puede ser null.
            // Si es 'Integer', podría ser null y necesitarías un chequeo.
            // Por la imagen, parece ser un `int` o `Integer` no nulo.
            sumaEstrellas += evaluacion.getEstrellas();
        }

        double media = sumaEstrellas / todasLasEvaluaciones.size();

        // Formatear la media a uno o dos decimales si es necesario (opcional aquí, se puede hacer en front)
        // double mediaFormateada = Math.round(media * 10.0) / 10.0; // Un decimal
        // double mediaFormateada = Math.round(media * 100.0) / 100.0; // Dos decimales

        resultado.put("mediaEstrellas", media); // Usamos la media sin formatear aquí
        resultado.put("totalValoraciones", todasLasEvaluaciones.size());

        return resultado;

    }

}
