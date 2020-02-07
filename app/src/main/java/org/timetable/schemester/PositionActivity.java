package org.timetable.schemester;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PositionActivity extends AppCompatActivity {
    Button teacher, student;
    FirebaseUser user;
    ImageButton modeSwitch;
    Window window;
    Animation hide, show, fadeon, fadeoff;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAppTheme(getThemeStatus());
        window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_position);
        hide = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.gone_centrally);
        show = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.emerge_centrally);
        fadeon = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadeliton);
        fadeoff= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadelitoff);
        modeSwitch = findViewById(R.id.modeSwitchBtnInitial);
        if(getThemeStatus() == 102){
            modeSwitch.setImageResource(R.drawable.ic_moonsmallicon);
        } else {
            modeSwitch.setImageResource(R.drawable.ic_suniconsmall);
            storeThemeStatus(101);
        }
        final Intent restart = new Intent(PositionActivity.this,PositionActivity.class);
        modeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modeSwitch.startAnimation(hide);
                modeSwitch.startAnimation(fadeoff);
                if(getThemeStatus() == 101){
                    modeSwitch.setImageResource(R.drawable.ic_moonsmallicon);
                    storeThemeStatus(102);
                    startActivity(restart);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    modeSwitch.startAnimation(show);
                    modeSwitch.startAnimation(fadeon);
                } else if(getThemeStatus() == 102){
                    modeSwitch.setImageResource(R.drawable.ic_suniconsmall);
                    storeThemeStatus(101);
                    startActivity(restart);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    modeSwitch.startAnimation(show);
                    modeSwitch.startAnimation(fadeon);
                } else {
                    modeSwitch.setImageResource(R.drawable.ic_moonsmallicon);
                    storeThemeStatus(102);
                    startActivity(restart);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    modeSwitch.startAnimation(show);
                    modeSwitch.startAnimation(fadeon);
                }
            }
        });
        teacher = findViewById(R.id.teacherbtn);
        student = findViewById(R.id.studentbtn);

        final Intent login = new Intent(PositionActivity.this, LoginActivity.class);
        teacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeUserPosition("teacher");
                Toast.makeText(PositionActivity.this, "Not available yet", Toast.LENGTH_LONG).show();
            }
        });
        student.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeUserPosition("student");
                startActivity(login);
            }
        });
    }

    protected void onResume(){
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            finish();
        }
        super.onResume();
    }
    private void storeUserPosition(String pos){
        SharedPreferences mSharedPreferences = getSharedPreferences("userDefinition", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString("position", pos);
        mEditor.apply();
    }

    private void storeThemeStatus(int themechoice){
        SharedPreferences mSharedPreferences = getSharedPreferences("schemeTheme", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putInt("themeCode", themechoice);
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
