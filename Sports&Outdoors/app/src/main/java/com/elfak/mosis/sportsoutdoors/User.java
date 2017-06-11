package com.elfak.mosis.sportsoutdoors;


public class User {
    private long id;
    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private String phonenumber;
    private String userphoto;

    private String lat;
    private String lon;

    public User(){

    }

    public User(long id, String u, String p, String f, String l, String pn, String up){
        this.id = id;
        this.username = u;
        this.password = p;
        this.firstname = f;
        this.lastname = l;
        this.phonenumber = pn;
        this.userphoto = up;
    }

    public void setId(long id){
        this.id = id;
    }

    public long getId(){
        return this.id;
    }

    public void setUsername(String u){
        this.username = u;
    }

    public void setPassword(String p){
        this.password = p;
    }

    public void setFirstname(String f){
        this.firstname = f;
    }

    public void setLastname(String l){
        this.lastname = l;
    }

    public void setPhonenumber(String pn){
        this.phonenumber = pn;
    }

    public void setUserphoto(String up){
        this.userphoto = up;
    }

    public String getUsername(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }

    public String getFirstname(){
        return this.firstname;
    }

    public String getLastname(){
        return this.lastname;
    }

    public String getPhonenumber(){
        return this.phonenumber;
    }

    public String getUserphoto(){
        return this.userphoto;
    }

    @Override
    public String toString(){
        return this.username;
    }

    public void setLatitude(String lat){
        this.lat = lat;
    }

    public String getLatitude(){
        return this.lat;
    }

    public void setLongitude(String lon){
        this.lon = lon;
    }

    public String getLongitude(){
        return this.lon;
    }
}
