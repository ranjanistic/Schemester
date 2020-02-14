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
import java.util.Set;

public class AdditionalLoginInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additional_login_info);
        Button done = findViewById(R.id.additionalConfirmed);
        Button relog = findViewById(R.id.reloginBtn);
        Spinner collegeSpin = findViewById(R.id.spinnerCollege);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.college_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        collegeSpin.setAdapter(adapter);

        Spinner courseSpin = findViewById(R.id.spinnerCourse);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.course_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpin.setAdapter(adapter2);

        relog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutCurrentUser();
                Intent i = new Intent(AdditionalLoginInfo.this, PositionActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.enter_from_left, R.anim.exit_from_right);
                finish();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAdditionalInfo("DBC", "PHY-H", "Y2");
                Toast.makeText(getApplicationContext(), "Welcome", Toast.LENGTH_LONG).show();
                Intent i = new Intent(AdditionalLoginInfo.this, MainActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.enter_from_bottom, R.anim.exit_from_top);
            }
        });

    }
    private void logoutCurrentUser(){

            FirebaseAuth.getInstance().signOut();
            Toast.makeText(AdditionalLoginInfo.this, "Logged out", Toast.LENGTH_LONG).show();
    }

    private void saveAdditionalInfo(String college, String course, String year){
        SharedPreferences mSharedPreferences = getSharedPreferences("additionalInfo", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString("college", college);
        mEditor.putString("course", course);
        mEditor.putString("year", year);
        mEditor.apply();
        }
}

