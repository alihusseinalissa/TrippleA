package com.ece.triplea.chat;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ece.triplea.R;

public  class ChatAppMsgViewHolder extends RecyclerView.ViewHolder {
    public TextView myTextView;
    public TextView myTextviewsend ;
    public ChatAppMsgViewHolder(View itemView) {
        super(itemView);
        myTextView = (TextView)itemView.findViewById(R.id.text_cardview);
        myTextviewsend =(TextView)itemView.findViewById(R.id.text_cardviewsend);
    }
}
