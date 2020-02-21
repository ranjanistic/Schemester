package org.timetable.schemester;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.content.ContentValues.TAG;


public class Preferences extends AppCompatActivity {
    Switch timeFormatSwitch;
    LinearLayout dobUpdate, emailChange, rollChange, appUpdate, deleteAcc, restarter, themebtn, feedback, clockTypeSwitch, loginAgain, anonymOps, userOps, ccySwitch, ccyGroup,
            devOpsGroup;
    ImageButton returnbtn;
    CustomVerificationDialog customVerificationDialogDeleteAccount, customVerificationDialogEmailChange;
    CustomLoadDialogClass customLoadDialogClass;
    CustomTextDialog customTextDialog;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    int CODE_DELETE_ACCOUNT = 102, CODE_CHANGE_EMAIL = 101;
    int versionCode = BuildConfig.VERSION_CODE;
    String versionName = BuildConfig.VERSION_NAME;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAppTheme();
        setContentView(R.layout.activity_preferences);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        anonymOps = findViewById(R.id.anonymousOptions);
        userOps = findViewById(R.id.accountOptions);
        ccyGroup = findViewById(R.id.ccyOptions);
        devOpsGroup = findViewById(R.id.developerOptions);
        themebtn = findViewById(R.id.themeChangeBtn);

        if(getThemeStatus() == ApplicationSchemester.CODE_THEME_LIGHT) {
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
            window.setNavigationBarColor(this.getResources().getColor(R.color.blue));
        } else if(getThemeStatus() == ApplicationSchemester.CODE_THEME_INCOGNITO){
            window.setStatusBarColor(this.getResources().getColor(R.color.black_overlay));
            window.setNavigationBarColor(this.getResources().getColor(R.color.black));
        } else {
            window.setStatusBarColor(this.getResources().getColor(R.color.charcoal));
            window.setNavigationBarColor(this.getResources().getColor(R.color.spruce));
        }
        if(getThemeStatus() == ApplicationSchemester.CODE_THEME_INCOGNITO){
            anonymOps.setVisibility(View.VISIBLE);
            userOps.setVisibility(View.GONE);
            ccyGroup.setVisibility(View.GONE);
            devOpsGroup.setVisibility(View.GONE);
            themebtn.setVisibility(View.GONE);
        } else {
            anonymOps.setVisibility(View.GONE);
            userOps.setVisibility(View.VISIBLE);
            ccyGroup.setVisibility(View.VISIBLE);
            devOpsGroup.setVisibility(View.VISIBLE);
            themebtn.setVisibility(View.VISIBLE);
        }

        customLoadDialogClass = new CustomLoadDialogClass(Preferences.this, new OnDialogLoadListener() {
            @Override
            public void onLoad() {
            }
            @Override
            public String onLoadText() {
                return "Just a moment";
            }
        });

        returnbtn = findViewById(R.id.backBtn);
        returnbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        loginAgain = findViewById(R.id.loginAgainBtn);
        loginAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mode = new Intent(Preferences.this, ModeOfConduct.class);
                startActivity(mode);
            }
        });

        ccySwitch = findViewById(R.id.ccyUpdateBtn);
        ccySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkIfEmailVerified()) {
                    Intent ccyIntent = new Intent(Preferences.this, AdditionalLoginInfo.class);
                    startActivity(ccyIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "Verify your email first, or change email ID if unable to do so", Toast.LENGTH_LONG)
                            .show();
                }
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
                customLoadDialogClass.show();
                readVersionCheckUpdate();
            }
        });

        themebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomOnOptListener customOnOptListener = new CustomOnOptListener(Preferences.this, new OnOptionChosenListener() {
                    @Override
                    public void onChoice(int choice) {
                        storeThemeStatus(choice);
                        CustomAlertDialog customAlertDialog = new CustomAlertDialog(Preferences.this, new OnDialogAlertListener() {
                            @Override
                            public void onDismiss() {
                                restartApplication();
                            }
                            @Override
                            public String onCallText() {
                                return "Requires restart";
                            }
                            @Override
                            public String onCallSub() {
                                return "Changing theme requires app restart.";
                            }
                        });
                        customAlertDialog.show();
                    }
                });
                customOnOptListener.show();
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
                rollUpdater();
            }
        });

        dobUpdate = findViewById(R.id.dobupdatebtn);
        dobUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                        if (isNetworkConnected()) {
                            Snackbar snackbar = Snackbar.make(view, "Send a link to your email address for this?", 5000);
                            snackbar.setActionTextColor(getResources().getColor(R.color.green));
                            snackbar.setAction("Send", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Snackbar.make(view, "Sending...", Snackbar.LENGTH_INDEFINITE)
                                            .show();
                                    String[] creds = getCredentials();
                                    resetLinkSender(creds[0]);
                                }
                            });
                            snackbar.show();
                        } else {
                                Snackbar.make(view, "A network error occurred.", Snackbar.LENGTH_LONG)
                                        .show();
                        }
                    }
        });

        restarter = findViewById(R.id.restartBtn);
        restarter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Preferences.this, "Restarting", Toast.LENGTH_SHORT).show();
                restartApplication();
            }
        });
        final TextView timetext = findViewById(R.id.timeformattext);
        timeFormatSwitch = findViewById(R.id.clockTypeSwitch);
        timeFormatSwitch.setChecked(getTimeFormat() == 12);
        clockTypeSwitch = findViewById(R.id.clockTypeSwitchView);
        clockTypeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timeFormatSwitch.isChecked()){
                    timeFormatSwitch.setChecked(false);
                 } else {
                    timeFormatSwitch.setChecked(true);
                }
            }
        });
        if(timeFormatSwitch.isChecked()) {
            timetext.setText(getResources().getString(R.string.time_format_12_hours));
        } else {
            timetext.setText(getResources().getString(R.string.time_format_24_hours));
        }
        timeFormatSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    timetext.setText(getResources().getString(R.string.time_format_12_hours));
                        storeTimeFormat(12);
                    Snackbar.make(buttonView,"Time format set to AM/PM.",Snackbar.LENGTH_LONG)
                            .show();
                } else {
                    timetext.setText(getResources().getString(R.string.time_format_24_hours));
                    storeTimeFormat(24);
                    Snackbar.make(buttonView,"Time format set to 24 hours.",Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        });

        feedback = findViewById(R.id.feedbackmailbtn);
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("mailto:darkmodelabs@gmail.com?cc=priyanshuranjan88@gmail.com&subject=Schemester%20User%20Feedback&body=Dear%20developers,%20");
                Intent web = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(web);
            }
        });
    }
    private Boolean checkIfEmailVerified() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        return user.isEmailVerified();
    }
    private void changeDobResetState(Boolean state, Float alpha){
        dobUpdate.setAlpha(alpha);
        dobUpdate.setClickable(state);
    }
    private void restartApplication(){
        Intent splash = new Intent(Preferences.this, Splash.class);
        splash.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // To clean up all activities
        startActivity(splash);
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_from_right);
    }

    private void resetLinkSender(final String email){
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Snackbar.make(findViewById(R.id.preferencesID), "Email has been sent. Check your mailbox.", Snackbar.LENGTH_LONG)
                                .show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(findViewById(R.id.preferencesID), "An error occurred. Try again later.", Snackbar.LENGTH_LONG)
                                .show();
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
                                          db.collection("userbase").document(getStoredEmail()).delete();
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
                                        return "You cannot recover an account once it is deleted. All of your personal data will be deleted. You'll need to create a new one after that. \n\nConfirm to delete account \'"+uid+"\'?";
                                    }
                                });
                                customConfirmDialogClass.setCanceledOnTouchOutside(false);
                                customConfirmDialogClass.show();
                            } else if(taskCode == CODE_CHANGE_EMAIL){
                                updateEmail();
                            }
                            customLoadDialogClass.hide();
                        } else {
                            Toast.makeText(Preferences.this, "Wrong credentials or network problem", Toast.LENGTH_SHORT).show();
                            customLoadDialogClass.hide();
                        }
                    }
                });
    }
    private void deleteUser(){
        customLoadDialogClass.show();
        if(isNetworkConnected()) {
            user.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                storeLoginStatus(false);
                                customLoadDialogClass.hide();
                                Toast.makeText(Preferences.this, "Your account was deleted permanently.", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(Preferences.this, PositionActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // To clean up all activities
                                startActivity(i);
                                overridePendingTransition(R.anim.enter_from_left, R.anim.exit_from_right);
                            } else {
                                customLoadDialogClass.hide();
                                Toast.makeText(Preferences.this, "Network problem maybe?", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            customLoadDialogClass.hide();
            Toast.makeText(getApplicationContext(), "Network problem?", Toast.LENGTH_LONG).show();
        }
    }

    private String getStoredEmail(){
        String cred;
        SharedPreferences mSharedPreferences = getSharedPreferences("credentials", MODE_PRIVATE);
        cred =  mSharedPreferences.getString("email", "");
        return cred;
    }

    String newMail;
    private void updateEmail(){
        customLoadDialogClass.hide();
        customTextDialog = new CustomTextDialog(Preferences.this, new OnDialogTextListener() {
            @Override
            public void onApply(String text) {
                customLoadDialogClass.show();
                newMail = text;
                setEmailIfConfirmed("Email to be verified", "A verification email will be sent to \'"+newMail+"\'. Confirm this is yours?", newMail);
                customLoadDialogClass.hide();
            }
            @Override
            public String onCallText() {
                return "Enter new email ID";
            }
            @Override
            public int textType() {
                return 38411;
            }
        });
        customTextDialog.setCanceledOnTouchOutside(false);
        customTextDialog.show();
    }

    private void rollUpdater(){
        customTextDialog = new CustomTextDialog(Preferences.this, new OnDialogTextListener() {
            @Override
            public void onApply(String text) {
                String[] readRoll = getCredentials();
                if (text.equals(readRoll[1])) {
                    final CustomTextDialog customTextDialog1 = new CustomTextDialog(Preferences.this, new OnDialogTextListener() {
                        @Override
                        public void onApply(String text) {
                            if(text.matches("[0-9]+/[0-9]+")) {
                                storeCredentials("", text);
                                Toast.makeText(getApplicationContext(), "Roll number updated", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Invalid roll number.", Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public String onCallText() {
                            return "Enter new roll number";
                        }
                        @Override
                        public int textType() {
                            return 7011;        //roll = 7011
                        }
                    });
                    customTextDialog1.setCanceledOnTouchOutside(false);
                    customTextDialog1.show();
                } else {
                    Toast.makeText(getApplicationContext(), "Incorrect roll number", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public String onCallText() {
                return "Enter previous roll number";
            }
            @Override
            public int textType() {
                return 0;
            }
        });
        customTextDialog.show();
    }
    private String[] getCredentials(){
        String[] cred = {"",""};
        SharedPreferences mSharedPreferences = getSharedPreferences("credentials", MODE_PRIVATE);
        cred[0] =  mSharedPreferences.getString("email", "");
        cred[1] =  mSharedPreferences.getString("roll", "");
        return cred;
    }

    private void setEmailIfConfirmed(final String head, final String body, final String updatemail){
        final CustomConfirmDialogClass customConfirmDialogClass = new CustomConfirmDialogClass(Preferences.this, new OnDialogConfirmListener() {
            @Override
            public void onApply(Boolean confirm) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Objects.requireNonNull(user).updateEmail(updatemail)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    migrateDataAtEmailChange(getStoredEmail(), newMail);
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
        customConfirmDialogClass.setCanceledOnTouchOutside(false);
        customConfirmDialogClass.show();
    }

    private void migrateDataAtEmailChange(final String migrateFrom, final String migrateTo) {
        db.collection("userbase").document(migrateFrom)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            String def = document.getString("definition");
                            Map<String, Object> statusData = new HashMap<>();
                            statusData.put("definition", readUserPosition());
                            db.collection("userbase").document(migrateTo)
                                    .set(statusData, SetOptions.merge());
                            db.collection("userbase").document(migrateFrom).delete();
                            return;
                        } else {
                            setEmailIfConfirmed("Failed to migrate", "Existing data failed to migrate, hence email ID was not updated. \nCheck your connection and try again.",migrateFrom);
                            return;
                        }
                    }
                });

    }

    private String readUserPosition(){
        SharedPreferences mSharedPreferences = this.getSharedPreferences("userDefinition", MODE_PRIVATE);
        return mSharedPreferences.getString("position", "");
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

    private void readVersionCheckUpdate(){
        if(isNetworkConnected()) {
            db.collection("appConfig").document("verCurrent")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (Objects.requireNonNull(document).exists()) {
                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                    int vcode = Integer.parseInt(document.get("verCode").toString());
                                    final String vname = document.getString("verName");
                                    final String link = document.getString("downlink");
                                    customLoadDialogClass.hide();
                                    if (vcode != versionCode || !Objects.equals(vname, versionName)) {
                                        Toast.makeText(getApplicationContext(), "Update available", Toast.LENGTH_LONG).show();
                                        final CustomConfirmDialogClass customConfirmDialogClass = new CustomConfirmDialogClass(Preferences.this, new OnDialogConfirmListener() {
                                            @Override
                                            public void onApply(Boolean confirm) {
                                                if (!storagePermissionGranted()) {
                                                    CustomConfirmDialogClass permissionDialog = new CustomConfirmDialogClass(Preferences.this, new OnDialogConfirmListener() {
                                                        @Override
                                                        public void onApply(Boolean confirm) {
                                                            customLoadDialogClass.dismiss();
                                                            requestStoragePermission();
                                                            if (storagePermissionGranted()){
                                                                if(isNetworkConnected()) {
                                                                    File file = new File(Environment.getExternalStorageDirectory() +"/Schemester/org.timetable.schemester-"+vname+".apk");
                                                                    if(file.exists()){
                                                                        showPackageAlert(vname);
                                                                    } else {
                                                                        downloader(link, vname);
                                                                    }
                                                                } else {
                                                                    Toast.makeText(getApplicationContext(), "Internet problem", Toast.LENGTH_LONG).show();
                                                                }
                                                            } else {
                                                                customLoadDialogClass.dismiss();
                                                            }
                                                        }
                                                        @Override
                                                        public String onCallText() {
                                                            return "Storage permission required";
                                                        }
                                                        @Override
                                                        public String onCallSub() {
                                                            return "To download and save the latest version on your device, we need your storage permission. Confirm?";
                                                        }
                                                    });
                                                    permissionDialog.show();
                                                } else {
                                                    File file = new File(Environment.getExternalStorageDirectory() +"/Schemester/org.timetable.schemester-"+vname+".apk");
                                                    if(file.exists()){
                                                        showPackageAlert(vname);
                                                    } else {
                                                        downloader(link,vname);
                                                    }
                                                }
                                            }
                                            @Override
                                            public String onCallText() {
                                                return "An update is available";
                                            }
                                            @Override
                                            public String onCallSub() {
                                                return "Your app version : " + versionName + "\nNew Version : " + vname + "\n\nUpdate to get the latest features and bug fixes. Download will start automatically. \nConfirm to download from website?";
                                            }
                                        });
                                        customConfirmDialogClass.setCanceledOnTouchOutside(false);
                                        customConfirmDialogClass.show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "App is up to date. Check again later.", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    customLoadDialogClass.hide();
                                    Toast.makeText(getApplicationContext(), "Server error.", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                customLoadDialogClass.hide();
                                Toast.makeText(getApplicationContext(), "Network problem?", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            customLoadDialogClass.hide();
            Toast.makeText(getApplicationContext(), "Network problem?", Toast.LENGTH_LONG).show();
        }
    }

    private void downloader(final String link,final  String version){
        CustomDownloadLoadDialog customDownloadLoadDialog = new CustomDownloadLoadDialog(Preferences.this, new OnDialogDownloadLoadListener() {
            @Override
            public String getLink() {
                return link;
            }
            @Override
            public String getVersion() {
                return version;
            }
            @Override
            public void afterFinish(Boolean isCompleted) {
                if (isCompleted) {
                    showPackageAlert(version);
                } else {
                    customLoadDialogClass.hide();
                    Toast.makeText(getApplicationContext(), "Download Interrupted", Toast.LENGTH_SHORT).show();
                }
            }
        });
        customDownloadLoadDialog.show();
    }

    private void showPackageAlert(final String newVname){
        CustomAlertDialog downloadFinishAlert = new CustomAlertDialog(Preferences.this, new OnDialogAlertListener() {
            @Override
            public void onDismiss() {
            }
            @Override
            public String onCallText() {
                return "Download completed";
            }
            @Override
            public String onCallSub() {
                return "Latest version is downloaded. \n\nGo to File manager > Internal Storage > Schemester >\n\nHere you'll find the latest package to install.\n\n(Delete that file if it is causing problems)";
            }
        });
        downloadFinishAlert.show();
    }
    private boolean storagePermissionGranted(){
        return (ContextCompat.checkSelfPermission(Preferences.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED));
    }
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(Preferences.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
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

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private void storeThemeStatus(int themechoice){
        SharedPreferences mSharedPreferences = getSharedPreferences("schemeTheme", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putInt("themeCode", themechoice);
        mEditor.apply();
    }

    private void storeTimeFormat(int format){
        SharedPreferences mSharedPreferences = getSharedPreferences("schemeTime", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putInt("format", format);
        mEditor.apply();
    }
    private int getTimeFormat() {
        SharedPreferences mSharedPreferences = this.getSharedPreferences("schemeTime", MODE_PRIVATE);
        return mSharedPreferences.getInt("format", 24);
    }

    public void setAppTheme() {
        SharedPreferences mSharedPreferences = this.getSharedPreferences("schemeTheme", MODE_PRIVATE);
        switch (mSharedPreferences.getInt("themeCode", 0)) {
            case ApplicationSchemester.CODE_THEME_INCOGNITO:
                setTheme(R.style.IncognitoTheme);break;
            case ApplicationSchemester.CODE_THEME_DARK:
                setTheme(R.style.BlueWhiteThemeDark);
                break;
            case ApplicationSchemester.CODE_THEME_LIGHT:
            default:setTheme(R.style.BlueWhiteThemeLight);
        }
    }
    private int getThemeStatus() {
        SharedPreferences mSharedPreferences = this.getSharedPreferences("schemeTheme", MODE_PRIVATE);
        return mSharedPreferences.getInt("themeCode", 0);
    }
}
