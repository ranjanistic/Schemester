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
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class ChatRoomActivity extends AppCompatActivity {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    ImageButton pullUp, pullDown;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    Boolean isCollapsed = true;
    ReadMessage readMessage;
    String newMessage;
    TextView receivedmsg, sendmsg, senderName, myName;
    ImageButton sendMsgBtn;
    EditText mymsg;
    Boolean thereIsANewMessage = false;
    CustomConfirmDialogClass customConfirmDialogClassVerfication;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAppTheme(getThemeStatus());
        setContentView(R.layout.activity_chat_room);

        if (!checkIfEmailVerified()) {
            Toast.makeText(ChatRoomActivity.this, "Please verify your email first.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            mymsg = findViewById(R.id.messageTextTyping);
            senderName = findViewById(R.id.senderID);
            sendmsg = findViewById(R.id.myMessage);
            myName = findViewById(R.id.myID);
            receivedmsg = findViewById(R.id.receivedMsg);
            sendMsgBtn = findViewById(R.id.sendTextMessage);
            pullUp = findViewById(R.id.pullUpbtn);
            pullDown = findViewById(R.id.pullDownbtn);
/*
            LinearLayout bottomDrawer = findViewById(R.id.chatMenu);
            final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomDrawer);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            bottomSheetBehavior.setHideable(true);
            bottomSheetBehavior.setPeekHeight(0);
            pullUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            });
            pullDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            });

 */
            sendMsgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!isInternetAvailable()){
                        Toast.makeText(getApplicationContext(),"Connection error", Toast.LENGTH_SHORT).show();
                    } else {
                        newMessage = mymsg.getText().toString();
                        sendmsg.setText(newMessage);
                        saveMessage(getPreviousMessageNumber()+1,newMessage);
                        setMessageToDatabase("DBC-DU","PHY-H","Y2", newMessage,
                                getPreviousMessageNumber()+1);
                    }
                }
            });
            readMessage = new ReadMessage();

        }
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
        readMessage.cancel(true);
        super.onStop();
    }

    public class ReadMessage extends AsyncTask<Void,Void,String>{
        @Override
        protected String doInBackground(Void... voids) {
            setMessageFromDatabase("DBC-DU","PHY-H","Y2");
            if(thereIsANewMessage){
                return getPreviousMessageText();
            } else {
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            if(result!=null) {
                receivedmsg.setText(result);
            }
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    readMessage = new ReadMessage();
                    readMessage.execute();
                }
            }, 100);
            super.onPostExecute(result);
        }
    }

    private void setOnline(Boolean status){
        Map<String, Object> data = new HashMap<>();
        data.put("active", status);
        DocumentReference coll  =  db.collection("userbase").document(getStoredEmail());
        coll.set(data, SetOptions.merge());
    }


    private String getStoredEmail(){
        String cred;
        SharedPreferences mSharedPreferences = getSharedPreferences("credentials", MODE_PRIVATE);
        cred =  mSharedPreferences.getString("email", "");
        return cred;
    }

    private Boolean checkIfEmailVerified() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        return user.isEmailVerified();
    }
    private void sendVerificationEmail() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),"A confirmation email is sent to your email address.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void setMessageToDatabase(String college, String course, String year,String message, int code){
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("text",message);
        userMap.put("serialnum", code);
        db.collection(college).document(course).collection(year).document("currentmsg")
        .update(userMap);
    }

    private void setMessageFromDatabase(String college, String course, String year){
        db.collection(college).document(course).collection(year).document("currentmsg")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                int code = Integer.parseInt(document.get("serialnum").toString());
                                if(getPreviousMessageNumber() < code){
                                    saveMessage(code,document.getString("text"));
                                    thereIsANewMessage = true;
                                } else {
                                    thereIsANewMessage = false;
                                }
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        }
                    }
                });
    }

    private void saveMessage(int num, String txt){
        SharedPreferences mSharedPreferences = getSharedPreferences("messages", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putInt("serial", num);
        mEditor.putString("msg", txt);
        mEditor.apply();
    }
    private int getPreviousMessageNumber(){
        SharedPreferences mSharedPreferences = getSharedPreferences("messages", MODE_PRIVATE);
        return mSharedPreferences.getInt("serial", 0);
    }
    private String getPreviousMessageText(){
        SharedPreferences mSharedPreferences = getSharedPreferences("messages", MODE_PRIVATE);
        return mSharedPreferences.getString("msg", "");
    }

    public void setAppTheme(int code) {
        switch (code) {
            case 101:
                setTheme(R.style.BlueLightTheme);
                break;
            case 102:
                setTheme(R.style.BlueDarkTheme);
                break;
            default:setTheme(R.style.BlueLightTheme);
        }
    }
    private int getThemeStatus() {
        SharedPreferences mSharedPreferences = this.getSharedPreferences("schemeTheme", MODE_PRIVATE);
        return mSharedPreferences.getInt("themeCode", 0);
    }
}
