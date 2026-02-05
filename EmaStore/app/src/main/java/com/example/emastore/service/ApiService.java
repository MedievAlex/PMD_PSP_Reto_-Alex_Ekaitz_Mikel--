package com.example.emastore.service;

import com.example.emastore.model.APK;
import com.example.emastore.model.Usuario;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface ApiService {

    String BASE_URL = "http://10.0.2.2:8080/api/";

    // ============= AUTENTICACIÓN =============

    @POST("login")
    Call<String> login(@Body Usuario usuario);

    @POST("register")
    Call<String> register(@Body Usuario usuario);

    // ============= GESTIÓN DE APKs =============

    /**
     * Obtiene todas las APKs disponibles
     */
    @GET("apks")
    Call<List<APK>> getApks();

    /**
     * Obtiene una APK específica por título
     * @param titulo Título de la APK
     */
    @GET("apk/{titulo}")
    Call<APK> getApk(@Path("titulo") String titulo);

    /**
     * Crea una nueva APK
     * @param apk Objeto APK a crear
     */
    @POST("apk")
    Call<APK> addApk(@Body APK apk);

    /**
     * Actualiza una APK existente
     * @param titulo Título de la APK a actualizar
     * @param apk Nuevos datos de la APK
     */
    @PUT("apk/{titulo}")
    Call<APK> updateApk(@Path("titulo") String titulo, @Body APK apk);

    /**
     * Elimina una APK
     * @param titulo Título de la APK a eliminar
     */
    @DELETE("apk/{titulo}")
    Call<String> deleteApk(@Path("titulo") String titulo);

    // ============= DESCARGA Y HASH =============

    /**
     * Descarga el archivo APK
     * @param titulo Título de la APK a descargar
     */
    @Streaming
    @GET("download/{titulo}")
    Call<ResponseBody> downloadApk(@Path("titulo") String titulo);

    /**
     * Obtiene el hash de una APK
     * @param titulo Título de la APK
     * @param algoritmo Algoritmo de hash (MD5, SHA-1, SHA-256). Opcional.
     */
    @GET("hash/{titulo}")
    Call<String> getHash(@Path("titulo") String titulo, @Query("algoritmo") String algoritmo);
}