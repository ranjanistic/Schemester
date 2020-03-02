package org.timetable.schemester;

import java.util.ArrayList;

public class ChatRoomModel {
    private String message;
    private String user;
    private String timeStamp;

    private ChatRoomModel(String text, String uid, String time){
        this.message = text;
        this.user = uid;
        this.timeStamp = time;
    }
    String getMessage(){return message;}
    String getUser(){return user;}
    String getTimeStamp(){return timeStamp;}

    void setMessage(String newMessage){ this.message = newMessage;}
    void setUser(String newUser){this.user = newUser;}
    void setTimeStamp(String newStamp){this.timeStamp = newStamp;}
    static ArrayList<ChatRoomModel> setModel(String msg, String id, String stamp, int val){
        ArrayList<ChatRoomModel> models = new ArrayList<>();
        for(int i = 0; i<val;i++) {
            models.add(new ChatRoomModel(msg, id, stamp));
        }
        return models;
    }
}
