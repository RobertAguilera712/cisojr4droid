package io.github.robertaguilera712.cisojr4droid.core;

import java.io.IOException;

import io.github.robertaguilera712.cisojr4droid.model.Rom;
import io.github.robertaguilera712.cisojr4droid.utils.Callback;

public class CompressionTask extends Task implements Runnable {

    public CompressionTask(Rom rom, Callback.UpdateProgress updateProgressCallback, Callback.Finish finishCallback) {
        super(rom, updateProgressCallback, finishCallback);
    }

    @Override
    public void run() {
        try {
            Ciso.compressIso(rom, updateProgressCallback, finishCallback, handler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        handler.post(() -> finishCallback.finish(rom));
    }
}
