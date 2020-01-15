package org.timetable.schedule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import java.util.Locale;
import java.util.TimeZone;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {
    TextView p1,p2,p3,p4,p5,p6,p7,p8,p9, c1,c2,c3,c4,c5,c6,c7,c8,c9, semestertxt, noclass;
    Button day,date, month, fullview;
    int getDate;
    String getDay, getMonth;
    LinearLayout linearLayout;
    ScrollView scrollView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setNavigationBarColor(this.getResources().getColor(R.color.blue));
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        linearLayout = findViewById(R.id.linearLayout);
        scrollView = findViewById(R.id.scrollView);
        noclass = findViewById(R.id.noclasstext);
        semestertxt = findViewById(R.id.sem_text);
        if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
            linearLayout.setVisibility(View.INVISIBLE);
            scrollView.setVisibility(View.INVISIBLE);
            noclass.setVisibility(View.VISIBLE);
        } else{
            noclass.setVisibility(View.GONE);
        }
        fullview = findViewById(R.id.full_schedule);
        fullview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, FullScheduleActivity.class);
                startActivity(i);
            }
        });
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
        day = findViewById(R.id.weekday_text);
        month = findViewById(R.id.month_text);
        getDate = calendar.get(Calendar.DAY_OF_MONTH);
        int getDayCount = calendar.get(Calendar.DAY_OF_WEEK);
        switch (getDayCount){
            case 1: getDay = "Sunday"; break;
            case  2: getDay = "Monday";break;
            case 3: getDay = "Tuesday";break;
            case 4: getDay = "Wednesday";break;
            case 5: getDay = "Thursday";break;
            case 6: getDay = "Friday";break;
            case 7: getDay = "Saturday";break;
            default:getDay = "Shit";
        }
        int getMonthCount = calendar.get(Calendar.MONTH);
        switch (getMonthCount){
            case 0: getMonth = "January"; break;
            case 1: getMonth = "February"; break;
            case 2: getMonth = "March"; break;
            case 3: getMonth = "April"; break;
            case 4: getMonth = "May"; break;
            case 5: getMonth = "June"; break;
            case 6: getMonth = "July"; break;
            case 7: getMonth = "August"; break;
            case 8: getMonth = "September"; break;
            case 9: getMonth = "October"; break;
            case 10: getMonth = "November"; break;
            case 11: getMonth = "December"; break;
            default: getMonth = "Shit";
        }
        date.setText(Integer.toString(getDate));
        day.setText(getDay);
        month.setText(getMonth);

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
        readDatabase(getDay);
        if(checkPeriod("08:30:00", "09:30:00")){
            p1.setBackgroundResource(R.drawable.roundactivetimecontainer);
        } else if(checkPeriod("09:30:00:00", "10:30:00")){
            p1.setBackgroundResource(R.drawable.roundtimecontainer);
            p2.setBackgroundResource(R.drawable.roundactivetimecontainer);
        }else if(checkPeriod("10:30:00", "11:30:00")){
            p1.setBackgroundResource(R.drawable.roundtimecontainer);
            p2.setBackgroundResource(R.drawable.roundtimecontainer);
            p3.setBackgroundResource(R.drawable.roundactivetimecontainer);
        }else if(checkPeriod("11:30:00","12:30:00")){
            p1.setBackgroundResource(R.drawable.roundtimecontainer);
            p2.setBackgroundResource(R.drawable.roundtimecontainer);
            p3.setBackgroundResource(R.drawable.roundtimecontainer);
            p4.setBackgroundResource(R.drawable.roundactivetimecontainer);
        }else if(checkPeriod("12:30:00","13:30:00")){
            p1.setBackgroundResource(R.drawable.roundtimecontainer);
            p2.setBackgroundResource(R.drawable.roundtimecontainer);
            p3.setBackgroundResource(R.drawable.roundtimecontainer);
            p4.setBackgroundResource(R.drawable.roundtimecontainer);
            p5.setBackgroundResource(R.drawable.roundactivetimecontainer);
        }else if(checkPeriod("13:30:00","14:30:00")){
            p1.setBackgroundResource(R.drawable.roundtimecontainer);
            p2.setBackgroundResource(R.drawable.roundtimecontainer);
            p3.setBackgroundResource(R.drawable.roundtimecontainer);
            p4.setBackgroundResource(R.drawable.roundtimecontainer);
            p5.setBackgroundResource(R.drawable.roundtimecontainer);
            p6.setBackgroundResource(R.drawable.roundactivetimecontainer);
        }else if(checkPeriod("14:30:00","15:30:00")){
            p1.setBackgroundResource(R.drawable.roundtimecontainer);
            p2.setBackgroundResource(R.drawable.roundtimecontainer);
            p3.setBackgroundResource(R.drawable.roundtimecontainer);
            p4.setBackgroundResource(R.drawable.roundtimecontainer);
            p5.setBackgroundResource(R.drawable.roundtimecontainer);
            p6.setBackgroundResource(R.drawable.roundtimecontainer);
            p7.setBackgroundResource(R.drawable.roundactivetimecontainer);
        }else if(checkPeriod("15:30:00","16:30:00")){
            p1.setBackgroundResource(R.drawable.roundtimecontainer);
            p2.setBackgroundResource(R.drawable.roundtimecontainer);
            p3.setBackgroundResource(R.drawable.roundtimecontainer);
            p4.setBackgroundResource(R.drawable.roundtimecontainer);
            p5.setBackgroundResource(R.drawable.roundtimecontainer);
            p6.setBackgroundResource(R.drawable.roundtimecontainer);
            p7.setBackgroundResource(R.drawable.roundtimecontainer);
            p8.setBackgroundResource(R.drawable.roundactivetimecontainer);
        }else if(checkPeriod("16:30:00","17:30:00")){
            p1.setBackgroundResource(R.drawable.roundtimecontainer);
            p2.setBackgroundResource(R.drawable.roundtimecontainer);
            p3.setBackgroundResource(R.drawable.roundtimecontainer);
            p4.setBackgroundResource(R.drawable.roundtimecontainer);
            p5.setBackgroundResource(R.drawable.roundtimecontainer);
            p6.setBackgroundResource(R.drawable.roundtimecontainer);
            p7.setBackgroundResource(R.drawable.roundtimecontainer);
            p8.setBackgroundResource(R.drawable.roundtimecontainer);
            p9.setBackgroundResource(R.drawable.roundactivetimecontainer);
        } else {
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
                                Toast.makeText(MainActivity.this, "Unable to read", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                            Toast.makeText(MainActivity.this, "fail exception", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
