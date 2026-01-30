package com.example.emastore;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

import model.User;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login);
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
    }

    private ArrayList<User> usuarios = new ArrayList<User>();

    public void login (){
        TextView usertext = findViewById(R.id.etUser);
        TextView userPass = findViewById(R.id.etPassword);
        User u = new User(usertext.getText().toString(), userPass.getText().toString());
        for (int i=0; i< usuarios.size(); i++){
           /* if(usuarios[i].username == u.getUsername() && usuarios[i].password == u.getPassword() ){
                setContentView(R.layout.activity_main);
            }*/
        }
    }
}