package org.timetable.schedule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity{
    TextView p1,p2,p3,p4,p5,p6,p7,p8,p9, c1,c2,c3,c4,c5,c6,c7,c8,c9, semestertxt, noclass;
    Button day,date, month, fullview;
    int getDate;
    String getDay, getMonth;
    LinearLayout linearLayout;
    ScrollView scrollView;
    Calendar calendar;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setNavigationBarColor(this.getResources().getColor(R.color.blue));
        calendar = Calendar.getInstance(TimeZone.getDefault());
        linearLayout = findViewById(R.id.linearLayout);
        scrollView = findViewById(R.id.scrollView);
        noclass = findViewById(R.id.noclasstext);
        semestertxt = findViewById(R.id.sem_text);

        p1 = findViewById(R.id.period1);
        p2 = findViewById(R.id.period2);
        p3 = findViewById(R.id.period3);
        p4 = findViewById(R.id.period4);
        p5 = findViewById(R.id.period5);
        p6 = findViewById(R.id.period6);
        p7 = findViewById(R.id.period7);
        p8 = findViewById(R.id.period8);
        p9 = findViewById(R.id.period9);

        c1 = findViewById(R.id.class1);
        c2 = findViewById(R.id.class2);
        c3 = findViewById(R.id.class3);
        c4 = findViewById(R.id.class4);
        c5 = findViewById(R.id.class5);
        c6 = findViewById(R.id.class6);
        c7 = findViewById(R.id.class7);
        c8 = findViewById(R.id.class8);
        c9 = findViewById(R.id.class9);

        date = findViewById(R.id.present_date);
        date.setText(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)));
        day = findViewById(R.id.weekday_text);
        day.setText(setWeekDay(calendar.get(Calendar.DAY_OF_WEEK)));
        month = findViewById(R.id.month_text);
        month.setText(setMonthFromCode(calendar.get(Calendar.MONTH)));
        fullview = findViewById(R.id.full_schedule);
        fullview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, FullScheduleActivity.class);
                startActivity(i);
            }
        });
        new updateTask().execute();
    }

    public class updateTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            checkHoliday();
            setSemester();
            readDatabase(setWeekDay(calendar.get(Calendar.DAY_OF_WEEK)));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            highlightCurrentPeriod();
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            new updateTask().execute();
            super.onProgressUpdate(values);
        }
    }
    
    private void checkHoliday(){
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            linearLayout.setVisibility(View.INVISIBLE);
            scrollView.setVisibility(View.INVISIBLE);
            noclass.setVisibility(View.VISIBLE);
        } else {
            noclass.setVisibility(View.GONE);
        }
    }
    
    private String setWeekDay(int daycode){
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
    
    private String setMonthFromCode(int getMonthCount){
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
    
    
    private void setSemester(){
        db.collection("semesterSchedule").document("semester")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                semestertxt.setText(document.get("semnum").toString());
                            } else {
                                Log.d(TAG, "No such document");
                                Toast.makeText(MainActivity.this, "Unable to read", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                            Toast.makeText(MainActivity.this, "fail excepti" +
                                    "on", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    
    private void highlightCurrentPeriod(){
        if(checkPeriod("08:30:00", "09:30:00")){
            p1.setBackgroundResource(R.drawable.roundactivetimecontainer);
        } else if(checkPeriod("09:30:00", "10:30:00")){
            p1.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p2.setBackgroundResource(R.drawable.roundactivetimecontainer);
        }else if(checkPeriod("10:30:00", "11:30:00")){
            p1.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p2.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p3.setBackgroundResource(R.drawable.roundactivetimecontainer);
        }else if(checkPeriod("11:30:00","12:30:00")){
            p1.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p2.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p3.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p4.setBackgroundResource(R.drawable.roundactivetimecontainer);
        }else if(checkPeriod("12:30:00","13:30:00")){
            p1.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p2.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p3.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p4.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p5.setBackgroundResource(R.drawable.roundactivetimecontainer);
        }else if(checkPeriod("13:30:00","14:30:00")){
            p1.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p2.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p3.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p4.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p5.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p6.setBackgroundResource(R.drawable.roundactivetimecontainer);
        }else if(checkPeriod("14:30:00","15:30:00")){
            p1.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p2.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p3.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p4.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p5.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p6.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p7.setBackgroundResource(R.drawable.roundactivetimecontainer);
        }else if(checkPeriod("15:30:00","16:30:00")){
            p1.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p2.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p3.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p4.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p5.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p6.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p7.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p8.setBackgroundResource(R.drawable.roundactivetimecontainer);
        }else if(checkPeriod("16:30:00","17:30:00")){
            p1.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p2.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p3.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p4.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p5.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p6.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p7.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p8.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p9.setBackgroundResource(R.drawable.roundactivetimecontainer);
        } else if(checkPeriod("17:30:00","00:00:00")) {
            p1.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p2.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p3.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p4.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p5.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p6.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p7.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p8.setBackgroundResource(R.drawable.roundtimeovercontainer);
            p9.setBackgroundResource(R.drawable.roundtimeovercontainer);
        } else if(checkPeriod("00:00:00","8:30:00")) {
            p1.setBackgroundResource(R.drawable.roundtimecontainer);
            p2.setBackgroundResource(R.drawable.roundtimecontainer);
            p3.setBackgroundResource(R.drawable.roundtimecontainer);
            p4.setBackgroundResource(R.drawable.roundtimecontainer);
            p5.setBackgroundResource(R.drawable.roundtimecontainer);
            p6.setBackgroundResource(R.drawable.roundtimecontainer);
            p7.setBackgroundResource(R.drawable.roundtimecontainer);
            p8.setBackgroundResource(R.drawable.roundtimecontainer);
            p9.setBackgroundResource(R.drawable.roundtimecontainer);
        }
    }

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

    private void readDatabase(String weekday){
        db.collection("semesterSchedule").document(weekday.toLowerCase())
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
                                Toast.makeText(MainActivity.this, "Server error.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                            Toast.makeText(MainActivity.this, "Try restarting the app.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


}
