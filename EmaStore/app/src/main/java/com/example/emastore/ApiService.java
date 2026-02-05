package com.example.emastore;

import com.example.emastore.Usuario;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    String BASE_URL = "http://localhost:8080/api/";

    @POST("login")
    Call<String> login(@Body Usuario usuario);

    @POST("register")
    Call<String> register(@Body Usuario usuario);

    @GET("apks")
    Call<List<APK>> getApks();
}