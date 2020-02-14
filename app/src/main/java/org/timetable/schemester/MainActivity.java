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
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity{
    int versionCode = BuildConfig.VERSION_CODE;
    String versionName = BuildConfig.VERSION_NAME;
    private TextView semestertxt, noclass,
            c1,c2,c3,c4,c5,c6,c7,c8,c9,p1,p2,p3,p4,p5,p6,p7,p8,p9,
            day, month, time;
    private TextView[] c = {c1,c2,c3,c4,c5,c6,c7,c8,c9},
    p = {p1,p2,p3,p4,p5,p6,p7,p8,p9};
    static String[] pKey = {"p1","p2","p3","p4","p5","p6","p7","p8","p9"};
    String COLLECTION_GLOBAL_INFO = "global_info",
            DOCUMENT_GLOBAL_SEMESTER = "semester",
            COLLECTION_COLLEGE_CODE , DOCUMENT_COURSE_NAME , COLLECTION_YEAR_CODE;

    private Button date;
    private ImageButton drawerArrow, switchThemeBtn;
    private LinearLayout headingView, settingTab, scheduleTab;
    private ScrollView scrollView;
    private Calendar calendar;
    private Animation hide, show, fadeOn, fadeOff;
    private HighlightUpdatedClassTask mHighlightClassTask;
    public Activity mainact;
    public static boolean isCreated = false;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private int[] periodStringResource12 = {
            R.string.period112, R.string.period212, R.string.period312, R.string.period412, R.string.period512,
            R.string.period612, R.string.period712, R.string.period812, R.string.period912
    }, periodStringResource24 = {
            R.string.period1, R.string.period2, R.string.period3, R.string.period4,
            R.string.period5, R.string.period6, R.string.period7, R.string.period8, R.string.period9
    }, timeStringResource = {
            R.string.time1, R.string.time2, R.string.time3, R.string.time4, R.string.time5,
            R.string.time6, R.string.time7, R.string.time8, R.string.time9, R.string.time10,
    }, periodView = {
            R.id.periodMain1, R.id.periodMain2, R.id.periodMain3, R.id.periodMain4, R.id.periodMain5,
            R.id.periodMain6, R.id.periodMain7, R.id.periodMain8, R.id.periodMain9,
    }, classView = {
            R.id.classMain1, R.id.classMain2, R.id.classMain3, R.id.classMain4, R.id.classMain5,
            R.id.classMain6, R.id.classMain7, R.id.classMain8, R.id.classMain9,
    };
    private LinearLayout bottomDrawer;
    private checkUpdate update;     //update checker asyncTask class
    private Window window;

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setAppTheme(getThemeStatus());
        mainact = this;
        isCreated = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        COLLECTION_COLLEGE_CODE = getAdditionalInfo()[0];
        DOCUMENT_COURSE_NAME = getAdditionalInfo()[1];
        COLLECTION_YEAR_CODE = getAdditionalInfo()[2];
        window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setNavigationBarColor(this.getResources().getColor(R.color.dull_white));
        storeUserDefinition(readUserPosition(),getStoredEmail());
        calendar = Calendar.getInstance(TimeZone.getDefault());

        setViews();         //assigning all views to their respective objects
        setBottomSheetFeature();            //setting bottom drawer behaviour
        runTimeDisplayOnBottomSheet();      //display current time
        setThemeConsequenciesAndActions();      //set navigation bar color and theme button listener
        setButtonClickListeners();      //self explanatory

        //alert internet availability
        if(!isInternetAvailable()){
            Toast.makeText(getApplicationContext(),"Connect to internet for latest details",Toast.LENGTH_LONG).show();
        }

        //initializing main schedule update task
        mHighlightClassTask = new HighlightUpdatedClassTask();

        //check holiday and display accordingly
        setHolidayViewIfHoliday();

        //check for updates task
        update = new checkUpdate();
        update.execute();

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
    }

    private void setBottomSheetFeature(){
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomDrawer);
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
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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
    private void setThemeConsequenciesAndActions(){
        hide = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.gone_centrally);
        show = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.emerge_centrally);
        fadeOn = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadeliton);
        fadeOff= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadelitoff);

        if(isLandscape()){
            if(getThemeStatus() == 101)
                window.setNavigationBarColor(this.getResources().getColor(R.color.white));
            else if(getThemeStatus() == 102)
                window.setNavigationBarColor(this.getResources().getColor(R.color.charcoal));
        } else {
            if(getThemeStatus() == 101)
                window.setNavigationBarColor(this.getResources().getColor(R.color.dull_white));
            else if(getThemeStatus() == 102)
                window.setNavigationBarColor(this.getResources().getColor(R.color.black_overlay));
        }

        final Intent restart = new Intent(MainActivity.this, MainActivity.class);
        if(getThemeStatus() == 102){
            switchThemeBtn.setBackgroundResource(R.drawable.ic_moonsmallicon);
        } else {
            switchThemeBtn.setBackgroundResource(R.drawable.ic_suniconsmall);
            storeThemeStatus(101);
        }

        switchThemeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchThemeBtn.startAnimation(hide);
                switchThemeBtn.startAnimation(fadeOff);
                if(getThemeStatus() == 101){
                    switchThemeBtn.setBackgroundResource(R.drawable.ic_moonsmallicon);
                    storeThemeStatus(102);
                    startActivity(restart);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    switchThemeBtn.startAnimation(show);
                    switchThemeBtn.startAnimation(fadeOn);
                } else if(getThemeStatus() == 102){
                    switchThemeBtn.setBackgroundResource(R.drawable.ic_suniconsmall);
                    storeThemeStatus(101);
                    startActivity(restart);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    switchThemeBtn.startAnimation(show);
                    switchThemeBtn.startAnimation(fadeOn);
                } else {
                    switchThemeBtn.setBackgroundResource(R.drawable.ic_moonsmallicon);
                    storeThemeStatus(102);
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
                if(!mHighlightClassTask.isCancelled()){
                    mHighlightClassTask.cancel(true);
                }
                Intent i = new Intent(MainActivity.this, FullScheduleActivity.class);
                startActivity(i);
            }
        });

        settingTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, Preferences.class);
                startActivity(i);
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
        if(isInternetAvailable()) {
            final String COLLECTION_APP_CONFIGURATION = "appConfig",DOCUMENT_VERSION_CURRENT = "verCurrent",
                    FIELD_VERSION_NAME = "verName", FIELD_DOWNLOAD_LINK = "downlink", FIELD_VERSION_CODE = "verCode";
            db.collection(COLLECTION_APP_CONFIGURATION).document(DOCUMENT_VERSION_CURRENT)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (Objects.requireNonNull(document).exists()) {
                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                    int vcode = Integer.parseInt(Objects.requireNonNull(document.get(FIELD_VERSION_CODE)).toString());
                                    final String vname = document.getString(FIELD_VERSION_NAME);
                                    final String link = document.getString(FIELD_DOWNLOAD_LINK);
                                    if (vcode != versionCode || !Objects.equals(vname, versionName)) {
                                        Toast.makeText(getApplicationContext(), "Update available", Toast.LENGTH_LONG).show();
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
                                                return "Your app version : " + versionName + "\nNew Version : " + vname + "\n\nUpdate to get the latest features and bug fixes. Download will start automatically. \nConfirm to download?";
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
                    Toast.makeText(getApplicationContext(), "Download Interrupted", Toast.LENGTH_SHORT).show();
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
        setSemester(COLLECTION_GLOBAL_INFO,DOCUMENT_GLOBAL_SEMESTER,COLLECTION_YEAR_CODE);
        date.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
        day.setText(getWeekdayFromCode(calendar.get(Calendar.DAY_OF_WEEK)));
        month.setText(getMonthFromCode(calendar.get(Calendar.MONTH)));
          if(!isHolidayToday()) {
              setTimeFormat(getTimeFormat());
              if(mHighlightClassTask.isCancelled()) {
                  mHighlightClassTask = new HighlightUpdatedClassTask();
                  mHighlightClassTask.execute();
              } else {
                  mHighlightClassTask.cancel(true);
                  mHighlightClassTask = new HighlightUpdatedClassTask();
                  mHighlightClassTask.execute();
              }
          }
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        mHighlightClassTask.cancel(true);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mHighlightClassTask.cancel(true);
        super.onPause();
    }

    @Override
    protected void onStop() {
        mHighlightClassTask.cancel(true);
        super.onStop();
    }

    private void storeThemeStatus(int themechoice){
        SharedPreferences mSharedPreferences = getSharedPreferences("schemeTheme", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putInt("themeCode", themechoice);
        mEditor.apply();
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

    //String resource extractor
    private String getStringResource(int res){
        return getResources().getString(res);
    }

    //schedule update task
    private class HighlightUpdatedClassTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            date.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
            day.setText(getWeekdayFromCode(calendar.get(Calendar.DAY_OF_WEEK)));
            month.setText(getMonthFromCode(calendar.get(Calendar.MONTH)));
            setSemester(COLLECTION_GLOBAL_INFO,DOCUMENT_GLOBAL_SEMESTER,COLLECTION_YEAR_CODE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (!isHolidayToday()) {
                readDatabase(COLLECTION_COLLEGE_CODE,DOCUMENT_COURSE_NAME,COLLECTION_YEAR_CODE,getWeekdayFromCode(calendar.get(Calendar.DAY_OF_WEEK)));
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            highlightCurrentPeriod();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mHighlightClassTask = new HighlightUpdatedClassTask();
                    mHighlightClassTask.execute();
                }
            }, 100);
            super.onPostExecute(aVoid);
        }
    }

    //whether user is a teacher or student
    private String readUserPosition(){
        SharedPreferences mSharedPreferences = this.getSharedPreferences("userDefinition", MODE_PRIVATE);
        return mSharedPreferences.getString("position", "");
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
        SharedPreferences mSharedPreferences = getSharedPreferences("otherHoliday", MODE_PRIVATE);
        return mSharedPreferences.getBoolean("holiday", false);
    }
    
    private String getWeekdayFromCode(int daycode){
        switch (daycode) {
            case 1: return getStringResource(R.string.sunday);
            case 2: return getStringResource(R.string.monday);
            case 3: return getStringResource(R.string.tuesday);
            case 4: return getStringResource(R.string.wednesday);
            case 5: return getStringResource(R.string.thursday);
            case 6: return getStringResource(R.string.friday);
            case 7: return getStringResource(R.string.saturday);
            default: return "Error";
        }
    }
    
    private String getMonthFromCode(int getMonthCount){
        switch (getMonthCount) {
            case 0:return getStringResource(R.string.jan);
            case 1: return getStringResource(R.string.feb);
            case 2: return getStringResource(R.string.mar);
            case 3: return getStringResource(R.string.apr);
            case 4: return getStringResource(R.string.may);
            case 5: return getStringResource(R.string.jun);
            case 6: return getStringResource(R.string.jul);
            case 7: return getStringResource(R.string.aug);
            case 8: return getStringResource(R.string.sept);
            case 9: return getStringResource(R.string.oct);
            case 10: return getStringResource(R.string.nov);
            case 11: return getStringResource(R.string.dec);
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
                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                    semestertxt.setText(Objects.requireNonNull(document.get(year)).toString());
                                } else {
                                    Log.d(TAG, "Server error in getting semester.");
                                    Toast.makeText(MainActivity.this, "Unable to read", Toast.LENGTH_LONG).show();
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
                d++;
            }
            return;
        } else if(checkPeriod("00:00:00",getStringResource(timeStringResource[0]))) {   //during night
            int d = 0;
            while (d<9) {
                p[d].setBackgroundResource(R.drawable.roundcontainerbox);
                p[d].setTextColor(getResources().getColor(R.color.white));
                d++;
            }
            return;
        } else {      //checking period during work hours and assigning 's'
            int k = 1;
            while (k<10){
                if(checkPeriod(getStringResource(timeStringResource[k]),getStringResource(timeStringResource[k+1]))){
                    s = k;
                    break;
                } else {
                    ++k;
                }
            }
        }
        while (i < s) {     //work hours highlighter
            p[i].setTextColor(getResources().getColor(R.color.white));
            p[i].setBackgroundResource(R.drawable.roundtimeovercontainer);
            p[s].setBackgroundResource(R.drawable.roundactivetimecontainer);
            p[s].setTextColor(getResources().getColor(R.color.white));
            ++i;
        }
        i=i+1;
        while (i<9){    //upcoming period highlighter
            p[i].setTextColor(getResources().getColor(R.color.white));
            p[i].setBackgroundResource(R.drawable.roundcontainerbox);
            ++i;
        }
    }

    /*
    private String returnPeriodDetail(){
        readDatabase(getWeekdayFromCode(Calendar.DAY_OF_WEEK));
        if(checkPeriod("08:30:00", "09:30:00")){
            return c1.getText().toString();
        } else if(checkPeriod("09:30:00", "10:30:00")){
            return c2.getText().toString();
        }else if(checkPeriod("10:30:00", "11:30:00")){
            return c3.getText().toString();
        }else if(checkPeriod("11:30:00","12:30:00")){
            return c4.getText().toString();
        }else if(checkPeriod("12:30:00","13:30:00")){
            return c5.getText().toString();
        }else if(checkPeriod("13:30:00","14:30:00")){
            return c6.getText().toString();
        }else if(checkPeriod("14:30:00","15:30:00")){
            return c7.getText().toString();
        }else if(checkPeriod("16:46:00","16:48:00")){
            return c8.getText().toString();
        }else if(checkPeriod("16:30:00","17:30:00")){
            return c9.getText().toString();
        } else {
            return "All classes over.";
        }
    }

    
    private String returnClassBeginTime(){
        String currently = new SimpleDateFormat("HH:mm:ss").format(new Date());
        if(currently.equals(getResources().getString(R.string.time1))) {
            return currently;
        } else if(currently.equals(getResources().getString(R.string.time2))) {
            return currently;
        } else if(currently.equals(getResources().getString(R.string.time3))) {
            return currently;
        } else if(currently.equals(getResources().getString(R.string.time4))) {
            return currently;
        } else if(currently.equals(getResources().getString(R.string.time5))) {
            return currently;
        } else if(currently.equals(getResources().getString(R.string.time6))) {
            return currently;
        } else if(currently.equals(getResources().getString(R.string.time7))) {
            return currently;
        } else if(currently.equals(getResources().getString(R.string.time8))) {
            return currently;
        } else if(currently.equals(getResources().getString(R.string.time9))) {
            return currently;
        } else {
            return "Not yet.";
        }
    }
*/
    String[] getAdditionalInfo() {
        String[] CCY = {null, null, null};
        SharedPreferences mSharedPreferences = getSharedPreferences("additionalInfo", MODE_PRIVATE);
        CCY[0] = mSharedPreferences.getString("college", "");
        CCY[1] = mSharedPreferences.getString("course", "");
        CCY[2] = mSharedPreferences.getString("year", "");
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
                                        i++;
                                    }
                                } else {
                                    Log.d(TAG, "No such document");
                                    Toast.makeText(MainActivity.this, "Server error.", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
    }

    //uploads user type - teacher or student - to database (personally  identifiable)
    private void storeUserDefinition(String pos, String uid){
        Map<String, Object> data = new HashMap<>();
        data.put("definition", pos);
        db.collection("userbase").document(uid)
                .set(data, SetOptions.merge());
    }

    //email stored on device
    private String getStoredEmail(){
        String cred;
        SharedPreferences mSharedPreferences = getSharedPreferences("credentials", MODE_PRIVATE);
        cred =  mSharedPreferences.getString("email", "");
        return cred;
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
        SharedPreferences mSharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        return mSharedPreferences.getBoolean("loginstatus", false);
    }

    public void setAppTheme(int code) {
        switch (code) {
            case 102: setTheme(R.style.DarkTheme);break;
            case 101:
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
