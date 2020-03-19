package org.timetable.schemester.listener;

public interface VerificationDrawerStatusListener {
    void emailChange(Boolean change);
    void sendLinkAgain(Boolean send);
    Boolean checkVerification(Boolean check);
}
