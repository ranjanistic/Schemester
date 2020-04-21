package org.timetable.schemester;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.timetable.schemester.student.AdditionalLoginInfo;

import java.util.Objects;

public class Splash extends AppCompatActivity {
    ApplicationSchemester schemester;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        schemester = (ApplicationSchemester) this.getApplication();
        super.onCreate(savedInstanceState);
        if (user != null) {
            if (!user.isEmailVerified()) {
                startActivity(new Intent(this, PositionActivity.class));
                finish();
            } else if (Objects.equals(getAdditionalInfo()[0], null)
                    && Objects.equals(getAdditionalInfo()[1], null)
                    && Objects.equals(getAdditionalInfo()[2], null)) {
                startActivity(new Intent(this, AdditionalLoginInfo.class));
                finish();
            } else {
                schemester.setCollegeCourseYear(getAdditionalInfo()[0], getAdditionalInfo()[1], getAdditionalInfo()[2]);
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        } else {
            startActivity(new Intent(this, PositionActivity.class));
            finish();
        }
    }

    private String[] getAdditionalInfo() {
        String[] CCY = new String[3];
        SharedPreferences mSharedPreferences = this.getSharedPreferences(schemester.getPREF_HEAD_ADDITIONAL_INFO(), MODE_PRIVATE);
        CCY[0] = mSharedPreferences.getString(schemester.getPREF_KEY_COLLEGE(), null);
        CCY[1] = mSharedPreferences.getString(schemester.getPREF_KEY_COURSE(), null);
        CCY[2] = mSharedPreferences.getString(schemester.getPREF_KEY_YEAR(), null);
        return CCY;
    }
}
