package com.elfak.mosis.sportsoutdoors;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEventActivity extends AppCompatActivity implements View.OnClickListener,  OnMapReadyCallback {

    Handler guiThread;
    ProgressDialog progressDialog;
    Context context;
    String datetime = null;
    ExecutorService transThread;
    TabHost tabs;
    Marker marker = null;
    GoogleMap mMap = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        guiThread = new Handler();
        progressDialog = new ProgressDialog(this);
        context = this;

        setUpMapIfNeeded();

        final Button addButton = (Button) findViewById(R.id.action_add_event);
        addButton.setOnClickListener(this);
        final Button backButton = (Button) findViewById(R.id.action_back_from_add_event);
        backButton.setOnClickListener(this);
        final Button buttonDatetime = (Button) findViewById(R.id.action_add_event_datetime);
        buttonDatetime.setOnClickListener(this);

        Spinner spinner = (Spinner) findViewById(R.id.type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        String[] array = new String[101];
        for(int i = 1; i<101; i++){
            array[i - 1] = Integer.toString(i);
        }

        spinner = (Spinner) findViewById(R.id.number_players_spinner);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter1);

        setResult(Activity.RESULT_CANCELED);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Drag the marker or longpress on the map to set location", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tabs=(TabHost) findViewById(R.id.tabhostAdd);
        tabs.setup();

        TabHost.TabSpec spec;
        spec =tabs.newTabSpec("tag1");
        spec.setContent(R.id.tab1Add);
        spec.setIndicator("1. Location");
        tabs.addTab(spec);

        spec=tabs.newTabSpec("tag2");
        spec.setContent(R.id.tab2Add);
        spec.setIndicator("2. Information");
        tabs.addTab(spec);

        tabs.setCurrentTab(0);


    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.action_add_event:
                EditText nameEditText = (EditText) findViewById(R.id.event_name_edittext);
                EditText desciptionEditText = (EditText) findViewById(R.id.event_description_edittext);

                String name = nameEditText.getText().toString();
                String description = desciptionEditText.getText().toString();

                Spinner spinner = (Spinner)findViewById(R.id.type_spinner);
                String type = spinner.getSelectedItem().toString();

                spinner = (Spinner)findViewById(R.id.number_players_spinner);
                String number_of_players = spinner.getSelectedItem().toString();

                LatLng loc = marker.getPosition();
                final String lat1 = String.valueOf(loc.latitude);
                final String lon1 = String.valueOf(loc.longitude);


                final SOEvent event = new SOEvent(0, name, description, type, Integer.valueOf(number_of_players), lat1, lon1, datetime, UserData.getInstance().getUser().getId());

                transThread = Executors.newSingleThreadExecutor();
                transThread.submit(new Runnable() {
                    @Override
                    public void run() {
                        guiStartProgressDialog("Adding new event in progress", "Event " + event.getName());

                        try {
                            final SOEvent event1 = AppHTTPHelper.addEvent(event);
                            if (event1 != null) {
                                SOEventsData.getInstance().addSignedUpEvent(event1);
                                setResult(Activity.RESULT_OK);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        guiDismissProgressDialog();
                                        guiNotifyUser("Event added successfully");
                                    }
                                });
                                setResult(Activity.RESULT_OK);
                                finish();
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        guiDismissProgressDialog();
                                        guiNotifyUser("Event could not be add. Please try again.");
                                    }
                                });
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                });


                break;
            case R.id.action_back_from_add_event:
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
            case R.id.action_add_event_datetime:

                showDateTimePicker();

                break;

        }
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
        guiThread.post(new Runnable(){
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        });



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

    public void showDateTimePicker() {
        final Calendar currentDate = Calendar.getInstance();
        final Calendar date1 = Calendar.getInstance();
        new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                date1.set(year, monthOfYear, dayOfMonth);
                new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        date1.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        date1.set(Calendar.MINUTE, minute);
                        setDatetimeTextview(date1);
                    }
                }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true).show();
            }
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();


    }

    public void setDatetimeTextview(Calendar date1){
        final TextView textviewDatetime = (TextView) findViewById(R.id.datetime_textview);

        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date today = date1.getTime();
        String reportDate = df.format(today);
        textviewDatetime.setText(reportDate);
        datetime = reportDate;
    }



    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        Intent intent = getIntent();
        String lat = intent.getStringExtra("lat");
        String lon = intent.getStringExtra("lon");

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(Double.parseDouble(lat), Double.parseDouble(lon)))
                .zoom(14)
                .bearing(90)
                .tilt(40)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        marker = map.addMarker(new MarkerOptions().draggable(true).title("Event location").
                position(new LatLng(Double.parseDouble(lat), Double.parseDouble(lon))).
                visible(true));

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                marker.setPosition(latLng);
            }
        });

    }

    private void setUpMapIfNeeded() {

        if (mMap == null) {
            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapAdd);
            mapFragment.getMapAsync(this);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED);
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
