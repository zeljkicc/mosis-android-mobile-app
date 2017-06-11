package com.elfak.mosis.sportsoutdoors;

import android.location.Location;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;


public class AppHTTPHelper {

    private static final String address = "http://192.168.43.217:10000";
    private static final String CHECK_LOGIN = "1";
    private static final String REGISTER = "2";
    private static final String ADD_EVENT = "3";
    private static final String ADD_FRIEND = "4";
    private static final String GET_ALL_EVENTS = "5";
    private static final String GET_ALL_FRIENDS = "6";
    private static final String GET_FRIENDS = "7";
    private static final String GET_DATA_FOR_VIEWEVENT = "8";
    private static final String SEND_CHAT_MESSAGE = "9";
    private static final String SING_UP_FOR_EVENT = "10";
    private static final String SING_OFF_FROM_EVENT = "11";
    private static final String GET_RATINGS = "12";
    private static final String SERVICE_EVENTS = "13";
    private static final String GET_EVENTS = "14";
    private static final String QUERY_EVENTS = "15";
    private static final String FINISH_EVENT = "16";
    private static final String GET_PLAYERS_FOR_EVENT = "17";
    private static final String SUBMIT_PLAYER_RATINGS = "18";
    private static final String LOG_OFF = "19";

    private static String inputStreamToString(InputStream is){
        String line = "";
        StringBuilder total = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        try{
            while((line = rd.readLine()) != null){
                total.append(line);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return total.toString();
    }


    public static User checkLogin(User user, String lat, String lon){
        String str = null;
        try {
            URL url = new URL(address + "//ServerMosis//login.php");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Gson gson = new Gson();
            String json = gson.toJson(user);



            Uri.Builder builder = new Uri.Builder().appendQueryParameter("req", CHECK_LOGIN).
                    appendQueryParameter("user", json)
                    .appendQueryParameter("lat", lat).
                    appendQueryParameter("lon", lon)
                  ;

            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){

                str = inputStreamToString(conn.getInputStream());

                user = gson.fromJson(str, User.class);
            }
            else{
                user=null;
            }
        }
        catch(Exception e){
            e.printStackTrace();
            user = null;
        }
        return user;
    }


    public static String registerUser(User user){
        String retStr = "";
        try {
            URL url = new URL(address + "//ServerMosis//login.php");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Gson gson = new Gson();
            String json = gson.toJson(user);
            Uri.Builder builder = new Uri.Builder().appendQueryParameter("req", REGISTER).
                    appendQueryParameter("user", json);

            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                retStr = inputStreamToString(conn.getInputStream());
            }
            else{
                retStr=String.valueOf("Error:" + responseCode);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            retStr = "Error during register, please try again!";
        }
        return retStr;
    }

    public static SOEvent addEvent(SOEvent event){
        String retStr = "";
        SOEvent event_ret = new SOEvent();
        try {
            URL url = new URL(address + "//ServerMosis//login.php");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Gson gson = new Gson();
            String json = gson.toJson(event);
            Uri.Builder builder = new Uri.Builder().appendQueryParameter("req", ADD_EVENT).
                    appendQueryParameter("event", json);

            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                retStr = inputStreamToString(conn.getInputStream());
                event_ret = gson.fromJson(retStr, SOEvent.class);

            }
            else{

                event_ret = null;
            }
        }
        catch(Exception e){
            e.printStackTrace();
            event_ret = null;
        }
        return event_ret;
    }


    //4
    public static User addFriend(long user_id, long friend_id){
        String retStr = "";
        User user = null;
        try {
            URL url = new URL(address + "//ServerMosis//login.php");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder().appendQueryParameter("req", ADD_FRIEND).
                    appendQueryParameter("friend_id", String.valueOf(friend_id)).appendQueryParameter("user_id", String.valueOf(user_id));

            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                retStr = inputStreamToString(conn.getInputStream());
                Gson gson = new Gson();
                user = gson.fromJson(retStr, User.class);
            }
            else{
                user = null;                ;
            }
        }
        catch(Exception e){
            e.printStackTrace();
            user=null;
        }
        return user;
    }


    public static ArrayList<SOEvent> getAllEvents(){
        String retStr = "";
        ArrayList<SOEvent> events;
        try {
            URL url = new URL(address + "//ServerMosis//login.php");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder().appendQueryParameter("req", GET_ALL_EVENTS);

            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                String str = inputStreamToString(conn.getInputStream());
                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<SOEvent>>(){}.getType();
                events = gson.fromJson(str, listType);
            }
            else{
                events = null;
            }
        }
        catch(Exception e){
            e.printStackTrace();
            events = null;
        }
        return events;
    }

    //bilo getAllFriends //6
    public static HashMap<String, Object> getAllFriendsAndSignedEvents(long id){
        String retStr = "";
        HashMap<String, Object> ret = new HashMap<>();
        try {
            URL url = new URL(address + "//ServerMosis//login.php");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder().appendQueryParameter("req", GET_ALL_FRIENDS)
                    .appendQueryParameter("id", Long.toString(id));

            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                String str = inputStreamToString(conn.getInputStream());

                JSONObject jObject = new JSONObject(str);

                ArrayList<SOEvent> events = new ArrayList<SOEvent>();
                ArrayList<User> friends = new ArrayList<User>();

                Gson gson = new Gson();
                Type listTypeUser = new TypeToken<ArrayList<User>>(){}.getType();

                Type listTypeEvent = new TypeToken<ArrayList<SOEvent>>(){}.getType();

                events = gson.fromJson(jObject.getString("events"), listTypeEvent );
                friends  = gson.fromJson(jObject.getString("friends"), listTypeUser );

                ret.put("events",  events);
                ret.put("friends", friends);


            }
            else{
                ret.put("events",  null);
                ret.put("friends", null);

            }
        }
        catch(Exception e){
            e.printStackTrace();
            ret.put("events", null);
            ret.put("friends", null);
        }
        return ret;
    }

    //GET_FRIENDS 7
    public static Hashtable<Long, LatLng> getFriendsLocations(long id, String lat, String lon){
        String retStr = "";

        Hashtable<Long, LatLng> friendsLocations = new Hashtable<Long, LatLng>();

        try {
            URL url = new URL(address + "//ServerMosis//login.php");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder().appendQueryParameter("req", GET_FRIENDS)
                    .appendQueryParameter("id", Long.toString(id)).appendQueryParameter("lat", lat)
                    .appendQueryParameter("lon", lon);

            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK) {
                String str = inputStreamToString(conn.getInputStream());
                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<FriendLocation>>() {
                }.getType();
                ArrayList<FriendLocation> friendsTmp = gson.fromJson(str, listType);

                for (int i = 0; i < friendsTmp.size(); i++) {
                    FriendLocation fl = friendsTmp.get(i);
                    friendsLocations.put(fl.id, new LatLng(Double.parseDouble(fl.lat), Double.parseDouble(fl.lon)));
                }
            }
            else{
                friendsLocations = null;
            }
        }
        catch(Exception e){
            e.printStackTrace();
            friendsLocations = null;
        }
        return friendsLocations;
    }

    public static DataForView getDataForViewEventActivity(long eventId, long userId, long numberOfMessages){//8
        String retStr = "";
        DataForView data = new DataForView();

        Hashtable<Long, LatLng> friendsLocations = new Hashtable<Long, LatLng>();

        try {
            URL url = new URL(address + "//ServerMosis//login.php");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder().appendQueryParameter("req", GET_DATA_FOR_VIEWEVENT).appendQueryParameter("eventId", Long.toString(eventId))
                    .appendQueryParameter("userId", Long.toString(userId)).appendQueryParameter("num_of_mes", Long.toString(numberOfMessages));

            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK) {
                String str = inputStreamToString(conn.getInputStream());
                Gson gson = new Gson();

                JSONObject jObject = new JSONObject(str);
                JSONArray jArrayM = jObject.getJSONArray("messages");
                JSONArray jArrayP = jObject.getJSONArray("players");

                for (int i=0; i < jArrayM.length(); i++)
                {
                    try {

                        String oneObject = (String)jArrayM.get(i);

                        data.messages.add(gson.fromJson(oneObject, Message.class));
                    } catch (JSONException e) {

                    }
                }

                for (int i=0; i < jArrayP.length(); i++)
                {
                    try {

                        String oneObject = (String)jArrayP.get(i);

                        data.players.add(gson.fromJson(oneObject, Player.class));
                    } catch (JSONException e) {

                    }
                }
            }
            else{
                data = null;
            }
        }
        catch(Exception e){
            e.printStackTrace();
            data = null;
        }
        return data;
    }


    public static String sendChatMessage(long eventId, String username, String message){//9
        String retStr = "";

        try {
            URL url = new URL(address + "//ServerMosis//login.php");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder().appendQueryParameter("req", SEND_CHAT_MESSAGE).appendQueryParameter("eventId", Long.toString(eventId))
                    .appendQueryParameter("username", username).appendQueryParameter("message", message);

            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                retStr = inputStreamToString(conn.getInputStream());
            }
            else{
                retStr=String.valueOf("Error:" + responseCode);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            retStr = "Error during sending, please try again!";
        }
        return retStr;
    }

    public static String signUpForEvent(long eventId, long userId, String username, SOEvent event){
        String retStr = "";

        Gson gson = new Gson();
        try {
            URL url = new URL(address + "//ServerMosis//login.php");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder().appendQueryParameter("req", SING_UP_FOR_EVENT).appendQueryParameter("eventId", Long.toString(eventId))
                    .appendQueryParameter("username", username).appendQueryParameter("userId", Long.toString(userId)).appendQueryParameter("event", gson.toJson(event));

            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                retStr = inputStreamToString(conn.getInputStream());
            }
            else{
                retStr=String.valueOf("Error:" + responseCode);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            retStr = "Error during sending, please try again!";
        }
        return retStr;

    }

    public static String signOffFromEvent(long eventId, long userId){
        String retStr = "";

        try {
            URL url = new URL(address + "//ServerMosis//login.php");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder().appendQueryParameter("req", SING_OFF_FROM_EVENT).appendQueryParameter("eventId", Long.toString(eventId))
                    .appendQueryParameter("userId", Long.toString(userId));

            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                retStr = inputStreamToString(conn.getInputStream());
            }
            else{
                retStr=String.valueOf("Error:" + responseCode);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            retStr = "Error during sending, please try again!";
        }
        return retStr;
    }

    //GET RATINGS //12
    public static ArrayList<Player> getPlayersRatings(int counter, long userId){
        String retStr = "";

        ArrayList<Player> players = new ArrayList<Player>();

        ArrayList<Player> users = new ArrayList<Player>();

        try {
            URL url = new URL(address + "//ServerMosis//login.php");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder().appendQueryParameter("req", GET_RATINGS)
                    .appendQueryParameter("userId", Long.toString(userId)).appendQueryParameter("counter", Long.toString(counter));


            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK) {
                String str = inputStreamToString(conn.getInputStream());

                JSONObject jObject = new JSONObject(str);
                JSONArray jArrayS = jObject.getJSONArray("scores");
                JSONArray jArrayP = jObject.getJSONArray("players");

                Gson gson = new Gson();


                for (int i=0; i < jArrayP.length(); i++)
                {
                    try {
                        String oneObject = jArrayP.get(i).toString();
                        players.add(gson.fromJson(oneObject, Player.class));
                    } catch (JSONException e) {

                    }
                }

                for (int i=0; i < jArrayS.length(); i++)
                {
                    try {
                        String oneObject = (String)jArrayS.get(i);

                        players.get(i).score = gson.fromJson(oneObject, Integer.class);

                    } catch (JSONException e) {

                    }
                }

            }
            else{
                players = null;
            }
        }
        catch(Exception e){
            e.printStackTrace();
            players = null;
        }
        return players;
    }


    //13
    public static HashMap<String, Object> getServiceEvents(long userId, String lat, String lon) {
        String retStr = "";

        HashMap<String, Object> ret = new HashMap<String, Object>();

        ArrayList<Integer> users;
        ArrayList<SOEvent> events;
        try {
            URL url = new URL(address + "//ServerMosis//login.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder().appendQueryParameter("req", SERVICE_EVENTS)
                    .appendQueryParameter("userId", Long.toString(userId)).appendQueryParameter("lat", lat).appendQueryParameter("lon", lon);


            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String str = inputStreamToString(conn.getInputStream());
                Gson gson = new Gson();

                JSONObject jObject = new JSONObject(str);
                JSONArray jArrayU = jObject.getJSONArray("users");
                JSONArray jArrayE = jObject.getJSONArray("events");

                users = new ArrayList<Integer>();
                events = new ArrayList<SOEvent>();

                for (int i=0; i < jArrayU.length(); i++)
                {
                    try {

                        users.add((Integer)jArrayU.get(i));
                    } catch (JSONException e) {

                    }
                }

                for (int i=0; i < jArrayE.length(); i++)
                {
                    try {

                        String oneObject = jArrayE.get(i).toString();

                        events.add(gson.fromJson(oneObject, SOEvent.class));

                    } catch (JSONException e) {

                    }
                }


            } else {
                users = null;
                events = null;

            }
        } catch (Exception e) {
            e.printStackTrace();
            users = null;
            events = null;
        }
        ret.put("users", users);
        ret.put("events", events);
        return ret;
    }

    //14: GET_EVENTS
    public static HashMap<String, Object> getEvents(long userId, String lat, String lon, int radius, ArrayList<Long> singedUpEventsIds) {
        String retStr = "";

        HashMap<String, Object> ret = new  HashMap<>();


        try {
            URL url = new URL(address + "//ServerMosis//login.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Gson gson = new Gson();

            Uri.Builder builder = new Uri.Builder().appendQueryParameter("req", GET_EVENTS)
                    .appendQueryParameter("userId", Long.toString(userId)).appendQueryParameter("lat", lat).appendQueryParameter("lon", lon)
                    .appendQueryParameter("radius", Integer.toString(radius)).appendQueryParameter("singedUpEventsIds", gson.toJson(singedUpEventsIds));


            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String str = inputStreamToString(conn.getInputStream());

                 ArrayList<SOEvent> events = new ArrayList<SOEvent>();
                 ArrayList<Long> ids = new ArrayList<Long>();

                JSONObject jObject = new JSONObject(str);

                Type idsType = new TypeToken<ArrayList<Long>>() {
                }.getType();
                ids = gson.fromJson(jObject.getString("ids"), idsType);

                for(int i =0; i< ids.size(); i++){
                    SOEventsData.getInstance().getSignedUpEventById(ids.get(i)).setFinished(true);
                }

                Type listType = new TypeToken<ArrayList<SOEvent>>() {
                }.getType();
               events = gson.fromJson(jObject.getString("events"), listType);

                ret.put("events", events);
                ret.put("ids", ids);

            } else {
                ret.put("events", null);
                ret.put("ids", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ret.put("events", null);
            ret.put("ids", null);
        }
        return ret;
    }

    //15: QUERY_EVENTS
    public static ArrayList<SOEvent> getQueriedEvents(String query1,boolean name, boolean category, boolean description) {
        String retStr = "";

        ArrayList<SOEvent> events = new ArrayList<SOEvent>();

        try {
            URL url = new URL(address + "//ServerMosis//login.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Gson gson = new Gson();

            Uri.Builder builder = new Uri.Builder().appendQueryParameter("req", QUERY_EVENTS)
                    .appendQueryParameter("query", query1).appendQueryParameter("name", String.valueOf(name))
                    .appendQueryParameter("category", String.valueOf(category)).appendQueryParameter("description", String.valueOf(description));


            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String str = inputStreamToString(conn.getInputStream());


                Type listType = new TypeToken<ArrayList<SOEvent>>() {
                }.getType();
                events = gson.fromJson(str, listType);

            } else {
             events = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            events = null;
        }
        return events;
    }

    //16 FINISH_EVENT
    public static String finishEvent(long eventId, long userId, SOEvent event){//16 //ispraviti
        String retStr = "";

        try {
            URL url = new URL(address + "//ServerMosis//login.php");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Gson gson = new Gson();

            Uri.Builder builder = new Uri.Builder().appendQueryParameter("req", FINISH_EVENT).appendQueryParameter("eventId", Long.toString(eventId))
                    .appendQueryParameter("userId", Long.toString(userId)).appendQueryParameter("event", gson.toJson(event));

            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                retStr = inputStreamToString(conn.getInputStream());
            }
            else{
                retStr=String.valueOf("Error:" + responseCode);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            retStr = "Error during sending, please try again!";
        }
        return retStr;
    }


    public static ArrayList<User> getPlayersFromEvent(Long eventId){//17GET_PLAYERS_FOR_EVENTS
        String retStr = "";

        ArrayList<User> players = new ArrayList<User>();

        try {
            URL url = new URL(address + "//ServerMosis//login.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Gson gson = new Gson();

            Uri.Builder builder = new Uri.Builder().appendQueryParameter("req", GET_PLAYERS_FOR_EVENT)
                    .appendQueryParameter("eventId", String.valueOf(eventId));


            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String str = inputStreamToString(conn.getInputStream());


                Type listType = new TypeToken<ArrayList<User>>() {
                }.getType();
                players = gson.fromJson(str, listType);

            } else {
                players = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            players = null;
        }
        return players;
    }


    //SUBMIT_PLAYER_RATINGS //18
    public static String submitPlayerRatings(ArrayList<Integer> ratings, ArrayList<Long> ids){
        String str = "";


        try {
            URL url = new URL(address + "//ServerMosis//login.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Gson gson = new Gson();

            Uri.Builder builder = new Uri.Builder().appendQueryParameter("req", SUBMIT_PLAYER_RATINGS)
                    .appendQueryParameter("ratings", gson.toJson(ratings)).appendQueryParameter("ids", gson.toJson(ids));


            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                str = inputStreamToString(conn.getInputStream());


            } else {
                str = "Failed";
            }
        } catch (Exception e) {
            e.printStackTrace();
            str = "Failed";
        }
        return str;
    }


    //LOG_OFF //19
    public static String logOffUser(Long id){
        String str = "";


        try {
            URL url = new URL(address + "//ServerMosis//login.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Gson gson = new Gson();

            Uri.Builder builder = new Uri.Builder().appendQueryParameter("req", LOG_OFF)
                    .appendQueryParameter("id", String.valueOf(id));


            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                str = inputStreamToString(conn.getInputStream());


            } else {
                str = "Failed";
            }
        } catch (Exception e) {
            e.printStackTrace();
            str = "Failed";
        }
        return str;
    }

}

class FriendLocation{
    public long id;
    public String lat;
    public String lon;
}



