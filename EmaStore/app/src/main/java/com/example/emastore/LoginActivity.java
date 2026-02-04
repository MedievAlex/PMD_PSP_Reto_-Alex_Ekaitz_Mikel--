package com.example.emastore;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import model.User;

public class LoginActivity extends AppCompatActivity {
    private final ArrayList<User> usuarios = new ArrayList<User>();
    private boolean isPasswordVisible = false;
    private ImageButton btnTogglePass;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        usuarios.add(new User("admin", "1234"));
        usuarios.add(new User("usuario1", "password1"));

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnSignUp = findViewById(R.id.btnSignup);
        Button btnExit = findViewById(R.id.btnExit);
        Button btnAudio = findViewById(R.id.bttnAudio);
        btnTogglePass = findViewById(R.id.btnTogglePass);
        etPassword = findViewById(R.id.etPassword);

        btnTogglePass.setImageResource(R.drawable.ic_visibility_off);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    EditText etUser = findViewById(R.id.etUser);
                    String user = etUser.getText().toString();
                    String pass = etPassword.getText().toString();

                    for (User u : usuarios) {
                        if (u.getUsername().equals(user) && u.getPassword().equals(pass)) {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }
                    Toast.makeText(LoginActivity.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    etUser.setText("");
                    etPassword.setText("");
                    etUser.requestFocus();
                } catch (Exception e) {
                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
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
                if(btnAudio.getText().equals(getString(R.string.mute_audio))) {
                    btnAudio.setText(R.string.unmute_audio);
                } else if(btnAudio.getText().equals(getString(R.string.unmute_audio))) {
                    btnAudio.setText(R.string.mute_audio);
                }
            }
        });
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
}