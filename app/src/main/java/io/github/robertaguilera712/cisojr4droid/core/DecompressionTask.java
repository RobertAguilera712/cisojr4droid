package io.github.robertaguilera712.cisojr4droid.core;

import java.io.IOException;
import java.util.zip.DataFormatException;

import io.github.robertaguilera712.cisojr4droid.model.Rom;
import io.github.robertaguilera712.cisojr4droid.utils.Callback;

public class DecompressionTask extends Task implements Runnable {

    public DecompressionTask(Rom rom, Callback.UpdateProgress updateProgressCallback, Callback.Finish finishCallback) {
        super(rom, updateProgressCallback, finishCallback);
    }

    @Override
    public void run() {
        try {
            Ciso.decompressCso(rom, updateProgressCallback, finishCallback, handler);
        } catch (IOException | DataFormatException e) {
            e.printStackTrace();
        }
        handler.post(() -> finishCallback.finish(rom));
    }
}
