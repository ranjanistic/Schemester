package org.timetable.schemester;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

        collegeCode = getResources().getStringArray(R.array.college_code_array);
        courseCode = getResources().getStringArray(R.array.course_code_array);
        yearCode = getResources().getStringArray(R.array.year_code_array);
        collegeArray = getResources().getStringArray(R.array.college_array);
        courseArray =  getResources().getStringArray(R.array.course_array);
        yearArray =  getResources().getStringArray(R.array.year_array);

        ArrayAdapter<CharSequence> colAdapter = ArrayAdapter.createFromResource(this,
                R.array.college_array, R.layout.custom_spinner_item);
        colAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        collegeSpin.setAdapter(colAdapter);

        ArrayAdapter<CharSequence> couAdapter = ArrayAdapter.createFromResource(this,
                R.array.course_array, R.layout.custom_spinner_item);
        couAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpin.setAdapter(couAdapter);

        ArrayAdapter<CharSequence> yeaAdapter = ArrayAdapter.createFromResource(this,
                R.array.year_array, R.layout.custom_spinner_item);
        yeaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpin.setAdapter(yeaAdapter);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        setDefaults(collegeSpin, collegeCode);
        setDefaults(courseSpin, courseCode);
        setDefaults(yearSpin, yearCode);
    }
    private void setDefaults(Spinner s, String[] strings){
        ArrayAdapter col = (ArrayAdapter) s.getAdapter(); //cast to an ArrayAdapter
        int o = 0;
        while(o<strings.length){
            if(Objects.equals(getAdditionalInfo()[0],strings[o])){
                s.setSelection(col.getPosition(o));
                break;
            } else{
                o++;
            }
        }

    }
    private String[] getAdditionalInfo() {
        String[] CCY = {null, null, null};
        SharedPreferences mSharedPreferences = getSharedPreferences("additionalInfo", MODE_PRIVATE);
        CCY[0] = mSharedPreferences.getString("college", "");
        CCY[1] = mSharedPreferences.getString("course", "");
        CCY[2] = mSharedPreferences.getString("year", "");
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
                schemester.toasterShort("Allowed");
                schemester.setCollegeCourseYear(colC, couC, yC);
                saveAdditionalInfo(colC, couC, yC);
                //Toast.makeText(getApplicationContext(), "Welcome", Toast.LENGTH_LONG).show();
                Intent i = new Intent(AdditionalLoginInfo.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                overridePendingTransition(R.anim.enter_from_bottom, R.anim.exit_from_top);
            }
        });
    }

    private void saveAdditionalInfo(String college, String course, String year){
        SharedPreferences mSharedPreferences = getSharedPreferences("additionalInfo", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString("college", college);
        mEditor.putString("course", course);
        mEditor.putString("year", year);
        mEditor.apply();
        }

    public void setAppTheme() {
        SharedPreferences mSharedPreferences = this.getSharedPreferences("schemeTheme", MODE_PRIVATE);
        switch (mSharedPreferences.getInt("themeCode", 0)) {
            case ApplicationSchemester.CODE_THEME_INCOGNITO: setTheme(R.style.IncognitoTheme); break;
            case ApplicationSchemester.CODE_THEME_DARK: setTheme(R.style.BlueDarkTheme);break;
            case ApplicationSchemester.CODE_THEME_LIGHT:
            default:setTheme(R.style.BlueLightTheme);
        }
    }
}

