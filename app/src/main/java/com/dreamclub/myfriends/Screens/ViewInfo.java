package com.dreamclub.myfriends.Screens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dreamclub.myfriends.AddFriend;
import com.dreamclub.myfriends.Crypt.EncryptionUtils;
import com.dreamclub.myfriends.Data.DataModel;
import com.dreamclub.myfriends.MainActivity;
import com.dreamclub.myfriends.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;

public class ViewInfo extends AppCompatActivity {

    ImageView photo;
    TextView name, counterView;

    MaterialButton tgButton, igButton, callButton, cardButton;
    String photoUrl, strName, tg, ig, call, card, birthDate, id, acId, UID;
    String decPhoto, decName, decTg, decIg, decCall, decCard, decBirth;
    int year, month, day;

    ClipboardManager mClipboardManager;
    ClipData clip;

    FloatingActionButton editButton;

    FirebaseDatabase database;
    DatabaseReference dbRef;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;

    CountDownTimer countDownTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_info);

        viewFind();
        getAccount();


        Intent intent = getIntent();
        id = intent.getStringExtra("id");

        getData(id, true);

    }

    public void viewFind(){
        photo = findViewById(R.id.photoFull);
        name = findViewById(R.id.nameFull);
        tgButton = findViewById(R.id.tgButtonLarge);
        igButton = findViewById(R.id.igButtonLarge);
        callButton = findViewById(R.id.callButtonLarge);
        cardButton = findViewById(R.id.cardButtonLarge);
        counterView = findViewById(R.id.counterLarge);
        editButton = findViewById(R.id.edit_fab);

        editButton.setOnClickListener(v->onEdit());
    }

    public void onEdit(){
        Intent intent = new Intent(this, AddFriend.class);
        intent.putExtra("photo", decPhoto);
        intent.putExtra("name", decName);
        intent.putExtra("tg", decTg);
        intent.putExtra("ig", decIg);
        intent.putExtra("call", decCall);
        intent.putExtra("card", decCard);
        intent.putExtra("year", year);
        intent.putExtra("month", month);
        intent.putExtra("day", day);
        intent.putExtra("id", id);
        intent.putExtra("isEdit", true);
        startActivity(intent);
    }
    public void updateUI(String mName, String mPhoto, String mTg, String mIg, String mCall, String mCard){

        name.setText(mName);

        if (mPhoto.length()>0){
            photo.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(mPhoto)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(photo);
        }

        if (mTg.length()>0){
            tgButton.setVisibility(View.VISIBLE);
            tgButton.setOnClickListener(v->{
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("tg:resolve?domain="+mTg));
                try {
                    startActivity(intent);
                }catch (Exception e){
                    try {
                        intent.setData(Uri.parse("https://t.me/"+mTg));
                    }catch (Exception er){
                        displayToast("Something went wrong!");
                        clip = ClipData.newPlainText("label", mTg);
                        mClipboardManager.setPrimaryClip(clip);
                        displayToast("URL was copied to clipBoard, you can check url!");
                        Log.e("intentError", er.toString());
                    }

                }

            });

        }else {
            tgButton.setVisibility(View.GONE);
        }
        if (mIg.length()>0){
            igButton.setVisibility(View.VISIBLE);
            igButton.setOnClickListener(v->{
                Intent igIntent = new Intent(Intent.ACTION_VIEW);
                igIntent.setData(Uri.parse("https://instagram.com/"+mIg));
                try {
                    startActivity(igIntent);
                }catch (Exception e){
                    displayToast("Something went wrong!");
                    clip = ClipData.newPlainText("label", mIg);
                    mClipboardManager.setPrimaryClip(clip);
                    displayToast("URL was copied to clipBoard, you can check url!");
                    Log.e("intentError", e.toString());
                }

            });
        }else {
            igButton.setVisibility(View.GONE);
        }
        if (mCall.length()>0){
            callButton.setVisibility(View.VISIBLE);
            callButton.setOnClickListener(v->{
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mCall));
                startActivity(intent);
            });
        }else {
            callButton.setVisibility(View.GONE);
        }
        if (mCard.length()>0){
            cardButton.setVisibility(View.VISIBLE);
            cardButton.setOnClickListener(v->{
                displayToast("Номер карты скопирован в буфер обмена");
                clip = ClipData.newPlainText("label", mCard);
                mClipboardManager.setPrimaryClip(clip);
            });
        }else {
            cardButton.setVisibility(View.GONE);
        }
    }

    public void count(int year, int month, int day, TextView textView){

        // задаём дату, до которой нужно считать время
        Calendar countdownDate = Calendar.getInstance();
        countdownDate.set(year, --month, day, 0, 0, 0); // 1 июня 2023 года

        // вычисляем количество миллисекунд до заданной даты
        long millisLeft = countdownDate.getTimeInMillis() - System.currentTimeMillis();

        if (countDownTimer!=null){
            countDownTimer = null;
        }
        // создаём объект класса CountDownTimer
        countDownTimer = new CountDownTimer(millisLeft, 1000) {
            public void onTick(long millisUntilFinished) {
                // обновляем счётчик на экране
                long days = millisUntilFinished/86400000;
                if (days>=365){
                    textView.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    textView.setTextColor(Color.RED);
                    textView.setText("Happy Birthday!");
                }
                else {
                    textView.setText(getCountdownText(millisUntilFinished));
                }



            }

            public void onFinish() {
                // действия, которые нужно выполнить после окончания обратного отсчёта
            }
        };
        countDownTimer.start();
    }
    @SuppressLint("DefaultLocale")
    public String getCountdownText(long timeInMillis) {
        long seconds = (timeInMillis / 1000) % 60;
        long minutes = (timeInMillis / (1000 * 60)) % 60;
        long hours = (timeInMillis / (1000 * 60 * 60)) % 24;
        long days = (timeInMillis / (1000 * 60 * 60 * 24));

        return String.format("%02dd:%02dh:%02dm:%02ds", days, hours, minutes, seconds);
    }
    public void displayToast(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public void getData(String mId, boolean isNew){


        database = FirebaseDatabase.getInstance("https://my-friends-91830-default-rtdb.europe-west1.firebasedatabase.app/");
        dbRef = database.getReference("DATA/"+acId);
        Query query = dbRef.orderByChild("id").equalTo(mId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()){
                    DataModel data = childSnapshot.getValue(DataModel.class);
                    assert data != null;
                    strName = data.getName();
                    photoUrl = data.getPhotoURL();
                    tg = data.getTgURL();
                    ig = data.getIgURL();
                    call = data.getCallNumb();
                    card = data.getCardNumb();
                    birthDate = data.getBirthDate();
                    decPhoto = "";

                    try {
                        if (photoUrl.length()>0){
                            decPhoto = EncryptionUtils.decrypt(photoUrl, UID);
                        }

                        decName = EncryptionUtils.decrypt(strName, UID);
                        decTg = EncryptionUtils.decrypt(tg, UID);
                        decIg = EncryptionUtils.decrypt(ig, UID);
                        decCall = EncryptionUtils.decrypt(call, UID);
                        decCard = EncryptionUtils.decrypt(card, UID);
                        decBirth = EncryptionUtils.decrypt(birthDate, UID);

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    makeBirthDate(decBirth, isNew);
                    updateUI(decName, decPhoto, decTg, decIg, decCall, decCard);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void makeBirthDate(String date, boolean isNew){
        String strYear = "0000";
        String strMonth = "01";
        String strDay = "01";
        try {
            strYear = date.substring(0, 4);
            strMonth = date.substring(5,7);
            strDay = date.substring(8,10);
        }catch (Exception e){
            Log.e("RECADAPTER", e.toString());
        }


        year = Integer.parseInt(strYear);
        int mYear = LocalDateTime.now().getYear();
        month = Integer.parseInt(strMonth);
        day = Integer.parseInt(strDay);

        if (checkDate(mYear, month, day)){
            mYear++;
        }
        if (isNew) count(mYear, month, day, counterView);
        else{
            if (countDownTimer!=null){
                countDownTimer.cancel();
                countDownTimer = null;
                count(mYear, month, day, counterView);
            }
        }

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

    public boolean checkDate(int year, int month, int day){
        LocalDateTime.now();

        LocalDateTime date1 = LocalDateTime.of(year, month, day, 0, 0);

        Duration duration = Duration.between(Instant.now(), date1.atZone(ZoneId.systemDefault()).toInstant());
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        return days < 0 || hours < 0 || minutes < 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {

        getData(id, false);
        super.onResume();
    }
}