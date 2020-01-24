package org.timetable.schemester;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
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
        TextInputLayout texthint = findViewById(R.id.dialog_text_hint);
        assert textDiaHead != null;
        assert textSubmit != null;
        assert textCancel != null;
        String head = onDialogTextListener.onCallText();
        final EditText text = findViewById(R.id.dialog_text);
        textDiaHead.setText(head);


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
}
