package org.timetable.schemester;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
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

@TargetApi(Build.VERSION_CODES.Q)
public class LoginActivity extends AppCompatActivity {
    ApplicationSchemester schemester;
    Button login, forgot;
    ImageView displayImage;
    EditText emailid, roll, bdate, bmonth, byear;
    TextView emailValid, rollValid;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String dob;
    private BottomSheetBehavior bottomSheetBehavior;
    View bottomDrawer, layer;
    TextInputLayout collegeRollInputLayout;
    CustomLoadDialogClass customLoadDialogClass;
    CustomConfirmDialogClass confirmEmailDialog;
    Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
    Button resetmail, resendmail, checkverif;
    TextView verificationHead, checkMailcapt, verificationBody, countdown, emailonDrawer;
    boolean isRollValid = false, isEmailValid = false, isDateValid = false, isTeacher = false;
    InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        schemester = (ApplicationSchemester) this.getApplication();
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setAppTheme();
        setContentView(R.layout.activity_login);
        setLoadingClass();
        setViewsAndInitials();
        setBottomSheetFeature();
        setListeners();
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getThemeStatus() == ApplicationSchemester.CODE_THEME_DARK) {
            displayImage.setImageResource(R.drawable.ic_moonsmallicon);
        } else {
            displayImage.setImageResource(R.drawable.ic_suniconsmall);
        }
        dob = bdate.getText().toString() + bmonth.getText().toString() + byear.getText().toString();
    }

    private void setLoadingClass() {
        customLoadDialogClass = new CustomLoadDialogClass(this, new OnDialogLoadListener() {
            @Override
            public void onLoad() {
            }

            @Override
            public String onLoadText() {
                return schemester.getStringResource(R.string.need_few_moments);
            }
        });
    }

    private void setViewsAndInitials() {
        collegeRollInputLayout = findViewById(R.id.collegeRollLayout);
        if (readUserPosition().equals(schemester.getStringResource(R.string.teacher))) {
            isTeacher = true;
            collegeRollInputLayout.setVisibility(View.GONE);
        } else {
            isTeacher = false;
            roll = findViewById(R.id.rollpass);
            rollValid = findViewById(R.id.rollValidityText);
        }
        bdate = findViewById(R.id.birthdate);
        bmonth = findViewById(R.id.birthmonth);
        byear = findViewById(R.id.birthyear);
        login = findViewById(R.id.registerbtn);
        emailid = findViewById(R.id.emailId);
        emailid.requestFocus();
        emailValid = findViewById(R.id.emailValidityText);
        forgot = findViewById(R.id.forgotBtn);
        displayImage = findViewById(R.id.imageOnlogin);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_clock);
        displayImage.startAnimation(animation);
        bottomDrawer = findViewById(R.id.verification_drawer);
        layer = findViewById(R.id.translucent_layerLogin);
        layer.setClickable(false);
        verificationHead = findViewById(R.id.VdrawerHead);
        checkMailcapt = findViewById(R.id.checkmailcaption);
        verificationBody = findViewById(R.id.VdrawerBody);
        resetmail = findViewById(R.id.changeEmailID);
        resendmail = findViewById(R.id.resendLink);
        checkverif = findViewById(R.id.checkVerification);
        countdown = findViewById(R.id.countdownText);
        emailonDrawer = findViewById(R.id.mailIDonVerification);
        setVerificationDrawerDefaults();
    }

    private void setListeners() {
        emailid.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                checkEmailValidity(emailid.getText().toString().trim(), s);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
        if (!isTeacher) {
            roll.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    if (s.length() == 0) {
                        emailid.requestFocus();
                    }
                    checkRollNumValidity(roll.getText().toString().trim(), s);
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int count, int after) {
                }
            });
        } else {
            isRollValid = true;
        }

        forgot.setOnClickListener(view -> {
            customLoadDialogClass.show();
            if (isEmailValid) {
                if (!(emailid.getText().toString().length() == 0)) {
                    Snackbar.make(view,
                            R.string.get_link_to_reset_email, 4000)
                            .setAction(schemester.getStringResource(R.string.send), view1 -> {
                                if (isInternetAvailable()) {
                                    Snackbar.make(view1, schemester.getStringResource(R.string.sending), Snackbar.LENGTH_INDEFINITE)
                                            .setTextColor(getResources().getColor(R.color.white))
                                            .setBackgroundTint(getResources().getColor(R.color.deep_blue))
                                            .show();
                                    forgot.setVisibility(View.GONE);
                                    customLoadDialogClass.show();
                                    resetLinkSender(emailid.getText().toString());
                                } else {
                                    Snackbar.make(view1, schemester.getStringResource(R.string.internet_problem), 3000)
                                            .setTextColor(getResources().getColor(R.color.white))
                                            .setBackgroundTint(getResources().getColor(R.color.dark_red))
                                            .show();
                                }
                            })
                            .setTextColor(getResources().getColor(R.color.white))
                            .setBackgroundTint(getResources().getColor(R.color.dead_blue))
                            .setActionTextColor(getResources().getColor(R.color.yellow))
                            .show();
                } else {
                    Snackbar.make(view, schemester.getStringResource(R.string.request_email_text), 4000)
                            .setTextColor(getResources().getColor(R.color.white))
                            .setBackgroundTint(getResources().getColor(R.color.dark_red))
                            .show();
                }
            } else {
                Snackbar.make(view, schemester.getStringResource(R.string.provide_valid_email_text), 4000)
                        .setTextColor(getResources().getColor(R.color.white))
                        .setBackgroundTint(getResources().getColor(R.color.dark_red))
                        .show();
            }
            customLoadDialogClass.hide();
        });
        bdate.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    if (Integer.parseInt(bdate.getText().toString()) >= 1 && Integer.parseInt(bdate.getText().toString()) <= 31 && s.length() == 2) {
                        bdate.setTextColor(getResources().getColor(R.color.white));
                        bmonth.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        Objects.requireNonNull(imm).showSoftInput(bmonth, InputMethodManager.SHOW_IMPLICIT);
                        isDateValid = true;
                    }
                } else {
                    bdate.setTextColor(getResources().getColor(R.color.light_red));
                    isDateValid = false;
                }
                if (s.length() == 0) {
                    roll.requestFocus();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
        bmonth.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    if (Integer.parseInt(bmonth.getText().toString()) >= 1 && Integer.parseInt(bmonth.getText().toString()) <= 12 && s.length() == 2) {
                        bmonth.setTextColor(getResources().getColor(R.color.white));
                        byear.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        Objects.requireNonNull(imm).showSoftInput(bmonth, InputMethodManager.SHOW_IMPLICIT);
                        isDateValid = true;
                    }
                } else {
                    bmonth.setTextColor(getResources().getColor(R.color.light_red));
                    isDateValid = false;
                }
                if (s.length() == 0) {
                    bdate.requestFocus();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
        byear.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    bmonth.requestFocus();
                }
            }
        });

        login.setOnClickListener(view -> registerInit());
        resendmail.setOnClickListener(view -> {
            sendVerificationEmail();
        });
        resetmail.setOnClickListener(view -> {
            deleteAccount(emailid.getText().toString(), bdate.getText().toString() + bmonth.getText().toString()
                    + byear.getText().toString());
        });
        checkverif.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            silentLogin(emailid.getText().toString(), bdate.getText().toString() + bmonth.getText().toString() + byear.getText().toString());
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        deleteAccount(emailid.getText().toString(), bdate.getText().toString() + bmonth.getText().toString() + byear.getText().toString());
        finish();
    }

    private void deleteAccount(String uid, String passphrase) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            mAuth.signInWithEmailAndPassword(uid, passphrase);
            user = FirebaseAuth.getInstance().getCurrentUser();
        }
        if (user != null) {
            user.delete()
                    .addOnCompleteListener(dtask -> {
                        if (dtask.isSuccessful()) {
                            storeLoginStatus(false);
                            bottomSheetBehavior.setHideable(true);
                            bottomSheetBehavior.setSkipCollapsed(false);
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            layer.setVisibility(View.GONE);
                            layer.setClickable(false);
                            emailid.setText("");
                            emailid.requestFocus();
                            inputMethodManager.toggleSoftInputFromWindow(emailid.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
                        } else {
                            bottomSheetBehavior.setHideable(true);
                            bottomSheetBehavior.setSkipCollapsed(false);
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            layer.setVisibility(View.GONE);
                            layer.setClickable(false);
                            emailid.setText("");
                            emailid.requestFocus();
                            inputMethodManager.toggleSoftInputFromWindow(emailid.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
                            Toast.makeText(this, "Account created, but not verified. Login again.", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void setTimerOnResendButton() {
        resendmail.setClickable(false);
        resendmail.setFocusable(false);
        resendmail.setAlpha(0.1F);
        countdown.setVisibility(View.VISIBLE);
        new CountDownTimer(60000, 1000) {
            public void onTick(long millis) {
                String string = millis / 1000 + " s";
                countdown.setText(string);
            }

            public void onFinish() {
                resendmail.setClickable(true);
                resendmail.setFocusable(true);
                resendmail.setAlpha(1);
                countdown.setVisibility(View.GONE);
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        if (user != null) {
            if (user.isEmailVerified()) {
                if (userHasProvidedAdditionalInfo()) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(LoginActivity.this, AdditionalLoginInfo.class));
                    finish();
                }
            }
        }
        super.onStart();
    }

    private void registerInit() {
        final String email, rollNum, dd, mm, yyyy;
        email = emailid.getText().toString();
        if (Objects.equals(readUserPosition(), schemester.getStringResource(R.string.student)))
            rollNum = roll.getText().toString();
        else rollNum = null;
        dd = bdate.getText().toString();
        mm = bmonth.getText().toString();
        yyyy = byear.getText().toString();
        if (TextUtils.isEmpty(email)) {
            schemester.toasterLong(schemester.getStringResource(R.string.email_id_required));
            emailid.requestFocus();
            inputMethodManager.toggleSoftInputFromWindow(emailid.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
            return;
        }
        if (!isEmailValid) {
            schemester.toasterLong(schemester.getStringResource(R.string.invalid_email_address));
            emailid.requestFocus();
            inputMethodManager.toggleSoftInputFromWindow(emailid.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
            return;
        }
        if (!isTeacher && TextUtils.isEmpty(rollNum)) {
            schemester.toasterLong(schemester.getStringResource(R.string.college_roll_required_text));
            roll.requestFocus();
            inputMethodManager.toggleSoftInputFromWindow(emailid.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
            return;
        }
        if (!isRollValid && !isTeacher) {
            schemester.toasterLong(schemester.getStringResource(R.string.invalid_roll));
            roll.requestFocus();
            inputMethodManager.toggleSoftInputFromWindow(emailid.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
            return;
        }
        if (TextUtils.isEmpty(dd)) {
            schemester.toasterLong(schemester.getStringResource(R.string.we_need_your_birthdate));
            bdate.requestFocus();
            inputMethodManager.toggleSoftInputFromWindow(emailid.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
            return;
        }
        if (Integer.parseInt(dd) < 1 || Integer.parseInt(dd) > 31) {
            schemester.toasterLong(schemester.getStringResource(R.string.invalid_bdate));
            bdate.requestFocus();
            inputMethodManager.toggleSoftInputFromWindow(emailid.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
            return;
        }
        if (TextUtils.isEmpty(mm)) {
            schemester.toasterLong(schemester.getStringResource(R.string.we_need_your_bmonth));
            bmonth.requestFocus();
            inputMethodManager.toggleSoftInputFromWindow(emailid.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
            return;
        }
        if (Integer.parseInt(mm) < 1 || Integer.parseInt(mm) > 12) {
            schemester.toasterLong(schemester.getStringResource(R.string.invalid_bmonth));
            bmonth.requestFocus();
            inputMethodManager.toggleSoftInputFromWindow(emailid.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
            return;
        }
        if (TextUtils.isEmpty(yyyy)) {
            schemester.toasterLong(schemester.getStringResource(R.string.we_need_byear));
            byear.requestFocus();
            inputMethodManager.toggleSoftInputFromWindow(emailid.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
            return;
        }
        if (Integer.parseInt(yyyy) > calendar.get(Calendar.YEAR)) {
            schemester.toasterLong(schemester.getStringResource(R.string.future_year_entry_warning));
            byear.requestFocus();
            inputMethodManager.toggleSoftInputFromWindow(emailid.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
            return;
        }
        if (!isDateValid) {
            schemester.toasterLong(schemester.getStringResource(R.string.invalid_date_format));
            bdate.requestFocus();
            inputMethodManager.toggleSoftInputFromWindow(emailid.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
        } else {
            confirmEmailDialog = new CustomConfirmDialogClass(LoginActivity.this, new OnDialogConfirmListener() {
                @Override
                public void onApply(Boolean confirm) {
                    emailonDrawer.setText(email);
                    new registerLoginTask().execute(email, dd + mm + yyyy);
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

    private class registerLoginTask extends AsyncTask<String, String, Boolean> {
        @Override
        protected Boolean doInBackground(String... creds) {
            if (!isInternetAvailable()) {
                return false;
            } else {
                register(creds[0], creds[1]);
                return true;
            }
        }

        @Override
        protected void onPreExecute() {
            customLoadDialogClass.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (!result) {
                schemester.toasterLong(schemester.getStringResource(R.string.internet_problem));
                customLoadDialogClass.hide();
            }
        }
    }

    private void silentLogin(final String emailId, final String password) {
        Toast.makeText(this, "Checking, please wait", Toast.LENGTH_LONG).show();
        mAuth.signInWithEmailAndPassword(emailId, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!checkIfEmailVerified()) {
                            schemester.toasterShort("Not verified, check your email, or send yourself link again.");
                        } else {
                            storeCredentials(emailId, roll.getText().toString());
                            startActivity(new Intent(LoginActivity.this, AdditionalLoginInfo.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY));
                            customLoadDialogClass.hide();
                            finish();
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }
                    } else {
                        schemester.toasterShort("Failed to verify. Network problem?");
                    }
                });
    }

    private String[] getAdditionalInfo() {
        String[] CCY = new String[3];
        SharedPreferences mSharedPreferences = this.getSharedPreferences(schemester.getPREF_HEAD_ADDITIONAL_INFO(), MODE_PRIVATE);
        CCY[0] = mSharedPreferences.getString(schemester.getPREF_KEY_COLLEGE(), null);
        CCY[1] = mSharedPreferences.getString(schemester.getPREF_KEY_COURSE(), null);
        CCY[2] = mSharedPreferences.getString(schemester.getPREF_KEY_YEAR(), null);
        return CCY;
    }

    private Boolean userHasProvidedAdditionalInfo() {
        //noinspection MismatchedReadAndWriteOfArray
        String[] addInfo = getAdditionalInfo(), nullArray = new String[3];
        return !Arrays.equals(addInfo, nullArray);
    }

    private void loginUser(final String emailIdFinalLogin, final String passwordFinalLogin) {
        mAuth.signInWithEmailAndPassword(emailIdFinalLogin, passwordFinalLogin)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        forgot.setVisibility(View.GONE);
                        storeLoginStatus(true);
                        if (!isTeacher) {
                            storeCredentials(emailIdFinalLogin, roll.getText().toString());
                        } else {
                            storeCredentials(emailIdFinalLogin, null);
                        }
                        if (checkIfEmailVerified()) {
                            if (userHasProvidedAdditionalInfo()) {
                                schemester.toasterLong(schemester.getStringResource(R.string.logged_in_as) + "\n" + emailIdFinalLogin);
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                overridePendingTransition(R.anim.enter_from_bottom, R.anim.exit_from_top);
                                finish();
                            } else {
                                schemester.toasterLong(schemester.getStringResource(R.string.complete_your_profile));
                                startActivity(new Intent(LoginActivity.this, AdditionalLoginInfo.class));
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            }
                        } else {
                            setVerificationDrawerDefaults();
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            customLoadDialogClass.hide();
                        }
                    } else {
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            schemester.toasterShort(schemester.getStringResource(R.string.incorrect_credentials));
                            forgot.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            storeLoginStatus(false);
                            if (!isInternetAvailable()) {
                                schemester.toasterShort(schemester.getStringResource(R.string.network_problem));
                            } else {
                                schemester.toasterShort(schemester.getStringResource(R.string.error_occurred_try_later));
                            }
                        }
                    }
                });
    }

    private void setVerificationDrawerDefaults() {
        verificationHead.setText(schemester.getStringResource(R.string.get_link_verfication));
        checkMailcapt.setText(schemester.getStringResource(R.string.will_send_link_at));
        verificationBody.setText(schemester.getStringResource(R.string.email_to_be_sent_drawer_text));
        resendmail.setText(schemester.getStringResource(R.string.send_link));
    }

    private void register(final String uid, final String passphrase) {
        mAuth.createUserWithEmailAndPassword(uid, passphrase)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        forgot.setVisibility(View.GONE);
                        storeLoginStatus(true);
                        if (!isTeacher) {
                            storeCredentials(uid, roll.getText().toString());
                        } else {
                            storeCredentials(uid, null);
                        }
                        setVerificationDrawerDefaults();
                        if (!checkIfEmailVerified()) {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    } else {
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (FirebaseAuthUserCollisionException e) {
                            loginUser(uid, passphrase);
                        } catch (Exception e) {
                            storeLoginStatus(false);
                            if (!isInternetAvailable()) {
                                schemester.toasterShort(schemester.getStringResource(R.string.network_problem));
                            } else {
                                schemester.toasterLong(schemester.getStringResource(R.string.error_occurred_try_later));
                            }
                        }
                    }
                    customLoadDialogClass.hide();
                });
    }

    private void setBottomSheetFeature() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomDrawer);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING || newState == BottomSheetBehavior.STATE_EXPANDED) {
                    layer.setClickable(true);
                }
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    FirebaseAuth.getInstance().signOut();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (slideOffset == 0) {
                    layer.setVisibility(View.GONE);
                    layer.setClickable(false);
                } else {
                    layer.setVisibility(View.VISIBLE);
                }
                layer.setAlpha((float) 0.8 * (slideOffset));
            }
        });
    }

    private void storeLoginStatus(Boolean logged) {
        getSharedPreferences(schemester.getPREF_HEAD_LOGIN_STAT(), MODE_PRIVATE).edit()
                .putBoolean(schemester.getPREF_KEY_LOGIN_STAT(), logged).apply();
    }

    private void storeCredentials(String mail, String roll) {
        getSharedPreferences(schemester.getPREF_HEAD_CREDENTIALS(), MODE_PRIVATE).edit()
                .putString(schemester.getPREF_KEY_EMAIL(), mail)
                .putString(schemester.getPREF_KEY_ROLL(), roll)
                .apply();
    }

    private String getEmailLocally() {
        return getSharedPreferences(schemester.getPREF_HEAD_CREDENTIALS(), MODE_PRIVATE)
                .getString(schemester.getPREF_KEY_EMAIL(), null);
    }

    private boolean isInternetAvailable() {
        try {
            return Runtime.getRuntime().exec("/system/bin/ping -c 1 8.8.8.8").waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void checkEmailValidity(String emailUnderInspection, Editable s) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z.]+\\.+[a-z]+";
        if (emailUnderInspection.matches(emailPattern) && s.length() > 0) {
            emailValid.setVisibility(View.VISIBLE);
            emailValid.setText(getResources().getString(R.string.valid));
            emailValid.setTextColor(getResources().getColor(R.color.white));
            emailValid.setBackgroundResource(R.drawable.topleftsharpboxgreen);
            emailid.setBackgroundColor(schemester.getColorResource(R.color.blue));
            isEmailValid = true;
        } else if (s.length() == 0) {
            emailValid.setVisibility(View.GONE);
        } else {
            emailValid.setVisibility(View.VISIBLE);
            emailValid.setText(getResources().getString(R.string.invalidtext));
            emailValid.setTextColor(getResources().getColor(R.color.white));
            emailValid.setBackgroundResource(R.drawable.topleftsharpboxred);
            emailid.setBackgroundColor(schemester.getColorResource(R.color.dark_red));
            isEmailValid = false;
        }
    }

    private void checkRollNumValidity(String rollUnderInspection, Editable s) {
        String rollPattern = "[0-9]+/[0-9]+";
        if (rollUnderInspection.matches(rollPattern) && s.length() > 0) {
            rollValid.setVisibility(View.VISIBLE);
            rollValid.setText(getResources().getString(R.string.valid));
            rollValid.setTextColor(getResources().getColor(R.color.white));
            rollValid.setBackgroundResource(R.drawable.topleftsharpboxgreen);
            isRollValid = true;
        } else if (s.length() == 0) {
            rollValid.setVisibility(View.GONE);
        } else {
            rollValid.setVisibility(View.VISIBLE);
            rollValid.setText(getResources().getString(R.string.invalidtext));
            rollValid.setTextColor(getResources().getColor(R.color.white));
            rollValid.setBackgroundResource(R.drawable.topleftsharpboxred);
            isRollValid = false;
        }
    }

    private void sendVerificationEmail() {
        schemester.toasterShort("sending");
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        user = FirebaseAuth.getInstance().getCurrentUser();
        customLoadDialogClass.hide();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            verificationHead.setText(schemester.getStringResource(R.string.waiting_for_verification));
                            checkMailcapt.setText(schemester.getStringResource(R.string.check_your_mailbox_at));
                            verificationBody.setText(schemester.getStringResource(R.string.email_sent_drawer_text));
                            resendmail.setText(schemester.getStringResource(R.string.resend));
                            setTimerOnResendButton();
                        }
                    });
        }
    }

    private Boolean checkIfEmailVerified() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).isEmailVerified();
    }

    private String readUserPosition() {
        return getSharedPreferences(schemester.getPREF_HEAD_USER_DEF(), MODE_PRIVATE)
                .getString(schemester.getPREF_KEY_USER_DEF(), null);
    }

    private void resetLinkSender(final String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Snackbar.make(findViewById(R.id.loginActivityID), schemester.getStringResource(R.string.email_sent_notif), 5000)
                                .setTextColor(getResources().getColor(R.color.white))
                                .setBackgroundTint(getResources().getColor(R.color.dead_blue))
                                .show();
                    } else {
                        Snackbar.make(findViewById(R.id.loginActivityID), schemester.getStringResource(R.string.network_error_occurred), 5000)
                                .setTextColor(getResources().getColor(R.color.white))
                                .setBackgroundTint(getResources().getColor(R.color.dark_red))
                                .show();
                    }
                });
        customLoadDialogClass.hide();
    }

    public void setAppTheme() {
        switch (getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE)
                .getInt(schemester.getPREF_KEY_THEME(), 0)) {
            case ApplicationSchemester.CODE_THEME_DARK:
                setTheme(R.style.BlueDarkTheme);
                break;
            case ApplicationSchemester.CODE_THEME_LIGHT:
            default:
                setTheme(R.style.BlueLightTheme);
        }
    }

    private int getThemeStatus() {
        return getSharedPreferences(schemester.getPREF_HEAD_THEME(), MODE_PRIVATE)
                .getInt(schemester.getPREF_KEY_THEME(), 0);
    }
}
