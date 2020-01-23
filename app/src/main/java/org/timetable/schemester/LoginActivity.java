package org.timetable.schemester;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

public class LoginActivity extends AppCompatActivity {
    Button login, forgot;
    EditText emailid, roll, bdate, bmonth, byear;
    TextView emailValid, rollValid;
    FirebaseAuth mAuth;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String dob;
    CustomLoadDialogClass customLoadDialogClass;
    Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
    boolean isRollValid = false, isEmailValid = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(MainActivity.isCreated) {
            MainActivity.mainact.finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        customLoadDialogClass = new CustomLoadDialogClass(this, new OnDialogLoadListener() {
            @Override
            public void onLoad() {
            }
            @Override
            public String onLoadText() {
                return "Need few moments...";
            }
        });
        login = findViewById(R.id.registerbtn);
        emailid = findViewById(R.id.emailId);
        emailValid = findViewById(R.id.emailValidityText);
        emailid.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                checkEmailValidity(emailid.getText().toString().trim(),s);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
        forgot = findViewById(R.id.forgotBtn);
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customLoadDialogClass.show();
                if(isEmailValid) {
                    resetLinkSender(emailid.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext(),"Please provide an email ID", Toast.LENGTH_LONG).show();
                }
                customLoadDialogClass.hide();
            }
        });
        roll = findViewById(R.id.rollpass);
        rollValid = findViewById(R.id.rollValidityText);
        roll.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                checkRollNumValidity(roll.getText().toString().trim(),s);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
        bdate = findViewById(R.id.birthdate);
        bmonth = findViewById(R.id.birthmonth);
        byear = findViewById(R.id.birthyear);
        dob = bdate.getText().toString()+  bmonth.getText().toString() + byear.getText().toString();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerInit();
            }
        });
    }
    private void registerInit(){
        String email, rollnum, dd, mm, yyyy, dob;
        email = emailid.getText().toString();
        rollnum = roll.getText().toString();
        dd = bdate.getText().toString();
        mm = bmonth.getText().toString();
        yyyy = byear.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Email ID required", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(rollnum)) {
            Toast.makeText(getApplicationContext(), "Your college roll number required.", Toast.LENGTH_LONG).show();
            return;
        } else if(!rollnum.trim().contains("18")){
            Toast.makeText(getApplicationContext(), "Only for 2018 batch 2nd year.", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(dd)) {
            Toast.makeText(getApplicationContext(), "We need your birth date.", Toast.LENGTH_LONG).show();
            return;
        }
        if(Integer.parseInt(dd)<1||Integer.parseInt(dd)>31){
            Toast.makeText(getApplicationContext(), "Invalid birth date.", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(mm)) {
            Toast.makeText(getApplicationContext(), "We need your birth month.", Toast.LENGTH_LONG).show();
            return;
        }
        if(Integer.parseInt(mm)<1||Integer.parseInt(mm)>12){
            Toast.makeText(getApplicationContext(), "Invalid birth month.", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(yyyy)) {
            Toast.makeText(getApplicationContext(), "We need your birth year.", Toast.LENGTH_LONG).show();
            return;
        }
        if(Integer.parseInt(yyyy)>calendar.get(Calendar.YEAR)){
            Toast.makeText(getApplicationContext(), "You cannot be born in future!", Toast.LENGTH_LONG).show();
            return;
        }
        if(!isEmailValid || !isRollValid){
            Toast.makeText(getApplicationContext(), "Invalid details", Toast.LENGTH_LONG).show();
            return;
        }
        else {
            customLoadDialogClass.show();
             dob = dd+mm+yyyy;
             if(isInternetAvailable()) {
                 new regisTask().execute(email, dob);
             } else {
                 Toast.makeText(getApplicationContext(),"No internet",Toast.LENGTH_SHORT).show();
                 customLoadDialogClass.hide();
             }
             return;
        }
    }

    public class regisTask extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... creds){
            String emailcred = creds[0];
            String passcred = creds[1];
            register(emailcred, passcred);
            return emailcred;
        }
        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
        }
    }

    private void loginUser(final String emailIdFinalLogin, final String passwordFinalLogin){
        mAuth.signInWithEmailAndPassword(emailIdFinalLogin, passwordFinalLogin)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            storeLoginStatus(true);
                            storeCredentials(emailIdFinalLogin,roll.getText().toString());
                            Toast.makeText(getApplicationContext(), "Logged in as "+emailIdFinalLogin, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.enter_from_bottom, R.anim.exit_from_top);
                            finish();
                        }
                        else {
                            storeLoginStatus(false);
                            Toast.makeText(getApplicationContext(), "Some of your credentials were incorrect.", Toast.LENGTH_LONG).show();
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
                            storeCredentials(uid,roll.getText().toString());
                            Toast.makeText(getApplicationContext(), "Welcome", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            customLoadDialogClass.hide();
                            overridePendingTransition(R.anim.enter_from_bottom, R.anim.exit_from_top);
                            finish();
                            sendVerificationEmail();
                        } else {
                            storeLoginStatus(false);
                            loginUser(uid,passphrase);
                        }
                    }
                });
    }
    private Boolean getLoginStatus(){
        SharedPreferences mSharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        return mSharedPreferences.getBoolean("loginstatus", false);
    }

    private void storeLoginStatus(Boolean logged){
        SharedPreferences mSharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean("loginstatus", logged);
        mEditor.apply();
    }

    private void storeCredentials(String mail, String rollnum){
        SharedPreferences mSharedPreferences = getSharedPreferences("credentials", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString("email", mail);
        mEditor.putString("roll", rollnum);
        mEditor.apply();
    }
    public boolean isInternetAvailable() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }

    private void checkEmailValidity(String emailUnderInspection, Editable s){
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z.]+\\.+[a-z]+";
        if (emailUnderInspection.matches(emailPattern) && s.length() > 0){
            emailValid.setVisibility(View.VISIBLE);
            emailValid.setText(getResources().getString(R.string.valid));
            emailValid.setTextColor(getResources().getColor(R.color.white));
            emailValid.setBackgroundResource(R.drawable.roundcontainerboxgreen);
            isEmailValid = true;
        }
        else if(s.length()==0){
            emailValid.setVisibility(View.GONE);
        } else {
            emailValid.setVisibility(View.VISIBLE);
            emailValid.setText(getResources().getString(R.string.invalidtext));
            emailValid.setTextColor(getResources().getColor(R.color.white));
            emailValid.setBackgroundResource(R.drawable.roundcontainerboxred);
            isEmailValid = false;
        }
    }

    private void checkRollNumValidity(String rollUnderInspection, Editable s){
        String rollPattern = "[0-9]+/[0-9]+";
        if (rollUnderInspection.matches(rollPattern) && s.length() > 0){
            rollValid.setVisibility(View.VISIBLE);
            rollValid.setText(getResources().getString(R.string.valid));
            rollValid.setTextColor(getResources().getColor(R.color.white));
            rollValid.setBackgroundResource(R.drawable.roundcontainerboxgreen);
            isRollValid = true;
        }
        else if(s.length() == 0){
            rollValid.setVisibility(View.GONE);
        } else{
            rollValid.setVisibility(View.VISIBLE);
            rollValid.setText(getResources().getString(R.string.invalidtext));
            rollValid.setTextColor(getResources().getColor(R.color.white));
            rollValid.setBackgroundResource(R.drawable.roundcontainerboxred);
            isRollValid = false;
        }
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

    private void resetLinkSender(final String email){
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(),"A link has been sent at "+email, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(),"Check your connection", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
    }
    private Boolean checkIfEmailVerified() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        return user.isEmailVerified();
    }
}