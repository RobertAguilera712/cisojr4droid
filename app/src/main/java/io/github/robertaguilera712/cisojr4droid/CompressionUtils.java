package io.github.robertaguilera712.cisojr4droid;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompressionUtils {
    private final SharedPreferences sharedPreferences;
    private final ArrayList<CompressionTask> compressionTasks;
    private final ArrayList<DecompressionTask> decompressionTasks;
    private final RecyclerViewUtils recyclerViewUtils;
    private final Context context;
    private final Callback.Actions actions;
    private final Handler handler;
    private ExecutorService executor;

    public CompressionUtils(SharedPreferences sharedPreferences, RecyclerViewUtils recyclerViewUtils, Context context, Handler handler, Callback.Actions actions) {
        this.sharedPreferences = sharedPreferences;
        this.compressionTasks = new ArrayList<>();
        this.decompressionTasks = new ArrayList<>();
        this.recyclerViewUtils = recyclerViewUtils;
        this.context = context;
        this.handler = handler;
        this.actions = actions;
        initExecutor();
    }

    public void initExecutor() {
        int simultaneousTasks = sharedPreferences.getInt(context.getString(R.string.saved_simultaneous_tasks_key), 1);
        if (simultaneousTasks == 1) {
            executor = Executors.newSingleThreadExecutor();
        } else {
            executor = Executors.newFixedThreadPool(simultaneousTasks);
        }
    }

    public void batchCompression() {
        if (!recyclerViewUtils.areRomsToCompress()) {
            Toast.makeText(context, R.string.no_files_warning, Toast.LENGTH_SHORT).show();
            return;
        }
        actions.setEnable(false);
        setCompressionTasks();
        Task.setHandler(handler);
        for (CompressionTask task : compressionTasks) {
            executor.execute(task);
        }
    }

    public void batchDecompression() {
        if (!recyclerViewUtils.areRomsToCompress()) {
            Toast.makeText(context, R.string.no_files_warning, Toast.LENGTH_SHORT).show();
            return;
        }
        actions.setEnable(false);
        setDecompressionTasks();
        Task.setHandler(handler);
        for (DecompressionTask task : decompressionTasks) {
            executor.execute(task);
        }
    }

    private void setCompressionTasks() {
        compressionTasks.clear();
        for (Rom rom : recyclerViewUtils.getRoms()) {
            recyclerViewUtils.queue(rom);
            compressionTasks.add(new CompressionTask(rom, recyclerViewUtils::updateProgress, recyclerViewUtils::finish));
        }
    }

    private void setDecompressionTasks() {
        decompressionTasks.clear();
        for (Rom rom : recyclerViewUtils.getRoms()) {
            recyclerViewUtils.queue(rom);
            decompressionTasks.add(new DecompressionTask(rom, recyclerViewUtils::updateProgress, recyclerViewUtils::finish));
        }
    }
}
