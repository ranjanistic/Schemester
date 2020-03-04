package org.timetable.schemester.student;


import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import org.timetable.schemester.ApplicationSchemester;
import org.timetable.schemester.MainActivity;
import org.timetable.schemester.R;

import java.util.ArrayList;
import java.util.Objects;
@TargetApi(Build.VERSION_CODES.Q)
public class AdditionalLoginInfo extends AppCompatActivity {
    ApplicationSchemester schemester;
    Spinner collegeSpin,courseSpin,yearSpin;
    String[] collegeCode, courseCode, yearCode,collegeArray, courseArray,yearArray;
    Button done;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        schemester = (ApplicationSchemester) this.getApplication();
        setAppTheme();
        setContentView(R.layout.activity_additional_login_info);
        done = findViewById(R.id.additionalConfirmed);
        Button back = findViewById(R.id.reloginBtn);
        collegeSpin = findViewById(R.id.spinnerCollege);
        courseSpin = findViewById(R.id.spinnerCourse);
        yearSpin = findViewById(R.id.spinnerYear);
        ImageView displayImage;
        displayImage = findViewById(R.id.imageOnlogin);
        if(getThemeStatus() == ApplicationSchemester.CODE_THEME_DARK){
            displayImage.setImageResource(R.drawable.ic_moonsmallicon);
        } else {
            displayImage.setImageResource(R.drawable.ic_suniconsmall);
        }
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.rotate_clock_faster);
        displayImage.startAnimation(animation);
        collegeCode = getResources().getStringArray(R.array.college_code_array);
        courseCode = getResources().getStringArray(R.array.course_code_array);
        yearCode = getResources().getStringArray(R.array.year_code_array);
        collegeArray = getResources().getStringArray(R.array.college_array);
        courseArray =  getResources().getStringArray(R.array.course_array);
        yearArray =  getResources().getStringArray(R.array.year_array);

        ArrayAdapter<CharSequence> colAdapter = ArrayAdapter.createFromResource(this,
                R.array.college_array, R.layout.custom_spinner_item);

        colAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        collegeSpin.setAdapter(colAdapter);

        ArrayAdapter<CharSequence> couAdapter = ArrayAdapter.createFromResource(this,
                R.array.course_array, R.layout.custom_spinner_item);
        couAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        courseSpin.setAdapter(couAdapter);

        ArrayAdapter<CharSequence> yeaAdapter = ArrayAdapter.createFromResource(this,
                R.array.year_array, R.layout.custom_spinner_item);
        yeaAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        yearSpin.setAdapter(yeaAdapter);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        String[][] ccyArray = new String[][]{collegeArray, courseArray, yearArray},
                ccyCodeArray = new String[][]{collegeCode, courseCode, yearCode};
        int j = 0;
        while(j<3) {
            int i = 0;
            while (i < ccyArray[j].length) {
                if (Objects.equals(getAdditionalInfo()[j], ccyCodeArray[j][i])) {
                    collegeSpin.setSelection(colAdapter.getPosition(ccyArray[j][i]), true);
                    break;
                } else ++i;
            }
            ++j;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private String[] getAdditionalInfo() {
        String[] CCY = new String[3];
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_ADDITIONAL_INFO(), MODE_PRIVATE);
        CCY[0] = mSharedPreferences.getString(schemester.getPREF_KEY_COLLEGE(), null);
        CCY[1] = mSharedPreferences.getString(schemester.getPREF_KEY_COURSE(), null);
        CCY[2] = mSharedPreferences.getString(schemester.getPREF_KEY_YEAR(), null);
        return CCY;
    }
    String colC, couC, yC;
    @Override
    protected void onStart() {
        super.onStart();

        collegeSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Object o = adapterView.getItemAtPosition(i);
                if(Objects.equals(o.toString(),collegeArray[i])){
                    colC = collegeCode[i];
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        courseSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Object o = adapterView.getItemAtPosition(i);
                if(Objects.equals(o.toString(),courseArray[i])){
                    couC = courseCode[i];
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        yearSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Object o = adapterView.getItemAtPosition(i);
                if(Objects.equals(o.toString(),yearArray[i])){
                    yC = yearCode[i];
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                schemester.toasterShort(schemester.getStringResource(R.string.welcome_back));
                schemester.setCollegeCourseYear(colC, couC, yC);
                saveAdditionalInfo(colC, couC, yC);
                Intent i = new Intent(AdditionalLoginInfo.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                overridePendingTransition(R.anim.enter_from_bottom, R.anim.exit_from_top);
            }
        });
    }

    private void saveAdditionalInfo(String college, String course, String year){
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_ADDITIONAL_INFO(), MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString(schemester.getPREF_KEY_COLLEGE(), college);
        mEditor.putString(schemester.getPREF_KEY_COURSE(), course);
        mEditor.putString(schemester.getPREF_KEY_YEAR(), year);
        mEditor.apply();
    }

    public void setAppTheme() {
        switch (getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE)
        .getInt(schemester.getPREF_KEY_THEME(), 0)) {
            case ApplicationSchemester.CODE_THEME_INCOGNITO: setTheme(R.style.IncognitoTheme); break;
            case ApplicationSchemester.CODE_THEME_DARK: setTheme(R.style.BlueDarkTheme);break;
            case ApplicationSchemester.CODE_THEME_LIGHT: default:setTheme(R.style.BlueLightTheme);
        }
    }
    private int getThemeStatus(){
        return getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE)
                .getInt(schemester.getPREF_KEY_THEME(), 0);
    }
}

