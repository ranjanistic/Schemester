package org.timetable.schemester.listener;

import android.graphics.drawable.Drawable;

public interface OnDialogTextListener {
    void onApply(String text);
    String onCallText();
    int textType();
}
