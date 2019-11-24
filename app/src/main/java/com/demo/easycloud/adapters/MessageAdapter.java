package com.demo.easycloud.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.demo.easycloud.R;
import com.demo.easycloud.ViewImageActivity;
import com.demo.easycloud.models.AllMethods;
import com.demo.easycloud.models.Message;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageAdapterViewHolder> {

    Context context;
    List<Message> messages;
    DatabaseReference messageDb;

    public MessageAdapter(Context context, List<Message> messages, DatabaseReference messageDb) {
        this.context = context;
        this.messages = messages;
        this.messageDb = messageDb;
    }


    @NonNull
    @Override
    public MessageAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_list_row, parent, false);
        return new MessageAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapterViewHolder holder, int position) {
        final Message message = messages.get(position);
        Log.d("Now: ", message.getName());
        Log.d("Now: ", "True " + AllMethods.name);
        if(message.getName().equals(AllMethods.name)) {
            String displayText = "You: ";
            if(!message.getMessage().contains("https://")) {
                displayText += message.getMessage();
            }
            holder.tvTitle.setText(displayText);

            if(message.getMessage().contains("https://")) {
                new ImageLoadTask(message.getMessage(), holder.imgView).execute();
                holder.imgView.setVisibility(View.VISIBLE);

                holder.imgView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("Img Clicked", message.getMessage());
                        Intent messageIntent = new Intent(v.getContext(), ViewImageActivity.class);
                        messageIntent.putExtra("imageUrl", message.getMessage()); // put image data in Intent
                        messageIntent.putExtra("shareImage", true);
                        v.getContext().startActivity(messageIntent); // start Intent
                    }
                });
            }

//            holder.tvTitle.setText("You: " + message.getMessage());
            holder.tvTitle.setGravity(Gravity.START);
//            holder.ll.setBackgroundColor(R.colorPrimary);
            holder.tvTitle.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            holder.tvTitle.setText(message.getName() + " : " + message.getMessage());
            holder.ibDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class MessageAdapterViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageButton ibDelete;
        LinearLayout ll;
        ImageView imgView;

        public MessageAdapterViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            ibDelete = (ImageButton) itemView.findViewById(R.id.deleteMessage);
            ll = (LinearLayout) itemView.findViewById(R.id.llMessage);
            imgView = (ImageView) itemView.findViewById(R.id.imageView);

            ibDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    messageDb.child(messages.get(getAdapterPosition()).getKey()).removeValue();
                }
            });
        }
    }
}
