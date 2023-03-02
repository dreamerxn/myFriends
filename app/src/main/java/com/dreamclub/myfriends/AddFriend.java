package com.dreamclub.myfriends;

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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.dreamclub.myfriends.Crypt.EncryptionUtils;
import com.dreamclub.myfriends.Data.DataModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public class AddFriend extends AppCompatActivity {

    private static final int PICK_PHOTO_FOR_AVATAR = 1;
    Button choose;

    Uri imageUri;
    TextInputEditText name, tgurl, igurl, callnumb, cardnumb;
    String strName, strTgUrl, strIgUrl, strCallNumb, strCardNumb, strBirthDate;
    ExtendedFloatingActionButton addButton;

    FirebaseDatabase database;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    DatabaseReference dbRef;
    String acID;
    MaterialButton birthDate;
    ImageButton choosePhoto;

    int mYear, mMonth, mDay;
    String UID;

    EncryptionUtils encrypt;
    TextInputLayout name_layout, tg_layout, ig_layout, call_layout, card_layout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        database = FirebaseDatabase.getInstance("https://my-friends-91830-default-rtdb.europe-west1.firebasedatabase.app/");

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        assert currentUser != null;
        String mail = currentUser.getEmail();
        UID = currentUser.getUid();
        assert mail != null;
        int pos = mail.indexOf('@');
        mail = mail.substring(0, pos);
        acID = delNoDigOrLet(mail);
        dbRef = database.getReference("DATA/"+acID);
        dbRef.keepSynced(true);

        encrypt = new EncryptionUtils();

        viewFind();
        choosePhoto.setOnClickListener(v->pickImage());

        addButton.setOnClickListener(v->{
            if (imageUri!=null){
                uploadImage(imageUri);
            }
            else Toast.makeText(this, "Choose a photo first", Toast.LENGTH_SHORT).show();
            
        });
        birthDate.setOnClickListener(v->{
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

            @SuppressLint("SetTextI18n") DatePickerDialog.OnDateSetListener dateSetListener = (view, year1, month1, dayOfMonth1) -> {
                mYear = year1;
                mMonth = month1;
                mDay = dayOfMonth1;
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

    public void uploadImage(Uri uri){

        strName = name.getText().toString();
        strTgUrl = tgurl.getText().toString();
        strIgUrl = igurl.getText().toString();
        strCallNumb = callnumb.getText().toString();
        strCardNumb = cardnumb.getText().toString();
        mMonth++;

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

                StorageReference storageReference = firebaseStorage.getReference(acID+"/"+strName+".jpg");

                UploadTask uploadTask = storageReference.putFile(uri);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(AddFriend.this, "Success", Toast.LENGTH_SHORT).show();
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                            @Override

                            public void onSuccess(Uri uri) {

                                String PhotoURL = uri.toString();
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

                                pushData(encPhoto, encName, encTg, encIg, encCall, encCard, encBirth);
                                finish();
                            }
                        });
                    }


                });
            }
        }
        else {
            Toast.makeText(this, getString(R.string.name_error), Toast.LENGTH_SHORT).show();
        }
        


    }
    public void pushData(String photoURL, String name, String tgURL, String igURL, String callNumb, String cardNUMB, String birthDate){


        int UID = ThreadLocalRandom.current().nextInt(0, 100000000);

        DataModel data = new DataModel(photoURL, name, tgURL, igURL, callNumb, cardNUMB, birthDate);
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
        InputFilter noSpaceFilter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
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
            }
        };

// Устанавливаем фильтр ввода в EditText
        editText.setFilters(new InputFilter[] {noSpaceFilter});
    }
}