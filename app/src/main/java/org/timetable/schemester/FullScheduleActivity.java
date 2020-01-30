package org.timetable.schemester;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;

import static android.content.ContentValues.TAG;

public class FullScheduleActivity extends AppCompatActivity {
    TextView   c1,c2,c3,c4,c5,c6,c7,c8,c9, email, roll, semester;
    Button m,t,w,th,f, logoutbtn;
    Button[] dayBtn = {m,t,w,th,f};
    String[] dayString = {"monday", "tuesday", "wednesday", "thursday", "friday"};
    View settingsview, aboutview;
    ImageButton setting, about, git, tweet, insta, dml, webbtn, fullsetting, updatecheck;
    String clg, course,year;
    NestedScrollView dayschedulePortrait, nestedScrollViewPort, nestedScrollViewLand;
    HorizontalScrollView horizontalScrollView;
    ScrollView scrollView;
    CustomVerificationDialog customVerificationDialog;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    CustomLoadDialogClass customLoadDialogClass;
    int versionCode = BuildConfig.VERSION_CODE;
    int i,j;
    String versionName = BuildConfig.VERSION_NAME;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAppTheme(getThemeStatus());
        setContentView(R.layout.activity_full_schedule);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if(getThemeStatus() == 101) {
            window.setStatusBarColor(this.getResources().getColor(R.color.blue));
            window.setNavigationBarColor(this.getResources().getColor(R.color.blue));
        } else if(getThemeStatus() == 102){
            window.setStatusBarColor(this.getResources().getColor(R.color.spruce));
            window.setNavigationBarColor(this.getResources().getColor(R.color.spruce));
        }
        clg = "DBC";
        course = "PHY-H";
        year = "Y2";
        c1 = findViewById(R.id.class1);
        c2 = findViewById(R.id.class2);
        c3 = findViewById(R.id.class3);
        c4 = findViewById(R.id.class4);
        c5 = findViewById(R.id.class5);
        c6 = findViewById(R.id.class6);
        c7 = findViewById(R.id.class7);
        c8 = findViewById(R.id.class8);
        c9 = findViewById(R.id.class9);

        dayBtn[0] = findViewById(R.id.mon);
        dayBtn[1] = findViewById(R.id.tue);
        dayBtn[2] = findViewById(R.id.wed);
        dayBtn[3] = findViewById(R.id.thu);
        dayBtn[4] = findViewById(R.id.fri);
        setting = findViewById(R.id.settingbtn);
        about = findViewById(R.id.aboutbtn);
        settingsview = findViewById(R.id.settingview);
        aboutview = findViewById(R.id.aboutview);
        
        isInternetAvailable();
        semester = findViewById(R.id.semtextsetting);
        settingsview.setVisibility(View.GONE);
        aboutview.setVisibility(View.GONE);
        git = findViewById(R.id.githubbtn);
        git.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://www.github.com/ranjanistic");
                Intent web = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(web);
            }
        });
        webbtn = findViewById(R.id.websiteBtn);
        webbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://darkmodelabs.github.io/SchemesterWeb/");
                Intent web = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(web);
            }
        });
        dml = findViewById(R.id.dmlabs);
        dml.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://www.github.com/darkmodelabs");
                Intent web = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(web);
            }
        });

        fullsetting = findViewById(R.id.fullsettingsbtn);
        fullsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FullScheduleActivity.this, Preferences.class);
                startActivity(intent);
            }
        });

        updatecheck = findViewById(R.id.checkupdateBtn);
        updatecheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customLoadDialogClass.show();
                readVersionCheckUpdate();
            }
        });

        customLoadDialogClass = new CustomLoadDialogClass(this, new OnDialogLoadListener() {
            @Override
            public void onLoad() {
            }

            @Override
            public String onLoadText() {
                return "Checking...";
            }
        });
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int getDayCount = calendar.get(Calendar.DAY_OF_WEEK);
        switch (getDayCount){
            case  2: readDatabase(clg,course,year,dayString[0]);
                dayBtn[0].setBackgroundResource(R.drawable.leftroundbtnselected);
                dayBtn[0].setTextColor(getResources().getColor(R.color.blue));
            break;
            case 3: readDatabase(clg,course,year,dayString[1]);
                dayBtn[1].setBackgroundResource(R.drawable.leftroundbtnselected);
                dayBtn[1].setTextColor(getResources().getColor(R.color.blue));
                break;
            case 4: readDatabase(clg,course,year,dayString[2]);
                dayBtn[2].setBackgroundResource(R.drawable.leftroundbtnselected);
                dayBtn[2].setTextColor(getResources().getColor(R.color.blue));
                break;
            case 5: readDatabase(clg,course,year,dayString[3]);
                dayBtn[3].setBackgroundResource(R.drawable.leftroundbtnselected);
                dayBtn[3].setTextColor(getResources().getColor(R.color.blue));
                break;
            case 6: readDatabase(clg,course,year,dayString[4]);
                dayBtn[4].setBackgroundResource(R.drawable.leftroundbtnselected);
                dayBtn[4].setTextColor(getResources().getColor(R.color.blue));
                break;
            default:readDatabase(clg,course,year,"monday");
                dayBtn[0].setBackgroundResource(R.drawable.leftroundbtnselected);
                dayBtn[0].setTextColor(getResources().getColor(R.color.blue));
        }
/*
        for (i=0, j =0 ;i<5;i++){
            dayBtn[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInternetAvailable();
                checkOrientationSetVisibility(View.VISIBLE);
                settingsview.setVisibility(View.GONE);
                aboutview.setVisibility(View.GONE);
                readDatabase(clg,course,year,dayString[i]);
                scrollTop();
                dayBtn[i].setBackgroundResource(R.drawable.leftroundbtnselected);
                dayBtn[i].setTextColor(getResources().getColor(R.color.blue));
                while(j<5) {
                    if(j!=i) {
                        dayBtn[j].setBackgroundResource(R.drawable.leftroundbtn);
                        dayBtn[j].setTextColor(getResources().getColor(R.color.white));
                    }
                    j++;
                }
            }
        });
        }
*/
        dayBtn[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInternetAvailable();
                scrollTop();
                dayBtn[0].setBackgroundResource(R.drawable.leftroundbtnselected);
                dayBtn[0].setTextColor(getResources().getColor(R.color.blue));
                dayBtn[1].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[1].setTextColor(getResources().getColor(R.color.white));
                dayBtn[2].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[2].setTextColor(getResources().getColor(R.color.white));
                dayBtn[3].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[3].setTextColor(getResources().getColor(R.color.white));
                dayBtn[4].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[4].setTextColor(getResources().getColor(R.color.white));
                checkOrientationSetVisibility(View.VISIBLE);
                settingsview.setVisibility(View.GONE);
                aboutview.setVisibility(View.GONE);
                readDatabase(clg,course,year,"monday");
            }
        });
        dayBtn[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInternetAvailable();
                scrollTop();
                dayBtn[0].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[0].setTextColor(getResources().getColor(R.color.white));
                dayBtn[1].setBackgroundResource(R.drawable.leftroundbtnselected);
                dayBtn[1].setTextColor(getResources().getColor(R.color.blue));
                dayBtn[2].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[2].setTextColor(getResources().getColor(R.color.white));
                dayBtn[3].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[3].setTextColor(getResources().getColor(R.color.white));
                dayBtn[4].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[4].setTextColor(getResources().getColor(R.color.white));
                checkOrientationSetVisibility(View.VISIBLE);
                settingsview.setVisibility(View.GONE);
                aboutview.setVisibility(View.GONE);
                readDatabase(clg,course,year,"tuesday");
            }
        });
        dayBtn[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInternetAvailable();
                scrollTop();
                dayBtn[0].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[0].setTextColor(getResources().getColor(R.color.white));
                dayBtn[1].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[1].setTextColor(getResources().getColor(R.color.white));
                dayBtn[2].setBackgroundResource(R.drawable.leftroundbtnselected);
                dayBtn[2].setTextColor(getResources().getColor(R.color.blue));
                dayBtn[3].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[3].setTextColor(getResources().getColor(R.color.white));
                dayBtn[4].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[4].setTextColor(getResources().getColor(R.color.white));
                checkOrientationSetVisibility(View.VISIBLE);
                settingsview.setVisibility(View.GONE);
                aboutview.setVisibility(View.GONE);
                readDatabase(clg,course,year,"wednesday");
            }
        });
        dayBtn[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInternetAvailable();
                scrollTop();
                dayBtn[0].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[0].setTextColor(getResources().getColor(R.color.white));
                dayBtn[1].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[1].setTextColor(getResources().getColor(R.color.white));
                dayBtn[2].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[2].setTextColor(getResources().getColor(R.color.white));
                dayBtn[3].setBackgroundResource(R.drawable.leftroundbtnselected);
                dayBtn[3].setTextColor(getResources().getColor(R.color.blue));
                dayBtn[4].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[4].setTextColor(getResources().getColor(R.color.white));
                checkOrientationSetVisibility(View.VISIBLE);
                settingsview.setVisibility(View.GONE);
                aboutview.setVisibility(View.GONE);
                readDatabase(clg,course,year,"thursday");
            }
        });
        dayBtn[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInternetAvailable();
                scrollTop();
                dayBtn[0].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[0].setTextColor(getResources().getColor(R.color.white));
                dayBtn[1].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[1].setTextColor(getResources().getColor(R.color.white));
                dayBtn[2].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[2].setTextColor(getResources().getColor(R.color.white));
                dayBtn[3].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[3].setTextColor(getResources().getColor(R.color.white));
                dayBtn[4].setBackgroundResource(R.drawable.leftroundbtnselected);
                dayBtn[4].setTextColor(getResources().getColor(R.color.blue));
                checkOrientationSetVisibility(View.VISIBLE);
                settingsview.setVisibility(View.GONE);
                aboutview.setVisibility(View.GONE);
                readDatabase(clg,course,year,"friday");
            }
        });


        email = findViewById(R.id.emailtextsetting);
        roll = findViewById(R.id.rolltextsetting);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readSemester(clg,course,year);
                String[] creds = getCredentials();
                email.setText(creds[0]);
                roll.setText(creds[1]);
                checkOrientationSetVisibility(View.GONE);
                settingsview.setVisibility(View.VISIBLE);
                aboutview.setVisibility(View.GONE);
            }
        });
        logoutbtn = findViewById(R.id.logout);
        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeLoginStatus(false);
                logOut();
            }
        });


        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkOrientationSetVisibility(View.GONE);
                settingsview.setVisibility(View.GONE);
                aboutview.setVisibility(View.VISIBLE);
            }
        });
    }

    private void readSemester(String source, String course,String year) {
            db.collection(source).document(course).collection(year).document("semester")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (Objects.requireNonNull(document).exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                  semester.setText(Objects.requireNonNull(document.get("semnum")).toString());
                            } else {
                                Log.d(TAG, "Server error");
                                Toast.makeText(FullScheduleActivity.this, "Server error", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.d(TAG, "Failed to receive data", task.getException());
                            Toast.makeText(FullScheduleActivity.this, "Please restart.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    private String[] getCredentials(){
        String[] cred = {"",""};
        SharedPreferences mSharedPreferences = getSharedPreferences("credentials", MODE_PRIVATE);
        cred[0] =  mSharedPreferences.getString("email", "");
        cred[1] =  mSharedPreferences.getString("roll", "");
        return cred;
    }
    private void readDatabase(String source, String course, String year, String weekday){
        db.collection(source).document(course).collection(year).document(weekday)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (Objects.requireNonNull(document).exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                c1.setText(Objects.requireNonNull(document.get("p1")).toString());
                                c2.setText(Objects.requireNonNull(document.get("p2")).toString());
                                c3.setText(Objects.requireNonNull(document.get("p3")).toString());
                                c4.setText(Objects.requireNonNull(document.get("p4")).toString());
                                c5.setText(Objects.requireNonNull(document.get("p5")).toString());
                                c6.setText(Objects.requireNonNull(document.get("p6")).toString());
                                c7.setText(Objects.requireNonNull(document.get("p7")).toString());
                                c8.setText(Objects.requireNonNull(document.get("p8")).toString());
                                c9.setText(Objects.requireNonNull(document.get("p9")).toString());
                            } else {
                                Log.d(TAG, "No such document");
                                Toast.makeText(FullScheduleActivity.this, "Server error. Try reinstalling.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                            Toast.makeText(FullScheduleActivity.this, "Connect to internet for latest details.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    private void logOut(){
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(FullScheduleActivity.this, "Logged out", Toast.LENGTH_LONG).show();
        Intent i=new Intent(FullScheduleActivity.this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // To clean up all activities
        startActivity(i);
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_from_right);
    }

    private void storeLoginStatus(Boolean logged){
        SharedPreferences mSharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean("loginstatus", logged);
        mEditor.apply();
    }
    
    private void scrollTop(){
        if(!isLandscape()) {
            scrollView = findViewById(R.id.verticalScrollviewPort);
            scrollView.smoothScrollTo(0,View.FOCUS_BACKWARD);
        } else{
            horizontalScrollView = findViewById(R.id.horizontalScrollLand);
            horizontalScrollView.smoothScrollTo(View.FOCUS_LEFT,0);
        }
    }

    private void readVersionCheckUpdate(){
        if(isNetworkConnected()) {
            db.collection("appConfig").document("verCurrent")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (Objects.requireNonNull(document).exists()) {
                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                    int vcode = Integer.parseInt(document.get("verCode").toString());
                                    final String vname = document.get("verName").toString();
                                    final String link = document.get("downlink").toString();
                                    if (vcode != versionCode || !vname.equals(versionName)) {
                                        customLoadDialogClass.hide();
                                        Toast.makeText(getApplicationContext(), "Update available", Toast.LENGTH_LONG).show();
                                        CustomConfirmDialogClass customConfirmDialogClass = new CustomConfirmDialogClass(FullScheduleActivity.this, new OnDialogConfirmListener() {
                                            @Override
                                            public void onApply(Boolean confirm) {
                                                Uri uri = Uri.parse(link);
                                                Intent web = new Intent(Intent.ACTION_VIEW, uri);
                                                startActivity(web);
                                            }

                                            @Override
                                            public String onCallText() {
                                                return "An update is available";
                                            }

                                            @Override
                                            public String onCallSub() {
                                                return "Your app version : " + versionName + "\nNew Version : " + vname + "\n\nUpdate to get the latest features and bug fixes. Download will start automatically. \nConfirm to download from website?";
                                            }
                                        });
                                        customConfirmDialogClass.setCanceledOnTouchOutside(false);
                                        customConfirmDialogClass.show();
                                    } else {
                                        customLoadDialogClass.hide();
                                        Toast.makeText(getApplicationContext(), "App is up to date. Check again later.", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    customLoadDialogClass.hide();
                                    Toast.makeText(getApplicationContext(), "Server error.", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                customLoadDialogClass.hide();
                                Toast.makeText(getApplicationContext(), "Network problem?", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            customLoadDialogClass.hide();
            Toast.makeText(getApplicationContext(), "Network problem?", Toast.LENGTH_LONG).show();
        }
    }
    private void checkOrientationSetVisibility(int visible){
            dayschedulePortrait= findViewById(R.id.weekdayplanview);
            dayschedulePortrait.setVisibility(visible);
    }
    public void setAppTheme(int code) {
        switch (code) {
            case 101:
                setTheme(R.style.AppTheme);
                break;
            case 102:
                setTheme(R.style.DarkTheme);
                break;
            default:setTheme(R.style.AppTheme);
        }
    }
    private int getThemeStatus() {
        SharedPreferences mSharedPreferences = this.getSharedPreferences("schemeTheme", MODE_PRIVATE);
        return mSharedPreferences.getInt("themeCode", 0);
    }
    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private void isInternetAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = Objects.requireNonNull(connectivityManager).getActiveNetworkInfo();
        if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())){
            Toast.makeText(getApplicationContext(),"Connect to internet for latest details",Toast.LENGTH_LONG).show();
        }
    }
}
