package org.timetable.schemester.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.timetable.schemester.R;

import java.util.ArrayList;

public class MessageListAdapter extends ArrayAdapter<ChatRoomModel> {
    TextView id, msg, time;
    private int type,
    USER_OTHER = 201, USER_ME = 202;
    MessageListAdapter(Context context, ArrayList<ChatRoomModel> users) {
        super(context, 0, users);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            ChatRoomModel model = getItem(position);
            assert model != null;
            if(model.getUserType() == USER_ME){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_sent_bubble, parent, false);
                msg = convertView.findViewById(R.id.myMsg);
                time = convertView.findViewById(R.id.my_message_time);
                msg.setText(model.getMessage());
                time.setText(model.getTimeStamp());
            } else {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_received_bubble, parent, false);
                id = convertView.findViewById(R.id.receivedID);
                msg = convertView.findViewById(R.id.receivedMsg);
                time = convertView.findViewById(R.id.received_message_time);
                id.setText(model.getUser());
                msg.setText(model.getMessage());
                time.setText(model.getTimeStamp());
            }
        }
        return convertView;
    }
}