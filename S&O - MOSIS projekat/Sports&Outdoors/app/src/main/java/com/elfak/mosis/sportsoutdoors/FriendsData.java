package com.elfak.mosis.sportsoutdoors;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.util.ArrayList;
import java.util.Hashtable;


public class FriendsData{
    private ArrayList<User> friends;
    private Hashtable<Long, Bitmap> profile_pics;

    private FriendsData(){
        friends = new ArrayList<User>();
        profile_pics = new Hashtable<Long, Bitmap>();

    }



    private static class SingletonHolder{
        public static final FriendsData instance = new FriendsData();
    }

    public static FriendsData getInstance(){
        return SingletonHolder.instance;
    }



    public void setInstance(ArrayList<User> friends){
        this.friends = friends;

        for(int i =0; i<friends.size(); i++){
            byte[] decodedString = Base64.decode(friends.get(i).getUserphoto(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            if(decodedByte != null) {
                profile_pics.put(friends.get(i).getId(), decodedByte);
            }
        }
    }

    public ArrayList<User> getFriends(){
        return friends;
    }

    public void addFriend(User user){
        friends.add(user);
    }

    public User getFriend(int index){
        return friends.get(index);
    }

    public void deleteFriend(int index){
        friends.remove(index);
    }

    public Bitmap getUserphoto(long id){
        return this.profile_pics.get(id);
    }
}
