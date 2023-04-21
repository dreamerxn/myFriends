package com.dreamclub.myfriends.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dreamclub.myfriends.Crypt.EncryptionUtils;
import com.dreamclub.myfriends.Data.IdeaDataModel;
import com.dreamclub.myfriends.R;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class IdeaRecAdapter extends RecyclerView.Adapter<IdeaRecAdapter.MyViewClass> {

    String UID;
    String acId;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    EncryptionUtils encryptionUtils;

    Context mContext;
    List<IdeaDataModel> mData;
    LayoutInflater layoutInflater;
    public IdeaRecAdapter(List<IdeaDataModel> data, Context context){
        this.mData = data;
        this.mContext = context;
        this.layoutInflater = LayoutInflater.from(context);
        getUser();
    }

    @NonNull
    @Override
    public MyViewClass onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.idea_item_list, parent, false);

        return new MyViewClass(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewClass holder, int position) {
        String textIdea = mData.get(position).getText();
        String dateIdea = mData.get(position).getDate();
        holder.ideaText.setText(textIdea);
        holder.ideaDate.setText(dateIdea);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewClass extends RecyclerView.ViewHolder{

        TextView ideaText, ideaDate;
        public MyViewClass(@NonNull View itemView){
            super(itemView);
            ideaText = itemView.findViewById(R.id.ideaText);
            ideaDate = itemView.findViewById(R.id.ideaDate);
        }
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
