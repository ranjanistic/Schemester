package org.timetable.schemester;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.timetable.schemester.dialog.CustomConfirmDialogClass;
import org.timetable.schemester.dialog.CustomLoadDialogClass;
import org.timetable.schemester.listener.OnDialogConfirmListener;
import org.timetable.schemester.listener.OnDialogLoadListener;
import org.timetable.schemester.student.AdditionalLoginInfo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;

public class LoginActivity extends AppCompatActivity {
    ApplicationSchemester schemester;
    Button login, forgot;
    EditText emailid, roll, bdate, bmonth, byear;
    TextView emailValid, rollValid;
    FirebaseAuth mAuth;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String dob;
    TextInputLayout collegeRollInputLayout;
    CustomLoadDialogClass customLoadDialogClass;
    CustomConfirmDialogClass confirmEmailDialog;
    Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
    boolean isRollValid = false, isEmailValid = false, isDateValid= false, isTeacher = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        schemester = (ApplicationSchemester) this.getApplication();
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setAppTheme();
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        collegeRollInputLayout = findViewById(R.id.collegeRollLayout);
        if(readUserPosition().equals(schemester.getStringResource(R.string.teacher))){
            isTeacher = true;
            collegeRollInputLayout.setVisibility(View.GONE);
        }
        customLoadDialogClass = new CustomLoadDialogClass(this, new OnDialogLoadListener() {
            @Override
            public void onLoad() {
            }
            @Override
            public String onLoadText() {
                return schemester.getStringResource(R.string.need_few_moments);
            }
        });
        login = findViewById(R.id.registerbtn);
        emailid = findViewById(R.id.emailId);
        emailValid = findViewById(R.id.emailValidityText);
        emailid.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) { checkEmailValidity(emailid.getText().toString().trim(),s); }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int count, int after) {}
        });
        forgot = findViewById(R.id.forgotBtn);
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customLoadDialogClass.show();
                if(isEmailValid) {
                    if(!(emailid.getText().toString().length() == 0)) {
                        CustomConfirmDialogClass customConfirmDialogClass = new CustomConfirmDialogClass(LoginActivity.this, new OnDialogConfirmListener() {
                            @Override
                            public void onApply(Boolean confirm) {
                                if(confirm) {
                                    forgot.setVisibility(View.GONE);
                                    customLoadDialogClass.show();
                                    resetLinkSender(emailid.getText().toString());
                                }
                            }
                            @Override
                            public String onCallText() {
                                return "Reset birth date link";
                            }
                            @Override
                            public String onCallSub() {
                                return "A link will be sent to your provided email ID, if the account already exists and you have forgot your date of birth.\n\nYou can reset your date of birth with that link. Confirm?";
                            }
                        });customConfirmDialogClass.show();
                    } else { schemester.toasterLong(schemester.getStringResource(R.string.request_email_text)); }
                } else { schemester.toasterLong(schemester.getStringResource(R.string.request_email_text)); }
                customLoadDialogClass.hide();
            }
        });
        if(!isTeacher) {
            roll = findViewById(R.id.rollpass);
            rollValid = findViewById(R.id.rollValidityText);
            roll.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) { checkRollNumValidity(roll.getText().toString().trim(), s); }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int count, int after) {}
            });
        } else { isRollValid = true; }

        bdate = findViewById(R.id.birthdate);
        bmonth = findViewById(R.id.birthmonth);
        byear = findViewById(R.id.birthyear);
        bdate.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if(s.length()!=0) {
                    if (Integer.parseInt(bdate.getText().toString()) >= 1 && Integer.parseInt(bdate.getText().toString()) <= 31 && s.length() == 2) {
                        bdate.setTextColor(getResources().getColor(R.color.white));
                        bmonth.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        Objects.requireNonNull(imm).showSoftInput(bmonth, InputMethodManager.SHOW_IMPLICIT);
                        isDateValid = true;
                    }
                }else{
                    bdate.setTextColor(getResources().getColor(R.color.light_red));
                    isDateValid = false;
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int count, int after) {}
        });
        bmonth.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if(s.length()!=0){
                    if(Integer.parseInt(bmonth.getText().toString())>=1 && Integer.parseInt(bmonth.getText().toString())<=12 && s.length() == 2 ){
                        bmonth.setTextColor(getResources().getColor(R.color.white));
                        byear.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        Objects.requireNonNull(imm).showSoftInput(bmonth, InputMethodManager.SHOW_IMPLICIT);
                        isDateValid = true;
                    }
                } else{
                    bmonth.setTextColor(getResources().getColor(R.color.dark_red));
                    isDateValid = false;
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int count, int after) {}
        });

        dob = bdate.getText().toString()+  bmonth.getText().toString() + byear.getText().toString();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerInit();
            }
        });
    }

    @Override
    protected void onResume() {
        if(user!=null && userHasProvidedAdditionalInfo()) { finish(); }
        super.onResume();
    }

    @Override
    protected void onStart() {
        if(user!=null && userHasProvidedAdditionalInfo()){
            Intent passToMain = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(passToMain);
            finish();
        } else if(user!=null && !userHasProvidedAdditionalInfo()){
            FirebaseAuth.getInstance().signOut();
        }
        super.onStart();
    }
    private void registerInit(){
        final String email, rollnum, dd, mm, yyyy;
        email = emailid.getText().toString();
        if(Objects.equals(readUserPosition(),schemester.getStringResource(R.string.student))) rollnum = roll.getText().toString();
        else rollnum = "0";
        dd = bdate.getText().toString();
        mm = bmonth.getText().toString();
        yyyy = byear.getText().toString();

        if (TextUtils.isEmpty(email)) {
            schemester.toasterLong(schemester.getStringResource(R.string.email_id_required));
            return;
        }
        if (!isTeacher&&TextUtils.isEmpty(rollnum)) {
            schemester.toasterLong(schemester.getStringResource(R.string.college_roll_required_text));
            return;
        }
        if (TextUtils.isEmpty(dd)) {
            schemester.toasterLong(schemester.getStringResource(R.string.we_need_your_birthdate));
            return;
        }
        if(Integer.parseInt(dd)<1||Integer.parseInt(dd)>31){
            schemester.toasterLong(schemester.getStringResource(R.string.invalid_bdate));
            return;
        }
        if (TextUtils.isEmpty(mm)) {
            schemester.toasterLong(schemester.getStringResource(R.string.we_need_your_bmonth));
            return;
        }
        if(Integer.parseInt(mm)<1||Integer.parseInt(mm)>12){
            schemester.toasterLong(schemester.getStringResource(R.string.invalid_bmonth));
            return;
        }
        if (TextUtils.isEmpty(yyyy)) {
            schemester.toasterLong(schemester.getStringResource(R.string.we_need_byear));
            return;
        }
        if(Integer.parseInt(yyyy)>calendar.get(Calendar.YEAR)){
            schemester.toasterLong(schemester.getStringResource(R.string.future_year_entry_warning));
            return;
        }
        if(!isEmailValid || !isRollValid &&!isTeacher){
            schemester.toasterLong(schemester.getStringResource(R.string.invalid_details));
            return;
        }
        if(!isDateValid){
            schemester.toasterLong(schemester.getStringResource(R.string.invalid_date_format));
        }
        else {
            confirmEmailDialog = new CustomConfirmDialogClass(LoginActivity.this, new OnDialogConfirmListener() {
                @Override
                public void onApply(Boolean confirm) {
                    dob = dd+mm+yyyy;
                    new registerLoginTask().execute(email, dob);
                }
                @Override
                public String onCallText() {
                    return schemester.getStringResource(R.string.important);
                }

                @Override
                public String onCallSub() {
                    return schemester.getStringResource(R.string.email_ID_provision_disclaimer);
                }
            });
            confirmEmailDialog.show();
            confirmEmailDialog.setCanceledOnTouchOutside(false);
        }
    }

    public class registerLoginTask extends AsyncTask<String,String,Boolean> {
        @Override
        protected Boolean doInBackground(String... creds){
            if(!isInternetAvailable()){
                return false;
            } else {
                String emailCred = creds[0];
                String passCred = creds[1];
                register(emailCred, passCred);
                return true;
            }
        }

        @Override
        protected void onPreExecute() {
            customLoadDialogClass.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result){
            super.onPostExecute(result);
            if(!result) {
                schemester.toasterLong(schemester.getStringResource(R.string.internet_problem));
                customLoadDialogClass.hide();
            }
        }
    }

    private String[] getAdditionalInfo() {
        String[] CCY = {null, null, null};
        SharedPreferences mSharedPreferences = this.getSharedPreferences(schemester.getPREF_HEAD_ADDITIONAL_INFO(), MODE_PRIVATE);
        CCY[0] = mSharedPreferences.getString(schemester.getPREF_KEY_COLLEGE(), "");
        CCY[1] = mSharedPreferences.getString(schemester.getPREF_KEY_COURSE(), "");
        CCY[2] = mSharedPreferences.getString(schemester.getPREF_KEY_YEAR(), "");
        return CCY;
    }
    private Boolean userHasProvidedAdditionalInfo(){
        String[] addInfo = getAdditionalInfo(), nullArray = {"","",""};
        return !Arrays.equals(addInfo, nullArray);
    }
    private void loginUser(final String emailIdFinalLogin, final String passwordFinalLogin){
        mAuth.signInWithEmailAndPassword(emailIdFinalLogin, passwordFinalLogin)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            storeLoginStatus(true);

                            //storeUserDefinition(readUserPosition(), emailIdFinalLogin);
                            if(!isTeacher) { storeCredentials(emailIdFinalLogin, roll.getText().toString()); }
                            else { storeCredentials(emailIdFinalLogin, ""); }

                            if(userHasProvidedAdditionalInfo()) {
                                schemester.toasterLong(schemester.getStringResource(R.string.logged_in_as)+"\n"+ emailIdFinalLogin);
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.enter_from_bottom, R.anim.exit_from_top);
                                finish();
                            } else {
                                schemester.toasterLong(schemester.getStringResource(R.string.complete_your_profile));
                                Intent intent = new Intent(LoginActivity.this, AdditionalLoginInfo.class);
                                startActivity(intent);
                                customLoadDialogClass.hide();
                            }
                        }
                        else {
                            storeLoginStatus(false);
                            schemester.toasterLong(schemester.getStringResource(R.string.incorrect_credentials));
                            forgot.setVisibility(View.VISIBLE);
                            customLoadDialogClass.dismiss();
                        }
                    }
                });
    }

    private void register(final String uid, final String passphrase){
        mAuth.createUserWithEmailAndPassword(uid, passphrase)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        user = FirebaseAuth.getInstance().getCurrentUser();
                        if (task.isSuccessful()) {
                            storeLoginStatus(true);

                            //storeUserDefinition(readUserPosition(), uid);
                            if(!isTeacher) { storeCredentials(uid, roll.getText().toString()); }
                            else { storeCredentials(uid, ""); }

                            Intent intent = new Intent(LoginActivity.this, AdditionalLoginInfo.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                            customLoadDialogClass.hide();
                            finish();
                            sendVerificationEmail();
                        } else {
                            storeLoginStatus(false);
                            loginUser(uid,passphrase);
                        }
                    }
                });
    }


    private void storeLoginStatus(Boolean logged){
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_LOGIN_STAT(), MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean(schemester.getPREF_KEY_LOGIN_STAT(), logged);
        mEditor.apply();
    }

    private void storeCredentials(String mail, String rollnum){
        SharedPreferences mSharedPreferences = getSharedPreferences(schemester.getPREF_HEAD_CREDENTIALS(), MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString(schemester.getPREF_KEY_EMAIL(), mail);
        mEditor.putString(schemester.getPREF_KEY_ROLL(), rollnum);
        mEditor.apply();
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

    private void checkEmailValidity(String emailUnderInspection, Editable s){
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z.]+\\.+[a-z]+";
        if (emailUnderInspection.matches(emailPattern) && s.length() > 0){
            emailValid.setVisibility(View.VISIBLE);
            emailValid.setText(getResources().getString(R.string.valid));
            emailValid.setTextColor(getResources().getColor(R.color.white));
            emailValid.setBackgroundResource(R.drawable.topleftsharpboxgreen);
            isEmailValid = true;
        }
        else if(s.length()==0){
            emailValid.setVisibility(View.GONE);
        } else {
            emailValid.setVisibility(View.VISIBLE);
            emailValid.setText(getResources().getString(R.string.invalidtext));
            emailValid.setTextColor(getResources().getColor(R.color.white));
            emailValid.setBackgroundResource(R.drawable.topleftsharpboxred);
            isEmailValid = false;
        }
    }

    private void checkRollNumValidity(String rollUnderInspection, Editable s){
        String rollPattern = "[0-9]+/[0-9]+";
        if (rollUnderInspection.matches(rollPattern) && s.length() > 0){
            rollValid.setVisibility(View.VISIBLE);
            rollValid.setText(getResources().getString(R.string.valid));
            rollValid.setTextColor(getResources().getColor(R.color.white));
            rollValid.setBackgroundResource(R.drawable.topleftsharpboxgreen);
            isRollValid = true;
        }
        else if(s.length() == 0){
            rollValid.setVisibility(View.GONE);
        } else{
            rollValid.setVisibility(View.VISIBLE);
            rollValid.setText(getResources().getString(R.string.invalidtext));
            rollValid.setTextColor(getResources().getColor(R.color.white));
            rollValid.setBackgroundResource(R.drawable.topleftsharpboxred);
            isRollValid = false;
        }
    }

    private void sendVerificationEmail() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) { schemester.toasterLong(schemester.getStringResource(R.string.confirmation_email_sent_text)); }
                        }
                    });
        }
    }

    private String readUserPosition(){
        SharedPreferences mSharedPreferences = this.getSharedPreferences(schemester.getPREF_HEAD_USER_DEF(), MODE_PRIVATE);
        return mSharedPreferences.getString(schemester.getPREF_KEY_USER_DEF(), "");
    }

    private void resetLinkSender(final String email){
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                schemester.toasterLong(schemester.getStringResource(R.string.a_link_has_been_sent_at)+email);
                            } else { schemester.toasterLong(schemester.getStringResource(R.string.check_your_connection)); }
                        }
                    });
            customLoadDialogClass.hide();
    }

    public void setAppTheme() {
        SharedPreferences mSharedPreferences = this.getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE);
        switch (mSharedPreferences.getInt(schemester.getPREF_KEY_THEME(), 0)) {
            case 102:
                setTheme(R.style.BlueDarkTheme);
                break;
            case 101:
            default:setTheme(R.style.BlueLightTheme);
        }
    }
}
