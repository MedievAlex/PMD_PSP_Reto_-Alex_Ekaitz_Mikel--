package com.example.emastore.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class APK {
    private String titulo;
    private String autor;
    private String descripcion;
    private String image;
    private Bitmap imageBitmap;

    public APK() {}

    public APK(String titulo, String autor, String descripcion, String image) {
        this.titulo = titulo;
        this.autor = autor;
        this.descripcion = descripcion;
        this.image = image;
        this.imageBitmap = decodeBase64ToBitmap(image);
    }

    // ============= GETTERS & SETTERS =============
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImage() { return image; }

    public void setImage(String image) {
        this.image = image;
        this.imageBitmap = decodeBase64ToBitmap(image);
    }

    public Bitmap getImageBitmap() {
        // Si imageBitmap es nulo pero tenemos el string base64, intentamos decodificar
        if (imageBitmap == null && image != null) {
            imageBitmap = decodeBase64ToBitmap(image);
        }
        return imageBitmap;
    }

    private Bitmap decodeBase64ToBitmap(String base64String) {
        if (base64String == null || base64String.trim().isEmpty()) {
            Log.w("APK", "Cadena Base64 nula o vacía");
            return null;
        }

        try {
            // Log para depuración
            Log.d("APK", "Longitud Base64: " + base64String.length());
            Log.d("APK", "Inicio Base64: " + base64String.substring(0, Math.min(base64String.length(), 50)));

            String cleanBase64 = base64String.trim();

            // Manejar diferentes formatos de Base64
            if (cleanBase64.startsWith("data:image")) {
                // Extraer solo la parte Base64 después de la coma
                int commaIndex = cleanBase64.indexOf(',');
                if (commaIndex != -1) {
                    cleanBase64 = cleanBase64.substring(commaIndex + 1);
                }
            }

            // Limpiar espacios y saltos de línea
            cleanBase64 = cleanBase64.replaceAll("\\s+", "");

            // Verificar que la cadena no esté vacía después de la limpieza
            if (cleanBase64.isEmpty()) {
                Log.e("APK", "Cadena Base64 vacía después de limpiar");
                return null;
            }

            // Decodificar Base64
            byte[] decodedBytes;
            try {
                decodedBytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT);
            } catch (IllegalArgumentException e) {
                Log.e("APK", "Error en formato Base64: " + e.getMessage());
                // Intentar con NO_WRAP si DEFAULT falla
                decodedBytes = android.util.Base64.decode(cleanBase64, android.util.Base64.NO_WRAP);
            }

            // Decodificar bytes a Bitmap
            Bitmap bmp = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            if (bmp == null) {
                Log.e("APK", "BitmapFactory.decodeByteArray devolvió null");
                return null;
            }

            Log.d("APK", "Bitmap creado: " + bmp.getWidth() + "x" + bmp.getHeight());

            // Redimensionar manteniendo la relación de aspecto
            int targetSize = 90;
            int width = bmp.getWidth();
            int height = bmp.getHeight();

            if (width > targetSize || height > targetSize) {
                float scale = Math.min((float) targetSize / width, (float) targetSize / height);
                int newWidth = Math.round(width * scale);
                int newHeight = Math.round(height * scale);

                bmp = Bitmap.createScaledBitmap(bmp, newWidth, newHeight, true);
                Log.d("APK", "Bitmap redimensionado: " + newWidth + "x" + newHeight);
            }

            return bmp;

        } catch (Exception e) {
            Log.e("APK", "Error decodificando Base64", e);
            return null;
        }
    }

    @Override
    public String toString() {
        return "APK [titulo=" + titulo + ", autor=" + autor + ", descripcion=" + descripcion + ", image=" + image + "]";
    }
}