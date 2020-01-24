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
    View settingsview, aboutview;
    ImageButton setting, about, git, tweet, insta, dml, webbtn, fullsetting, updatecheck;
    String clg, course,year;
    NestedScrollView dayschedulePortrait;
    CustomVerificationDialog customVerificationDialog;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    CustomLoadDialogClass customLoadDialogClass;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_schedule);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.blue));
        window.setNavigationBarColor(this.getResources().getColor(R.color.blue));
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
        m = findViewById(R.id.mon);
        t = findViewById(R.id.tue);
        w = findViewById(R.id.wed);
        th = findViewById(R.id.thu);
        f = findViewById(R.id.fri);
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

            }
        });

        customLoadDialogClass = new CustomLoadDialogClass(this, new OnDialogLoadListener() {
            @Override
            public void onLoad() {
            }

            @Override
            public String onLoadText() {
                return "Deleting your account";
            }
        });
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int getDayCount = calendar.get(Calendar.DAY_OF_WEEK);
        switch (getDayCount){
            case  2: readDatabase(clg,course,year,"monday");
                m.setBackgroundResource(R.drawable.leftroundbtnselected);
                m.setTextColor(getResources().getColor(R.color.black));
            break;
            case 3: readDatabase(clg,course,year,"tuesday");
                t.setBackgroundResource(R.drawable.leftroundbtnselected);
                t.setTextColor(getResources().getColor(R.color.black));
                break;
            case 4: readDatabase(clg,course,year,"wednesday");
                w.setBackgroundResource(R.drawable.leftroundbtnselected);
                w.setTextColor(getResources().getColor(R.color.black));
                break;
            case 5: readDatabase(clg,course,year,"thursday");
                th.setBackgroundResource(R.drawable.leftroundbtnselected);
                th.setTextColor(getResources().getColor(R.color.black));
                break;
            case 6: readDatabase(clg,course,year,"friday");
                f.setBackgroundResource(R.drawable.leftroundbtnselected);
                f.setTextColor(getResources().getColor(R.color.black));
                break;
            default:readDatabase(clg,course,year,"monday");
                m.setBackgroundResource(R.drawable.leftroundbtnselected);
                m.setTextColor(getResources().getColor(R.color.black));
        }

        m.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInternetAvailable();
                m.setBackgroundResource(R.drawable.leftroundbtnselected);
                m.setTextColor(getResources().getColor(R.color.black));
                t.setBackgroundResource(R.drawable.leftroundbtn);
                t.setTextColor(getResources().getColor(R.color.white));
                w.setBackgroundResource(R.drawable.leftroundbtn);
                w.setTextColor(getResources().getColor(R.color.white));
                th.setBackgroundResource(R.drawable.leftroundbtn);
                th.setTextColor(getResources().getColor(R.color.white));
                f.setBackgroundResource(R.drawable.leftroundbtn);
                f.setTextColor(getResources().getColor(R.color.white));
                checkOrientationSetVisibility(View.VISIBLE);
                settingsview.setVisibility(View.GONE);
                aboutview.setVisibility(View.GONE);
                readDatabase(clg,course,year,"monday");
            }
        });
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInternetAvailable();
                m.setBackgroundResource(R.drawable.leftroundbtn);
                m.setTextColor(getResources().getColor(R.color.white));
                t.setBackgroundResource(R.drawable.leftroundbtnselected);
                t.setTextColor(getResources().getColor(R.color.black));
                w.setBackgroundResource(R.drawable.leftroundbtn);
                w.setTextColor(getResources().getColor(R.color.white));
                th.setBackgroundResource(R.drawable.leftroundbtn);
                th.setTextColor(getResources().getColor(R.color.white));
                f.setBackgroundResource(R.drawable.leftroundbtn);
                f.setTextColor(getResources().getColor(R.color.white));
                checkOrientationSetVisibility(View.VISIBLE);
                settingsview.setVisibility(View.GONE);
                aboutview.setVisibility(View.GONE);
                readDatabase(clg,course,year,"tuesday");
            }
        });
        w.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInternetAvailable();
                m.setBackgroundResource(R.drawable.leftroundbtn);
                m.setTextColor(getResources().getColor(R.color.white));
                t.setBackgroundResource(R.drawable.leftroundbtn);
                t.setTextColor(getResources().getColor(R.color.white));
                w.setBackgroundResource(R.drawable.leftroundbtnselected);
                w.setTextColor(getResources().getColor(R.color.black));
                th.setBackgroundResource(R.drawable.leftroundbtn);
                th.setTextColor(getResources().getColor(R.color.white));
                f.setBackgroundResource(R.drawable.leftroundbtn);
                f.setTextColor(getResources().getColor(R.color.white));
                checkOrientationSetVisibility(View.VISIBLE);
                settingsview.setVisibility(View.GONE);
                aboutview.setVisibility(View.GONE);
                readDatabase(clg,course,year,"wednesday");
            }
        });
        th.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInternetAvailable();
                m.setBackgroundResource(R.drawable.leftroundbtn);
                m.setTextColor(getResources().getColor(R.color.white));
                t.setBackgroundResource(R.drawable.leftroundbtn);
                t.setTextColor(getResources().getColor(R.color.white));
                w.setBackgroundResource(R.drawable.leftroundbtn);
                w.setTextColor(getResources().getColor(R.color.white));
                th.setBackgroundResource(R.drawable.leftroundbtnselected);
                th.setTextColor(getResources().getColor(R.color.black));
                f.setBackgroundResource(R.drawable.leftroundbtn);
                f.setTextColor(getResources().getColor(R.color.white));
                checkOrientationSetVisibility(View.VISIBLE);
                settingsview.setVisibility(View.GONE);
                aboutview.setVisibility(View.GONE);
                readDatabase(clg,course,year,"thursday");
            }
        });
        f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInternetAvailable();
                m.setBackgroundResource(R.drawable.leftroundbtn);
                m.setTextColor(getResources().getColor(R.color.white));
                t.setBackgroundResource(R.drawable.leftroundbtn);
                t.setTextColor(getResources().getColor(R.color.white));
                w.setBackgroundResource(R.drawable.leftroundbtn);
                w.setTextColor(getResources().getColor(R.color.white));
                th.setBackgroundResource(R.drawable.leftroundbtn);
                th.setTextColor(getResources().getColor(R.color.white));
                f.setBackgroundResource(R.drawable.leftroundbtnselected);
                f.setTextColor(getResources().getColor(R.color.black));
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


    private void checkOrientationSetVisibility(int visible){
            dayschedulePortrait= findViewById(R.id.weekdayplanview);
            dayschedulePortrait.setVisibility(visible);
    }
    
    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
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
