package com.proyecto.ProyectoConectacare.service.impl;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.proyecto.ProyectoConectacare.exception.PresentationException;
import com.proyecto.ProyectoConectacare.model.Anuncio;
import com.proyecto.ProyectoConectacare.service.AnuncioService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class AnuncioServiceImpl implements AnuncioService {
    private static final String COLECCION = "anuncios";
    private final Firestore db;

    public AnuncioServiceImpl(Firestore db) {
        this.db = db;
    }

    /**
     * Crea un nuevo anuncio asociado al ID de cliente proporcionado y lo guarda en la base de datos.
     * El método obtiene el nombre del cliente de la base de datos, lo asigna al anuncio,
     * genera un ID único para el anuncio y lo conserva.
     *
     * @param clienteId Identificador único del cliente al que pertenece el anuncio.
     * @param anuncio Objeto de anuncio que se creará y almacenará.
     * @return El anuncio creado con detalles adicionales, como el nombre del cliente y el ID generado.
     * @throws PresentationException Si se produce un error durante las operaciones de la base de datos.
     */
    @Override
    public Anuncio crearAnuncio(String clienteId, Anuncio anuncio) {
        anuncio.setClienteId(clienteId); // UID correcto del cliente

        try {
            DocumentSnapshot clienteDoc = db.collection("usuarios").document(clienteId).get().get();
            String nombreCliente = clienteDoc.getString("nombre");
            anuncio.setNombreCliente(nombreCliente);
            DocumentReference docRef = db.collection(COLECCION).document();
            anuncio.setId(docRef.getId());

            docRef.set(anuncio).get();
            return anuncio;
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al crear anuncio", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Recupera un anuncio (Anuncio) por su identificador único de la base de datos.
     *
     * @param id El identificador único del anuncio a recuperar.
     * @return El objeto Anuncio correspondiente al ID proporcionado, si se encuentra.
     * @throws PresentationException Si no se encuentra el anuncio o si se produce un error durante el proceso de recuperación.
     */
    @Override
    public Anuncio getAnuncioById(String id) {
        try {
            DocumentSnapshot document = db.collection(COLECCION).document(id).get().get();
            if (document.exists()) {
                return document.toObject(Anuncio.class);
            }
            throw new PresentationException("Anuncio no encontrado", HttpStatus.NOT_FOUND);
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al obtener anuncio", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Recupera todos los anuncios de la base de datos.
     * El método recupera todos los documentos de la colección de anuncios, los convierte en objetos Anuncio,
     * les asigna sus IDs de documento únicos y los devuelve como una lista.
     *
     * @return Una lista de objetos Anuncio recuperados de la base de datos.
     * @throws PresentationException Si se produce un error durante el proceso de recuperación.
     */
    @Override
    public List<Anuncio> getAllAnuncios() {
        try {
            return db.collection(COLECCION).get().get().getDocuments()
                    .stream()
                    .map(doc -> {
                        Anuncio anuncio = doc.toObject(Anuncio.class);
                        anuncio.setId(doc.getId());
                        return anuncio;
                    })
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al obtener anuncios", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Recupera una lista de objetos Anuncio asociados a un clienteId determinado.
     *
     * @param clienteId: el ID del cliente cuyos anuncios asociados se recuperarán.
     * @return: una lista de objetos Anuncio asociados al clienteId especificado.
     * @throws: PresentationException si se produce un error durante el proceso de recuperación.
     */
    @Override
    public List<Anuncio> getAnunciosByClienteId(String clienteId) {
        try {
            return db.collection(COLECCION)
                    .whereEqualTo("clienteId", clienteId)
                    .get().get().getDocuments()
                    .stream()
                    .map(doc -> doc.toObject(Anuncio.class))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error al obtener anuncios", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Comprueba la existencia de un anuncio en la base de datos mediante su identificador.
     *
     * @param anuncioId: el identificador único del anuncio a comprobar.
     * @return true si el anuncio existe, false en caso contrario.
     * @throws PresentationException si se produce un error al acceder a la base de datos.
     */
    public boolean existeAnuncio(String anuncioId) {
        try {
            return db.collection("anuncios").document(anuncioId).get().get().exists();
        } catch (InterruptedException | ExecutionException e) {
            throw new PresentationException("Error verificando anuncio", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Cuenta el número total de anuncios en la base de datos.
     *
     * @return El número total de anuncios encontrados en la base de datos.
     * @throws PresentationException si se produce un error al recuperar los datos.
     */
    public int contarTotalAnuncios() {
        try {
            return db.collection("anuncios").get().get().getDocuments().size();
        } catch (Exception e) {
            throw new PresentationException("Error al contar anuncios", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
