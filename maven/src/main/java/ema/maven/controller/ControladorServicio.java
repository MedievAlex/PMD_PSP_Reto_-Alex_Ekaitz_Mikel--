package ema.maven.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ema.maven.model.Usuarios;
import ema.maven.service.*;

@RestController
@RequestMapping("")
public class ControladorServicio {
	private final Servicio servicio;

	public ControladorServicio(Servicio servicio) {
		this.servicio = servicio;
	}
	@PostMapping("/alumnos/buscar")
	public ResponseEntity<Usuarios> login(@RequestBody Usuarios user) {
	    Usuarios userrecibido = servicio.login(user);
	    
	    if (userrecibido == null) {
	        return ResponseEntity.noContent().build();
	    } else {
	        return ResponseEntity.ok(userrecibido);
	    }
	}
	@PostMapping()
    public ResponseEntity<?> signUp(@RequestBody Usuarios user){
    	int devuelto=0;
    	if(user == null) {
    		return ResponseEntity.badRequest().body("El usuario introduzido no es valido.");
    	}
    	devuelto= servicio.signUp(user);
    	if(devuelto ==409) {
    		return ResponseEntity.status(409).build();
    	}
    	return ResponseEntity.status(201).body(user);
    }
	

	
}
