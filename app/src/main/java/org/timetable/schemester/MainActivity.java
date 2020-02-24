package org.timetable.schemester;

// The homepage of the application. Only logged in user should reach here.

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity{
    ApplicationSchemester schemester;
    private TextView semestertxt, noclass,
    c1,c2,c3,c4,c5,c6,c7,c8,c9,p1,p2,p3,p4,p5,p6,p7,p8,p9,
    day, month, time;
    private TextView[] c = {c1,c2,c3,c4,c5,c6,c7,c8,c9},
    p = {p1,p2,p3,p4,p5,p6,p7,p8,p9};
    String[] pKey = {"p1","p2","p3","p4","p5","p6","p7","p8","p9"};
    private TextView loginIdOnDrawer;
    private Button date;
    private ImageButton drawerArrow, switchThemeBtn;
    private LinearLayout headingView, settingTab, scheduleTab;
    private ScrollView scrollView;
    private Calendar calendar;
    private Animation hide, show, fadeOn, fadeOff;
    private ReadClassFromDatabaseTask mreadClassFromDatabaseTask;
    public Activity mainact;
    public static boolean isCreated = false;
    highlighterTask mhighilighterTask;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private int[]  periodView = {
            R.id.periodMain1, R.id.periodMain2, R.id.periodMain3, R.id.periodMain4, R.id.periodMain5,
            R.id.periodMain6, R.id.periodMain7, R.id.periodMain8, R.id.periodMain9,
    }, classView = {
            R.id.classMain1, R.id.classMain2, R.id.classMain3, R.id.classMain4, R.id.classMain5,
            R.id.classMain6, R.id.classMain7, R.id.classMain8, R.id.classMain9,
    };
    int[] timeStringResource = {
        R.string.time1, R.string.time2, R.string.time3, R.string.time4, R.string.time5,
                R.string.time6, R.string.time7, R.string.time8, R.string.time9, R.string.time10,
    };
    private LinearLayout bottomDrawer;
    private checkUpdate update;     //update checker asyncTask class
    private Window window;
    private BottomSheetBehavior bottomSheetBehavior;

    String COLLECTION_GLOBAL_INFO = "global_info", DOCUMENT_GLOBAL_SEMESTER = "semester",
            COLLECTION_COLLEGE_CODE , DOCUMENT_COURSE_CODE , COLLECTION_YEAR_CODE;
    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        schemester = (ApplicationSchemester) this.getApplication();
        setAppTheme();
        mainact = this;
        isCreated = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        schemester.setCollegeCourseYear(getAdditionalInfo()[0],getAdditionalInfo()[1],getAdditionalInfo()[2]);
        COLLECTION_COLLEGE_CODE = getAdditionalInfo()[0];
        DOCUMENT_COURSE_CODE = getAdditionalInfo()[1];
        COLLECTION_YEAR_CODE = getAdditionalInfo()[2];

        setWindowDecorDefaults();
        storeUserDefinition(readUserPosition(),getStoredEmail());
        calendar = Calendar.getInstance(TimeZone.getDefault());
        setViews();         //assigning all views to their respective objects
        setBottomSheetFeature();            //setting bottom drawer behaviour
        runTimeDisplayOnBottomSheet();      //display current time
        setThemeConsequencesAndActions();      //set navigation bar color and theme button listener
        setButtonClickListeners();

        //check holiday and display accordingly
        setHolidayViewIfHoliday();
        
        //initializing main schedule update task
        mreadClassFromDatabaseTask = new ReadClassFromDatabaseTask();
        mhighilighterTask = new highlighterTask();
        //check for updates task
        update = new checkUpdate();
        update.execute();
    }

    @Override
    public void onBackPressed() {
        this.finishAffinity();
        super.onBackPressed();
    }

    private class checkNetAsync extends AsyncTask<Void,Void,Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            return isInternetAvailable();
        }
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(!aBoolean) schemester.toasterLong(schemester.getStringResource(R.string.internet_error));
            super.onPostExecute(aBoolean);
        }
    }
    private void setViews(){
        scrollView = findViewById(R.id.scrollView);
        headingView = findViewById(R.id.period_view);
        noclass = findViewById(R.id.noclasstext);
        settingTab = findViewById(R.id.settingTab);
        scheduleTab = findViewById(R.id.fullScheduleTab);
        semestertxt = findViewById(R.id.sem_text);
        switchThemeBtn = findViewById(R.id.switchThemeMain);
        time = findViewById(R.id.present_time);
        drawerArrow = findViewById(R.id.drawerarrow);
        int k = 0;
        while(k<9) {
            p[k] = findViewById(periodView[k]);
            c[k] = findViewById(classView[k]);
            ++k;
        }
        bottomDrawer = findViewById(R.id.bottom_drawer);
        date = findViewById(R.id.present_date);
        day = findViewById(R.id.weekday_text);
        month = findViewById(R.id.month_text);
        loginIdOnDrawer = findViewById(R.id.drawerLoginID);
    }

    private void setWindowDecorDefaults(){
        window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setNavigationBarColor(this.getResources().getColor(R.color.dull_white));
    }

    private void setBottomSheetFeature(){
        bottomSheetBehavior = BottomSheetBehavior.from(bottomDrawer);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setHideable(false);
        LinearLayout drawerPeek = findViewById(R.id.drawerarrowHolder);
        drawerPeek.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);
        bottomSheetBehavior.setPeekHeight(drawerPeek.getMeasuredHeight());
        drawerArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    drawerArrow.setRotation(-90);
                } else if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    drawerArrow.setRotation(90);
                }
            }
        });

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        if(getThemeStatus() == ApplicationSchemester.CODE_THEME_INCOGNITO){
            loginIdOnDrawer.setText(schemester.getStringResource(R.string.anonymous));
            loginIdOnDrawer.setTextColor(getResources().getColor(R.color.blue));
        } else loginIdOnDrawer.setText(getCredentials()[0]);
    }
    private String[] getCredentials(){
        String[] cred = {null,null};
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_CREDENTIALS(), MODE_PRIVATE);
        cred[0] =  mSharedPreferences.getString(schemester.getPREF_KEY_EMAIL(), "");
        cred[1] =  mSharedPreferences.getString(schemester.getPREF_KEY_ROLL(), "");
        return cred;
    }
    @SuppressLint("SimpleDateFormat")
    private void runTimeDisplayOnBottomSheet(){
        final Handler timeHandler = new Handler(getMainLooper());
        timeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(getTimeFormat() == 12) {
                    time.setText(new SimpleDateFormat("hh:mm a").format(new Date()));
                } else{
                    time.setText(new SimpleDateFormat("HH:mm").format(new Date()));
                }
                timeHandler.postDelayed(this, 10);
            }
        }, 10);
    }
    private void setThemeConsequencesAndActions(){
        hide = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.gone_centrally);
        show = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.emerge_centrally);
        fadeOn = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadeliton);
        fadeOff= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadelitoff);

        if(isLandscape()){
            if(getThemeStatus() == ApplicationSchemester.CODE_THEME_DARK)
                window.setNavigationBarColor(this.getResources().getColor(R.color.charcoal));
            else if(getThemeStatus() == ApplicationSchemester.CODE_THEME_INCOGNITO)
                window.setNavigationBarColor(this.getResources().getColor(R.color.black_overlay));
            else    window.setNavigationBarColor(this.getResources().getColor(R.color.dull_white));
        } else {
            if(getThemeStatus() == ApplicationSchemester.CODE_THEME_LIGHT)
                window.setNavigationBarColor(this.getResources().getColor(R.color.dull_white));
            else window.setNavigationBarColor(this.getResources().getColor(R.color.black_overlay));
        }

        final Intent restart = new Intent(MainActivity.this, MainActivity.class);
        if(getThemeStatus() == ApplicationSchemester.CODE_THEME_DARK){
            switchThemeBtn.setBackgroundResource(R.drawable.ic_moonsmallicon);
        } else if(getThemeStatus() == ApplicationSchemester.CODE_THEME_INCOGNITO) {
            switchThemeBtn.setBackgroundResource(R.drawable.ic_icognitoman);
        }else{
            switchThemeBtn.setBackgroundResource(R.drawable.ic_suniconsmall);
            storeThemeStatus(ApplicationSchemester.CODE_THEME_LIGHT);
        }

        switchThemeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchThemeBtn.startAnimation(hide);
                switchThemeBtn.startAnimation(fadeOff);
                if(getThemeStatus() == ApplicationSchemester.CODE_THEME_LIGHT){
                    switchThemeBtn.setBackgroundResource(R.drawable.ic_moonsmallicon);
                    storeThemeStatus(ApplicationSchemester.CODE_THEME_DARK);
                    startActivity(restart);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    switchThemeBtn.startAnimation(show);
                    switchThemeBtn.startAnimation(fadeOn);
                } else if(getThemeStatus() == ApplicationSchemester.CODE_THEME_DARK){
                    switchThemeBtn.setBackgroundResource(R.drawable.ic_suniconsmall);
                    storeThemeStatus(ApplicationSchemester.CODE_THEME_LIGHT);
                    startActivity(restart);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    switchThemeBtn.startAnimation(show);
                    switchThemeBtn.startAnimation(fadeOn);
                }else if(getThemeStatus() == ApplicationSchemester.CODE_THEME_INCOGNITO){
                    Intent mode = new Intent(MainActivity.this, ModeOfConduct.class);
                    startActivity(mode);
                } else {
                    switchThemeBtn.setBackgroundResource(R.drawable.ic_moonsmallicon);
                    storeThemeStatus(ApplicationSchemester.CODE_THEME_DARK);
                    startActivity(restart);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    switchThemeBtn.startAnimation(show);
                    switchThemeBtn.startAnimation(fadeOn);
                }
            }
        });
    }

    private void setButtonClickListeners(){
        scheduleTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setActivityChangerButtonsDisabled(true);
                mreadClassFromDatabaseTask.cancel(true);
                Intent i = new Intent(MainActivity.this, FullScheduleActivity.class);
                startActivity(i);
            }
        });

        settingTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setActivityChangerButtonsDisabled(true);
                mreadClassFromDatabaseTask.cancel(true);
                mhighilighterTask.cancel(true);
                Intent i = new Intent(MainActivity.this, Preferences.class);
                startActivity(i);
            }
        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    private class checkUpdate extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            checkUpdateThenNotify();
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private void checkUpdateThenNotify(){
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
                                    if (vcode != ApplicationSchemester.versionCode || !Objects.equals(vname, ApplicationSchemester.versionName)) {
                                        schemester.toasterLong("Update available");
                                        final CustomConfirmDialogClass customConfirmDialogClass = new CustomConfirmDialogClass(MainActivity.this, new OnDialogConfirmListener() {
                                            @Override
                                            public void onApply(Boolean confirm) {
                                                if (storagePermissionGranted()){
                                                    if(isInternetAvailable()) {
                                                        File file = new File(Environment.getExternalStorageDirectory() +"/Schemester/org.timetable.schemester-"+vname+".apk");
                                                        if(file.exists()){
                                                            showPackageAlert(vname);
                                                        } else {
                                                            downloader(link, vname);
/*                                                            testing
                                                            Intent down = new Intent(MainActivity.this, PermanentActionActivity.class);
                                                            down.putExtra("link", link);
                                                            down.putExtra("vname", vname);
                                                            startActivity(down);
 */
                                                        }
                                                    }
                                                } else {
                                                    CustomConfirmDialogClass permissionDialog = new CustomConfirmDialogClass(MainActivity.this, new OnDialogConfirmListener() {
                                                        @Override
                                                        public void onApply(Boolean confirm) {
                                                            requestStoragePermission();
                                                            update.cancel(true);
                                                            new checkUpdate().execute();
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
                                                }
                                            }
                                            @Override
                                            public String onCallText() {
                                                return "An update is available";
                                            }
                                            @Override
                                            public String onCallSub() {
                                                return "Your app version : " + ApplicationSchemester.versionName + "\nNew Version : " + vname + "\n\nUpdate to get the latest features and bug fixes. Download will start automatically. \nConfirm to download?";
                                            }
                                        });
                                        customConfirmDialogClass.setCanceledOnTouchOutside(false);
                                        customConfirmDialogClass.show();
                                    }
                                }
                            }
                        }
                    });
    }

    /**
     *  downloader initiates apk download task in CustomDownloadLoadDialog class
     * @param link : latest apk link is passed from database to this parameter, to download it.
     * @param version: latest version name is passed from database to this parameter, to display the user
     */
    private void downloader(final String link,final  String version){
        CustomDownloadLoadDialog customDownloadLoadDialog = new CustomDownloadLoadDialog(MainActivity.this, new OnDialogDownloadLoadListener() {
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
                    showPackageAlert(version);
                } else {
                    schemester.toasterShort("Download Interrupted");
                    update.cancel(true);
                }
            }
        });
        customDownloadLoadDialog.show();
    }

    /**
     * @param newVname: shows local package availability alert to user
     */

    private void showPackageAlert(final String newVname){
        CustomAlertDialog downloadFinishAlert = new CustomAlertDialog(MainActivity.this, new OnDialogAlertListener() {
            @Override
            public void onDismiss() {
            }
            @Override
            public String onCallText() {
                return "Download completed";
            }
            @Override
            public String onCallSub() {
                return "Latest version is downloaded. \n\nGo to File manager > Internal Storage > Schemester >\n\nHere you'll find the latest package ("+newVname+") to install.\n\n(If it is causing problems, delete that file and try again.)";
            }
        });
        downloadFinishAlert.show();
    }

    @Override
    protected void onStart() {
        setActivityChangerButtonsDisabled(false);
        new checkNetAsync().execute();
        setSemester(schemester.getCOLLECTION_GLOBAL_INFO(),
                schemester.getDOCUMENT_GLOBAL_SEMESTER(),
                schemester.getCOLLECTION_YEAR_CODE());
        date.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
        day.setText(getWeekdayFromCode(calendar.get(Calendar.DAY_OF_WEEK)));
        month.setText(getMonthFromCode(calendar.get(Calendar.MONTH)));
          if(!isHolidayToday()) {
              setTimeFormat(getTimeFormat());
              if(mreadClassFromDatabaseTask.isCancelled()) {
                  mreadClassFromDatabaseTask = new ReadClassFromDatabaseTask();
                  mreadClassFromDatabaseTask.execute();
              } else {
                  mreadClassFromDatabaseTask.cancel(true);
                  mreadClassFromDatabaseTask = new ReadClassFromDatabaseTask();
                  mreadClassFromDatabaseTask.execute();
              }
              if(mhighilighterTask.isCancelled()) {
                  mhighilighterTask = new highlighterTask();
                  mhighilighterTask.execute();
              } else {
                  mhighilighterTask.cancel(true);
                  mhighilighterTask = new highlighterTask();
                  mhighilighterTask.execute();
              }
          }
        super.onStart();
    }

    private class highlighterTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            highlightCurrentPeriod();
            super.onPostExecute(aVoid);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new highlighterTask().execute();
                }
            }, 100);
        }
    }

    @Override
    protected void onDestroy() {
        mreadClassFromDatabaseTask.cancel(true);
        mhighilighterTask.cancel(true);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mreadClassFromDatabaseTask.cancel(true);
        mhighilighterTask.cancel(true);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setActivityChangerButtonsDisabled(false);
    }

    @Override
    protected void onStop() {
        mreadClassFromDatabaseTask.cancel(true);
        mhighilighterTask.cancel(true);
        super.onStop();
    }

    private void setActivityChangerButtonsDisabled(Boolean disabled){
        settingTab.setClickable(!disabled);
        scheduleTab.setClickable(!disabled);
    }

    /**
     * gets saved format preference from user choice stored in sharedPreferences
     * and sets time format accordingly
     * @param tFormat: 12 or 24 hours format.
     */
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

    //schedule receiver from database task
    private class ReadClassFromDatabaseTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            date.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
            day.setText(getWeekdayFromCode(calendar.get(Calendar.DAY_OF_WEEK)));
            month.setText(getMonthFromCode(calendar.get(Calendar.MONTH)));
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            setSemester(COLLECTION_GLOBAL_INFO,
                    DOCUMENT_GLOBAL_SEMESTER,
                    COLLECTION_YEAR_CODE);
                readDatabase(COLLECTION_COLLEGE_CODE,
                        DOCUMENT_COURSE_CODE,
                        COLLECTION_YEAR_CODE,
                        getWeekdayFromCode(calendar.get(Calendar.DAY_OF_WEEK))
                );
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            final Handler handler = new Handler(getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mreadClassFromDatabaseTask = new ReadClassFromDatabaseTask();
                    mreadClassFromDatabaseTask.execute();
                }
            }, 1500);
            super.onPostExecute(aVoid);
        }
    }

    //whether user is a teacher or student
    private String readUserPosition(){
        SharedPreferences mSharedPreferences = this.getSharedPreferences(schemester.getPREF_HEAD_USER_DEF(), MODE_PRIVATE);
        return mSharedPreferences.getString(schemester.getPREF_KEY_USER_DEF(), "");
    }

    //check holiday and set view accordingly
    private void setHolidayViewIfHoliday(){
        if (isHolidayToday()) {
            scrollView.setVisibility(View.INVISIBLE);
            headingView.setVisibility(View.INVISIBLE);
            noclass.setVisibility(View.VISIBLE);
        } else {
            scrollView.setVisibility(View.VISIBLE);
            headingView.setVisibility(View.VISIBLE);
            noclass.setVisibility(View.INVISIBLE);
        }
    }

    //returns true if holiday
    private Boolean isHolidayToday(){
        return (readSavedHolidayFromCloud()||
                calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ||
                calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY);
    }

    //returns true if holiday is set explicitly by authority on database
    private Boolean readSavedHolidayFromCloud(){
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_OTHER_HOLIDAY(), MODE_PRIVATE);
        return mSharedPreferences.getBoolean(schemester.getPREF_KEY_OTHER_HOLIDAY(), false);
    }

    private String getWeekdayFromCode(int dayCode){
        switch (dayCode) {
            case 1: return schemester.getStringResource(R.string.sunday);
            case 2: return schemester.getStringResource(R.string.monday);
            case 3: return schemester.getStringResource(R.string.tuesday);
            case 4: return schemester.getStringResource(R.string.wednesday);
            case 5: return schemester.getStringResource(R.string.thursday);
            case 6: return schemester.getStringResource(R.string.friday);
            case 7: return schemester.getStringResource(R.string.saturday);
            default: return "Error";
        }
    }

    private String getMonthFromCode(int getMonthCount){
        switch (getMonthCount) {
            case 0:return schemester.getStringResource(R.string.jan);
            case 1: return schemester.getStringResource(R.string.feb);
            case 2: return schemester.getStringResource(R.string.mar);
            case 3: return schemester.getStringResource(R.string.apr);
            case 4: return schemester.getStringResource(R.string.may);
            case 5: return schemester.getStringResource(R.string.jun);
            case 6: return schemester.getStringResource(R.string.jul);
            case 7: return schemester.getStringResource(R.string.aug);
            case 8: return schemester.getStringResource(R.string.sept);
            case 9: return schemester.getStringResource(R.string.oct);
            case 10: return schemester.getStringResource(R.string.nov);
            case 11: return schemester.getStringResource(R.string.dec);
            default: return "Error";
        }
    }

    /**
     *Reads semester from database and sets to textView.
     * Following parameters are database hierarchy names
     * @param source: collection  (global info)
     * @param doc: document     (semester)
     * @param year: course year of user
     */
    private void setSemester(String source, String doc,final String year) {
        if (getLoginStatus()) {
            db.collection(source).document(doc)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (Objects.requireNonNull(document).exists()) {
                                    semestertxt.setText(Objects.requireNonNull(document.get(year)).toString());
                                } else {
                                    schemester.toasterShort("Unable to read");
                                }
                            }
                        }
                    });
        }
    }

    //highlights current time period in main view
    private void highlightCurrentPeriod(){
        int i = 0, s = 0;
        if(checkPeriod(getStringResource(timeStringResource[0]), getStringResource(timeStringResource[1]))){
            p[i].setTextColor(getResources().getColor(R.color.white));
            p[i].setBackgroundResource(R.drawable.roundactivetimecontainer);
            return;
        } else if(checkPeriod(getStringResource(timeStringResource[9]),"23:59:59")) {     //after day is over
            int d = 0;
            while (d<9) {
                p[d].setTextColor(getResources().getColor(R.color.white));
                p[d].setBackgroundResource(R.drawable.roundtimeovercontainer);
                ++d;
            }
            return;
        } else if(checkPeriod("00:00:00",getStringResource(timeStringResource[0]))) {   //during night
            int n = 0;
            while (n<9) {
                p[n].setBackgroundResource(R.drawable.roundcontainerbox);
                p[n].setTextColor(getResources().getColor(R.color.white));
                ++n;
            }
            return;
        } else {      //checking period during work hours and assigning 's'
            int k = 0;
            while (k<9){
                if(checkPeriod(getStringResource(timeStringResource[k]),getStringResource(timeStringResource[k+1]))){
                    s = k;
                    k=9;
                } else {
                    ++k;
                }
            }
        }
        if(s>i) {
            while (i < s) {     //work hours highlighter
                p[i].setTextColor(getResources().getColor(R.color.white));
                p[i].setBackgroundResource(R.drawable.roundtimeovercontainer);
                p[s].setBackgroundResource(R.drawable.roundactivetimecontainer);
                p[s].setTextColor(getResources().getColor(R.color.white));
                ++i;
            }
        }
        i=i+1;
        while (i<9){    //upcoming period highlighter
            p[i].setTextColor(getResources().getColor(R.color.white));
            p[i].setBackgroundResource(R.drawable.roundcontainerbox);
            ++i;
        }
        
    }


    private String[] getAdditionalInfo() {
        String[] CCY = new String[3];
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.PREF_HEAD_ADDITIONAL_INFO, MODE_PRIVATE);
        CCY[0] = mSharedPreferences.getString(schemester.PREF_KEY_COLLEGE, "");
        CCY[1] = mSharedPreferences.getString(schemester.getPREF_KEY_COURSE(), "");
        CCY[2] = mSharedPreferences.getString(schemester.getPREF_KEY_YEAR(), "");
        return CCY;
    }

    /**
     * checks current time between provided time slot and returns true/false accordingly
     * @param begin: beginning time
     * @param end: ending time
     * @return: true if current time lies between begin and end
     */
    @SuppressLint("SimpleDateFormat")
    private Boolean checkPeriod(String begin, String end){
        String currentTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
        SimpleDateFormat parser = new SimpleDateFormat("HH:mm:ss");
        try {
            Date start = parser.parse(begin);
            Date finish = parser.parse(end);
            Date userTime = parser.parse(currentTime);
            assert userTime != null;
            if (userTime.after(start) && userTime.before(finish)) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * reads schedule from database and sets textView accordingly
     * @param source: collection source (college)
     * @param course: document course (course)
     * @param year: collection year
     * @param weekday: document today's day
     */
    private void readDatabase(String source, String course, String year, String weekday){
            db.collection(source).document(course).collection(year).document(weekday.toLowerCase())
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
                                        c[i].setText(document.getString(pKey[i]));
                                        ++i;
                                    }
                                }
                            }
                        }
                    });
    }

    //uploads user type - teacher or student - to database (personally  identifiable)
    private void storeUserDefinition(String pos, String uid){
        Map<String, Object> data = new HashMap<>();
        data.put(schemester.getFIELD_USER_DEFINITION(), pos);
        db.collection(schemester.getCOLLECTION_USERBASE()).document(uid)
                .update(data);
    }

    //email stored on device
    private String getStoredEmail(){
        String cred;
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_CREDENTIALS(), MODE_PRIVATE);
        cred =  mSharedPreferences.getString(schemester.getPREF_KEY_EMAIL(), "");
        return cred;
    }
    public String getStringResource(int resId){
        return getResources().getString(resId);
    }
    
    //storage permission requestor to store apk on update availability
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
    }

    //returns condition of permission grant
    private boolean storagePermissionGranted(){
        return (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED));
    }

    private Boolean getLoginStatus(){
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_LOGIN_STAT(), MODE_PRIVATE);
        return mSharedPreferences.getBoolean(schemester.getPREF_KEY_LOGIN_STAT(), false);
    }

    private void storeThemeStatus(int themechoice){
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putInt(schemester.getPREF_KEY_THEME(), themechoice);
        mEditor.apply();
    }

    public void setAppTheme() {
        SharedPreferences mSharedPreferences = this.getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE);
        switch (mSharedPreferences.getInt(schemester.getPREF_KEY_THEME(), 0)) {
            case ApplicationSchemester.CODE_THEME_INCOGNITO: setTheme(R.style.IncognitoTheme); break;
            case ApplicationSchemester.CODE_THEME_DARK: setTheme(R.style.DarkTheme);break;
            case ApplicationSchemester.CODE_THEME_LIGHT:
            default:setTheme(R.style.AppTheme);
        }
    }

    public int getThemeStatus(){
        SharedPreferences mSharedPreferences = this.getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE);
        return mSharedPreferences.getInt(schemester.getPREF_KEY_THEME(), 0);
    }

    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
    private int getTimeFormat() {
        SharedPreferences mSharedPreferences = this.getSharedPreferences("schemeTime", MODE_PRIVATE);
        return mSharedPreferences.getInt("format", 24);
    }
    public boolean isInternetAvailable() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException | InterruptedException e) { e.printStackTrace(); }
        return false;
    }
}
