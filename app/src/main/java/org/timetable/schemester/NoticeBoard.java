package org.timetable.schemester;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.Distribution;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import static android.content.ContentValues.TAG;

public class NoticeBoard extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    TextView head,body,signedby;
    SwipeRefreshLayout swipeRefreshLayout;
    Button read,save,delete;
    LinearLayout noticeBoard;
    noticeUpdate mnoticeUpdate;
    Boolean isDeleted = false, isSaved = false, isRead = false;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAppTheme(getThemeStatus());
        setContentView(R.layout.activity_notice_board);
        noticeBoard = findViewById(R.id.noticeLayout);
        head = findViewById(R.id.noticeHead);
        body = findViewById(R.id.noticeBody);
        signedby = findViewById(R.id.signee);
        read = findViewById(R.id.markread);
        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        mnoticeUpdate = new noticeUpdate();
        mnoticeUpdate.execute();
    }
    @Override
    public void onRefresh() {
        Toast.makeText(this, "Refresh", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getNotice();
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }
    @Override
    protected void onStart() {
        Boolean[] settings;
        settings = readNoticeSettings();
        if(!settings[2]) {
            String[] notice;
            notice = readNotice();
            head.setText(notice[0]);
            body.setText(notice[1]);
            signedby.setText(notice[2]);
        } else {
        }
        if(settings[0]){
            noticeBoard.setAlpha((float) 0.5);
            read.setText("Marked Read");
            read.setAlpha((float)0.5);
            save.setVisibility(View.INVISIBLE);
            delete.setVisibility(View.INVISIBLE);
        }
        super.onStart();
    }
    public class noticeUpdate extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            getNotice();
            return null;
        }

        @Override
        protected void onPreExecute() {
            read.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isRead = true;
                    savenoticeSetting(true, false,false);
                }
            });
            save = findViewById(R.id.savenotice);
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isSaved = true;
                    savenoticeSetting(false, false,true);
                }
            });
            delete = findViewById(R.id.deletenotice);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isDeleted = true;
                    savenoticeSetting(false, false,true);
                    saveNotice("","","");
                }
            });
            //savenoticeSetting(isRead,isSaved,isDeleted);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mnoticeUpdate= new noticeUpdate();
                    mnoticeUpdate.execute();
                }
            }, 10);
            super.onPostExecute(aVoid);
        }
    }
    private void getNotice(){
      if(isNetworkConnected()){
          db.collection("global_info").document("notice")
                  .get()
                  .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                      @Override
                      public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                          if (task.isSuccessful()) {
                              DocumentSnapshot document = task.getResult();
                              if (Objects.requireNonNull(document).exists()) {
                                  Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                  String[] older =  readNotice();
                                  if(older[0].equals(document.getString("head"))|| older[1].equals(document.getString("body"))||older.equals(document.getString("signee"))){
                                      return;
                                  } else {
                                      savenoticeSetting(false,false,false);
                                      saveNotice(document.getString("head"), document.getString("body"), document.getString("signee"));
                                      noticeBoard.setAlpha(1);
                                  }
                              } else {
                                  Log.d(TAG, "Server error");
                                  Toast.makeText(NoticeBoard.this, "Server error", Toast.LENGTH_LONG).show();
                              }
                          } else {
                              Log.d(TAG, "Failed to receive data", task.getException());
                              Toast.makeText(NoticeBoard.this, "Please restart.", Toast.LENGTH_LONG).show();
                          }
                      }
                  });
      } else {
          Toast.makeText(NoticeBoard.this, "Check connection", Toast.LENGTH_LONG).show();
      }
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
    private String[] readNotice(){
        String[] fullNotice = {"","","",""};
        SharedPreferences mSharedPreferences = this.getSharedPreferences("schemeNotice", MODE_PRIVATE);
         fullNotice[0] = mSharedPreferences.getString("nheading", "");
        fullNotice[1] = mSharedPreferences.getString("nbody", "");
        fullNotice[2] = mSharedPreferences.getString("nsignee", "");
        return fullNotice;
    }

    private void saveNotice(String head, String body, String signee){
        SharedPreferences mSharedPreferences = getSharedPreferences("schemeNotice", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString("nheading",head);
        mEditor.putString("nbody",body);
        mEditor.putString("nsignee",signee);
        mEditor.apply();
    }
    private void savenoticeSetting( Boolean read, Boolean saved,Boolean deleted){
        SharedPreferences mSharedPreferences = getSharedPreferences("schemeNoticeAction", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean("read",read);
        mEditor.putBoolean("save",saved);
        mEditor.putBoolean("delete",deleted);
        mEditor.apply();
    }
    private Boolean[] readNoticeSettings(){
        Boolean[] action = {false,false,false};
        SharedPreferences mSharedPreferences = this.getSharedPreferences("schemeNoticeAction", MODE_PRIVATE);
        action[0] = mSharedPreferences.getBoolean("read", false);
        action[1] = mSharedPreferences.getBoolean("save", false);
        action[2] = mSharedPreferences.getBoolean("delete",false);
        return action;
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
}
