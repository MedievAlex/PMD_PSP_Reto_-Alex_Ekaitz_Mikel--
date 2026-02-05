package com.example.emastore.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emastore.R;
import com.example.emastore.service.ApiService;
import com.example.emastore.client.RetrofitClient;
import com.example.emastore.model.APK;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MenuAdapter adapter;
    private List<APK> listaAPKs = new ArrayList<>();
    private MediaPlayer mediaPlayer;
    private boolean isAudioPlaying = true;
    private String usuarioActual;
    private TextView textView3;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            initViews();
            obtenerUsuarioActual();
            setupRecyclerView();
            setupButtons();
            setupAudio();
            cargarAPKsDesdeAPI();

        } catch (Exception e) {
            Log.e(TAG, "ERROR en onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error al iniciar: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            volverALogin();
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        textView3 = findViewById(R.id.textView3);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    private void obtenerUsuarioActual() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("usuario")) {
            usuarioActual = intent.getStringExtra("usuario");
            Log.d(TAG, "Usuario del Intent: " + usuarioActual);
        }

        if (usuarioActual == null || usuarioActual.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE);
            usuarioActual = prefs.getString("nombreUsuario", "Invitado");
            boolean estaLogueado = prefs.getBoolean("estaLogueado", false);

            if (!estaLogueado) {
                Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
                volverALogin();
                return;
            }
        }

        if (textView3 != null) {
            textView3.setText("Usuario: " + usuarioActual);
        }
    }

    private void setupRecyclerView() {
        adapter = new MenuAdapter(listaAPKs, new MenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(APK apk) {
                abrirDetallesAPK(apk);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void cargarAPKsDesdeAPI() {
        ApiService apiService = RetrofitClient.getApiService();

        Call<List<APK>> call = apiService.getApks();
        call.enqueue(new Callback<List<APK>>() {
            @Override
            public void onResponse(Call<List<APK>> call, Response<List<APK>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaAPKs.clear();
                    listaAPKs.addAll(response.body());

                    adapter.notifyDataSetChanged();

                    Log.d(TAG, "APKs cargadas: " + listaAPKs.size());
                    if (listaAPKs.isEmpty()) {
                        Toast.makeText(MainActivity.this,
                                "No hay APKs disponibles",
                                Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Log.e(TAG, "Error al cargar APKs. Código: " + response.code());
                    Toast.makeText(MainActivity.this,
                            "Error al cargar APKs: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    cargarDatosEjemplo();
                }
            }

            @Override
            public void onFailure(Call<List<APK>> call, Throwable t) {
                Log.e(TAG, "Error de conexión: " + t.getMessage());
                Toast.makeText(MainActivity.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
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

    private void abrirDetallesAPK(APK apk) {
        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
        intent.putExtra("titulo", apk.getTitulo());
        intent.putExtra("autor", apk.getAutor());
        intent.putExtra("descripcion", apk.getDescripcion());
        intent.putExtra("image", apk.getImage());

        startActivity(intent);
    }

    private void setupButtons() {
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnExit = findViewById(R.id.btnExit);
        Button btnAudio = findViewById(R.id.bttnAudio);

        if (btnLogout != null) {
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cerrarSesion();
                }
            });
        }

        if (btnExit != null) {
            btnExit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    salirAplicacion();
                }
            });
        }
        if (btnAudio != null) {
            btnAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleAudio();
                }
            });
        }
    }

    private void cerrarSesion() {
        SharedPreferences prefs = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void salirAplicacion() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        finishAffinity();
        System.exit(0);
    }

    private void toggleAudio() {
        try {
            Button btnAudio = findViewById(R.id.bttnAudio);
            if (isAudioPlaying) {
                btnAudio.setText(R.string.unmute_audio);
                audioPlayer(false);
                isAudioPlaying = false;
                Log.d(TAG, "Audio pausado");
            } else {
                btnAudio.setText(R.string.mute_audio);
                audioPlayer(true);
                isAudioPlaying = true;
                Log.d(TAG, "Audio iniciado");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en toggleAudio: " + e.getMessage());
        }
    }

    private void setupAudio() {
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                if (isAudioPlaying) {
                    mediaPlayer.start();
                }
                Log.d(TAG, "MediaPlayer creado exitosamente");
            } else {
                Log.e(TAG, "Error: MediaPlayer es null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creando MediaPlayer: " + e.getMessage());
        }
    }

    private void audioPlayer(boolean shouldPlay) {
        try {
            if (mediaPlayer != null) {
                if (shouldPlay) {
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                    }
                } else {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    }
                }
            } else {
                Log.w(TAG, "MediaPlayer es null en audioPlayer");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en audioPlayer: " + e.getMessage());
        }
    }

    private void volverALogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mediaPlayer != null && isAudioPlaying && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}