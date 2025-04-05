package com.proyecto.ProyectoConectacare.service.impl;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.proyecto.ProyectoConectacare.exception.PresentationException;
import com.proyecto.ProyectoConectacare.model.Usuario;
import com.proyecto.ProyectoConectacare.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {
    private Firestore db;
    private final String COLECCION = "usuarios";

    public UsuarioServiceImpl(Firestore db) {
        this.db = db;
    }

    @Override
    public Usuario createUsuario(Usuario usuario) {
        try {
            DocumentReference docRef = db.collection(COLECCION).document();
            usuario.setId(docRef.getId());
            docRef.set(usuario).get();
            return usuario;
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al crear usuario", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Usuario updateUsuario(String id, Map<String, Object> updates) {
        try {
            DocumentReference docRef = db.collection(COLECCION).document(id);

            // Verificar si el documento existe
            DocumentSnapshot snapshot = docRef.get().get();
            if (!snapshot.exists()) {
                throw new PresentationException("Usuario no encontrado", HttpStatus.NOT_FOUND);
            }

            // Logs para depurar
            System.out.println("Campos recibidos para actualizar: " + updates);
            System.out.println("Tipo de 'necesidades' en Firestore: " + snapshot.get("necesidades"));

            // Actualizar usando set() con merge para evitar problemas con update()
            docRef.set(updates, SetOptions.merge()).get();

            // Obtener y retornar el usuario actualizado
            DocumentSnapshot updatedSnapshot = docRef.get().get();
            return updatedSnapshot.toObject(Usuario.class);

        } catch (InterruptedException | ExecutionException e) {
            // Imprimir el error real
            e.printStackTrace();
            String errorMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            throw new PresentationException("Error en Firestore: " + errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public Usuario getUsuarioById(String id) {
        try {
            DocumentSnapshot document = db.collection(COLECCION).document(id).get().get();
            if (document.exists()) {
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
