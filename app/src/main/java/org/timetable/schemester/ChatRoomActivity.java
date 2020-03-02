package org.timetable.schemester;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ListAdapter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    }

    private void setClickListeners(){
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
                String time = new SimpleDateFormat(schemester.getStringResource(R.string.time_format_hhmm_ampm), Locale.getDefault()).format(new Date());
                populateUsersList(myTypedMsg.getText().toString(),getStoredEmail(), time, USER_OTHER);
                myTypedMsg.setText(null);
            }
        });
    }

    private void populateUsersList(String message, String id, String time, int sender) {
        ArrayList<ChatRoomModel> arrayOfUsers = ChatRoomModel.setModel(message,id,time,25);
        MessageListAdapter adapter = new MessageListAdapter(this, arrayOfUsers, sender);
        listView.setAdapter(adapter);
    }

    private String getStoredEmail(){
        String cred;
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_CREDENTIALS(), MODE_PRIVATE);
        cred =  mSharedPreferences.getString(schemester.getPREF_KEY_EMAIL(), null);
        return cred;
    }
    private Boolean checkIfEmailVerified() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).isEmailVerified();
    }

    public void setAppTheme() {
        switch (this.getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE)
                .getInt(schemester.getPREF_KEY_THEME(), 0)) {
            case ApplicationSchemester.CODE_THEME_DARK:
                setTheme(R.style.BlueDarkTheme);
                break;
            case ApplicationSchemester.CODE_THEME_LIGHT:
            default:setTheme(R.style.BlueLightTheme);
        }
    }
}
