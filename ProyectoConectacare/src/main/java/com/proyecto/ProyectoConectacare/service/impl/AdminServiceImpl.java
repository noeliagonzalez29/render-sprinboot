package com.proyecto.ProyectoConectacare.service.impl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;

import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.opencsv.CSVWriter;
import com.proyecto.ProyectoConectacare.exception.PresentationException;
import com.proyecto.ProyectoConectacare.model.Anuncio;
import com.proyecto.ProyectoConectacare.model.Rol;
import com.proyecto.ProyectoConectacare.model.Usuario;
import com.proyecto.ProyectoConectacare.service.AdminService;
import com.proyecto.ProyectoConectacare.service.AnuncioService;
import com.proyecto.ProyectoConectacare.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class AdminServiceImpl implements AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);
    private final UsuarioService usuarioService;
    private Firestore db;
    private final AnuncioService anuncioService;
    private static final String COLECCION_LOGS = "logEstadisticas";
    private static final String EVENTO_INICIO_SESION = "INICIO_SESION";
    private static final String EVENTO_REGISTRO_NUEVO = "REGISTRO";
    public AdminServiceImpl(UsuarioService usuarioService, Firestore db, AnuncioService anuncioService) {
        this.usuarioService = usuarioService;
        this.db = db;
        this.anuncioService = anuncioService;
    }
    /**
     * Obtiene una lista de todos los usuarios.
     *
     * @return una lista de objetos Usuario que representan a todos los usuarios.
     */
    @Override
    public List<Usuario> obtenerUsuarios() {
        return usuarioService.getAllUsuarios();
    }

    /**
     * Elimina un usuario por su identificador único.
     *
     * @param id el identificador único del usuario que se va a eliminar.
     */
    @Override
    public void eliminarUsuario(String id) {
        usuarioService.deleteUsuario(id);
    }

    /**
     * Cuenta la cantidad de documentos en la colección de logs
     * que coinciden con un tipo de evento específico.
     *
     * @param nombreEvento El valor del campo "evento" a contar (ej. "inicioSesion").
     * @return El número de eventos encontrados.
     * @throws PresentationException Si ocurre un error al consultar Firestore.
     */
    private int contarEventos(String nombreEvento) {
        logger.debug("Iniciando conteo para evento: {}", nombreEvento);
        try {
            // 1. Crear la consulta a la colección de logs filtrando por evento
            Query query = db.collection(COLECCION_LOGS)
                    .whereEqualTo("evento", nombreEvento);

            // 2. Ejecutar la consulta (asíncrona)
            ApiFuture<QuerySnapshot> future = query.get();

            // 3. Obtener el resultado de forma síncrona (esperar)
            // Esto puede lanzar InterruptedException o ExecutionException
            QuerySnapshot querySnapshot = future.get();

            // 4. Obtener el tamaño (número de documentos)
            int count = querySnapshot.size();
            logger.info("Conteo para evento '{}' finalizado. Resultado: {}", nombreEvento, count);
            return count;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restablecer flag de interrupción
            logger.error("❌ Conteo de evento '{}' interrumpido", nombreEvento, e);
            throw new PresentationException("Conteo de estadísticas interrumpido: " + nombreEvento, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ExecutionException e) {
            logger.error("❌ Error durante la ejecución del conteo para evento '{}'", nombreEvento, e);
            // La causa real está en e.getCause()
            throw new PresentationException("Error al obtener estadísticas: " + nombreEvento, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // Captura para cualquier otro error inesperado
            logger.error("❌ Error inesperado al contar evento '{}'", nombreEvento, e);
            throw new PresentationException("Error inesperado en estadísticas: " + nombreEvento, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Cuenta el número total de inicios de sesión registrados en el sistema.
     *
     * @return El número total de eventos "inicioSesion".
     */
    @Override
    public int contarInicioSesion() {
        // Llama al método helper reutilizable
        return contarEventos(EVENTO_INICIO_SESION);
    }

    /**
     * Cuenta el número total de registros nuevos en el sistema.
     *
     * @return El número total de eventos "registroNuevo".
     */
    @Override
    public int contarRegistros() {
        // Llama al método helper reutilizable
        return contarEventos(EVENTO_REGISTRO_NUEVO);
    }

    public byte[] generarCSVBytes(List<Usuario> usuarios) { // No necesita declarar throws si envolvemos todo en PresentationException
        logger.info("Iniciando generación de CSV en memoria (byte[]) para {} usuarios.", usuarios.size());

        // Usar try-with-resources para asegurar el cierre automático de los streams y writers
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8); // Especificar UTF-8
             CSVWriter csvWriter = new CSVWriter(osw)) // Usar el OutputStreamWriter
        {
            // 1. Escribir BOM (Byte Order Mark) para compatibilidad UTF-8 en Excel
            osw.write('\ufeff');

            // 2. Escribir la cabecera del CSV
            String[] cabecera = {
                    "ID Usuario", "Nombre", "Email", "Rol",
                    "Total Inicios Sesión", "Total Registros", "Total Anuncios",
                    "Anuncios de este Usuario"
            };
            csvWriter.writeNext(cabecera);

            // 3. Obtener estadísticas generales (puede lanzar PresentationException)
            int totalIniciosSesion = this.contarInicioSesion();
            int totalRegistros = this.contarRegistros();
            int totalAnunciosGeneral = anuncioService.contarTotalAnuncios();

            // 4. Iterar sobre usuarios y escribir filas
            for (Usuario usuario : usuarios) {
                int anunciosPorEsteUsuario = 0; // Valor por defecto

                // Calcular anuncios solo para roles relevantes
                if (usuario.getRol() == Rol.CLIENTE) { // Ajusta si es necesario
                    try {
                        // Obtener tamaño de la lista de anuncios del usuario
                        anunciosPorEsteUsuario = anuncioService.getAnunciosByClienteId(usuario.getId()).size();
                    } catch (PresentationException e) {
                        // Error específico al obtener anuncios de UN usuario: loguear y continuar
                        logger.warn("No se pudo contar anuncios para usuario {} [{}]: {}. Usando 0.",
                                usuario.getEmail(), usuario.getId(), e.getMessage());
                        // anunciosPorEsteUsuario ya es 0, no hace falta reasignar
                    } catch (Exception e) {
                        // Otro error inesperado para este usuario: loguear y continuar
                        logger.error("Error inesperado contando anuncios para usuario {} [{}]: {}",
                                usuario.getEmail(), usuario.getId(), e.getMessage(), e);
                        // anunciosPorEsteUsuario ya es 0
                    }
                }

                // Preparar y escribir la fila del usuario
                String[] datosUsuario = {
                        usuario.getId() != null ? usuario.getId() : "",
                        usuario.getNombre() != null ? usuario.getNombre() : "",
                        usuario.getEmail() != null ? usuario.getEmail() : "",
                        usuario.getRol() != null ? usuario.getRol().toString() : "",
                        String.valueOf(totalIniciosSesion),
                        String.valueOf(totalRegistros),
                        String.valueOf(totalAnunciosGeneral),
                        String.valueOf(anunciosPorEsteUsuario)
                };
                csvWriter.writeNext(datosUsuario);
            }

            // 5. Asegurar que todo se escriba antes de devolver los bytes
            csvWriter.flush(); // Flush CSVWriter
            osw.flush();     // Flush OutputStreamWriter

            logger.info("Generación de CSV en memoria completada.");
            // 6. Devolver los bytes si todo fue exitoso
            return baos.toByteArray();

        } catch (IOException e) {
            // Error de bajo nivel durante la escritura en memoria o flush
            logger.error("❌ Error de E/S durante la generación del CSV en memoria: {}", e.getMessage(), e);
            // Lanzar tu excepción personalizada indicando un error interno
            throw new PresentationException("Error de escritura al generar el archivo CSV.", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (PresentationException e) {
            // Error lanzado por tus métodos de conteo o de anuncios (ya logueado ahí)
            logger.error("❌ Error de negocio (PresentationException) al generar datos para CSV: {}", e.getMessage(), e);
            // Relanzar la misma excepción para que el controlador/manejador global la capture
            throw e;
        } catch (Exception e) {
            // Cualquier otro error inesperado
            logger.error("❌ Error inesperado durante la generación del CSV en memoria: {}", e.getMessage(), e);
            // Lanzar tu excepción personalizada
            throw new PresentationException("Error inesperado al generar el informe CSV.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // Ya no se necesita un return aquí porque todos los caminos o retornan los bytes
        // o lanzan una excepción.
    }

}