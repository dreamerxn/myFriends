package com.dreamclub.myfriends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dreamclub.myfriends.Adapters.MyRecAdapter;
import com.dreamclub.myfriends.Data.DataModel;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView mRecycler;
    MyRecAdapter adapter;
    List<DataModel> list;
    FirebaseDatabase database;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    DatabaseReference dbRef;
    String acID;

    ExtendedFloatingActionButton addButton;
    ClipboardManager clipboardManager;
    ProgressBar myProgressBar;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewsFind();

        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        mRecycler.setHasFixedSize(false);
        list = new ArrayList<>();


        appCheck();
        fromRDatabase();

    }

    public void appCheck(){
        FirebaseApp.initializeApp(/*context=*/ this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());

        firebaseAppCheck.getToken(true)
                .addOnSuccessListener(appCheckTokenResult -> {
                    Toast.makeText(MainActivity.this, "Token: "+appCheckTokenResult.getToken(), Toast.LENGTH_SHORT).show();
                    Log.d("Token","Token: "+appCheckTokenResult.getToken() );

                });

    }

    private void viewsFind(){
        addButton = findViewById(R.id.addButton);
        myProgressBar = findViewById(R.id.myProgress);
        mRecycler = findViewById(R.id.friendsList);

        addButton.setOnClickListener(v->startActivity(new Intent(this, AddFriend.class)));
    }


    public void fromRDatabase(){
        database = FirebaseDatabase.getInstance("https://my-friends-91830-default-rtdb.europe-west1.firebasedatabase.app/");

        try {
            database.setPersistenceEnabled(true);
        }catch (Exception e){
            Log.e("DataBase", e.toString());
        }

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        assert currentUser != null;
        String mail = currentUser.getEmail();
        assert mail != null;
        int pos = mail.indexOf('@');
        mail = mail.substring(0, pos);
        acID = delNoDigOrLet(mail);
        dbRef = database.getReference("DATA/"+acID);
        dbRef.keepSynced(true);

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);

        mRecycler.setLayoutManager(staggeredGridLayoutManager);
        adapter = new MyRecAdapter(this, list, clipboardManager);


        dbRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();

                for(DataSnapshot dataSnapshot : snapshot.getChildren()){

                    DataModel dataModel = dataSnapshot.getValue(DataModel.class);
                    list.add(dataModel);
                }
                mRecycler.setAdapter(adapter);

                myProgressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "GG: "+error, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private static String delNoDigOrLet (String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLetterOrDigit(s.charAt(i)))
                sb.append(s.charAt(i));
        }
        return sb.toString();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    protected void onPause(){

        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();
    }
}