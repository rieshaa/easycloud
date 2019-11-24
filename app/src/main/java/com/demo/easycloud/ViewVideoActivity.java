package com.demo.easycloud;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.codekidlabs.storagechooser.StorageChooser;
import com.demo.easycloud.adapters.DownloadTask;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ViewVideoActivity extends AppCompatActivity {

    Bundle dataBundle;

    FirebaseAuth auth;
    FirebaseUser currentUser;

    Button downloadFile;

    String imageUrl;

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
        setContentView(R.layout.activity_view_video);
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

        dataBundle = getIntent().getExtras();

        setTitle(dataBundle.getString("imageTitle"));

        downloadFile = (Button) findViewById(R.id.downloadFile);

        downloadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToFile();
            }
        });

        imageUrl = dataBundle.getString("imageUrl");

//        Log.d("URL", imageUrl);
//        Picasso.with(this).load(imageUrl).placeholder(R.drawable.ic_action_image).into(viewImage);
//        videoView.setVideoURI(Uri.parse(imageUrl));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_image, menu);
        if(dataBundle.getString("imagePath").contains("/trash/")) {
            menu.findItem(R.id.action_share).setVisible(false);
            menu.findItem(R.id.action_trash).setVisible(false);
        } else {
            menu.findItem(R.id.action_delete).setVisible(false);
            menu.findItem(R.id.action_restore).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_download) {
            saveToFile();
        } else if (id == R.id.action_restore) {
            restoreImg();
        } else if (id == R.id.action_delete) {
            deleteImage();
        } else if (id == R.id.action_share) {
            Intent redirectIntent = new Intent(ViewVideoActivity.this, ShareToContactsActivity.class);

            Bundle dataBundle = new Bundle();
            dataBundle.putString("imageUrl", imageUrl);
            redirectIntent.putExtras(dataBundle);

            startActivity(redirectIntent);

            return true;
        } else if(id == R.id.action_trash) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ViewVideoActivity.this);
            builder.setTitle("Delete");
            builder.setMessage("Are you sure do you want to delete this file?");
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    final FirebaseStorage storage = FirebaseStorage.getInstance();

                    final String imagePath = dataBundle.getString("imagePath");
                    final String imageName = storage.getReference().child(imagePath).getName();
                    Task<byte[]> task = storage.getReference().child(imagePath).getBytes(Long.MAX_VALUE);
                    task.addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            storage.getReference().child(currentUser.getUid()).child("trash").child(imageName).putBytes(bytes);
                            storage.getReference().child(dataBundle.getString("imagePath")).delete();
                        }
                    });
                    Intent i = new Intent(ViewVideoActivity.this, MainActivity.class);
                    startActivity(i);
                }
            });
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }

        return super.onOptionsItemSelected(item);
    }

    public void deleteImage() {
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.getReference().child(getIntent().getStringExtra("imagePath")).delete();
        super.onBackPressed();
    }

    public void restoreImg() {
        final FirebaseStorage storage = FirebaseStorage.getInstance();

        final String imagePath = getIntent().getStringExtra("imagePath");
        final String imageName = storage.getReference().child(imagePath).getName();
        Task<byte[]> task = storage.getReference().child(imagePath).getBytes(Long.MAX_VALUE);
        task.addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                storage.getReference().child(currentUser.getUid()).child(imageName).putBytes(bytes);
                storage.getReference().child(imagePath).delete();
            }
        });
        super.onBackPressed();
    }



    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 200);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 200:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && cameraAccepted)
                        Log.d("Permission", "Granted");
                    else {

                        Log.d("Permission", "Denied");

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE},
                                                            200);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(ViewVideoActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    void saveToFile() {
        // Initialize Builder
        if(checkPermission()) {

            StorageChooser chooser = new StorageChooser.Builder()
                    .withActivity(ViewVideoActivity.this)
                    .withFragmentManager(getFragmentManager())
                    .withMemoryBar(true)
                    .allowCustomPath(true)
                    .setType(StorageChooser.DIRECTORY_CHOOSER)
                    .withPredefinedPath("/storage")
                    .build();

// Show dialog whenever you want by
            chooser.show();

// get path that the user has chosen
            chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
                @Override
                public void onSelect(String path) {
                    Log.e("SELECTED_PATH", path);
                    new DownloadTask(ViewVideoActivity.this, imageUrl, dataBundle.getString("imageTitle")+"."+dataBundle.getString("extension"), path);
                }
            });
        } else {
            requestPermission();
        }
//        final Intent chooserIntent = new Intent(this, DirectoryChooserActivity.class);
//
//        final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
//                .newDirectoryName("DirChooserSample")
//                .allowReadOnlyDirectory(true)
//                .allowNewDirectoryNameModification(true)
//                .build();
//
//        chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);
//        startActivityForResult(chooserIntent, 2);
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("file/*");
////        intent.setType("folder/*");
//
////        Intent theIntent = new Intent(Intent.ACTION_PICK);
////        theIntent.putExtra(Intent.EXTRA_TITLE,"A Custom Title"); //optional
////        theIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS); //optional
////        try {
//            startActivityForResult(intent,2);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
