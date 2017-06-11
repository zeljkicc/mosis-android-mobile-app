package com.elfak.mosis.sportsoutdoors;

import java.util.ArrayList;

public class UserData {

    private User user;


    private UserData() {
        user = new User();
    }

    private static class SingletonHolder{
        public static final UserData instance = new UserData();
    }

    public static UserData getInstance(){
        return SingletonHolder.instance;
    }

    public User getUser(){
        return user;
    }

    public void setUser(User user){
        this.user = user;
    }

}
