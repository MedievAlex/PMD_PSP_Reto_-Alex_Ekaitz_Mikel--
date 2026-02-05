package com.example.emastore;

import com.example.emastore.Usuario;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    String BASE_URL = "http://10.0.2.2:8080/api/";

    @POST("login")
    Call<ResponseBody> login(@Body Usuario usuario);

    @POST("register")
    Call<String> register(@Body Usuario usuario);

    @GET("apks")
    Call<List<APK>> getApks();
}