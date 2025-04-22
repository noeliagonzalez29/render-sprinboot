package com.proyecto.ProyectoConectacare.service.impl;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.proyecto.ProyectoConectacare.exception.PresentationException;
import com.proyecto.ProyectoConectacare.model.Usuario;
import com.proyecto.ProyectoConectacare.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioServiceImpl.class);
    private Firestore db;
    private final String COLECCION = "usuarios";

    public UsuarioServiceImpl(Firestore db) {
        this.db = db;
    }

    @Override
    public Usuario createUsuario(Usuario usuario) {
        try {
            DocumentReference docRef = db.collection(COLECCION).document(usuario.getId());
            docRef.set(usuario).get();
            return usuario;
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al crear usuario", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Usuario updateUsuario(String id, Map<String, Object> updates) {
        try {
            logger.debug("Iniciando actualización para el usuario: {}", id);
            DocumentReference docRef = db.collection(COLECCION).document(id);

            // Verificar existencia
            DocumentSnapshot snapshot = docRef.get().get();
            if (!snapshot.exists()) {
                logger.error("Usuario no encontrado: {}", id);
                throw new PresentationException("Usuario no encontrado", HttpStatus.NOT_FOUND);
            }

            logger.debug("Campos a actualizar: {}", updates);
            docRef.update(updates).get();
            logger.debug("Campos actualizados correctamente");

            DocumentSnapshot updatedSnapshot = docRef.get().get();
            return updatedSnapshot.toObject(Usuario.class);

        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error al actualizar usuario: {}", e.getMessage(), e); // <-- Log detallado
            throw new PresentationException("Error interno", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @Override
    public Usuario getUsuarioById(String id) {
        try {
            DocumentSnapshot document = db.collection(COLECCION).document(id).get().get();
            if (document.exists()) {
                System.out.println("Datos del documento: " + document.getData());
                return document.toObject(Usuario.class);
            }
            throw new PresentationException("Usuario no encontrado", HttpStatus.NOT_FOUND);
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al obtener usuario", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<Usuario> getAllUsuarios() {
        try {
            return db.collection(COLECCION).get().get().getDocuments()
                    .stream()
                    .map(doc -> doc.toObject(Usuario.class))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al obtener usuarios", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteUsuario(String id) {
        try {
            DocumentReference docRef = db.collection(COLECCION).document(id);
            if (!docRef.get().get().exists()) {
                throw new PresentationException("Usuario no encontrado", HttpStatus.NOT_FOUND);
            }
            docRef.delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al eliminar usuario", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
