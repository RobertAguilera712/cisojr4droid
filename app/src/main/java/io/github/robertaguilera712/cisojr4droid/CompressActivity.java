package io.github.robertaguilera712.cisojr4droid;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.robertaguilera712.cisojr4droid.databinding.ActivityCompressBinding;

public class CompressActivity extends AppCompatActivity {

    private ActivityCompressBinding binding;
    private Uri inputUri, outputUri;
    private String outFilenamePlaceholder = "out.cso";
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
    private final DecimalFormat formatter = new DecimalFormat("###.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCompressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.compression_levels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spCompressionLevel.setAdapter(adapter);
        binding.tvInputFilename.setText(String.format(getString(R.string.input_filename), ""));
        binding.tvOutputFilename.setText(String.format(getString(R.string.output_filename), ""));
        binding.btnPickInputFile.setText(String.format(getString(R.string.select_input_filename), "iso"));
        binding.btnPickInputFile.setOnClickListener(v -> getInputFilename());
        binding.btnPickOutputFile.setOnClickListener(v -> getOutputFilename());
        binding.btnCompress.setOnClickListener(v -> compress());
    }

    private void getInputFilename() {
        resetProgress();
        Intent data = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        data.setType("application/x-iso9660-image");
        String title = String.format(getString(R.string.select_input_filename), "iso");
        data = Intent.createChooser(data, title);
        requestInput.launch(data);
    }

    private void getOutputFilename() {
        resetProgress();
        Intent data = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        data.setType("application/octet-stream");
        data.putExtra(Intent.EXTRA_TITLE, outFilenamePlaceholder);
        data = Intent.createChooser(data, getString(R.string.select_save_location));
        requestOutput.launch(data);
    }

    private void resetProgress(){
        binding.tvProgress.setText("");
        binding.progressBar.setProgress(0);
    }

    private boolean filesSelected() {
        if (inputUri == null) {
            Toast.makeText(this, R.string.select_input_warning, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (outputUri == null) {
            Toast.makeText(this, R.string.select_save_warning, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void compress() {
        if (!filesSelected()) return;

        final ContentResolver resolver = getContentResolver();
        try {
            final ParcelFileDescriptor pfdIn = resolver.openFileDescriptor(inputUri, "r");
            final ParcelFileDescriptor pfdOut = resolver.openFileDescriptor(outputUri, "rw");
            final FileInputStream in = new FileInputStream(pfdIn.getFileDescriptor());
            final FileOutputStream out = new FileOutputStream(pfdOut.getFileDescriptor());
            int compressionLevel = Integer.parseInt(binding.spCompressionLevel.getSelectedItem().toString());
            Ciso ciso = new Ciso(executor, mainThreadHandler);
            ciso.compressIso(in, out, compressionLevel, this::updateProgress,
                    () -> onFinish(pfdIn, pfdOut));
            disableButtons();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateProgress(final double progress) {
        final String stringProgress = formatter.format(progress);
        final String progressText = String.format(getString(R.string.compressing), stringProgress);
        final int intProgress = (int) progress;
        binding.tvProgress.setText(progressText);
        binding.progressBar.setProgress(intProgress);
    }

    private void onFinish(final ParcelFileDescriptor pfdIn, final ParcelFileDescriptor pfdOut) {
        String filename = getFilename(inputUri);
        binding.tvProgress.setText(String.format(getString(R.string.compression_completed), filename));
        binding.progressBar.setProgress(100);
        reset();
        try {
            pfdIn.close();
            pfdOut.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void disableButtons() {
        binding.btnPickInputFile.setEnabled(false);
        binding.btnPickOutputFile.setEnabled(false);
        binding.btnCompress.setEnabled(false);
        binding.spCompressionLevel.setEnabled(false);
    }

    private void reset() {
        binding.btnPickInputFile.setEnabled(true);
        binding.btnPickOutputFile.setEnabled(true);
        binding.btnCompress.setEnabled(true);
        binding.spCompressionLevel.setEnabled(true);
        inputUri = null;
        outputUri = null;
        binding.tvInputFilename.setText(String.format(getString(R.string.input_filename), ""));
        binding.tvOutputFilename.setText(String.format(getString(R.string.output_filename), ""));
    }

    ActivityResultLauncher<Intent> requestOutput = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            outputUri = data.getData();
                            String text = String.format(getString(R.string.output_filename), getFilename(outputUri));
                            binding.tvOutputFilename.setText(text);
                        }
                    }
                }
            }
    );

    ActivityResultLauncher<Intent> requestInput = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            inputUri = data.getData();
                            String inputFilename = getFilename(inputUri);
                            outFilenamePlaceholder = inputFilename.replaceFirst(".iso$", ".cso");
                            String text = String.format(getString(R.string.input_filename), inputFilename);
                            binding.tvInputFilename.setText(text);
                        }
                    }
                }
            }
    );

    private String getFilename(Uri uri) {
        try (Cursor returnCursor = getContentResolver().query(uri, null, null, null, null)) {
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            return returnCursor.getString(nameIndex);
        }
    }

}