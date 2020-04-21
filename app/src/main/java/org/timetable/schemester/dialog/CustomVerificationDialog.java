package org.timetable.schemester.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDialog;

import org.timetable.schemester.R;
import org.timetable.schemester.listener.OnDialogApplyListener;

import java.util.Objects;

public class CustomVerificationDialog extends AppCompatDialog {
    private OnDialogApplyListener onDialogApplyListener;

    public CustomVerificationDialog(Context context, OnDialogApplyListener onDialogApplyListener) {
        super(context);
        this.onDialogApplyListener = onDialogApplyListener;
    }

    private EditText etmail, etpass;
    private Button cancel, submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_verification_dialog);
        Objects.requireNonNull(this.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        etmail = findViewById(R.id.cred_email);
        etpass = findViewById(R.id.cred_pass);
        etmail.requestFocus();
        cancel = findViewById(R.id.cred_no);
        submit = findViewById(R.id.cred_yes);
        cancel.setOnClickListener(view -> cancel());
        submit.setOnClickListener(view -> {
            String email, pass;
            email = etmail.getText().toString();
            pass = etpass.getText().toString();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getContext(), "Email ID is required.", Toast.LENGTH_LONG).show();
            } else if (TextUtils.isEmpty(pass)) {
                Toast.makeText(getContext(), "Date of birth is necessary.", Toast.LENGTH_LONG).show();
            } else {
                onDialogApplyListener.onApply(email, pass);
                dismiss();
            }
        });
    }
}
