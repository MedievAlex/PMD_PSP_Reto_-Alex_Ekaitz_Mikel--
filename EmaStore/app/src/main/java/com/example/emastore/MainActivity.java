package com.example.emastore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MenuAdapter adapter;
    private List<MenuItem> menuItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        setupButtons();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.setHasFixedSize(true);
    }

    private void setupRecyclerView() {
        menuItems = new ArrayList<>();

        menuItems.add(new MenuItem(1, "OPCIÓN 1", R.drawable.ic_launcher_background));
        menuItems.add(new MenuItem(2, "OPCIÓN 2", R.drawable.ic_launcher_foreground));
        menuItems.add(new MenuItem(3, "OPCIÓN 3", R.drawable.ic_launcher_background));
        menuItems.add(new MenuItem(4, "OPCIÓN 4", R.drawable.ic_launcher_foreground));
        menuItems.add(new MenuItem(5, "OPCIÓN 5", R.drawable.ic_launcher_background));
        menuItems.add(new MenuItem(6, "OPCIÓN 6", R.drawable.ic_launcher_foreground));
        menuItems.add(new MenuItem(7, "OPCIÓN 7", R.drawable.ic_launcher_background));


        adapter = new MenuAdapter(menuItems, new MenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MenuItem item) {
                Toast.makeText(MainActivity.this,
                        "Clicked: " + item.getTitle(),
                        Toast.LENGTH_SHORT).show();

                switch (item.getId()) {
                    case 1:
                        // startActivity(new Intent(MainActivity.this, Opcion1Activity.class));
                        break;
                    case 2:
                        // startActivity(new Intent(MainActivity.this, Opcion2Activity.class));
                        break;
                    // ... etc
                }
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void setupButtons() {
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnExit = findViewById(R.id.btnExit);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
                System.exit(0);
            }
        });
    }
}