package com.dreamclub.myfriends.Data;

import android.os.CountDownTimer;

public class DataModel {
    String photoURL;
    String name;
    String tgURL;
    String igURL;
    String callNumb;
    String cardNumb;
    String birthDate;
    CountDownTimer countDownTimer;
    // ...

    public CountDownTimer getCountDownTimer() {
        return countDownTimer;
    }

    public void setCountDownTimer(CountDownTimer countDownTimer) {
        this.countDownTimer = countDownTimer;
    }
    public DataModel(){}
    public DataModel(String photoURL, String name, String tgURL, String igURL, String callNumb, String cardNUMB, String birthDate){
        this.photoURL = photoURL;
        this.name = name;
        this.tgURL = tgURL;
        this.igURL = igURL;
        this.callNumb = callNumb;
        this.cardNumb = cardNUMB;
        this.birthDate = birthDate;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTgURL() {
        return tgURL;
    }

    public void setTgURL(String tgURL) {
        this.tgURL = tgURL;
    }

    public String getIgURL() {
        return igURL;
    }

    public void setIgURL(String igURL) {
        this.igURL = igURL;
    }

    public String getCallNumb() {
        return callNumb;
    }

    public void setCallNumb(String callNumb) {
        this.callNumb = callNumb;
    }

    public String getCardNumb() {
        return cardNumb;
    }

    public void setCardNumb(String cardNumb) {
        this.cardNumb = cardNumb;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
}
