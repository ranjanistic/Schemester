package org.timetable.schemester;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.LongSparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChatRoomActivity extends AppCompatActivity {
    ApplicationSchemester schemester;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    String TAG_MESSAGE_DATA = "localMessages";
    ImageButton exit, menu, send;
    EditText myTypedMsg;
    Boolean connected;
    RecyclerView mMessageRecycler;
    MessageListAdapter mMessageAdapter;
    Handler dataHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        schemester = (ApplicationSchemester) this.getApplication();
        if(!checkIfEmailVerified()){
            schemester.toasterLong("Email not verified");
            finish();
        }
        setContentView(R.layout.activity_chat_room);
        setViewsAndListeners();
        mMessageRecycler = findViewById(R.id.chatRecycleView);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setOnline(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setOnline(true);
        dataHandler = new Handler(getMainLooper());
        dataHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new checkNewMessage().execute();
                dataHandler.postDelayed(this,10);
            }
        }, 10);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setOnline(false);
    }

    private void setOnline(Boolean status){
        Map<String,Object> state = new HashMap<>();
        state.put("active",status);
        firestore.collection(schemester.getCOLLECTION_USERBASE())
                .document(getStoredEmail())
                .update(state);
    }

    private void setViewsAndListeners(){
        exit = findViewById(R.id.exitRoomBtn);
        menu = findViewById(R.id.infoRoomBtn);
        send = findViewById(R.id.sendMyMsgBtn);
        myTypedMsg = findViewById(R.id.messageTyping);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new checkNetAsync().execute();
                    String[] msgSet = {null,null,null};
                    msgSet[0] = String.valueOf(Calendar.getInstance().getTimeInMillis());
                    msgSet[1] = myTypedMsg.getText().toString();
                    msgSet[2] = getStoredEmail();
                    new sendNewMessage().execute(msgSet);

            }
        });
    }
    private String getStoredEmail(){
        String cred;
        SharedPreferences mSharedPreferences = getSharedPreferences("credentials", MODE_PRIVATE);
        cred =  mSharedPreferences.getString("email", "");
        return cred;
    }

    private class checkNewMessage extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            if(isInternetAvailable()) {
                checkNewMessageInCloud();
            }
            return null;
        }
    }

    private class sendNewMessage extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... keyValUser) {
            if(isInternetAvailable()) {
                sendMessageToCloud(keyValUser[0], keyValUser[1], keyValUser[2]);
            }
            return null;
        }
    }
    private void sendMessageToCloud(final String key, final String value, String uid){
        Map<String,Object> data = new HashMap<>();
        data.put("text",value);
        data.put("time",key);
        data.put("uid",uid);
        firestore.collection(schemester.getCOLLECTION_COLLEGE_CODE())
                .document(schemester.getDOCUMENT_COURSE_CODE())
                .collection(schemester.getCOLLECTION_YEAR_CODE())
                .document("currentmsg")
                .update(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            saveDataToLocalStorage(key,value);
                        }
                    }
                });
    }

    private void checkNewMessageInCloud() {
        firestore.collection(schemester.getCOLLECTION_COLLEGE_CODE())
                .document(schemester.getDOCUMENT_COURSE_CODE())
                .collection(schemester.getCOLLECTION_YEAR_CODE())
                .document("currentmsg")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (Objects.requireNonNull(document).exists()) {
                                long serverlast;
                                Long localLast;
                                serverlast = document.getLong("time");
                                localLast = retrieveMessageFromLocal();
                                if(!(serverlast == localLast)){
                                    Toast.makeText(getApplicationContext(),"New",Toast.LENGTH_SHORT).show();
                                    saveDataToLocalStorage(String.valueOf(document.getLong("time")), document.getString("text"));
                                    mMessageAdapter = new MessageListAdapter(ChatRoomActivity.this, Objects.equals(getStoredEmail(),document.getString("uid")),
                                            document.getString("text"),String.valueOf(document.getLong("time")), document.getString("uid"));
                                } else {
                                    Toast.makeText(getApplicationContext(),"Phew",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });
    }
    private void saveDataToLocalStorage(String mID, String msg){
        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put(mID,msg);
        File file = new File(getDir(TAG_MESSAGE_DATA, MODE_PRIVATE), "msgMap");
        ObjectOutputStream outputStream;
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(file,true));
            outputStream.writeObject(msgMap);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    Long readData;
    private Long retrieveMessageFromLocal(){
        File file = new File(getDir(TAG_MESSAGE_DATA, MODE_PRIVATE), "msgMap");
        ObjectInputStream inputStream = null;
        try {
            inputStream = new ObjectInputStream(new FileInputStream(file));
            inputStream.readObject();
            readData = inputStream.readLong();
            inputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return readData;
    }

    private class checkNetAsync extends AsyncTask<Void,Void,Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            return isInternetAvailable();
        }
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            connected = aBoolean;
            if(!aBoolean) Toast.makeText(getApplicationContext(),"Connection error", Toast.LENGTH_SHORT).show();
            super.onPostExecute(aBoolean);
        }
    }
    private Boolean checkIfEmailVerified() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).isEmailVerified();
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
