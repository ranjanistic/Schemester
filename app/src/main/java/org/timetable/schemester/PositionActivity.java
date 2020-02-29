package org.timetable.schemester;

import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PositionActivity extends AppCompatActivity {
    ApplicationSchemester schemester;
    Button teacher, student;
    FirebaseUser user;
    ImageButton modeSwitch;
    Window window;
    Animation hide, show, fadeon, fadeoff;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        schemester = (ApplicationSchemester) this.getApplication(); 
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
        schemester.imageButtonLongPressToast(modeSwitch,"Touch to renovate");
        if(getThemeStatus() == ApplicationSchemester.CODE_THEME_DARK){
            modeSwitch.setImageResource(R.drawable.ic_moonsmallicon);
        } else {
            modeSwitch.setImageResource(R.drawable.ic_suniconsmall);
            storeThemeStatus(ApplicationSchemester.CODE_THEME_LIGHT);
        }
        final Intent restart = new Intent(PositionActivity.this,PositionActivity.class);
        modeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modeSwitch.startAnimation(hide);
                modeSwitch.startAnimation(fadeoff);
                if(getThemeStatus() == ApplicationSchemester.CODE_THEME_LIGHT){
                    modeSwitch.setImageResource(R.drawable.ic_moonsmallicon);
                    storeThemeStatus(ApplicationSchemester.CODE_THEME_DARK);
                    startActivity(restart);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    modeSwitch.startAnimation(show);
                    modeSwitch.startAnimation(fadeon);
                } else if(getThemeStatus() == ApplicationSchemester.CODE_THEME_DARK){
                    modeSwitch.setImageResource(R.drawable.ic_suniconsmall);
                    storeThemeStatus(ApplicationSchemester.CODE_THEME_LIGHT);
                    startActivity(restart);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    modeSwitch.startAnimation(show);
                    modeSwitch.startAnimation(fadeon);
                } else {
                    modeSwitch.setImageResource(R.drawable.ic_moonsmallicon);
                    storeThemeStatus(ApplicationSchemester.CODE_THEME_DARK);
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
                storeUserPosition(schemester.getStringResource(R.string.teacher));
                schemester.toasterLong(schemester.getStringResource(R.string.under_construction_message));
            }
        });
        student.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeUserPosition(schemester.getStringResource(R.string.student));
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
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_USER_DEF(), MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString(schemester.getPREF_KEY_USER_DEF(), pos);
        mEditor.apply();
    }

    private void storeThemeStatus(int themechoice){
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putInt(schemester.getPREF_KEY_THEME(), themechoice);
        mEditor.apply();
    }
    public void setAppTheme(int code) {
        switch (code) {
            case ApplicationSchemester.CODE_THEME_DARK: setTheme(R.style.BlueDarkTheme);break;
            case ApplicationSchemester.CODE_THEME_LIGHT:
            default:setTheme(R.style.BlueLightTheme);
        }
    }
    private int getThemeStatus() {
        SharedPreferences mSharedPreferences = this.getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE);
        return mSharedPreferences.getInt(schemester.getPREF_KEY_THEME(), 0);
    }
}
