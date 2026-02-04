package com.example.emastore;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details);
        Button btnExit = findViewById(R.id.btnExit);
        //Button btnLogout = findViewById(R.id.btnLogout);
        Button btnBack = findViewById(R.id.btnBack);
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
        Button btnAudio = findViewById(R.id.bttnAudio);

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
       /* btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DetailsActivity.this, LoginActivity.class));
            }
        });*/
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DetailsActivity.this, MainActivity.class));
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
    private void audioPlayer(boolean isPlaying){
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.background_music);

        if(isPlaying) {
            mediaPlayer.start();
        } else {
            mediaPlayer.pause();
        }
    }
}