package ema.maven.model;

public class Usuario {
	private int id;
	private String nombre;
	private String contraseña;
	
	public Usuario() {}
	
	public Usuario(int id, String nombre, String contraseña) {
		this.id = id;
		this.nombre = nombre;
		this.contraseña = contraseña;
	}
	public Usuario(String nombre, String contraseña) {
		this.nombre = nombre;
		this.contraseña = contraseña;
	}
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getContraseña() {
		return contraseña;
	}

	public void setContraseña(String contraseña) {
		this.contraseña = contraseña;
	}

	@Override
	public String toString() {
		return "Alumno [id=" + id + ", nombre=" + nombre + ", contraseña=" + contraseña + "]";
	}
}
