package org.timetable.schemester.listener;

import android.graphics.drawable.Drawable;

public interface OnDialogConfirmListener {
    void onApply(Boolean confirm);
    String onCallText();
    String onCallSub();
}
