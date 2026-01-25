package ema.maven.controller;

import java.util.ArrayList;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
		if(user == null) {
	        return ResponseEntity.badRequest().body("Faltan campos obligatorios");
	    }
		
	    int resultado = servicio.login(user);
	    
	    switch(resultado) {
        case 400:
            return ResponseEntity.badRequest().body("Faltan campos obligatorios");
        case 401:
            return ResponseEntity.status(401).body("Credenciales inválidas");
        case 200:
        	return ResponseEntity.ok().body(user.getNombre().toLowerCase().trim());
        default:
            return ResponseEntity.internalServerError().build();
	    }
	}
	
	@PostMapping("/register")
	public ResponseEntity<String> signUp(@RequestBody Usuario user) {
	    if(user == null) {
	        return ResponseEntity.badRequest().body("Faltan campos obligatorios");
	    }
	    
	    int resultado = servicio.signUp(user);
	    
	    switch(resultado) {
	        case 400:
	            return ResponseEntity.badRequest().body("Faltan campos obligatorios");
	        case 409:
	            return ResponseEntity.status(409).body("El usuario ya existe");
	        case 201:
	            return ResponseEntity.status(201).body(user.getNombre().toLowerCase().trim());
	        case 500:
	            return ResponseEntity.internalServerError().body("Error al guardar los datos");
	        default:
	            return ResponseEntity.internalServerError().build();
	    }
	}
	
	@GetMapping("/apks")
	public ResponseEntity<ArrayList<APK>> listApks() {
		ArrayList<APK> apks = servicio.listarAPKs();
		
		if (apks == null) {
			return ResponseEntity.internalServerError().build();
		}
		else if (apks.isEmpty()) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.status(200).body(apks);
		}
	}
}
