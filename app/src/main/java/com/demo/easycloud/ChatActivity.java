package com.demo.easycloud;

import android.os.Bundle;

import com.demo.easycloud.adapters.MessageAdapter;
import com.demo.easycloud.models.AllMethods;
import com.demo.easycloud.models.Message;
import com.demo.easycloud.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference messageDb;
    MessageAdapter messageAdapter;
    User user;
    List<Message> messageList;

    Bundle dataBundle;

    RecyclerView rvMessage;
    EditText editText;
    ImageButton sendBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        boolean darkTheme = pref.getBoolean("darkTheme", false);
        Log.d("DarkTheme", darkTheme+"");
        if (darkTheme) {
            setTheme(R.style.DarkTheme_NoActionBar);
        } else {
            setTheme(R.style.AppTheme_NoActionBar);
        }

        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        dataBundle = getIntent().getExtras();

        setTitle(dataBundle.get("name").toString());

        init();
    }

    private void init() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        user = new User();

        rvMessage = (RecyclerView) findViewById(R.id.rvMessage);
        editText = (EditText) findViewById(R.id.message);
        sendBt = (ImageButton) findViewById(R.id.btnSend);

        sendBt.setOnClickListener(this);

        if(dataBundle.containsKey("shareImage")) {
            editText.setText(dataBundle.getString("shareImage"));
        }

        messageList = new ArrayList<>();
    }

    @Override
    public void onClick(View v) {
        if(!TextUtils.isEmpty(editText.getText().toString())) {
            Message message = new Message(editText.getText().toString(), user.getName());
            editText.setText("");
            messageDb.push().setValue(message);
        } else {
            Toast.makeText(getApplicationContext(), "You can't send blank message", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        final FirebaseUser currentUser = auth.getCurrentUser();

        database.getReference("Users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user.setUid(currentUser.getUid());
                user.setName(dataSnapshot.child("name").getValue().toString());
                user.setEmail(currentUser.getEmail());

                AllMethods.name = user.getName();
                Log.d("On Start", user.toString());


                if(dataBundle.get("uid").toString().equals("groupchat")) {
                    messageDb = database.getReference("groupmsg");//group chat
                } else {
                    String childKey = getChatKey(currentUser.getUid(), dataBundle.get("uid").toString());
                    messageDb = database.getReference("privatemessage").child(childKey);//private chat
                }
                messageDb.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Message message = dataSnapshot.getValue(Message.class);
                        message.setKey(dataSnapshot.getKey());

                        messageList.add(message);
                        displayMessages(messageList);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Message message = dataSnapshot.getValue(Message.class);
                        message.setKey(dataSnapshot.getKey());

                        List<Message> newMessages = new ArrayList<Message>();


                        for(Message m: messageList) {
                            if(m.getKey().equals(message.getKey())) {
                                newMessages.add(message);
                            } else {
                                newMessages.add(m);
                            }
                        }
                        messageList = newMessages;

                        displayMessages(messageList);
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                        Message message = dataSnapshot.getValue(Message.class);
                        message.setKey(dataSnapshot.getKey());

                        List<Message> newMessages = new ArrayList<Message>();

                        for(Message m: messageList) {
                            if(!m.getKey().equals(message.getKey())) {
                                newMessages.add(m);
                            }
                        }

                        messageList = newMessages;
                        displayMessages(messageList);

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);

        if(dataBundle.get("uid").toString().equals("groupchat")) {
            MenuItem menuItem = menu.findItem(R.id.action_delete_chat);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_logout) {
            auth.signOut();
            finish();
            startActivity(new Intent(ChatActivity.this, LoginActivity.class));
        } else if(item.getItemId() == R.id.action_delete_chat) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Are you sure, You wanted to delete this chat");
            alertDialogBuilder.setPositiveButton("yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            final FirebaseUser currentUser = auth.getCurrentUser();
                            String childKey = getChatKey(currentUser.getUid(), dataBundle.get("uid").toString());
                            database.getReference("privatemessage").child(childKey).setValue(null);
                            database.getReference("chathistories").child(currentUser.getUid()).child(dataBundle.get("uid").toString()).setValue(null);
                            finish();
                        }
                    });

            alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        messageList = new ArrayList<Message>();

    }

    private void displayMessages(List<Message> messageList) {
        rvMessage.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
        messageAdapter = new MessageAdapter(ChatActivity.this, messageList, messageDb);
        rvMessage.setAdapter(messageAdapter);
    }

    private String getChatKey(String uid1, String uid2) {// private chat
        if (uid1.compareTo(uid2) > 0) {
            return uid1 + uid2;
        } else {
            return uid2 + uid1;
        }
    }
}

