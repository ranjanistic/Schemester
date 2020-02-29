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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.timetable.schemester.dialog.CustomAlertDialog;
import org.timetable.schemester.dialog.CustomConfirmDialogClass;
import org.timetable.schemester.dialog.CustomDownloadLoadDialog;
import org.timetable.schemester.dialog.DurationDetailsDialog;
import org.timetable.schemester.listener.DurationDetailDialogListener;
import org.timetable.schemester.listener.OnDialogAlertListener;
import org.timetable.schemester.listener.OnDialogConfirmListener;
import org.timetable.schemester.listener.OnDialogDownloadLoadListener;

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


public class MainActivity extends AppCompatActivity{
    ApplicationSchemester schemester;
    private TextView semesterText, noClassText, day, month, time;
    private TextView[] c = new TextView[9], p = new TextView[9];
    private TextView loginIdOnDrawer;
    private Button date;
    private ImageView noClassImage;
    private ImageButton drawerArrow, switchThemeBtn;
    private LinearLayout headingView, settingTab, scheduleTab;
    private LinearLayout[] duration = new LinearLayout[9];
    private ScrollView scrollView;
    private Calendar calendar;
    private Animation hide, show, fadeOn, fadeOff;
    private ReadClassFromDatabaseTask mReadClassFromDatabaseTask;
    public Activity mainact;
    public static boolean isCreated = false;
    private HighlighterTask mHighlighterTask;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private int[]  periodView = {
            R.id.periodMain1, R.id.periodMain2, R.id.periodMain3, R.id.periodMain4, R.id.periodMain5,
            R.id.periodMain6, R.id.periodMain7, R.id.periodMain8, R.id.periodMain9,
    }, classView = {
            R.id.classMain1, R.id.classMain2, R.id.classMain3, R.id.classMain4, R.id.classMain5,
            R.id.classMain6, R.id.classMain7, R.id.classMain8, R.id.classMain9,
    }, durationView = {
            R.id.duration1,R.id.duration2,R.id.duration3,R.id.duration4,R.id.duration5,
            R.id.duration6,R.id.duration7,R.id.duration8,R.id.duration9,
    };
    int[] timeStringResource = {
        R.string.time1, R.string.time2, R.string.time3, R.string.time4, R.string.time5,
                R.string.time6, R.string.time7, R.string.time8, R.string.time9, R.string.time10,
    }, pkeyResource = {R.string.p1,R.string.p2,R.string.p3,R.string.p4
            ,R.string.p5,R.string.p6,R.string.p7,R.string.p8,R.string.p9
    };
    private LinearLayout bottomDrawer;
    private checkUpdate update;     //update checker asyncTask class
    private Window window;
    private BottomSheetBehavior bottomSheetBehavior;
    private String COLLECTION_GLOBAL_INFO, DOCUMENT_GLOBAL_SEMESTER,
            COLLECTION_COLLEGE_CODE , DOCUMENT_COURSE_CODE , COLLECTION_YEAR_CODE;
    DurationDetailsDialog durationDetailsDialog;
    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        schemester = (ApplicationSchemester) this.getApplication();
        setAppTheme();
        mainact = this;
        isCreated = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        assignDefaultValues();
        setWindowDecorDefaults();
        storeUserDefinition(readUserPosition(),getStoredEmail());
        setViews();         //assigning all views to their respective objects
        setBottomSheetFeature();            //setting bottom drawer behaviour
        runTimeDisplayOnBottomSheet();      //display current time
        setThemeConsequencesAndActions();      //set navigation bar color and theme button listener
        setButtonClickListeners();

        //check holiday and display accordingly
        setHolidayViewIfHoliday();

        //initializing main schedule update task
        mReadClassFromDatabaseTask = new ReadClassFromDatabaseTask();
        mHighlighterTask = new HighlighterTask();
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
            if(!aBoolean) {
                schemester.toasterLong(schemester.getStringResource(R.string.internet_error));
            }
            else {
                if(userWantsUpdateNotification()) {
                    //check for updates task
                    update = new checkUpdate();
                    update.execute();
                }
            }
            super.onPostExecute(aBoolean);
        }
    }

    private void assignDefaultValues(){
        schemester.setCollegeCourseYear(getAdditionalInfo()[0],getAdditionalInfo()[1],getAdditionalInfo()[2]);
        COLLECTION_GLOBAL_INFO = schemester.getCOLLECTION_GLOBAL_INFO();
        DOCUMENT_GLOBAL_SEMESTER = schemester.getDOCUMENT_GLOBAL_SEMESTER();
        COLLECTION_COLLEGE_CODE = getAdditionalInfo()[0];
        DOCUMENT_COURSE_CODE = getAdditionalInfo()[1];
        COLLECTION_YEAR_CODE = getAdditionalInfo()[2];
        calendar = Calendar.getInstance(TimeZone.getDefault());
    }
    private void setWindowDecorDefaults(){
        window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setNavigationBarColor(this.getResources().getColor(R.color.dull_white));
    }

    private void setViews(){
        scrollView = findViewById(R.id.scrollView);
        headingView = findViewById(R.id.period_view);
        noClassText = findViewById(R.id.noclasstext);
        noClassImage = findViewById(R.id.noclassImage);
        settingTab = findViewById(R.id.settingTab);
        scheduleTab = findViewById(R.id.fullScheduleTab);
        semesterText = findViewById(R.id.sem_text);
        switchThemeBtn = findViewById(R.id.switchThemeMain);
        time = findViewById(R.id.present_time);
        drawerArrow = findViewById(R.id.drawerarrow);
        int k = 0;
        while(k<9) {
            p[k] = findViewById(periodView[k]);
            c[k] = findViewById(classView[k]);
            duration[k] = findViewById(durationView[k]);
            ++k;
        }
        bottomDrawer = findViewById(R.id.bottom_drawer);
        date = findViewById(R.id.present_date);
        day = findViewById(R.id.weekday_text);
        month = findViewById(R.id.month_text);
        loginIdOnDrawer = findViewById(R.id.drawerLoginID);
        schemester.imageLongPressToast(noClassImage,"Weekend & Chill?");
        schemester.textLongPressToast(date,"Today's date");
        schemester.imageButtonLongPressToast(switchThemeBtn,"Touch to renovate");
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
        String[] cred = new String[2];
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
                    time.setText(new SimpleDateFormat(getStringResource(R.string.time_format_hhmm_ampm)).format(new Date()));
                } else{
                    time.setText(new SimpleDateFormat(getStringResource(R.string.time_format_hhmm)).format(new Date()));
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
        if(getThemeStatus() == ApplicationSchemester.CODE_THEME_DARK){
            switchThemeBtn.setBackgroundResource(R.drawable.ic_moonsmallicon);
            noClassImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_nightfullmoonbeachgradient));
        } else if(getThemeStatus() == ApplicationSchemester.CODE_THEME_INCOGNITO) {
            switchThemeBtn.setBackgroundResource(R.drawable.ic_icognitoman);
            noClassImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_nightfullmoonbeachgradient));
        }else{
            switchThemeBtn.setBackgroundResource(R.drawable.ic_suniconsmall);
            noClassImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_redsunsetbeachgradient));
            storeThemeStatus(ApplicationSchemester.CODE_THEME_LIGHT);
        }
    }

    private void setButtonClickListeners(){
        final Intent restart = new Intent(MainActivity.this, MainActivity.class);
        switchThemeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchThemeBtn.startAnimation(hide);
                switchThemeBtn.startAnimation(fadeOff);
                if(getThemeStatus() == ApplicationSchemester.CODE_THEME_LIGHT){
                    storeThemeStatus(ApplicationSchemester.CODE_THEME_DARK);
                    startActivity(restart);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    switchThemeBtn.startAnimation(show);
                    switchThemeBtn.startAnimation(fadeOn);
                } else if(getThemeStatus() == ApplicationSchemester.CODE_THEME_DARK){
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
                    storeThemeStatus(ApplicationSchemester.CODE_THEME_DARK);
                    startActivity(restart);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    switchThemeBtn.startAnimation(show);
                    switchThemeBtn.startAnimation(fadeOn);
                }
            }
        });
        scheduleTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setActivityChangerButtonsDisabled(true);
                mReadClassFromDatabaseTask.cancel(true);
                Intent i = new Intent(MainActivity.this, FullScheduleActivity.class);
                startActivity(i);
            }
        });

        settingTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setActivityChangerButtonsDisabled(true);
                mReadClassFromDatabaseTask.cancel(true);
                mHighlighterTask.cancel(true);
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
        int d;
        for(d = 0;d<9;++d) {
            final int finalD = d;
            duration[d].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    durationDetailsDialog = new DurationDetailsDialog(MainActivity.this, new DurationDetailDialogListener() {
                        @Override
                        public String onCallClassName() { return c[finalD].getText().toString(); }
                        @Override
                        public String onCallClassDuration() { return p[finalD].getText().toString(); }
                        @Override
                        public String classLocation() { return getStringResource(R.string.n_a); }
                        @Override
                        public Boolean classIsOn() { return null; }
                    });
                    durationDetailsDialog.show();
                }
            });
        }
        d=0;
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
                                        schemester.toasterLong(getStringResource(R.string.update_available));
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
                                                            return getStringResource(R.string.storage_permit_required);
                                                        }
                                                        @Override
                                                        public String onCallSub() {
                                                            return getStringResource(R.string.storage_permit_request_text);
                                                        }
                                                    });
                                                    permissionDialog.show();
                                                }
                                            }
                                            @Override
                                            public String onCallText() {
                                                return getStringResource(R.string.an_update_is_available);
                                            }
                                            @Override
                                            public String onCallSub() {
                                                return getStringResource(R.string.your_app_ver_colon) +
                                                        ApplicationSchemester.versionName + "\n"+
                                                        getStringResource(R.string.new_ver_colon) + vname +"\n\n"+
                                                        getStringResource(R.string.update_persuade_text) +"\n"+ 
                                                        getStringResource(R.string.confirm_to_download); }
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
                    schemester.toasterShort(getStringResource(R.string.download_interrupted));
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
                return getStringResource(R.string.download_completed);
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
              setTimeFormatInMainSchedule(getTimeFormat());
              if(mHighlighterTask.isCancelled()) {
                  mHighlighterTask = new HighlighterTask();
                  mHighlighterTask.execute();
              } else {
                  mHighlighterTask.cancel(true);
                  mHighlighterTask = new HighlighterTask();
                  mHighlighterTask.execute();
              }
              if(mReadClassFromDatabaseTask.isCancelled()) {
                  mReadClassFromDatabaseTask = new ReadClassFromDatabaseTask();
                  mReadClassFromDatabaseTask.execute();
              } else {
                  mReadClassFromDatabaseTask.cancel(true);
                  mReadClassFromDatabaseTask = new ReadClassFromDatabaseTask();
                  mReadClassFromDatabaseTask.execute();
              }
          }
        super.onStart();
    }

    private class HighlighterTask extends AsyncTask<Void,Void,Void>{
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
                    new HighlighterTask().execute();
                }
            }, 100);
        }
    }

    @Override
    protected void onDestroy() {
        mReadClassFromDatabaseTask.cancel(true);
        mHighlighterTask.cancel(true);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mReadClassFromDatabaseTask.cancel(true);
        mHighlighterTask.cancel(true);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setActivityChangerButtonsDisabled(false);
    }

    @Override
    protected void onStop() {
        mReadClassFromDatabaseTask.cancel(true);
        mHighlighterTask.cancel(true);
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
    private void setTimeFormatInMainSchedule(int tFormat){
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
                    mReadClassFromDatabaseTask = new ReadClassFromDatabaseTask();
                    mReadClassFromDatabaseTask.execute();
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
            noClassText.setVisibility(View.VISIBLE);
            noClassImage.setVisibility(View.VISIBLE);
        } else {
            scrollView.setVisibility(View.VISIBLE);
            headingView.setVisibility(View.VISIBLE);
            noClassText.setVisibility(View.INVISIBLE);
            noClassImage.setVisibility(View.INVISIBLE);
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
        String[] dayString = {
                schemester.getStringResource(R.string.sunday),
                schemester.getStringResource(R.string.monday),
                schemester.getStringResource(R.string.tuesday),
                schemester.getStringResource(R.string.wednesday),
                schemester.getStringResource(R.string.thursday),
                schemester.getStringResource(R.string.friday),
                schemester.getStringResource(R.string.saturday),
        };
        for(int m = 1;m<8;++m) {
            if (m==dayCode) {
                return dayString[m-1];
            }
        }
        return "Error";
    }

    private String getMonthFromCode(int getMonthCount){
        String[] monthString = {
         schemester.getStringResource(R.string.jan),
        schemester.getStringResource(R.string.feb),
         schemester.getStringResource(R.string.mar),
         schemester.getStringResource(R.string.apr),
         schemester.getStringResource(R.string.may),
         schemester.getStringResource(R.string.jun),
         schemester.getStringResource(R.string.jul),
         schemester.getStringResource(R.string.aug),
        schemester.getStringResource(R.string.sept),
        schemester.getStringResource(R.string.oct),
         schemester.getStringResource(R.string.nov),
         schemester.getStringResource(R.string.dec)
        };
        for(int m = 0;m<12;++m) {
            if (m==getMonthCount) {
                return monthString[m];
            }
        }
        return "Error";
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
                                    semesterText.setText(Objects.requireNonNull(document.get(year)).toString());
                                } else {
                                    schemester.toasterShort(getStringResource(R.string.unable_to_read));
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
        } else if(checkPeriod(getStringResource(timeStringResource[9]),getStringResource(R.string.one_second_before_new_day))) {     //after day is over
            int d = 0;
            while (d<9) {
                p[d].setTextColor(getResources().getColor(R.color.white));
                p[d].setBackgroundResource(R.drawable.roundtimeovercontainer);
                ++d;
            }
            return;
        } else if(checkPeriod(getStringResource(R.string.zero_seconds_after_new_day),getStringResource(timeStringResource[0]))) {   //during night
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
        String currentTime = new SimpleDateFormat(getStringResource(R.string.time_pattern_hhmmss)).format(new Date());
        SimpleDateFormat parser = new SimpleDateFormat(getStringResource(R.string.time_pattern_hhmmss));
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
                                    int i = 0;
                                    while(i<9) {
                                        if(Objects.equals(document.getString(getStringResource(pkeyResource[i])),"Nothing")){
                                            duration[i].setVisibility(View.GONE);
                                        } else {
                                            c[i].setText(document.getString(getStringResource(pkeyResource[i])));
                                        }
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
    
    //storage permission request to store apk on update availability
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

    private Boolean userWantsUpdateNotification(){
        return this.getSharedPreferences(schemester.getPREF_HEAD_UPDATE_NOTIFY(), MODE_PRIVATE)
                .getBoolean(schemester.getPREF_KEY_UPDATE_NOTIFY(), true);
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
        SharedPreferences mSharedPreferences = this.getSharedPreferences(schemester.getPREF_HEAD_TIME_FORMAT(), MODE_PRIVATE);
        return mSharedPreferences.getInt(schemester.getPREF_KEY_TIME_FORMAT(), 24);
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
