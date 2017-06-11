package com.elfak.mosis.sportsoutdoors;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RankingListActivity extends AppCompatActivity {

    SOEvent event;
    ListView rankingList;

    ScheduledExecutorService thread;
    ExecutorService threadGetPlayers;
    Future<?> future;

    CustomRankAdapter mAdapter;

    ArrayList<Player> players;

    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Players rankings");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rankingList = (ListView) findViewById(R.id.ranking_list_all);

        players = new ArrayList<>();


        mAdapter = new CustomRankAdapter(this, players);
        rankingList.setAdapter(mAdapter);

        thread = Executors.newSingleThreadScheduledExecutor();
        threadGetPlayers = Executors.newSingleThreadExecutor();

        threadGetPlayers.submit(new Runnable() {
            @Override
            public void run() {
                final ArrayList<Player> players1 = AppHTTPHelper.getPlayersRatings(counter, UserData.getInstance().getUser().getId());
                players = players1;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.addAll(players1);

                        mAdapter.notifyDataSetChanged();

                    }
                });
            }
        });

        future = thread.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {




                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        }, 0, 20, TimeUnit.SECONDS);


    }

    @Override
    protected void onDestroy() {
        thread.shutdown();
        threadGetPlayers.shutdown();
        super.onDestroy();


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
}



class CustomRankAdapter extends ArrayAdapter<Player>{
private static class ViewHolder{
    TextView position;
    TextView name;
    TextView score;
}

Context context;

ArrayList<Player> players;

    public CustomRankAdapter(Context context, ArrayList<Player> players){

        super(context, R.layout.player_rank_item, players);

    }




    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Player player = getItem(position);
        ViewHolder holder;

        if(convertView == null){
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.player_rank_item, parent, false);


            holder.position = (TextView) convertView.findViewById(R.id.player_pos);
            holder.name = (TextView) convertView.findViewById(R.id.player_name);
            holder.score = (TextView) convertView.findViewById(R.id.player_score);

            convertView.setTag(holder);

        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }


        holder.position.setText(String.valueOf(position + 1) + ". ");

        String name = getItem(position).username;
        holder.name.setText(name + "  ");

        String score = String.valueOf(getItem(position).score);
        holder.score.setText(score);

        return convertView;
    }


}
