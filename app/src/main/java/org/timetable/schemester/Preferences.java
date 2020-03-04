package org.timetable.schemester;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.timetable.schemester.dialog.CustomAlertDialog;
import org.timetable.schemester.dialog.CustomConfirmDialogClass;
import org.timetable.schemester.dialog.CustomDownloadLoadDialog;
import org.timetable.schemester.dialog.CustomLoadDialogClass;
import org.timetable.schemester.dialog.CustomOnOptListener;
import org.timetable.schemester.dialog.CustomTextDialog;
import org.timetable.schemester.dialog.CustomVerificationDialog;
import org.timetable.schemester.listener.OnDialogAlertListener;
import org.timetable.schemester.listener.OnDialogApplyListener;
import org.timetable.schemester.listener.OnDialogConfirmListener;
import org.timetable.schemester.listener.OnDialogDownloadLoadListener;
import org.timetable.schemester.listener.OnDialogLoadListener;
import org.timetable.schemester.listener.OnDialogTextListener;
import org.timetable.schemester.listener.OnOptionChosenListener;
import org.timetable.schemester.student.AdditionalLoginInfo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@TargetApi(Build.VERSION_CODES.Q)
public class Preferences extends AppCompatActivity {
    ApplicationSchemester schemester;
    Switch timeFormatSwitch;
    LinearLayout dobUpdate, emailChange, rollChange, appUpdate, deleteAcc, restarter, themebtn, appUpdateNotif,
            feedback, clockTypeSwitch, loginAgain, anonymOps, userOps, ccySwitch, ccyGroup,devOpsGroup;
    CheckBox appUpdateNotifCheck;
    ImageButton returnbtn;
    TextView timetext;
    CustomVerificationDialog customVerificationDialogDeleteAccount, customVerificationDialogEmailChange;
    CustomLoadDialogClass customLoadDialogClass;
    CustomTextDialog customTextDialog;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    Window window;
    int CODE_DELETE_ACCOUNT = 102, CODE_CHANGE_EMAIL = 101;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        schemester = (ApplicationSchemester) this.getApplication();
        setAppTheme();
        setContentView(R.layout.activity_preferences);
        storeLoginStatus(true);
        window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        findViewsAndSetObjects();
        setThemeConsequences();
        initiateCustomDialogs();
        setListenersAndInitialize();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setAllActivityStarterButtonsDisabled(false);
    }
    @Override
    protected void onResume() {
        super.onResume();
        setAllActivityStarterButtonsDisabled(false);
    }
    //Startup functions
    private void findViewsAndSetObjects(){
        anonymOps = findViewById(R.id.anonymousOptions);
        userOps = findViewById(R.id.accountOptions);
        ccyGroup = findViewById(R.id.ccyOptions);
        devOpsGroup = findViewById(R.id.developerOptions);
        themebtn = findViewById(R.id.themeChangeBtn);
        returnbtn = findViewById(R.id.backBtn);
        loginAgain = findViewById(R.id.loginAgainBtn);
        ccySwitch = findViewById(R.id.ccyUpdateBtn);
        deleteAcc = findViewById(R.id.accountDelete);
        appUpdate = findViewById(R.id.appUpdateBtn);
        emailChange = findViewById(R.id.changeEmailIdBtn);
        rollChange = findViewById(R.id.rollChangebtn);
        dobUpdate = findViewById(R.id.dobupdatebtn);
        restarter = findViewById(R.id.restartBtn);
        timetext = findViewById(R.id.timeformattext);
        feedback = findViewById(R.id.feedbackmailbtn);
        timeFormatSwitch = findViewById(R.id.clockTypeSwitch);
        clockTypeSwitch = findViewById(R.id.clockTypeSwitchView);
        appUpdateNotif = findViewById(R.id.automaticAppUpdatePrefer);
        appUpdateNotifCheck = findViewById(R.id.appUpdateNotificationCheckbox);
    }

    private void setThemeConsequences(){
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
    }
    private void initiateCustomDialogs(){
        customLoadDialogClass = new CustomLoadDialogClass(Preferences.this, new OnDialogLoadListener() {
            @Override
            public void onLoad() { }
            @Override
            public String onLoadText() {
                return schemester.getStringResource(R.string.just_a_moment);
            }
        });

    }
    private void setListenersAndInitialize(){
        appUpdateNotifCheck.setChecked(userWantsUpdateNotification());
        returnbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        loginAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Preferences.this, ModeOfConduct.class));
            }
        });

        ccySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkIfEmailVerified()) {
                    setAllActivityStarterButtonsDisabled(true);
                    Intent ccyIntent = new Intent(Preferences.this, AdditionalLoginInfo.class);
                    startActivity(ccyIntent);
                } else {
                    schemester.toasterLong("Verify your email first, or change email ID if unable to do so");
                }
            }
        });

        deleteAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customVerificationDialogDeleteAccount = new CustomVerificationDialog(Preferences.this, new OnDialogApplyListener() {
                    @Override
                    public void onApply(String email, String password) {
                        customLoadDialogClass.show();
                        authenticate(email, password, CODE_DELETE_ACCOUNT);
                    }
                });
                customVerificationDialogDeleteAccount.show();
            }
        });


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
                                return schemester.getStringResource(R.string.requires_restart);
                            }
                            @Override
                            public String onCallSub() {
                                return schemester.getStringResource(R.string.changing_theme_requires_restart);
                            }
                        });
                        customAlertDialog.show();
                    }
                });
                customOnOptListener.show();
            }
        });

        emailChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            customVerificationDialogEmailChange = new CustomVerificationDialog(Preferences.this, new OnDialogApplyListener() {
                @Override
                public void onApply(String email, String password) {
                    customLoadDialogClass.show();
                    authenticate(email, password, CODE_CHANGE_EMAIL);
                }
            });
                customVerificationDialogEmailChange.show();
                customLoadDialogClass.hide();
            }
        });

        rollChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rollUpdater();
            }
        });

        dobUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkConnected()) {
                    Snackbar snackbar = Snackbar.make(view, schemester.getStringResource(R.string.confirm_to_send_email_link), 5000)
                            .setBackgroundTint(getResources().getColor(R.color.dead_blue))
                            .setTextColor(getResources().getColor(R.color.white));
                    snackbar.setActionTextColor(getResources().getColor(R.color.yellow));
                    snackbar.setAction(schemester.getStringResource(R.string.send), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Snackbar.make(view, schemester.getStringResource(R.string.sending), Snackbar.LENGTH_INDEFINITE)
                                    .setBackgroundTint(getResources().getColor(R.color.dark_blue))
                                    .setTextColor(getResources().getColor(R.color.white))
                                    .show();
                            resetLinkSender(getCredentials()[0]);
                        }
                    });
                    snackbar.show();
                } else {
                    Snackbar.make(view, schemester.getStringResource(R.string.network_error_occurred), Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(R.color.dark_red))
                            .setTextColor(getResources().getColor(R.color.white))
                            .show();
                }
            }
        });

        restarter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                schemester.toasterShort(schemester.getStringResource(R.string.restarting));
                restartApplication();
            }
        });

        timeFormatSwitch.setChecked(getTimeFormat() == 12);
        clockTypeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { timeFormatSwitch.setChecked(!timeFormatSwitch.isChecked()); }
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
                    Snackbar.make(buttonView,schemester.getStringResource(R.string.time_format_12_notify),Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(R.color.dead_blue))
                            .setTextColor(getResources().getColor(R.color.white))
                            .show();
                } else {
                    timetext.setText(getResources().getString(R.string.time_format_24_hours));
                    storeTimeFormat(24);
                    Snackbar.make(buttonView,schemester.getStringResource(R.string.time_format_24_notify),Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(R.color.dead_blue))
                            .setTextColor(getResources().getColor(R.color.white))
                            .show();
                }
            }
        });
        appUpdateNotifCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeUpdateNotificationPreference(!userWantsUpdateNotification());
                appUpdateNotifCheck.setChecked(userWantsUpdateNotification());
            }
        });
        appUpdateNotifCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    Snackbar.make(compoundButton, schemester.getStringResource(R.string.update_notification_enabled_text), 5000)
                            .setBackgroundTint(getResources().getColor(R.color.dead_blue))
                            .setTextColor(getResources().getColor(R.color.white))
                            .show();
                } else {
                    Snackbar.make(compoundButton, schemester.getStringResource(R.string.update_notification_disabled_text), 7000)
                            .setBackgroundTint(getResources().getColor(R.color.dark_red))
                            .setTextColor(getResources().getColor(R.color.white))
                            .setAction("Undo", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    storeUpdateNotificationPreference(true);
                                    appUpdateNotifCheck.setChecked(userWantsUpdateNotification());
                                }
                            })
                            .setActionTextColor(getResources().getColor(R.color.yellow))
                            .show();
                }
            }
        });
        appUpdateNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeUpdateNotificationPreference(!userWantsUpdateNotification());
                appUpdateNotifCheck.setChecked(userWantsUpdateNotification());
            }
        });

        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(schemester.getStringResource(R.string.user_feedback_mail_text));
                Intent web = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(web);
            }
        });
    }

    //Account action functions
    private void resetLinkSender(final String email){
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Snackbar.make(findViewById(R.id.preferencesID), schemester.getStringResource(R.string.email_sent_notif), Snackbar.LENGTH_LONG)
                                .setBackgroundTint(getResources().getColor(R.color.dead_blue))
                                .setTextColor(getResources().getColor(R.color.white))
                                .show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(findViewById(R.id.preferencesID), schemester.getStringResource(R.string.error_occurred_try_later), Snackbar.LENGTH_LONG)
                                .setBackgroundTint(getResources().getColor(R.color.dark_red))
                                .setTextColor(getResources().getColor(R.color.white))
                                .show();
                    }
                });
    }
    private void authenticate(final String uid, String passphrase, final int taskCode){
        customLoadDialogClass.show();
        if(!uid.equals(getCredentials()[0])){
            schemester.toasterLong(schemester.getStringResource(R.string.incorrect_credentials));
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
                            schemester.toasterShort(schemester.getStringResource(R.string.authentication_passed));
                            if(taskCode == CODE_DELETE_ACCOUNT){
                                CustomConfirmDialogClass customConfirmDialogClass = new CustomConfirmDialogClass(Preferences.this, new OnDialogConfirmListener() {
                                    @Override
                                    public void onApply(Boolean confirm) {
                                      if(confirm){
                                          db.collection(schemester.getCOLLECTION_USERBASE()).document(getCredentials()[0]).delete();
                                          deleteUser();
                                      } else { customLoadDialogClass.hide(); }
                                    }
                                    @Override
                                    public String onCallText() {
                                        return schemester.getStringResource(R.string.delete_account_permanently);
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
                            schemester.toasterLong(schemester.getStringResource(R.string.wrong_creds_or_net_problem));
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
                                schemester.toasterLong(schemester.getStringResource(R.string.account_deleted_permanently));
                                Intent i = new Intent(Preferences.this, PositionActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // To clean up all activities
                                startActivity(i);
                                overridePendingTransition(R.anim.enter_from_left, R.anim.exit_from_right);
                            } else {
                                customLoadDialogClass.hide();
                                schemester.toasterShort(schemester.getStringResource(R.string.network_problem));
                            }
                        }
                    });
        } else {
            customLoadDialogClass.hide();
            schemester.toasterLong(schemester.getStringResource(R.string.network_problem));
        }
    }
    private void rollUpdater(){
        customTextDialog = new CustomTextDialog(Preferences.this, new OnDialogTextListener() {
            @Override
            public void onApply(String text) {
                if (text.equals(getCredentials()[1])) {
                    final CustomTextDialog customTextDialog1 = new CustomTextDialog(Preferences.this, new OnDialogTextListener() {
                        @Override
                        public void onApply(String text) {
                            if(text.matches("[0-9]+/[0-9]+")) {
                                storeCredentials(null, text);
                                schemester.toasterLong(schemester.getStringResource(R.string.roll_num_updated));
                            } else { schemester.toasterLong(schemester.getStringResource(R.string.invalid_roll)); }
                        }
                        @Override
                        public String onCallText() { return schemester.getStringResource(R.string.enter_new_roll); }
                        @Override
                        public int textType() {
                            return 7011;        //roll = 7011
                        }
                    });
                    customTextDialog1.setCanceledOnTouchOutside(false);
                    customTextDialog1.show();
                } else { schemester.toasterLong(schemester.getStringResource(R.string.incorrect_roll)); }
            }
            @Override
            public String onCallText() { return schemester.getStringResource(R.string.enter_previous_roll); }
            @Override
            public int textType() {
                return 0;
            }
        });
        customTextDialog.show();
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
                return schemester.getStringResource(R.string.enter_new_email);
            }
            @Override
            public int textType() {
                return 38411;
            }
        });
        customTextDialog.setCanceledOnTouchOutside(false);
        customTextDialog.show();
    }
    private void setEmailIfConfirmed(final String head, final String body, final String updateMail) {
        final CustomConfirmDialogClass customConfirmDialogClass = new CustomConfirmDialogClass(Preferences.this, new OnDialogConfirmListener() {
            @Override
            public void onApply(Boolean confirm) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Objects.requireNonNull(user).updateEmail(updateMail)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    migrateDataAtEmailChange(getCredentials()[0], newMail);
                                    customLoadDialogClass.hide();
                                    storeCredentials(newMail, null);
                                    setEmailChangedAlert("Your new login ID aka email ID is \'" + updateMail + "\'. You'll need to login again with new email ID.");
                                } else {
                                    customLoadDialogClass.hide();
                                    schemester.toasterLong(schemester.getStringResource(R.string.network_problem));
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
        db.collection(schemester.getCOLLECTION_USERBASE()).document(migrateFrom)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> statusData = new HashMap<>();
                            statusData.put(schemester.getFIELD_USER_DEFINITION(), readUserPosition());
                            db.collection(schemester.getCOLLECTION_USERBASE()).document(migrateTo)
                                    .set(statusData, SetOptions.merge());
                            db.collection(schemester.getCOLLECTION_USERBASE()).document(migrateFrom).delete();
                        } else {
                            setEmailIfConfirmed(schemester.getStringResource(R.string.failed_to_migrate), schemester.getStringResource(R.string.migration_failed_text)+"\n"+
                                    schemester.getStringResource(R.string.check_connection_try_again),migrateFrom);
                        }
                    }
                });

    }
    private void setEmailChangedAlert(final String body){
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
                return "Email successfully changed";
            }
            @Override
            public String onCallSub() {
                return body;
            }
        });
        customAlertDialog.show();
    }
    private void readVersionCheckUpdate(){
        if(isNetworkConnected()) {
            db.collection(schemester.COLLECTION_APP_CONFIGURATION).document(schemester.DOCUMENT_VERSION_CURRENT)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (Objects.requireNonNull(document).exists()) {
                                    int vcode = Integer.parseInt(Objects.toString(document.get(schemester.FIELD_VERSION_CODE)));
                                    final String vname = document.getString(schemester.FIELD_VERSION_NAME);
                                    final String link = document.getString(schemester.FIELD_DOWNLOAD_LINK);
                                    customLoadDialogClass.hide();
                                    if (vcode != ApplicationSchemester.versionCode || !Objects.equals(vname, ApplicationSchemester.versionName)) {
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
                                                                    schemester.toasterLong(schemester.getStringResource(R.string.internet_problem));
                                                                }
                                                            } else {
                                                                customLoadDialogClass.dismiss();
                                                            }
                                                        }
                                                        @Override
                                                        public String onCallText() {
                                                            return schemester.getStringResource(R.string.storage_permit_required);
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
                                                return "Your app version : " + ApplicationSchemester.versionName + "\nNew Version : " + vname + "\n\nUpdate to get the latest features and bug fixes. Download will start automatically. \nConfirm to download from website?";
                                            }
                                        });
                                        customConfirmDialogClass.setCanceledOnTouchOutside(false);
                                        customConfirmDialogClass.show();
                                    } else {
                                        schemester.toasterLong(schemester.getStringResource(R.string.app_uptodate_check_later));
                                    }
                                } else {
                                    customLoadDialogClass.hide();
                                    schemester.toasterLong(schemester.getStringResource(R.string.server_error));
                                }
                            } else {
                                customLoadDialogClass.hide();
                                schemester.toasterLong(schemester.getStringResource(R.string.network_problem));
                            }
                        }
                    });
        } else {
            customLoadDialogClass.hide();
            schemester.toasterLong(schemester.getStringResource(R.string.network_problem));
        }
    }

    //App update step functions
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
                    schemester.toasterLong(schemester.getStringResource(R.string.download_interrupted));
                }
            }
        });
        customDownloadLoadDialog.show();
    }
    private void showPackageAlert(final String newVname){
        CustomAlertDialog downloadFinishAlert = new CustomAlertDialog(Preferences.this, new OnDialogAlertListener() {
            @Override
            public void onDismiss() {}
            @Override
            public String onCallText() {
                return "Download completed";
            }
            @Override
            public String onCallSub() {
                return "Latest version is downloaded. \n\nGo to File manager > Internal Storage > Schemester >\n\nHere you'll find the latest package ("+ newVname +" ) to install.\n\n(Delete that file if it is causing problems and try again)";
            }
        });downloadFinishAlert.show();
    }
    private boolean storagePermissionGranted(){
        return (ContextCompat.checkSelfPermission(Preferences.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == (PackageManager.PERMISSION_GRANTED));
    }
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(Preferences.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
    }

    //Stored preference dependent functions
    private void storeCredentials(String mail, String roll){
        SharedPreferences.Editor mEditor = getSharedPreferences(schemester.getPREF_HEAD_CREDENTIALS(), MODE_PRIVATE).edit();
        if(mail != null) mEditor.putString(schemester.getPREF_KEY_EMAIL(), mail).apply();
        if(roll != null) mEditor.putString(schemester.getPREF_KEY_ROLL(), roll).apply();
    }
    private String[] getCredentials(){
        String[] cred = new String[2];
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_CREDENTIALS(), MODE_PRIVATE);
        cred[0] =  mSharedPreferences.getString(schemester.getPREF_KEY_EMAIL(), null);
        cred[1] =  mSharedPreferences.getString(schemester.getPREF_KEY_ROLL(), null);
        return cred;
    }
    private void storeLoginStatus(Boolean logged){
        getSharedPreferences(schemester.PREF_HEAD_LOGIN_STAT, MODE_PRIVATE).edit()
                .putBoolean(schemester.PREF_KEY_LOGIN_STAT, logged).apply();
    }
    private void storeUpdateNotificationPreference(Boolean getUpdateNotification){
        getSharedPreferences(schemester.getPREF_HEAD_UPDATE_NOTIFY(), MODE_PRIVATE).edit()
                .putBoolean(schemester.getPREF_KEY_UPDATE_NOTIFY(),getUpdateNotification).apply();
    }
    private Boolean userWantsUpdateNotification(){
        return this.getSharedPreferences(schemester.getPREF_HEAD_UPDATE_NOTIFY(), MODE_PRIVATE)
                .getBoolean(schemester.getPREF_KEY_UPDATE_NOTIFY(), true);
    }
    private void storeTimeFormat(int format){
        getSharedPreferences(schemester.getPREF_HEAD_TIME_FORMAT(), MODE_PRIVATE).edit()
                .putInt(schemester.getPREF_KEY_TIME_FORMAT(), format).apply();
    }
    private int getTimeFormat() {
        return this.getSharedPreferences(schemester.getPREF_HEAD_TIME_FORMAT(), MODE_PRIVATE)
                .getInt(schemester.getPREF_KEY_TIME_FORMAT(), 24);
    }
    private void storeThemeStatus(int themeChoice){
        getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE).edit()
                .putInt(schemester.getPREF_KEY_THEME(), themeChoice).apply();
    }
    private String readUserPosition(){
        return this.getSharedPreferences(schemester.getPREF_HEAD_USER_DEF(), MODE_PRIVATE)
                .getString(schemester.getPREF_KEY_USER_DEF(), null);
    }
    public void setAppTheme() {
        switch (this.getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE)
                .getInt(schemester.getPREF_KEY_THEME(), 0)) {
            case ApplicationSchemester.CODE_THEME_INCOGNITO: setTheme(R.style.IncognitoTheme);break;
            case ApplicationSchemester.CODE_THEME_DARK: setTheme(R.style.BlueWhiteThemeDark);break;
            case ApplicationSchemester.CODE_THEME_LIGHT: default:setTheme(R.style.BlueWhiteThemeLight);
        }
    }
    private int getThemeStatus() {
        return this.getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE)
                .getInt(schemester.getPREF_KEY_THEME(), 0);
    }

    //Quick action functions
    private Boolean checkIfEmailVerified() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).isEmailVerified();
    }
    private void restartApplication(){
        startActivity(new Intent(Preferences.this, Splash.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_from_right);
    }
    private void setAllActivityStarterButtonsDisabled(Boolean state){
        ccySwitch.setClickable(!state);
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm != null ? Objects.requireNonNull(cm).getActiveNetworkInfo() : null) != null && Objects.requireNonNull(cm.getActiveNetworkInfo()).isConnected();
    }
}
