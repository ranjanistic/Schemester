package org.timetable.schemester;

import android.app.Application;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ApplicationSchemester extends Application {
    public static final int CODE_THEME_LIGHT = 101, CODE_THEME_DARK = 102,
            CODE_THEME_INCOGNITO = 103;
    public static final int versionCode = BuildConfig.VERSION_CODE;
    public static final String versionName = BuildConfig.VERSION_NAME,
            applicationId = BuildConfig.APPLICATION_ID;

    String COLLECTION_GLOBAL_INFO = "global_info", DOCUMENT_GLOBAL_SEMESTER = "semester",
            DOCUMENT_LOCAL_INFO = "local_info", DOCUMENT_HOLIDAY_INFO = "holiday_info",
            DOCUMENT_NOTICE = "notice",
            FIELD_HOLIDAY = "holiday",
            DOCUMENT_YEAR_AUTHORITY = "authority", FIELD_CR_CODE = "CRCode",
            COLLECTION_COLLEGE_CODE, DOCUMENT_COURSE_CODE, COLLECTION_YEAR_CODE,
            COLLECTION_APP_CONFIGURATION = "appConfig", DOCUMENT_VERSION_CURRENT = "version", DOCUMENT_LINKS = "links",
            FIELD_VERSION_NAME = "name", FIELD_DOWNLOAD_LINK = "link", FIELD_VERSION_CODE = "code", FIELD_WEBSITE = "website",
            COLLECTION_USERBASE = "userbase", FIELD_USER_ACTIVE = "active", FIELD_USER_DEFINITION = "definition", FIELD_USER_NAME = "name",
            PREF_KEY_COLLEGE = "college", PREF_KEY_COURSE = "course", PREF_KEY_YEAR = "year", PREF_HEAD_ADDITIONAL_INFO = "additionalInfo",
            PREF_KEY_THEME = "themeCode", PREF_HEAD_THEME = "schemeTheme",
            PREF_HEAD_CREDENTIALS = "credentials", PREF_KEY_EMAIL = "email", PREF_KEY_ROLL = "roll",
            PREF_HEAD_LOGIN_STAT = "login", PREF_KEY_LOGIN_STAT = "loginstatus",
            PREF_HEAD_TIME_FORMAT = "schemeTime", PREF_KEY_TIME_FORMAT = "format",
            PREF_HEAD_USER_DEF = "userDefinition", PREF_KEY_USER_DEF = "position",
            PREF_HEAD_UPDATE_NOTIFY = "schemeUpdateNotification", PREF_KEY_UPDATE_NOTIFY = "getSchemeUpdateNotification",
            PREF_HEAD_MESSAGE_DATA = "schemeChat", PREF_KEY_MESSAGE_COUNT = "chatCount",
            PREF_KEY_STUDENT_CR = "CRStat", PREF_KEY_CR_CODE = "crCode";

    public String getPREF_HEAD_MESSAGE_DATA() {
        return PREF_HEAD_MESSAGE_DATA;
    }

    public String getPREF_KEY_MESSAGE_COUNT() {
        return PREF_KEY_MESSAGE_COUNT;
    }

    public String getPREF_HEAD_UPDATE_NOTIFY() {
        return PREF_HEAD_UPDATE_NOTIFY;
    }

    public String getPREF_KEY_UPDATE_NOTIFY() {
        return PREF_KEY_UPDATE_NOTIFY;
    }

    public String getPREF_HEAD_USER_DEF() {
        return PREF_HEAD_USER_DEF;
    }

    public String getPREF_KEY_USER_DEF() {
        return PREF_KEY_USER_DEF;
    }

    public String getPREF_KEY_STUDENT_CR() {
        return PREF_KEY_STUDENT_CR;
    }

    public String getPREF_KEY_CR_CODE() {
        return PREF_KEY_CR_CODE;
    }

    public String getPREF_HEAD_TIME_FORMAT() {
        return PREF_HEAD_TIME_FORMAT;
    }

    public String getPREF_KEY_TIME_FORMAT() {
        return PREF_KEY_TIME_FORMAT;
    }

    public String getPREF_HEAD_LOGIN_STAT() {
        return PREF_HEAD_LOGIN_STAT;
    }

    public String getPREF_KEY_LOGIN_STAT() {
        return PREF_KEY_LOGIN_STAT;
    }

    public String getPREF_HEAD_CREDENTIALS() {
        return PREF_HEAD_CREDENTIALS;
    }

    public String getPREF_KEY_EMAIL() {
        return PREF_KEY_EMAIL;
    }

    public String getPREF_KEY_ROLL() {
        return PREF_KEY_ROLL;
    }

    public String getPREF_KEY_COLLEGE() {
        return PREF_KEY_COLLEGE;
    }

    public String getPREF_KEY_COURSE() {
        return PREF_KEY_COURSE;
    }

    public String getPREF_KEY_YEAR() {
        return PREF_KEY_YEAR;
    }

    public String getPREF_HEAD_ADDITIONAL_INFO() {
        return PREF_HEAD_ADDITIONAL_INFO;
    }

    public String getPREF_KEY_THEME() {
        return PREF_KEY_THEME;
    }

    public String getPREF_HEAD_THEME() {
        return PREF_HEAD_THEME;
    }

    public String getFIELD_HOLIDAY() {
        return FIELD_HOLIDAY;
    }

    public String getDOCUMENT_LINKS() {
        return DOCUMENT_LINKS;
    }

    public String getFIELD_WEBSITE() {
        return FIELD_WEBSITE;
    }

    String FIELD_DEV_WEB = "developer", FIELD_ASSO_WEB = "associate";

    public String getFIELD_DEV_WEB() {
        return FIELD_DEV_WEB;
    }

    public String getFIELD_ASSO_WEB() {
        return FIELD_ASSO_WEB;
    }

    public String getCOLLECTION_GLOBAL_INFO() {
        return COLLECTION_GLOBAL_INFO;
    }

    public String getDOCUMENT_GLOBAL_SEMESTER() {
        return DOCUMENT_GLOBAL_SEMESTER;
    }

    public String getDOCUMENT_LOCAL_INFO() {
        return DOCUMENT_LOCAL_INFO;
    }

    public String getDOCUMENT_HOLIDAY_INFO() {
        return DOCUMENT_HOLIDAY_INFO;
    }

    public String getCOLLECTION_COLLEGE_CODE() {
        return COLLECTION_COLLEGE_CODE;
    }

    public String getDOCUMENT_YEAR_AUTHORITY() {
        return DOCUMENT_YEAR_AUTHORITY;
    }

    public String getFIELD_CR_CODE() {
        return FIELD_CR_CODE;
    }

    public String getDOCUMENT_COURSE_CODE() {
        return DOCUMENT_COURSE_CODE;
    }

    public String getCOLLECTION_YEAR_CODE() {
        return COLLECTION_YEAR_CODE;
    }

    public String getDOCUMENT_NOTICE() {
        return DOCUMENT_NOTICE;
    }

    public String getCOLLECTION_APP_CONFIGURATION() {
        return COLLECTION_APP_CONFIGURATION;
    }

    public String getDOCUMENT_VERSION_CURRENT() {
        return DOCUMENT_VERSION_CURRENT;
    }

    public String getFIELD_VERSION_NAME() {
        return FIELD_VERSION_NAME;
    }

    public String getFIELD_DOWNLOAD_LINK() {
        return FIELD_DOWNLOAD_LINK;
    }

    public String getFIELD_VERSION_CODE() {
        return FIELD_VERSION_CODE;
    }

    public String getCOLLECTION_USERBASE() {
        return COLLECTION_USERBASE;
    }

    public String getFIELD_USER_ACTIVE() {
        return FIELD_USER_ACTIVE;
    }

    public String getFIELD_USER_DEFINITION() {
        return FIELD_USER_DEFINITION;
    }

    public String getFIELD_USER_NAME() {
        return FIELD_USER_NAME;
    }

    public void setCollegeCourseYear(String college, String course, String year) {
        this.COLLECTION_COLLEGE_CODE = college;
        this.DOCUMENT_COURSE_CODE = course;
        this.COLLECTION_YEAR_CODE = year;
    }

    public String getStringResource(int resID) {
        return getResources().getString(resID);
    }

    public int getColorResource(int resID) {
        return getResources().getColor(resID);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setCollegeCourseYear(getAdditionalInfo()[0], getAdditionalInfo()[1], getAdditionalInfo()[2]);
    }

    String[] getAdditionalInfo() {
        String[] CCY = new String[3];
        SharedPreferences mSharedPreferences = getSharedPreferences(PREF_HEAD_ADDITIONAL_INFO, MODE_PRIVATE);
        CCY[0] = mSharedPreferences.getString(PREF_KEY_COLLEGE, null);
        CCY[1] = mSharedPreferences.getString(PREF_KEY_COURSE, null);
        CCY[2] = mSharedPreferences.getString(PREF_KEY_YEAR, null);
        return CCY;
    }

    public void buttonLongPressToast(Button button, final String message) {
        button.setOnLongClickListener(view -> {
            toasterShort(message);
            return true;
        });
    }

    public void textLongPressToast(TextView textView, final String message) {
        textView.setOnLongClickListener(view -> {
            toasterShort(message);
            return true;
        });
    }

    public void imageLongPressToast(ImageView imageView, final String message) {
        imageView.setOnLongClickListener(view -> {
            toasterShort(message);
            return true;
        });
    }

    public void imageButtonLongPressToast(ImageButton imageButton, final String message) {
        imageButton.setOnLongClickListener(view -> {
            toasterShort(message);
            return true;
        });
    }

    public void toasterLong(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public void toasterShort(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
