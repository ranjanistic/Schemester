package org.timetable.schemester;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class Preferences extends AppCompatActivity {
    Switch notificationSwitch;
    ImageButton dobUpdate, emailChange, rollChange, appUpdate, deleteAcc, returnbtn;
    CustomVerificationDialog customVerificationDialogDeleteAccount, customVerificationDialogEmailChange;
    CustomLoadDialogClass customLoadDialogClass;
    CustomTextDialog customTextDialog;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    int CODE_DELETE_ACCOUNT = 102, CODE_CHANGE_EMAIL = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        customLoadDialogClass = new CustomLoadDialogClass(Preferences.this, new OnDialogLoadListener() {
            @Override
            public void onLoad() {
            }
            @Override
            public String onLoadText() {
                return "Hold up";
            }
        });
        returnbtn = findViewById(R.id.backBtn);
        returnbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Preferences.this, FullScheduleActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.enter_from_left,R.anim.exit_from_right);
            }
        });

        deleteAcc = findViewById(R.id.accountDelete);
        deleteAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customVerificationDialogDeleteAccount.show();
            }
        });
        customVerificationDialogDeleteAccount = new CustomVerificationDialog(Preferences.this, new OnDialogApplyListener() {
            @Override
            public void onApply(String email, String password) {
                customLoadDialogClass.show();
                authenticate(email, password, CODE_DELETE_ACCOUNT);
            }
        });

        appUpdate = findViewById(R.id.appUpdateBtn);
        appUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        emailChange = findViewById(R.id.changeEmailIdBtn);
        emailChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customVerificationDialogEmailChange.show();
                customLoadDialogClass.hide();
            }
        });
        customVerificationDialogEmailChange = new CustomVerificationDialog(Preferences.this, new OnDialogApplyListener() {
            @Override
            public void onApply(String email, String password) {
                customLoadDialogClass.show();
                authenticate(email, password, CODE_CHANGE_EMAIL);
            }
        });

        rollChange = findViewById(R.id.rollChangebtn);
        rollChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String getroll = rollReturner("Enter previous roll number");
                String[] readRoll = getCredentials();
                if(getroll.equals(readRoll[1])){
                    storeCredentials("",rollReturner("Set new roll number"));
                }
            }
        });

        dobUpdate = findViewById(R.id.dobupdatebtn);
        dobUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }


    private void authenticate(final String uid, String passphrase, final int taskCode){
        customLoadDialogClass.show();
        String[] creds;
        creds = getCredentials();
        if(!uid.equals(creds[0])){
            Toast.makeText(Preferences.this, "Wrong credentials.", Toast.LENGTH_SHORT).show();
            customLoadDialogClass.hide();
            return;
        }
        AuthCredential credential = EmailAuthProvider
                .getCredential(uid, passphrase);
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Preferences.this, "Authentication passed", Toast.LENGTH_SHORT).show();
                            if(taskCode == CODE_DELETE_ACCOUNT){
                                CustomConfirmDialogClass customConfirmDialogClass = new CustomConfirmDialogClass(Preferences.this, new OnDialogConfirmListener() {
                                    @Override
                                    public void onApply(Boolean confirm) {
                                      if(confirm){
                                          deleteUser();
                                      } else {
                                          customLoadDialogClass.hide();
                                      }
                                    }
                                    @Override
                                    public String onCallText() {
                                        return "Delete account permanently?";
                                    }
                                    @Override
                                    public String onCallSub() {
                                        return "You cannot recover an account once it is deleted. You'll need to create a new one after that. Confirm to delete account \'"+uid+"\'?";
                                    }
                                });
                                customConfirmDialogClass.setCanceledOnTouchOutside(false);
                                customConfirmDialogClass.show();
                            } else if(taskCode == CODE_CHANGE_EMAIL){
                                updateEmail();
                            }
                            customLoadDialogClass.hide();
                        } else {
                            Toast.makeText(Preferences.this, "Wrong credentials or network problem.", Toast.LENGTH_SHORT).show();
                            customLoadDialogClass.hide();
                        }
                    }
                });
    }
    private void deleteUser(){
        user.delete()
                .addOnCompleteListener (new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            storeLoginStatus(false);
                            customLoadDialogClass.hide();
                            Toast.makeText(Preferences.this, "Your account was deleted permanently.", Toast.LENGTH_SHORT).show();
                            Intent i=new Intent(Preferences.this, LoginActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // To clean up all activities
                            startActivity(i);
                            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_from_right);
                        } else {
                            customLoadDialogClass.hide();
                            Toast.makeText(Preferences.this, "Network problem maybe?", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    String newMail;
    private void updateEmail(){
        customLoadDialogClass.hide();
        customTextDialog = new CustomTextDialog(Preferences.this, new OnDialogTextListener() {
            @Override
            public void onApply(String text) {
                newMail = text;
                setEmailIfConfirmed("Email to be verified", "A verification email will be sent to \'"+newMail+"\'. Confirm this is yours?", newMail);
                customLoadDialogClass.hide();
            }
            @Override
            public String onCallText() {
                return "Enter new email ID";
            }
        });
        customTextDialog.show();
    }

    String rollinput;
    private String rollReturner(final String heading){

        customTextDialog = new CustomTextDialog(Preferences.this, new OnDialogTextListener() {
            @Override
            public void onApply(String text) {
                rollinput = text;
            }
            @Override
            public String onCallText() {
                return heading;
            }
        });
        customTextDialog.show();
        return rollinput;
    }
    private String[] getCredentials(){
        String[] cred = {"",""};
        SharedPreferences mSharedPreferences = getSharedPreferences("credentials", MODE_PRIVATE);
        cred[0] =  mSharedPreferences.getString("email", "");
        cred[1] =  mSharedPreferences.getString("roll", "");
        return cred;
    }

    private void setEmailIfConfirmed(final String head, final String body, final String updatemail){
        customLoadDialogClass.show();
        CustomConfirmDialogClass customConfirmDialogClass = new CustomConfirmDialogClass(Preferences.this, new OnDialogConfirmListener() {
            @Override
            public void onApply(Boolean confirm) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Objects.requireNonNull(user).updateEmail(updatemail)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    customLoadDialogClass.hide();
                                    storeCredentials(newMail,"");
                                    setAlert("Email successfully changed","Your new login ID aka email ID is \'"+updatemail+"\'. You'll need to login again with new email ID.");
                                } else {
                                    customLoadDialogClass.hide();
                                    Toast.makeText(getApplicationContext(), "Network problem?", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                customLoadDialogClass.hide();
            }

            @Override
            public String onCallText() {
                return head;
            }

            @Override
            public String onCallSub() {
                return body;
            }
        });
        customConfirmDialogClass.show();
    }

    private void setAlert(final String head, final String body){

        CustomAlertDialog customAlertDialog = new CustomAlertDialog(Preferences.this, new OnDialogAlertListener() {
            @Override
            public void onDismiss(){
                FirebaseAuth.getInstance().signOut();
                Intent i=new Intent(Preferences.this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // To clean up all activities
                startActivity(i);
                overridePendingTransition(R.anim.enter_from_left, R.anim.exit_from_right);
            }
            @Override
            public String onCallText() {
                return head;
            }
            @Override
            public String onCallSub() {
                return body;
            }
        });
        customAlertDialog.show();

    }

    private void sendVerificationEmail() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),"A confirmation email is sent to your new email address.", Toast.LENGTH_LONG).show();
                        } else{
                            Toast.makeText(getApplicationContext(), "Network problem?", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void storeCredentials(String mail, String rollnum){
        SharedPreferences mSharedPreferences = getSharedPreferences("credentials", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        if(!mail.equals("")) {
            mEditor.putString("email", mail);
            mEditor.apply();
        }
        if(!rollnum.equals("")) {
            mEditor.putString("roll", rollnum);
            mEditor.apply();
        }
    }

    private void storeLoginStatus(Boolean logged){
        SharedPreferences mSharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean("loginstatus", logged);
        mEditor.apply();
    }

}
