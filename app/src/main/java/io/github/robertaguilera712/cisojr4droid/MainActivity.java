package io.github.robertaguilera712.cisojr4droid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import io.github.robertaguilera712.cisojr4droid.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnGoCompression.setOnClickListener(v -> {
            Intent intent = new Intent(this, CompressionActivity.class);
            startActivity(intent);
        });
        binding.btnGoDecompression.setOnClickListener(v -> {
            Intent intent = new Intent(this, DecompressionActivity.class);
            startActivity(intent);
        });
    }
}