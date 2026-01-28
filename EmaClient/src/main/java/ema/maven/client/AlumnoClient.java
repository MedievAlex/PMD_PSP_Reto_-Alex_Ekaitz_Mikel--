package ema.maven.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import ema.maven.model.Alumno;
import java.util.Arrays;
import java.util.List;

public class AlumnoClient {
	private final WebClient webClient;

	public AlumnoClient(String baseUrl) {
		this.webClient = WebClient.builder().baseUrl(baseUrl)
				.defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
	}

	public AlumnoClient() {
		this("http://localhost:8080");
	}

	public List<Alumno> getAllAlumnos() {
		try {
			Alumno[] alumnosArray = webClient.get().uri("/alumnos").retrieve().bodyToMono(Alumno[].class).block();

			return alumnosArray != null ? Arrays.asList(alumnosArray) : Arrays.asList();

		} catch (WebClientResponseException e) {
			System.err.println("Error HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
			return Arrays.asList();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			return Arrays.asList();
		}
	}

	public Alumno getAlumnoById(int id) {
		try {
			return webClient.get().uri("/alumnos/{id}", id).retrieve().bodyToMono(Alumno.class).block();

		} catch (WebClientResponseException e) {
			if (e.getStatusCode().value() == 404) {
				System.out.println("Alumno con ID " + id + " no encontrado");
			} else {
				System.err.println("Error HTTP " + e.getStatusCode());
			}
			return null;
		}
	}

	public List<Alumno> getAlumnosByCurso(String curso) {
		try {
			Alumno[] alumnosArray = webClient.get().uri("/alumnos/curso/{curso}", curso).retrieve()
					.bodyToMono(Alumno[].class).block();

			return alumnosArray != null ? Arrays.asList(alumnosArray) : Arrays.asList();

		} catch (WebClientResponseException e) {
			if (e.getStatusCode().value() == 204) {
				System.out.println("No hay alumnos en el curso: " + curso);
			} else {
				System.err.println("Error HTTP " + e.getStatusCode());
			}
			return Arrays.asList();
		}
	}

	public int getAlumnosCount() {
		try {
			JsonNode response = webClient.get().uri("/alumnos/count").retrieve().bodyToMono(JsonNode.class).block();

			if (response != null && response.has("total")) {
				return response.get("total").asInt();
			}

			return 0;

		} catch (WebClientResponseException e) {
			System.err.println("Error contando alumnos: " + e.getStatusCode());
			return 0;
		}
	}

	public Alumno createAlumno(Alumno alumno) {
		try {
			return webClient.post().uri("/alumnos").bodyValue(alumno).retrieve().bodyToMono(Alumno.class).block();

		} catch (WebClientResponseException e) {
			if (e.getStatusCode().value() == 409) {
				System.err.println("Error: El alumno ya existe");
			} else {
				System.err.println("Error HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
			}
			return null;
		}
	}

	public Alumno updateAlumno(int id, Alumno alumno) {
		try {
			return webClient.put().uri("/alumnos/{id}", id).bodyValue(alumno).retrieve().bodyToMono(Alumno.class)
					.block();

		} catch (WebClientResponseException e) {
			if (e.getStatusCode().value() == 404) {
				System.err.println("Error: Alumno con ID " + id + " no encontrado");
			} else {
				System.err.println("Error HTTP " + e.getStatusCode());
			}
			return null;
		}
	}

	public boolean deleteAlumno(int id) {
		try {
			ResponseEntity<Void> response = webClient.delete().uri("/alumnos/{id}", id).retrieve().toEntity(Void.class)
					.block();

			return response != null && response.getStatusCode().is2xxSuccessful();

		} catch (WebClientResponseException e) {
			if (e.getStatusCode().value() == 404) {
				System.err.println("Error: Alumno con ID " + id + " no encontrado");
			} else {
				System.err.println("Error HTTP " + e.getStatusCode());
			}
			return false;
		}
	}

	public List<Alumno> searchAlumnosByName(String nombre) {
		try {
			Alumno[] alumnosArray = webClient.get()
					.uri(uriBuilder -> uriBuilder.path("/alumnos/buscar").queryParam("nombre", nombre).build())
					.retrieve().bodyToMono(Alumno[].class).block();

			return alumnosArray != null ? Arrays.asList(alumnosArray) : Arrays.asList();

		} catch (WebClientResponseException e) {
			System.err.println("Error en búsqueda: " + e.getStatusCode());
			return Arrays.asList();
		}
	}
}