package io.github.robertaguilera712.cisojr4droid.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import io.github.robertaguilera712.cisojr4droid.utils.Callback;
import io.github.robertaguilera712.cisojr4droid.R;
import io.github.robertaguilera712.cisojr4droid.model.Rom;
import io.github.robertaguilera712.cisojr4droid.databinding.DialogEditRomBinding;

public class EditRomDialog extends DialogFragment {
    private DialogEditRomBinding binding;
    private final Rom romToUpdate;
    private final boolean isDecompression;
    private final Callback.OnRomUpdate onRomUpdate;

    public EditRomDialog(Rom romToUpdate, boolean isDecompression, Callback.OnRomUpdate onRomUpdate) {
        this.romToUpdate = romToUpdate;
        this.isDecompression = isDecompression;
        this.onRomUpdate = onRomUpdate;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogEditRomBinding.inflate(LayoutInflater.from(getContext()));
        setCurrentValues();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(binding.getRoot())
                .setTitle(romToUpdate.getInputFilename())
                .setPositiveButton(R.string.dialog_save_button, (DialogInterface dialog, int id) -> updateRom())
                .setNegativeButton(R.string.dialog_cancel_button, (DialogInterface dialog, int id) -> {});
        return builder.create();
    }

    private void updateRom(){
        int compressionLevel = Integer.parseInt(binding.spCompressionLevel.getSelectedItem().toString());
        boolean deleteAfter = binding.swDeleteAfter.isChecked();
        String outputFilename = binding.txtOutputFilename.getText().append(Rom.getOutputFileExtension()).toString();
        romToUpdate.setCompressionLevel(compressionLevel);
        romToUpdate.setDelete(deleteAfter);
        romToUpdate.setOutputFilename(outputFilename);
        onRomUpdate.onRomUpdate();
    }

    private void setCurrentValues(){
        int compressionLevel = romToUpdate.getCompressionLevel();
        boolean deleteAfter = romToUpdate.isDelete();
        binding.spCompressionLevel.setSelection(compressionLevel - 1);
        binding.swDeleteAfter.setChecked(deleteAfter);
        binding.txtOutputFilename.setText(romToUpdate.getOutputBaseFilename());
        binding.txtOutExtension.setText(Rom.getOutputFileExtension());
        if (isDecompression){
            binding.lbCompressionLevel.setVisibility(View.GONE);
            binding.spCompressionLevel.setVisibility(View.GONE);
        }
    }

}
