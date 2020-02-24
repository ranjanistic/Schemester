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
import com.google.protobuf.LazyStringArrayList;

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
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import static android.content.ContentValues.TAG;

public class FullScheduleActivity extends AppCompatActivity {
    ApplicationSchemester schemester;
    TextView c1,c2,c3,c4,c5,c6,c7,c8,c9, p1,p2,p3,p4,p5,p6,p7,p8,p9,email, roll, semester, courseText, collegeText, yearText;
    Button m,t,w,th,f, logoutbtn;
    TextView[] p = {p1,p2,p3,p4,p5,p6,p7,p8,p9};       //period objects
    TextView[] c = {c1,c2,c3,c4,c5,c6,c7,c8,c9};        //class name textview objects
    Button[] dayBtn = {m,t,w,th,f};
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String[] dayString = {"monday", "tuesday", "wednesday", "thursday", "friday"};
    View settingsview, aboutview;
    ImageButton setting, about, git, dml, webbtn, fullsetting, updatecheck, noticebtn;
    NestedScrollView dayschedulePortrait;       //common view for both orientations
    HorizontalScrollView horizontalScrollView;
    ScrollView scrollView;
    TextView versionNameView;
    ImageButton chatbtn, incognito;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    CustomLoadDialogClass customLoadDialogClass;
    CustomDownloadLoadDialog customDownloadLoadDialog;

    //Following assignments for version check and app update feature.
     String websiteLink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        schemester = (ApplicationSchemester) this.getApplication();
        super.onCreate(savedInstanceState);
        setAppTheme(getThemeStatus());
        setContentView(R.layout.activity_full_schedule);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        //Setting navigation and status bar color according to theme
        if (getThemeStatus() == ApplicationSchemester.CODE_THEME_INCOGNITO) {
            window.setStatusBarColor(this.getResources().getColor(R.color.black));
            window.setNavigationBarColor(this.getResources().getColor(R.color.black));
        } else if (getThemeStatus() == ApplicationSchemester.CODE_THEME_DARK) {
            window.setStatusBarColor(this.getResources().getColor(R.color.spruce));
            window.setNavigationBarColor(this.getResources().getColor(R.color.spruce));
        } else {
            window.setStatusBarColor(this.getResources().getColor(R.color.blue));
            window.setNavigationBarColor(this.getResources().getColor(R.color.blue));
        }
        schemester.setCollegeCourseYear(getAdditionalInfo()[0], getAdditionalInfo()[1], getAdditionalInfo()[2]);
        incognito = findViewById(R.id.incognitoBtn);
        if (getThemeStatus() == ApplicationSchemester.CODE_THEME_INCOGNITO) {
            incognito.setImageResource(R.drawable.ic_usericon);
        } else incognito.setImageResource(R.drawable.ic_icognitoman);
        incognito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(FullScheduleActivity.this, ModeOfConduct.class);
                startActivity(i);
            }
        });


        int[] periodView = {R.id.period1, R.id.period2, R.id.period3, R.id.period4, R.id.period5, R.id.period6, R.id.period7, R.id.period8, R.id.period9,
        }, classView = {R.id.class1, R.id.class2, R.id.class3, R.id.class4, R.id.class5, R.id.class6, R.id.class7, R.id.class8, R.id.class9,
        };
        int k = 0;
        while (k < 9) {
            p[k] = findViewById(periodView[k]);
            c[k] = findViewById(classView[k]);
            ++k;
        }
        int[] workDayID = {R.id.mon, R.id.tue, R.id.wed, R.id.thu, R.id.fri};
        int w = 0;
        while (w < 5) {
            dayBtn[w] = findViewById(workDayID[w]);
            ++w;
        }

        versionNameView = findViewById(R.id.versionCodeName);
        versionNameView.setText(ApplicationSchemester.versionName);
        about = findViewById(R.id.aboutbtn);
        settingsview = findViewById(R.id.settingview);
        aboutview = findViewById(R.id.aboutview);
        noticebtn = findViewById(R.id.noticebutton);

        isInternetAvailable();              //remind user to connect to internet

        semester = findViewById(R.id.semtextsetting);
        courseText = findViewById(R.id.courseTextSetting);
        collegeText = findViewById(R.id.collegeTextSetting);
        yearText = findViewById(R.id.yearTextSetting);

        int i = 0;
        while(i<getResources().getStringArray(R.array.college_code_array).length) {
            if (Objects.equals(getAdditionalInfo()[0], getResources().getStringArray(R.array.college_code_array)[i])) {
                collegeText.setText(getResources().getStringArray(R.array.college_array)[i]);
            }
            ++i;
        }
        i = 0;
        while(i<getResources().getStringArray(R.array.course_code_array).length) {
            if (Objects.equals(getAdditionalInfo()[1], getResources().getStringArray(R.array.course_code_array)[i])) {
                courseText.setText(getResources().getStringArray(R.array.course_array)[i]);
            }
            ++i;
        }
        i=0;
        while(i<getResources().getStringArray(R.array.year_code_array).length) {
            if (Objects.equals(getAdditionalInfo()[2], getResources().getStringArray(R.array.year_code_array)[i])) {
                yearText.setText(getResources().getStringArray(R.array.year_array)[i]);
            }
            ++i;
        }

        //by default, settings and about pages not shown
        settingsview.setVisibility(View.GONE);
        aboutview.setVisibility(View.GONE);
        getWebsiteLinkFromDatabase();
        git = findViewById(R.id.githubbtn);
        git.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(schemester.getStringResource(R.string.ranjanistic_github));
                Intent web = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(web);
            }
        });
        webbtn = findViewById(R.id.websiteBtn);
        webbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(websiteLink);
                Intent web = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(web);
            }
        });
        dml = findViewById(R.id.dmlabs);
        dml.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(schemester.getStringResource(R.string.darkmodelabs_github));
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

        //TODO: Notice board  and chat room feature
        noticebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                schemester.toasterLong("Under Construction, will be available soon!");
/*                Intent nIntent = new Intent(FullScheduleActivity.this, NoticeBoard.class);
                startActivity(nIntent);
 */
            }
        });
        final CustomConfirmDialogClass customConfirmDialogClassVerfication = new CustomConfirmDialogClass(FullScheduleActivity.this, new OnDialogConfirmListener() {
            @Override
            public void onApply(Boolean confirm) {
                sendVerificationEmail();
            }

            @Override
            public String onCallText() {
                return "Email not verified";
            }

            @Override
            public String onCallSub() {
                return "You need to verify your email ID to enter the chat room, because authenticity is important while interaction with others.\n\nConfirm to receive a verification link to your provided email ID and verify yourself there?\n\n(You can also change your email ID in settings, if you don't have access to your current email ID..)";
            }
        });
        chatbtn = findViewById(R.id.chatButton);
        chatbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!checkIfEmailVerified()) {
                    customConfirmDialogClassVerfication.show();
                }else if(getThemeStatus() == ApplicationSchemester.CODE_THEME_INCOGNITO){
                    Intent mode = new Intent(FullScheduleActivity.this, ModeOfConduct.class);
                    startActivity(mode);
                } else {
                    schemester.toasterLong("Under Construction, will be available soon!");
/*                    Intent room = new Intent(FullScheduleActivity.this, ChatRoomActivity.class);
                    startActivity(room);
 */
                }
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
            case 3: readDatabase(schemester.getCOLLECTION_COLLEGE_CODE(),schemester.getDOCUMENT_COURSE_CODE(),schemester.getCOLLECTION_YEAR_CODE(),dayString[1]);
                dayBtn[1].setBackgroundResource(R.drawable.leftroundbtnselected);
                dayBtn[1].setTextColor(getResources().getColor(R.color.blue));
                break;
            case 4: readDatabase(schemester.getCOLLECTION_COLLEGE_CODE(),schemester.getDOCUMENT_COURSE_CODE(),schemester.getCOLLECTION_YEAR_CODE(),dayString[2]);
                dayBtn[2].setBackgroundResource(R.drawable.leftroundbtnselected);
                dayBtn[2].setTextColor(getResources().getColor(R.color.blue));
                break;
            case 5: readDatabase(schemester.getCOLLECTION_COLLEGE_CODE(),schemester.getDOCUMENT_COURSE_CODE(),schemester.getCOLLECTION_YEAR_CODE(),dayString[3]);
                dayBtn[3].setBackgroundResource(R.drawable.leftroundbtnselected);
                dayBtn[3].setTextColor(getResources().getColor(R.color.blue));
                break;
            case 6: readDatabase(schemester.getCOLLECTION_COLLEGE_CODE(),schemester.getDOCUMENT_COURSE_CODE(),schemester.getCOLLECTION_YEAR_CODE(),dayString[4]);
                dayBtn[4].setBackgroundResource(R.drawable.leftroundbtnselected);
                dayBtn[4].setTextColor(getResources().getColor(R.color.blue));
                break;
            case  2:
            default:readDatabase(schemester.getCOLLECTION_COLLEGE_CODE(),schemester.getDOCUMENT_COURSE_CODE(),schemester.getCOLLECTION_YEAR_CODE(),dayString[0]);
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
                readDatabase(schemester.getCOLLECTION_COLLEGE_CODE(),schemester.getDOCUMENT_COURSE_CODE(),schemester.getCOLLECTION_YEAR_CODE(),dayString[0]);
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
                readDatabase(schemester.getCOLLECTION_COLLEGE_CODE(),schemester.getDOCUMENT_COURSE_CODE(),schemester.getCOLLECTION_YEAR_CODE(),dayString[1]);
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
                readDatabase(schemester.getCOLLECTION_COLLEGE_CODE(),schemester.getDOCUMENT_COURSE_CODE(),schemester.getCOLLECTION_YEAR_CODE(),dayString[2]);
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
                readDatabase(schemester.getCOLLECTION_COLLEGE_CODE(),schemester.getDOCUMENT_COURSE_CODE(),schemester.getCOLLECTION_YEAR_CODE(),dayString[3]);
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
                readDatabase(schemester.getCOLLECTION_COLLEGE_CODE(),schemester.getDOCUMENT_COURSE_CODE(),schemester.getCOLLECTION_YEAR_CODE(),dayString[4]);
            }
        });

        email = findViewById(R.id.emailtextsetting);
        roll = findViewById(R.id.rolltextsetting);
        setting = findViewById(R.id.settingbtn);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readSemester(schemester.getCOLLECTION_GLOBAL_INFO(),schemester.getDOCUMENT_GLOBAL_SEMESTER(),schemester.getCOLLECTION_YEAR_CODE());
                if(getThemeStatus() == ApplicationSchemester.CODE_THEME_INCOGNITO) {
                    email.setText(schemester.getStringResource(R.string.anonymous));
                    roll.setText(schemester.getStringResource(R.string.anonymous));
                }else {
                    email.setText(getCredentials()[0]);
                    roll.setText(getCredentials()[1]);
                }
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
                logoutCurrentUser();
            }
        });
    }
    private void getWebsiteLinkFromDatabase(){
        db.collection(schemester.getCOLLECTION_APP_CONFIGURATION()).document(schemester.getDOCUMENT_LINKS())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (Objects.requireNonNull(document).exists()) {
                                websiteLink = document.getString(schemester.getFIELD_WEBSITE());
                            }
                        }
                    }
                });
    }


    @Override
    protected void onStart() {
        setTimeFormat(getTimeFormat());
        super.onStart();
    }

    String[] getAdditionalInfo() {
        String[] CCY = new String[3];
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_ADDITIONAL_INFO(), MODE_PRIVATE);
        CCY[0] = mSharedPreferences.getString(schemester.getPREF_KEY_COLLEGE(), "");
        CCY[1] = mSharedPreferences.getString(schemester.getPREF_KEY_COURSE(), "");
        CCY[2] = mSharedPreferences.getString(schemester.getPREF_KEY_YEAR(), "");
        return CCY;
    }

    private void sendVerificationEmail() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        Objects.requireNonNull(user).sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            schemester.toasterLong("A confirmation email is sent to your email address");
                            logoutCurrentUser();
                        }
                    }
                });
    }
    private Boolean checkIfEmailVerified() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        return user.isEmailVerified();
    }
    //to set time display of period according to chosen format
    private void setTimeFormat(int tFormat){
        int i = 0;
        if(tFormat == 12) {
            while (i < 9) {
                p[i].setText(getStringResource(schemester.getPeriodStringResource12()[i]));
                ++i;
            }
        } else {
            while (i < 9) {
                p[i].setText(getStringResource(schemester.getPeriodStringResource24()[i]));
                ++i;
            }
        }
    }

    //string resource extractor
    private String getStringResource(int res){
        return getResources().getString(res);
    }

    private int getTimeFormat() {
        SharedPreferences mSharedPreferences = this.getSharedPreferences(schemester.getPREF_HEAD_TIME_FORMAT(), MODE_PRIVATE);
        return mSharedPreferences.getInt(schemester.getPREF_KEY_TIME_FORMAT(), 24);
    }

    //this reads semester name from database and sets in semester textview
    private void readSemester(String source, String course,final String year) {
        db.collection(source).document(course)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (Objects.requireNonNull(document).exists()) {
                                semester.setText(Objects.requireNonNull(document.get(year)).toString());
                            } else {
                                schemester.toasterLong("Unable to read");
                            }
                        }
                    }
                });
    }

    //gets locally stored credentials during login/register
    private String[] getCredentials(){
        String[] cred = new String[2];
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_CREDENTIALS(), MODE_PRIVATE);
        cred[0] =  mSharedPreferences.getString(schemester.getPREF_KEY_EMAIL(), "");
        cred[1] =  mSharedPreferences.getString(schemester.getPREF_KEY_ROLL(), "");
        return cred;
    }
    //reads period details from database and sets to their resp. text views
    private void readDatabase(String source, String course, String year, String weekday){
        db.collection(source).document(course).collection(year).document(weekday)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (Objects.requireNonNull(document).exists()) {
                                int i = 0;
                                while(i<9) {
                                    c[i].setText(document.getString(schemester.getPKey()[i]));
                                    i++;
                                }
                            } else {
                                schemester.toasterLong("Server error. Try reinstalling");
                            }
                        } else {
                            schemester.toasterLong(schemester.getStringResource(R.string.internet_error));
                        }
                    }
                });
    }

    //logs out the user
    private void logoutCurrentUser(){
        if(isNetworkConnected()) {
            setOnline(false);
            FirebaseAuth.getInstance().signOut();
            schemester.toasterLong("Logged out");
            Intent i = new Intent(FullScheduleActivity.this, PositionActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP); // To clean up all activities
            startActivity(i);
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_from_right);
            finish();
        } else {
            schemester.toasterShort("Connect to internet");
        }
    }

    //sets active status of user to database (to be used for chat room feature)
    private void setOnline(Boolean status){
        Map<String, Object> data = new HashMap<>();
        data.put(schemester.getFIELD_USER_ACTIVE(), status);
        db.collection(schemester.getCOLLECTION_USERBASE()).document(email.getText().toString())
                .update(data);
    }

    private void storeLoginStatus(Boolean logged){
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_LOGIN_STAT(), MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean(schemester.getPREF_KEY_LOGIN_STAT(), logged);
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
            db.collection(schemester.getCOLLECTION_APP_CONFIGURATION()).document(schemester.getDOCUMENT_VERSION_CURRENT())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (Objects.requireNonNull(document).exists()) {
                                    int vcode = Integer.parseInt(Objects.toString(document.get(schemester.getFIELD_VERSION_CODE())));
                                    final String vname = document.getString(schemester.getFIELD_VERSION_NAME());
                                    final String link = document.getString(schemester.getFIELD_DOWNLOAD_LINK());
                                    customLoadDialogClass.hide();
                                    if (vcode != ApplicationSchemester.versionCode || !Objects.equals(vname,ApplicationSchemester.versionName)) {
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
                                                return "Your app version : " + ApplicationSchemester.versionName + "\nNew Version : " + vname + "\n\nUpdate to get the latest features and bug fixes. Download will start automatically. \nConfirm to download from website?";
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
            case ApplicationSchemester.CODE_THEME_INCOGNITO:
                setTheme(R.style.IncognitoTheme);
                break;
            case ApplicationSchemester.CODE_THEME_DARK:
                setTheme(R.style.BlueDarkTheme);
                break;
            case ApplicationSchemester.CODE_THEME_LIGHT:
            default:setTheme(R.style.BlueLightTheme);
        }
    }
    private int getThemeStatus() {
        SharedPreferences mSharedPreferences = getApplication().getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE);
        return mSharedPreferences.getInt(schemester.getPREF_KEY_THEME(), 0);
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
            schemester.toasterLong(schemester.getStringResource(R.string.internet_error));
        }
    }
}
