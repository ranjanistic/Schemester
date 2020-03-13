package org.timetable.schemester;

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
import android.os.Build;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

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

@TargetApi(Build.VERSION_CODES.Q)
public class FullScheduleActivity extends AppCompatActivity {
    private ApplicationSchemester schemester;
    TextView email, roll, semester, versionNameText;
    private Button logoutBtn;
    private TextView[] p = new TextView[9],       //period objects
                c = new TextView[9],    //class name text view objects
            ccyTextView = new TextView[3];
    private Button[] dayBtn = new Button[5];
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private View settingsView, aboutView;
    private ImageButton setting, about, git, dml, website, fullSettings, updateCheck, notice,chat, incognito;
    private NestedScrollView dayScheduleNested;       //common view for both orientations
    HorizontalScrollView horizontalScrollView;
    private CustomLoadDialogClass customLoadDialogClass;
    CustomDownloadLoadDialog customDownloadLoadDialog;
    CustomConfirmDialogClass customConfirmDialogClassVerification;
    Window window;
    Calendar calendar;
     String websiteLink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        schemester = (ApplicationSchemester) this.getApplication();
        super.onCreate(savedInstanceState);
        setAppTheme();
        setContentView(R.layout.activity_full_schedule);
        storeLoginStatus(true);
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
        dayScheduleNested= findViewById(R.id.weekdayplanview);
        incognito = findViewById(R.id.incognitoBtn);
        versionNameText = findViewById(R.id.versionCodeName);
        about = findViewById(R.id.aboutbtn);
        settingsView = findViewById(R.id.settingview);
        aboutView = findViewById(R.id.aboutview);
        notice = findViewById(R.id.noticebutton);
        chat = findViewById(R.id.chatButton);

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
        ccyTextView[0] = findViewById(R.id.collegeTextSetting);
        ccyTextView[1] = findViewById(R.id.courseTextSetting);
        ccyTextView[2] = findViewById(R.id.yearTextSetting);
        email = findViewById(R.id.emailtextsetting);
        roll = findViewById(R.id.rolltextsetting);
        setting = findViewById(R.id.settingbtn);
        logoutBtn = findViewById(R.id.logout);
        fullSettings = findViewById(R.id.fullsettingsbtn);
        git = findViewById(R.id.githubbtn);
        updateCheck = findViewById(R.id.checkupdateBtn);
        website = findViewById(R.id.websiteBtn);
        dml = findViewById(R.id.dmlabs);
        schemester.imageButtonLongPressToast(notice,"Notice board");
        schemester.imageButtonLongPressToast(chat,"Chat Room");
        schemester.imageButtonLongPressToast(setting,"Quick settings");
        schemester.imageButtonLongPressToast(about,"About");
        schemester.imageButtonLongPressToast(incognito,"Go anonymous");
        schemester.imageButtonLongPressToast(fullSettings, "Full settings");
        schemester.imageButtonLongPressToast(updateCheck,"Check for updates");
        schemester.imageButtonLongPressToast(website,"Visit online");
    }


    private void setThemeConsequences(){
        if (getThemeStatus() == ApplicationSchemester.CODE_THEME_INCOGNITO) {
            window.setStatusBarColor(schemester.getColorResource(R.color.black));
            window.setNavigationBarColor(schemester.getColorResource(R.color.black));
            setIncognitoMode(true);
        } else if (getThemeStatus() == ApplicationSchemester.CODE_THEME_DARK) {
            window.setStatusBarColor(schemester.getColorResource(R.color.spruce));
            window.setNavigationBarColor(schemester.getColorResource(R.color.spruce));
            setIncognitoMode(false);
        } else {
            window.setStatusBarColor(schemester.getColorResource(R.color.blue));
            window.setNavigationBarColor(schemester.getColorResource(R.color.blue));
            setIncognitoMode(false);
        }
    }
    private void setIncognitoMode(Boolean isIncognito){
        if(isIncognito){
            incognito.setImageResource(R.drawable.ic_usericon);
            logoutBtn.setVisibility(View.GONE);
            email.setText(schemester.getStringResource(R.string.anonymous));
            roll.setText(schemester.getStringResource(R.string.anonymous));
        } else {
            incognito.setImageResource(R.drawable.ic_icognitoman);
            logoutBtn.setVisibility(View.VISIBLE);
            email.setText(getCredentials()[0]);
            roll.setText(getCredentials()[1]);
        }
    }

    private void setListenersAndDefaultViews(){
        schemester.setCollegeCourseYear(getAdditionalInfo()[0], getAdditionalInfo()[1], getAdditionalInfo()[2]);
        versionNameText.setText(ApplicationSchemester.versionName);
        calendar = Calendar.getInstance(TimeZone.getDefault());
        //by default, settings and about pages not shown
        settingsView.setVisibility(View.GONE);
        aboutView.setVisibility(View.GONE);

        String[][] ccyArray = new String[][]{getResources().getStringArray(R.array.college_array),
                getResources().getStringArray(R.array.course_array),
                getResources().getStringArray(R.array.year_array)},
                ccyCodeArray = new String[][]{
                        getResources().getStringArray(R.array.college_code_array),
                        getResources().getStringArray(R.array.course_code_array),
                        getResources().getStringArray(R.array.year_code_array)
                };

        int j = 0;
        while(j<3) {
            int i = 0;
            while (i < ccyArray[j].length) {
                if (Objects.equals(getAdditionalInfo()[j], ccyCodeArray[j][i])) {
                    ccyTextView[j].setText(ccyArray[j][i]);
                }
                ++i;
            }
            ++j;
        }
        if(isLandscape()) {
            for (int d = 0;d<5;++d) {
                dayBtn[d].setText(getResources().getStringArray(R.array.weekdays)[d+1]);
            }
        } else {
            for (int d = 0;d<5;++d) {
                dayBtn[d].setText(getResources().getStringArray(R.array.weekdays_trimmed)[d+1]);
            }
        }
        getWebsiteLinkFromDatabase();

        incognito.setOnClickListener(view -> startActivity(new Intent(FullScheduleActivity.this, ModeOfConduct.class)));

        git.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(schemester.getStringResource(R.string.ranjanistic_github)))));

        website.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(websiteLink))));

        dml.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(schemester.getStringResource(R.string.darkmodelabs_github)))));

        fullSettings.setOnClickListener(view -> startActivity(new Intent(FullScheduleActivity.this, Preferences.class)));


        updateCheck.setOnClickListener(view -> {
            customLoadDialogClass.show();
            readVersionCheckUpdate();
        });

        //TODO: Notice board  and chat room feature
        notice.setOnClickListener(view -> {
            schemester.toasterLong(schemester.getStringResource(R.string.under_construction_message));
/*                Intent nIntent = new Intent(FullScheduleActivity.this, NoticeBoard.class);
            startActivity(nIntent);
*/
        });
        chat.setOnClickListener(view -> {
            if(!checkIfEmailVerified()) {
                customConfirmDialogClassVerification.show();
            }else if(getThemeStatus() == ApplicationSchemester.CODE_THEME_INCOGNITO){
                startActivity(new Intent(FullScheduleActivity.this, ModeOfConduct.class));
            } else {
                schemester.toasterLong(schemester.getStringResource(R.string.under_construction_message));
//                    startActivity( new Intent(FullScheduleActivity.this, ChatRoomActivity.class));
            }
        });
        setting.setOnClickListener(view -> {
            clearAllWeekDayButtonSelection();
            readAndSetSemesterFromDatabase(schemester.getCOLLECTION_GLOBAL_INFO(),schemester.getDOCUMENT_GLOBAL_SEMESTER(),schemester.getCOLLECTION_YEAR_CODE());
            dayScheduleNested.setVisibility(View.GONE);
            settingsView.setVisibility(View.VISIBLE);
            aboutView.setVisibility(View.GONE);
        });
        about.setOnClickListener(view -> {
            clearAllWeekDayButtonSelection();
            dayScheduleNested.setVisibility(View.GONE);
            settingsView.setVisibility(View.GONE);
            aboutView.setVisibility(View.VISIBLE);
        });
        logoutBtn.setOnClickListener(view -> Snackbar.make(view, schemester.getStringResource(R.string.confirm_logout_text), 5000)
               .setBackgroundTint(schemester.getColorResource(R.color.dead_blue))
               .setTextColor(schemester.getColorResource(R.color.white))
               .setAction(schemester.getStringResource(R.string.logout), view1 -> {
                   storeLoginStatus(false);
                   logoutCurrentUser();
               }).setActionTextColor(schemester.getColorResource(R.color.yellow))
               .show());

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
            //noinspection MagicConstant
            if(dayValues[k] == calendar.get(Calendar.DAY_OF_WEEK)){
                readDatabase(schemester.getCOLLECTION_COLLEGE_CODE(),schemester.getDOCUMENT_COURSE_CODE(),schemester.getCOLLECTION_YEAR_CODE(),
                        getResources().getStringArray(R.array.weekday_key)[k+1].toLowerCase());
                dayBtn[k].setBackgroundResource(R.drawable.leftroundbtnselected);
                dayBtn[k].setTextColor(schemester.getColorResource(R.color.blue));
            }
            ++k;
        }
        if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ){
            readDatabase(schemester.getCOLLECTION_COLLEGE_CODE(),schemester.getDOCUMENT_COURSE_CODE(),
                    schemester.getCOLLECTION_YEAR_CODE(),getResources().getStringArray(R.array.weekday_key)[1].toLowerCase());
            dayBtn[0].setBackgroundResource(R.drawable.leftroundbtnselected);
            dayBtn[0].setTextColor(schemester.getColorResource(R.color.blue));
        }
    }

    private void setWeekDayBtnClickListener() {
        for (int di = 0; di < 5; di++) {
            final int finalDI = di;
            dayBtn[di].setOnClickListener(view -> {
                setLoadingTextView();
                alertForInternetDisability();
                scrollTop();
                dayScheduleNested.setVisibility(View.VISIBLE);
                settingsView.setVisibility(View.GONE);
                aboutView.setVisibility(View.GONE);
                readDatabase(schemester.getCOLLECTION_COLLEGE_CODE(), schemester.getDOCUMENT_COURSE_CODE(),
                        schemester.getCOLLECTION_YEAR_CODE(), getResources().getStringArray(R.array.weekday_key)[finalDI+1].toLowerCase());
                int j = 0;
                while(j<5) {
                    if(finalDI == j) {
                        dayBtn[j].setBackgroundResource(R.drawable.leftroundbtnselected);
                        dayBtn[j].setTextColor(schemester.getColorResource(R.color.blue));
                    } else {
                        dayBtn[j].setBackgroundResource(R.drawable.leftroundbtn);
                        dayBtn[j].setTextColor(schemester.getColorResource(R.color.white));
                    }
                    ++j;
                }
            });
        }
    }
    private void clearAllWeekDayButtonSelection(){
        int i = 0;
        while(i<5) {
            dayBtn[i].setBackgroundResource(R.drawable.leftroundbtn);
            dayBtn[i].setTextColor(schemester.getColorResource(R.color.white));
            ++i;
        }
    }
    private void getWebsiteLinkFromDatabase(){
        db.collection(schemester.getCOLLECTION_APP_CONFIGURATION()).document(schemester.getDOCUMENT_LINKS())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (Objects.requireNonNull(document).exists()) {
                            websiteLink = document.getString(schemester.getFIELD_WEBSITE());
                        }
                    }
                });
    }


    private String[] getAdditionalInfo() {
        String[] CCY = new String[3];
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_ADDITIONAL_INFO(), MODE_PRIVATE);
        CCY[0] = mSharedPreferences.getString(schemester.getPREF_KEY_COLLEGE(), null);
        CCY[1] = mSharedPreferences.getString(schemester.getPREF_KEY_COURSE(), null);
        CCY[2] = mSharedPreferences.getString(schemester.getPREF_KEY_YEAR(), null);
        return CCY;
    }

    private void sendVerificationEmail() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        Objects.requireNonNull(user).sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        schemester.toasterLong(schemester.getStringResource(R.string.confirmation_email_sent_text));
                        logoutCurrentUser();
                    }
                })
                .addOnFailureListener(e -> schemester.toasterLong(schemester.getStringResource(R.string.check_connection_try_again)));
    }
    private Boolean checkIfEmailVerified() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).isEmailVerified();
    }
    
    //to set time display of period according to chosen format
    private void setTimeFormat(int tFormat){
        int i = 0;
        if(tFormat == 12) {
            while (i < 9) {
                p[i].setText(getResources().getStringArray(R.array.periods_12)[i]);
                ++i;
            }
        } else {
            while (i < 9) {
                p[i].setText(getResources().getStringArray(R.array.periods_24)[i]);
                ++i;
            }
        }
    }

    private int getTimeFormat() {
        return getSharedPreferences(schemester.getPREF_HEAD_TIME_FORMAT(), MODE_PRIVATE)
                .getInt(schemester.getPREF_KEY_TIME_FORMAT(), 24);
    }

    //this reads semester name from database and sets in semester text view
    private void readAndSetSemesterFromDatabase(String source, String course,final String year) {
        db.collection(source).document(course)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (Objects.requireNonNull(document).exists()) {
                            semester.setText(Objects.requireNonNull(document.get(year)).toString());
                        } else { schemester.toasterLong(schemester.getStringResource(R.string.unable_to_read)); }
                    }
                });
    }

    //gets locally stored credentials during login/register
    private String[] getCredentials(){
        String[] cred = new String[2];
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_CREDENTIALS(), MODE_PRIVATE);
        cred[0] =  mSharedPreferences.getString(schemester.getPREF_KEY_EMAIL(), null);
        cred[1] =  mSharedPreferences.getString(schemester.getPREF_KEY_ROLL(), null);
        return cred;
    }
    
    //reads period details from database and sets to their resp. text views
    private void readDatabase(String source, String course, String year, String weekday){
        db.collection(source).document(course).collection(year).document(weekday)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int i = 0;
                        DocumentSnapshot document = task.getResult();
                        if (Objects.requireNonNull(document).exists()) {
                            while(i<9) {
                                c[i].setText(document.getString(getResources().getStringArray(R.array.period_key_array)[i]));
                                ++i;
                            }
                        } else schemester.toasterLong(schemester.getStringResource(R.string.server_error_try_reinstall));
                    } else { schemester.toasterLong(schemester.getStringResource(R.string.internet_error)); }
                });
    }

    //logs out the user
    private void logoutCurrentUser(){
        if(isNetworkConnected()) {
            FirebaseAuth.getInstance().signOut();
            schemester.toasterLong(schemester.getStringResource(R.string.logged_out));
            startActivity(new Intent(FullScheduleActivity.this, PositionActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP)
            );
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_from_right);
            finish();
        } else { schemester.toasterShort(schemester.getStringResource(R.string.connect_to_internet)); }
    }
    
    private void storeLoginStatus(Boolean logged){
        getSharedPreferences(schemester.getPREF_HEAD_LOGIN_STAT(), MODE_PRIVATE).edit()
                .putBoolean(schemester.getPREF_KEY_LOGIN_STAT(), logged).apply();
    }

    private void scrollTop(){
        if(!isLandscape()) {
            dayScheduleNested.smoothScrollTo(0,View.FOCUS_UP);
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
                    .addOnCompleteListener(task -> {
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
                if (isCompleted)
                    showPackageAlert();
                else {
                    customLoadDialogClass.hide();
                    schemester.toasterShort(schemester.getStringResource(R.string.download_interrupted));
                }
            }
        });
        customDownloadLoadDialog.show();
    }
    private void setLoadingTextView() {
         int i = 0;
         while (i < 9) { c[i].setText(getResources().getString(R.string.loading));i++; }
    }
    private void showPackageAlert(){
        CustomAlertDialog downloadFinishAlert = new CustomAlertDialog(FullScheduleActivity.this, new OnDialogAlertListener() {
            @Override
            public void onDismiss() {}
            @Override
            public String onCallText() { return schemester.getStringResource(R.string.download_completed); }
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
    public void setAppTheme() {
        switch (getApplication().getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE)
                .getInt(schemester.getPREF_KEY_THEME(), 0)) {
            case ApplicationSchemester.CODE_THEME_INCOGNITO: setTheme(R.style.IncognitoTheme);break;
            case ApplicationSchemester.CODE_THEME_DARK: setTheme(R.style.BlueDarkTheme);break;
            case ApplicationSchemester.CODE_THEME_LIGHT:
                default:setTheme(R.style.BlueLightTheme);
        }
    }
    private int getThemeStatus() {
        return getApplication().getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE)
                .getInt(schemester.getPREF_KEY_THEME(), 0);
    }
    private boolean isLandscape() { return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE; }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm != null ? cm.getActiveNetworkInfo() : null) != null && cm.getActiveNetworkInfo().isConnected();
    }
    private void alertForInternetDisability() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = Objects.requireNonNull(connectivityManager).getActiveNetworkInfo();
        if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())){
            schemester.toasterLong(schemester.getStringResource(R.string.internet_error));
        }
    }
}
