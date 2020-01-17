package org.timetable.schedule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    Button login;
    EditText emailid, roll, bdate, bmonth, byear;
    FirebaseAuth mAuth;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String dob;
    CustomLoadDialogClass customLoadDialogClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.blue));
        window.setNavigationBarColor(this.getResources().getColor(R.color.blue));
        mAuth = FirebaseAuth.getInstance();
        login = findViewById(R.id.registerbtn);
        emailid = findViewById(R.id.emailId);
        roll = findViewById(R.id.rollpass);
        bdate = findViewById(R.id.birthdate);
        bmonth = findViewById(R.id.birthmonth);
        byear = findViewById(R.id.birthyear);
        dob = bdate.getText().toString()+  bmonth.getText().toString() + byear.getText().toString();
        customLoadDialogClass = new CustomLoadDialogClass(this, new OnDialogLoadListener() {
            @Override
            public void onLoad() {
            }
            @Override
            public String onLoadText() {
                return "Need few moments...";
            }
        });
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
        }
        if (TextUtils.isEmpty(dd)) {
            Toast.makeText(getApplicationContext(), "We need your birth date.", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(mm)) {
            Toast.makeText(getApplicationContext(), "We need your birth month.", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(yyyy)) {
            Toast.makeText(getApplicationContext(), "We need your birth year.", Toast.LENGTH_LONG).show();
            return;
        }
        else {
            customLoadDialogClass.show();
             dob = dd+mm+yyyy;
            new regisTask().execute(email, dob);
        }
    }

    public class regisTask extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... creds){
            String emailcred = creds[0];
            String passcred = creds[1];
            if(getLoginStatus()){
                loginUser(emailcred, passcred);
            }else {
                register(emailcred, passcred);
            }
            return emailcred;
        }
        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
        }
    }

    private void loginUser(final String emailIdFinalLogin, String passwordFinalLogin){
        mAuth.signInWithEmailAndPassword(emailIdFinalLogin, passwordFinalLogin)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Logged in as "+emailIdFinalLogin, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            overridePendingTransition(R.anim.top_out,R.anim.bottom_in);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Login failed! An error occurred", Toast.LENGTH_LONG).show();
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
                            customLoadDialogClass.hide();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            overridePendingTransition(R.anim.top_out,R.anim.bottom_in);
                            startActivity(intent);
                            finish();
                        } else {
                            storeLoginStatus(false);
                            Toast.makeText(getApplicationContext(), "An error occurred. Network problem?", Toast.LENGTH_LONG).show();
                            customLoadDialogClass.hide();
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
}
