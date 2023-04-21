package com.dreamclub.myfriends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.dreamclub.myfriends.Adapters.MyRecAdapter;
import com.dreamclub.myfriends.Crypt.EncryptionUtils;
import com.dreamclub.myfriends.Data.DataModel;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView mRecycler;
    MyRecAdapter adapter;
    Spinner sort_by;
    List<DataModel> list, sortedList;
    FirebaseDatabase database;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    DatabaseReference dbRef;
    String acID, acId, UID;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    ImageButton layout_change_button;
    FloatingActionButton addButton;
    ClipboardManager clipboardManager;
    ProgressBar myProgressBar;
    BottomNavigationView bottomNavigationView;
    int layout = 0;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance("https://my-friends-91830-default-rtdb.europe-west1.firebasedatabase.app/");

        try {
            database.setPersistenceEnabled(true);
        }catch (Exception e){
            Log.e("DataBase", e.toString());
        }

        viewsFind();

        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        mRecycler.setLayoutManager(linearLayoutManager);

        mRecycler.setHasFixedSize(false);
        list = new ArrayList<>();

        getAccount();

        appCheck();


        ArrayAdapter<CharSequence> mAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.spinner_values,
                android.R.layout.simple_spinner_item
        );
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sort_by.setAdapter(mAdapter);

        sort_by.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedValue = parent.getItemAtPosition(position).toString();
                if (position==0){
                    fromRDatabase();
//                    fromRDatabase(0);

                } else if (position==1) {
                    listSort(position);
//                    fromRDatabase(1);

                } else if (position==2) {
                    listSort(position);
//                    fromRDatabase(2);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Ничего не делаем
            }
        });



    }


    public void appCheck(){
        FirebaseApp.initializeApp(/*context=*/ this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());

        firebaseAppCheck.getToken(true)
                .addOnSuccessListener(appCheckTokenResult -> {
                    Log.d("Token","Token: "+appCheckTokenResult.getToken() );

                });

    }

    public void listSort(int pos){
        if (pos==1) {
            sortedList = list;

            list.sort(Comparator.comparing(DataModel::getBirthDate));
            adapter = new MyRecAdapter(MainActivity.this, list, clipboardManager);
        } else if (pos==2) {
            list.sort(Comparator.comparing(DataModel::getName));
            adapter = new MyRecAdapter(MainActivity.this, list, clipboardManager);
        }
        updateUIofRecycler(15);
    }

    public void updateUIofRecycler(int i){
        if (i==0){
            if (layout==0){
                layout=1;
                StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                mRecycler.setLayoutManager(staggeredGridLayoutManager);
                adapter.setLinear(false);
            } else if (layout==1) {
                layout=0;
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                mRecycler.setLayoutManager(linearLayoutManager);
                adapter.setLinear(true);
            }
        }
        else {
            if (layout==1){
                StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                mRecycler.setLayoutManager(staggeredGridLayoutManager);
                adapter.setLinear(false);
            } else if (layout==0) {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                mRecycler.setLayoutManager(linearLayoutManager);
                adapter.setLinear(true);
            }
        }


        mRecycler.setAdapter(adapter);
        if (adapter!=null)  adapter.notifyDataSetChanged();

    }
    private void viewsFind(){
        addButton = findViewById(R.id.addButton);
        myProgressBar = findViewById(R.id.myProgress);
        mRecycler = findViewById(R.id.friendsList);

        sort_by = findViewById(R.id.sort_by);

        layout_change_button = findViewById(R.id.layout_change_button);

        layout_change_button.setOnClickListener(v->{
            updateUIofRecycler(0);
        });
        addButton.setOnClickListener(v->startActivity(new Intent(this, AddFriend.class)));

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setBackground(null);
        bottomNavigationView.getMenu().getItem(1).setEnabled(false);

    }


    public void fromRDatabase(){
        dbRef = database.getReference("DATA/"+UID);



        dbRef.keepSynced(true);



        adapter = new MyRecAdapter(this, list, clipboardManager);

        dbRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    DataModel dataModel = childSnapshot.getValue(DataModel.class);
                    String nnn = childSnapshot.getKey();
//                    list.add(dataModel);
                    String ph = dataModel.getPhotoURL();
                    String nm = dataModel.getName();
                    String tg = dataModel.getTgURL();
                    String ig = dataModel.getIgURL();
                    String cl = dataModel.getCallNumb();
                    String cr = dataModel.getCardNumb();
                    String bd = dataModel.getBirthDate();
                    String photoURL = "";
                    String name;
                    String tgURL;
                    String igURL;
                    String callNumb;
                    String cardNumb;
                    String birthDate;
                    try {
                        if (ph.length()>0){
                            photoURL = EncryptionUtils.decrypt(ph, acId);
                        }

                        name = EncryptionUtils.decrypt(nm, acId);
                        tgURL = EncryptionUtils.decrypt(tg, acId);
                        igURL = EncryptionUtils.decrypt(ig, acId);
                        callNumb = EncryptionUtils.decrypt(cl, acId);
                        cardNumb = EncryptionUtils.decrypt(cr, acId);
                        birthDate = EncryptionUtils.decrypt(bd, acId);


                        list.add(new DataModel(photoURL, name, tgURL, igURL, callNumb, cardNumb, birthDate, dataModel.getId()));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                updateUIofRecycler(111);
                adapter.notifyDataSetChanged();
                myProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FireBase", "Err: "+error);
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
    private void getAccount(){
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        assert currentUser != null;
        String mail = currentUser.getEmail();
        assert mail != null;
        int pos = mail.indexOf('@');
        mail = mail.substring(0, pos);
        acID = delNoDigOrLet(mail);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this, gso);
        UID = currentUser.getUid();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account !=null){
            acId = account.getId();
        }
    }

    @Override
    protected void onStart() {
        fromRDatabase();
        super.onStart();

    }
}