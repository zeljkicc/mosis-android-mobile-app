package com.elfak.mosis.sportsoutdoors;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        NavigationView.OnNavigationItemSelectedListener,
        LocationListener, GoogleMap.OnMyLocationButtonClickListener, OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {


    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1001;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 1002;
    private static final int REQUEST_ENABLE_BT = 1003;
    private String mConnectedDeviceName = null;
    private StringBuffer mOutStringBuffer;//???
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mChatService = null;
    private boolean bluetoothEnabled = false;

    public static final String TAG = MainActivity.class.getSimpleName();

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;


    public static boolean server = false;

    private static final int USER_LOGIN = 1;

    Handler guiThread;
    ProgressDialog progressDialog;
    Context context;

    Intent intentNotifyService;
    ComponentName service;
    boolean serviceEnabled = false;
    BroadcastReceiver receiver;

    ScheduledExecutorService scheduledExecutorServiceFriends, scheduledExecutorServiceEvents;

    ScheduledExecutorService friendsTransThread;

    Marker myMarker = null;
    Location myLocation = null;
    String lon;
    String lat;
    int radius = 1;

    Stack<Marker> markersForEvents;

    Hashtable<Long, LatLng> friendsLocations = new Hashtable<Long, LatLng>();

    Hashtable<Long, Marker> friendsMarkers = new Hashtable<Long, Marker>();

    private View mCustomMarkerView;
    private ImageView mMarkerImageView;

    Future<?> futureFriends, futureEvents;


    boolean showFriends = false;
    Boolean userSigned = false;

    final static int ADD_NEW_EVENT = 8;

    Circle myCircle = null;

    NavigationView mNavigationView;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        setUpMapIfNeeded();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5 * 1000)
                .setFastestInterval(1 * 1000);


        mCustomMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
        mMarkerImageView = (ImageView) mCustomMarkerView.findViewById(R.id.profile_image);

        guiThread = new Handler();
        progressDialog = new ProgressDialog(this);
        context = this;

        friendsTransThread = Executors.newSingleThreadScheduledExecutor();

        markersForEvents = new Stack<Marker>();

        scheduledExecutorServiceFriends = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorServiceEvents = Executors.newSingleThreadScheduledExecutor();

        FloatingActionButton fabCenter = (FloatingActionButton) findViewById(R.id.fabCenter);
        fabCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
                        .zoom(14)
                        .bearing(90)
                        .tilt(40)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        FloatingActionButton fabRadius = (FloatingActionButton) findViewById(R.id.fabRadius);
        fabRadius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowDialogRadius();
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();

        }

        String username = "1";
        String password = "1";

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);

        if (UserData.getInstance().getUser().getUsername() == null) {

            Intent loginActivity = new Intent(this, LoginActivity.class);

            loginActivity.putExtra("lat", lat);
            loginActivity.putExtra("lon", lon);
            startActivityForResult(loginActivity, USER_LOGIN);
        }
        else{
            setDataAfterLogin();
        }


        final android.support.v7.widget.SwitchCompat friendsSwitch = (android.support.v7.widget.SwitchCompat) mNavigationView.getMenu().findItem(R.id.switches).getSubMenu().findItem(R.id.item_switch_friends).getActionView().findViewById(R.id.friends_switch);
        friendsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !showFriends) {
                    showFriends = true;

                    addFriendsMarkers();

                } else if (!isChecked && showFriends) {
                    showFriends = false;
                    removeFriendsMarkers();
                }
            }

        });

        final android.support.v7.widget.SwitchCompat serviceSwitch = (android.support.v7.widget.SwitchCompat) mNavigationView.getMenu().findItem(R.id.switches).getSubMenu().findItem(R.id.item_switch_service).getActionView().findViewById(R.id.service_switch);
        serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!serviceEnabled && isChecked) {

                    intentNotifyService = new Intent(context, NotifyService.class);
                    service = startService(intentNotifyService);

                    serviceEnabled = true;
                } else if (serviceEnabled && !isChecked) {

                    stopService(new Intent(intentNotifyService));

                    serviceEnabled = false;
                }
            }

        });


        final android.support.v7.widget.SwitchCompat bluetoothSwitch = (android.support.v7.widget.SwitchCompat) mNavigationView.getMenu().findItem(R.id.switches).getSubMenu().findItem(R.id.item_switch_bluetooth).getActionView().findViewById(R.id.bluetooth_switch);
        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!bluetoothEnabled && isChecked) {

                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

                    } else if (mChatService == null) {
                        setupChat();
                    }

                    bluetoothEnabled = true;
                } else if (bluetoothEnabled && !isChecked){
                    if (mChatService != null) {
                        mChatService.stop();
                    }

                    if (mBluetoothAdapter.isEnabled()) {

                    }

                }
            }

        });
    }



    public void startMyLocationThread() {

        try {

            futureFriends = scheduledExecutorServiceFriends.scheduleAtFixedRate(new Runnable() {
                public void run() {

                    lon = Double.toString(myLocation.getLongitude());
                    lat = Double.toString(myLocation.getLatitude());

                    friendsLocations = AppHTTPHelper.getFriendsLocations(UserData.getInstance().getUser().getId(), lat, lon);

                    runOnUiThread(new Runnable() {
                        public void run() {

                            if (showFriends)
                                addFriendsMarkers();


                        }
                    });
                }
            }, 0, 3, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        scheduledExecutorServiceFriends.shutdown();
        scheduledExecutorServiceEvents.shutdown();

        friendsTransThread.shutdown();

        if(mChatService != null){
            mChatService.stop();
        }


        ExecutorService thread = Executors.newSingleThreadScheduledExecutor();
        thread.submit(new Runnable() {
            @Override
            public void run() {
                AppHTTPHelper.logOffUser(UserData.getInstance().getUser().getId());
            }
        });
        thread.shutdown();
    }


    @Override
    public void onResume() {
        super.onResume();

        setUpMapIfNeeded();
        mGoogleApiClient.connect();

        if (mChatService != null) {
            if (mChatService.getState() == BluetoothService.STATE_NONE && bluetoothEnabled) {
                mChatService.start();
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected()) {
           LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.custom_infowindow, null);

                TextView title = (TextView) v.findViewById(R.id.titleIO);
                TextView snippet = (TextView) v.findViewById(R.id.snippetIO);

                String mTitle = marker.getTitle();
                if (mTitle != null)
                    title.setText(mTitle);

                String mSnippet = marker.getSnippet();
                if (mSnippet != null)
                    snippet.setText(mSnippet);
                return v;
            }

            @Override
            public View getInfoContents(Marker marker) {


                return null;
            }
        });

        mMap.setOnMyLocationButtonClickListener(this);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            case R.id.discoverable: {
                ensureDiscoverable();
                return true;
            }

            case  R.id.action_search_events:
                Intent searchableIntent = new Intent(this, SearchableActivity.class);
                startActivity(searchableIntent);

                break;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            switch (requestCode) {
                case USER_LOGIN:
                    if (resultCode == Activity.RESULT_OK) {

                     setDataAfterLogin();

                    } else {
                            finish();
                    }

                    break;

                case ADD_NEW_EVENT:
                    if (resultCode == Activity.RESULT_OK) {



                    } else {

                    }

                    break;


                case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
                case REQUEST_CONNECT_DEVICE_INSECURE:
                    if (resultCode == Activity.RESULT_OK) {
                        guiStartProgressDialog("Adding friend", "Adding friend in progress");

                        connectDevice(data, false);
                    }
                    break;
                case REQUEST_ENABLE_BT:
                    if (resultCode == Activity.RESULT_OK) {
                        setupChat();
                    } else {
                        Log.d(TAG, "BT not enabled");
                        Toast.makeText(context, R.string.bt_not_enabled_leaving,
                                Toast.LENGTH_SHORT).show();
                    }


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }




    public void setEvents() {
        if(futureEvents!=null){
            futureEvents.cancel(true);
        }
        futureEvents = scheduledExecutorServiceEvents.scheduleAtFixedRate(new Runnable() {
            public void run() {

                String lon;
                String lat;

                final HashMap<String, Object> hashMapEvents = AppHTTPHelper.getEvents(UserData.getInstance().getUser().getId(), String.valueOf(myLocation.getLatitude()), String.valueOf(myLocation.getLongitude()), radius, SOEventsData.getInstance().getSignedEventsIds());


                final ArrayList<SOEvent> events = (ArrayList<SOEvent>)hashMapEvents.get("events");

                final ArrayList<Long> ids = (ArrayList<Long>)hashMapEvents.get("ids");

                SOEventsData.getInstance().setSoEvents(events);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        while(markersForEvents.size()!=0) {
                            Marker m = markersForEvents.pop();
                            m.remove();
                        }

                        if (events != null) {
                            for (int i = 0; i < events.size(); i++) {
                                SOEvent event = events.get(i);
                                String longitude = event.getLongitude();
                                String latitude = event.getLatitude();
                                LatLng loc = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

                                MarkerOptions markerOptions = new MarkerOptions();

                                markerOptions.position(loc);
                                markerOptions.title(event.getName());
                                markerOptions.snippet(event.getDescription() + "\nStarts at " + event.getDatetime() + "\nFor " + event.getNumberOfPlayers() + " players");
                                switch (event.getType()) {
                                    case "Football":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_soccer));
                                        break;
                                    case "Basketball":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_basketball));
                                        break;
                                    case "Tennis":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_tennis));
                                        break;
                                    case "Volleyball":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_volleyball));
                                        break;
                                    case "Handball":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_handball));
                                        break;
                                    case "Swimming":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_swimming));
                                        break;
                                    case "Table tennis":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_tebletennis));
                                        break;
                                    case "Baseball":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_baseball));
                                        break;
                                    case "Beach volleyball":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_beachvolleyball));
                                        break;
                                    case "Billiard":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_billiard_2));
                                        break;
                                    case "Bobsleigh":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bobsleigh));
                                        break;
                                    case "Bollie":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bollie));
                                        break;
                                    case "Boxing":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_boxing));
                                        break;
                                    case "Climbing":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_climbing));
                                        break;
                                    case "Cricket":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_cricket));
                                        break;
                                    case "Curling":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_curling_2));
                                        break;
                                    case "Cycling":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_cycling));
                                        break;
                                    case "Deep sea fishing":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_deepseafishing));
                                        break;
                                    case "Diving":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_diving));
                                        break;
                                    case "Fishing":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_fishing));
                                        break;
                                    case "Fitness":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_fitness));
                                        break;
                                    case "Golfing":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_golfing));
                                        break;
                                    case "Hanggliding":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_hanggliding));
                                        break;
                                    case "Hiking":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_hiking));
                                        break;
                                    case "Horseriding":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_horseriding));
                                        break;
                                    case "Hunting":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_hunting));
                                        break;
                                    case "Ice hockey":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_icehockey));
                                        break;
                                    case "Ice skating":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_iceskating));
                                        break;
                                    case "Jogging":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_jogging));
                                        break;
                                    case "Judo":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_judo));
                                        break;
                                    case "Karate":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_karate));
                                        break;
                                    case "Kayaking":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_kayaking));
                                        break;
                                    case "Kitesurfing":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_kitesurfing));
                                        break;
                                    case "Motorbike":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_motorbike));
                                        break;
                                    case "Mountain biking":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mountainbiking_3));
                                        break;
                                    case "Nordic ski":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_nordicski));
                                        break;
                                    case "Paragliding":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_paragliding));
                                        break;
                                    case "Parasailing":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_parasailing));
                                        break;
                                    case "Rollerblaiding":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_rollerblade));
                                        break;
                                    case "Skating":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_rollerskate));
                                        break;
                                    case "Rugby":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_rugbyfield));
                                        break;
                                    case "Sailing":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_sailing));
                                        break;
                                    case "Scuba diving":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_scubadiving));
                                        break;
                                    case "Shotting range":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_shootingrange));
                                        break;
                                    case "Skiing":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_skiing));
                                        break;
                                    case "Skijumping":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_skijump));
                                        break;
                                    case "Sledging":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_sledge));
                                        break;
                                    case "Sledging - summer":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_sledge_summer));
                                        break;
                                    case "Snorkeling":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_snorkeling));
                                        break;
                                    case "Snowboarding":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_snowboarding));
                                        break;
                                    case "Snowmobiling":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_snowmobiling));
                                        break;
                                    case "Snowshoeing":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_snowshoeing));
                                        break;
                                    case "Speedriding":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_speedriding));
                                        break;
                                    case "Spelunking":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_spelunking));
                                        break;
                                    case "Sumo":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_sumo_2));
                                        break;
                                    case "Surfing":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_surfing));
                                        break;
                                    case "Taekwondo":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_taekwondo_2));
                                        break;
                                    case "US football":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_usfootball));
                                        break;
                                    case "Watercraft":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_watercraft));
                                        break;
                                    case "Waterskiing":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_waterskiing));
                                        break;
                                    case "Windsurfing":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_windsurfing));
                                        break;
                                    case "Wrestling":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_tennis));
                                        break;
                                    case "Yoga":
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_yoga));
                                        break;
                                }

                                Marker marker = mMap.addMarker(markerOptions);
                                marker.setVisible(true);

                                markersForEvents.push(marker);
                            }


                            if(myCircle != null){
                                myCircle.remove();
                                myCircle = null;
                            }
                                CircleOptions co = new CircleOptions();
                                co.center(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
                                co.radius(radius * 1000);
                                co.fillColor(0x33331CFF);
                                co.strokeColor(0x8C331CFF);
                                co.strokeWidth(5);

                                myCircle = mMap.addCircle(co);

                        }
                    }
                });
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public void addFriendsMarkers() {
        ArrayList<User> friends = FriendsData.getInstance().getFriends();
        if (friends != null) {
            for (int i = 0; i < friends.size(); i++) {

                User user = friends.get(i);

                Marker marker = friendsMarkers.get(user.getId());
                LatLng loc = friendsLocations.get(user.getId());
                if(loc != null) {
                    if (marker == null) {

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(loc);


                        markerOptions.title(user.getFirstname() + " " + user.getLastname());


                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomMarkerView, FriendsData.getInstance().getUserphoto(friends.get(i).getId()))));

                        markerOptions.snippet("Username: " + user.getUsername() + "\n" +
                                "Phone number: " + user.getPhonenumber());

                        marker = mMap.addMarker(markerOptions);
                        friendsMarkers.put(friends.get(i).getId(), marker);
                        marker.setVisible(true);
                    } else {
                        marker.setPosition(loc);
                    }
                }
                else{
                    if(marker!=null){
                        marker.remove();
                    }
                }

            }
        }

    }


    public void removeFriendsMarkers() {
        ArrayList<User> friends = FriendsData.getInstance().getFriends();
        if (friends != null) {
            for (int i = 0; i < friends.size(); i++) {

                User user = friends.get(i);

                Marker marker = friendsMarkers.get(user.getId());


                if (marker != null) {
                    marker.remove();
                }

                friendsMarkers.remove(user.getId());
            }
        }
    }


    private Bitmap getMarkerBitmapFromView(View view, Bitmap bitmap) {

        mMarkerImageView.setImageBitmap(bitmap);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = view.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        view.draw(canvas);
        return returnedBitmap;
    }


    @Override
    public void onLocationChanged(Location location) {

        handleNewLocation(location);

        UserData.getInstance().getUser().setLatitude(String.valueOf(location.getLatitude()));
        UserData.getInstance().getUser().setLongitude(String.valueOf(location.getLongitude()));

    }




    private AlertDialog createDialogBox(Activity a, String name) {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(a)
                .setTitle("Friend request")
                .setMessage("User " + name + " wants to add you as a friend. Do you accept?")

                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(getApplicationContext(), "Yes chosen",
                                Toast.LENGTH_SHORT).show();


                    }
                })

                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(getApplicationContext(), "No chosen",
                                Toast.LENGTH_SHORT).show();


                    }
                })
                .create();
        return myQuittingDialogBox;
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
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {

                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            } catch (IntentSender.SendIntentException e) {

                e.printStackTrace();
            }
        } else {

            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    boolean firstTime = true;
    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        myLocation = location;

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);


        if(myMarker == null) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title("I am here!");
            myMarker = mMap.addMarker(options);
        }
        else{
            myMarker.setPosition(latLng);
        }

        if(firstTime) {
            firstTime = false;

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .zoom(14)
                    .bearing(90)
                    .tilt(40)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
             SupportMapFragment mapFragment =
                     (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
             mapFragment.getMapAsync(this);
        }
    }

    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }


    private void guiNotifyUser(final String message) {
        guiThread.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void guiStartProgressDialog(final String title, final String message) {
        guiThread.post(new Runnable() {
            @Override
            public void run() {
                progressDialog.setTitle(title);
                progressDialog.setMessage(message);
                progressDialog.show();

            }
        });
    }

    private void guiDismissProgressDialog(){
        guiThread.post(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        });

    }


@Override
public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
        drawer.closeDrawer(GravityCompat.START);
    } else {
        super.onBackPressed();
    }
}

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_new_event_nav:
                String lon = Double.toString(myLocation.getLongitude());
                String lat = Double.toString(myLocation.getLatitude());


                Intent addNewEvent = new Intent(this, AddEventActivity.class);
                addNewEvent.putExtra("lat", lat);
                addNewEvent.putExtra("lon", lon);


                startActivityForResult(addNewEvent, ADD_NEW_EVENT);
                return true;
            case R.id.action_add_new_friend_nav:
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                break;
            case R.id.action_friends_list_nav:
                Intent friendsListIntent = new Intent(this, FriendsListActivity.class);
                startActivity(friendsListIntent);
                break;
            case R.id.action_event_list_nav:
                Intent eventsListIntent = new Intent(this, EventsListActivity.class);
                SOEventsData.getInstance().near = true;
                SOEventsData.getInstance().search = false;
                eventsListIntent.putExtra("my_events", false);
                startActivity(eventsListIntent);
                break;
            case R.id.action_ranking_list_nav:
                Intent rankingListIntent = new Intent(this, RankingListActivity.class);
                startActivity(rankingListIntent);
                break;
            case R.id.action_my_event_list:
                Intent myEventsListIntent = new Intent(this, EventsListActivity.class);
                myEventsListIntent.putExtra("my_events", true);
                SOEventsData.getInstance().near = false;
                SOEventsData.getInstance().search = false;
                startActivity(myEventsListIntent);
                break;

            case R.id.action_log_off:
                final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
                popDialog.setIcon(R.drawable.ic_exit_to_app);
                popDialog.setTitle("Log off");
                popDialog.setMessage("You are about to close the application.");

                popDialog.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ExecutorService thread = Executors.newSingleThreadScheduledExecutor();
                                thread.submit(new Runnable() {
                                    @Override
                                    public void run() {
                                        AppHTTPHelper.logOffUser(UserData.getInstance().getUser().getId());
                                    }
                                });
                                thread.shutdown();
                                dialog.dismiss();
                                UserData.getInstance().setUser(null);
                                finish();
                            }
                        });

                popDialog.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                popDialog.create();
                popDialog.show();

                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void ShowDialogRadius(){
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final SeekBar seek = new SeekBar(this);
        seek.setMax(5);
        seek.setKeyProgressIncrement(1);
        seek.setProgress(radius - 1);

        popDialog.setIcon(android.R.drawable.btn_star_big_on);
        popDialog.setTitle("Please select desired radius ");

        LinearLayout linear=new LinearLayout(this);

        linear.setOrientation(LinearLayout.VERTICAL);
        final TextView text=new TextView(this);
        text.setText(String.valueOf(radius));
        text.setPadding(10, 10, 10, 10);

        linear.addView(seek);
        linear.addView(text);

        popDialog.setView(linear);

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                radius = progress + 1;
                text.setText(String.valueOf(progress + 1));

            }


            public void onStartTrackingTouch(SeekBar arg0) {

            }


            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        final FloatingActionButton fabRadius = (FloatingActionButton) findViewById(R.id.fabRadius);


        popDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

        popDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {


                        switch (radius) {
                            case 1:
                                fabRadius.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_looks_one));
                                break;

                            case 2:
                                fabRadius.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_looks_two));
                                break;

                            case 3:
                                fabRadius.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_looks_3));
                                break;

                            case 4:
                                fabRadius.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_looks_4));
                                break;

                            case 5:
                                fabRadius.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_looks_5));
                                break;

                            case 6:
                                fabRadius.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_looks_6));
                                break;
                        }

                        setEvents();

                        dialog.dismiss();
                    }
                });



        popDialog.create();
        popDialog.show();
    }




    private void setupChat() {
        Log.d(TAG, "setupChat()");
        mChatService = new BluetoothService(context, mHandler);
        mOutStringBuffer = new StringBuffer("");

        if (mChatService != null) {

            if (mChatService.getState() == BluetoothService.STATE_NONE) {
                mChatService.start();
            }
        }

    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void sendMessage(String message) {
        if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            mChatService.write(send);
            mOutStringBuffer.setLength(0);
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    if(readMessage.substring(0, 3).equals("Add")){
                        MainActivity.this.ShowDialogFriendRequest(readMessage.substring(3));
                    }
                    else if(readMessage.substring(0, 3).equals("Yes")){
                        Toast.makeText(context, mConnectedDeviceName + ": Yes ", Toast.LENGTH_SHORT).show();

                        MainActivity.this.addFriend(readMessage.substring(3));
                    }
                    else if(readMessage.equals("No1")){
                        Toast.makeText(context, mConnectedDeviceName + ": No ", Toast.LENGTH_SHORT).show();
                    }
                    else if(readMessage.equals("Refresh")){
                        ExecutorService thread1 = Executors.newSingleThreadExecutor();
                        thread1.submit(new Runnable() {
                            @Override
                            public void run() {
                               HashMap<String, Object> hashMap1 =  AppHTTPHelper.getAllFriendsAndSignedEvents(UserData.getInstance().getUser().getId());

                                ArrayList<User> friends = new ArrayList<User>();

                                friends = (ArrayList<User>)hashMap1.get("friends");

                                FriendsData.getInstance().setInstance(friends);
                            }
                        });
                        thread1.shutdown();

                    }

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);

                        Toast.makeText(context, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                    break;
                case Constants.MESSAGE_DEVICE_NAME_SERVER:
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(context, "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();



                    break;
                case Constants.MESSAGE_DEVICE_NAME_CLIENT:
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(context, "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                    MainActivity.this.sendMessage("Add" + UserData.getInstance().getUser().getUsername());
                    break;
                case Constants.MESSAGE_TOAST:
                        Toast.makeText(context, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();

                    break;
            }
        }
    };

    private void connectDevice(Intent data, boolean secure) {
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mChatService.connect(device, secure);
    }

    private void showStatus(String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }


    public void ShowDialogFriendRequest(String user){
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        popDialog.setIcon(R.drawable.ic_person_add);
        popDialog.setTitle("Friend request");
        popDialog.setMessage("User " + user + " wants to add you as a friend.");

        popDialog.setPositiveButton("Sure :)",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sendMessage("Yes" + String.valueOf(UserData.getInstance().getUser().getId()));

                        dialog.dismiss();
                    }
                });

        popDialog.setNegativeButton("No way",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sendMessage("No1");

                        dialog.dismiss();
                    }
                });

        popDialog.create();
        popDialog.show();
    }


    public void addFriend(final String friend_id){

        ExecutorService addFriendExecutor = Executors.newSingleThreadExecutor();
        addFriendExecutor.submit(new Runnable() {
            User friend = null;

            @Override
            public void run() {
                friend = AppHTTPHelper.addFriend(UserData.getInstance().getUser().getId(), Long.parseLong(friend_id));
                guiDismissProgressDialog();
                if (friend != null) {
                    FriendsData.getInstance().addFriend(friend);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ShowDialogFriendAdded(friend, "Success", "Friend " +friend.getUsername() + " succesfully added");
                            sendMessage("Refresh");
                        }
                        });

                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final AlertDialog.Builder popDialog = new AlertDialog.Builder(context);
                            popDialog.setIcon(R.drawable.ic_person_add_black);
                            popDialog.setTitle("Failed");
                            popDialog.setMessage("Friend adding failed");

                            popDialog.setPositiveButton("Ok",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });

                            popDialog.create();
                            popDialog.show();
                        }
                    });

                }
            }
        });

    }



    public void ShowDialogFriendAdded(User friend, String title, String message){
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        popDialog.setIcon(R.drawable.ic_person_add_black);
        popDialog.setTitle(title);
        popDialog.setMessage(message);

        popDialog.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        popDialog.setNegativeButton("Check friend profile",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

        popDialog.create();
        popDialog.show();
    }

    public void setPreference(){

    }

    public void getPreferences(){

    }

    public void deletePreferences(){

    }


    public void setDataAfterLogin(){

        friendsTransThread.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    final HashMap<String, Object> hashMap1 = AppHTTPHelper.getAllFriendsAndSignedEvents(UserData.getInstance().getUser().getId());

                    ArrayList<User> friends = new ArrayList<User>();
                    ArrayList<SOEvent> events1 = new ArrayList<SOEvent>();

                    friends = (ArrayList<User>)hashMap1.get("friends");

                    events1 = (ArrayList<SOEvent>)hashMap1.get("events");

                    FriendsData.getInstance().setInstance(friends);


                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }, 0, 3, TimeUnit.SECONDS);



        startMyLocationThread();

        setEvents();

        final TextView textViewName1 = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.textView1Nav);
        final TextView textViewEmail = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.textView2Nav);
        final TextView textViewName2 = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.textView3Nav);
        final ImageView imageView = (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.imageViewNav);

        User user = UserData.getInstance().getUser();
        textViewName1.setText(user.getFirstname());
        textViewName2.setText(user.getLastname());
        textViewEmail.setText(user.getUsername());

        byte[] decodedString = Base64.decode(user.getUserphoto(), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        if(decodedByte != null) {
            imageView.setImageBitmap(decodedByte);
        }


        setEvents();

    }



}


