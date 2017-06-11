package com.elfak.mosis.sportsoutdoors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;


public class GradePlayersAdapter extends RecyclerView.Adapter<GradePlayersAdapter.ViewHolder>{

    private ArrayList<User> mDataset;

    private static ArrayList<Spinner> spinners;

    Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextViewName, mTextViewUsername;
        public ImageView mImageView;
        public Spinner spinner;
        public ViewHolder(View v) {
            super(v);
            mTextViewName = (TextView) v.findViewById(R.id.friend_name);
            mTextViewUsername= (TextView) v.findViewById(R.id.friend_username);
            mImageView = (ImageView) v.findViewById(R.id.friend_profile_picture);
            spinner = (Spinner) v.findViewById(R.id.spinner_grade);

            spinners.add(spinner);

        }
    }

    public GradePlayersAdapter(Context context, ArrayList<User> myDataset) {
        mDataset = myDataset;
        mContext = context;

        spinners = new ArrayList<>();
    }

    @Override
    public GradePlayersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grade_cardview, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(GradePlayersAdapter.ViewHolder holder, int position) {

        User user = mDataset.get(position);
        holder.mTextViewName.setText(user.getFirstname() + " " + user.getLastname());
        holder.mTextViewUsername.setText(user.getUsername());

        byte[] decodedString = Base64.decode(mDataset.get(position).getUserphoto(), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        holder.mImageView.setImageBitmap(decodedByte);



        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                R.array.grades_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinner.setAdapter(adapter);

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public Long getId(int index){

        return mDataset.get(index).getId();
    }

    public int getScore(int index){

        return (spinners.get(index).getSelectedItemPosition() + 1);
    }

}
