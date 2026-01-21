package ema.maven.controller;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ema.maven.model.Alumno;
import ema.maven.service.*;

@RestController
@RequestMapping("")
public class ControladorServicio {
	private final Servicio servicio;

	public ControladorServicio(Servicio servicio) {
		this.servicio = servicio;
	}

	@GetMapping("/alumnos")
	public ResponseEntity<ArrayList<Alumno>> getAll() {
		return ResponseEntity.ok(servicio.getAll());
	}
	
	@GetMapping("/alumnos/{id}")
	public ResponseEntity<Alumno> getById(@PathVariable int id) {
		Alumno alumno = servicio.getById(id);
		
		if (alumno == null) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok(alumno);
		}
	}
	
	@GetMapping("/alumnos/curso/{curso}")
	public ResponseEntity<ArrayList<Alumno>> getByCurso(@PathVariable String curso) {
		ArrayList<Alumno> alumnos = servicio.getByCurso(curso);
		
		if (alumnos.isEmpty()) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.ok(alumnos);
		}
	}
	
	@GetMapping("/alumnos/count")
	public ResponseEntity<HashMap<String, Integer>> getCount() {
	    HashMap<String, Integer> response = new HashMap<>();
	    response.put("total", servicio.getAll().size());
	    return ResponseEntity.ok(response);
	}
	
	@GetMapping("/alumnos/buscar")
	public ResponseEntity<ArrayList<Alumno>> getByName(@RequestParam String nombre) {
		ArrayList<Alumno> alumnos = servicio.getByName(nombre);
		
		if (alumnos.isEmpty()) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.ok(alumnos);
		}
	}
}
