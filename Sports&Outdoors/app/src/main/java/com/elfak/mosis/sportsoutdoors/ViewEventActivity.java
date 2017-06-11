package com.elfak.mosis.sportsoutdoors;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ViewEventActivity extends AppCompatActivity {

    private final static int USER_SING_UP = 1;
    private final static int  USER_SINGED_UP = 2;

    private final static int FINISH = 3;

    private final static int FINISHED = 4;

    private final static int GRADE_ACTIVITY = 101;

    ScheduledExecutorService thread;
    ExecutorService sendMessageThread;
    Future<?> future;
    long numberOfMessages = 0;
    long numberOfPlayers = 0;
    SOEvent event;
    ArrayList<Player> mPlayers = new ArrayList<Player>();
    ArrayList<Message> mMessages = new ArrayList<Message>();

    Button button;

    int button_status = -1;
    TextView numberTV;

    ArrayAdapter<Player> mPlayerAdapter;
    ArrayAdapter<Message> mMessageAdapter;

    ListView listviewPlayers;
    ListView listviewMessages;

    TabHost tabs;

    Handler guiThread;
    ProgressDialog progressDialog;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Event details");

        guiThread = new Handler();
        progressDialog = new ProgressDialog(this);
        context = this;


        button = (Button) findViewById(R.id.eventview_button);

        tabs=(TabHost) findViewById(R.id.tabhost);
        tabs.setup();


        TabHost.TabSpec spec;
        spec =tabs.newTabSpec("tag1");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Info");
        tabs.addTab(spec);

        spec=tabs.newTabSpec("tag2");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Players");
        tabs.addTab(spec);

        spec=tabs.newTabSpec("tag3");
        spec.setContent(R.id.tab3);
        spec.setIndicator("Chat");
        tabs.addTab(spec);

        tabs.setCurrentTab(0);
        tabs.setOnTabChangedListener(new AnimatedTabHostListener(this, tabs));

        Intent i = getIntent();
        final int index = Integer.valueOf(i.getStringExtra("eventId"));

        final boolean from_near_list = i.getBooleanExtra("from_near_list", false);

        boolean my_event = false;
        ArrayList<Long> ids = SOEventsData.getInstance().getSignedEventsIds();


if(!SOEventsData.getInstance().search) {
    if (from_near_list) {

        event = SOEventsData.getInstance().getSoEvent(index);

    } else {

        event = SOEventsData.getInstance().getSignedUpEvent(index);

    }
}
        else{
        event = SOEventsData.getInstance().searchEvent;
        }


            if (event.getUserId() != UserData.getInstance().getUser().getId()) {
                my_event = ids.contains(event.getId());


                if (my_event) {
                    button.setText("Sign off");
                    button_status = USER_SINGED_UP;
                } else {
                    button.setText("Sign up");
                    button_status = USER_SING_UP;
                }
            } else {
                button.setText(R.string.finish);
                button_status = FINISH;
            }

        if(event.getFinished()) {
            button.setText("Grade players");
            button_status = FINISHED;

        }

        if(SOEventsData.getInstance().search){
            button.setVisibility(View.GONE);
        }

        mPlayers = new ArrayList<Player>();
        listviewPlayers = (ListView) findViewById(R.id.eventview_list_players);
        mPlayerAdapter = new ArrayAdapter<Player>(this, android.R.layout.simple_list_item_1, mPlayers);
        listviewPlayers.setAdapter(mPlayerAdapter);

        mMessages = new ArrayList<Message>();
        listviewMessages = (ListView) findViewById(R.id.eventview_list_chat);
        mMessageAdapter = new ArrayAdapter<Message>(this, android.R.layout.simple_list_item_1, mMessages);
        listviewMessages.setAdapter(mMessageAdapter);

        final TextView nameTV = (TextView) findViewById(R.id.eventview_name);
        final TextView descriptionTV = (TextView) findViewById(R.id.eventview_description);
        numberTV = (TextView) findViewById(R.id.eventview_number);
        final TextView typeTV = (TextView) findViewById(R.id.eventview_type);
        final TextView datetimeTV = (TextView) findViewById(R.id.eventview_datetime);
        final TextView userTV = (TextView) findViewById(R.id.eventview_user);

        if(event.getName()!=null)
        nameTV.setText(event.getName());
        if(event.getDescription()!=null)
        descriptionTV.setText(event.getDescription());
        numberTV.setText(String.valueOf(event.getNumberOfPlayers()));
        if(event.getType()!=null)
        typeTV.setText(event.getType());
        if(event.getDatetime()!=null)
        datetimeTV.setText(event.getDatetime());




        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    sendMessageThread.submit(new Runnable() {
                        @Override
                        public void run() {

                            if( button_status == USER_SING_UP) {
                                guiStartProgressDialog("Signing up", "You are beeing signed up for this event");
                                String ret = AppHTTPHelper.signUpForEvent(event.getId(), UserData.getInstance().getUser().getId(), UserData.getInstance().getUser().getUsername(), event);
                                if(ret.equals("Success")){
                                    SOEventsData.getInstance().addSignedUpEvent(event);
                                    button_status = USER_SINGED_UP;

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            button.setText(R.string.sign_off);
                                        }
                                    });
                                    guiDismissProgressDialog();

                                }

                            }
                            else if(button_status == USER_SINGED_UP){
                                guiStartProgressDialog("Signing off", "You are beeing signed off from this event");
                                String ret = AppHTTPHelper.signOffFromEvent(event.getId(), UserData.getInstance().getUser().getId());
                                if(ret.equals("Success")) {
                                    SOEventsData.getInstance().removeSignedUpEvent(event.getId());
                                    button_status = USER_SING_UP;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            button.setText(R.string.sign_up);
                                        }
                                    });

                                }
                                guiDismissProgressDialog();

                            }
                            else if(button_status == FINISH){
                                guiStartProgressDialog("Finishing event", "You are finishing current event");

                                String ret = AppHTTPHelper.finishEvent(event.getId(), UserData.getInstance().getUser().getId(), event);
                                if(ret.equals("Success")) {

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {


                                            guiDismissProgressDialog();
                                            Toast.makeText(context, "Event was finished successfuly", Toast.LENGTH_SHORT).show();

                                            Intent gradeIntent = new Intent(context, GradePlayersActivity.class);
                                            gradeIntent.putExtra("index", index);
                                            startActivityForResult(gradeIntent, GRADE_ACTIVITY);

                                        }
                                    });

                                }else{
                                guiDismissProgressDialog();}
                            }

                            else if(button_status == FINISHED){
                                Intent gradeIntent = new Intent(context, GradePlayersActivity.class);
                                gradeIntent.putExtra("index", index);
                                startActivityForResult(gradeIntent, GRADE_ACTIVITY);
                            }



                        }
                    });

            }
        });

        final Button buttonSendMessage = (Button) findViewById(R.id.send_message_button);
        final EditText editTextMessage = (EditText) findViewById(R.id.send_message_edittext);
        buttonSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String message = editTextMessage.getText().toString();
                sendMessageThread.submit(new Runnable() {
                    @Override
                    public void run() {
                       String ret = AppHTTPHelper.sendChatMessage(event.getId(), UserData.getInstance().getUser().getUsername(), message);

                    }
                });

            }
        });

        sendMessageThread = Executors.newSingleThreadExecutor();
        thread = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    protected void onStart() {
        super.onStart();

        setDataForViewEventActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        sendMessageThread.shutdown();
        thread.shutdown();
    }

    public void setDataForViewEventActivity(){
        final ListView listviewPlayers = (ListView) findViewById(R.id.eventview_list_players);

        try{
        future = thread.scheduleAtFixedRate(new Runnable() {
            public void run() {


                final DataForView data = AppHTTPHelper.getDataForViewEventActivity(event.getId(), UserData.getInstance().getUser().getId(), numberOfMessages);


                if(data.messages.size()!=0){
                    for(int i = data.messages.size()-1; i >= 0; i--){
                        mMessages.add(0, data.messages.get(i));
                    }
                    numberOfMessages = mMessages.size();

                }

                if(data.players.size()!=0){
                    mPlayers = data.players;
                    numberOfPlayers = mPlayers.size();
                }
                else{
                    numberOfPlayers = 0;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(data.players.size()!=0){
                            mPlayerAdapter.clear();
                            mPlayerAdapter.addAll(mPlayers);

                        }


                        numberTV.setText(String.valueOf(numberOfPlayers) + " / " + String.valueOf(event.getNumberOfPlayers()));


                        if(data.messages.size()!=0){
                            mMessageAdapter.notifyDataSetChanged();
                            listviewMessages.smoothScrollToPosition(0);
                        }

                    }
                });

            }
        }, 0, 5, TimeUnit.SECONDS);}
        catch (Exception e){
            e.printStackTrace();
        }
    }


    float lastX, lastY;
    @Override
    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                lastX = touchevent.getX();
                break;
            }
            case MotionEvent.ACTION_UP: {
                float currentX = touchevent.getX();

                if (lastX < currentX) {

                    switchTabs(false);
                }

                if (lastX > currentX) {
                    switchTabs(true);
                }

                break;
            }
        }
        return false;
    }




    public void switchTabs(boolean direction) {
        if (direction)
        {
            if (tabs.getCurrentTab() == 0)
                tabs.setCurrentTab(tabs.getTabWidget().getTabCount() - 1);
            else
                tabs.setCurrentTab(tabs.getCurrentTab() - 1);
        } else

        {
            if (tabs.getCurrentTab() != (tabs.getTabWidget()
                    .getTabCount() - 1))
                tabs.setCurrentTab(tabs.getCurrentTab() + 1);
            else
                tabs.setCurrentTab(0);
        }
    }


    public class AnimatedTabHostListener implements TabHost.OnTabChangeListener
    {

        private static final int ANIMATION_TIME = 240;
        private TabHost tabHost;
        private View previousView;
        private View currentView;
        private GestureDetector gestureDetector;
        private int currentTab;

        public AnimatedTabHostListener(Context context, TabHost tabHost)
        {
            this.tabHost = tabHost;
            this.previousView = tabHost.getCurrentView();
            gestureDetector = new GestureDetector(context, new MyGestureDetector());
            tabHost.setOnTouchListener(new View.OnTouchListener()
            {
                public boolean onTouch(View v, MotionEvent event)
                {
                    if (gestureDetector.onTouchEvent(event))
                    {
                        return false;
                    }
                    else
                    {
                        return true;
                    }
                }
            });
        }


        @Override
        public void onTabChanged(String tabId)
        {

            currentView = tabHost.getCurrentView();
            if (tabHost.getCurrentTab() > currentTab)
            {
                previousView.setAnimation(outToLeftAnimation());
                currentView.setAnimation(inFromRightAnimation());
            }
            else
            {
                previousView.setAnimation(outToRightAnimation());
                currentView.setAnimation(inFromLeftAnimation());
            }
            previousView = currentView;
            currentTab = tabHost.getCurrentTab();

        }


        private Animation inFromRightAnimation()
        {
            Animation inFromRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 1.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                    0.0f);
            return setProperties(inFromRight);
        }


        private Animation outToRightAnimation()
        {
            Animation outToRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                    1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
            return setProperties(outToRight);
        }


        private Animation inFromLeftAnimation()
        {
            Animation inFromLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                    0.0f);
            return setProperties(inFromLeft);
        }


        private Animation outToLeftAnimation()
        {
            Animation outtoLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                    -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
            return setProperties(outtoLeft);
        }


        private Animation setProperties(Animation animation)
        {
            animation.setDuration(ANIMATION_TIME);
            animation.setInterpolator(new AccelerateInterpolator());
            return animation;
        }


        class MyGestureDetector extends GestureDetector.SimpleOnGestureListener
        {
            private static final int SWIPE_MIN_DISTANCE = 120;
            private static final int SWIPE_MAX_OFF_PATH = 250;
            private static final int SWIPE_THRESHOLD_VELOCITY = 200;
            private int maxTabs;


            public MyGestureDetector()
            {
                maxTabs = tabHost.getTabContentView().getChildCount();
            }


            @Override
            public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY)
            {
                int newTab = 0;
                if (Math.abs(event1.getY() - event2.getY()) > SWIPE_MAX_OFF_PATH)
                {
                    return false;
                }
                if (event1.getX() - event2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
                {

                    newTab = currentTab + 1;
                }
                else if (event2.getX() - event1.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
                {

                    newTab = currentTab - 1;
                }
                if (newTab < 0 || newTab > (maxTabs - 1))
                {
                    return false;
                }
                tabHost.setCurrentTab(newTab);
                return super.onFling(event1, event2, velocityX, velocityY);
            }
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
        else if(id == R.id.action_show_on_map){
            Intent i = new Intent(this, EventLocationActivity.class);
            i.putExtra("lat", event.getLatitude());
            i.putExtra("lon", event.getLongitude());
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_event_view, menu);

        return true;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == GRADE_ACTIVITY){
            if(resultCode == Activity.RESULT_OK){
                SOEventsData.getInstance().removeSignedUpEvent(event.getId());
                setResult(Activity.RESULT_OK);
                finish();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
