package com.demo.easycloud;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import com.demo.easycloud.adapters.FilesAdapter;
import com.demo.easycloud.models.File;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ViewFolderActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseUser currentUser;
    FirebaseStorage storage;
    StorageReference storageRef;

    String newFolderName;
    String selectedPath;

    RecyclerView listOfFiles;
    FloatingActionButton fab, newFolder, uploadFile;
    private boolean isFABOpen;

    private List<File> fileList;
    FilesAdapter filesAdapter;

    ProgressBar progressBar;

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
        setContentView(R.layout.activity_view_folder);
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

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        fileList = new ArrayList<File>();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        newFolder = (FloatingActionButton) findViewById(R.id.newFolder);
        uploadFile = (FloatingActionButton) findViewById(R.id.uploadFile);

        progressBar = (ProgressBar) findViewById(R.id.progressbar_folder);

        selectedPath = getIntent().getExtras().getString("path");

        setTitle(getIntent().getExtras().getString("imageTitle"));

        if(getIntent().getExtras().getString("imageTitle").equals("Trash")) {
            fab.hide();
            newFolder.hide();
            uploadFile.hide();
        }

        listOfFiles = (RecyclerView) findViewById(R.id.files);

        filesAdapter = new FilesAdapter(fileList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        listOfFiles.setLayoutManager(mLayoutManager);
        listOfFiles.setItemAnimator(new DefaultItemAnimator());
        listOfFiles.setAdapter(filesAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });

        uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, 2);
            }
        });

        newFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewFolderActivity.this);
                builder.setTitle("Enter Folder Name");

                final EditText input = new EditText(ViewFolderActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Log.d("Input", input.getText().toString());
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("*/*");
                        newFolderName = input.getText().toString();
                        startActivityForResult(intent, 3);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        getFiles();
    }

    private void showFABMenu(){
        isFABOpen=true;
        newFolder.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        uploadFile.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        Resources resources = getResources();
        fab.setImageDrawable(resources.getDrawable(R.drawable.ic_action_close));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        newFolder.animate().translationY(0);
        uploadFile.animate().translationY(0);
        Resources resources = getResources();
        fab.setImageDrawable(resources.getDrawable(R.drawable.ic_action_add));
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Uri uri = data.getData();
                    progressBar.setVisibility(View.VISIBLE);
                    uploadNewFile(uri, "");
                }
            }
        } else if (requestCode == 3) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Uri uri = data.getData();
                    progressBar.setVisibility(View.VISIBLE);
                    uploadNewFile(uri, newFolderName+"/");
                }
            }
        }
    }

    private void uploadNewFile(Uri uri, String folderName) {
        StorageReference newFileRef = storageRef.child(selectedPath+"/"+folderName+uri.getLastPathSegment());
        newFileRef.putFile(uri).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d("Failed", exception.toString());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressBar.setVisibility(View.GONE);
                getFiles();
            }
        });
    }

    private void getFiles() {
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference dashboardRef = storage.getReference().child(selectedPath);

        dashboardRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                fileList.clear();
                for (StorageReference folder: listResult.getPrefixes()) {
                    if(!folder.getName().equals("trash")) {
                        File file = new File();
                        file.setFileType("Folder");
                        file.setTitle(folder.getName());
                        file.setFolder(true);
                        file.setPath(folder.getPath());
                        fileList.add(file);
                        filesAdapter.notifyDataSetChanged();
                    }
                }

                for (final StorageReference item: listResult.getItems()) {
                    item.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                        @Override
                        public void onSuccess(StorageMetadata storageMetadata) {
                            final File file = new File();
                            DateFormat uploadedDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                            Date uploadedDate = new Date(storageMetadata.getUpdatedTimeMillis());
                            file.setUpdatedOn(uploadedDateFormat.format(uploadedDate));
                            file.setFileType(storageMetadata.getContentType());
                            file.setTitle(storageMetadata.getName());
                            file.setPath(storageMetadata.getPath());
                            item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    file.setUri(uri.toString());
                                    fileList.add(file);
                                    filesAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                }
            }
        });
    }

}
