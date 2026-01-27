package ema.maven.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

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
	private static final String FILES_DIR = "files";
	private static final String APKS_DIR = FILES_DIR + "/apks";
	private static final String IMAGES_DIR = FILES_DIR + "/images";

	@PostConstruct
	public void init() throws StreamReadException, DatabindException, IOException {
		cargarUsuarios();
		calcularUltimoId();
		generarAPKs();
	}

	private void guardarJson(String rutaArchivo, Object datos) {
		try {
			File archivo = new File(rutaArchivo);
			mapeador.writerWithDefaultPrettyPrinter().writeValue(archivo, datos);
		} catch (IOException e) {
			System.err.println("Error guardando " + rutaArchivo + ": " + e.getMessage());
		}
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
		guardarJson(USERS_FILE, usuarios);
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
	private String obtenerIconoBase64(String nombre) {
		try {
			File carpetaIconos = new File(IMAGES_DIR);
			File[] iconos = carpetaIconos.listFiles();

			if (iconos == null) {
				return "";
			}

			for (File icono : iconos) {
				String nombreIcono = icono.getName().toLowerCase();

				if (nombreIcono.startsWith(nombre.toLowerCase())) {
					byte[] bytes = Files.readAllBytes(icono.toPath());

					return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
				}
			}
		} catch (Exception e) {
			System.err.println("Error obteniendo icono para " + nombre + ": " + e.getMessage());
		}

		return "";
	}

	private void generarAPKs() {
		apks.clear();
		
		new File(APKS_DIR).mkdirs(); // Crear carpetas si no existen
	    new File(IMAGES_DIR).mkdirs();

		File carpetaApks = new File(APKS_DIR);
		File[] archivos = carpetaApks.listFiles();

		if (archivos == null)
			return;

		for (File a : archivos) {
			if (a.isFile() && a.getName().toLowerCase().endsWith(".apk")) {
				String nombre = a.getName().replace(".apk", "");

				String icono = obtenerIconoBase64(nombre);

				APK apk = new APK();
				apk.setTitulo(nombre);
				apk.setDescripcion("Aplicación Android: " + apk.getTitulo());
				apk.setImage(icono);

				apks.add(apk);
			}
		}

		guardarJson(APKS_FILE, apks);
	}

	public ArrayList<APK> listarAPKs() {
		return new ArrayList<>(apks);
	}

	public Resource descargarAPK(String nombreApk) throws IOException {
	    Path apkPath = Paths.get(APKS_DIR + "/" + nombreApk);
	    Resource resource = new UrlResource(apkPath.toUri());
	    
	    if (!resource.exists()) {
	    	return null;
	    }
	    
	    return resource;
	}
}
