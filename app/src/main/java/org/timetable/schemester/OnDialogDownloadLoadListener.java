package org.timetable.schemester;

public interface OnDialogDownloadLoadListener {
    String getLink();
    String getVersion();
    void afterFinish(Boolean isCompleted);
}
