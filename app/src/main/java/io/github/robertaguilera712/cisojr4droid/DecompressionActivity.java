package io.github.robertaguilera712.cisojr4droid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import io.github.robertaguilera712.cisojr4droid.databinding.ActivityDecompressionBinding;

public class DecompressionActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private ActivityDecompressionBinding binding;
    private RecyclerViewUtils recyclerViewUtils;
    private FileUtils fileUtils;
    private CompressionUtils compressionUtils;
    private boolean buttonsEnabled = true;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDecompressionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar2);
        setRomOptions();
        Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        recyclerViewUtils = new RecyclerViewUtils(binding.rvRoms, true, new LinearLayoutManager(this), sharedPreferences, this, getSupportFragmentManager(), mainThreadHandler, this::setActionsEnable);
        fileUtils = new FileUtils(getActivityResultRegistry(), getContentResolver(), sharedPreferences, this, recyclerViewUtils);
        getLifecycle().addObserver(fileUtils);
        compressionUtils = new CompressionUtils(sharedPreferences, recyclerViewUtils, this, mainThreadHandler, this::setActionsEnable);
        binding.swipeRefreshLayout.setOnRefreshListener(this);
    }

    private void setRomOptions() {
        Rom.setResolver(getContentResolver());
        Rom.setOutputMimeType(getString(R.string.iso_mime_type));
        Rom.setInputMimeType(getString(R.string.cso_mime_type));
        Rom.setOutputFileExtension(getString(R.string.iso_extension));
        Rom.setInputFileExtension(getString(R.string.cso_extension));
    }

    private void setActionsEnable(boolean enable) {
        buttonsEnabled = enable;
        invalidateOptionsMenu();
        binding.swipeRefreshLayout.setEnabled(enable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compression_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (buttonsEnabled) {
            for (int i = 0, n = menu.size(); i < n; i++) {
                GuiUtils.enableMenuItem(menu.getItem(i));
            }
        } else {
            for (int i = 0, n = menu.size(); i < n; i++) {
                GuiUtils.disableMenuItem(menu.getItem(i));
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_openRoms) {
            fileUtils.selectRomsFolder();
            return true;
        } else if (itemId == R.id.menu_compress) {
            compressionUtils.batchDecompression();
            return true;
        } else if (itemId == R.id.menu_settings) {
            SettingsDialog dialog = new SettingsDialog(sharedPreferences, true, this::applySettings);
            dialog.show(getSupportFragmentManager(), "settings");
            return true;
        } else if (itemId == R.id.menu_refresh) {
            refresh();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void refresh() {
        binding.swipeRefreshLayout.setRefreshing(true);
        recyclerViewUtils.loadRoms(fileUtils.listRoms());
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    private void applySettings() {
        recyclerViewUtils.reloadRomsSettings();
        compressionUtils.initExecutor();
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    @Override
    public void onBackPressed() {
        if (buttonsEnabled) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, R.string.going_back_warning, Toast.LENGTH_LONG).show();
        }
    }
}