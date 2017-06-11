package com.elfak.mosis.sportsoutdoors;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.common.base.Predicate;

//import com.google.common.base.Predicate;
//import com.google.common.collect.Collections2;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchableActivity extends AppCompatActivity{

    SearchRecentSuggestions  suggestions;

    Handler guiThread;
    ProgressDialog progressDialog;
    Context context;

    TabHost tabs;

    TextView no_events_found;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        guiThread = new Handler();
        progressDialog = new ProgressDialog(this);
        context = this;

        setTitle("Search events");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        no_events_found = (TextView) findViewById(R.id.no_events_found);
        no_events_found.setVisibility(View.GONE);

        tabs=(TabHost) findViewById(R.id.tabhostAdd);
        tabs.setup();

        TabHost.TabSpec spec;


        spec =tabs.newTabSpec("tag1");
        spec.setContent(R.id.content1);
        spec.setIndicator("Name");
        tabs.addTab(spec);

        spec=tabs.newTabSpec("tag2");
        spec.setContent(R.id.content1);
        spec.setIndicator("Category");
        tabs.addTab(spec);

        spec=tabs.newTabSpec("tag3");
        spec.setContent(R.id.content1);
        spec.setIndicator("Description");
        tabs.addTab(spec);

        tabs.setCurrentTab(0);

        suggestions = new SearchRecentSuggestions(this,
                MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

           guiStartProgressDialog("Searching", "Searching events online");
            final String query = intent.getStringExtra(SearchManager.QUERY);

            mRecyclerView = (RecyclerView) findViewById(R.id.events_recycler_view);

            mLayoutManager = new LinearLayoutManager(context);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());

            suggestions.saveRecentQuery(query, null);

            final int current = tabs.getCurrentTab();


            ExecutorService thread = Executors.newSingleThreadScheduledExecutor();
            thread.submit(new Runnable() {
                @Override
                public void run() {
                    final ArrayList<SOEvent> queried_events;
                            if(current == 0) {
                                queried_events   = AppHTTPHelper.getQueriedEvents(query, true, false, false);
                            }
                            else if (current == 1){
                                queried_events = AppHTTPHelper.getQueriedEvents(query, false, true, false);
                            }
                            else{
                                 queried_events = AppHTTPHelper.getQueriedEvents(query, false, false, true);
                            }

                            runOnUiThread(new Runnable() {
                                @Override

                                public void run() {

                                    if(queried_events != null) {
                                        no_events_found.setVisibility(View.GONE);
                                        SOEventsData.getInstance().search = true;
                                        SOEventsData.getInstance().near = false;
                                        mAdapter = new EventsListAdapter(context, queried_events);
                                        mRecyclerView.setAdapter(mAdapter);


                                    }
                                    else{
                                        no_events_found.setVisibility(View.VISIBLE);
                                    }

                                    guiDismissProgressDialog();
                                }
                            });




                }
            });

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_searchable, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        MenuItemCompat.expandActionView(menu.findItem(R.id.action_search));

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
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
    protected void onDestroy() {
        SOEventsData.getInstance().search = false;
        super.onDestroy();
    }
}
