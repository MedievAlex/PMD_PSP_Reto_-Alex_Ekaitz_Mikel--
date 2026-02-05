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

    @GET("apks")
    Call<List<APK>> getApks();

    @GET("apk/{titulo}")
    Call<APK> getApk(@Path("titulo") String titulo);

    @POST("apk")
    Call<APK> addApk(@Body APK apk);

    @PUT("apk/{titulo}")
    Call<APK> updateApk(@Path("titulo") String titulo, @Body APK apk);

    @DELETE("apk/{titulo}")
    Call<Void> deleteApk(@Path("titulo") String titulo);

    // ============= DESCARGA Y HASH =============

    @Streaming
    @GET("download/{titulo}")
    Call<ResponseBody> downloadApk(@Path("titulo") String titulo);

    @GET("hash/{titulo}")
    Call<String> getHash(@Path("titulo") String titulo, @Query("algoritmo") String algoritmo
    );
}
