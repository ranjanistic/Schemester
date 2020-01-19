package org.timetable.schedule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
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
import java.util.TimeZone;

import static android.content.ContentValues.TAG;

public class FullScheduleActivity extends AppCompatActivity {
    TextView   c1,c2,c3,c4,c5,c6,c7,c8,c9, email, roll, semester;
    Button m,t,w,th,f, logoutbtn;
    View dayschedule, settingsview, aboutview;
    ImageButton setting, about, git, tweet, insta, dml;
    ScrollView dayschedulePortrait;
    HorizontalScrollView dayscheduleLandscape;
    String semesterresult;
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
        if(isLandscape()) {
            dayscheduleLandscape = findViewById(R.id.weekdayplanview);
            dayscheduleLandscape.setVisibility(View.VISIBLE);
        } else{
            dayschedulePortrait= findViewById(R.id.weekdayplanview);
            dayschedulePortrait.setVisibility(View.VISIBLE);
        }
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
        insta = findViewById(R.id.instabtn);
        insta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://www.instagram.com/ranjanistic");
                Intent web = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(web);
            }
        });
        tweet = findViewById(R.id.tweetbtn);
        tweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://www.twitter.com/ranjanistic");
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
            case  2: readDatabase("monday");
                m.setBackgroundResource(R.drawable.leftroundbtnselected);
                m.setTextColor(getResources().getColor(R.color.black));
            break;
            case 3: readDatabase("tuesday");
                t.setBackgroundResource(R.drawable.leftroundbtnselected);
                t.setTextColor(getResources().getColor(R.color.black));
                break;
            case 4: readDatabase("wednesday");
                w.setBackgroundResource(R.drawable.leftroundbtnselected);
                w.setTextColor(getResources().getColor(R.color.black));
                break;
            case 5: readDatabase("thursday");
                th.setBackgroundResource(R.drawable.leftroundbtnselected);
                th.setTextColor(getResources().getColor(R.color.black));
                break;
            case 6: readDatabase("friday");
                f.setBackgroundResource(R.drawable.leftroundbtnselected);
                f.setTextColor(getResources().getColor(R.color.black));
                break;
            default:readDatabase("monday");
                m.setBackgroundResource(R.drawable.leftroundbtnselected);
                m.setTextColor(getResources().getColor(R.color.black));
        }

        m.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                readDatabase("monday");
            }
        });
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                readDatabase("tuesday");
            }
        });
        w.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                readDatabase("wednesday");
            }
        });
        th.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                readDatabase("thursday");
            }
        });
        f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                readDatabase("friday");
            }
        });
        semester = findViewById(R.id.semtextsetting);
        email = findViewById(R.id.emailtextsetting);
        roll = findViewById(R.id.rolltextsetting);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                semester.setText(readSemester());
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

        final CustomVerificationDialog customVerificationDialog = new CustomVerificationDialog(FullScheduleActivity.this, new OnDialogApplyListener() {
            @Override
            public void onApply(String email, String password) {
                customLoadDialogClass.show();
                authenticate(email, password);
            }
        });

        Button deleteacc = findViewById(R.id.deletebtn);
        deleteacc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customVerificationDialog.show();
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

    private String readSemester(){
        db.collection("semesterSchedule").document("semester")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                  semesterresult =  document.get("semnum").toString();
                            } else {
                                Log.d(TAG, "No such document");
                                Toast.makeText(FullScheduleActivity.this, "Unable to read", Toast.LENGTH_LONG).show();
                                semesterresult = "Error";
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                            Toast.makeText(FullScheduleActivity.this, "fail exception sem", Toast.LENGTH_LONG).show();
                            semesterresult = "Connection error";
                        }
                    }
                });
        return semesterresult;
    }
    private String[] getCredentials(){
        String[] cred = {"",""};
        SharedPreferences mSharedPreferences = getSharedPreferences("credentials", MODE_PRIVATE);
        cred[0] =  mSharedPreferences.getString("email", "");
        cred[1] =  mSharedPreferences.getString("roll", "");
        return cred;
    }
    private void readDatabase(String weekday){
        db.collection("semesterSchedule").document(weekday)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                c1.setText(document.get("p1").toString());
                                c2.setText(document.get("p2").toString());
                                c3.setText(document.get("p3").toString());
                                c4.setText(document.get("p4").toString());
                                c5.setText(document.get("p5").toString());
                                c6.setText(document.get("p6").toString());
                                c7.setText(document.get("p7").toString());
                                c8.setText(document.get("p8").toString());
                                c9.setText(document.get("p9").toString());
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
    }
    private void authenticate(String uid, String passphrase){
        customLoadDialogClass.show();
        AuthCredential credential = EmailAuthProvider
                .getCredential(uid, passphrase);
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(FullScheduleActivity.this, "Authentication passed", Toast.LENGTH_SHORT).show();
                            deleteUser();
                        } else {
                            Toast.makeText(FullScheduleActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            customLoadDialogClass.hide();
                        }
                    }
                });
    }
    private void deleteUser(){
        user.delete()
                .addOnCompleteListener (new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            storeLoginStatus(false);
                            customLoadDialogClass.hide();
                            Toast.makeText(FullScheduleActivity.this, "Your account was deleted permanently.", Toast.LENGTH_SHORT).show();
                            Intent i=new Intent(FullScheduleActivity.this, LoginActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // To clean up all activities
                            startActivity(i);
                        } else {
                            customLoadDialogClass.hide();
                            Toast.makeText(FullScheduleActivity.this, "Network problem", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void storeLoginStatus(Boolean logged){
        SharedPreferences mSharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean("loginstatus", logged);
        mEditor.apply();
    }

    private void checkOrientationSetVisibility(int visible){
        if(isLandscape()){
            dayscheduleLandscape = findViewById(R.id.weekdayplanview);
            dayscheduleLandscape.setVisibility(visible);
        } else{
            dayschedulePortrait= findViewById(R.id.weekdayplanview);
            dayschedulePortrait.setVisibility(visible);
        }
    }
    
    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
}
