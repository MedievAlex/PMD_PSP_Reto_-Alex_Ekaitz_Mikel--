package ema.maven.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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
				usuarios = mapeador.readValue(archivoData, new TypeReference<ArrayList<Usuario>>() {});
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

		try {
			for (Usuario a : usuarios) {
				if (a.getNombre().equals(nombre) && a.getContraseña().equals(user.getContraseña())) {
					return a;
				}
			}
	
			return null;
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Sign Up
	public Usuario signUp(Usuario user) {
		String nombre = user.getNombre().toLowerCase().trim();

		for (Usuario u : usuarios) {
			if (u.getNombre().equals(nombre)) {
				return null;
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

		return user;
	}

	// ========== MÉTODOS PARA APKS ==========
	private String obtenerIcono(String titulo) {
		try {
			File carpetaIconos = new File(IMAGES_DIR); // Crear carpeta si no existe
			if (!carpetaIconos.exists()) {
				carpetaIconos.mkdirs();
			}
			
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

		File carpetaFiles = new File(FILES_DIR); // Crear carpeta si no existe
		if (!carpetaFiles.exists()) {
			carpetaFiles.mkdirs();
		}
		
		File carpetaApks = new File(APKS_DIR); // Crear carpeta si no existe
		if (!carpetaApks.exists()) {
			carpetaApks.mkdirs();
		}
				
		File[] archivos = carpetaApks.listFiles();

		if (archivos == null)
			return;

		for (File a : archivos) {
			if (a.isFile() && a.getName().toLowerCase().endsWith(".apk")) {
				String nombre = a.getName().replace(".apk", "");

				String icono = obtenerIcono(nombre);

				APK apk = new APK();
				apk.setTitulo(nombre);
				apk.setAutor("ema");
				apk.setDescripcion("Aplicación Android: " + nombre);
				apk.setImage(icono);

				apks.add(apk);
			}
		}

		guardarJson(APKS_FILE, apks);
	}

	// Obtener todas las APKs
	public ArrayList<APK> getAPKs() {
		return apks;
	}

	// Obtener una APK
	public APK getAPK(String titulo) {
		try {
			for (APK apk : apks) {
				if (apk.getTitulo().equalsIgnoreCase(titulo)) {
					return apk;
				}
			}
	
			return null;
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Crear una APK en el json
	public APK addAPK(APK apk) {
	    if (getAPK(apk.getTitulo()) != null) {
	        return null;
	    }
	    
	    if (apk.getAutor() == null || apk.getAutor().trim().isEmpty()) {
	        apk.setAutor("Desconocido");
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
	        
	        return apk;
	    } catch (Exception e) {
	        apks.remove(apk);
	        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

	// Modificar una APK
	public APK updateAPK(String titulo, APK apk) {
	    APK existente = getAPK(titulo);

	    if (existente == null) {
	        return null;
	    }

	    String tituloOriginal = existente.getTitulo();
	    String descripcionOriginal = existente.getDescripcion();
	    String autorOriginal = existente.getAutor();
	    String imagenOriginal = existente.getImage();

	    if (apk.getTitulo() != null && !apk.getTitulo().equalsIgnoreCase(titulo)) {
	        if (getAPK(apk.getTitulo()) != null) {
	            throw new ResponseStatusException(HttpStatus.CONFLICT);
	        }

	        existente.setTitulo(apk.getTitulo());
	    }

	    if (apk.getDescripcion() != null) {
	        existente.setDescripcion(apk.getDescripcion());
	    }
	    
	    if (apk.getAutor() != null) {
	    	existente.setAutor(apk.getAutor());
	    }
	    
	    if (apk.getImage() != null || apk.getImage().trim().isEmpty()) {
	    	existente.setImage(apk.getImage());
	    }

	    try {
	        guardarJson(APKS_FILE, apks);
	    } catch (Exception e) {
	        existente.setTitulo(tituloOriginal);
	        existente.setDescripcion(descripcionOriginal);
	        existente.setAutor(autorOriginal);
	        existente.setImage(imagenOriginal);
	        
	        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
	    }

	    return existente;
	}

	// Eliminar una APK
	public void deleteAPK(String titulo) {
	    APK existente = getAPK(titulo);

	    if (existente == null) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	    }

	    apks.remove(existente);

	    try {
	        guardarJson(APKS_FILE, apks);
	    } catch (Exception e) {
	        apks.add(existente);
	        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

	// Descargar una APK
	public Resource downloadAPK(String titulo) {
	    try {
	        Path path = Paths.get(APKS_DIR + "/" + titulo);
	        
	        if (!Files.exists(path) || !Files.isRegularFile(path)) {
	            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	        }
	        
	        Resource resource = new UrlResource(path.toUri());

	        return resource;
	    } catch (ResponseStatusException e) {  // Relanzar para que no siempre devuelva un 500
	        throw e;
	    } catch (Exception e) {
	        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	// Comprobar hash APK
	public String getHash(String titulo, String algoritmo) {
		List<String> algoritmos = List.of("MD5", "SHA-1", "SHA-256");
		
		try {
			Path path = Paths.get(APKS_DIR + "/" + titulo);

	        if (!Files.exists(path) || !Files.isRegularFile(path)) {
	            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	        }
	        
	        if (algoritmo == null) {
	        	algoritmo = "SHA-256"; // Algoritmo por defecto
	        } else if (!algoritmos.contains(algoritmo)) {
	            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
	        }
	        
	        MessageDigest digest = MessageDigest.getInstance(algoritmo); // Elegir algoritmo
	        
	        try (FileInputStream fis = new FileInputStream(path.toFile())) { // Obtener array de bytes del archivo
	            byte[] buffer = new byte[1024];
	            int bytesRead;
	            while ((bytesRead = fis.read(buffer)) != -1) {
	                digest.update(buffer, 0, bytesRead);
	            }
	        }
		    
	        byte[] hashBytes = digest.digest();
	        
	        StringBuilder hexString = new StringBuilder();
	        
	        for (byte b : hashBytes) {
	            hexString.append(String.format("%02X", b));
	        }

	        return hexString.toString();
		} catch (ResponseStatusException e) {  // Relanzar para que no siempre devuelva un 500
	        throw e;
	    } catch (Exception e) {
	        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
}
