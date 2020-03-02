package org.timetable.schemester;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.timetable.schemester.dialog.CustomAlertDialog;
import org.timetable.schemester.dialog.CustomConfirmDialogClass;
import org.timetable.schemester.dialog.CustomDownloadLoadDialog;
import org.timetable.schemester.dialog.CustomLoadDialogClass;
import org.timetable.schemester.listener.OnDialogAlertListener;
import org.timetable.schemester.listener.OnDialogConfirmListener;
import org.timetable.schemester.listener.OnDialogDownloadLoadListener;
import org.timetable.schemester.listener.OnDialogLoadListener;

import java.io.File;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;


public class FullScheduleActivity extends AppCompatActivity {
    ApplicationSchemester schemester;
    TextView email, roll, semester, courseText, collegeText, yearText;
    Button  logoutbtn;
    TextView[] p = new TextView[9],       //period objects
                c = new TextView[9];        //class name text view objects
    Button[] dayBtn = new Button[5];
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    View settingsview, aboutview;
    ImageButton setting, about, git, dml, webbtn, fullsetting, updatecheck, noticebtn;
    NestedScrollView dayschedulePortrait;       //common view for both orientations
    HorizontalScrollView horizontalScrollView;
    TextView versionNameView;
    ImageButton chatbtn, incognito;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    CustomLoadDialogClass customLoadDialogClass;
    CustomDownloadLoadDialog customDownloadLoadDialog;
    Window window;
    CustomConfirmDialogClass customConfirmDialogClassVerification;
    Calendar calendar;
     String websiteLink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        schemester = (ApplicationSchemester) this.getApplication();
        super.onCreate(savedInstanceState);
        setAppTheme(getThemeStatus());
        setContentView(R.layout.activity_full_schedule);
        storeLoginStatus(true);
        /*
         * sequence of functions called one by, names are self explanatory.
         * The order of function call can be important, for initialization of any object is followed by its utilization (for example).
         */
        alertForInternetDisability();
        setWindowDecorDefaults();
        findViewsAndAssignObjects();
        setThemeConsequences();
        initiateCustomDialogBoxes();
        setListenersAndDefaultViews();
        setDayBtnDefaultsAndDayDetails();
        setWeekDayBtnClickListener();
    }
    @Override
    protected void onStart() {
        setTimeFormat(getTimeFormat());
        super.onStart();
    }
    private void setWindowDecorDefaults(){
        window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }
    private void findViewsAndAssignObjects(){
        dayschedulePortrait= findViewById(R.id.weekdayplanview);
        incognito = findViewById(R.id.incognitoBtn);
        versionNameView = findViewById(R.id.versionCodeName);
        about = findViewById(R.id.aboutbtn);
        settingsview = findViewById(R.id.settingview);
        aboutview = findViewById(R.id.aboutview);
        noticebtn = findViewById(R.id.noticebutton);
        chatbtn = findViewById(R.id.chatButton);

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
            schemester.buttonLongPressToast(dayBtn[w],getResources().getStringArray(R.array.weekdays)[w+1]);
            ++w;
        }
        semester = findViewById(R.id.semtextsetting);
        courseText = findViewById(R.id.courseTextSetting);
        collegeText = findViewById(R.id.collegeTextSetting);
        yearText = findViewById(R.id.yearTextSetting);
        email = findViewById(R.id.emailtextsetting);
        roll = findViewById(R.id.rolltextsetting);
        setting = findViewById(R.id.settingbtn);
        logoutbtn = findViewById(R.id.logout);
        fullsetting = findViewById(R.id.fullsettingsbtn);
        git = findViewById(R.id.githubbtn);
        updatecheck = findViewById(R.id.checkupdateBtn);
        webbtn = findViewById(R.id.websiteBtn);

        schemester.imageButtonLongPressToast(noticebtn,"Notices from authority");
        schemester.imageButtonLongPressToast(chatbtn,"Chat with others!");
        schemester.imageButtonLongPressToast(setting,"Quick settings");
        schemester.imageButtonLongPressToast(about,"About");
        schemester.imageButtonLongPressToast(incognito,"Go anonymous");
        schemester.imageButtonLongPressToast(fullsetting, "Full settings");
        schemester.imageButtonLongPressToast(updatecheck,"Check for updates");
        schemester.imageButtonLongPressToast(webbtn,"Visit online");
    }

    private void setThemeConsequences(){
        if (getThemeStatus() == ApplicationSchemester.CODE_THEME_INCOGNITO) {
            window.setStatusBarColor(this.getResources().getColor(R.color.black));
            window.setNavigationBarColor(this.getResources().getColor(R.color.black));
            incognito.setImageResource(R.drawable.ic_usericon);
            logoutbtn.setVisibility(View.GONE);
            email.setText(schemester.getStringResource(R.string.anonymous));
            roll.setText(schemester.getStringResource(R.string.anonymous));
        } else if (getThemeStatus() == ApplicationSchemester.CODE_THEME_DARK) {
            window.setStatusBarColor(this.getResources().getColor(R.color.spruce));
            window.setNavigationBarColor(this.getResources().getColor(R.color.spruce));
            incognito.setImageResource(R.drawable.ic_icognitoman);
            logoutbtn.setVisibility(View.VISIBLE);
        } else {
            window.setStatusBarColor(this.getResources().getColor(R.color.blue));
            window.setNavigationBarColor(this.getResources().getColor(R.color.blue));
            incognito.setImageResource(R.drawable.ic_icognitoman);
            logoutbtn.setVisibility(View.VISIBLE);
        }
        if(getThemeStatus() != ApplicationSchemester.CODE_THEME_INCOGNITO) {
            email.setText(getCredentials()[0]);
            roll.setText(getCredentials()[1]);
        }
    }

    private void setListenersAndDefaultViews(){
        schemester.setCollegeCourseYear(getAdditionalInfo()[0], getAdditionalInfo()[1], getAdditionalInfo()[2]);
        versionNameView.setText(ApplicationSchemester.versionName);
        calendar = Calendar.getInstance(TimeZone.getDefault());
        //by default, settings and about pages not shown
        settingsview.setVisibility(View.GONE);
        aboutview.setVisibility(View.GONE);
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
        if(isLandscape()) {
            int d = 0;
            while (d<5) {
                dayBtn[d].setText(getResources().getStringArray(R.array.weekdays)[d+1]);
                ++d;
            }
        }
        getWebsiteLinkFromDatabase();

        incognito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(FullScheduleActivity.this, ModeOfConduct.class);
                startActivity(i);
            }
        });

        git.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(schemester.getStringResource(R.string.ranjanistic_github));
                Intent web = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(web);
            }
        });

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

        fullsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FullScheduleActivity.this, Preferences.class);
                startActivity(intent);
            }
        });


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
                schemester.toasterLong(schemester.getStringResource(R.string.under_construction_message));
/*                Intent nIntent = new Intent(FullScheduleActivity.this, NoticeBoard.class);
                startActivity(nIntent);
 */
            }
        });
        chatbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!checkIfEmailVerified()) {
                    customConfirmDialogClassVerification.show();
                }else if(getThemeStatus() == ApplicationSchemester.CODE_THEME_INCOGNITO){
                    Intent mode = new Intent(FullScheduleActivity.this, ModeOfConduct.class);
                    startActivity(mode);
                } else {
                    schemester.toasterLong(schemester.getStringResource(R.string.under_construction_message));
                    Intent room = new Intent(FullScheduleActivity.this, ChatRoomActivity.class);
                    startActivity(room);
                }
            }
        });

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearAllWeekDayButtonSelection();
                readAndSetSemesterFromDatabase(schemester.getCOLLECTION_GLOBAL_INFO(),schemester.getDOCUMENT_GLOBAL_SEMESTER(),schemester.getCOLLECTION_YEAR_CODE());
                dayschedulePortrait.setVisibility(View.GONE);
                settingsview.setVisibility(View.VISIBLE);
                aboutview.setVisibility(View.GONE);
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearAllWeekDayButtonSelection();
                dayschedulePortrait.setVisibility(View.GONE);
                settingsview.setVisibility(View.GONE);
                aboutview.setVisibility(View.VISIBLE);
            }
        });
        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Snackbar snackbar
                = Snackbar.make(view, "Sure to log out?", 7000);
                        snackbar.setBackgroundTint(getResources().getColor(R.color.dead_blue))
                        .setTextColor(getResources().getColor(R.color.white))
                        .setAction("Logout", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                storeLoginStatus(false);
                                logoutCurrentUser();
                            }
                        }).setActionTextColor(getResources().getColor(R.color.yellow))
                        .show();
            }
        });

    }

    private void initiateCustomDialogBoxes(){
        //verification confirmation alert box
        customConfirmDialogClassVerification = new CustomConfirmDialogClass(FullScheduleActivity.this, new OnDialogConfirmListener() {
            @Override
            public void onApply(Boolean confirm) { sendVerificationEmail(); }
            @Override
            public String onCallText() { return schemester.getStringResource(R.string.email_not_verified); }
            @Override
            public String onCallSub() {
                return schemester.getStringResource(R.string.email_verification_persuade_text);
            }
        });
        
        //loading box
        customLoadDialogClass = new CustomLoadDialogClass(this, new OnDialogLoadListener() {
            @Override
            public void onLoad() {}
            @Override
            public String onLoadText() { return schemester.getStringResource(R.string.checking); }
        });
    }

    private void setDayBtnDefaultsAndDayDetails(){
        int[] dayValues = {Calendar.MONDAY,Calendar.TUESDAY,Calendar.WEDNESDAY,Calendar.THURSDAY,Calendar.FRIDAY};
        int k =0;
        while(k<5){
            if(dayValues[k] == calendar.get(Calendar.DAY_OF_WEEK)){
                readDatabase(schemester.getCOLLECTION_COLLEGE_CODE(),schemester.getDOCUMENT_COURSE_CODE(),schemester.getCOLLECTION_YEAR_CODE(),schemester.getWorkDayString()[k]);
                dayBtn[k].setBackgroundResource(R.drawable.leftroundbtnselected);
                dayBtn[k].setTextColor(getResources().getColor(R.color.blue));
            }
            ++k;
        }
        if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ){
            readDatabase(schemester.getCOLLECTION_COLLEGE_CODE(),schemester.getDOCUMENT_COURSE_CODE(),schemester.getCOLLECTION_YEAR_CODE(),schemester.getWorkDayString()[0]);
            dayBtn[0].setBackgroundResource(R.drawable.leftroundbtnselected);
            dayBtn[0].setTextColor(getResources().getColor(R.color.blue));
        }
    }

    int di = 0;
    private void setWeekDayBtnClickListener() {
        for (di = 0; di < 5; di++) {
            final int finalDI = di;
            dayBtn[di].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertForInternetDisability();
                    scrollTop();
                    setLoadingTextView();
                    dayschedulePortrait.setVisibility(View.VISIBLE);
                    settingsview.setVisibility(View.GONE);
                    aboutview.setVisibility(View.GONE);
                    readDatabase(schemester.getCOLLECTION_COLLEGE_CODE(), schemester.getDOCUMENT_COURSE_CODE(),
                            schemester.getCOLLECTION_YEAR_CODE(), schemester.getWorkDayString()[finalDI]);
                    int j = 0;
                    while(j<5) {
                        if(finalDI == j) {
                            dayBtn[j].setBackgroundResource(R.drawable.leftroundbtnselected);
                            dayBtn[j].setTextColor(getResources().getColor(R.color.blue));
                        } else {
                            dayBtn[j].setBackgroundResource(R.drawable.leftroundbtn);
                            dayBtn[j].setTextColor(getResources().getColor(R.color.white));
                        }
                        ++j;
                    }
                }
            });
        }
        di = 0;
    }
    private void clearAllWeekDayButtonSelection(){
        int i = 0;
        while(i<5) {
            dayBtn[i].setBackgroundResource(R.drawable.leftroundbtn);
            dayBtn[i].setTextColor(getResources().getColor(R.color.white));
            ++i;
        }
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


    private String[] getAdditionalInfo() {
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
                            schemester.toasterLong(schemester.getStringResource(R.string.confirmation_email_sent_text));
                            logoutCurrentUser();
                        }
                    }
                });
    }
    private Boolean checkIfEmailVerified() { return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).isEmailVerified(); }
    
    //to set time display of period according to chosen format
    private void setTimeFormat(int tFormat){
        int i = 0;
        if(tFormat == 12) {
            while (i < 9) {
                p[i].setText(schemester.getStringResource(schemester.getPeriodStringResource12()[i]));
                ++i;
            }
        } else {
            while (i < 9) {
                p[i].setText(schemester.getStringResource(schemester.getPeriodStringResource24()[i]));
                ++i;
            }
        }
    }

    private int getTimeFormat() {
        SharedPreferences mSharedPreferences = this.getSharedPreferences(schemester.getPREF_HEAD_TIME_FORMAT(), MODE_PRIVATE);
        return mSharedPreferences.getInt(schemester.getPREF_KEY_TIME_FORMAT(), 24);
    }

    //this reads semester name from database and sets in semester text view
    private void readAndSetSemesterFromDatabase(String source, String course,final String year) {
        db.collection(source).document(course)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (Objects.requireNonNull(document).exists()) {
                                semester.setText(Objects.requireNonNull(document.get(year)).toString());
                            } else { schemester.toasterLong(schemester.getStringResource(R.string.unable_to_read)); }
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
                                    i++; } 
                            } else schemester.toasterLong(schemester.getStringResource(R.string.server_error_try_reinstall));
                        } else { schemester.toasterLong(schemester.getStringResource(R.string.internet_error)); }
                    }
                });
    }

    //logs out the user
    private void logoutCurrentUser(){
        if(isNetworkConnected()) {
            FirebaseAuth.getInstance().signOut();
            schemester.toasterLong(schemester.getStringResource(R.string.logged_out));
            Intent i = new Intent(FullScheduleActivity.this, PositionActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP); // To clean up all activities
            startActivity(i);
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_from_right);
            finish();
        } else { schemester.toasterShort(schemester.getStringResource(R.string.connect_to_internet)); }
    }
    
    private void storeLoginStatus(Boolean logged){
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_LOGIN_STAT(), MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean(schemester.getPREF_KEY_LOGIN_STAT(), logged);
        mEditor.apply();
    }

    private void scrollTop(){
        if(!isLandscape()) {
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
                                    final String pathName = Environment.getExternalStorageDirectory() +"/" +schemester.getStringResource(R.string.app_name)+"/"+
                                            ApplicationSchemester.applicationId+"-"+vname+".apk";
                                    customLoadDialogClass.hide();
                                    if (vcode != ApplicationSchemester.versionCode || !Objects.equals(vname,ApplicationSchemester.versionName)) {
                                        Toast.makeText(getApplicationContext(), schemester.getStringResource(R.string.update_available), Toast.LENGTH_LONG).show();
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
                                                                    File file = new File(pathName);
                                                                    if(file.exists()){
                                                                        showPackageAlert();
                                                                    } else { downloader(link, vname); }
                                                                } else { schemester.toasterLong(schemester.getStringResource(R.string.internet_problem)); }
                                                            } else { customLoadDialogClass.dismiss(); }
                                                        }
                                                        @Override
                                                        public String onCallText() {
                                                            return schemester.getStringResource(R.string.storage_permit_required);
                                                        }
                                                        @Override
                                                        public String onCallSub() {
                                                            return "To download and save the latest version on your device, we need your storage permission. Confirm?";
                                                        }
                                                    });
                                                    permissionDialog.show();
                                                } else {
                                                    File file = new File(pathName);
                                                    if(file.exists()){ showPackageAlert(); }
                                                    else { downloader(link,vname); }
                                                }
                                            }
                                            @Override
                                            public String onCallText() {
                                                return schemester.getStringResource(R.string.an_update_is_available);
                                            }
                                            @Override
                                            public String onCallSub() {
                                                return schemester.getStringResource(R.string.your_app_ver_colon) +
                                                        ApplicationSchemester.versionName + "\n"+schemester.getStringResource(R.string.new_ver_colon)+
                                                        vname + "\n\nUpdate to get the latest features and bug fixes. Download will start automatically. \nConfirm to download from website?";
                                            }
                                        });
                                        customConfirmDialogClass.setCanceledOnTouchOutside(false);
                                        customConfirmDialogClass.show();
                                    } else { schemester.toasterLong(schemester.getStringResource(R.string.app_uptodate_check_later)); }
                                } else {
                                    customLoadDialogClass.hide();
                                    schemester.toasterLong(schemester.getStringResource(R.string.server_error));
                                }
                            } else {
                                customLoadDialogClass.hide();
                                schemester.toasterLong(schemester.getStringResource(R.string.network_problem));
                            }
                        }
                    });
        } else {
            customLoadDialogClass.hide();
            schemester.toasterLong(schemester.getStringResource(R.string.network_problem));
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
                 schemester.toasterShort(schemester.getStringResource(R.string.download_interrupted));
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
         public void onDismiss() {}
         @Override
         public String onCallText() {
             return schemester.getStringResource(R.string.download_completed);
         }
         @Override
         public String onCallSub() {
             return "Latest version is downloaded. \n\nGo to File manager > Internal Storage > Schemester\n\nHere you'll find the latest package to install.\n\n(Delete that file if it is causing problems)";
         }
     });downloadFinishAlert.show();
 }
 
private boolean storagePermissionGranted(){ return (ContextCompat.checkSelfPermission(FullScheduleActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED)); }

private void requestStoragePermission(){
    ActivityCompat.requestPermissions(FullScheduleActivity.this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
            1);
    customLoadDialogClass.hide();
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
    private boolean isLandscape() { return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE; }
    
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm != null ? cm.getActiveNetworkInfo() : null) != null && cm.getActiveNetworkInfo().isConnected();
    }

    private void alertForInternetDisability() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = Objects.requireNonNull(connectivityManager).getActiveNetworkInfo();
        if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())){
            schemester.toasterLong(schemester.getStringResource(R.string.internet_error));
        }
    }
}
