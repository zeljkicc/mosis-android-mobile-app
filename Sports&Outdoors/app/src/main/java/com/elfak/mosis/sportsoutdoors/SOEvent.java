package com.elfak.mosis.sportsoutdoors;


public class SOEvent {
    private long id;
    private String name;
    private String description;
    private String type;
    private int number_of_players;
    String longitude;
    String latitude;
    String datetime;
    long user_id;
    boolean finished;

    long mID;

    public SOEvent(){

    }

    public SOEvent(long id, String name, String description, String type, int number_of_players, String latitude, String longitude, String datetime, long user_id){
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.number_of_players = number_of_players;
        this.latitude = latitude;
        this.longitude = longitude;
        this.datetime = datetime;
        this.user_id = user_id;
        finished = false;
    }

    public long getId(){
        return this.id;
    }

    public void setId(long id){
        this.id = id;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getDescription(){
        return this.description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    @Override
    public String toString(){
        return this.name;
    }

    public String getLongitude(){
        return this.longitude;
    }

    public void setLongitude(String longitude){
        this.longitude = longitude;
    }

    public String getLatitude(){
        return this.latitude;
    }

    public void setLatitude(String latitude){
        this.latitude = latitude;
    }

    public String getType(){
        return this.type;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getDatetime(){
        return this.datetime;
    }

    public void setDatetime(String type){
        this.datetime = datetime;
    }

    public long getUserId(){
        return this.user_id;
    }

    public void setUserId(long user_id){
        this.user_id = user_id;
    }

    public int getNumberOfPlayers(){
        return this.number_of_players;
    }

    public void setNumberOfPlayers(int number_of_players){
        this.number_of_players = number_of_players;
    }


    public boolean containsQuery(String query){
        if(this.name.toLowerCase().contains(query.toLowerCase()) || this.description.toLowerCase().contains(query.toLowerCase()))
            return true;
        else return false;
    }

    public void setFinished(boolean finished) {

        this.finished = finished;
    }

    public boolean getFinished() {
        return finished;
    }


    public long getmID() {
        return mID;
    }

    public void setID(long mID) {
        this.mID = mID;
    }

}
