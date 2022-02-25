package io.github.robertaguilera712.cisojr4droid;

import android.os.Handler;

public class Task {

    protected static Handler handler;

    protected final Rom rom;
    protected final Callback.UpdateProgress updateProgressCallback;
    protected final Callback.Finish finishCallback;

    public Task(Rom rom, Callback.UpdateProgress updateProgressCallback, Callback.Finish finishCallback) {
        this.rom = rom;
        this.updateProgressCallback = updateProgressCallback;
        this.finishCallback = finishCallback;
    }

    public static void setHandler(Handler handler) {
        Task.handler = handler;
    }
}
