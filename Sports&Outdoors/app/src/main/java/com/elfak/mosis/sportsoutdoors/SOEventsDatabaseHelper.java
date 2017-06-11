package com.elfak.mosis.sportsoutdoors;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SOEventsDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_CREATE = "create table " + SOEventsDBAdapter.DATABASE_TABLE + " ("
            + SOEventsDBAdapter.EVENT_DB_ID + " integer primary key autoincrement, "
    + SOEventsDBAdapter.EVENT_SERVER_ID + " text, "
    + SOEventsDBAdapter.EVENT_NAME + " text, "
    + SOEventsDBAdapter.EVENT_DESCRIPTION + " text, "
    + SOEventsDBAdapter.EVENT_TYPE + " text, "
    + SOEventsDBAdapter.EVENT_NUMBER_OF_PLAYERS + " text, "
    + SOEventsDBAdapter.LONGITUDE + " text, "
    + SOEventsDBAdapter.LATITUDE + " text, "
    + SOEventsDBAdapter.DATETIME + " text, "
    + SOEventsDBAdapter.USER_ID + " text, "
    + SOEventsDBAdapter.FINISHED + " text);";

    public SOEventsDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            db.execSQL(DATABASE_CREATE);
        }catch(SQLiteException e){
            Log.v("SOEventsDatabaseHelper", e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + SOEventsDBAdapter.DATABASE_TABLE);
        onCreate(db);
    }
}
