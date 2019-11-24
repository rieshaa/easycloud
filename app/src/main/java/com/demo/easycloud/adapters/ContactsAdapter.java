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
import com.demo.easycloud.MainActivity;
import com.demo.easycloud.R;
import com.demo.easycloud.models.File;
import com.demo.easycloud.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactHolder> implements Filterable {

    private List<User> fileList;
    private List<User> completFileList;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    final FirebaseUser currentUser = auth.getCurrentUser();
    FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference messageHistoryDb;



    public ContactsAdapter(List<User> fileList) {
        this.fileList = fileList;
        this.completFileList = fileList;
    }

    @Override
    public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contacts_list_row, parent, false);

        return new ContactHolder(itemView);
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
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
    public void onBindViewHolder(ContactHolder holder, int position) {
        User file = fileList.get(position);
        holder.contactName.setText(file.getName());
        Log.d("ContactAdapter", file.toString());

    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public class ContactHolder extends RecyclerView.ViewHolder {
        public TextView contactName;

        public ImageView profilePhoto;

        public ContactHolder(View view) {
            super(view);
            contactName = (TextView) view.findViewById(R.id.contactName);
            profilePhoto = (ImageView) view.findViewById(R.id.profilePhoto);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    final User selectedUser = fileList.get(position);
                    messageHistoryDb = mFirebaseDatabase.getReference("chathistories").child(currentUser.getUid());
                    messageHistoryDb.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (!snapshot.hasChild(selectedUser.getUid())) {
                                messageHistoryDb.child(selectedUser.getUid()).setValue(selectedUser);


                                mFirebaseDatabase.getReference("Users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if(dataSnapshot.getValue() != null) {
                                            User user = dataSnapshot.getValue(User.class);
                                            user.setUid(currentUser.getUid());
                                            mFirebaseDatabase.getReference("chathistories").child(selectedUser.getUid()).child(currentUser.getUid()).setValue(user);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    Intent i = new Intent(v.getContext(), ChatActivity.class);
                    Bundle dataBundle = new Bundle();
                    dataBundle.putString("uid", fileList.get(position).getUid());
                    dataBundle.putString("name", fileList.get(position).getName());
                    dataBundle.putString("email", fileList.get(position).getEmail());

                    if(!fileList.get(position).getShareImage().equals("")) {
                        dataBundle.putString("shareImage", fileList.get(position).getShareImage());
                    }
                    i.putExtras(dataBundle);
                    v.getContext().startActivity(i);
                }
            });
        }
    }
}