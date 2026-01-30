package ema.maven.controller;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
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
public class ControladorServicio {
	private final Servicio servicio;

	public ControladorServicio(Servicio servicio) {
		this.servicio = servicio;
	}
	
	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody Usuario user) {
		if (user == null || user.getNombre() == null || user.getContraseña() == null) {
			return ResponseEntity.badRequest().build();
		}
		
		try {
		    Usuario u = servicio.login(user);
		    
		    if (u == null) {
		    	return ResponseEntity.status(401).build();
		    }
		    
		    return ResponseEntity.ok(u.getNombre());
		} catch (ResponseStatusException e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping("/register")
	public ResponseEntity<String> signUp(@RequestBody Usuario user) {
		if (user == null || user.getNombre() == null || user.getContraseña() == null) {
			return ResponseEntity.badRequest().build();
		}
		
		try {
		    Usuario u = servicio.signUp(user);
		    
		    if (u == null) {
		    	return ResponseEntity.status(409).build();
		    }
		    
		    return ResponseEntity.status(201).body(u.getNombre());
		} catch (ResponseStatusException e) {
			return ResponseEntity.internalServerError().build();
		}
	}
	
	@GetMapping("/apks")
	public ResponseEntity<ArrayList<APK>> getApks() {
		ArrayList<APK> apks = servicio.getAPKs();
		
		return ResponseEntity.ok().body(apks);
	}
	
	@GetMapping("/apk/{titulo}")
	public ResponseEntity<APK> getAPK(@PathVariable String titulo) {
		if (titulo == null || titulo.trim().isEmpty()) {
			return ResponseEntity.badRequest().build();
		}
		
		try {
		    APK apk = servicio.getAPK(titulo);
		    
		    if (apk == null) {
		    	return ResponseEntity.notFound().build();
		    }
		    
		    return ResponseEntity.ok(apk);
		} catch (ResponseStatusException e) {
			return ResponseEntity.internalServerError().build();
		}
	}
	
	@PostMapping("/apk")
	public ResponseEntity<APK> addAPK(@RequestBody APK apk) {
	    if (apk.getTitulo() == null || apk.getTitulo().trim().isEmpty()) {
	        return ResponseEntity.badRequest().build();
	    }
	    
	    try {
		    APK creada = servicio.addAPK(apk);
		    
		    if (creada == null) {
		    	return ResponseEntity.status(409).build();
		    }
		    
		    return ResponseEntity.status(201).body(creada);
	    } catch (ResponseStatusException e) {
			return ResponseEntity.internalServerError().build();
		}
	}
	
	@PutMapping("/apk/{titulo}")
	public ResponseEntity<APK> updateAPK(@PathVariable String titulo, @RequestBody APK apk) {
		if (titulo == null || titulo.trim().isEmpty() || apk.getTitulo() == null || apk.getTitulo().trim().isEmpty()) {
			return ResponseEntity.badRequest().build();
		}
		
		try {
		    APK actualizada = servicio.updateAPK(titulo, apk);
		    
		    if (actualizada == null) {
		    	return ResponseEntity.notFound().build();
		    }
		    
		    return ResponseEntity.ok(actualizada);
		} catch (ResponseStatusException e) {
			if (e.getStatusCode() == HttpStatus.CONFLICT) {
				return ResponseEntity.status(409).build();
			} else {
				return ResponseEntity.internalServerError().build();
			}
		}
	}

	@DeleteMapping("/apk/{titulo}")
	public ResponseEntity<Void> deleteAPK(@PathVariable String titulo) {
		if (titulo == null || titulo.trim().isEmpty()) {
			return ResponseEntity.badRequest().build();
		}
		
		try {
		    servicio.deleteAPK(titulo);
		    
		    return ResponseEntity.noContent().build();
		} catch (ResponseStatusException e) {
			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				return ResponseEntity.notFound().build();
			} else {
				return ResponseEntity.internalServerError().build();
			}
		}
	}
	
	@GetMapping("/download/{titulo}")
	public ResponseEntity<Resource> downloadAPK(@PathVariable String titulo) {
		if (titulo == null || titulo.trim().isEmpty()) {
			return ResponseEntity.badRequest().build();
		}
		
		try {
			String apk = titulo + ".apk";
	    	Resource resource = servicio.downloadAPK(apk);
	        
	        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=\"" + apk + "\"").body(resource); // Cabecera para que el navegador sepa que es una descarga
		} catch (ResponseStatusException e) {
			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				return ResponseEntity.notFound().build();
			} else {
				return ResponseEntity.internalServerError().build();
			}
		}
	}
	
	@GetMapping("/hash/{titulo}")
	public ResponseEntity<String> getHash(@PathVariable String titulo, @RequestParam(required = false) String algoritmo) {
		if (titulo == null || titulo.trim().isEmpty()) {
			return ResponseEntity.badRequest().build();
		}
		
		try {
			String apk = titulo + ".apk";
			String hash = servicio.getHash(apk, algoritmo);
			return ResponseEntity.ok().body(hash);
		} catch (ResponseStatusException e) {
			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				return ResponseEntity.notFound().build();
			} else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
				return ResponseEntity.badRequest().body("Los algoritmos válidos son: MD5, SHA-1 y SHA-256");
			} else {
				return ResponseEntity.internalServerError().build();
			}
		}
	}
}
