package io.github.robertaguilera712.cisojr4droid.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import io.github.robertaguilera712.cisojr4droid.utils.Callback;
import io.github.robertaguilera712.cisojr4droid.R;
import io.github.robertaguilera712.cisojr4droid.databinding.DialogSettingsBinding;

public class SettingsDialog extends DialogFragment {


    private DialogSettingsBinding binding;
    private final SharedPreferences sharedPreferences;
    private final boolean isDecompression;
    private final Callback.OnSaveSettings saveCallback;

    public SettingsDialog(SharedPreferences sharedPreferences, boolean isDecompression, Callback.OnSaveSettings saveCallback) {
        this.sharedPreferences = sharedPreferences;
        this.isDecompression = isDecompression;
        this.saveCallback = saveCallback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogSettingsBinding.inflate(LayoutInflater.from(getContext()));
        setCurrentSettings();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(binding.getRoot())
                .setTitle(R.string.settings)
                .setPositiveButton(R.string.dialog_save_button, (DialogInterface dialog, int id) -> saveSettings())
                .setNegativeButton(R.string.dialog_cancel_button, (DialogInterface dialog, int id) ->  {});
        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setCurrentSettings() {
        int compressionLevel = sharedPreferences.getInt(getString(R.string.saved_compression_level_key), 1);
        boolean deleteAfter = sharedPreferences.getBoolean(getString(R.string.saved_delete_after_key), false);
        int simultaneousTasks = sharedPreferences.getInt(getString(R.string.saved_simultaneous_tasks_key), 1);
        fillSimultaneousTaskSpinner();
        binding.swDeleteAfter.setChecked(deleteAfter);
        binding.spCompressionLevel.setSelection(compressionLevel - 1);
        binding.spSimultaneousTasks.setSelection(simultaneousTasks - 1);
        if (isDecompression){
            binding.lbCompressionLevel.setVisibility(View.GONE);
            binding.spCompressionLevel.setVisibility(View.GONE);
        }
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int newCompressionLevel = Integer.parseInt(binding.spCompressionLevel.getSelectedItem().toString());
        boolean newDeleteAfter = binding.swDeleteAfter.isChecked();
        int newSimultaneousTasks = Integer.parseInt(binding.spSimultaneousTasks.getSelectedItem().toString());
        editor.putInt(getString(R.string.saved_compression_level_key), newCompressionLevel);
        editor.putBoolean(getString(R.string.saved_delete_after_key), newDeleteAfter);
        editor.putInt(getString(R.string.saved_simultaneous_tasks_key), newSimultaneousTasks);
        editor.apply();
        saveCallback.save();
    }

    private void fillSimultaneousTaskSpinner() {
        int cpuCount = Runtime.getRuntime().availableProcessors();
        Integer[] simultaneousValues = new Integer[cpuCount];

        for (int i = 0; i < cpuCount; i++) {
            simultaneousValues[i] = i + 1;
        }

        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, simultaneousValues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spSimultaneousTasks.setAdapter(adapter);
    }
}
