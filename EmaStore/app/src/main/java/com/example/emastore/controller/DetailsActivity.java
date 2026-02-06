package com.example.emastore.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.emastore.R;
import com.example.emastore.controller.MediaAdapter;
import com.example.emastore.model.APK;
import com.example.emastore.model.MediaItem;
import com.example.emastore.service.ApiService;
import com.example.emastore.service.AudioService;

import java.util.ArrayList;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {

    // Constantes
    private static final String PREFS_NAME = "UsuarioPrefs";
    private static final String KEY_NOMBRE_USUARIO = "nombreUsuario";
    private static final String KEY_ESTA_LOGUEADO = "estaLogueado";
    private static final String KEY_AUDIO_ENABLED = "audioEnabled";

    // Views
    private TextView textTitulo, textAutor, textDescripcion, textUsuario;
    private ImageView imageAPK;
    private APK apkActual;
    private String usuarioActual;
    private boolean isAudioPlaying = true;

    // Carrusel simple
    private ViewPager2 viewPager;
    private LinearLayout dotsContainer;
    private MediaAdapter adapter;
    private List<MediaItem> mediaList = new ArrayList<>();
    private Handler autoPlayHandler = new Handler();
    private Runnable autoPlayTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (verificarSesionExistente()) return;

        setContentView(R.layout.activity_details);
        inicializarVistas();
        configurarBotones();
        cargarDatosDesdeIntent();
        configurarCarruselSimple();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isAudioPlaying = prefs.getBoolean(KEY_AUDIO_ENABLED, true);
    }

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

    private void inicializarVistas() {
        textTitulo = findViewById(R.id.textTitulo);
        textAutor = findViewById(R.id.textAutor);
        textDescripcion = findViewById(R.id.textDescripcion);
        textUsuario = findViewById(R.id.textView3);
        imageAPK = findViewById(R.id.imageAPK);
        viewPager = findViewById(R.id.viewPager);
        dotsContainer = findViewById(R.id.dotsContainer);
    }

    // ========== CARRUSEL ==========
    private void configurarCarruselSimple() {
        mediaList.add(new MediaItem(MediaItem.TYPE_VIDEO, R.raw.video_demo));
        mediaList.add(new MediaItem(MediaItem.TYPE_IMAGE, R.raw.foto1));
        mediaList.add(new MediaItem(MediaItem.TYPE_IMAGE, R.raw.foto2));
        mediaList.add(new MediaItem(MediaItem.TYPE_IMAGE, R.raw.foto3));
        mediaList.add(new MediaItem(MediaItem.TYPE_IMAGE, R.raw.foto4));

        adapter = new MediaAdapter(mediaList);
        viewPager.setAdapter(adapter);

        crearPuntosSimples();
        iniciarAutoPlay();
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                actualizarPuntos(position);
                pausarVideoAnterior();
                reiniciarAutoPlay();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    pausarVideoActual();
                }
            }
        });
    }
    private void pausarVideoAnterior() {
        RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
        if (recyclerView != null) {
            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
                if (holder instanceof MediaAdapter.ViewHolder) {
                    ((MediaAdapter.ViewHolder) holder).pausarVideo();
                }
            }
        }
    }
    private void pausarVideoActual() {
        RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
        if (recyclerView != null) {
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(viewPager.getCurrentItem());
            if (holder instanceof MediaAdapter.ViewHolder) {
                ((MediaAdapter.ViewHolder) holder).pausarVideo();
            }
        }
    }
    private void crearPuntosSimples() {
        dotsContainer.removeAllViews();
        for (int i = 0; i < mediaList.size(); i++) {
            ImageView dot = new ImageView(this);
            dot.setImageResource(R.drawable.dot_unselected);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(12, 12);
            params.setMargins(4, 0, 4, 0);
            dot.setLayoutParams(params);
            dotsContainer.addView(dot);
        }
        actualizarPuntos(0);
    }
    private void actualizarPuntos(int position) {
        for (int i = 0; i < dotsContainer.getChildCount(); i++) {
            ImageView dot = (ImageView) dotsContainer.getChildAt(i);
            dot.setImageResource(i == position ? R.drawable.dot_selected : R.drawable.dot_unselected);
        }
    }
    private void iniciarAutoPlay() {
        autoPlayTask = new Runnable() {
            @Override
            public void run() {
                int current = viewPager.getCurrentItem();
                int next = current + 1;
                if (next >= mediaList.size()) next = 0;

                pausarVideoActual();

                viewPager.setCurrentItem(next, true);
                autoPlayHandler.postDelayed(this, 10000);
            }
        };
        autoPlayHandler.postDelayed(autoPlayTask, 10000);
    }
    private void reiniciarAutoPlay() {
        autoPlayHandler.removeCallbacks(autoPlayTask);
        autoPlayHandler.postDelayed(autoPlayTask, 4000);
    }
    private void detenerAutoPlay() {
        if (autoPlayHandler != null && autoPlayTask != null) {
            autoPlayHandler.removeCallbacks(autoPlayTask);
        }
    }

    // ========== BOTONES ==========
    private void configurarBotones() {
        Button btnBack = findViewById(R.id.btnBack);
        Button btnExit = findViewById(R.id.btnExit);
        Button btnAudio = findViewById(R.id.bttnAudio);
        Button btnDownload = findViewById(R.id.btnDownload);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isAudioPlaying = prefs.getBoolean(KEY_AUDIO_ENABLED, true);

        btnAudio.setText(isAudioPlaying ? R.string.mute_audio : R.string.unmute_audio);
        btnAudio.setOnClickListener(v -> toggleAudio(btnAudio));

        btnBack.setOnClickListener(v -> volverAMain());
        btnExit.setOnClickListener(v -> salirAplicacion());
        btnDownload.setOnClickListener(v -> descargarAPK());
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

    private void cargarDatosDesdeIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            String titulo = intent.getStringExtra("titulo");
            String autor = intent.getStringExtra("autor");
            String descripcion = intent.getStringExtra("descripcion");
            String image = intent.getStringExtra("image");

            apkActual = new APK(titulo, autor, descripcion, image);

            textTitulo.setText(apkActual.getTitulo());
            textAutor.setText(apkActual.getAutor());
            textDescripcion.setText(apkActual.getDescripcion());

            cargarUsuarioActual();

            if (apkActual.getImageBitmap() != null) {
                imageAPK.setImageBitmap(apkActual.getImageBitmap());
            }
        }
    }

    private void cargarUsuarioActual() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        usuarioActual = prefs.getString(KEY_NOMBRE_USUARIO, "Invitado");
        textUsuario.setText("Usuario: " + usuarioActual);
    }

    private void descargarAPK() {
        if (apkActual == null) return;
        String downloadUrl = ApiService.BASE_URL + "download/" + apkActual.getTitulo();
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)));
        Toast.makeText(this, "Descargando " + apkActual.getTitulo(), Toast.LENGTH_SHORT).show();
    }

    private void volverAMain() {
        pausarAudioService();
        startActivity(new Intent(this, MainActivity.class));
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

    @Override
    protected void onResume() {
        super.onResume();
        if (isAudioPlaying) iniciarAudioService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausarVideoActual();
        detenerAutoPlay();
        if (isAudioPlaying) pausarAudioService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pausarVideoAnterior();
        detenerAutoPlay();
    }
}