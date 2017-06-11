package com.elfak.mosis.sportsoutdoors;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class EventsListActivity extends AppCompatActivity {

    ArrayList<String> events;

    public static int VIEW_EVENT = 1545;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public static boolean my_events = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent i = getIntent();
        Bundle data = i.getExtras();
        boolean my_events1 = data.getBoolean("my_events");

        my_events = my_events1;


        mRecyclerView = (RecyclerView) findViewById(R.id.events_recycler_view);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        if(my_events) {
            setTitle("Mine or signed for events");
            mAdapter = new EventsListAdapter(this, SOEventsData.getInstance().getSignedUpEvents());

        }
        else {
            setTitle("Events near me");
            mAdapter = new EventsListAdapter(this, SOEventsData.getInstance().getSoEvents());
        }


        mRecyclerView.setAdapter(mAdapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == VIEW_EVENT){
            if (resultCode == Activity.RESULT_OK){
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
