package ema.maven.model;

public class APK {
	private String image;
	private String titulo;
	private String descripcion;
	private String apk;
	
	public APK(String image, String titulo, String descripcion, String apk) {
		this.image = image;
		this.titulo = titulo;
		this.descripcion = descripcion;
		this.apk = apk;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
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

	public String getApk() {
		return apk;
	}

	public void setApk(String apk) {
		this.apk = apk;
	}

	@Override
	public String toString() {
		return "APK [image=" + image + ", titulo=" + titulo + ", descripcion=" + descripcion + ", apk=" + apk + "]";
	}
	
}
