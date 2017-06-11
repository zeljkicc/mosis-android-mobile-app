package com.elfak.mosis.sportsoutdoors;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;

public class SOEventsDBAdapter {

    public static final String DATABASE_NAME = "SODb";
    public static final String DATABASE_TABLE = "SOEvents";
    public static final int DATABASE_VERSION = 1;

    public static final String EVENT_SERVER_ID = "EVENT_SERVER_ID";
    public static final String EVENT_NAME = "EVENT_NAME";
    public static final String EVENT_DESCRIPTION = "EVENT_DESCRIPTION" ;
    public static final String EVENT_TYPE = "EVENT_TYPE";
    public static final String EVENT_NUMBER_OF_PLAYERS = "EVENT_NUMBER_OF_PLAYERS";
    public static final String LONGITUDE = "EVENT_LONGITUDE";
    public static final String LATITUDE = "EVENT_LATITUDE";
    public static final String DATETIME = "EVENT_DATETIME";
    public static final String USER_ID = "EVENT_USER_ID";
    public static final String FINISHED = "EVENT_FINISHED";
    public static final String EVENT_DB_ID = "EVENT_DB_ID";

    private SQLiteDatabase db;

    private final Context context;
    private SOEventsDatabaseHelper dbHelper;

    public  SOEventsDBAdapter(Context cont){
        context = cont;
        dbHelper = new SOEventsDatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public SOEventsDBAdapter open() throws SQLException {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        db.close();
    }

    public long insertEntry(SOEvent event){
        ContentValues contentValues = new ContentValues();

        contentValues.put(EVENT_SERVER_ID, String.valueOf(event.getId()));
        contentValues.put(EVENT_NAME, event.getName());
        contentValues.put(EVENT_DESCRIPTION, event.getDescription());
        contentValues.put(EVENT_TYPE, event.getType());
        contentValues.put(EVENT_NUMBER_OF_PLAYERS, String.valueOf(event.getNumberOfPlayers()));
        contentValues.put(LONGITUDE, event.getLongitude());
        contentValues.put(LATITUDE, event.getLatitude());
        contentValues.put(DATETIME, event.getDatetime());
        contentValues.put(USER_ID, String.valueOf(event.getUserId()));
        contentValues.put(FINISHED, String.valueOf(event.getFinished()));

        long id = -1;
        db.beginTransaction();
        try{
            id=db.insert(DATABASE_TABLE, null, contentValues);
            db.setTransactionSuccessful();
        }catch (SQLiteException e){
            Log.v("MyPlacesDBAdapter", e.getMessage());
        }finally {
            db.endTransaction();
        }
        return id;
    }

    public boolean removeEntry(long id){
        boolean success = false;
        db.beginTransaction();
        try{
            success = db.delete(DATABASE_TABLE, EVENT_DB_ID+"=" + id, null) > 0;
            db.setTransactionSuccessful();
        }catch (SQLiteException e){
            Log.v("MyPlacesDBAdapter", e.getMessage());
        }finally {
            db.endTransaction();
        }
        return success;
    }

    public ArrayList<SOEvent> getAllEntries(){
        ArrayList<SOEvent> events = null;
        Cursor cursor = null;
        db.beginTransaction();
        try{
            cursor = db.query(DATABASE_TABLE,null,null,null,null,null,null);
            db.setTransactionSuccessful();
        }catch(SQLiteException e){
            Log.v("MyPlacesDBAdapter", e.getMessage());
        }finally {
            db.endTransaction();
        }
        if(cursor!=null){
            events = new ArrayList<SOEvent>();
            SOEvent event = null;
            while(cursor.moveToNext()){
                event = new SOEvent();
                event.setId(Long.valueOf(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.EVENT_SERVER_ID)))));
                event.setLatitude(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.LATITUDE))));
                event.setLongitude(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.LONGITUDE))));
                event.setName(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.EVENT_NAME))));
                event.setType(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.EVENT_TYPE))));
                event.setNumberOfPlayers(Integer.valueOf(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.EVENT_NUMBER_OF_PLAYERS)))));
                event.setUserId(Long.valueOf(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.USER_ID)))));
                event.setDescription(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.EVENT_DESCRIPTION))));
                event.setDatetime(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.DATETIME))));
                event.setFinished(Boolean.valueOf(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.FINISHED)))));
                event.setID(Long.valueOf(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.EVENT_SERVER_ID)))));

                events.add(event);
            }

        }
        return events;
    }

    public SOEvent getEntry(long id){
        SOEvent event = null;
        Cursor cursor = null;
        db.beginTransaction();
        try{
            cursor = db.query(DATABASE_TABLE,null,EVENT_DB_ID+"=" + id,null,null,null,null);
            db.setTransactionSuccessful();
        }catch(SQLiteException e){
            Log.v("MyPlacesDBAdapter", e.getMessage());
        }finally {
            db.endTransaction();
        }
        if(cursor!=null){
            while(cursor.moveToFirst()){
                event = new SOEvent();
                event.setId(Long.valueOf(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.EVENT_SERVER_ID)))));
                event.setLatitude(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.LATITUDE))));
                event.setLongitude(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.LONGITUDE))));
                event.setName(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.EVENT_NAME))));
                event.setType(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.EVENT_TYPE))));
                event.setNumberOfPlayers(Integer.valueOf(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.EVENT_NUMBER_OF_PLAYERS)))));
                event.setUserId(Long.valueOf(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.USER_ID)))));
                event.setDescription(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.EVENT_DESCRIPTION))));
                event.setDatetime(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.DATETIME))));
                event.setFinished(Boolean.valueOf(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.FINISHED)))));
                event.setID(Long.valueOf(cursor.getString((cursor.getColumnIndex(SOEventsDBAdapter.EVENT_SERVER_ID)))));
            }

        }
        return event;
    }

    public int updateEntry(long id, SOEvent event){
        String where = EVENT_DB_ID + "=" + id;

        ContentValues contentValues = new ContentValues();

        contentValues.put(EVENT_SERVER_ID, String.valueOf(event.getId()));
        contentValues.put(EVENT_NAME, event.getName());
        contentValues.put(EVENT_DESCRIPTION, event.getDescription());
        contentValues.put(EVENT_TYPE, event.getType());
        contentValues.put(EVENT_NUMBER_OF_PLAYERS, String.valueOf(event.getNumberOfPlayers()));
        contentValues.put(LONGITUDE, event.getLongitude());
        contentValues.put(LATITUDE, event.getLatitude());
        contentValues.put(DATETIME, event.getDatetime());
        contentValues.put(USER_ID, String.valueOf(event.getUserId()));
        contentValues.put(FINISHED, String.valueOf(event.getFinished()));

        return db.update(DATABASE_TABLE, contentValues, where, null);

    }
}
