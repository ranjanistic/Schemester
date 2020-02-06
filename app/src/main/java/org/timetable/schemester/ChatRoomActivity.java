package org.timetable.schemester;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChatRoomActivity extends AppCompatActivity {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    ImageButton pullDown;
    Boolean isCollapsed = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAppTheme(getThemeStatus());
        setContentView(R.layout.activity_chat_room);
        if(!checkIfEmailVerified()){
            Toast.makeText(ChatRoomActivity.this, "Please verify your email first.", Toast.LENGTH_LONG).show();
          //  finish();
        }
        pullDown = findViewById(R.id.pulldownbtn);
        final CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorLayout);
       final LinearLayout roomdetailView = findViewById(R.id.roomDetails);
       final LinearLayout chatDrawer = findViewById(R.id.chatDrawerTop);
        roomdetailView.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);
        chatDrawer.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        coordinatorLayout.setTranslationY((float)(0-roomdetailView.getMeasuredHeight()));
        pullDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isCollapsed) {
                    coordinatorLayout.setTranslationY(roomdetailView.getMeasuredHeight()/2);
                    isCollapsed = false;
                } else {
                    coordinatorLayout.setTranslationY((float)(0-roomdetailView.getMeasuredHeight()));
                    isCollapsed = true;
                }
            }
        });
    }
    private Boolean checkIfEmailVerified() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        return user.isEmailVerified();
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
