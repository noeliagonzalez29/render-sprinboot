package com.proyecto.ProyectoConectacare.config;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * Esta clase de configuración es responsable de configurar e inicializar los servicios de Firebase, como FirebaseApp, Firestore y FirebaseAuth en una aplicación Spring Boot. Utiliza una combinación de configuración basada en variables de entorno para entornos de producción y configuración basada en archivos JSON para desarrollo local.
 *
 * - El método `firebaseApp` inicializa una instancia de FirebaseApp cargando las credenciales desde una variable de entorno (`GOOGLE_CREDENTIALS`) en producción o desde un archivo de configuración JSON local durante el desarrollo.
 * - El método `firestore` proporciona una instancia de Firestore vinculada a la FirebaseApp inicializada, que se utiliza para interactuar con la base de datos de Firestore.
 * - El método `firebaseAuth` proporciona una instancia de FirebaseAuth, lo que permite la autenticación y la gestión de usuarios.
 *
 * Dependencias:
 * - Esta clase se basa en `ResourceLoader` para gestionar los archivos de recursos. * - El SDK de Google Firebase proporciona las clases necesarias, como `FirebaseApp`, `Firestore`, `FirebaseAuth` y `FirebaseOptions`, para configurar y administrar Firebase.
 *
 * Beans:
 * - `FirebaseApp` actúa como punto de entrada para los servicios de Firebase.
 * - `Firestore` permite las operaciones con la base de datos de Firestore.
 * - `FirebaseAuth` admite funcionalidades relacionadas con la autenticación.
 *
 * Casos de uso:
 * - Esta configuración es crucial para integrar los servicios de Firebase en la aplicación.
 * - La autenticación, las operaciones con la base de datos u otros servicios basados ​​en Firebase requerirán esta configuración.
 *
 * Notas:
 * - Para entornos de producción, asegúrese de que la variable de entorno `GOOGLE_CREDENTIALS` contenga las credenciales JSON de Firebase.
 * - Para el desarrollo local, el archivo de configuración JSON debe ubicarse en la ruta de clase con el nombre y la ruta esperados.
 */
@Configuration
public class FirebaseConfig {

    private final ResourceLoader resourceLoader;

    public FirebaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // 1. Intentar cargar desde variable de entorno en Render
        String firebaseJson = System.getenv("GOOGLE_CREDENTIALS");

        if (firebaseJson != null) {
            // 2. Configuración para producción (Render)
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            new ByteArrayInputStream(firebaseJson.getBytes())
                    ))
                    .build();

            return FirebaseApp.initializeApp(options);
        }
        // 3. para desarrollo local (archivo JSON)
        else {
            Resource resource = new ClassPathResource("proyectoconectacare-firebase-adminsdk-fbsvc-9394117754.json");
            InputStream serviceAccount = resource.getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            return FirebaseApp.initializeApp(options);
        }
    }

    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }
}
