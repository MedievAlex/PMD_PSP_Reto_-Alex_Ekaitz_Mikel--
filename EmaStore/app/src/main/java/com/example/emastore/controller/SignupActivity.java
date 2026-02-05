package com.example.emastore.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.emastore.R;
import com.example.emastore.service.ApiService;
import com.example.emastore.client.RetrofitClient;
import com.example.emastore.model.Usuario;
import com.example.emastore.service.AudioService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    // ============= CONSTANTES =============
    private static final String TAG = "SignupActivity";
    private static final String PREFS_NAME = "UsuarioPrefs";
    private static final String KEY_NOMBRE_USUARIO = "nombreUsuario";
    private static final String KEY_ESTA_LOGUEADO = "estaLogueado";
    private static final String KEY_AUDIO_ENABLED = "audioEnabled";

    // ============= VARIABLES DE INSTANCIA =============
    private EditText etPassword, etUser;
    private ImageButton btnTogglePass;
    private boolean isPasswordVisible = false;
    private boolean isAudioPlaying = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (verificarSesionExistente()) {
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        inicializarVistas();
        configurarListeners();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isAudioPlaying = prefs.getBoolean(KEY_AUDIO_ENABLED, true);

        if (isAudioPlaying) {
            iniciarAudioService();
        }
    }

    private boolean verificarSesionExistente() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean estaLogueado = prefs.getBoolean(KEY_ESTA_LOGUEADO, false);

        if (estaLogueado) {
            String nombreUsuario = prefs.getString(KEY_NOMBRE_USUARIO, "");
            irAMainActivity(nombreUsuario);
            return true;
        }
        return false;
    }

    /**
     * Inicializa todas las vistas
     */
    private void inicializarVistas() {
        etPassword = findViewById(R.id.etPassword);
        etUser = findViewById(R.id.etUser);
        btnTogglePass = findViewById(R.id.btnTogglePass);

        btnTogglePass.setImageResource(R.drawable.ic_visibility);
    }

    /**
     * Configura todos los listeners de botones
     */
    private void configurarListeners() {
        Button btnSignUp = findViewById(R.id.btnSignup);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnExit = findViewById(R.id.btnExit);
        Button btnAudio = findViewById(R.id.bttnAudio);

        btnSignUp.setOnClickListener(v -> realizarSignup());

        btnTogglePass.setOnClickListener(v -> togglePasswordVisibility());

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });

        btnExit.setOnClickListener(v -> {
            detenerAudioService();
            finish();
        });

        btnAudio.setOnClickListener(v -> {
            isAudioPlaying = !isAudioPlaying;

            if (isAudioPlaying) {
                btnAudio.setText(R.string.mute_audio);
                iniciarAudioService();
            } else {
                btnAudio.setText(R.string.unmute_audio);
                pausarAudioService();
            }

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_AUDIO_ENABLED, isAudioPlaying).apply();
        });

        btnAudio.setText(isAudioPlaying ? R.string.mute_audio : R.string.unmute_audio);
    }

    /**
     * Realiza el proceso de registro
     */
    private void realizarSignup() {
        String username = etUser.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    "Por favor, completa todos los campos",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(username);
        usuario.setContraseña(password);

        ApiService apiService = RetrofitClient.getApiService();
        Call<String> call = apiService.register(usuario);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                manejarRespuestaSignup(response);
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "Error de conexión", t);
                Toast.makeText(SignupActivity.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Maneja la respuesta del servidor al registrarse
     */
    private void manejarRespuestaSignup(Response<String> response) {
        Log.d(TAG, "Código HTTP: " + response.code());

        if (response.isSuccessful() && response.body() != null) {
            String respuesta = response.body();

            Toast.makeText(SignupActivity.this,
                    "Cuenta creada con éxito. Bienvenido " + respuesta,
                    Toast.LENGTH_SHORT).show();

            guardarUsuarioEnPrefs(respuesta);
            irAMainActivity(respuesta);

        } else if (response.code() == 409) {
            Toast.makeText(SignupActivity.this,
                    "El usuario ya existe",
                    Toast.LENGTH_SHORT).show();

        } else if (response.code() == 400) {
            Toast.makeText(SignupActivity.this,
                    "Datos inválidos",
                    Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(SignupActivity.this,
                    "Error del servidor: " + response.code(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Guarda la sesión del usuario en SharedPreferences
     */
    private void guardarUsuarioEnPrefs(String nombreUsuario) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_NOMBRE_USUARIO, nombreUsuario);
        editor.putBoolean(KEY_ESTA_LOGUEADO, true);
        editor.apply();
    }

    /**
     * Navega a MainActivity
     */
    private void irAMainActivity(String nombreUsuario) {
        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
        intent.putExtra("usuario", nombreUsuario);
        startActivity(intent);
        finish();
    }

    /**
     * Alterna la visibilidad de la contraseña
     */
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            btnTogglePass.setImageResource(R.drawable.ic_visibility);
        } else {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            btnTogglePass.setImageResource(R.drawable.ic_visibility_off);
        }
        etPassword.setSelection(etPassword.getText().length());
        isPasswordVisible = !isPasswordVisible;
    }

    // ============= CONTROL DEL SERVICIO DE AUDIO =============

    private void iniciarAudioService() {
        Intent serviceIntent = new Intent(this, AudioService.class);
        serviceIntent.setAction(AudioService.ACTION_PLAY);
        startService(serviceIntent);
    }

    private void pausarAudioService() {
        Intent serviceIntent = new Intent(this, AudioService.class);
        serviceIntent.setAction(AudioService.ACTION_PAUSE);
        startService(serviceIntent);
    }

    private void detenerAudioService() {
        Intent serviceIntent = new Intent(this, AudioService.class);
        stopService(serviceIntent);
    }

    // ============= CICLO DE VIDA =============

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}