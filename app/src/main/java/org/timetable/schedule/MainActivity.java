package org.timetable.schedule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {
    TextView p1,p2,p3,p4,p5,p6,p7,p8,p9, c1,c2,c3,c4,c5,c6,c7,c8,c9, semestertxt, noclass;
    Button day,date, month;
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
                            Toast.makeText(MainActivity.this, "fail exception", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        readDatabase(getDay);
        if(checkPeriod("08:30")){
            p1.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        } else if(checkPeriod("09:30")){
            p1.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p2.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }else if(checkPeriod("10:30")){
            p1.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p2.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p3.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }else if(checkPeriod("11:30")){
            p1.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p2.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p3.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p4.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }else if(checkPeriod("12:30")){
            p1.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p2.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p3.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p4.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p5.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }else if(checkPeriod("13:30")){
            p1.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p2.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p3.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p4.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p5.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p6.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }else if(checkPeriod("14:30")){
            p1.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p2.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p3.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p4.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p5.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p6.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p7.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }else if(checkPeriod("15:30")){
            p1.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p2.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p3.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p4.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p5.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p6.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p7.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p8.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }else if(checkPeriod("16:30")){
            p1.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p2.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p3.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p4.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p5.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p6.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p7.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p8.setBackgroundColor(getResources().getColor(R.color.blue_grey));
            p9.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    private Boolean checkPeriod(String checktime){
        @SuppressLint("SimpleDateFormat")
        String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
        return currentTime.equals(checktime);
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
