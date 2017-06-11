package com.elfak.mosis.sportsoutdoors;

import android.text.Html;

import java.util.ArrayList;


public class DataForView {
    ArrayList<Message> messages;
    ArrayList<Player> players;
    DataForView(){
        messages = new ArrayList<Message>();
        players = new ArrayList<Player>();
    }
}

class Message{
    public String message;
    public String username;

    Message(String m, String u){
        this.message = m;
        this.username = u;
    }

    @Override
    public String toString(){
        if(this.username.equals(UserData.getInstance().getUser().getUsername())){
        return "Me: " +  this.message ;}
        else {return this.username + ": " +  this.message;}
    }
}

class Player{
    public long userId;
    public String username;
    public int score;

    Player(long uId, String u){
        this.userId = uId;
        this.username = u;
    }

    @Override
    public String toString(){
        return username;
    }
}
