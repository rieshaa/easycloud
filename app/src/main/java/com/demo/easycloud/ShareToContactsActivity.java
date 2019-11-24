package com.demo.easycloud;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.demo.easycloud.adapters.ContactsAdapter;
import com.demo.easycloud.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

public class ShareToContactsActivity extends AppCompatActivity {

    private List<User> contactsList;

    FirebaseAuth auth;
    FirebaseUser currentUser;
    FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

    ProgressBar progressBar;

    RecyclerView listOfContacts;
    ContactsAdapter contactsAdapter;

    Bundle dataBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        boolean darkTheme = pref.getBoolean("darkTheme", false);
        if (darkTheme) {
            setTheme(R.style.DarkTheme_NoActionBar);
        } else {
            setTheme(R.style.AppTheme_NoActionBar);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_to_contacts);
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

        setTitle("Share To");

        dataBundle = getIntent().getExtras();

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        contactsList = new ArrayList<User>();

        progressBar = (ProgressBar) findViewById(R.id.progressbar_contacts);

        listOfContacts = (RecyclerView) findViewById(R.id.contactList);

        contactsAdapter = new ContactsAdapter(contactsList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(ShareToContactsActivity.this);
        listOfContacts.setLayoutManager(mLayoutManager);
        listOfContacts.setItemAnimator(new DefaultItemAnimator());
        listOfContacts.setAdapter(contactsAdapter);
        getContacts();
    }

    public void getContacts() {
        DatabaseReference databaseReference = mFirebaseDatabase.getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                contactsList.clear();
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    if(!currentUser.getUid().equals(childDataSnapshot.getKey())) {
                        User user = new User();
                        user.setEmail(childDataSnapshot.child("email").getValue().toString());
                        user.setName(childDataSnapshot.child("name").getValue().toString());
                        user.setUid(childDataSnapshot.getKey().toString());
                        user.setShareImage(dataBundle.getString("imageUrl"));

                        contactsList.add(user);
                    }
                }
                contactsAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
