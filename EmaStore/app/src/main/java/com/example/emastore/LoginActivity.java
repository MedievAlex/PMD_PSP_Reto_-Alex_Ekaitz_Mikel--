package com.example.emastore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import model.User;

public class LoginActivity extends AppCompatActivity {
    private ArrayList<User> usuarios = new ArrayList<User>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login);
        usuarios.add(new User("admin", "1234"));
        usuarios.add(new User("usuario1", "password1"));
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
    }



    public void login() {
        TextView usuario = findViewById(R.id.etUser);
        TextView password = findViewById(R.id.etPassword);
        String user = usuario.getText().toString();
        String pass = password.getText().toString();

        for (User u : usuarios) {
            if (u.getUsername().equals(user) && u.getPassword().equals(pass)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return;
            }
        }

        Toast.makeText(this, "Login incorrecto", Toast.LENGTH_SHORT).show();
    }
}