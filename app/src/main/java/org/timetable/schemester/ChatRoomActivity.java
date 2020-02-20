package org.timetable.schemester;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.AsyncDifferConfig;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class ChatRoomActivity extends AppCompatActivity {
    ApplicationSchemester schemester;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    ImageButton pullUp, pullDown, exit;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    ReadMessage readMessage;
    String newMessage;
    LinearLayout chatView;
    TextView receivedmsg, sendmsg, senderName, myName;
    ImageButton sendMsgBtn;
    LinearLayout sendlayout, receiveLayout;
    EditText mymsg;
    Boolean thereIsANewMessage = false;
    checkNetSendMyMessageTask netCheckMessageSendTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        schemester = (ApplicationSchemester) this.getApplication();
        super.onCreate(savedInstanceState);
        setAppTheme();
        setContentView(R.layout.activity_chat_room);
        if (!checkIfEmailVerified()) {
            Toast.makeText(ChatRoomActivity.this, "Please verify your email first.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            sendlayout = findViewById(R.id.sendMsgLayout);
            receiveLayout = findViewById(R.id.receiveMsgLayout);
            mymsg = findViewById(R.id.messageTextTyping);
            sendmsg = findViewById(R.id.myMessage);
            senderName = findViewById(R.id.senderID);
            myName = findViewById(R.id.myID);
            receivedmsg = findViewById(R.id.receivedMsg);
            sendMsgBtn = findViewById(R.id.sendTextMessage);
            pullUp = findViewById(R.id.pullUpbtn);
            pullDown = findViewById(R.id.pullDownbtn);
            chatView = findViewById(R.id.chatWindowLinear);
            exit = findViewById(R.id.exitBtn);
            exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            sendMsgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    netCheckMessageSendTask = new checkNetSendMyMessageTask();
                    netCheckMessageSendTask.execute();
                }
            });
            readMessage = new ReadMessage();
            readMessage.execute();
        }
    }


    private class checkNetSendMyMessageTask extends AsyncTask<Void,Void,Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {
            return isInternetAvailable();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result) {
                Calendar calendar = Calendar.getInstance();
                newMessage = mymsg.getText().toString();
                if(!(newMessage.equals(" ")||newMessage.equals(""))){
                    chatView.removeAllViews();
                    chatView.addView(sendlayout);
                    long t = calendar.getTimeInMillis();
                    saveMessageToDevice(t,newMessage,getUserEmailIDFromLocalStorage());
                    updateMessageInDatabase(schemester.getCOLLECTION_COLLEGE_CODE(),
                            schemester.getDOCUMENT_COURSE_CODE(),
                            schemester.getCOLLECTION_YEAR_CODE(),
                            newMessage, getUserEmailIDFromLocalStorage(), t
                    );
                    sendmsg.setText(newMessage);
                    myName.setText(getUserEmailIDFromLocalStorage());
                    mymsg.setText("");
                }
            } else{
                Toast.makeText(getApplicationContext(),"Connection error", Toast.LENGTH_SHORT).show();
            }
            netCheckMessageSendTask.cancel(true);
            super.onPostExecute(result);
        }
    }

    @Override
    protected void onStart() {
        setOnline(true);
        if(readMessage.isCancelled()) {
            readMessage = new ReadMessage();
            readMessage.execute();
        } else {
            readMessage.cancel(true);
            readMessage = new ReadMessage();
            readMessage.execute();
        }
        super.onStart();
    }
    @Override
    protected void onStop() {
        setOnline(false);
        readMessage.cancel(true);
        super.onStop();
    }

    public class ReadMessage extends AsyncTask<Void,Void,Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {
            getMessageFromDatabase(schemester.getCOLLECTION_COLLEGE_CODE(),
                    schemester.getDOCUMENT_COURSE_CODE(),
                    schemester.getCOLLECTION_YEAR_CODE());
            return thereIsANewMessage;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result) {
                senderName.setText(getPreviousMessageAndSenderText()[0]);
                receivedmsg.setText(getPreviousMessageAndSenderText()[1]);
                chatView.removeAllViews();
                chatView.addView(receiveLayout);
            }
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    readMessage = new ReadMessage();
                    readMessage.execute();
                }
            }, 10);
            super.onPostExecute(result);
        }
    }

    private void updateMessageInDatabase(String college, String course, String year,String message, String uid, Long timestamp){
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("text",message);
        userMap.put("uid", uid);
        userMap.put("time", timestamp);
        db.collection(college).document(course).collection(year).document("currentmsg")
        .update(userMap);
        saveMessageToDevice(timestamp,message,uid);
    }
    private void saveMessageToDevice(long num, String txt, String id){
        SharedPreferences mSharedPreferences = getSharedPreferences("messages", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putLong("stamp", num);
        mEditor.putString("msg", txt);
        mEditor.putString("id", id);
        mEditor.apply();
    }
    private String[] getPreviousMessageAndSenderText(){
        String[] idMsg = {"",""};
        SharedPreferences mSharedPreferences = getSharedPreferences("messages", MODE_PRIVATE);
        idMsg[0] = mSharedPreferences.getString("id", "");
        idMsg[1] = mSharedPreferences.getString("msg", "");
        return idMsg;
    }
    private void getMessageFromDatabase(String college, String course, String year){
        db.collection(college).document(course).collection(year).document("currentmsg")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                long ts = Long.parseLong(document.get("time").toString());
                                if(!getUserEmailIDFromLocalStorage().equals(document.getString("uid"))){
                                    thereIsANewMessage = true;
                                    saveMessageToDevice(ts,document.getString("text"),document.getString("uid"));
                                } else {
                                    thereIsANewMessage = false;
                                }
                            }
                        }
                    }
                });
    }

    private boolean isInternetAvailable() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException | InterruptedException e){ e.printStackTrace(); }
        return false;
    }
    String[] getAdditionalInfo() {
        String[] CCY = {null, null, null};
        SharedPreferences mSharedPreferences = getSharedPreferences("additionalInfo", MODE_PRIVATE);
        CCY[0] = mSharedPreferences.getString("college", "");
        CCY[1] = mSharedPreferences.getString("course", "");
        CCY[2] = mSharedPreferences.getString("year", "");
        return CCY;
    }

    private void setOnline(Boolean status){
        Map<String, Object> data = new HashMap<>();
        data.put("active", status);
        db.collection("userbase").document(getUserEmailIDFromLocalStorage())
                .update(data);
    }

    public void setAppTheme() {
        SharedPreferences mSharedPreferences = this.getSharedPreferences("schemeTheme", MODE_PRIVATE);
        switch (mSharedPreferences.getInt("themeCode", 0)) {
            case ApplicationSchemester.CODE_THEME_DARK:
                setTheme(R.style.BlueDarkTheme);
                break;
            case ApplicationSchemester.CODE_THEME_LIGHT:
            default:setTheme(R.style.BlueLightTheme);
        }
    }

    private String getUserEmailIDFromLocalStorage(){
        String cred;
        SharedPreferences mSharedPreferences = getSharedPreferences("credentials", MODE_PRIVATE);
        cred =  mSharedPreferences.getString("email", "");
        return cred;
    }

    private Boolean checkIfEmailVerified() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        return user.isEmailVerified();
    }
}
