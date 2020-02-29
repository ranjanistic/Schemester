package org.timetable.schemester.listener;

public interface OnDialogDownloadLoadListener {
    String getLink();
    String getVersion();
    void afterFinish(Boolean isCompleted);
}
