package com.demo.easycloud;

import android.content.Intent;
import android.os.Bundle;

import com.demo.easycloud.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText textEmail, textName, textPassword;

    ProgressBar progressBar;

    FirebaseAuth auth;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        textEmail = (TextInputEditText) findViewById(R.id.email_id_register);
        textName = (TextInputEditText) findViewById(R.id.name_register);
        textPassword = (TextInputEditText) findViewById(R.id.password_register);

        progressBar = (ProgressBar) findViewById(R.id.progressbar_register);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    public void registerUser(View v) {
        progressBar.setVisibility(View.VISIBLE);
        final String name = textName.getText().toString();
        Log.d("Register Name:", name);
        final String email = textEmail.getText().toString();
        final String password = textPassword.getText().toString();

        if(!name.equals("") && !email.equals("") && password.length() > 6) {
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                Log.d("Register", auth.getCurrentUser().toString());
                                FirebaseUser firebaseUser = auth.getCurrentUser();

                                User user = new User();
                                user.setEmail(email);
                                user.setName(name);

                                reference.child(firebaseUser.getUid()).setValue(user)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.d("Create User", "message: "+task.getException());
                                                if(task.isSuccessful()) {
                                                    Toast.makeText(getApplicationContext(), "Registered successfully", Toast.LENGTH_SHORT).show();
                                                    progressBar.setVisibility(View.GONE);
                                                    finish();
                                                    Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                                                    startActivity(i);
                                                }
                                            }
                                        });

                            }
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    public void gotoLogin(View v) {
        Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(i);
    }

}
