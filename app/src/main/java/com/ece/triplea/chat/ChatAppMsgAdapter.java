package com.ece.triplea.chat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ece.triplea.R;

import java.util.ArrayList;
import java.util.List;


public class ChatAppMsgAdapter extends RecyclerView.Adapter<ChatAppMsgViewHolder> {

    private List<ChatAppMsgDTO> msgDtoList = null;

    public ChatAppMsgAdapter(List<ChatAppMsgDTO> msgDtoList) {
        this.msgDtoList = msgDtoList;
    }

    @Override
    public void onBindViewHolder(ChatAppMsgViewHolder holder, int position) {
        ChatAppMsgDTO msgDto = this.msgDtoList.get(position);

        // If the message is a sent message.
         if(msgDto.MSG_TYPE_SENT.equals(msgDto.getMsgType()))
        {
            holder.myTextviewsend.setText(msgDtoList.get(position).getMsgContent());
            holder.myTextviewsend.setVisibility(View.VISIBLE);
            holder.myTextView.setVisibility(View.GONE);
        }


        // If the message is a received message.
        else if(msgDto.MSG_TYPE_RECEIVED.equals(msgDto.getMsgType()))
        {
         holder.myTextView.setText(msgDtoList.get(position).getMsgContent());
         holder.myTextView.setVisibility(View.VISIBLE);
         holder.myTextviewsend.setVisibility(View.GONE);
        }



    }

    @Override
    public ChatAppMsgViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.cardview, parent, false);
        return new ChatAppMsgViewHolder(view);
    }

    @Override
    public int getItemCount() {
        if(msgDtoList==null)
        {
            msgDtoList = new ArrayList<ChatAppMsgDTO>();
        }
        return msgDtoList.size();
    }
}

