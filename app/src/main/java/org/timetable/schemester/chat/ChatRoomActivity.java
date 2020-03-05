package org.timetable.schemester.chat;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.timetable.schemester.ApplicationSchemester;
import org.timetable.schemester.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
@TargetApi(Build.VERSION_CODES.Q)
public class ChatRoomActivity extends AppCompatActivity {
    ApplicationSchemester schemester;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    ImageButton exit, menu, send;
    EditText myTypedMsg;
    TextView roomName;
    ListView listView;
    String timeFormat;
    String appFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Schemester";
    String MUTFile = appFolder;
    String UTypeFile = appFolder;
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
        File path = new File(appFolder);
        if(!path.exists()) {
            if (path.mkdir()) {
                schemester.toasterShort("directory created");
            }
        } else {
            schemester.toasterShort("directory exists");
        }
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
    String time;
    private void setClickListeners(){
        exit.setOnClickListener(view -> finish());

        //TODO: menu
        menu.setOnClickListener(view -> new readMessageAsync().execute());

        //TODO: send to server --> read from server -->check if same message -->if not-->populate local storage old-->else-->append local storage-->populate local storage new
        send.setOnClickListener(view -> {
            time = new SimpleDateFormat(timeFormat, Locale.getDefault()).format(new Date());
            if(messageIsEligible(myTypedMsg.getText().toString())) {
                new checkNetAsync().execute();
            } else{
                schemester.toasterLong("no");
            }
        });
    }

    private class readMessageAsync extends AsyncTask<Void,Void,Boolean>{
        @Override
        protected Boolean doInBackground(Void... voids) {
            return readMessageFromDatabase();
        }
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            schemester.toasterShort("read database");

            if(aBoolean) {
                schemester.toasterShort("populate appended");
                //TODO:display messages from local file ,appended--> populateChatRoomMessagesView(localFileMessageArray, localFileUIDArray ,localFileTimeArray, LocalFileUserTypeArray);
            } else {
                schemester.toasterShort("populate old");

                //TODO:display messages from local file ,!appended--> setLocalAvailableMessages(localFileMessageArray, localFileUIDArray ,localFileTimeArray, LocalFileUserTypeArray);
            }
            super.onPostExecute(aBoolean);
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
                .addOnSuccessListener(aVoid -> {
                    myTypedMsg.setText("");
                    mut.clear();
                })
                .addOnFailureListener(e -> schemester.toasterShort("Server write error"));
    }

    private boolean readMessageFromDatabase(){
        final boolean[] newmessage = new boolean[1];
        firestore.collection(schemester.getCOLLECTION_COLLEGE_CODE())
                .document(schemester.getDOCUMENT_COURSE_CODE())
                .collection(schemester.getCOLLECTION_YEAR_CODE())
                .document("currentmsg")
                .get()
                .addOnCompleteListener(task -> {
                    String[] serverMUT = new String[3];
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (Objects.requireNonNull(document).exists()) {
                            serverMUT[0] = document.getString("text");
                            serverMUT[1] = document.getString("uid");
                            serverMUT[2] = document.getString("time");
                            String[][] set = getMessageSetFromLocalStorage();
                            if(!Arrays.equals(serverMUT, getLastMUT())){
                                setLastMUT(serverMUT);
                                schemester.toasterLong("new new");
                                newmessage[0] = true;
                                deviceMessageCount(1);
                                appendMessageSetToLocalStorage(serverMUT[0],serverMUT[1],serverMUT[2]);
                                appendUserTypeSetToLocalStorage(serverMUT[1].equals(getStoredEmail())?USER_ME:USER_OTHER);
                                populateChatRoomMessagesView(set[0], set[1], set[2], getUserTypeSetFromLocalStorage());
                                //TODO: appendLocalFile(serverMUT[0], serverMUT[1], serverMUT[2], serverMUT[1].equals(getStoredEmail())?USER_ME:USER_OTHER);
                            } else {
                                newmessage[0]  = false;
                                schemester.toasterLong("Nothing new");
                                setLocalAvailableMessages(set[0], set[1], set[2], getUserTypeSetFromLocalStorage());
                            }
                        }
                    }
                });
        return newmessage[0];
    }


    private String[][] getMessageSetFromLocalStorage(){
        String[] message = new String[(int)deviceMessageCount(0)],
                uid = new String[(int)deviceMessageCount(0)],
                time = new String[(int)deviceMessageCount(0)];
        String[] set = new String[(int)deviceMessageCount(0)];

        File file = new File(MUTFile, "/MUT.txt");
        if(file.exists()) {
            try {
                int i = 0;
                while (i < deviceMessageCount(0)) {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    try {
                        set[i] = br.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ++i;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            int k = 0;
            while (k < set.length) {
                char[] line = set[k].toCharArray();              // a b c : d e f : g h
                int c = 0;
                int count = 0;
                int m = 0;
                while (c < line.length) {
                    while (count < 2) {
                        if (line[c] == '|') {
                            while (m < c) {
                                if (count == 0) {
                                    message[k] += line[m];          //abc
                                } else if (count == 1) {
                                    uid[k] += line[m];
                                }
                                ++m;
                            }
                            ++count;
                        } else if(line[c] == '\n'){
                            count = 3;
                        }
                    }
                    m = c + 1;
                    ++c;
                    if (count == 2) {
                        time[k] += line[c];
                    } else {
                        c = line.length;
                    }
                }
                ++k;
            }
        } else {
            schemester.toasterShort("no MUT");
        }
        return new String[][]{message, uid,time};
    }

    private void appendMessageSetToLocalStorage(String message, String uid, String time){
        File file = new File(MUTFile,"/MUT.txt");
        String set = message+"|"+uid+"|"+time+"\n";
        if(!file.exists()){
            try {
                final boolean newFile = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(file,true);
            try {
                outputStream.write(set.getBytes());
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private int[] getUserTypeSetFromLocalStorage(){
        int[] type = new int[(int)deviceMessageCount(0)];
        File file = new File(UTypeFile,"/UType.txt");
        if(file.exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(file);
                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    int i = 0;
                    while (i < deviceMessageCount(0)) {
                        while(!(br.read() =='\n')) {
                            type[i] = Integer.parseInt(br.readLine());
                        }
                        ++i;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            schemester.toasterShort("no UTYPE");
        }
        return type;
    }
    private void appendUserTypeSetToLocalStorage(int type){
        File file = new File(UTypeFile,"/UType.txt");
        if(!file.exists()){
            try {
                final boolean newFile = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(file,true);
            try {
                String numString = type + "\n";
                outputStream.write(numString.getBytes());
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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

    /**gets arrays of messages, with associated id,time and sender type arrays, increments device message count by 1, and sends the total number of
     * data entries to ChatRoomModel.java, which then adds every data set by looping till total sets, and MessageListAdapter then
     * sets values to display according to user type of each data set (model).
     */
    private void populateChatRoomMessagesView(String[] message, String[] id, String[] time, int[] sender) {
            ArrayList<ChatRoomModel> arrayOfUsers = ChatRoomModel.setModel(message, id, time, deviceMessageCount(1), sender);
            MessageListAdapter adapter = new MessageListAdapter(this, arrayOfUsers);
            listView.setAdapter(adapter);
    }

    private void setLocalAvailableMessages(String[] message, String[] id, String[] time, int[] sender){
        ArrayList<ChatRoomModel> arrayOfUsers = ChatRoomModel.setModel(message,id,time,deviceMessageCount(0),sender);
        MessageListAdapter adapter = new MessageListAdapter(this, arrayOfUsers);
        listView.setAdapter(adapter);
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
            if(isInternetAvailable()){
                setMessageToDatabase(new String[]{myTypedMsg.getText().toString(), getStoredEmail(), time});
                return true;
            }
            return false;
        }
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (!aBoolean) {
                schemester.toasterLong(schemester.getStringResource(R.string.internet_error));
            } else new readMessageAsync().execute();
        }
    }
    private String getStoredEmail(){
        return getSharedPreferences(schemester.getPREF_HEAD_CREDENTIALS(), MODE_PRIVATE)
                .getString(schemester.getPREF_KEY_EMAIL(), null);
    }
    private Boolean checkIfEmailVerified() { return Objects.requireNonNull(user).isEmailVerified(); }
    private void setAppTheme() {
        switch (getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE).getInt(schemester.getPREF_KEY_THEME(), 0)) {
            case ApplicationSchemester.CODE_THEME_DARK: setTheme(R.style.BlueDarkTheme);break;
            case ApplicationSchemester.CODE_THEME_LIGHT: default:setTheme(R.style.BlueLightTheme);
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
