package com.demo.easycloud.adapters;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.demo.easycloud.ChatActivity;
import com.demo.easycloud.R;
import com.demo.easycloud.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatHolder> implements Filterable {

    private List<User> fileList;
    private List<User> completFileList;


    public ChatsAdapter(List<User> fileList) {
        this.fileList = fileList;
        this.completFileList = fileList;
    }

    @Override
    public ChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_list_row, parent, false);

        return new ChatHolder(itemView);
    }


    @Override
    public Filter getFilter() {
        return new Filter() {//search result
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    fileList = completFileList;
                } else {
                    List<User> filteredList = new ArrayList<>();
                    for (User user : completFileList) {
                        if (user.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(user);
                        }
                    }
                    fileList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = fileList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                fileList = (ArrayList<User>) filterResults.values;

                notifyDataSetChanged();
            }
        };
    }

    @Override
    public void onBindViewHolder(ChatHolder holder, int position) {
        User file = fileList.get(position);
        holder.contactName.setText(file.getName());
        Log.d("ContactAdapter", file.toString());
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public class ChatHolder extends RecyclerView.ViewHolder {
        public TextView contactName;

        public ChatHolder(View view) {
            super(view);
            contactName = (TextView) view.findViewById(R.id.contactName);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Intent i = new Intent(v.getContext(), ChatActivity.class);

                    Bundle dataBundle = new Bundle();
                    dataBundle.putString("uid", fileList.get(position).getUid());
                    dataBundle.putString("name", fileList.get(position).getName());
                    dataBundle.putString("email", fileList.get(position).getEmail());
                    i.putExtras(dataBundle);

                    v.getContext().startActivity(i);
                }
            });
        }
    }
}