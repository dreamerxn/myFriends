package com.dreamclub.myfriends.Adapters;

import static androidx.core.content.ContextCompat.getDrawable;
import static androidx.core.content.ContextCompat.getSystemService;
import static androidx.core.content.ContextCompat.startActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dreamclub.myfriends.AddFriend;
import com.dreamclub.myfriends.Crypt.EncryptionUtils;
import com.dreamclub.myfriends.Data.DataModel;
import com.dreamclub.myfriends.MainActivity;
import com.dreamclub.myfriends.R;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.sql.Time;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MyRecAdapter extends RecyclerView.Adapter<MyRecAdapter.MyViewClass>{

    Context mContext;
    List<DataModel> mDataModel;
    LayoutInflater layoutInflater;
    String UID;
    String acId;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    Timer timer;
    ClipboardManager mClipboardManager;
    ClipData clip;
    CountDownTimer countDownTimer;

    EncryptionUtils encryptionUtils;


    public MyRecAdapter(Context context, List<DataModel> dataModel, ClipboardManager clipboardManager){
        this.mContext = context;
        this.mDataModel = dataModel;
        this.layoutInflater = LayoutInflater.from(context);
        this.mClipboardManager = clipboardManager;
        getUser();

    }

    public class MyViewClass extends RecyclerView.ViewHolder{

        ImageView photoView;
        Button tgButton, igButton, callButton, cardButton;
        TextView name, counterView;
        LinearLayout linear;

        public MyViewClass(@NonNull View itemView) {
            super(itemView);

            photoView = itemView.findViewById(R.id.photo);
            tgButton = itemView.findViewById(R.id.tgButton);
            igButton = itemView.findViewById(R.id.igButton);
            callButton = itemView.findViewById(R.id.callButton);
            cardButton = itemView.findViewById(R.id.cardButton);
            name = itemView.findViewById(R.id.name);
            counterView = itemView.findViewById(R.id.counter);
            linear = itemView.findViewById(R.id.parent_of_list);

        }

    }

    @NonNull
    @Override
    public MyViewClass onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.list_item, parent, false);
        return new MyViewClass(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewClass holder, int position) {

        String ph = mDataModel.get(position).getPhotoURL();
        String nm = mDataModel.get(position).getName();
        String tg = mDataModel.get(position).getTgURL();
        String ig = mDataModel.get(position).getIgURL();
        String cl = mDataModel.get(position).getCallNumb();
        String cr = mDataModel.get(position).getCardNumb();
        String bd = mDataModel.get(position).getBirthDate();
        String decPhoto = "", decName = "", decTgUrl = "", decIgUrl = "", decCallNumb = "", decCardNumb = "", decBirth = "";
        try {
            if (ph.length()>0) decPhoto = EncryptionUtils.decrypt(ph, UID);

            if (nm.length()>0) decName = EncryptionUtils.decrypt(nm, UID);
            if (tg.length()>0) decTgUrl = EncryptionUtils.decrypt(tg, UID);
            if (ig.length()>0) decIgUrl = EncryptionUtils.decrypt(ig, UID);
            if (cl.length()>0) decCallNumb = EncryptionUtils.decrypt(cl, UID);
            if (cr.length()>0) decCardNumb = EncryptionUtils.decrypt(cr, UID);
            if (bd.length()>0) decBirth = EncryptionUtils.decrypt(bd, UID);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String birthDate = decBirth;
        String photoURL = decPhoto;
        String name = decName;
        String tgUrl = decTgUrl;
        String igUrl = decIgUrl;
        String callNumb = decCallNumb;
        String cardNumb = decCardNumb;

        Glide.with(mContext)
                .load(photoURL)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(holder.photoView);


        holder.name.setText(name);
        if (tgUrl.length()>0){
            holder.tgButton.setVisibility(View.VISIBLE);
            holder.tgButton.setOnClickListener(v->{
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("tg:resolve?domain="+tgUrl));
                try {
                    mContext.startActivity(intent);
                }catch (Exception e){
                    try {
                        intent.setData(Uri.parse("https://t.me/"+tgUrl));
                    }catch (Exception er){
                        displayToast("Something went wrong!");
                        clip = ClipData.newPlainText("label", tgUrl);
                        mClipboardManager.setPrimaryClip(clip);
                        displayToast("URL was copied to clipBoard, you can check url!");
                        Log.e("intentError", er.toString());
                    }

                }

            });

        }else {
            holder.tgButton.setVisibility(View.GONE);
        }
        if (igUrl.length()>0){
            holder.igButton.setVisibility(View.VISIBLE);
            holder.igButton.setOnClickListener(v->{
                Intent igIntent = new Intent(Intent.ACTION_VIEW);
                igIntent.setData(Uri.parse("https://instagram.com/"+igUrl));
                try {
                    mContext.startActivity(igIntent);
                }catch (Exception e){
                    displayToast("Something went wrong!");
                    clip = ClipData.newPlainText("label", igUrl);
                    mClipboardManager.setPrimaryClip(clip);
                    displayToast("URL was copied to clipBoard, you can check url!");
                    Log.e("intentError", e.toString());
                }

            });
        }else {
            holder.igButton.setVisibility(View.GONE);
        }
        if (callNumb.length()>0){
            holder.callButton.setVisibility(View.VISIBLE);
            holder.callButton.setOnClickListener(v->{
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + callNumb));
                mContext.startActivity(intent);
            });
        }else {
            holder.callButton.setVisibility(View.GONE);
        }
        if (cardNumb.length()>0){
            holder.cardButton.setVisibility(View.VISIBLE);
            holder.cardButton.setOnClickListener(v->{
                displayToast("Номер карты скопирован в буфер обмена");
                clip = ClipData.newPlainText("label", cardNumb);
                mClipboardManager.setPrimaryClip(clip);
            });
        }else {
            holder.cardButton.setVisibility(View.GONE);
        }



        String strMonth = "01";
        String strDay = "01";
        try {
            strMonth = birthDate.substring(5,7);
            strDay = birthDate.substring(8,10);
        }catch (Exception e){
            Log.e("RECADAPTER", e.toString());
        }


        int year = LocalDateTime.now().getYear();
        int month = Integer.parseInt(strMonth);
        int day = Integer.parseInt(strDay);

        if (checkDate(year, month, day)){
            year++;
        }
        int mYear = year;


        count(mYear, month, day, holder.counterView, position);

    }

    @Override
    public int getItemCount() {
        return mDataModel.size();
    }


    public void displayToast(String s){
        Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
    }



    public boolean checkDate(int year, int month, int day){
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime date1 = LocalDateTime.of(year, month, day, 0, 0);

        Duration duration = Duration.between(Instant.now(), date1.atZone(ZoneId.systemDefault()).toInstant());
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        return days < 0 || hours < 0 || minutes < 0;
    }

    public void count(int year, int month, int day, TextView textView, int pos){

        // задаём дату, до которой нужно считать время
        Calendar countdownDate = Calendar.getInstance();
        countdownDate.set(year, --month, day, 0, 0, 0); // 1 июня 2023 года

        // вычисляем количество миллисекунд до заданной даты
        long millisLeft = countdownDate.getTimeInMillis() - System.currentTimeMillis();


        // создаём объект класса CountDownTimer
        countDownTimer = new CountDownTimer(millisLeft, 1000) {
            public void onTick(long millisUntilFinished) {
                // обновляем счётчик на экране
                textView.setText(getCountdownText(millisUntilFinished));


            }

            public void onFinish() {
                // действия, которые нужно выполнить после окончания обратного отсчёта
            }
        };

        DataModel dataModel = mDataModel.get(pos);
        dataModel.setCountDownTimer(countDownTimer);
        // запускаем обратный отсчёт
        countDownTimer.start();
    }


    public String getCountdownText(long timeInMillis) {
        long seconds = (timeInMillis / 1000) % 60;
        long minutes = (timeInMillis / (1000 * 60)) % 60;
        long hours = (timeInMillis / (1000 * 60 * 60)) % 24;
        long days = (timeInMillis / (1000 * 60 * 60 * 24));

        return String.format("%02dd:%02dh:%02dm:%02ds", days, hours, minutes, seconds);
    }

    public void getUser(){
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        assert currentUser != null;
        UID = currentUser.getUid();
        acId = currentUser.getEmail();
        assert acId != null;
        acId = delNoDigOrLet(acId);
        encryptionUtils = new EncryptionUtils();
    }

    private static String delNoDigOrLet (String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLetterOrDigit(s.charAt(i)))
                sb.append(s.charAt(i));
        }
        return sb.toString();
    }
}
