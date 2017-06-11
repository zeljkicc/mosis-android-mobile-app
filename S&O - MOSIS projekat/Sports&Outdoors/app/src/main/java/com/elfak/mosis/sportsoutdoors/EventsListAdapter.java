package com.elfak.mosis.sportsoutdoors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class EventsListAdapter extends RecyclerView.Adapter<EventsListAdapter.ViewHolder>{
    private static ArrayList<SOEvent> mDataset;

    Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextViewName, mTextViewType, mTextViewDescription;
        public ViewHolder(View v) {
            super(v);
            mTextViewName = (TextView) v.findViewById(R.id.event_card_name);
            mTextViewType = (TextView) v.findViewById(R.id.event_card_type);
            mTextViewDescription = (TextView) v.findViewById(R.id.event_card_description);

            v.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    int pos = getAdapterPosition();
                    Intent viewEventActivity = new Intent(v.getContext(), ViewEventActivity.class);
                    viewEventActivity.putExtra("eventId", String.valueOf(pos));
                    if(SOEventsData.getInstance().near){
                        viewEventActivity.putExtra("from_near_list", true);

                    }

                    SOEventsData.searchEvent = mDataset.get(pos);


                    ((Activity)v.getContext()).startActivityForResult(viewEventActivity, EventsListActivity.VIEW_EVENT);


                }
            });
        }
    }

    public EventsListAdapter(Context context, ArrayList<SOEvent> myDataset) {
        mDataset = myDataset;
        mContext = context;
    }

    @Override
    public EventsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.events_list_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(EventsListAdapter.ViewHolder holder, int position) {

       SOEvent event = mDataset.get(position);
        if(mDataset.get(position).getFinished() != true)
            holder.mTextViewName.setText(event.getName());
        else holder.mTextViewName.setText("This event was finished: \n" + event.getName());
        holder.mTextViewType.setText(event.getType());
        holder.mTextViewDescription.setText(event.getDescription());

if(mDataset.get(position).getFinished() == true && SOEventsData.getInstance().search == false)
        ((CardView) holder.mTextViewDescription.getParent().getParent()).setCardBackgroundColor(Color.parseColor("#ffd27f"));

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
