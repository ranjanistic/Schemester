package org.timetable.schemester;

import androidx.appcompat.app.AppCompatActivity;

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

public class ModeOfConduct extends AppCompatActivity {
    ApplicationSchemester schemester;
    Button continueBtn, cancel;
    TextView cCaption, cLoginPath, cMessage;
    ImageView cBackImg;
    Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        schemester = (ApplicationSchemester) this.getApplication();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_of_conduct);
        continueBtn = findViewById(R.id.continueConduct);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getThemeStatus() != ApplicationSchemester.CODE_THEME_INCOGNITO) {
                    storeThemeStatus(ApplicationSchemester.CODE_THEME_INCOGNITO);
                } else {
                    storeThemeStatus(ApplicationSchemester.CODE_THEME_LIGHT);
                }
                Intent i = new Intent(ModeOfConduct.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();

            }
        });
        cancel = findViewById(R.id.cancelConduct);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        View activityFull = findViewById(R.id.confirmationActivity);
        window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        cCaption = findViewById(R.id.continueTitle);
        cLoginPath = findViewById(R.id.continueAs);
        cMessage = findViewById(R.id.continueInfo);
        cBackImg = findViewById(R.id.continueBackImage);
        String returnUserMsg = schemester.getStringResource(R.string.returning_as)+" " +getCredentials()[0]+" "
                + schemester.getStringResource(R.string.return_as_user_message)
                + "Are you in as\n " + getCredentials()[0]+"?",
                anonymUserMsg = schemester.getStringResource(R.string.continue_as) +" " + schemester.getStringResource(R.string.anonymous)+" "
                        +  schemester.getStringResource(R.string.continue_as_user_message) +
                        "Are you in as anonymous?";
        if (getThemeStatus() == 103) {
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
            window.setNavigationBarColor(this.getResources().getColor(R.color.white));
            cMessage.setTypeface(Typeface.DEFAULT_BOLD);
            cCaption.setTextColor(getResources().getColor(R.color.black));
            cLoginPath.setTextColor(getResources().getColor(R.color.black));
            cMessage.setTextColor(getResources().getColor(R.color.black));
            cBackImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_settingico_mini));
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
    private String[] getCredentials(){
        String[] cred = {"",""};
        SharedPreferences mSharedPreferences = getSharedPreferences("credentials", MODE_PRIVATE);
        cred[0] =  mSharedPreferences.getString("email", "");
        cred[1] =  mSharedPreferences.getString("roll", "");
        return cred;
    }

    private void storeThemeStatus(int themechoice){
        SharedPreferences mSharedPreferences = this.getSharedPreferences("schemeTheme", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putInt("themeCode", themechoice);
        mEditor.apply();
    }

    private int getThemeStatus(){
        SharedPreferences mSharedPreferences = this.getSharedPreferences("schemeTheme", MODE_PRIVATE);
        return mSharedPreferences.getInt("themeCode", 0);
    }

}
