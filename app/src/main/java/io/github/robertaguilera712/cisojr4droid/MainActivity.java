package io.github.robertaguilera712.cisojr4droid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import io.github.robertaguilera712.cisojr4droid.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnIsoToCso.setOnClickListener(v -> goToCompressScreen());
        binding.btnCsoToIso.setOnClickListener(v -> goToDecompressScreen());
    }

    private void goToCompressScreen(){
        Intent intent = new Intent(this, CompressActivity.class);
        startActivity(intent);
    }

    private void goToDecompressScreen(){
        Intent intent = new Intent(this, DecompressActivity.class);
        startActivity(intent);
    }
}