package com.elfak.mosis.sportsoutdoors;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotifyService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    ScheduledExecutorService thread;
    long userId;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    Location myLocation = null;

    User myUser;

    public NotifyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        thread = Executors.newSingleThreadScheduledExecutor();

        userId=UserData.getInstance().getUser().getId();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5 * 1000)
                .setFastestInterval(1 * 1000);

        mGoogleApiClient.connect();

        myUser = UserData.getInstance().getUser();
    }

    @Override
    public void onDestroy() {
        thread.shutdown();

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

        super.onDestroy();

    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        thread.scheduleAtFixedRate( new Runnable(){

            public void run() {

                    try{

                            HashMap<String, Object> values = AppHTTPHelper.getServiceEvents(myUser.getId(), String.valueOf(myLocation.getLatitude()), String.valueOf(myLocation.getLongitude()));

                            ArrayList<User> users = (ArrayList<User>)values.get("users");
                            ArrayList<SOEvent> events = (ArrayList<SOEvent>)values.get("events");


                            int numUsers = users.size();
                            int numEvents = events.size();


                           if(numUsers != 0 || numEvents !=0) {

                               String msg = "There are events in your area";

                               if(numUsers == 0 && numEvents == 1)
                                   msg = "There is an event in your area. Check it out :)";


                               showNotification(msg, users, events);
                            }




                     //   }
                    } catch(Exception e) { e.printStackTrace(); }

            }//run
        }, 10, 15, TimeUnit.SECONDS);
    }


    int notificationId= 1;
    NotificationManager notificationManager;
    Notification myNotification;

    public void showNotification(String text, ArrayList<User> users, ArrayList<SOEvent> events){
        notificationManager= (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher1)
                        .setContentTitle("New S&O message").setOnlyAlertOnce(true)
                        .setContentText(text).setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS
                        | Notification.DEFAULT_VIBRATE);

        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);



        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        inboxStyle.setBigContentTitle("List of events in your area:");

        int eventsSize = events.size();
        int usersSize = users.size();
        for (int i=0; i < eventsSize; i++) {

            inboxStyle.addLine(i + 1 + ". Event " + events.get(i).getName());
        }

        mBuilder.setStyle(inboxStyle);



if(events.size() > 0)
        mNotificationManager.notify(notificationId, mBuilder.build());
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            return;
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        handleNewLocation(location);


    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    private void handleNewLocation(Location location) {
        myLocation = location;
    }
}
