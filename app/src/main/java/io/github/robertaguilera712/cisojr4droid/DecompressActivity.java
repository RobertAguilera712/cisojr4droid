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
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.DataFormatException;

import io.github.robertaguilera712.cisojr4droid.databinding.ActivityDecompressBinding;

public class DecompressActivity extends AppCompatActivity {

    private ActivityDecompressBinding binding;
    private Uri inputUri, outputUri;
    private String outFilenamePlaceholder = "out.iso";
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
    private final DecimalFormat formatter = new DecimalFormat("###.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDecompressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvInputFilenameD.setText(String.format(getString(R.string.input_filename), ""));
        binding.tvOutputFilenameD.setText(String.format(getString(R.string.output_filename), ""));
        binding.btnPickInputFileD.setText(String.format(getString(R.string.select_input_filename), "cso"));
        binding.btnPickInputFileD.setOnClickListener(v -> getInputFilename());
        binding.btnPickOutputFileD.setOnClickListener(v -> getOutputFilename());
        binding.btnDecompress.setOnClickListener(v -> decompress());
    }

    private void getInputFilename() {
        resetProgress();
        Intent data = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        data.setType("application/octet-stream");
        String title = String.format(getString(R.string.select_input_filename), "cso");
        data = Intent.createChooser(data, title);
        requestInput.launch(data);
    }

    private void getOutputFilename() {
        resetProgress();
        Intent data = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        data.setType("application/x-iso9660-image");
        data.putExtra(Intent.EXTRA_TITLE, outFilenamePlaceholder);
        data = Intent.createChooser(data, getString(R.string.select_save_location));
        requestOutput.launch(data);
    }

    private void resetProgress(){
        binding.tvProgressD.setText("");
        binding.progressBarD.setProgress(0);
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

    private void decompress() {
        if (!filesSelected()) return;

        final ContentResolver resolver = getContentResolver();
        try {
            final ParcelFileDescriptor pfdIn = resolver.openFileDescriptor(inputUri, "r");
            final ParcelFileDescriptor pfdOut = resolver.openFileDescriptor(outputUri, "rw");
            final FileInputStream in = new FileInputStream(pfdIn.getFileDescriptor());
            final FileOutputStream out = new FileOutputStream(pfdOut.getFileDescriptor());
            Ciso ciso = new Ciso(executor, mainThreadHandler);
            ciso.decompressCso(in, out, this::updateProgress, () -> onFinish(pfdIn, pfdOut));
            disableButtons();
        } catch (IOException | DataFormatException e) {
            e.printStackTrace();
        }
    }

    private void updateProgress(final double progress) {
        final String stringProgress = formatter.format(progress);
        final String progressText = String.format(getString(R.string.decompressing), stringProgress);
        final int intProgress = (int) progress;
        binding.tvProgressD.setText(progressText);
        binding.progressBarD.setProgress(intProgress);
    }

    private void onFinish(final ParcelFileDescriptor pfdIn, final ParcelFileDescriptor pfdOut) {
        String filename = getFilename(inputUri);
        binding.tvProgressD.setText(String.format(getString(R.string.decompression_completed), filename));
        binding.progressBarD.setProgress(100);
        reset();
        try {
            pfdIn.close();
            pfdOut.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void disableButtons() {
        binding.btnPickInputFileD.setEnabled(false);
        binding.btnPickOutputFileD.setEnabled(false);
        binding.btnDecompress.setEnabled(false);
    }

    private void reset() {
        binding.btnPickInputFileD.setEnabled(true);
        binding.btnPickOutputFileD.setEnabled(true);
        binding.btnDecompress.setEnabled(true);
        inputUri = null;
        outputUri = null;
        binding.tvInputFilenameD.setText(String.format(getString(R.string.input_filename), ""));
        binding.tvOutputFilenameD.setText(String.format(getString(R.string.output_filename), ""));
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
                            binding.tvOutputFilenameD.setText(text);
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
                            outFilenamePlaceholder = inputFilename.replaceFirst(".cso$", ".iso");
                            String text = String.format(getString(R.string.input_filename), inputFilename);
                            binding.tvInputFilenameD.setText(text);
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