package com.dreamclub.myfriends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dreamclub.myfriends.Crypt.EncryptionUtils;
import com.dreamclub.myfriends.Data.DataModel;
import com.dreamclub.myfriends.Screens.EditActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class AddFriend extends AppCompatActivity {

    private static final int PICK_PHOTO_FOR_AVATAR = 1;
    Button choose;

    Uri imageUri;
    TextInputEditText name, tgurl, igurl, callnumb, cardnumb;
    CheckBox hasTg, hasIg, hasCall, hasCard;
    String strName, strTgUrl, strIgUrl, strCallNumb, strCardNumb, strBirthDate, photoUrl;
    ExtendedFloatingActionButton addButton, delButton;

    String id, itemId;

    FirebaseDatabase database;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    DatabaseReference dbRef;
    String acID, acId;
    MaterialButton birthDate;
    ImageButton choosePhoto;

    int mYear, mMonth, mDay, dayOfYear;
    String UID;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    Calendar dayOfYearCalendar;

    EncryptionUtils encrypt;
    TextInputLayout name_layout, tg_layout, ig_layout, call_layout, card_layout;

    boolean isEdit;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        viewFind();

        Intent intent = getIntent();
        isEdit = intent.getBooleanExtra("isEdit", false);
        if (isEdit){
            addButton.setText(getString(R.string.save));
            photoUrl = intent.getStringExtra("photo");
            strName = intent.getStringExtra("name");
            strTgUrl = intent.getStringExtra("tg");
            strIgUrl = intent.getStringExtra("ig");
            strCallNumb = intent.getStringExtra("call");
            strCardNumb = intent.getStringExtra("card");
            mYear = intent.getIntExtra("year", 0);
            mMonth = intent.getIntExtra("month", 0);
            mDay = intent.getIntExtra("day", 0);
            itemId = intent.getStringExtra("id");
            updateUI();
            delButton.setVisibility(View.VISIBLE);

        }
        else{
            photoUrl = "";
            delButton.setVisibility(View.GONE);

        }

        database = FirebaseDatabase.getInstance("https://my-friends-91830-default-rtdb.europe-west1.firebasedatabase.app/");

        getAccount();

        dbRef = database.getReference("DATA/"+acId);
        dbRef.keepSynced(true);

        encrypt = new EncryptionUtils();


        checkBoxes();
        choosePhoto.setOnClickListener(v->pickImage());


        addButton.setOnClickListener(v->{
            long timestamp = System.currentTimeMillis();
            id = Long.toString(timestamp);
            if (imageUri!=null){
                upload(imageUri, id);
            }
            else {
                upload(id, photoUrl);
            }

        });

        birthDate.setOnClickListener(v->{
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);


            @SuppressLint("SetTextI18n") DatePickerDialog.OnDateSetListener dateSetListener = (view, year1, month1, dayOfMonth1) -> {
                mYear = year1;
                mMonth = month1+1;
                mDay = dayOfMonth1;

                dayOfYearCalendar = Calendar.getInstance();
                dayOfYearCalendar.set(Calendar.YEAR, mYear);
                dayOfYearCalendar.set(Calendar.MONTH, mMonth-1);
                dayOfYearCalendar.set(Calendar.DAY_OF_MONTH, mDay);
                dayOfYear = dayOfYearCalendar.get(Calendar.DAY_OF_YEAR);
                birthDate.setText(mYear+"-"+mMonth+"-"+mDay);
            };
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, dateSetListener, year, month, dayOfMonth);
            datePickerDialog.show();
        });
    }

    public void viewFind(){

        addButton = findViewById(R.id.buttonAdd);
        name = findViewById(R.id.editName);
        tgurl = findViewById(R.id.editTgUrl);
        igurl = findViewById(R.id.editIgUrl);
        callnumb = findViewById(R.id.editCallNumber);
        cardnumb = findViewById(R.id.editCardNumber);
        birthDate = findViewById(R.id.birthDatePick);

        card_layout = findViewById(R.id.card_layout);

        choosePhoto = findViewById(R.id.choosedPhoto);

        tg_layout = findViewById(R.id.tg_layout);
        ig_layout = findViewById(R.id.ig_layout);
        call_layout = findViewById(R.id.call_layout);
        card_layout = findViewById(R.id.card_layout);

        hasTg = findViewById(R.id.hasTg);
        hasIg = findViewById(R.id.hasIg);
        hasCall = findViewById(R.id.hasCall);
        hasCard = findViewById(R.id.hasCard);


        delButton = findViewById(R.id.deleteButton);
        delButton.setOnClickListener(v->delete(itemId));

        checkForWhiteSpaces(tgurl);
        checkForWhiteSpaces(igurl);
        cardnumb.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String cardNumber = s.toString().replaceAll("\\s", "");

                if(!cardNumber.matches("^4[0-9]{12}(?:[0-9]{3})?$|^(5[1-5][0-9]{2}|222[1-9]|22[3-9][0-9]|2[3-6][0-9]{2}|27[01][0-9]|2720)[0-9]{12}$|^(?:2131|1800|35\\d{3})\\d{11}$")){
                card_layout.setError(getString(R.string.card_error));

                } else {
                    card_layout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    public void checkBoxes(){
        hasTg.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                tg_layout.setVisibility(View.VISIBLE);
                // действия, которые нужно выполнить, если флажок установлен
            } else {
                tg_layout.setVisibility(View.GONE);
                // действия, которые нужно выполнить, если флажок снят
            }
        });
        hasIg.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                ig_layout.setVisibility(View.VISIBLE);
                // действия, которые нужно выполнить, если флажок установлен
            } else {
                ig_layout.setVisibility(View.GONE);
                // действия, которые нужно выполнить, если флажок снят
            }
        });
        hasCall.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                call_layout.setVisibility(View.VISIBLE);
                // действия, которые нужно выполнить, если флажок установлен
            } else {
                call_layout.setVisibility(View.GONE);
                // действия, которые нужно выполнить, если флажок снят
            }
        });;
        hasCard.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                card_layout.setVisibility(View.VISIBLE);
                // действия, которые нужно выполнить, если флажок установлен
            } else {
                card_layout.setVisibility(View.GONE);
                // действия, которые нужно выполнить, если флажок снят
            }
        });;
    }

    @SuppressLint("SetTextI18n")
    public void updateUI(){
        name.setText(strName);
        tgurl.setText(strTgUrl);
        igurl.setText(strIgUrl);
        callnumb.setText(strCallNumb);
        cardnumb.setText(strCardNumb);
        if (photoUrl.length()>0){
            Glide.with(this)
                    .load(photoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(choosePhoto);
        }
        if (strTgUrl.length()>0){
            tg_layout.setVisibility(View.VISIBLE);
            hasTg.setChecked(true);
        }

        if (strIgUrl.length()>0){
            ig_layout.setVisibility(View.VISIBLE);
            hasIg.setChecked(true);
        }

        if (strCallNumb.length()>0){
            call_layout.setVisibility(View.VISIBLE);
            hasCall.setChecked(true);
        }

        if (strCardNumb.length()>0){
            card_layout.setVisibility(View.VISIBLE);
            hasCard.setChecked(false);
        }

        birthDate.setText(mYear+"-"+mMonth+"-"+mDay);
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
            choosePhoto.setImageURI(selectedImage);
            choosePhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageUri = selectedImage;
        }
    }

    public void upload(Uri uri, String mId){
        if (photoUrl!=null&&photoUrl.length()>0){
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl);
            storageRef.delete()
                    .addOnSuccessListener(aVoid -> {

                    })
                    .addOnFailureListener(e -> {
                        // Произошла ошибка при удалении файла
                    });
        }

        strName = Objects.requireNonNull(name.getText()).toString();
        strTgUrl = Objects.requireNonNull(tgurl.getText()).toString();
        strIgUrl = Objects.requireNonNull(igurl.getText()).toString();
        strCallNumb = Objects.requireNonNull(callnumb.getText()).toString();
        strCardNumb = Objects.requireNonNull(cardnumb.getText()).toString();

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        if (!strName.isEmpty()){
            if (mYear==0 || mMonth==0 || mDay==0){
                Toast.makeText(this, getString(R.string.bday_error), Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, getString(R.string.upload), Toast.LENGTH_SHORT).show();
                String strMonth = Integer.toString(mMonth);
                if (mMonth<10){
                    strMonth = "0"+mMonth;
                }
                String strDay = Integer.toString(mDay);
                if (mDay<10){
                    strDay = "0"+mDay;
                }
                strBirthDate = mYear+"-"+strMonth+"-"+strDay;

                StorageReference storageReference = firebaseStorage.getReference(acId+"/"+strName+".jpg");

                UploadTask uploadTask = storageReference.putFile(uri);
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    storageReference.getDownloadUrl().addOnSuccessListener(uri1 -> {

                        String PhotoURL = uri1.toString();
                        String encPhoto, encName, encTg, encIg, encCall, encCard, encBirth;
                        try {
                            encPhoto = EncryptionUtils.encrypt(PhotoURL, UID);
                            encName = EncryptionUtils.encrypt(strName, UID);
                            encTg = EncryptionUtils.encrypt(strTgUrl, UID);
                            encIg = EncryptionUtils.encrypt(strIgUrl, UID);
                            encCall = EncryptionUtils.encrypt(strCallNumb, UID);
                            encCard = EncryptionUtils.encrypt(strCardNumb, UID);
                            encBirth = EncryptionUtils.encrypt(strBirthDate, UID);

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        if (isEdit) save(encPhoto, encName, encTg, encIg, encCall, encCard, encBirth, itemId);
                        else pushData(encPhoto, encName, encTg, encIg, encCall, encCard, encBirth, mId);
                        finish();
                        Toast.makeText(this, getString(R.string.loaded), Toast.LENGTH_SHORT).show();
                    });
                });
            }
        }
        else {
            Toast.makeText(this, getString(R.string.name_error), Toast.LENGTH_SHORT).show();
        }
        


    }

    public void upload(String mId, String mPhoto){

        strName = Objects.requireNonNull(name.getText()).toString();
        strTgUrl = Objects.requireNonNull(tgurl.getText()).toString();
        strIgUrl = Objects.requireNonNull(igurl.getText()).toString();
        strCallNumb = Objects.requireNonNull(callnumb.getText()).toString();
        strCardNumb = Objects.requireNonNull(cardnumb.getText()).toString();

        if (!strName.isEmpty()){
            if (mYear==0 || mMonth==0 || mDay==0){
                Toast.makeText(this, getString(R.string.bday_error), Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, getString(R.string.upload), Toast.LENGTH_SHORT).show();
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
                    if (mPhoto.length()>0) encPhoto = EncryptionUtils.encrypt(mPhoto, UID);
                    else encPhoto = "";
                    encName = EncryptionUtils.encrypt(strName, UID);
                    encTg = EncryptionUtils.encrypt(strTgUrl, UID);
                    encIg = EncryptionUtils.encrypt(strIgUrl, UID);
                    encCall = EncryptionUtils.encrypt(strCallNumb, UID);
                    encCard = EncryptionUtils.encrypt(strCardNumb, UID);
                    encBirth = EncryptionUtils.encrypt(strBirthDate, UID);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (isEdit) save(encPhoto, encName, encTg, encIg, encCall, encCard, encBirth, itemId);
                else pushData(encPhoto, encName, encTg, encIg, encCall, encCard, encBirth, mId);
                finish();
                Toast.makeText(this, getString(R.string.loaded), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, getString(R.string.name_error), Toast.LENGTH_SHORT).show();
        }



    }
    public void pushData(String photoURL, String name, String tgURL, String igURL, String callNumb, String cardNUMB, String birthDate, String mId){

        DataModel data = new DataModel(photoURL, name, tgURL, igURL, callNumb, cardNUMB, birthDate, mId);
        dbRef.push().setValue(data);

    }
    private static String delNoDigOrLet (String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLetterOrDigit(s.charAt(i)))
                sb.append(s.charAt(i));
        }
        return sb.toString();
    }

    public void checkForWhiteSpaces(EditText editText){
        InputFilter noSpaceFilter = (source, start, end, dest, dstart, dend) -> {
            // Проверяем каждый введенный символ
            for (int i = start; i < end; i++) {
                if (Character.isWhitespace(source.charAt(i))) {
                    // Если символ является пробелом, то возвращаем пустую строку, чтобы его не добавлять
                    String text = editText.getText().toString();
                    if (!text.isEmpty()) {
                        return text;
                    }
                    else return "";
                }
            }
            // Если символ не является пробелом, то возвращаем его без изменений
            return null;
        };

// Устанавливаем фильтр ввода в EditText
        editText.setFilters(new InputFilter[] {noSpaceFilter});
    }


    private void getAccount(){
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        assert currentUser != null;
        String mail = currentUser.getEmail();
        UID = currentUser.getUid();
        assert mail != null;
        int pos = mail.indexOf('@');
        mail = mail.substring(0, pos);
        acID = delNoDigOrLet(mail);


        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account !=null){
            acId = account.getId();
        }
    }


    public void delete(String mId){
        Query query = dbRef.orderByChild("id").equalTo(mId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()){
                    childSnapshot.getRef().removeValue();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if (photoUrl!=null&&photoUrl.length()>0){
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl);
            storageRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddFriend.this, getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                        finishAffinity();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    })
                    .addOnFailureListener(e -> {
                        // Произошла ошибка при удалении файла
                    });
        }

    }
    public void save(String photoURL, String name, String tgURL, String igURL, String callNumb, String cardNUMB, String birthDate, String mId){
        Query query = dbRef.orderByChild("id").equalTo(mId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()){
                    DataModel data = new DataModel(photoURL, name, tgURL, igURL, callNumb, cardNUMB, birthDate, mId);
                    childSnapshot.getRef().setValue(data);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}