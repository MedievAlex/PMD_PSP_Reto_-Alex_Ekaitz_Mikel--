package ema.maven.model;

public class APK {
	private String titulo;
	private String descripcion;
	private String image;
	
	public APK() {}
	
	public APK(String titulo, String descripcion, String image, String apk) {
		this.titulo = titulo;
		this.descripcion = descripcion;
		this.image = image;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
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
		return "APK [titulo=" + titulo + ", descripcion=" + descripcion + "image=" + image + "]";
	}
	
}
