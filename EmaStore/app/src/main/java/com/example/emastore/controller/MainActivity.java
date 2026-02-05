package com.example.emastore.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emastore.R;
import com.example.emastore.client.RetrofitClient;
import com.example.emastore.model.APK;
import com.example.emastore.service.ApiService;
import com.example.emastore.service.AudioService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    // ============= CONSTANTES =============
    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "UsuarioPrefs";
    private static final String KEY_NOMBRE_USUARIO = "nombreUsuario";
    private static final String KEY_ESTA_LOGUEADO = "estaLogueado";
    private static final String KEY_AUDIO_ENABLED = "audioEnabled";

    // ============= VARIABLES DE INSTANCIA =============
    private RecyclerView recyclerView;
    private MenuAdapter adapter;
    private List<APK> listaAPKs = new ArrayList<>();
    private TextView textViewUsuario;
    private String usuarioActual;
    private boolean isAudioPlaying = true;

    // ============= CICLO DE VIDA =============
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (verificarSesionExistente()) return;

        setContentView(R.layout.activity_main);
        inicializarVistas();
        configurarBotones();
        configurarRecyclerView();
        cargarUsuarioActual();
        cargarAPKsDesdeAPI();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isAudioPlaying = prefs.getBoolean(KEY_AUDIO_ENABLED, true);
    }

    // ============= SESIÓN =============
    private boolean verificarSesionExistente() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean estaLogueado = prefs.getBoolean(KEY_ESTA_LOGUEADO, false);
        if (!estaLogueado) {
            volverALogin();
            return true;
        }
        usuarioActual = prefs.getString(KEY_NOMBRE_USUARIO, "Invitado");
        return false;
    }

    private void cargarUsuarioActual() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("usuario")) {
            usuarioActual = intent.getStringExtra("usuario");
        }
        if (textViewUsuario != null) {
            textViewUsuario.setText("Usuario: " + usuarioActual);
        }
    }

    private void guardarUsuarioEnPrefs(String nombreUsuario) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_NOMBRE_USUARIO, nombreUsuario);
        editor.putBoolean(KEY_ESTA_LOGUEADO, true);
        editor.apply();
    }

    // ============= VISTAS =============
    private void inicializarVistas() {
        recyclerView = findViewById(R.id.recyclerView);
        textViewUsuario = findViewById(R.id.textView3);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    // ============= BOTONES =============
    private void configurarBotones() {
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnExit = findViewById(R.id.btnExit);
        Button btnAudio = findViewById(R.id.bttnAudio);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isAudioPlaying = prefs.getBoolean(KEY_AUDIO_ENABLED, true);
        if (btnAudio != null) {
            btnAudio.setText(isAudioPlaying ? R.string.mute_audio : R.string.unmute_audio);
            btnAudio.setOnClickListener(v -> toggleAudio(btnAudio));
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> cerrarSesion());
        }

        if (btnExit != null) {
            btnExit.setOnClickListener(v -> salirAplicacion());
        }
    }

    private void toggleAudio(Button btnAudio) {
        isAudioPlaying = !isAudioPlaying;
        btnAudio.setText(isAudioPlaying ? R.string.mute_audio : R.string.unmute_audio);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_AUDIO_ENABLED, isAudioPlaying).apply();

        if (isAudioPlaying) iniciarAudioService();
        else pausarAudioService();
    }

    private void iniciarAudioService() {
        startService(new Intent(this, AudioService.class).setAction(AudioService.ACTION_PLAY));
    }

    private void pausarAudioService() {
        startService(new Intent(this, AudioService.class).setAction(AudioService.ACTION_PAUSE));
    }

    private void cerrarSesion() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().remove(KEY_NOMBRE_USUARIO).remove(KEY_ESTA_LOGUEADO).apply();

        pausarAudioService();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void salirAplicacion() {
        pausarAudioService();
        finishAffinity();
        System.exit(0);
    }

    // ============= RECYCLER VIEW / API =============
    private void configurarRecyclerView() {
        adapter = new MenuAdapter(listaAPKs, apk -> abrirDetallesAPK(apk));
        recyclerView.setAdapter(adapter);
    }

    private void abrirDetallesAPK(APK apk) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("titulo", apk.getTitulo());
        intent.putExtra("autor", apk.getAutor());
        intent.putExtra("descripcion", apk.getDescripcion());
        intent.putExtra("image", apk.getImage());
        startActivity(intent);
    }

    private void cargarAPKsDesdeAPI() {
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<APK>> call = apiService.getApks();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<APK>> call, Response<List<APK>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaAPKs.clear();
                    listaAPKs.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "Error al cargar APKs: " + response.code(), Toast.LENGTH_SHORT).show();
                    cargarDatosEjemplo();
                }
            }

            @Override
            public void onFailure(Call<List<APK>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                cargarDatosEjemplo();
            }
        });
    }

    private void cargarDatosEjemplo() {
        listaAPKs.clear();
        listaAPKs.add(new APK("WhatsApp", "Meta", "Mensajería instantánea", "whatsapp_icon"));
        listaAPKs.add(new APK("Instagram", "Meta", "Red social de fotos", "instagram_icon"));
        listaAPKs.add(new APK("Twitter", "Twitter Inc", "Red social de microblogging", "twitter_icon"));
        listaAPKs.add(new APK("Spotify", "Spotify AB", "Streaming de música", "spotify_icon"));
        listaAPKs.add(new APK("Netflix", "Netflix Inc", "Streaming de video", "netflix_icon"));
        listaAPKs.add(new APK("YouTube", "Google", "Plataforma de videos", "youtube_icon"));
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Mostrando datos de ejemplo", Toast.LENGTH_SHORT).show();
    }

    // ============= NAVEGACIÓN =============
    private void volverALogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // ============= CICLO DE VIDA =============
    @Override
    protected void onResume() {
        super.onResume();
        if (isAudioPlaying) iniciarAudioService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isAudioPlaying) pausarAudioService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pausarAudioService();
    }
}
