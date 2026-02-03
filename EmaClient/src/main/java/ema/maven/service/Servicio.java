package ema.maven.service;

import ema.maven.model.APK;
import ema.maven.model.Usuario;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class Servicio {

    private final WebClient webClient;

    public Servicio() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8080/api")
                .build();
    }

    public Mono<String> login(Usuario u) {
        return webClient.post()
                .uri("/login")
                .bodyValue(u)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> signUp(Usuario u) {
        return webClient.post()
                .uri("/register")
                .bodyValue(u)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<APK> getAPK(String titulo) {
        return webClient.get()
                .uri("/apk/{titulo}", titulo)
                .retrieve()
                .bodyToMono(APK.class);
    }

    public Mono<APK> addAPK(APK apk) {
        return webClient.post()
                .uri("/apk")
                .bodyValue(apk)
                .retrieve()
                .bodyToMono(APK.class);
    }

    public Mono<APK> updateAPK(String titulo, APK apk) {
        return webClient.put()
                .uri("/apk/{titulo}", titulo)
                .bodyValue(apk)
                .retrieve()
                .bodyToMono(APK.class);
    }

    public Mono<Void> deleteAPK(String titulo) {
        return webClient.delete()
                .uri("/apk/{titulo}", titulo)
                .retrieve()
                .toBodilessEntity() // Para peticiones sin body (204)
                .then();
    }

    public Mono<String> getHash(String titulo, String algoritmo) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/hash/{titulo}")
                        .queryParam("algoritmo", algoritmo)
                        .build(titulo))
                .retrieve()
                .bodyToMono(String.class);
    }

    public Flux<APK> getAPKs() {
        return webClient.get()
                .uri("/apks")
                .retrieve()
                .bodyToFlux(APK.class);
    }

    public Mono<Resource> downloadAPK(String titulo) {
        return webClient.get()
                .uri("/download/{titulo}", titulo)
                .retrieve()
                .bodyToMono(Resource.class);
    }
}
