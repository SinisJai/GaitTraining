package com.example.msway;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.msway.utils.DataManager;

public class MusicSelectionActivity extends AppCompatActivity {
    private ListView lvMusicGenres;
    private Button btnBack;
    private TextView tvSelectedMusic;
    private DataManager dataManager;

    private String[] musicGenres = {
            "Classical",
            "Jazz",
            "Pop",
            "Rock",
            "Electronic",
            "Country",
            "Ambient",
            "Hip Hop"
    };

    private String selectedGenre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_music_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainMusicSelectionLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeComponents();
    }

    private void initializeComponents() {
        lvMusicGenres = findViewById(R.id.lvMusicGenres);
        btnBack = findViewById(R.id.btnBack);
        tvSelectedMusic = findViewById(R.id.tvSelectedMusic);

        dataManager = new DataManager(getApplicationContext());

        // Load currently selected genre if any
        selectedGenre = dataManager.getSelectedMusicGenre();
        if (selectedGenre != null && !selectedGenre.isEmpty()) {
            tvSelectedMusic.setText(getString(R.string.currently_selected_genre, selectedGenre));
        } else {
            tvSelectedMusic.setText(R.string.no_music_selected);
        }

        // Set up adapter for music genres list
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                musicGenres
        );
        lvMusicGenres.setAdapter(adapter);

        lvMusicGenres.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedGenre = musicGenres[position];
                dataManager.saveSelectedMusicGenre(selectedGenre);
                tvSelectedMusic.setText(getString(R.string.currently_selected_genre, selectedGenre));
                Toast.makeText(MusicSelectionActivity.this,
                        getString(R.string.music_genre_selected, selectedGenre),
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
