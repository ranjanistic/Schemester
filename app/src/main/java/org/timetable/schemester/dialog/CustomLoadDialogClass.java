package org.timetable.schemester.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialog;

import org.timetable.schemester.R;
import org.timetable.schemester.listener.OnDialogLoadListener;

import java.util.Objects;

public class CustomLoadDialogClass extends AppCompatDialog {
    private OnDialogLoadListener onDialogLoadListener;

    public CustomLoadDialogClass(Context context, OnDialogLoadListener onDialogLoadListener) {
        super(context);
        this.onDialogLoadListener = onDialogLoadListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_load_dialog);
        this.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(this.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView loadtext = findViewById(R.id.loadingcaption);
        onDialogLoadListener.onLoad();
        String caption = onDialogLoadListener.onLoadText();
        assert loadtext != null;
        loadtext.setText(caption);
    }
}
