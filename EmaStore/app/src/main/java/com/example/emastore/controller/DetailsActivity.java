package com.example.emastore.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.emastore.R;
import com.example.emastore.client.RetrofitClient;
import com.example.emastore.model.APK;
import com.example.emastore.service.ApiService;
import com.example.emastore.service.AudioService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity {

    // ============= CONSTANTES =============
    private static final String PREFS_NAME = "UsuarioPrefs";
    private static final String KEY_NOMBRE_USUARIO = "nombreUsuario";
    private static final String KEY_ESTA_LOGUEADO = "estaLogueado";
    private static final String KEY_AUDIO_ENABLED = "audioEnabled";

    // ============= VARIABLES DE INSTANCIA =============
    private TextView textTitulo, textAutor, textDescripcion, textUsuario;
    private ImageView imageAPK;
    private APK apkActual;
    private String usuarioActual;
    private boolean isAudioPlaying = true;

    // ============= CICLO DE VIDA =============
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (verificarSesionExistente()) return;

        setContentView(R.layout.activity_details);
        inicializarVistas();
        configurarBotones();
        cargarDatosDesdeIntent();

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
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        usuarioActual = prefs.getString(KEY_NOMBRE_USUARIO, "Invitado");

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("usuario")) {
            String usuarioDelIntent = intent.getStringExtra("usuario");
            if (usuarioDelIntent != null && !usuarioDelIntent.isEmpty()) {
                usuarioActual = usuarioDelIntent;
            }
        }

        if (textUsuario != null) {
            textUsuario.setText("Usuario: " + usuarioActual);
        }
    }

    // ============= VISTAS =============
    private void inicializarVistas() {
        textTitulo = findViewById(R.id.textTitulo);
        textAutor = findViewById(R.id.textAutor);
        textDescripcion = findViewById(R.id.textDescripcion);
        textUsuario = findViewById(R.id.textView3);
        imageAPK = findViewById(R.id.imageAPK);
    }

    // ============= BOTONES =============
    private void configurarBotones() {
        Button btnBack = findViewById(R.id.btnBack);
        Button btnExit = findViewById(R.id.btnExit);
        Button btnAudio = findViewById(R.id.bttnAudio);
        Button btnDownload = findViewById(R.id.btnDownload);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isAudioPlaying = prefs.getBoolean(KEY_AUDIO_ENABLED, true);
        if (btnAudio != null) {
            btnAudio.setText(isAudioPlaying ? R.string.mute_audio : R.string.unmute_audio);
            btnAudio.setOnClickListener(v -> toggleAudio(btnAudio));
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> volverAMain());
        }

        if (btnExit != null) {
            btnExit.setOnClickListener(v -> salirAplicacion());
        }

        if (btnDownload != null) {
            btnDownload.setOnClickListener(v -> descargarAPK());
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

    // ============= DATOS DEL APK =============
    private void cargarDatosDesdeIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            String titulo = intent.getStringExtra("titulo");
            String autor = intent.getStringExtra("autor");
            String descripcion = intent.getStringExtra("descripcion");
            String image = intent.getStringExtra("image");

            apkActual = new APK(titulo, autor, descripcion, image);

            if (textTitulo != null) textTitulo.setText(apkActual.getTitulo());
            if (textAutor != null) textAutor.setText(apkActual.getAutor());
            if (textDescripcion != null) textDescripcion.setText(apkActual.getDescripcion());

            cargarUsuarioActual();

            if (imageAPK != null && apkActual.getImageBitmap() != null) {
                imageAPK.setImageBitmap(apkActual.getImageBitmap());
            }
        }
    }

    // ============= DESCARGAR APK =============
    private void descargarAPK() {
        if (apkActual == null) return;

        String titulo = apkActual.getTitulo();
        String downloadUrl = ApiService.BASE_URL + "download/" + titulo;

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
        startActivity(browserIntent);
        Toast.makeText(this, "Descargando " + titulo, Toast.LENGTH_SHORT).show();
    }

    // ============= NAVEGACIÓN =============
    private void volverAMain() {
        pausarAudioService();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void volverALogin() {
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
    }
}