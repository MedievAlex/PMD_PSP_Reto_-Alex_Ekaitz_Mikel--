package com.example.emastore;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class DetailsActivity extends AppCompatActivity {
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details);

        Button btnExit = findViewById(R.id.btnExit);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnAudio = findViewById(R.id.bttnAudio);
        videoView = findViewById(R.id.videoView);


        try {

            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.video_demo;

            Uri videoUri = Uri.parse(videoPath);
            videoView.setVideoURI(videoUri);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    videoView.start();
                    mp.setLooping(true);
                }
            });
            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Toast.makeText(DetailsActivity.this,
                            "Error al cargar el video. ¿Está en res/raw/?",
                            Toast.LENGTH_LONG).show();
                    return true;
                }
            });

        } catch (Exception e) {
            Toast.makeText(this,
                    "Error: " + e.getMessage() +
                            "\nAsegúrate de crear la carpeta res/raw/ y poner un video .mp4",
                    Toast.LENGTH_LONG).show();
        }

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DetailsActivity.this, MainActivity.class));
            }
        });

        btnAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnAudio.getText().equals(getString(R.string.mute_audio))) {
                    btnAudio.setText(R.string.unmute_audio);
                    audioPlayer(true);
                } else if(btnAudio.getText().equals(getString(R.string.unmute_audio))) {
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
            mediaPlayer.setLooping(true);
        } else {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null) {
            videoView.start();
        }
    }
}