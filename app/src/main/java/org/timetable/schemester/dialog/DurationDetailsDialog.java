package org.timetable.schemester.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialog;

import org.timetable.schemester.listener.DurationDetailDialogListener;
import org.timetable.schemester.R;

import java.util.Objects;

public class DurationDetailsDialog extends AppCompatDialog {
    private DurationDetailDialogListener durationDetailDialogListener;
    public DurationDetailsDialog(Context context, DurationDetailDialogListener durationDetailDialogListener){
        super(context);
        this.durationDetailDialogListener = durationDetailDialogListener;
    }
    private TextView subject, duration,location,classOn;
    private Button dismiss;
    private ImageView dialogImage;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.duration_detail_dialog);
        setCanceledOnTouchOutside(true);
        Objects.requireNonNull(this.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogImage = findViewById(R.id.durationDialogImage);
         subject = findViewById(R.id.className);
        duration = findViewById(R.id.classDuration);
         location = findViewById(R.id.locationOfClass);
         classOn = findViewById(R.id.classOnStatus);
         dismiss = findViewById(R.id.dialog_dismiss);

        subject.setText(durationDetailDialogListener.onCallClassName());
        duration.setText(durationDetailDialogListener.onCallClassDuration());
        location.setText(durationDetailDialogListener.classLocation());
        if(Objects.equals(durationDetailDialogListener.classIsOn(),true)){
            classOn.setText(R.string.yes);
            classOn.setBackgroundResource(R.drawable.roundfillboxgreen);
        } else if(Objects.equals(durationDetailDialogListener.classIsOn(),false)){
            classOn.setText(R.string.no);
            classOn.setBackgroundResource(R.drawable.roundfillboxred);
        } else if(Objects.equals(durationDetailDialogListener.classIsOn(),null)) {
            classOn.setText(R.string.n_a);
            classOn.setBackgroundResource(R.drawable.roundfillboxneutral);
        }
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
