package com.demo.easycloud;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.demo.easycloud.adapters.ImageLoadTask;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.demo.easycloud.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser currentUser;

    FirebaseStorage storage;
    StorageReference storageRef;

    CircularImageView editProfileDp, removeProfileDp, profileDp;

    FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

    EditText profileName, profileOldPassword, profileNewPassword, profileEmail;

    ScrollView settingsView;

    Switch darkModeSwitch;

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        boolean darkTheme = pref.getBoolean("darkTheme", false);
        Log.d("DarkTheme", darkTheme+"");
        if (darkTheme) {
            setTheme(R.style.DarkTheme_NoActionBar);
        } else {
            setTheme(R.style.AppTheme_NoActionBar);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
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

        setTitle("Settings");

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        darkModeSwitch = (Switch) findViewById(R.id.switchToDarkMode);

        profileName = (EditText) findViewById(R.id.profile_name);
        profileOldPassword = (EditText) findViewById(R.id.profile_old_password);
        profileNewPassword = (EditText) findViewById(R.id.profile_new_password);
        profileEmail = (EditText) findViewById(R.id.profile_email);

        editProfileDp = (CircularImageView) findViewById(R.id.editProfile);
        removeProfileDp = (CircularImageView) findViewById(R.id.removeProfile);
        profileDp = (CircularImageView) findViewById(R.id.profilePicture);

        settingsView = (ScrollView) findViewById(R.id.settingScrollView);


        editProfileDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, 2);
            }
        });

        boolean isDarkTheme = pref.getBoolean("darkTheme", false);

        if(isDarkTheme) {
            darkModeSwitch.setChecked(true);
        }

        darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pref.edit().putBoolean("darkTheme", isChecked).apply();
                finish();
                Intent i = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(i);
            }
        });

        removeProfileDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeProfileDp();
            }
        });

        getProfileDetails();
    }

    public  void getProfileDetails() {
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabase.getReference("Users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue() != null) {

                    User user = dataSnapshot.getValue(User.class);

                    Log.d("UserClass", user.toString());
                    user.setUid(dataSnapshot.getKey());
                    Log.d("Profile", user.toString());
                    profileName.setText(user.getName());
                    profileEmail.setText(user.getEmail());
                    if(user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                        new ImageLoadTask(user.getPhotoUrl(), profileDp).execute();
                    } else {
                        profileDp.setImageResource(R.drawable.ic_action_profile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Uri uri = data.getData();

                    saveProfileDp(uri);
                }
            }
        }
    }

    private void removeProfileDp() {
        mFirebaseDatabase.getReference("Users").child(currentUser.getUid()).child("photoUrl").setValue("");
        getProfileDetails();
    }

    private void saveProfileDp(Uri uri) {
        StorageReference profilePicRef = storageRef.child("profile/"+currentUser.getUid());
        profilePicRef.putFile(uri).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        mFirebaseDatabase.getReference("Users").child(currentUser.getUid()).child("photoUrl").setValue(uri.toString());
                        getProfileDetails();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            }
        });
    }

    public void updateProfile(View view) {
        final String email = currentUser.getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(email,profileNewPassword.getText().toString());

        currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    currentUser.updatePassword(profileOldPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(!task.isSuccessful()){
                                Snackbar snackbar_fail = Snackbar
                                        .make(settingsView, "Something went wrong. Please try again later", Snackbar.LENGTH_LONG);
                                snackbar_fail.show();
                            }else {
                                Snackbar snackbar_su = Snackbar
                                        .make(settingsView, "Password Successfully Modified", Snackbar.LENGTH_LONG);
                                snackbar_su.show();
                                profileNewPassword.setText("");
                                profileOldPassword.setText("");
                            }
                        }
                    });
                } else {
                    Snackbar snackbar_su = Snackbar
                            .make(settingsView, "Authentication Failed", Snackbar.LENGTH_LONG);
                    snackbar_su.show();
                }
            }
        });


//        Log.d("newPasswrod", profilePassword.getText().toString());
//        currentUser.updatePassword(profilePassword.getText().toString()).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.d("new", e.toString());
//            }
//        }).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if(!task.isSuccessful()){
//                    Snackbar snackbar_fail = Snackbar
//                            .make(settingsView,"Something went wrong. Please try again later", Snackbar.LENGTH_LONG);
//                    snackbar_fail.show();
//                }else {
//                    Snackbar snackbar_su = Snackbar
//                            .make(settingsView, "Password Successfully Modified", Snackbar.LENGTH_LONG);
//                    snackbar_su.show();
//                }
//            }
//        });
    }

}
