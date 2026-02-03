package ema.maven.service;

import ema.maven.model.APK;
import ema.maven.model.Usuario;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class Servicio {

    private final WebClient webClient;

    public Servicio() {
        this("http://localhost:8080/api");
    }

    public Servicio(String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // -------------------- APK --------------------

    public APK getAPK(String titulo) {
        try {
            return webClient.get()
                    .uri("/apk/{titulo}", titulo)
                    .retrieve()
                    .bodyToMono(APK.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                System.out.println("APK con título " + titulo + " no encontrada");
            } else {
                System.err.println("Error HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error conectando con la API: " + e.getMessage());
            return null;
        }
    }

    public APK[] getAllAPKs() {
        try {
            return webClient.get()
                    .uri("/apks")
                    .retrieve()
                    .bodyToMono(APK[].class)
                    .block();
        } catch (Exception e) {
            System.err.println("Error al obtener todas las APKs: " + e.getMessage());
            return new APK[0];
        }
    }

    public APK addAPK(APK apk) {
        try {
            return webClient.post()
                    .uri("/apk")
                    .bodyValue(apk)
                    .retrieve()
                    .bodyToMono(APK.class)
                    .block();
        } catch (WebClientResponseException e) {
            System.err.println("Error al agregar APK: " + e.getResponseBodyAsString());
            return null;
        }
    }

    public APK updateAPK(String titulo, APK apk) {
        try {
            return webClient.put()
                    .uri("/apk/{titulo}", titulo)
                    .bodyValue(apk)
                    .retrieve()
                    .bodyToMono(APK.class)
                    .block();
        } catch (WebClientResponseException e) {
            System.err.println("Error al actualizar APK: " + e.getResponseBodyAsString());
            return null;
        }
    }

    public boolean deleteAPK(String titulo) {
        try {
            webClient.delete()
                    .uri("/apk/{titulo}", titulo)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (WebClientResponseException e) {
            System.err.println("Error al eliminar APK: " + e.getResponseBodyAsString());
            return false;
        }
    }

    // -------------------- Usuarios --------------------

    public Usuario login(Usuario usuario) {
        try {
            String nombre = webClient.post()
                    .uri("/login")
                    .bodyValue(usuario)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return nombre != null ? new Usuario(nombre, "") : null;

        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                System.out.println("Login fallido para usuario " + usuario.getNombre());
            } else {
                System.err.println("Error HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error conectando con la API: " + e.getMessage());
            return null;
        }
    }

    public Usuario signUp(Usuario usuario) {
        try {
            String nombre = webClient.post()
                    .uri("/register")
                    .bodyValue(usuario)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return nombre != null ? new Usuario(nombre, "") : null;

        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 409) {
                System.out.println("Registro fallido, usuario ya existe: " + usuario.getNombre());
            } else {
                System.err.println("Error HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error conectando con la API: " + e.getMessage());
            return null;
        }
    }
}
