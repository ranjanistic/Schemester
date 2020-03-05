package org.timetable.schemester;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
@TargetApi(Build.VERSION_CODES.Q)
public class ModeOfConduct extends AppCompatActivity {
    ApplicationSchemester schemester;
    Button continueBtn, cancel;
    TextView cCaption, cLoginPath, cMessage;
    ImageView cBackImg;
    Window window;
    View activityFull;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        schemester = (ApplicationSchemester) this.getApplication();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_of_conduct);
        setWindowDecorDefaults();
        setViewsAndInitials();
        setListeners();

        String returnUserMsg = schemester.getStringResource(R.string.returning_as)+" " +getCredentials()[0]+" "
                + schemester.getStringResource(R.string.return_as_user_message)
                + "Are you in as\n " + getCredentials()[0]+"?",
                anonymUserMsg = schemester.getStringResource(R.string.continue_as) +" " + schemester.getStringResource(R.string.anonymous)+" "
                        +  schemester.getStringResource(R.string.continue_as_user_message) +
                        "Are you in as anonymous?";
        if (getThemeStatus() == ApplicationSchemester.CODE_THEME_INCOGNITO) {
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
            window.setNavigationBarColor(this.getResources().getColor(R.color.white));
            cMessage.setTypeface(Typeface.DEFAULT_BOLD);
            cCaption.setTextColor(getResources().getColor(R.color.black));
            cLoginPath.setTextColor(getResources().getColor(R.color.black));
            cMessage.setTextColor(getResources().getColor(R.color.black));
            cBackImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_usericon));
            activityFull.setBackgroundColor(getResources().getColor(R.color.white));
            cCaption.setText(schemester.getStringResource(R.string.returning_as));
            cLoginPath.setText(getCredentials()[0]);
            cMessage.setText(returnUserMsg);
        } else {
            window.setStatusBarColor(this.getResources().getColor(R.color.black));
            window.setNavigationBarColor(this.getResources().getColor(R.color.black));
            cCaption.setTextColor(getResources().getColor(R.color.white));
            cLoginPath.setTextColor(getResources().getColor(R.color.white));
            cMessage.setTextColor(getResources().getColor(R.color.white));
            cBackImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_icognitoman));
            activityFull.setBackgroundColor(getResources().getColor(R.color.black));
            cCaption.setText(schemester.getStringResource(R.string.continue_as));
            cLoginPath.setText(schemester.getStringResource(R.string.anonymous));
            cMessage.setText(anonymUserMsg);
        }
    }
    private void setWindowDecorDefaults(){
        window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }
    private void setViewsAndInitials(){
        activityFull = findViewById(R.id.confirmationActivity);
        continueBtn = findViewById(R.id.continueConduct);
        cancel = findViewById(R.id.cancelConduct);
        cCaption = findViewById(R.id.continueTitle);
        cLoginPath = findViewById(R.id.continueAs);
        cMessage = findViewById(R.id.continueInfo);
        cBackImg = findViewById(R.id.continueBackImage);
    }
    private void setListeners(){
        cancel.setOnClickListener(view -> finish());
        continueBtn.setOnClickListener(view -> {
            if(getThemeStatus() != ApplicationSchemester.CODE_THEME_INCOGNITO) {
                storeThemeStatus(ApplicationSchemester.CODE_THEME_INCOGNITO);
            } else { storeThemeStatus(ApplicationSchemester.CODE_THEME_LIGHT); }
            startActivity(new Intent(ModeOfConduct.this, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });
    }
    private String[] getCredentials(){
        String[] cred = new String[2];
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_CREDENTIALS(), MODE_PRIVATE);
        cred[0] =  mSharedPreferences.getString(schemester.getPREF_KEY_EMAIL(), null);
        cred[1] =  mSharedPreferences.getString(schemester.getPREF_KEY_ROLL(), null);
        return cred;
    }

    private void storeThemeStatus(int themeChoice){
        getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE).edit()
                .putInt(schemester.getPREF_KEY_THEME(), themeChoice).apply();
    }

    private int getThemeStatus(){
        return getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE)
                .getInt(schemester.getPREF_KEY_THEME(), 0);
    }

}
