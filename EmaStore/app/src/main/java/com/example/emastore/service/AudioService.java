package com.example.emastore.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.example.emastore.R;

public class AudioService extends Service {

    public static final String ACTION_PLAY =
            "com.example.emastore.action.PLAY";
    public static final String ACTION_PAUSE =
            "com.example.emastore.action.PAUSE";
    public static final String ACTION_TOGGLE =
            "com.example.emastore.action.TOGGLE";

    private MediaPlayer mediaPlayer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);

        if (mediaPlayer == null) {
            stopSelf();
            return;
        }

        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(0.5f, 0.5f);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null || intent.getAction() == null || mediaPlayer == null) {
            return START_NOT_STICKY;
        }

        String action = intent.getAction();

        switch (action) {
            case ACTION_PLAY:
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
                break;

            case ACTION_PAUSE:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                break;

            case ACTION_TOGGLE:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                }
                break;
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
