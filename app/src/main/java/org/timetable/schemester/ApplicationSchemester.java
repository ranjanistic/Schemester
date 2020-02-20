package org.timetable.schemester;

import android.app.Application;
import android.content.SharedPreferences;

public class ApplicationSchemester  extends Application {
    public static final int CODE_THEME_LIGHT = 101, CODE_THEME_DARK = 102,
    CODE_THEME_INCOGNITO = 103;

    String[] pKey = {"p1","p2","p3","p4","p5","p6","p7","p8","p9"};
    public String[] getPKey(){
        return pKey;
    }

    String COLLECTION_GLOBAL_INFO = "global_info", DOCUMENT_GLOBAL_SEMESTER = "semester",
    COLLECTION_COLLEGE_CODE , DOCUMENT_COURSE_CODE , COLLECTION_YEAR_CODE,
    COLLECTION_APP_CONFIGURATION = "appConfig",DOCUMENT_VERSION_CURRENT = "verCurrent",
    FIELD_VERSION_NAME = "verName", FIELD_DOWNLOAD_LINK = "downlink", FIELD_VERSION_CODE = "verCode";
    public String getCOLLECTION_GLOBAL_INFO(){
        return COLLECTION_GLOBAL_INFO;
    }
    public String getDOCUMENT_GLOBAL_SEMESTER(){
        return DOCUMENT_GLOBAL_SEMESTER;
    }

    public String getCOLLECTION_COLLEGE_CODE(){ return COLLECTION_COLLEGE_CODE; }
    public String getDOCUMENT_COURSE_CODE(){
        return DOCUMENT_COURSE_CODE;
    }
    public String getCOLLECTION_YEAR_CODE(){
        return COLLECTION_YEAR_CODE;
    }

    public String getCOLLECTION_APP_CONFIGURATION(){
        return COLLECTION_APP_CONFIGURATION;
    }
    public String getDOCUMENT_VERSION_CURRENT(){
        return DOCUMENT_VERSION_CURRENT;
    }
    public String getFIELD_VERSION_NAME(){
        return FIELD_VERSION_NAME;
    }
    public String getFIELD_DOWNLOAD_LINK(){
        return FIELD_DOWNLOAD_LINK;
    }
    public String getFIELD_VERSION_CODE(){
        return FIELD_VERSION_CODE;
    }
    public void setCollegeCourseYear(String college, String course, String year){
        this.COLLECTION_COLLEGE_CODE = college;
        this.DOCUMENT_COURSE_CODE = course;
        this.COLLECTION_YEAR_CODE = year;
    }

    int[] periodStringResource12 = {
            R.string.period112, R.string.period212, R.string.period312, R.string.period412, R.string.period512,
            R.string.period612, R.string.period712, R.string.period812, R.string.period912
    }, periodStringResource24 = {
            R.string.period1, R.string.period2, R.string.period3, R.string.period4,
            R.string.period5, R.string.period6, R.string.period7, R.string.period8, R.string.period9
    }, timeStringResource = {
            R.string.time1, R.string.time2, R.string.time3, R.string.time4, R.string.time5,
            R.string.time6, R.string.time7, R.string.time8, R.string.time9, R.string.time10,
    };
    public int[] getPeriodStringResource12(){
        return periodStringResource12;
    }
    public int[] getPeriodStringResource24(){
        return periodStringResource24;
    }
    public int[] getTimeStringResource(){
        return timeStringResource;
    }
    public String getStringResource(int resId){
        return getResources().getString(resId);
    }
}
