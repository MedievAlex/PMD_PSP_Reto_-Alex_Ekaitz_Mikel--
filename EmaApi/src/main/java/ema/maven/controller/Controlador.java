package ema.maven.controller;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.core.io.Resource;

import ema.maven.model.APK;
import ema.maven.model.Usuario;
import ema.maven.service.*;

@RestController
@RequestMapping("api")
public class Controlador {
	private final Servicio servicio;

	public Controlador(Servicio servicio) {
		this.servicio = servicio;
	}
	
	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody Usuario user) {
		if (user == null || user.getNombre() == null || user.getContraseña() == null) {
			return ResponseEntity.badRequest().body("Nombre y contraseña obligatorios");
		}
		
		try {
		    Usuario u = servicio.login(user);
		    
		    if (u == null) {
		    	return ResponseEntity.status(401).body("Usuario o contraseña incorrectos");
		    }
		    
		    return ResponseEntity.ok(u.getNombre());
		} catch (ResponseStatusException e) {
			return ResponseEntity.internalServerError().body("Error interno en el servidor");
		}
	}

	@PostMapping("/register")
	public ResponseEntity<String> signUp(@RequestBody Usuario user) {
		if (user == null || user.getNombre() == null || user.getContraseña() == null) {
			return ResponseEntity.badRequest().body("Nombre y contraseña obligatorios");
		}
		
		try {
		    Usuario u = servicio.signUp(user);
		    
		    if (u == null) {
		    	return ResponseEntity.status(409).body("El usuario ya existe");
		    }
		    
		    return ResponseEntity.status(201).body(u.getNombre());
		} catch (ResponseStatusException e) {
			return ResponseEntity.internalServerError().body("Error interno en el servidor");
		}
	}
	
	@GetMapping("/apks")
	public ResponseEntity<?> getApks() {
		try {
			ArrayList<APK> apks = servicio.getAPKs();
			
			if (apks == null || apks.isEmpty()) {
				return ResponseEntity.ok().body("No hay APKs disponibles");
			}
			
			return ResponseEntity.ok().body(apks);
		} catch (ResponseStatusException e) {
			return ResponseEntity.internalServerError().body("Error interno al obtener las APKs");
		}
	}
	
	@GetMapping("/apk/{titulo}")
	public ResponseEntity<?> getAPK(@PathVariable String titulo) {
		if (titulo == null || titulo.trim().isEmpty()) {
			return ResponseEntity.badRequest().body("El título de la APK no puede estar vacío");
		}
		
		try {
		    APK apk = servicio.getAPK(titulo);
		    
		    if (apk == null) {
		    	return ResponseEntity.status(404).body("No se encontró la APK con título: " + titulo);
		    }
		    
		    return ResponseEntity.ok(apk);
		} catch (ResponseStatusException e) {
			return ResponseEntity.internalServerError().body("Error interno al buscar la APK");
		}
	}
	
	@PostMapping("/apk")
	public ResponseEntity<?> addAPK(@RequestBody APK apk) {
	    if (apk == null || apk.getTitulo() == null || apk.getTitulo().trim().isEmpty()) {
	        return ResponseEntity.badRequest().body("El título de la APK no puede estar vacío");
	    }
	    
	    String image = apk.getImage();
	    
	    if (apk.getImage() != null && !apk.getImage().trim().isEmpty() && !image.startsWith("data:image/png;base64,")) {
	    	return ResponseEntity.badRequest().body("Solo se permiten imágenes en formato PNG");
	    }
	    
	    try {
		    APK creada = servicio.addAPK(apk);
		    
		    if (creada == null) {
		    	return ResponseEntity.status(409).body("Ya existe una APK con el título: " + apk.getTitulo());
		    }
		    
		    return ResponseEntity.status(201).body(creada);
	    } catch (ResponseStatusException e) {
	    	return ResponseEntity.internalServerError().body("Error interno al crear la APK");
		}
	}
	
	@PutMapping("/apk/{titulo}")
	public ResponseEntity<?> updateAPK(@PathVariable String titulo, @RequestBody APK apk) {
		if (titulo == null || titulo.trim().isEmpty()) {
			return ResponseEntity.badRequest().body("El título de la APK no puede estar vacío");
		}
		
		if (apk == null || apk.getTitulo() == null || apk.getTitulo().trim().isEmpty()) {
			return ResponseEntity.badRequest().body("El título de la APK en el cuerpo no puede estar vacío");
		}
		
		try {
		    APK actualizada = servicio.updateAPK(titulo, apk);
		    
		    if (actualizada == null) {
		    	return ResponseEntity.status(404).body("No se encontró la APK con título: " + titulo);
		    }
		    
		    return ResponseEntity.ok(actualizada);
		} catch (ResponseStatusException e) {
			if (e.getStatusCode() == HttpStatus.CONFLICT) {
				return ResponseEntity.status(409).body("Conflicto al actualizar: el nuevo título ya existe");
			} else {
				return ResponseEntity.internalServerError().body("Error interno al actualizar la APK");
			}
		}
	}

	@DeleteMapping("/apk/{titulo}")
	public ResponseEntity<String> deleteAPK(@PathVariable String titulo) {
		if (titulo == null || titulo.trim().isEmpty()) {
			return ResponseEntity.badRequest().body("El título de la APK no puede estar vacío");
		}
		
		try {
		    servicio.deleteAPK(titulo);
		    
		    return ResponseEntity.status(204).body("APK eliminada correctamente: " + titulo);
		} catch (ResponseStatusException e) {
			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				return ResponseEntity.status(404).body("No se encontró la APK con título: " + titulo);
			} else {
				return ResponseEntity.internalServerError().body("Error interno al eliminar la APK");
			}
		}
	}
	
	@GetMapping("/download/{titulo}")
	public ResponseEntity<?> downloadAPK(@PathVariable String titulo) {
	    if (titulo == null || titulo.trim().isEmpty()) {
	        return ResponseEntity.badRequest().body("El título de la APK no puede estar vacío");
	    }

	    try {
	        String apk = titulo + ".apk";
	        Resource resource = servicio.downloadAPK(apk);

	        return ResponseEntity.ok()
	        		.contentType(MediaType.APPLICATION_OCTET_STREAM)
	                .header("Content-Disposition", "attachment; filename=\"" + apk + "\"")
	                .body(resource);
	    } catch (ResponseStatusException e) {
	        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
	            return ResponseEntity.status(404).body("No se encontró el archivo APK: " + titulo);
	        } else {
	            return ResponseEntity.internalServerError().body("Error interno al intentar descargar la APK: " + e.getMessage());
	        }
	    }
	}
	
	@GetMapping("/hash/{titulo}")
	public ResponseEntity<String> getHash(@PathVariable String titulo, @RequestParam(required = false) String algoritmo) {
		if (titulo == null || titulo.trim().isEmpty()) {
			return ResponseEntity.badRequest().body("El título de la APK no puede estar vacío");
		}
		
		try {
			String apk = titulo + ".apk";
			String hash = servicio.getHash(apk, algoritmo);
			return ResponseEntity.ok().body(hash);
		} catch (ResponseStatusException e) {
			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				return ResponseEntity.status(404).body("No se encontró el archivo APK: " + titulo);
			} else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
				return ResponseEntity.badRequest().body("Los algoritmos válidos son: MD5, SHA-1 y SHA-256");
			} else {
				return ResponseEntity.internalServerError().body("Error interno al calcular el hash");
			}
		}
	}
}