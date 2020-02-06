package org.timetable.schemester;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PositionActivity extends AppCompatActivity {
    Button teacher, student;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAppTheme(getThemeStatus());
        setContentView(R.layout.activity_position);

        teacher = findViewById(R.id.teacherbtn);
        student = findViewById(R.id.studentbtn);
        final Intent login = new Intent(PositionActivity.this, LoginActivity.class);
        teacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeUserPosition("teacher");
                Toast.makeText(PositionActivity.this, "Not available yet", Toast.LENGTH_LONG).show();
                //startActivity(login);
                //finish();
            }
        });
        student.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeUserPosition("student");
                startActivity(login);
                finish();
            }
        });
    }

    private void storeUserPosition(String pos){
        SharedPreferences mSharedPreferences = getSharedPreferences("userDefinition", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString("position", pos);
        mEditor.apply();
    }

    public void setAppTheme(int code) {
        switch (code) {
            case 101:
                setTheme(R.style.BlueLightTheme);
                break;
            case 102:
                setTheme(R.style.BlueDarkTheme);
                break;
            default:setTheme(R.style.BlueLightTheme);
        }
    }
    private int getThemeStatus() {
        SharedPreferences mSharedPreferences = this.getSharedPreferences("schemeTheme", MODE_PRIVATE);
        return mSharedPreferences.getInt("themeCode", 0);
    }
}
