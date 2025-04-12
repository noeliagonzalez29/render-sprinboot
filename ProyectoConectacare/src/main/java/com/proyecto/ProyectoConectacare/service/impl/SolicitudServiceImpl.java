package com.proyecto.ProyectoConectacare.service.impl;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
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


    public SolicitudServiceImpl(Firestore db) {
        this.db = db;

    }

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
                    .map(doc -> {
                        Solicitud s = doc.toObject(Solicitud.class);
                        s.setId(doc.getId()); // 👈 importante para tener el id del documento
                        return s;
                    })
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al obtener solicitudes", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    public List<SolicitudConTrabajadorDTO> getSolicitudesByClienteId(String clienteId) {
        try {
            List<String> misAnunciosIds = db.collection("anuncios")
                    .whereEqualTo("clienteId", clienteId)
                    .get().get().getDocuments()
                    .stream()
                    .map(DocumentSnapshot::getId)
                    .toList();

            if (misAnunciosIds.isEmpty()) return new ArrayList<>();

            return db.collection("solicitudes")
                    .whereIn("anuncioId", misAnunciosIds)
                    .get().get().getDocuments()
                    .stream()
                    .map(doc -> {
                        Solicitud solicitud = doc.toObject(Solicitud.class);
                        solicitud.setId(doc.getId());

                        DocumentSnapshot trabajadorDoc = null;
                        try {
                            trabajadorDoc = db.collection("usuarios")
                                    .document(solicitud.getTrabajadorId())
                                    .get().get();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (ExecutionException e) {
                            throw new RuntimeException(e);
                        }

                        return mapToDto(solicitud, trabajadorDoc);
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new PresentationException("Error al obtener solicitudes del cliente", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

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

    public Solicitud marcarComoCompletado(String solicitudId) {
        try {
            DocumentReference docRef = db.collection("solicitudes").document(solicitudId);
            docRef.update("completado", true).get();
            return docRef.get().get().toObject(Solicitud.class);
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al actualizar solicitud", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    public Solicitud actualizarEstadoSolicitud(String solicitudId, EstadoSolicitud nuevoEstado) {
        try {
            DocumentReference docRef = db.collection(COLECCION).document(solicitudId);

            Map<String, Object> updates = new HashMap<>();
            updates.put("estado", nuevoEstado);

            docRef.update(updates).get(); // Espera que se aplique

            // Devolver la solicitud actualizada (opcional)
            DocumentSnapshot snapshot = docRef.get().get();
            return snapshot.toObject(Solicitud.class);

        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al actualizar estado de solicitud", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
