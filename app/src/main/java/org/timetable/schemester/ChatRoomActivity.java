package org.timetable.schemester;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ListAdapter;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.protobuf.Internal;

import java.io.IOException;
import java.lang.ref.Reference;
import java.nio.channels.CompletionHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ChatRoomActivity extends AppCompatActivity {
    ApplicationSchemester schemester;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseListAdapter<ChatRoomModel> adapter;
    ImageButton exit, menu, send;
    EditText myTypedMsg;
    TextView roomName;
    ListView listView;
    CheckNet checkNet;
    ChatRoomModel model;
    String timeFormat;
    int USER_ME = 202, USER_OTHER = 201;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        schemester = (ApplicationSchemester) this.getApplication();
        setAppTheme();
        setContentView(R.layout.activity_chat_room);
        if(!checkIfEmailVerified() || user==null){
            schemester.toasterLong("Email not verified, or login again");
            finish();
        }
        setViewsAndDefaults();
        setClickListeners();
        checkNet = new CheckNet(this);
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
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        setOnline(false);
    }

    private void setOnline(Boolean status){
        Map<String,Object> state = new HashMap<>();
        state.put("active",status);
        firestore.collection(schemester.getCOLLECTION_USERBASE()).document(getStoredEmail())
                .update(state);
    }

    private void setViewsAndDefaults() {
         listView = findViewById(R.id.messageList);
         roomName = findViewById(R.id.chatRoomName);
        exit = findViewById(R.id.exitRoomBtn);
        menu = findViewById(R.id.infoRoomBtn);
        send = findViewById(R.id.sendMyMsgBtn);
        myTypedMsg = findViewById(R.id.messageTyping);
        String room = schemester.getCOLLECTION_COLLEGE_CODE()+", "+ schemester.getDOCUMENT_COURSE_CODE()+", "
                +schemester.getCOLLECTION_YEAR_CODE();
        roomName.setText(room);
        if(getTimeFormat() == 12) {
            timeFormat = schemester.getStringResource(R.string.time_format_hhmmss_ampm);
        } else timeFormat = schemester.getStringResource(R.string.time_pattern_hhmmss);
    }

    private void setClickListeners(){
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //TODO: menu
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new readMessageAsync().execute();
            }
        });

        //TODO: send to server --> read from server --> check if same message -->ifnot-->populate again-->else-->read server
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String time = new SimpleDateFormat(timeFormat, Locale.getDefault()).format(new Date());
                if(messageIsEligible(myTypedMsg.getText().toString())) {
                    setMessageToDatabase(new String[]{myTypedMsg.getText().toString(), getStoredEmail(), time});
                    //populateUsersList(myTypedMsg.getText().toString(), getStoredEmail(), time, USER_ME);
                } else{
                    schemester.toasterLong("no");
                }
            }
        });
    }

    private class readMessageAsync extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            readMessageFromDatabase();
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            schemester.toasterShort("read database");
            super.onPostExecute(aVoid);
        }
    }

    private void setMessageToDatabase(final String[] localMUT){
        final Map<String,Object> mut = new HashMap<>();
        mut.put("text",localMUT[0]);
        mut.put("uid", localMUT[1]);
        mut.put("time", localMUT[2]);
        firestore.collection(schemester.getCOLLECTION_COLLEGE_CODE())
                .document(schemester.getDOCUMENT_COURSE_CODE())
                .collection(schemester.getCOLLECTION_YEAR_CODE())
                .document("currentmsg")
                .update(mut)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        myTypedMsg.setText("");
//                        setLastMUT(localMUT);
                        mut.clear();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        schemester.toasterShort("Server write error");
                    }
                });
    }

    private void readMessageFromDatabase(){
        firestore.collection(schemester.getCOLLECTION_COLLEGE_CODE())
                .document(schemester.getDOCUMENT_COURSE_CODE())
                .collection(schemester.getCOLLECTION_YEAR_CODE())
                .document("currentmsg")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        String[] serverMUT = new String[3];
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (Objects.requireNonNull(document).exists()) {
                                serverMUT[0] = document.getString("text");
                                serverMUT[1] = document.getString("uid");
                                serverMUT[2] = document.getString("time");
                                if(!Arrays.equals(serverMUT, getLastMUT())){
                                    setLastMUT(serverMUT);
                                    schemester.toasterLong("new new");
                                    if(serverMUT[1].equals(getStoredEmail())) {
                                        //TODO: appendLocalFile(serverMUT[0], serverMUT[1], serverMUT[2], USER_ME);
                                        //populateUsersList(serverMUT[0], serverMUT[1], serverMUT[2], USER_ME);
                                    } else {
                                        //TODO: appendLocalFile(serverMUT[0], serverMUT[1], serverMUT[2], USER_OTHER);
                                        //populateUsersList(serverMUT[0], serverMUT[1], serverMUT[2], USER_OTHER);
                                    }
                                    //TODO: populateUsersList(localFileMessageArray, localFileUIDArray ,localFileTimeArray, LocalFileUserTypeArray);
                                } else {
                                    schemester.toasterLong("Nothing new");
                                }
                            }
                        }
                    }
                });
    }

    private String[] getLastMUT(){
        String[] mut = new String[3];
        SharedPreferences preferences = getSharedPreferences(schemester.getPREF_HEAD_MESSAGE_DATA(), MODE_PRIVATE);
        mut[0] = preferences.getString("lastMessage", null);
        mut[1] = preferences.getString("lastUser", null);
        mut[2] = preferences.getString("lastTime",null);
        return mut;
    }
    private void setLastMUT(String[] mut){
        getSharedPreferences(schemester.getPREF_HEAD_MESSAGE_DATA(), MODE_PRIVATE).edit()
                .putString("lastMessage", mut[0])
                .putString("lastUser", mut[1])
                .putString("lastTime", mut[2])
                .apply();
    }

    private Boolean messageIsEligible(String text){
        return !text.equals("");
    }
    private void populateUsersList(String[] message, String[] id, String[] time, int[] sender) {
            ArrayList<ChatRoomModel> arrayOfUsers = ChatRoomModel.setModel(message, id, time, deviceMessageCount(1), sender);
            MessageListAdapter adapter = new MessageListAdapter(this, arrayOfUsers);
            listView.setAdapter(adapter);
    }
    //private void getLocalAvailableMessages()
    private void setLocalAvailableMessages(String[] message, String[] id, String[] time, int[] sender){
        ArrayList<ChatRoomModel> arrayOfUsers = ChatRoomModel.setModel(message,id,time,deviceMessageCount(0),sender);
        MessageListAdapter adapter = new MessageListAdapter(this, arrayOfUsers);
        listView.setAdapter(adapter);
    }

    private void deviceMessages(){

    }
    private long deviceMessageCount(int increment){
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_MESSAGE_DATA(), MODE_PRIVATE);
        long old = mSharedPreferences.getLong(schemester.getPREF_KEY_MESSAGE_COUNT(),0);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putLong(schemester.getPREF_KEY_MESSAGE_COUNT(), old+increment);
        mEditor.apply();
        return mSharedPreferences.getLong(schemester.getPREF_KEY_MESSAGE_COUNT(),0);
    }

    private class checkNetAsync extends AsyncTask<Void,Void,Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            return isInternetAvailable();
        }
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (!aBoolean) {
                schemester.toasterLong(schemester.getStringResource(R.string.internet_error));
            }
        }
    }
    private String getStoredEmail(){
        return getSharedPreferences(schemester.getPREF_HEAD_CREDENTIALS(), MODE_PRIVATE)
                .getString(schemester.getPREF_KEY_EMAIL(), null);
    }
    private Boolean checkIfEmailVerified() {
        return Objects.requireNonNull(user).isEmailVerified();
    }
    private void setAppTheme() {
        switch (getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE).getInt(schemester.getPREF_KEY_THEME(), 0)) {
            case ApplicationSchemester.CODE_THEME_DARK: setTheme(R.style.BlueDarkTheme);break;
            case ApplicationSchemester.CODE_THEME_LIGHT:
            default:setTheme(R.style.BlueLightTheme);
        }
    }

    private int getTimeFormat() {
        return getSharedPreferences(schemester.getPREF_HEAD_TIME_FORMAT(), MODE_PRIVATE)
                .getInt(schemester.getPREF_KEY_TIME_FORMAT(), 24);
    }
    private boolean isInternetAvailable() {
        try { return Runtime.getRuntime().exec("/system/bin/ping -c 1 8.8.8.8").waitFor() == 0; }
        catch (IOException | InterruptedException e) { e.printStackTrace(); }
        return false;
    }
}
