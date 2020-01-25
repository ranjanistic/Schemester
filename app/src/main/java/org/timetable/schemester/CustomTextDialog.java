package org.timetable.schemester;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDialog;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class CustomTextDialog extends AppCompatDialog {
    private OnDialogTextListener onDialogTextListener;
    public CustomTextDialog(Context context, OnDialogTextListener onDialogTextListener){
        super(context);
        this.onDialogTextListener = onDialogTextListener;
    }
    public boolean isValid;
    private TextView validity;
    int textCode = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_text_dialog);
        Objects.requireNonNull(this.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCanceledOnTouchOutside(false);
        final TextView textDiaHead = findViewById(R.id.text_dialog_head);
        final Button textCancel = findViewById(R.id.text_cancel);
        final Button textSubmit = findViewById(R.id.text_submit);
        validity = findViewById(R.id.ValidityText);
        assert textDiaHead != null;
        assert textSubmit != null;
        assert textCancel != null;
        String head = onDialogTextListener.onCallText();
        textDiaHead.setText(head);
         textCode = onDialogTextListener.textType();
        final EditText text = findViewById(R.id.dialog_text);
        text.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                checkTextValidity(text.getText().toString().trim(),s, textCode);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
        
        textCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });

        textSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String entryTxt = text.getText().toString();
                if ( TextUtils.isEmpty(entryTxt)){
                    Toast.makeText(getContext(),"This field can\'t be empty.",Toast.LENGTH_LONG).show();
                } else{
                    onDialogTextListener.onApply(entryTxt);
                    dismiss();
                }
            }
        });
    }
    String pattern;
    private void checkTextValidity(String textUnderInspection, Editable s, int textType) {
        if (textType == 7011) {      //roll
            pattern = "[0-9]+/[0-9]+";
        } else  if (textType == 38411) {      //email
            pattern = "[a-zA-Z0-9._-]+@[a-z.]+\\.+[a-z]+";
        }
        if (pattern != null) {
            if (textUnderInspection.matches(pattern) && s.length() > 0) {
                validity.setVisibility(View.VISIBLE);
                validity.setText(getContext().getResources().getString(R.string.valid));
                validity.setTextColor(getContext().getResources().getColor(R.color.white));
                validity.setBackgroundResource(R.drawable.roundcontainerboxgreen);
                isValid = true;
            } else if (s.length() == 0) {
                validity.setVisibility(View.GONE);
                isValid = false;
            } else {
                validity.setVisibility(View.VISIBLE);
                validity.setText(getContext().getResources().getString(R.string.invalidtext));
                validity.setTextColor(getContext().getResources().getColor(R.color.white));
                validity.setBackgroundResource(R.drawable.roundcontainerboxred);
                isValid = false;
            }
        }
    }
    }
