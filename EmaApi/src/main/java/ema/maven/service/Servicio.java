package ema.maven.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;

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
	public Usuario login(Usuario user) {
		String nombre = user.getNombre().toLowerCase().trim();

		for (Usuario a : usuarios) {
			if (a.getNombre().equals(nombre) && a.getContraseña().equals(user.getContraseña())) {
				return a;
			}
		}

		return null;
	}

	// Sign Up
	public String signUp(Usuario user) {
		String nombre = user.getNombre().toLowerCase().trim();

		for (Usuario u : usuarios) {
			if (u.getNombre().equals(nombre)) {
				throw new ResponseStatusException(HttpStatus.CONFLICT); // Devolver un 409 directamente si el titulo ya existe
			}
		}

		int nuevoId = siguienteId();
		user.setId(nuevoId);
		user.setNombre(nombre);

		usuarios.add(user);

		try {
			guardarJson(USERS_FILE, usuarios);
		} catch (Exception e) {
			usuarios.remove(user);
			ultimoId--;
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return nombre;
	}

	// ========== MÉTODOS PARA APKS ==========
	private String obtenerIcono(String titulo) {
		try {
			File carpetaIconos = new File(IMAGES_DIR);
			File[] iconos = carpetaIconos.listFiles();

			if (iconos == null) {
				return "";
			}

			for (File icono : iconos) {
				String nombreIcono = icono.getName().toLowerCase();

				if (nombreIcono.startsWith(titulo.toLowerCase())) {
					byte[] bytes = Files.readAllBytes(icono.toPath());

					return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes); // Encodear el array de bytes a base64
				}
			}

			for (File icono : iconos) {
				if (icono.getName().equalsIgnoreCase("default.png")) {
					byte[] bytes = Files.readAllBytes(icono.toPath());
					return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
				}
			}
		} catch (Exception e) {
			System.err.println("Error obteniendo icono para " + titulo + ": " + e.getMessage());
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

				String icono = obtenerIcono(nombre);

				APK apk = new APK();
				apk.setTitulo(nombre);
				apk.setDescripcion("Aplicación Android: " + nombre);
				apk.setImage(icono);

				apks.add(apk);
			}
		}

		guardarJson(APKS_FILE, apks);
	}

	// Obtener todas las APKs
	public ArrayList<APK> getAPKs() {
		return new ArrayList<>(apks); // Devolver copia y no lista original
	}

	// Obtener una APK
	public APK getAPK(String titulo) {
		for (APK apk : apks) {
			if (apk.getTitulo().equalsIgnoreCase(titulo)) {
				return apk;
			}
		}

		return null;
	}

	// Crear una APK en el json
	public APK addAPK(APK apk) {
	    if (getAPK(apk.getTitulo()) != null) {
	        throw new ResponseStatusException(HttpStatus.CONFLICT);
	    }

	    if (apk.getDescripcion() == null || apk.getDescripcion().trim().isEmpty()) {
	        apk.setDescripcion("Aplicación Android: " + apk.getTitulo());
	    }

	    if (apk.getImage() == null || apk.getImage().trim().isEmpty()) {
	        String icono = obtenerIcono(apk.getTitulo());
	        apk.setImage(icono);
	    }

	    apks.add(apk);

	    try {
	        guardarJson(APKS_FILE, apks);
	    } catch (Exception e) {
	        apks.remove(apk);
	        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR); // Devolver un 500 directamente si hay excepcion
	    }

	    return apk;
	}

	// Modificar una APK
	public APK updateAPK(String titulo, APK apk) {
	    APK existente = getAPK(titulo);

	    if (existente == null) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND); // Devolver un 404 directamente si no existe
	    }

	    String tituloOriginal = existente.getTitulo();
	    String descripcionOriginal = existente.getDescripcion();

	    if (apk.getTitulo() != null && !apk.getTitulo().equalsIgnoreCase(titulo)) {
	        if (getAPK(apk.getTitulo()) != null) {
	            throw new ResponseStatusException(HttpStatus.CONFLICT); // Devolver un 409 directamente si el titulo ya existe
	        }

	        existente.setTitulo(apk.getTitulo());
	    }

	    if (apk.getDescripcion() != null) {
	        existente.setDescripcion(apk.getDescripcion());
	    }

	    try {
	        guardarJson(APKS_FILE, apks);
	    } catch (Exception e) {
	        existente.setTitulo(tituloOriginal);
	        existente.setDescripcion(descripcionOriginal);
	        
	        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR); // Devolver un 500 directamente si hay excepcion
	    }

	    return existente;
	}

	// Eliminar una APK
	public void deleteAPK(String titulo) {
	    APK existente = getAPK(titulo);

	    if (existente == null) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND); // Devolver un 404 directamente si no existe
	    }

	    apks.remove(existente);

	    try {
	        guardarJson(APKS_FILE, apks);
	    } catch (Exception e) {
	        apks.add(existente);
	        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR); // Devolver un 500 directamente si hay excepcion
	    }
	}

	// Descargar una APK
	public Resource downloadAPK(String titulo) {
	    try {
	        Path path = Paths.get(APKS_DIR + "/" + titulo);
	        Resource resource = new UrlResource(path.toUri());

	        if (!resource.exists()) {
	            throw new ResponseStatusException(HttpStatus.NOT_FOUND); // Devolver un 404 directamente si no existe
	        }

	        return resource;
	    } catch (ResponseStatusException e) {  // Relanzar 404 para que no siempre devuelva un 500
	        throw e;
	    } catch (Exception e) {
	        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR); // Devolver un 500 directamente si hay excepcion
	    }
	}
}
