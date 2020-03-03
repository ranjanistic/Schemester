package org.timetable.schemester;

import java.util.ArrayList;

public class ChatRoomModel {
    private String message;
    private String user;
    private String timeStamp;
    private int userType;
    private ChatRoomModel(String text, String uid, String time, int type){
        this.message = text;
        this.user = uid;
        this.timeStamp = time;
        this.userType = type;
    }
    String getMessage(){return message;}
    String getUser(){return user;}
    String getTimeStamp(){return timeStamp;}
    int getUserType(){return userType;}

    static ArrayList<ChatRoomModel> setModel(String[] msg, String[] id, String[] stamp, long val, int[] sender){
        ArrayList<ChatRoomModel> models = new ArrayList<>();
        for(int i = 0; i<val;i++) {
            models.add(new ChatRoomModel(msg[i], id[i], stamp[i],sender[i]));
        }
        return models;
    }
}
