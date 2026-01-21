package ema.maven.service;

import java.util.ArrayList;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ema.maven.model.Alumno;

@Service
public class Servicio {
	public ArrayList<Alumno> getAll() {
	    try {
	        return new ObjectMapper().readValue(
	            new ClassPathResource("alumnos.json").getFile(),
	            new TypeReference<ArrayList<Alumno>>() {}
	        );
	    } catch (Exception e) { return new ArrayList<>(); }
	}
	
	public Alumno getById(int id) {
	    try {
			ArrayList<Alumno> alumnos = new ObjectMapper().readValue(new ClassPathResource("alumnos.json").getFile(), new TypeReference<ArrayList<Alumno>>() {});
			
			for (Alumno a : alumnos) {
				if (a.getId() == id) {
					return a;
				}
			}
			
			return null;
		} catch (Exception e) {
			return null;
		}
	}
	
	public ArrayList<Alumno> getByCurso(String curso) {
		ArrayList<Alumno> alumnosCurso = new ArrayList<>();

	    try {
	    	ArrayList<Alumno> alumnos = new ObjectMapper().readValue(new ClassPathResource("alumnos.json").getFile(), new TypeReference<ArrayList<Alumno>>() {});
	    	
	    	for (Alumno a : alumnos) {
	    		if (a.getCurso().equalsIgnoreCase(curso)) {
	    			alumnosCurso.add(a);
	    		}
	    	}
	    	
	    	return alumnosCurso;
	    } catch (Exception e) { return new ArrayList<>(); }
	}
	
	public ArrayList<Alumno> getByName(String nombre) {
		ArrayList<Alumno> alumnosCurso = new ArrayList<>();
		String nombreLower = nombre.toLowerCase();

	    try {
	    	ArrayList<Alumno> alumnos = new ObjectMapper().readValue(new ClassPathResource("alumnos.json").getFile(), new TypeReference<ArrayList<Alumno>>() {});
	    	
	    	for (Alumno a : alumnos) {
	    		if (a.getNombre().toLowerCase().contains(nombreLower)) {
	    			alumnosCurso.add(a);
	    		}
	    	}
	    	
	    	return alumnosCurso;
	    } catch (Exception e) { return new ArrayList<>(); }
	}
}
