package ema.maven.model;

public class APK {
	private String titulo;
	private String autor;
	private String descripcion;
	private String image;
	
	public APK() {}
	
	public APK(String titulo, String autor, String descripcion, String image) {
		this.titulo = titulo;
		this.autor = autor;
		this.descripcion = descripcion;
		this.image = image;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	
	public String getAutor() {
		return autor;
	}
	
	public void setAutor(String autor) {
		this.autor = autor;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Override
	public String toString() {
		return "APK [titulo=" + titulo + ", autor=" + autor + ", descripcion=" + descripcion + ", image=" + image + "]";
	}
}
