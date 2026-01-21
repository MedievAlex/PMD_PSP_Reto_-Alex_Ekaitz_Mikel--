package ema.maven.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ema.maven.model.Usuarios;
import jakarta.annotation.PostConstruct;

@Service
public class Servicio {
	private ArrayList<Usuarios> usuarios = new ArrayList<Usuarios>();
	private ObjectMapper mapeador = new ObjectMapper();

	@PostConstruct
	public void init() throws StreamReadException, DatabindException, IOException {
		cargarArrayJson();

	}

	private void cargarArrayJson() throws StreamReadException, DatabindException, IOException {
		try {
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream("alumnos.json");
			if (inputStream != null) {
				usuarios = mapeador.readValue(inputStream, new TypeReference<ArrayList<Usuarios>>() {
				});
				System.out.println("Datos cargados correctamente: " + usuarios.size() + " usuarios");
			} else {
				System.err.println("No se encontró el archivo usuarios.json");
			}
		} catch (Exception e) {
			System.err.println("Error al cargar el archivo JSON: " + e.getMessage());
		}

	}

	public Usuarios login(Usuarios user) {
	    String nombreLower = user.getNombre().toLowerCase();
	    for (Usuarios a : usuarios) {
	        if (a.getNombre().toLowerCase().equals(nombreLower) 
	                && a.getContraseña().equals(user.getContraseña())) {
	            return a;
	        }
	    }
	    return null;
	}

	public int signUp(Usuarios user) {
		int devolver = 202;
		for (Usuarios user1 : usuarios) {
			if (user1.getNombre().equalsIgnoreCase(user.getNombre())) {
				return 409;
			}
		}
		usuarios.add(user);
		try {
			File file = new ClassPathResource("alumnos.json").getFile();
			mapeador.writerWithDefaultPrettyPrinter().writeValue(file, usuarios);
		} catch (IOException e) {
			e.printStackTrace();
			return 500; 
		}
		return devolver; 
	}

}
