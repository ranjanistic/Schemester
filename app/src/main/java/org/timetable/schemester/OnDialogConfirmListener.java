package org.timetable.schemester;

import android.graphics.drawable.Drawable;

public interface OnDialogConfirmListener {
    void onApply(Boolean confirm);
    String onCallText();
    String onCallSub();
}
