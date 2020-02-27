package org.timetable.schemester;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;

public class Splash extends AppCompatActivity {
    ApplicationSchemester schemester;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        schemester = (ApplicationSchemester) this.getApplication();
        super.onCreate(savedInstanceState);
        if(user!=null) {
            if(!(Objects.equals(getAdditionalInfo()[0],"")&&Objects.equals(getAdditionalInfo()[1],"")&&Objects.equals(getAdditionalInfo()[2],""))) {
                schemester.setCollegeCourseYear(getAdditionalInfo()[0], getAdditionalInfo()[1], getAdditionalInfo()[2]);
                isHolidayOtherThanWeekend(getAdditionalInfo()[0], getAdditionalInfo()[1]);
                isHolidayOtherThanWeekend(getAdditionalInfo()[0], schemester.getDOCUMENT_LOCAL_INFO());
                isHolidayOtherThanWeekend(schemester.getCOLLECTION_GLOBAL_INFO(), schemester.getDOCUMENT_HOLIDAY_INFO());
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, PositionActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            Intent intent = new Intent(this, PositionActivity.class);
            startActivity(intent);
            finish();
        }
    }
    private String[] getAdditionalInfo() {
        String[] CCY = new String[3];
        SharedPreferences mSharedPreferences = this.getSharedPreferences(schemester.getPREF_HEAD_ADDITIONAL_INFO(), MODE_PRIVATE);
        CCY[0] = mSharedPreferences.getString(schemester.getPREF_KEY_COLLEGE(), "");
        CCY[1] = mSharedPreferences.getString(schemester.getPREF_KEY_COURSE(), "");
        CCY[2] = mSharedPreferences.getString(schemester.getPREF_KEY_YEAR(), "");
        return CCY;
    }
    private void isHolidayOtherThanWeekend(String collector, String doc){
        db.collection(collector).document(doc).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (Objects.requireNonNull(document).exists()) {
                                saveHolidayStatus(document.getBoolean(schemester.getFIELD_HOLIDAY()));
                            }
                        }
                    }
                });
    }
    private void saveHolidayStatus(Boolean isHoliday){
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_OTHER_HOLIDAY(), MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean(schemester.getPREF_KEY_OTHER_HOLIDAY(), isHoliday);
        mEditor.apply();
    }
}
