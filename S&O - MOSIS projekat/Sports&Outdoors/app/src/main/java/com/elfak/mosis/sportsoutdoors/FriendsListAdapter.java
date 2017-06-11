package com.elfak.mosis.sportsoutdoors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.ViewHolder>{
    private ArrayList<User> mDataset;

    Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextViewName, mTextViewUsername;
        public ImageView mImageView;
        public ViewHolder(View v) {
            super(v);
            mTextViewName = (TextView) v.findViewById(R.id.friend_name);
            mTextViewUsername= (TextView) v.findViewById(R.id.friend_username);
            mImageView = (ImageView) v.findViewById(R.id.friend_profile_picture);


        }
    }

    public FriendsListAdapter(Context context, ArrayList<User> myDataset) {
        mDataset = myDataset;
        mContext = context;
    }

    @Override
    public FriendsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friends_list_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(FriendsListAdapter.ViewHolder holder, int position) {

        User user = mDataset.get(position);
        holder.mTextViewName.setText(user.getFirstname() + " " + user.getLastname());
        holder.mTextViewUsername.setText(user.getUsername());

        byte[] decodedString = Base64.decode(mDataset.get(position).getUserphoto(), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        holder.mImageView.setImageBitmap(decodedByte);

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }



}

