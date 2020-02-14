package org.timetable.schemester;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import static android.content.ContentValues.TAG;

public class Splash extends AppCompatActivity {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] addInfo = getAdditionalInfo();
        if(user!=null) {
            if(Objects.equals(addInfo[0],"")||Objects.equals(addInfo[1],"")||Objects.equals(addInfo[2],"")){
                Intent intent = new Intent(this, AdditionalLoginInfo.class);
                startActivity(intent);
                finish();
            } else {
                isHolidayOtherThanWeekend(addInfo[0], addInfo[1]);
                isHolidayOtherThanWeekend(addInfo[0], "local_info");
                isHolidayOtherThanWeekend("global_info", "holiday_info");
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            Intent intent = new Intent(this, PositionActivity.class);
            startActivity(intent);
            finish();
        }
    }
    private String[] getAdditionalInfo() {
        String[] CCY = {null, null, null};
        SharedPreferences mSharedPreferences = this.getSharedPreferences("additionalInfo", MODE_PRIVATE);
        CCY[0] = mSharedPreferences.getString("college", "");
        CCY[1] = mSharedPreferences.getString("course", "");
        CCY[2] = mSharedPreferences.getString("year", "");
        return CCY;
    }
    private void isHolidayOtherThanWeekend(String collector, String doc){
        db.collection(collector).document(doc)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (Objects.requireNonNull(document).exists()) {
                                Log.d(TAG, "Document Holiday data: " + document.getData());
                                saveHolidayStatus(document.getBoolean("holiday"));
                            }
                        }
                    }
                });
    }
    private void saveHolidayStatus(Boolean isHoliday){
        SharedPreferences mSharedPreferences = getSharedPreferences("otherHoliday", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean("holiday", isHoliday);
        mEditor.apply();
    }
/*
    private Boolean getLoginStatus(){
        SharedPreferences mSharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        return mSharedPreferences.getBoolean("loginstatus", false);
    }

    public void setAppTheme(int code) {
        switch (code) {
            case 101:
                setTheme(R.style.splashTheme);
                break;
            case 102:
                setTheme(R.style.splashThemeDark);
                break;
                default:setTheme(R.style.splashTheme);
        }
    }

    private int getThemeStatus() {
        SharedPreferences mSharedPreferences = this.getSharedPreferences("schemeTheme", MODE_PRIVATE);
        return mSharedPreferences.getInt("themeCode", 0);
    }
    */

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("NewPeriod", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
