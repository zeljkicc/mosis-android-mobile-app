package com.elfak.mosis.sportsoutdoors;

import java.util.ArrayList;

public class SOEventsData {

    SOEventsDBAdapter dbAdapter;

    private ArrayList<SOEvent> soEvents;

    static SOEvent searchEvent;

    public boolean near = true;
    public static boolean search = false;

    private ArrayList<SOEvent> signedUpEvents;

    private SOEventsData(){
        soEvents = new ArrayList<SOEvent>();

        searchEvent = null;

        signedUpEvents = new ArrayList<SOEvent>();

        dbAdapter = new SOEventsDBAdapter(MyApplication.getContext());
        dbAdapter.open();
        this.signedUpEvents = dbAdapter.getAllEntries();
        dbAdapter.close();

    }



    private static class SingletonHolder{
        public static final SOEventsData instance = new SOEventsData();
    }

    public static SOEventsData getInstance(){
        return SingletonHolder.instance;
    }

    public ArrayList<SOEvent> getSoEvents(){
        return soEvents;
    }

    public void setSoEvents(ArrayList<SOEvent> events){

        this.soEvents = events;
    }

    public void setSignedUpEvents(ArrayList<SOEvent> events){

    }

    public ArrayList<SOEvent> getSignedUpEvents(){
       return this.signedUpEvents ;
    }

    public void addNewEvent(SOEvent event){
        soEvents.add(event);
    }

    public SOEvent getSoEvent(int index){
        return soEvents.get(index);
    }

    public void deleteSOEvent(int index){
        soEvents.remove(index);
    }


    public void addSignedUpEvent(SOEvent event){
        dbAdapter.open();
        long dbID = dbAdapter.insertEntry(event);
        dbAdapter.close();
        event.setID(dbID);
        signedUpEvents.add(event);
    }

    public boolean removeSignedUpEvent(Long id){
        for(SOEvent event : this.signedUpEvents){
            if(id == event.getId()){
                this.signedUpEvents.remove(event);

                dbAdapter.open();
                dbAdapter.removeEntry(event.getmID());
                dbAdapter.close();

                return true;
            }
        }
        return false;

    }

    public SOEvent getSignedUpEventById(Long id){
        for(SOEvent event : this.signedUpEvents){
            if(id == event.getId()){



                return event;
            }
        }
        return null;

    }

    public SOEvent getSignedUpEvent(int index){
        return signedUpEvents.get(index);
    }

    public ArrayList<Long> getSignedEventsIds(){
        ArrayList<Long> list = new ArrayList<>();
        for(SOEvent e : signedUpEvents){
            list.add(e.getId());
        }

        return list;
    }

    public void updateSignedEvent(int index, SOEvent event){

       signedUpEvents.set(index, event);

        dbAdapter.open();
        dbAdapter.updateEntry(event.getmID(), event);
        dbAdapter.close();
    }


}
