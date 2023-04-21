package com.dreamclub.myfriends.Adapters;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dreamclub.myfriends.Crypt.EncryptionUtils;
import com.dreamclub.myfriends.Data.DataModel;
import com.dreamclub.myfriends.R;
import com.dreamclub.myfriends.Screens.ViewInfo;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import java.time.Duration;
import java.time.Instant;

import java.time.LocalDateTime;
import java.time.ZoneId;

import java.util.Calendar;

import java.util.List;



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
    ClipboardManager mClipboardManager;
    ClipData clip;
    CountDownTimer countDownTimer;

    EncryptionUtils encryptionUtils;

    boolean isLinear;

    public MyRecAdapter(Context context, List<DataModel> dataModel, ClipboardManager clipboardManager){
        this.mContext = context;
        this.mDataModel = dataModel;
        this.layoutInflater = LayoutInflater.from(context);
        this.mClipboardManager = clipboardManager;
        getUser();

    }

    public void setLinear(boolean isLinear){
        this.isLinear = isLinear;
    }
    public static class MyViewClass extends RecyclerView.ViewHolder{

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
        View view;
        if(isLinear){
            view = layoutInflater.inflate(R.layout.linear_list_item, parent, false);
        }
        else {
            view = layoutInflater.inflate(R.layout.list_item, parent, false);
        }

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
        String birthDate = mDataModel.get(position).getBirthDate();


        holder.name.setText(nm);
        if (ph.length()>0){
            Glide.with(mContext)
                    .load(ph)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(holder.photoView);
        }else {
            if (!isLinear){
                holder.photoView.setVisibility(View.GONE);
            }

        }

        if (tg.length()>0){
            holder.tgButton.setVisibility(View.VISIBLE);
            holder.tgButton.setOnClickListener(v->{
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("tg:resolve?domain="+ tg));
                try {
                    mContext.startActivity(intent);
                }catch (Exception e){
                    try {
                        intent.setData(Uri.parse("https://t.me/"+ tg));
                    }catch (Exception er){
                        displayToast("Something went wrong!");
                        clip = ClipData.newPlainText("label", tg);
                        mClipboardManager.setPrimaryClip(clip);
                        displayToast("URL was copied to clipBoard, you can check url!");
                        Log.e("intentError", er.toString());
                    }

                }

            });

        }else {
            holder.tgButton.setVisibility(View.GONE);
        }
        if (ig.length()>0){
            holder.igButton.setVisibility(View.VISIBLE);
            holder.igButton.setOnClickListener(v->{
                Intent igIntent = new Intent(Intent.ACTION_VIEW);
                igIntent.setData(Uri.parse("https://instagram.com/"+ ig));
                try {
                    mContext.startActivity(igIntent);
                }catch (Exception e){
                    displayToast("Something went wrong!");
                    clip = ClipData.newPlainText("label", ig);
                    mClipboardManager.setPrimaryClip(clip);
                    displayToast("URL was copied to clipBoard, you can check url!");
                    Log.e("intentError", e.toString());
                }

            });
        }else {
            holder.igButton.setVisibility(View.GONE);
        }
        if (cl.length()>0){
            holder.callButton.setVisibility(View.VISIBLE);
            holder.callButton.setOnClickListener(v->{
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + cl));
                mContext.startActivity(intent);
            });
        }else {
            holder.callButton.setVisibility(View.GONE);
        }
        if (cr.length()>0){
            holder.cardButton.setVisibility(View.VISIBLE);
            holder.cardButton.setOnClickListener(v->{
                displayToast("Номер карты скопирован в буфер обмена");
                clip = ClipData.newPlainText("label", cr);
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


        holder.linear.setOnClickListener(v->{
            Intent intent = new Intent(mContext, ViewInfo.class);
            intent.putExtra("photo", ph);
            intent.putExtra("name", nm);
            intent.putExtra("tg", tg);
            intent.putExtra("ig", ig);
            intent.putExtra("call", cl);
            intent.putExtra("card", cr);
            intent.putExtra("year", mYear);
            intent.putExtra("month", month);
            intent.putExtra("day", day);
            intent.putExtra("id", mDataModel.get(position).getId());
            mContext.startActivity(intent);
        });
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
        LocalDateTime.now();

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
