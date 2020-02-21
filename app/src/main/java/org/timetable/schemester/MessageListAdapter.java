package org.timetable.schemester;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private Context mContext;
    private Boolean mCurrent;
    private String mMessage, mTime, mUser;
    MessageListAdapter(Context context, Boolean current, String message, String time, String user) {
        this.mContext = context;
        this.mCurrent = current;
        this.mMessage = message;
        this.mTime = time;
        this.mUser = user;
    }
    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        if (mCurrent) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    // Inflates the appropriate layout according to the ViewType.
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_sent_bubble, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_received_bubble, parent, false);
            return new ReceivedMessageHolder(view);
        }
        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(mMessage,mTime);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(mMessage, mTime,mUser);
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.myMsg);
            timeText = itemView.findViewById(R.id.my_message_time);
        }

        void bind(String message, String time) {
            messageText.setText(message);
            // Format the stored timestamp into a readable String using method.
            timeText.setText(time);
        }
    }
    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;
        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText =  itemView.findViewById(R.id.receivedMsg);
            timeText =  itemView.findViewById(R.id.received_message_time);
            nameText = itemView.findViewById(R.id.receivedID);
        }
        void bind(String message, String time, String uid) {
            messageText.setText(message);
            timeText.setText(time);
            nameText.setText(uid);
        }
    }
}