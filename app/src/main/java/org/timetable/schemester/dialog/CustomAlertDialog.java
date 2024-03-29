package org.timetable.schemester.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialog;

import org.timetable.schemester.R;
import org.timetable.schemester.listener.OnDialogAlertListener;

import java.util.Objects;

public class CustomAlertDialog extends AppCompatDialog {
    private OnDialogAlertListener onDialogAlertListener;

    public CustomAlertDialog(Context context, OnDialogAlertListener onDialogAlertListener) {
        super(context);
        this.onDialogAlertListener = onDialogAlertListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCanceledOnTouchOutside(false);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_alert_dialog);
        Objects.requireNonNull(this.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final TextView alcont = findViewById(R.id.alert_content);
        final TextView alsubcont = findViewById(R.id.alert_subcontent);
        final Button dismiss = findViewById(R.id.alert_dismiss_btn);

        String head = onDialogAlertListener.onCallText();
        String subhead = onDialogAlertListener.onCallSub();

        assert alcont != null;
        alcont.setText(head);
        assert alsubcont != null;
        alsubcont.setText(subhead);

        assert dismiss != null;
        dismiss.setOnClickListener(view -> {
            onDialogAlertListener.onDismiss();
            dismiss();
        });
    }

    @Override
    public void onBackPressed() {
        setCancelable(false);
        super.onBackPressed();
    }
}