package org.timetable.schemester;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity{
    TextView semestertxt, noclass;
    TextView c1,c2,c3,c4,c5,c6,c7,c8,c9,p1,p2,p3,p4,p5,p6,p7,p8,p9 ;
    TextView[] c = {c1,c2,c3,c4,c5,c6,c7,c8,c9};
    TextView[] p = {p1,p2,p3,p4,p5,p6,p7,p8,p9 };
    String[] pkey = {"p1","p2","p3","p4","p5","p6","p7","p8","p9"};
    String clg, course,year;
    Button day,date, month;
    ImageButton fullview, drawerArrow;
    int getDate, notificationId = 101;
    String getDay, getMonth;
    LinearLayout linearLayout, headingview, landscapeView, settingtab, scheduletab;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    ScrollView scrollView;
    Calendar calendar;
    updateTask mupdateTask;
    public static Activity mainact;
    public static boolean isCreated = false;
    Context ctx;
    Boolean isHoliday = false;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setAppTheme(getThemeStatus());
        clg = "DBC";
        course = "PHY-H";
        year = "Y2";
        mainact = this;
        isCreated = true;
        super.onCreate(savedInstanceState);
        ctx = this;
        setContentView(R.layout.activity_main);
        final Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setNavigationBarColor(this.getResources().getColor(R.color.dull_white));
        calendar = Calendar.getInstance(TimeZone.getDefault());
        linearLayout = findViewById(R.id.linearLayout);
        scrollView = findViewById(R.id.scrollView);
        headingview = findViewById(R.id.period_view);
        landscapeView = findViewById(R.id.mainLinearlayoutLandscape);
        noclass = findViewById(R.id.noclasstext);
        settingtab = findViewById(R.id.settingTab);
        scheduletab = findViewById(R.id.fullScheduleTab);
        semestertxt = findViewById(R.id.sem_text);
        drawerArrow = findViewById(R.id.drawerarrow);
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

        LinearLayout bottomDrawer = findViewById(R.id.bottom_drawer);
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomDrawer);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setHideable(false);
        LinearLayout drawerPeek = findViewById(R.id.drawerarrowHolder);
        drawerPeek.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);
        bottomSheetBehavior.setPeekHeight(drawerPeek.getMeasuredHeight());
        //bottomSheetBehavior.setPeekHeight();
        //fullview = findViewById(R.id.full_schedule);
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

        p[0] = findViewById(R.id.period1);
        p[1] = findViewById(R.id.period2);
        p[2] = findViewById(R.id.period3);
        p[3] = findViewById(R.id.period4);
        p[4] = findViewById(R.id.period5);
        p[5] = findViewById(R.id.period6);
        p[6] = findViewById(R.id.period7);
        p[7] = findViewById(R.id.period8);
        p[8] = findViewById(R.id.period9);

/*
        c1 = findViewById(R.id.class1);
        c2 = findViewById(R.id.class2);
        c3 = findViewById(R.id.class3);
        c4 = findViewById(R.id.class4);
        c5 = findViewById(R.id.class5);
        c6 = findViewById(R.id.class6);
        c7 = findViewById(R.id.class7);
        c8 = findViewById(R.id.class8);
        c9 = findViewById(R.id.class9);
*/   c[0] = findViewById(R.id.class1);
        c[1] = findViewById(R.id.class2);
        c[2] = findViewById(R.id.class3);
        c[3] = findViewById(R.id.class4);
        c[4] = findViewById(R.id.class5);
        c[5] = findViewById(R.id.class6);
        c[6] = findViewById(R.id.class7);
        c[7] = findViewById(R.id.class8);
        c[8] = findViewById(R.id.class9);
        date = findViewById(R.id.present_date);
        day = findViewById(R.id.weekday_text);
        month = findViewById(R.id.month_text);
        mupdateTask = new updateTask();

        scheduletab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mupdateTask.isCancelled()){
                    mupdateTask.cancel(true);
                }
                Intent i = new Intent(MainActivity.this, FullScheduleActivity.class);
                startActivity(i);
            }
        });

        settingtab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, Preferences.class);
                startActivity(i);
            }
        });

        if(!isInternetAvailable()){
            Toast.makeText(getApplicationContext(),"Connect to internet for latest details",Toast.LENGTH_LONG).show();
        }
        setHolidayViewIfHoliday();
    }

    @Override
    protected void onStart() {
          setHolidayViewIfHoliday();
        setSemester("global_info","semester",year);
        date.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
        day.setText(getWeekdayFromCode(calendar.get(Calendar.DAY_OF_WEEK)));
        month.setText(getMonthFromCode(calendar.get(Calendar.MONTH)));
          if(!isHolidayToday()) {
              setTimeFormat(getTimeFormat());
              if(mupdateTask.isCancelled()) {
                  new updateTask().execute();
              } else {
                  mupdateTask.cancel(true);
                  new updateTask().execute();
              }
          }
        super.onStart();
    }

    @Override
    protected void onPause() {
        mupdateTask.cancel(true);
        super.onPause();
    }

    private void setTimeFormat(int tFormat){
        if(tFormat == 12){
            p[0].setText(getResources().getString(R.string.period112));
            p[1].setText(getResources().getString(R.string.period212));
            p[2].setText(getResources().getString(R.string.period312));
            p[3].setText(getResources().getString(R.string.period412));
            p[4].setText(getResources().getString(R.string.period512));
            p[5].setText(getResources().getString(R.string.period612));
            p[6].setText(getResources().getString(R.string.period712));
            p[7].setText(getResources().getString(R.string.period812));
            p[8].setText(getResources().getString(R.string.period912));
        } else {
            p[0].setText(getResources().getString(R.string.period1));
            p[1].setText(getResources().getString(R.string.period2));
            p[2].setText(getResources().getString(R.string.period3));
            p[3].setText(getResources().getString(R.string.period4));
            p[4].setText(getResources().getString(R.string.period5));
            p[5].setText(getResources().getString(R.string.period6));
            p[6].setText(getResources().getString(R.string.period7));
            p[7].setText(getResources().getString(R.string.period8));
            p[8].setText(getResources().getString(R.string.period9));
        }
    }
    public class updateTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPreExecute() {
            date.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
            day.setText(getWeekdayFromCode(calendar.get(Calendar.DAY_OF_WEEK)));
            month.setText(getMonthFromCode(calendar.get(Calendar.MONTH)));
            setSemester("global_info","semester",year);
            setHolidayViewIfHoliday();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (!isHolidayToday()) {
                readDatabase(clg,course,year,getWeekdayFromCode(calendar.get(Calendar.DAY_OF_WEEK)));
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
                    mupdateTask = new updateTask();
                    mupdateTask.execute();
                }
            }, 10);
            super.onPostExecute(aVoid);
        }

    }

    private void setHolidayViewIfHoliday(){
        if (isHolidayToday()) {
            scrollView.setVisibility(View.INVISIBLE);
            headingview.setVisibility(View.INVISIBLE);
            noclass.setVisibility(View.VISIBLE);
        } else {
            noclass.setVisibility(View.GONE);
        }
    }
    private Boolean isHolidayToday(){
        if(islocalHoliday("global_info","holiday_info")){
            isHoliday = true;
        } else if(islocalHoliday(clg,"local_info")){
            isHoliday = true;
        } else if(islocalHoliday(clg,course)){
            isHoliday = true;
        } else isHoliday = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY;
        return isHoliday;
    }
    private Boolean islocalHoliday(String collector, String doc){
        if (getLoginStatus()) {
            db.collection(collector).document(doc)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                     isHoliday = Boolean.parseBoolean(Objects.requireNonNull(document.get("holiday")).toString());
                                } else {
                                    Log.d(TAG, "Server error in getting semester.");
                                    Toast.makeText(MainActivity.this, "Unable to read", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
        }
        return isHoliday;
    }
    
    private String getWeekdayFromCode(int daycode){
        switch (daycode) {
            case 1:
                return "Sunday";
            case 2:
                return "Monday";
            case 3:
                return "Tuesday";
            case 4:
                return "Wednesday";
            case 5:
                return "Thursday";
            case 6:
                return "Friday";
            case 7:
                return "Saturday";
            default:
                return "Error";
        }
    }
    
    private String getMonthFromCode(int getMonthCount){
        switch (getMonthCount) {
            case 0:
                return "January";
            case 1:
                return "February";
            case 2:
                return "March";
            case 3:
                return "April";
            case 4:
                return "May";
            case 5:
                return "June";
            case 6:
                return "July";
            case 7:
                return "August";
            case 8:
                return "September";
            case 9:
                return "October";
            case 10:
                return "November";
            case 11:
                return "December";
            default:
                return "Error";
        }
    }
    
    
    private void setSemester(String source, String doc,final String year) {
        if (getLoginStatus()) {
            db.collection(source).document(doc)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
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
    
    private void highlightCurrentPeriod(){
        int i = 0, s = 0;
        if(checkPeriod("08:30:00", "09:30:00")){
            //notifier("08:30:00", c[0].getText().toString());
            p[i].setTextColor(getResources().getColor(R.color.white));
            p[i].setBackgroundResource(R.drawable.roundactivetimecontainer);
            return;
        } else if(checkPeriod("09:30:00", "10:30:00")){
            //notifier("09:30:00", c[1].getText().toString());
            s = 1;
        }else if(checkPeriod("10:30:00", "11:30:00")){
            //notifier("10:30:00", c[2].getText().toString());
            s = 2;
        }else if(checkPeriod("11:30:00","12:30:00")){
            //notifier("11:30:00", c[3].getText().toString());
            s = 3;
        }else if(checkPeriod("12:30:00","13:30:00")){
            //notifier("12:30:00", c[4].getText().toString());
            s = 4;
        }else if(checkPeriod("13:30:00","14:30:00")){
            //notifier("13:30:00", c[5].getText().toString());
            s = 5;
        }else if(checkPeriod("14:30:00","15:30:00")){
            //notifier("14:30:00", c[6].getText().toString());
            s = 6;
        }else if(checkPeriod("15:30:00","16:30:00")){
            //notifier("15:30:00", c[7].getText().toString());
            s = 7;
        }else if(checkPeriod("16:30:00","17:30:00")){
            //notifier("16:30:00", c[8].getText().toString());
            s = 8;
        } else if(checkPeriod("17:30:00","23:59:59")) {
            int d = 0;
            while (d<9) {
                p[d].setTextColor(getResources().getColor(R.color.white));
                p[d].setBackgroundResource(R.drawable.roundtimeovercontainer);
                d++;
            }
            return;
        } else if(checkPeriod("00:00:00","8:30:00")) {
            int d = 0;
            while (d<9) {
                p[d].setBackgroundResource(R.drawable.roundcontainerbox);
                p[d].setTextColor(getResources().getColor(R.color.white));
                d++;
            }
            return;
        }
        while (i < s) {
            p[i].setTextColor(getResources().getColor(R.color.white));
            p[i].setBackgroundResource(R.drawable.roundtimeovercontainer);
            p[s].setBackgroundResource(R.drawable.roundactivetimecontainer);
            p[s].setTextColor(getResources().getColor(R.color.white));
            ++i;
        }
        i=i+1;
        while (i<9){
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

    private void readDatabase(String source, String course, String year, String weekday){
            db.collection(source).document(course).collection(year).document(weekday.toLowerCase())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                    int i = 0;
                                    while(i<9) {
                                        c[i].setText(document.get(pkey[i]).toString());
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
    private void notifier(String begin, String classname){
        String currentTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
        SimpleDateFormat parser = new SimpleDateFormat("HH:mm:ss");
        try {
            Date start = parser.parse(begin);
            Date userTime = parser.parse(currentTime);
            assert userTime != null;
            if (userTime.equals(start)) {
                createNotification(classname);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private Boolean getLoginStatus(){
        SharedPreferences mSharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        return mSharedPreferences.getBoolean("loginstatus", false);
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }
/*
    @Override
    protected void onDestroy() {
        stopService(mServiceIntent);
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();
    }*/
    private void createNotification(String className){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "NewPeriod")
                .setSmallIcon(R.drawable.ic_icon)
                .setContentTitle("A class has started.")
                .setContentText(className+" almost started. Reach there before it gets too late.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(className+". Reach there before it gets too late."))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
// notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, builder.build());
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
    private int getTimeFormat() {
        SharedPreferences mSharedPreferences = this.getSharedPreferences("schemeTime", MODE_PRIVATE);
        return mSharedPreferences.getInt("format", 24);
    }
    public boolean isInternetAvailable() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }
}
