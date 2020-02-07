package org.timetable.schemester;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.google.firebase.firestore.SetOptions;
import com.google.j2objc.annotations.ObjectiveCName;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import static android.content.ContentValues.TAG;

public class FullScheduleActivity extends AppCompatActivity {
    TextView c1,c2,c3,c4,c5,c6,c7,c8,c9, p1,p2,p3,p4,p5,p6,p7,p8,p9,email, roll, semester;
    Button m,t,w,th,f, logoutbtn;
    TextView[] p = {p1,p2,p3,p4,p5,p6,p7,p8,p9};       //period objects
    TextView[] c = {c1,c2,c3,c4,c5,c6,c7,c8,c9};        //class name textview objects
    String[] pkey = {"p1","p2","p3","p4","p5","p6","p7","p8","p9"};     //keys to access database values
    Button[] dayBtn = {m,t,w,th,f};
    String[] dayString = {"monday", "tuesday", "wednesday", "thursday", "friday"};
    View settingsview, aboutview;
    ImageButton setting, about, git, dml, webbtn, fullsetting, updatecheck, noticebtn;
    String clg, course,year;        //TODO :To be assigned by user.
    NestedScrollView dayschedulePortrait;       //common view for both orientations
    HorizontalScrollView horizontalScrollView;
    ScrollView scrollView;
    ImageButton chatbtn;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    CustomLoadDialogClass customLoadDialogClass;
    CustomDownloadLoadDialog customDownloadLoadDialog;

    int[] workDayHalfStringResource = {
            R.string.mon,
            R.string.tue,
            R.string.wed,
            R.string.thu,
            R.string.fri,
    };
    //For time period string resources - 12 and 24 hours separately
    int[] periodStringResource12 = {
            R.string.period112,
            R.string.period212,
            R.string.period312,
            R.string.period412,
            R.string.period512,
            R.string.period612,
            R.string.period712,
            R.string.period812,
            R.string.period912},
            periodStringResource24 = {
                    R.string.period1,
                    R.string.period2,
                    R.string.period3,
                    R.string.period4,
                    R.string.period5,
                    R.string.period6,
                    R.string.period7,
                    R.string.period8,
                    R.string.period9
            };

    //Following assignments for version check and app update feature.
    int versionCode = BuildConfig.VERSION_CODE;
    String versionName = BuildConfig.VERSION_NAME;

    //Theme codes
     final static int CODE_DARK_THEME = 102, CODE_LIGHT_THEME = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAppTheme(getThemeStatus());
        setContentView(R.layout.activity_full_schedule);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        
        //Setting navigation and status bar color according to theme
        if(getThemeStatus() == CODE_LIGHT_THEME) {
            window.setStatusBarColor(this.getResources().getColor(R.color.blue));
            window.setNavigationBarColor(this.getResources().getColor(R.color.blue));
        } else if(getThemeStatus() == CODE_DARK_THEME){
            window.setStatusBarColor(this.getResources().getColor(R.color.spruce));
            window.setNavigationBarColor(this.getResources().getColor(R.color.spruce));
        } else {
            window.setStatusBarColor(this.getResources().getColor(R.color.blue));
            window.setNavigationBarColor(this.getResources().getColor(R.color.blue));
        }
        //TODO - Accept these values from user.
        clg = "DBC";
        course = "PHY-H";
        year = "Y2";
        
        int[] periodView = { R.id.period1, R.id.period2, R.id.period3, R.id.period4, R.id.period5, R.id.period6, R.id.period7, R.id.period8, R.id.period9,
        }, classView = { R.id.class1, R.id.class2, R.id.class3, R.id.class4, R.id.class5, R.id.class6, R.id.class7, R.id.class8, R.id.class9,
        };
        int k = 0;
        while(k<9) {
            p[k] = findViewById(periodView[k]);
            c[k] = findViewById(classView[k]);
            ++k;
        }
        int[] workDayID = {R.id.mon, R.id.tue, R.id.wed, R.id.thu, R.id.fri};
        int w = 0;
        while(w<5) {
            dayBtn[w] = findViewById(workDayID[w]);
            ++w;
        }
        
        setting = findViewById(R.id.settingbtn);
        about = findViewById(R.id.aboutbtn);
        settingsview = findViewById(R.id.settingview);
        aboutview = findViewById(R.id.aboutview);
        noticebtn = findViewById(R.id.noticebutton);

        isInternetAvailable();              //remind user to connect to internet

        semester = findViewById(R.id.semtextsetting);

        //by default, settings and about pages not shown
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

        //TODO: Notice board  and chatroom feature
        noticebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(FullScheduleActivity.this, "Under Construction, will be available soon!", Toast.LENGTH_LONG).show();
/*                Intent nIntent = new Intent(FullScheduleActivity.this, NoticeBoard.class);
                startActivity(nIntent);
 */
            }
        });

        chatbtn = findViewById(R.id.chatButton);
        chatbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(FullScheduleActivity.this, "Under Construction, will be available soon!", Toast.LENGTH_LONG).show();
        /*        Intent room = new Intent(FullScheduleActivity.this, ChatRoomActivity.class);
                startActivity(room);
         */
            }
        });

        customLoadDialogClass = new CustomLoadDialogClass(this, new OnDialogLoadListener() {
            @Override
            public void onLoad() {
            }
            @Override
            public String onLoadText() {
                return "Checking";
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
            default:readDatabase(clg,course,year,dayString[0]);
                dayBtn[0].setBackgroundResource(R.drawable.leftroundbtnselected);
                dayBtn[0].setTextColor(getResources().getColor(R.color.blue));
        }

        dayBtn[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInternetAvailable();
                scrollTop();
                setLoadingTextView();
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
                readDatabase(clg,course,year,dayString[0]);
            }
        });
        dayBtn[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInternetAvailable();
                scrollTop();
                setLoadingTextView();
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
                readDatabase(clg,course,year,dayString[1]);
            }
        });
        dayBtn[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInternetAvailable();
                scrollTop();
                setLoadingTextView();
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
                readDatabase(clg,course,year,dayString[2]);
            }
        });
        dayBtn[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInternetAvailable();
                scrollTop();
                setLoadingTextView();
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
                readDatabase(clg,course,year,dayString[3]);
            }
        });
        dayBtn[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInternetAvailable();
                scrollTop();
                setLoadingTextView();
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
                readDatabase(clg,course,year,dayString[4]);
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
                dayBtn[0].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[0].setTextColor(getResources().getColor(R.color.white));
                dayBtn[1].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[1].setTextColor(getResources().getColor(R.color.white));
                dayBtn[2].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[2].setTextColor(getResources().getColor(R.color.white));
                dayBtn[3].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[3].setTextColor(getResources().getColor(R.color.white));
                dayBtn[4].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[4].setTextColor(getResources().getColor(R.color.white));
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkOrientationSetVisibility(View.GONE);
                settingsview.setVisibility(View.GONE);
                aboutview.setVisibility(View.VISIBLE);
                dayBtn[0].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[0].setTextColor(getResources().getColor(R.color.white));
                dayBtn[1].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[1].setTextColor(getResources().getColor(R.color.white));
                dayBtn[2].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[2].setTextColor(getResources().getColor(R.color.white));
                dayBtn[3].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[3].setTextColor(getResources().getColor(R.color.white));
                dayBtn[4].setBackgroundResource(R.drawable.leftroundbtn);
                dayBtn[4].setTextColor(getResources().getColor(R.color.white));
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
    }

    @Override
    protected void onStart() {
        setTimeFormat(getTimeFormat());
        super.onStart();
    }

    //to set time display of period according to chosen format
    private void setTimeFormat(int tFormat){
        int i = 0;
        if(tFormat == 12) {
            while (i < 9) {
                p[i].setText(getStringResource(periodStringResource12[i]));
                ++i;
            }
        } else {
            while (i < 9) {
                p[i].setText(getStringResource(periodStringResource24[i]));
                ++i;
            }
        }
    }

    //string resource extractor
    private String getStringResource(int res){
        return getResources().getString(res);
    }

    private int getTimeFormat() {
        SharedPreferences mSharedPreferences = this.getSharedPreferences("schemeTime", MODE_PRIVATE);
        return mSharedPreferences.getInt("format", 24);
    }

    //this reads semester name from database and sets in semester textview
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
                            Toast.makeText(FullScheduleActivity.this, "Connect to internet for latest details.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    //gets locally stored credentials during login/register
    private String[] getCredentials(){
        String[] cred = {"",""};
        SharedPreferences mSharedPreferences = getSharedPreferences("credentials", MODE_PRIVATE);
        cred[0] =  mSharedPreferences.getString("email", "");
        cred[1] =  mSharedPreferences.getString("roll", "");
        return cred;
    }
    //reads period details from database and sets to their resp. textviews
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
                                int i = 0;
                                while(i<9) {
                                    c[i].setText(document.getString(pkey[i]));
                                    i++;
                                }
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

    //logs out the user
    private void logOut(){
        if(isNetworkConnected()) {
            setOnline(false);
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(FullScheduleActivity.this, "Logged out", Toast.LENGTH_LONG).show();
            Intent i = new Intent(FullScheduleActivity.this, PositionActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP); // To clean up all activities
            startActivity(i);
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_from_right);
            finish();
        } else {
            Toast.makeText(FullScheduleActivity.this, "Connect to internet", Toast.LENGTH_SHORT).show();
        }
    }

    //sets active status of user to database (to be used for chatroom feature)
    private void setOnline(Boolean status){
        Map<String, Object> data = new HashMap<>();
        data.put("active", status);
        db.collection("userbase").document(email.getText().toString())
                .set(data, SetOptions.merge());
    }

    private void storeLoginStatus(Boolean logged){
        SharedPreferences mSharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean("loginstatus", logged);
        mEditor.apply();
    }

    private void scrollTop(){
        if(!isLandscape()) {
            dayschedulePortrait= findViewById(R.id.weekdayplanview);
            dayschedulePortrait.smoothScrollTo(0,View.FOCUS_UP);
        } else{
            horizontalScrollView = findViewById(R.id.horizontalScrollLand);
            horizontalScrollView.smoothScrollTo(View.FOCUS_LEFT,0);
        }
    }

    //app updater function
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
                                    customLoadDialogClass.hide();
                                    if (vcode != versionCode || !vname.equals(versionName)) {
                                        Toast.makeText(getApplicationContext(), "Update available", Toast.LENGTH_LONG).show();
                                        final CustomConfirmDialogClass customConfirmDialogClass = new CustomConfirmDialogClass(FullScheduleActivity.this, new OnDialogConfirmListener() {
                                            @Override
                                            public void onApply(Boolean confirm) {
                                                if (!storagePermissionGranted()) {
                                                    CustomConfirmDialogClass permissionDialog = new CustomConfirmDialogClass(FullScheduleActivity.this, new OnDialogConfirmListener() {
                                                        @Override
                                                        public void onApply(Boolean confirm) {
                                                            customLoadDialogClass.dismiss();
                                                            requestStoragePermission();
                                                            if (storagePermissionGranted()){
                                                                if(isNetworkConnected()) {
                                                                    File file = new File(Environment.getExternalStorageDirectory() +"/Schemester/org.timetable.schemester-"+vname+".apk");
                                                                    if(file.exists()){
                                                                        showPackageAlert();
                                                                    } else {
                                                                        downloader(link, vname);
                                                                    }
                                                                } else {
                                                                    Toast.makeText(getApplicationContext(), "Internet problem", Toast.LENGTH_LONG).show();
                                                                }
                                                            } else {
                                                                customLoadDialogClass.dismiss();
                                                            }
                                                        }
                                                        @Override
                                                        public String onCallText() {
                                                            return "Storage permission required";
                                                        }
                                                        @Override
                                                        public String onCallSub() {
                                                            return "To download and save the latest version on your device, we need your storage permission. Confirm?";
                                                        }
                                                    });
                                                    permissionDialog.show();
                                                } else {
                                                    File file = new File(Environment.getExternalStorageDirectory() +"/Schemester/org.timetable.schemester-"+vname+".apk");
                                                    if(file.exists()){
                                                        showPackageAlert();
                                                    } else {
                                                        downloader(link,vname);
                                                    }
                                                }
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
     //                                   customLoadDialogClass.hide();
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

 private void downloader(final String link,final  String version){
     customDownloadLoadDialog = new CustomDownloadLoadDialog(FullScheduleActivity.this, new OnDialogDownloadLoadListener() {
         @Override
         public String getLink() {
             return link;
         }
         @Override
         public String getVersion() {
             return version;
         }
         @Override
         public void afterFinish(Boolean isCompleted) {
             if (isCompleted) {
                 showPackageAlert();
             } else {
                 customLoadDialogClass.hide();
                 Toast.makeText(getApplicationContext(), "Download Interrupted", Toast.LENGTH_SHORT).show();
             }
         }
     });
     customDownloadLoadDialog.show();
 }
 private void setLoadingTextView() {
     int i = 0;
     while (i < 9) {
         c[i].setText(getResources().getString(R.string.loading));
         i++;
     }
 }

 private void showPackageAlert(){
     CustomAlertDialog downloadFinishAlert = new CustomAlertDialog(FullScheduleActivity.this, new OnDialogAlertListener() {
         @Override
         public void onDismiss() {
         }
         @Override
         public String onCallText() {
             return "Download completed";
         }
         @Override
         public String onCallSub() {
             return "Latest version is downloaded. \n\nGo to File manager > Internal Storage > Schemester\n\nHere you'll find the latest package to install.\n\n(Delete that file if it is causing problems)";
         }
     });
     downloadFinishAlert.show();
 }
private boolean storagePermissionGranted(){
    return (ContextCompat.checkSelfPermission(FullScheduleActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED));
}
private void requestStoragePermission(){
    ActivityCompat.requestPermissions(FullScheduleActivity.this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
            1);
    customLoadDialogClass.hide();
}
    private void checkOrientationSetVisibility(int visible){
            dayschedulePortrait= findViewById(R.id.weekdayplanview);
            dayschedulePortrait.setVisibility(visible);
    }
    public void setAppTheme(int code) {
        switch (code) {
            case CODE_LIGHT_THEME:
                setTheme(R.style.BlueLightTheme);
                break;
            case CODE_DARK_THEME:
                setTheme(R.style.BlueDarkTheme);
                break;
            default:setTheme(R.style.BlueLightTheme);
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
