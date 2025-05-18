package com.proyecto.ProyectoConectacare.service.impl;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.proyecto.ProyectoConectacare.dto.SolicitudConTrabajadorDTO;
import com.proyecto.ProyectoConectacare.exception.PresentationException;
import com.proyecto.ProyectoConectacare.model.EstadoSolicitud;
import com.proyecto.ProyectoConectacare.model.Solicitud;
import com.proyecto.ProyectoConectacare.service.SolicitudService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class SolicitudServiceImpl implements SolicitudService {
    private static final String COLECCION = "solicitudes";
    private final Firestore db;
    private static final String COLECCION_ANUNCIOS = "anuncios";
    private static final String COLECCION_EVALUACIONES = "evaluaciones";
    private static final String COLECCION_USUARIOS = "usuarios";

    public SolicitudServiceImpl(Firestore db) {
        this.db = db;

    }

    /**
     * Crea una nueva instancia de Solicitud y la persiste en la base de datos.
     * El método recupera información relacionada de la colección "anuncios"
     * para rellenar el objeto Solicitud con los datos necesarios.
     *
     * @param solicitud El objeto Solicitud que contiene los datos iniciales de la nueva instancia.
     * Debe incluir un ID de Anuncio válido para vincular la solicitud a un anuncio existente.
     * @return El objeto Solicitud completamente creado con su ID, estado y campos adicionales configurados.
     * @throws PresentationException Si no se encuentra el Anuncio relacionado o se produce algún error al crear la Solicitud.
     */
    @Override
    public Solicitud createSolicitud(Solicitud solicitud) {
        try {
            // 1. Obtener el anuncio relacionado
            DocumentSnapshot anuncioDoc = db.collection("anuncios")
                    .document(solicitud.getAnuncioId())
                    .get().get();

            if (!anuncioDoc.exists()) {
                throw new PresentationException("Anuncio no encontrado", HttpStatus.NOT_FOUND);
            }

            // 2. Asignar clienteId desde el anuncio
            String clienteId = anuncioDoc.getString("clienteId");
            solicitud.setClienteId(clienteId);

            // 3. Crear la solicitud
            DocumentReference docRef = db.collection(COLECCION).document();
            solicitud.setId(docRef.getId());
            solicitud.setEstado(EstadoSolicitud.PENDIENTE);
            docRef.set(solicitud).get();

            return solicitud;

        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al crear solicitud", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Obtiene y recupera de la base de datos un objeto Solicitud correspondiente al ID especificado.
     *
     * @param id El identificador único de la Solicitud que se recuperará.
     * @return El objeto Solicitud asociado al ID dado.
     * @throws PresentationException si no se encuentra la Solicitud o si hay un error durante la recuperación.
     */
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

    /**
     * Recupera una lista de objetos Solicitud asociados a un ID de anuncio específico.
     *
     * @param anuncioId: el ID del anuncio para el que se deben recuperar las solicitudes.
     * @return: una lista de objetos Solicitud asociados al ID de anuncio especificado.
     * @throws: PresentationException si se produce un error durante el proceso de recuperación.
     */
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

    /**
     * Recupera una lista de objetos "Solicitud" asociados con el trabajadorId especificado.
     *
     * @param trabajadorId: el ID del trabajador cuyas solicitudes se recuperarán.
     * @return: una lista de objetos "Solicitud" asociados con el trabajadorId proporcionado.
     * @throws: una excepción PresentationException si se produce un error durante el proceso de recuperación.
     */
    @Override
    public List<Solicitud> getSolicitudesByTrabajadorId(String trabajadorId) {
        try {
            return db.collection(COLECCION)
                    .whereEqualTo("trabajadorId", trabajadorId)
                    .get().get().getDocuments()
                    .stream()
                    .map(doc -> {
                        Solicitud s = doc.toObject(Solicitud.class);
                        s.setId(doc.getId());
                        return s;
                    })
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al obtener solicitudes", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /**
     * Recupera una lista de objetos "SolicitudConTrabajadorDTO" asociados a un ID de cliente específico.
     * El método obtiene datos de varias colecciones, incluyendo "anuncios", "solicitudes",
     * "evaluaciones" y "usuarios". Mapea los datos recuperados en DTO, gestionando propiedades adicionales como el estado de finalización y la existencia de la evaluación.
     *
     * @param clienteId El identificador único del cliente cuyas solicitudes se recuperarán.
     * @return Una lista de objetos "SolicitudConTrabajadorDTO" asociados al ID de cliente especificado.
     * Si no se encuentran solicitudes, se devuelve una lista vacía.
     * @throws PresentationException Si se produce algún error durante la recuperación de la base de datos o la asignación de datos.
     */
    @Override
    public List<SolicitudConTrabajadorDTO> getSolicitudesByClienteId(String clienteId) {
        try {
            // 1. Obtener los IDs de los anuncios del cliente
            List<String> misAnunciosIds = db.collection(COLECCION_ANUNCIOS)
                    .whereEqualTo("clienteId", clienteId)
                    .get().get().getDocuments()
                    .stream()
                    .map(DocumentSnapshot::getId)
                    .collect(Collectors.toList()); // Usar collect(Collectors.toList()) para compatibilidad

            if (misAnunciosIds.isEmpty()) {
                return new ArrayList<>();
            }

            // 2. Obtener todas las solicitudes para esos anuncios
            List<QueryDocumentSnapshot> solicitudDocumentos = db.collection(COLECCION)
                    .whereIn("anuncioId", misAnunciosIds)
                    .get().get().getDocuments();

            List<SolicitudConTrabajadorDTO> dtosNoEvaluadas = new ArrayList<>();

            for (QueryDocumentSnapshot doc : solicitudDocumentos) {
                Solicitud solicitud = doc.toObject(Solicitud.class);
                solicitud.setId(doc.getId()); // Asegurar que el ID esté presente

                // 3. Verificar si existe una evaluación para esta solicitud
                boolean existeEvaluacion;
                try {
                    // Hacemos una consulta a la colección 'evaluaciones' para ver si hay algún
                    // documento con el 'solicitudId' actual. Usamos limit(1) porque solo
                    // necesitamos saber si existe al menos uno.
                    existeEvaluacion = !db.collection(COLECCION_EVALUACIONES)
                            .whereEqualTo("solicitudId", solicitud.getId())
                            .limit(1)
                            .get().get().isEmpty();
                } catch (InterruptedException | ExecutionException e) {
                    // Si hay un error al verificar la evaluación, por precaución, podríamos
                    // omitir esta solicitud o loguear y decidir. Aquí la omitimos para
                    // evitar mostrar una evaluada por error.
                    System.err.println("Error al verificar evaluación para solicitud " + solicitud.getId() + ": " + e.getMessage() + ". Omitiendo.");
                    continue;
                }

                // 4. Si ya existe una evaluación, saltamos esta solicitud
                if (existeEvaluacion) {
                    continue;
                }

                // 5. Si no hay evaluación, procedemos a obtener datos del trabajador y crear el DTO
                DocumentSnapshot trabajadorDoc = null;
                if (solicitud.getTrabajadorId() != null && !solicitud.getTrabajadorId().isEmpty()){
                    try {
                        trabajadorDoc = db.collection(COLECCION_USUARIOS)
                                .document(solicitud.getTrabajadorId())
                                .get().get();
                    } catch (InterruptedException | ExecutionException e) {
                        System.err.println("Error obteniendo datos del trabajador " + solicitud.getTrabajadorId() + ": " + e.getMessage());
                        // trabajadorDoc permanecerá null, mapToDto debe manejarlo
                    }
                }

                SolicitudConTrabajadorDTO dto = mapToDto(solicitud, trabajadorDoc);
                dto.setCompletado(solicitud.isCompletado()); // isCompletado() es el getter para boolean
                dto.setEvaluacionExistente(false); // Ya filtramos, así que sabemos que no existe evaluación

                dtosNoEvaluadas.add(dto);
            }
            return dtosNoEvaluadas;

        } catch (Exception e) { // Captura más general para la lógica principal
            throw new PresentationException("Error al obtener solicitudes del cliente: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /**
     * Asigna una Solicitud y su DocumentSnapshot asociado de un trabajador a una SolicitudConTrabajadorDTO.
     *
     * @param solicitud: el objeto de solicitud que contiene la información principal de la solicitud.
     * @param trabajadorDoc: el DocumentSnapshot que contiene los detalles del trabajador.
     * @return: una SolicitudConTrabajadorDTO rellenada con los datos de la solicitud y trabajadorDoc.
     */
    private SolicitudConTrabajadorDTO mapToDto(Solicitud solicitud, DocumentSnapshot trabajadorDoc) {
        SolicitudConTrabajadorDTO dto = new SolicitudConTrabajadorDTO();
        dto.setId(solicitud.getId());
        dto.setAnuncioId(solicitud.getAnuncioId());
        dto.setTrabajadorId(solicitud.getTrabajadorId());
        dto.setFechaSolicitud(solicitud.getFechaSolicitud());
        dto.setEstado(solicitud.getEstado());

        if (trabajadorDoc.exists()) {
            dto.setTrabajadorNombre(trabajadorDoc.getString("nombre") + " " + trabajadorDoc.getString("apellido"));
            dto.setDisponibilidad(trabajadorDoc.getString("disponibilidad"));
            dto.setEstudios(trabajadorDoc.getString("estudios"));
            dto.setExperiencia(trabajadorDoc.getString("experiencia"));

            // Manejar habilidades de forma segura
            Object habilidadesObj = trabajadorDoc.get("habilidades");
            if (habilidadesObj instanceof List) {
                dto.setHabilidades((List<String>) habilidadesObj);
            } else {
                dto.setHabilidades(new ArrayList<>());
            }
        } else {
            dto.setTrabajadorNombre("Trabajador no encontrado");
            dto.setHabilidades(new ArrayList<>());
        }

        return dto;
    }

    /**
     * Marca una solicitud específica como completada en la base de datos y recupera el objeto de solicitud actualizado.
     *
     * @param solicitudId: el identificador de la solicitud que se marcará como completada.
     * @return: el objeto de Solicitud actualizado después de que se haya marcado como completada.
     * @throws: PresentationException si hay un error al actualizar la solicitud en la base de datos.
     */
    public Solicitud marcarComoCompletado(String solicitudId) {
        try {
            DocumentReference docRef = db.collection("solicitudes").document(solicitudId);
            docRef.update("completado", true).get();
            return docRef.get().get().toObject(Solicitud.class);
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al actualizar solicitud", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /**
     * Actualiza el estado de una solicitud existente en la base de datos.
     *
     * @param solicitudId El identificador único de la solicitud que se desea actualizar.
     * @param nuevoEstado El nuevo estado que se desea asignar a la solicitud.
     * @return La solicitud actualizada con los datos más recientes desde la base de datos.
     * @throws PresentationException Si ocurre un error durante la actualización o recuperación de la solicitud.
     */
    @Override
    public Solicitud actualizarEstadoSolicitud(String solicitudId, EstadoSolicitud nuevoEstado) {
        try {
            DocumentReference docRef = db.collection(COLECCION).document(solicitudId);

            Map<String, Object> updates = new HashMap<>();
            updates.put("estado", nuevoEstado);

            docRef.update(updates).get(); // Espera que se aplique

            DocumentSnapshot snapshot = docRef.get().get();
            return snapshot.toObject(Solicitud.class);

        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al actualizar estado de solicitud", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
