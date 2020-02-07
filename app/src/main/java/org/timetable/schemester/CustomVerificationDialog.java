package org.timetable.schemester;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDialog;

import java.util.Objects;

public class CustomVerificationDialog extends AppCompatDialog {
    OnDialogApplyListener onDialogApplyListener;
    CustomVerificationDialog(Context context, OnDialogApplyListener onDialogApplyListener){
        super(context);
        this.onDialogApplyListener = onDialogApplyListener;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_verification_dialog);
        Objects.requireNonNull(this.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final EditText etmail = findViewById(R.id.cred_email);
        final EditText etpass = findViewById(R.id.cred_pass);
        Button cancel = findViewById(R.id.cred_no);
        Button submit = findViewById(R.id.cred_yes);
        assert cancel != null;
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
        assert submit != null;
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email,pass;
                    email = etmail.getText().toString();
                    pass = etpass.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getContext(), "Email ID is required.", Toast.LENGTH_LONG).show();
     //               return;
                }
                else if (TextUtils.isEmpty(pass)) {
                    Toast.makeText(getContext(), "Date of birth is necessary.", Toast.LENGTH_LONG).show();
//                    return;
                }
                else{
                    onDialogApplyListener.onApply(email,pass);
                    dismiss();
                }
            }
        });
    }
}
