package com.example.emastore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
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

import java.util.ArrayList;

import model.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private EditText etPassword;
    private ImageButton btnTogglePass;

    private EditText etUser;
    private ArrayList<User> usuarios = new ArrayList<User>();
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/

        etPassword = findViewById(R.id.etPassword);
        btnTogglePass = findViewById(R.id.btnTogglePass);
        Button btnSignUp = findViewById(R.id.btnSignup);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnExit = findViewById(R.id.btnExit);
        etUser = findViewById(R.id.etUser);
        Button btnAudio = findViewById(R.id.bttnAudio);

        btnTogglePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realizarSignup();

            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
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
                if(btnAudio.getText().equals(R.string.mute_audio)) {
                    btnAudio.setText(R.string.unmute_audio);
                    audioPlayer(true);
                } else if(btnAudio.getText().equals(R.string.unmute_audio)) {
                    btnAudio.setText(R.string.mute_audio);
                    audioPlayer(false);
                }
            }
        });
        audioPlayer(true);
    }
    private void realizarSignup() {
        String username = etUser.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        Log.v(username, password);

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos",
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
                Log.d("LOGIN_RESPONSE", "Código: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    String nombreUsuario = response.body();

                    Toast.makeText(SignupActivity.this,
                            "Bienvenido " + nombreUsuario,
                            Toast.LENGTH_SHORT).show();

                    guardarUsuarioEnPrefs(nombreUsuario);

                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                    intent.putExtra("usuario", nombreUsuario);
                    startActivity(intent);
                    finish();

                } else if (response.code() == 401) {
                    Toast.makeText(SignupActivity.this,
                            "Usuario o contraseña incorrectos",
                            Toast.LENGTH_SHORT).show();
                } else if (response.code() == 400) {
                    Toast.makeText(SignupActivity.this,
                            "Datos inválidos",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignupActivity.this,
                            "Error en el servidor: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("RetrofitError", "Error de conexión: ", t);
                Toast.makeText(SignupActivity.this,
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

    private void audioPlayer(boolean isPlaying){
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.background_music);

        if(isPlaying) {
            mediaPlayer.start();
        } else {
            mediaPlayer.pause();
        }
    }
}