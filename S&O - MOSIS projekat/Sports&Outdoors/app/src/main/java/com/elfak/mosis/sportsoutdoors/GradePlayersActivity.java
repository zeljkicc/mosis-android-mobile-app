package com.elfak.mosis.sportsoutdoors;

import android.app.Activity;
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
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GradePlayersActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    Handler guiThread;
    ProgressDialog progressDialog;
    Context context;

    ExecutorService transThread;

    private ArrayList<User> players;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_grade_players);

       setTitle("Grade players: ");

        setResult(Activity.RESULT_CANCELED);

        players = new ArrayList<>();

        guiThread = new Handler();
        progressDialog = new ProgressDialog(this);
        context = this;

        transThread = Executors.newSingleThreadExecutor();

        mRecyclerView = (RecyclerView) findViewById(R.id.events_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        Intent i = getIntent();
        int index = i.getIntExtra("index", 0);
       final SOEvent event = SOEventsData.getInstance().getSignedUpEvent(index);

        Button button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transThread.submit(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<Integer> ratings = new ArrayList<Integer>();

                        ArrayList<Long> ids = new ArrayList<Long>();

                        for(int i =0; i < players.size(); i++) {
                            ids.add(((GradePlayersAdapter)mAdapter).getId(i));
                            ratings.add(((GradePlayersAdapter)mAdapter).getScore(i));
                        }



                        String ret = AppHTTPHelper.submitPlayerRatings(ratings, ids);

                        if(ret.equals("Success")){
                            guiNotifyUser("Successfully added player ratings");

                                    setResult(Activity.RESULT_OK);
                                    ((Activity) context).finish();
                        }
                        else{
                            guiNotifyUser("Failed while adding player ratings. Please try again.");
                        }
                    }
                });
            }
        });

        setData(event);
    }

    public void setData(final SOEvent event) {

        guiStartProgressDialog("Setting up", "Setting up players. Please wait.");
        transThread.submit(new Runnable() {
            @Override
            public void run() {
                players = AppHTTPHelper.getPlayersFromEvent(event.getId());

                if (players != null) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mAdapter = new GradePlayersAdapter(context, players);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "No players played", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                guiDismissProgressDialog();
            }
        });
    }

    @Override
    protected void onDestroy() {
        transThread.shutdown();
        super.onDestroy();
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

    private void guiDismissProgressDialog() {
        guiThread.post(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        });

    }
}
