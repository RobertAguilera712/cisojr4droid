package io.github.robertaguilera712.cisojr4droid;

public class Callback {

    public interface UpdateProgress {
        void update(final Rom rom);
    }

    public interface Finish {
        void finish(final Rom rom);
    }

    public interface OnSaveSettings {
        void save();
    }

    public interface OnItemClick {
        void onItemClick(int position);
    }

    public interface OnRomUpdate{
        void onRomUpdate();
    }

    public interface Actions{
        void setEnable(boolean enable);
    }
}