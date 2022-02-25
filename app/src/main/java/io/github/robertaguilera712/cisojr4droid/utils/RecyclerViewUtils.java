package io.github.robertaguilera712.cisojr4droid.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;

import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.github.robertaguilera712.cisojr4droid.adapters.MyAdapter;
import io.github.robertaguilera712.cisojr4droid.R;
import io.github.robertaguilera712.cisojr4droid.model.Rom;
import io.github.robertaguilera712.cisojr4droid.dialogs.EditRomDialog;

public class RecyclerViewUtils {
    private final ArrayList<Rom> roms;
    private final RecyclerView recyclerView;
    private final boolean isDecompression;
    private final RecyclerView.LayoutManager layoutManager;
    private final SharedPreferences sharedPreferences;
    private final Context context;
    private final MyAdapter adapter;
    private final FragmentManager fragmentManager;
    private final Handler handler;
    private final Callback.Actions actions;

    public RecyclerViewUtils(RecyclerView recyclerView, boolean isDecompression, RecyclerView.LayoutManager layoutManager, SharedPreferences sharedPreferences, Context context, FragmentManager fragmentManager, Handler handler, Callback.Actions actions) {
        roms = new ArrayList<>();
        this.adapter = new MyAdapter(roms, isDecompression, context, this::onItemClick);
        this.isDecompression = isDecompression;
        this.layoutManager = layoutManager;
        this.recyclerView = recyclerView;
        this.sharedPreferences = sharedPreferences;
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.handler = handler;
        this.actions = actions;
        init();
    }

    private void init() {
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void onItemClick(int position) {
        Rom clickedRom = roms.get(position);
        if (clickedRom.getStatus().equals(context.getString(R.string.status_added))) {
            EditRomDialog dialog = new EditRomDialog(clickedRom, isDecompression, () -> adapter.notifyItemChanged(position));
            dialog.show(fragmentManager, "editRom");
        }
    }

    public void loadRoms(ArrayList<Rom> roms) {
        clear();
        this.roms.addAll(roms);
        adapter.notifyItemRangeInserted(0, roms.size());
    }

    public void reloadRomsSettings() {
        int compressionLevel = sharedPreferences.getInt(context.getString(R.string.saved_compression_level_key), 1);
        boolean deleteAfter = sharedPreferences.getBoolean(context.getString(R.string.saved_delete_after_key), false);
        for (Rom rom : roms) {
            rom.setCompressionLevel(compressionLevel);
            rom.setDelete(deleteAfter);
            adapter.notifyItemChanged(roms.indexOf(rom));
        }
    }

    private void clear() {
        final int size = roms.size();
        roms.clear();
        adapter.notifyItemRangeRemoved(0, size);
    }

    public void queue(Rom rom) {
        rom.setStatus(context.getString(R.string.status_queue));
        adapter.notifyItemChanged(roms.indexOf(rom));
    }

    public void updateProgress(Rom rom) {
        String status = String.format(context.getString(R.string.status_converting), rom.getProgress());
        rom.setStatus(status);
        adapter.notifyItemChanged(roms.indexOf(rom));
    }

    public void finish(Rom rom) {
        if (rom.isDelete()) {
            deleteInputFile(rom.getInputUri());
        }
        rom.setProgress(100);
        rom.setStatus(context.getString(R.string.status_conversion_finished));
        adapter.notifyItemChanged(roms.indexOf(rom));

        if (roms.indexOf(rom) == roms.size() - 1) {
            handler.post(() -> actions.setEnable(true));
        }
    }

    private void deleteInputFile(Uri inputUri) {
        DocumentFile inputFile = DocumentFile.fromSingleUri(context, inputUri);
        if (inputFile != null) {
            inputFile.delete();
        }
    }

    public boolean areRomsToCompress() {
        int count = 0;
        for (Rom rom : roms) {
            if (rom.getProgress() == 0) {
                count++;
            }
        }
        return count > 0;
    }

    public ArrayList<Rom> getRoms() {
        return roms;
    }
}
