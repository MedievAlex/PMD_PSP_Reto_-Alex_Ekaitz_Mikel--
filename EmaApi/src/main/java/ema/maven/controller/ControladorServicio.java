package ema.maven.controller;

import java.util.ArrayList;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
	    String nombre = servicio.login(user);
	    return ResponseEntity.ok(nombre);
	}

	@PostMapping("/register")
	public ResponseEntity<String> signUp(@RequestBody Usuario user) {
	    String nombre = servicio.signUp(user);
	    return ResponseEntity.status(201).body(nombre);
	}
	
	
	@GetMapping("/apks")
	public ResponseEntity<ArrayList<APK>> getApks() {
		ArrayList<APK> apks = servicio.getAPKs();
		
		return ResponseEntity.ok().body(apks);
	}
	
	@PostMapping("/apk")
	public ResponseEntity<APK> addAPK(@RequestBody APK apk) {
	    if (apk.getTitulo() == null || apk.getTitulo().trim().isEmpty()) {
	        return ResponseEntity.badRequest().build();
	    }
	    
	    APK creada = servicio.addAPK(apk);
	    return ResponseEntity.status(201).body(creada);
	}
	
	@PutMapping("/apk/{titulo}")
	public ResponseEntity<APK> updateAPK(@PathVariable String titulo, @RequestBody APK apk) {
	    APK actualizada = servicio.updateAPK(titulo, apk);
	    
	    return ResponseEntity.ok(actualizada);
	}

	@DeleteMapping("/apk/{titulo}")
	public ResponseEntity<Void> deleteAPK(@PathVariable String titulo) {
	    servicio.deleteAPK(titulo);
	    
	    return ResponseEntity.noContent().build();
	}
	
	@GetMapping("/download/{titulo}")
	public ResponseEntity<Resource> downloadAPK(@PathVariable String titulo) {
    	Resource resource = servicio.downloadAPK(titulo);
        
        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=\"" + titulo + "\"").body(resource); // Cabecera para que el navegador sepa que es una descarga
	}
}
