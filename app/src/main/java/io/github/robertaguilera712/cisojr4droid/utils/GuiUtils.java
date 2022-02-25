package io.github.robertaguilera712.cisojr4droid.utils;

import android.graphics.drawable.Drawable;
import android.view.MenuItem;

public class GuiUtils {
    public static void enableMenuItem(MenuItem item) {
        item.setEnabled(true);
        Drawable itemIcon = item.getIcon();
        if (itemIcon != null) {
            itemIcon.setAlpha(255);
        }
    }

    public static void disableMenuItem(MenuItem item) {
        item.setEnabled(false);
        Drawable itemIcon = item.getIcon();
        if (itemIcon != null) {
            itemIcon.setAlpha(128);
        }
    }
}
