package com.example.emastore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.ResponseBody;

public class LoginActivity extends AppCompatActivity {
    private boolean isPasswordVisible = false;
    private ImageButton btnTogglePass;
    private EditText etPassword, etUser;
    private MediaPlayer mediaPlayer;
    private boolean isAudioPlaying = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnSignUp = findViewById(R.id.btnSignup);
        Button btnExit = findViewById(R.id.btnExit);
        Button btnAudio = findViewById(R.id.bttnAudio);
        btnTogglePass = findViewById(R.id.btnTogglePass1);
        etPassword = findViewById(R.id.etPassword1);
        etUser = findViewById(R.id.etUser);

        btnTogglePass.setImageResource(R.drawable.ic_visibility_off);

        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realizarLogin();
            }
        });
        btnTogglePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
                finish();
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAudioPlaying) {
                    btnAudio.setText(R.string.unmute_audio);
                    audioPlayer(false);
                } else {
                    btnAudio.setText(R.string.mute_audio);
                    audioPlayer(true);
                }
                isAudioPlaying = !isAudioPlaying;
            }
        });

        audioPlayer(true);
    }

    private void realizarLogin() {
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
        Call<ResponseBody> call = apiService.login(usuario);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {

                Log.d("LOGIN_RESPONSE", "Código HTTP: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String nombreUsuario = response.body().string();

                        Toast.makeText(LoginActivity.this,
                                "Bienvenido " + nombreUsuario,
                                Toast.LENGTH_SHORT).show();

                        guardarUsuarioEnPrefs(nombreUsuario);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("usuario", nombreUsuario);
                        startActivity(intent);
                        finish();

                    } catch (Exception e) {
                        Log.e("LOGIN_PARSE_ERROR", "Error leyendo respuesta", e);
                        Toast.makeText(LoginActivity.this,
                                "Error al procesar la respuesta del servidor",
                                Toast.LENGTH_SHORT).show();
                    }

                } else if (response.code() == 401) {
                    Toast.makeText(LoginActivity.this,
                            "Usuario o contraseña incorrectos",
                            Toast.LENGTH_SHORT).show();

                } else if (response.code() == 400) {
                    Toast.makeText(LoginActivity.this,
                            "Datos inválidos",
                            Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(LoginActivity.this,
                            "Error del servidor: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("RetrofitError", "Error de conexión", t);
                Toast.makeText(LoginActivity.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarUsuarioEnPrefs(String nombreUsuario) {
        SharedPreferences prefs = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("nombreUsuario", nombreUsuario);
        editor.putBoolean("estaLogueado", true);
        editor.apply();
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            btnTogglePass.setImageResource(R.drawable.ic_visibility_off);
        } else {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            btnTogglePass.setImageResource(R.drawable.ic_visibility);
        }
        etPassword.setSelection(etPassword.getText().length());
        isPasswordVisible = !isPasswordVisible;
    }
    private void audioPlayer(boolean shouldPlay) {
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
    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && isAudioPlaying && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }
}