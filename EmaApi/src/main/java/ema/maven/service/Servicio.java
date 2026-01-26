package ema.maven.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ema.maven.model.APK;
import ema.maven.model.Usuario;
import jakarta.annotation.PostConstruct;

@Service
public class Servicio {
	private ObjectMapper mapeador = new ObjectMapper();

	private ArrayList<Usuario> usuarios = new ArrayList<Usuario>();
	private ArrayList<APK> apks = new ArrayList<APK>();

	private int ultimoId = 0;

	private static final String DATA_DIR = "data";
	private static final String USERS_FILE = DATA_DIR + "/usuarios.json";
	private static final String APKS_FILE = DATA_DIR + "/apks.json";

	@PostConstruct
	public void init() throws StreamReadException, DatabindException, IOException {
		cargarUsuarios();
		cargarAPKs();
		calcularUltimoId();
	}

	// ========== MÉTODOS PARA USUARIOS ==========
	private void cargarUsuarios() {
		try {
			File carpetaData = new File(DATA_DIR);
			if (!carpetaData.exists()) {
				carpetaData.mkdirs();
			}

			File archivoData = new File(USERS_FILE);

			if (archivoData.exists()) {
				usuarios = mapeador.readValue(archivoData, new TypeReference<ArrayList<Usuario>>() {
				});
			} else {
				usuarios = new ArrayList<>();

				mapeador.writerWithDefaultPrettyPrinter().writeValue(archivoData, usuarios);
			}

		} catch (Exception e) {
			System.err.println("Error cargando usuarios: " + e.getMessage());
			usuarios = new ArrayList<>();
		}
	}

	private void guardarUsuarios() {
		try {
			File file = new File(USERS_FILE);
			mapeador.writerWithDefaultPrettyPrinter().writeValue(file, usuarios);
		} catch (IOException e) {
			System.err.println("Error guardando usuarios: " + e.getMessage());
		}
	}

	private void calcularUltimoId() {
		ultimoId = 0;

		for (Usuario usuario : usuarios) {
			if (usuario.getId() > ultimoId) {
				ultimoId = usuario.getId();
			}
		}
	}

	private int siguienteId() {
		ultimoId++;
		return ultimoId;
	}

	// Login
	public int login(Usuario user) {
		String nombreLower;

		if (user == null || user.getNombre() == null || user.getContraseña() == null) {
			return 400;
		}

		nombreLower = user.getNombre().toLowerCase().trim();

		for (Usuario a : usuarios) {
			if (a.getNombre().equals(nombreLower) && a.getContraseña().equals(user.getContraseña())) {
				return 200;
			}
		}

		return 401;
	}

	// Sign Up
	public int signUp(Usuario user) {
		String nombreLower;

		if (user == null || user.getNombre() == null || user.getContraseña() == null) {
			return 400;
		}

		nombreLower = user.getNombre().toLowerCase().trim();

		for (Usuario u : usuarios) {
			if (u.getNombre().equals(nombreLower)) {
				return 409;
			}
		}

		int nuevoId = siguienteId();
		user.setId(nuevoId);
		user.setNombre(nombreLower);

		usuarios.add(user);

		try {
			guardarUsuarios();
		} catch (Exception e) {
			e.printStackTrace();
			usuarios.remove(user);
			ultimoId--;

			return 500;
		}

		return 201;
	}

	// ========== MÉTODOS PARA APKS ==========
	private void cargarAPKs() {
		try {
			File carpetaData = new File(DATA_DIR);
			if (!carpetaData.exists()) {
				carpetaData.mkdirs();
			}

			File archivoData = new File(APKS_FILE);

			if (archivoData.exists()) {
				apks = mapeador.readValue(archivoData, new TypeReference<ArrayList<APK>>() {
				});
			} else {
				apks = new ArrayList<>();
				mapeador.writerWithDefaultPrettyPrinter().writeValue(archivoData, apks);
			}

		} catch (Exception e) {
			System.err.println("Error cargando APKs: " + e.getMessage());
			apks = new ArrayList<>();
		}
	}

	public ArrayList<APK> listarAPKs() {
		return new ArrayList<>(apks);
	}
}
