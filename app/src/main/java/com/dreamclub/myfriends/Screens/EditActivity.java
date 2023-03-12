package com.dreamclub.myfriends.Screens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dreamclub.myfriends.Crypt.EncryptionUtils;
import com.dreamclub.myfriends.Data.DataModel;
import com.dreamclub.myfriends.MainActivity;
import com.dreamclub.myfriends.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Calendar;
import java.util.Objects;

public class EditActivity extends AppCompatActivity {

    private static final int PICK_PHOTO_FOR_AVATAR = 1;

    FirebaseDatabase database;
    DatabaseReference dbRef;

    FirebaseUser currentUser;

    Uri imageUri;

    String photoUrl, strName, tg, ig, call, card, id, strBirthDate, UID;
    int year, month, day;
    int mYear, mMonth, mDay, dayOfYear;

    TextInputLayout name_layout, tg_layout, ig_layout, call_layout, card_layout;

    ImageButton editPhoto;
    TextInputEditText editName, editTg, editIg, editCall, editCard;
    CheckBox hasTg, hasIg, hasCall, hasCard;

    MaterialButton pickDate;

    ExtendedFloatingActionButton saveButton, deleteButton;

    FirebaseAuth firebaseAuth;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    String acId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        getAccount();

        database = FirebaseDatabase.getInstance("https://my-friends-91830-default-rtdb.europe-west1.firebasedatabase.app/");
        dbRef = database.getReference("DATA/"+acId);

        Intent intent = getIntent();
        photoUrl = intent.getStringExtra("photo");
        strName = intent.getStringExtra("name");
        tg = intent.getStringExtra("tg");
        ig = intent.getStringExtra("ig");
        call = intent.getStringExtra("call");
        card = intent.getStringExtra("card");
        year = intent.getIntExtra("year", 0);
        month = intent.getIntExtra("month", 0);
        day = intent.getIntExtra("day", 0);
        id = intent.getStringExtra("id");



        viewFind();

        updateUI();
        checkBoxes();
    }

    public void viewFind() {

        editName = findViewById(R.id.edit_editName);
        editTg = findViewById(R.id.edit_editTgUrl);
        editIg = findViewById(R.id.edit_editIgUrl);
        editCall = findViewById(R.id.edit_editCallNumber);
        editCard = findViewById(R.id.edit_editCardNumber);


        editPhoto = findViewById(R.id.editPhoto);

        editPhoto.setOnClickListener(v->pickImage());

        name_layout = findViewById(R.id.edit_name_layout);
        tg_layout = findViewById(R.id.edit_tg_layout);
        ig_layout = findViewById(R.id.edit_ig_layout);
        call_layout = findViewById(R.id.edit_call_layout);
        card_layout = findViewById(R.id.edit_card_layout);

        hasTg = findViewById(R.id.edit_hasTg);
        hasIg = findViewById(R.id.edit_hasIg);
        hasCall = findViewById(R.id.edit_hasCall);
        hasCard = findViewById(R.id.edit_hasCard);

        pickDate = findViewById(R.id.edit_birthDatePick);

        pickDate.setOnClickListener(v->{
            Calendar calendar = Calendar.getInstance();
            int Year = calendar.get(Calendar.YEAR);
            int Month = calendar.get(Calendar.MONTH);
            int DayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);


            @SuppressLint("SetTextI18n") DatePickerDialog.OnDateSetListener dateSetListener = (view, year1, month1, dayOfMonth1) -> {


                mYear = year1;
                mMonth = month1+1;
                mDay = dayOfMonth1;

                month1 = month1+1;
                pickDate.setText(year1+"-"+month1+"-"+dayOfMonth1);
            };
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, dateSetListener, Year, Month, DayOfMonth);
            datePickerDialog.show();
        });

        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v->save(id));
        deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v->delete(id));

    }


    public void upload(String mId){

        strName = Objects.requireNonNull(editName.getText()).toString();
        tg = Objects.requireNonNull(editTg.getText()).toString();
        ig = Objects.requireNonNull(editIg.getText()).toString();
        call = Objects.requireNonNull(editCall.getText()).toString();
        card = Objects.requireNonNull(editCard.getText()).toString();

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        if (!strName.isEmpty()){
            if (mYear==0 || mMonth==0 || mDay==0){
                Toast.makeText(this, getString(R.string.bday_error), Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, getString(R.string.upload), Toast.LENGTH_LONG).show();
                String strMonth = Integer.toString(mMonth);
                if (mMonth<10){
                    strMonth = "0"+mMonth;
                }
                String strDay = Integer.toString(mDay);
                if (mDay<10){
                    strDay = "0"+mDay;
                }
                strBirthDate = mYear+"-"+strMonth+"-"+strDay;

                String encPhoto, encName, encTg, encIg, encCall, encCard, encBirth;
                try {
                    encPhoto = "";
                    encName = EncryptionUtils.encrypt(strName, UID);
                    encTg = EncryptionUtils.encrypt(tg, UID);
                    encIg = EncryptionUtils.encrypt(ig, UID);
                    encCall = EncryptionUtils.encrypt(call, UID);
                    encCard = EncryptionUtils.encrypt(card, UID);
                    encBirth = EncryptionUtils.encrypt(strBirthDate, UID);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        }
        else {
            Toast.makeText(this, getString(R.string.name_error), Toast.LENGTH_SHORT).show();
        }



    }

    public void updateUI(){
        editName.setText(strName);
        editTg.setText(tg);
        editIg.setText(ig);
        editCall.setText(call);
        editCard.setText(card);
        if (photoUrl.length()>0){
            Glide.with(this)
                    .load(photoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(editPhoto);
        }
        if (tg.length()>0){
            tg_layout.setVisibility(View.VISIBLE);
            hasTg.setChecked(true);
        }

        if (ig.length()>0){
            ig_layout.setVisibility(View.VISIBLE);
            hasIg.setChecked(true);
        }

        if (call.length()>0){
            call_layout.setVisibility(View.VISIBLE);
            hasCall.setChecked(true);
        }

        if (card.length()>0){
            card_layout.setVisibility(View.VISIBLE);
            hasCard.setChecked(false);
        }

        pickDate.setText(year+"-"+month+"-"+day);
    }

    public void checkBoxes(){
        hasTg.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                tg_layout.setVisibility(View.VISIBLE);
            } else {
                tg_layout.setVisibility(View.GONE);
            }
        });
        hasIg.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                ig_layout.setVisibility(View.VISIBLE);
            } else {
                ig_layout.setVisibility(View.GONE);
            }
        });
        hasCall.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                call_layout.setVisibility(View.VISIBLE);
            } else {
                call_layout.setVisibility(View.GONE);
            }
        });
        hasCard.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                card_layout.setVisibility(View.VISIBLE);
            } else {
                card_layout.setVisibility(View.GONE);
            }
        });
    }

    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FOR_AVATAR && data != null) {
            Uri selectedImage = data.getData();
            editPhoto.setImageURI(selectedImage);
            editPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageUri = selectedImage;
        }
    }


    public void save(String mId){
        Query query = dbRef.orderByChild("id").equalTo(mId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()){
                    DataModel data = new DataModel("photoURL", "name", "tgURL", "igURL", "999", "555", "2023-12-12", mId);
                    childSnapshot.getRef().setValue(data);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void delete(String mId){
        Query query = dbRef.orderByChild("id").equalTo(mId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()){
                    childSnapshot.getRef().removeValue();
                    Toast.makeText(EditActivity.this, "deleted", Toast.LENGTH_SHORT).show();
                    finishAffinity();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAccount(){
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        assert currentUser != null;
        UID = currentUser.getUid();

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account !=null){
            acId = account.getId();
        }
    }

}