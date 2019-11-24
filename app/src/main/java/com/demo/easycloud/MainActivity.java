package com.demo.easycloud;

import com.demo.easycloud.adapters.ChatsAdapter;
import com.demo.easycloud.adapters.ContactsAdapter;
import com.demo.easycloud.adapters.FilesAdapter;
import com.demo.easycloud.models.File;
import com.demo.easycloud.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SimpleAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser currentUser;
    FirebaseStorage storage;
    StorageReference storageRef;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private TabLayout tabs;
    private SearchView searchView;

    ProgressBar progressBar;

    String newFolderName;

    FloatingActionButton fab, newFolder, uploadFile;
    private boolean isFABOpen;

    SharedPreferences pref;

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
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        tabs = (TabLayout) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);// inbuild adapter to load fragment
        tabs.setupWithViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }@Override
            public void onPageSelected(int position) {//tab
                if(position != 0) {//fab
                    fab.hide();
                    uploadFile.hide();
                    newFolder.hide();
                }
                else {
                    fab.show();
                    uploadFile.show();
                    newFolder.show();
                }
            }@Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();// getting storage bucket reference

        fab = (FloatingActionButton) findViewById(R.id.fab);
        newFolder = (FloatingActionButton) findViewById(R.id.newFolder);
        uploadFile = (FloatingActionButton) findViewById(R.id.uploadFile);

        if(auth.getCurrentUser() == null) {
            Intent redirectToLogin = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(redirectToLogin);
        }

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
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);// open local file
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");// all file
                startActivityForResult(intent, 2);//upload
            }
        });

        newFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Enter Folder Name");

                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);// define text

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {//upload
//                        Log.d("Input", input.getText().toString());
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("*/*");
                        newFolderName = input.getText().toString();
                        startActivityForResult(intent, 3);//upload
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

    }

// open n close fab button
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {//after selecting file this event will trigger
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {//transfering the the receive event to all fragment
            if(requestCode == 3) {// passing the new folder name to fragment if 3
                data.putExtra("folderName", newFolderName);
            }
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);

//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        searchView = (SearchView) menu.findItem(R.id.action_search)
//                .getActionView();
//        searchView.setSearchableInfo(searchManager
//                .getSearchableInfo(getComponentName()));
//        searchView.setMaxWidth(Integer.MAX_VALUE);
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                for (Fragment fragment : getSupportFragmentManager().getFragments()) {
//                    Log.d("FragmentId", fragment.getContext()+"");
//                }
////                movieAdapter.getFilter().filter(query);
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String query) {
//                movieAdapter.getFilter().filter(query);
//                return false;
//            }
//        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_light_theme) {
            pref.edit().putBoolean("darkTheme", false).apply();
            finish();
            startActivity(getIntent());
        } else if (id == R.id.action_dark_theme) {
            pref.edit().putBoolean("darkTheme", true).apply();
            finish();
            startActivity(getIntent());
        } else if (id == R.id.action_settings) {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
        } else if(item.getItemId() == R.id.action_view_trash) {
            Intent i = new Intent(MainActivity.this, ViewFolderActivity.class);

            Bundle dataBundle = new Bundle();
            dataBundle.putString("path", currentUser.getUid()+"/trash");
            dataBundle.putString("imageTitle", "Trash");
            i.putExtras(dataBundle);

            startActivity(i);
        } else if(item.getItemId() == R.id.action_web_login) {
            Intent i = new Intent(MainActivity.this, QrCodeScannerActivity.class);
            startActivity(i);
        } else if(item.getItemId() == R.id.action_logout) {
            auth.signOut();

            String webLoginCode = pref.getString("webLogin", "");//sync logout

            FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference webchatDb = mFirebaseDatabase.getReference("webchat");
            webchatDb.child(webLoginCode).setValue(null);

            finish();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    public static class DashboardFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";
        private List<File> fileList;

        FirebaseAuth auth;
        FirebaseUser currentUser;
        FirebaseStorage storage;
        StorageReference storageRef;

        ProgressBar progressBar;

        FloatingActionButton fab, newFolder, uploadFile;
        RecyclerView listOfFiles;
        FilesAdapter filesAdapter;
        private boolean isFABOpen;

        SearchView searchView;

        SharedPreferences pref;

        public DashboardFragment() {
            auth = FirebaseAuth.getInstance();
            currentUser = auth.getCurrentUser();

            storage = FirebaseStorage.getInstance();
            storageRef = storage.getReference();
        }

        public static DashboardFragment newInstance() {
            DashboardFragment fragment = new DashboardFragment();
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            setHasOptionsMenu(true);

            auth = FirebaseAuth.getInstance();
            currentUser = auth.getCurrentUser();

            fileList = new ArrayList<File>();


            progressBar = (ProgressBar) rootView.findViewById(R.id.progressbar_dashboard);

            listOfFiles = (RecyclerView) rootView.findViewById(R.id.files);

            filesAdapter = new FilesAdapter(fileList);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
            listOfFiles.setLayoutManager(mLayoutManager);
            listOfFiles.setItemAnimator(new DefaultItemAnimator());
            listOfFiles.setAdapter(filesAdapter);

            if(auth.getCurrentUser() == null) {
                Intent redirectToLogin = new Intent(getContext(), LoginActivity.class);
                startActivity(redirectToLogin);
            } else {
                getFiles();
            }

            return rootView;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == 2) {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        Log.d("NewFileUploaded", data.getDataString());
                        final Uri uri = data.getData();
                        progressBar.setVisibility(View.VISIBLE);
                        uploadNewFile(uri, "");
                    }
                }
            } else if (requestCode == 3) {//RECREATING
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        final Uri uri = data.getData();
                        progressBar.setVisibility(View.VISIBLE);
                        uploadNewFile(uri, data.getStringExtra("folderName")+"/");
                    }
                }
            }
        }

        private void uploadNewFile(Uri uri, String folderName) {
            StorageReference newFileRef = storageRef.child(currentUser.getUid()+"/"+folderName+uri.getLastPathSegment());
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
            StorageReference dashboardRef = storage.getReference().child(currentUser.getUid());// to view specific user's file

            dashboardRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                @Override
                public void onSuccess(ListResult listResult) {
                    fileList.clear();//refresh
                    for (StorageReference folder: listResult.getPrefixes()) {// to view folder
                        if(!folder.getName().equals("trash")) {// eliminating trash folder
                            File file = new File();
                            file.setFileType("Folder");
                            file.setTitle(folder.getName());
                            file.setFolder(true);
                            file.setPath(folder.getPath());
                            fileList.add(file);
                            filesAdapter.notifyDataSetChanged();//refresh the listview
                        }
                    }

                    for (final StorageReference item: listResult.getItems()) {//can view file
                        item.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                            @Override
                            public void onSuccess(StorageMetadata storageMetadata) {
                                final File file = new File();
                                DateFormat uploadedDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                                Date uploadedDate = new Date(storageMetadata.getUpdatedTimeMillis());
                                file.setUpdatedOn(uploadedDateFormat.format(uploadedDate));//upload timestamp day
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

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_main, menu);

            MenuItem viewTrash = menu.findItem(R.id.action_view_trash);
            viewTrash.setVisible(true);

            SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
            searchView = (SearchView) menu.findItem(R.id.action_search)
                    .getActionView();
            searchView.setSearchableInfo(searchManager
                    .getSearchableInfo(getActivity().getComponentName()));
            searchView.setMaxWidth(Integer.MAX_VALUE);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    filesAdapter.getFilter().filter(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    filesAdapter.getFilter().filter(query);
                    return false;
                }
            });
        }
    }

    public static class ChatFragment extends Fragment {
        private List<User> contactsList;

        FirebaseAuth auth;
        FirebaseUser currentUser;
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

        ProgressBar progressBar;

        RecyclerView listOfFiles;
        ChatsAdapter chatsAdapter;

        SearchView searchView;
        SharedPreferences pref;

        public ChatFragment() {
            auth = FirebaseAuth.getInstance();
            currentUser = auth.getCurrentUser();
        }

        public static ChatFragment newInstance() {
            ChatFragment fragment = new ChatFragment();
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_chats, container, false);

            setHasOptionsMenu(true);

            auth = FirebaseAuth.getInstance();
            currentUser = auth.getCurrentUser();

            contactsList = new ArrayList<User>();

            progressBar = (ProgressBar) rootView.findViewById(R.id.progressbar_chats);

            listOfFiles = (RecyclerView) rootView.findViewById(R.id.chatList);


            chatsAdapter = new ChatsAdapter(contactsList);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
            listOfFiles.setLayoutManager(mLayoutManager);
            listOfFiles.setItemAnimator(new DefaultItemAnimator());
            listOfFiles.setAdapter(chatsAdapter);

            if(auth.getCurrentUser() == null) {
                Intent redirectToLogin = new Intent(getContext(), LoginActivity.class);
                startActivity(redirectToLogin);
            } else {
                getChats();
            }

            return rootView;
        }

        public void getChats() {
            mFirebaseDatabase.getReference("chathistories").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    contactsList.clear();

                    User groupChat = new User();
                    groupChat.setName("Group Chat");
                    groupChat.setUid("groupchat");

                    contactsList.add(groupChat);

                    if(dataSnapshot.getValue() != null) {
                        for (DataSnapshot child: dataSnapshot.getChildren()) {
                            User user = child.getValue(User.class);
                            contactsList.add(user);
                        }
                    }
                    chatsAdapter.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_main, menu);

            MenuItem viewTrash = menu.findItem(R.id.action_view_trash);
            viewTrash.setVisible(false);

            SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
            searchView = (SearchView) menu.findItem(R.id.action_search)
                    .getActionView();
            searchView.setSearchableInfo(searchManager
                    .getSearchableInfo(getActivity().getComponentName()));
            searchView.setMaxWidth(Integer.MAX_VALUE);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    chatsAdapter.getFilter().filter(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    chatsAdapter.getFilter().filter(query);
                    return false;
                }
            });
        }
    }

    public static class ContactFragment extends Fragment {
        private List<User> contactsList;

        FirebaseAuth auth;
        FirebaseUser currentUser;
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

        ProgressBar progressBar;

        RecyclerView listOfContacts;
        ContactsAdapter contactsAdapter;

        SearchView searchView;

        SharedPreferences pref;
        public ContactFragment() {
            auth = FirebaseAuth.getInstance();
            currentUser = auth.getCurrentUser();
        }

        public static ContactFragment newInstance() {
            ContactFragment fragment = new ContactFragment();
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);

            setHasOptionsMenu(true);

            auth = FirebaseAuth.getInstance();
            currentUser = auth.getCurrentUser();

            contactsList = new ArrayList<User>();

            progressBar = (ProgressBar) rootView.findViewById(R.id.progressbar_contacts);

            listOfContacts = (RecyclerView) rootView.findViewById(R.id.contactList);

            contactsAdapter = new ContactsAdapter(contactsList);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
            listOfContacts.setLayoutManager(mLayoutManager);
            listOfContacts.setItemAnimator(new DefaultItemAnimator());
            listOfContacts.setAdapter(contactsAdapter);


            if(auth.getCurrentUser() == null) {
                Intent redirectToLogin = new Intent(getContext(), LoginActivity.class);
                startActivity(redirectToLogin);
            } else {
                getContacts();
            }

            return rootView;
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

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_main, menu);

            MenuItem viewTrash = menu.findItem(R.id.action_view_trash);
            viewTrash.setVisible(false);

            SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
            searchView = (SearchView) menu.findItem(R.id.action_search)
                    .getActionView();
            searchView.setSearchableInfo(searchManager
                    .getSearchableInfo(getActivity().getComponentName()));
            searchView.setMaxWidth(Integer.MAX_VALUE);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    contactsAdapter.getFilter().filter(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    contactsAdapter.getFilter().filter(query);
                    return false;
                }
            });
        }
    }

    public static class SectionsPagerAdapter extends FragmentPagerAdapter {

        private List<String> pageTitle;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            pageTitle = new ArrayList<String>();
            pageTitle.add("Dashboard");
            pageTitle.add("Chats");
            pageTitle.add("Contacts");
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0) {
                return DashboardFragment.newInstance();
            } else if(position == 1) {
                return ChatFragment.newInstance();
            } else {
                return ContactFragment.newInstance();
            }
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitle.get(position);
        }

        @Override
        public int getCount() {
            return 3;
        }

    }

}
