package org.timetable.schemester.dialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialog;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.timetable.schemester.ApplicationSchemester;
import org.timetable.schemester.R;
import org.timetable.schemester.listener.DurationDetailDialogListener;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
//TODO: ApplicationClass isn't working out
public class DurationDetailsDialog extends AppCompatDialog {
    private DurationDetailDialogListener durationDetailDialogListener;
    public DurationDetailsDialog(Context context, DurationDetailDialogListener durationDetailDialogListener){
        super(context);
        this.durationDetailDialogListener = durationDetailDialogListener;
    }
    private TextView subject, duration,location,classOn;
    private Button dismiss;
    private ImageView dialogImage;
    private ImageButton classStatBtn;
    ApplicationSchemester schemester;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        schemester = (ApplicationSchemester)this.getContext();
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
        classStatBtn = findViewById(R.id.editClassStatus);
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
        dismiss.setOnClickListener(view -> dismiss());
        if(isClassRepresentative()){
            dismiss.setVisibility(View.VISIBLE);
            dismiss.setOnClickListener(view -> {

            });
        }else{
            dismiss.setVisibility(GONE);
        }
    }
    private Boolean isClassRepresentative(){
        Boolean[] b = new Boolean[1];
        db.collection(schemester.getCOLLECTION_COLLEGE_CODE())
                .document(schemester.getDOCUMENT_COURSE_CODE())
                .collection(schemester.getCOLLECTION_YEAR_CODE())
                .document(schemester.getDOCUMENT_YEAR_AUTHORITY())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (Objects.requireNonNull(document).exists()) {
                            String code = document.getString(schemester.getFIELD_CR_CODE());
                            b[0] = Objects.equals(code,getLocalCrCode());
                            storeCRStatus(b[0],b[0]?getLocalCrCode():null);
                        }
                    }
                });
        return b[0];
    }
    private void storeCRStatus(Boolean isCR, String code){
        getContext().getSharedPreferences(schemester.getPREF_HEAD_USER_DEF(), MODE_PRIVATE).edit()
                .putBoolean(schemester.getPREF_KEY_STUDENT_CR(), isCR)
                .putString(schemester.getPREF_KEY_CR_CODE(), code)
                .apply();
    }
    private String getLocalCrCode(){
        return getContext().getSharedPreferences(schemester.getPREF_HEAD_USER_DEF(), MODE_PRIVATE)
                .getString(schemester.getPREF_KEY_CR_CODE(), null);
    }
    private String[] getAdditionalInfo() {
        String[] CCY = new String[3];
        SharedPreferences mSharedPreferences = getContext().getSharedPreferences(
                schemester.getPREF_HEAD_ADDITIONAL_INFO(), MODE_PRIVATE);
        CCY[0] = mSharedPreferences.getString(schemester.getPREF_KEY_COLLEGE(), null);
        CCY[1] = mSharedPreferences.getString(schemester.getPREF_KEY_COURSE(), null);
        CCY[2] = mSharedPreferences.getString(schemester.getPREF_KEY_YEAR(), null);
        return CCY;
    }
}
