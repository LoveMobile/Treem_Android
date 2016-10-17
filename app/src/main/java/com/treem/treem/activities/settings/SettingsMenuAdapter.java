package com.treem.treem.activities.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.treem.treem.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter for settings menu
 */
public class SettingsMenuAdapter extends ArrayAdapter<Integer>{
    //menu items
    private static final List<Integer> settingsItems = new ArrayList<>();

    //items with arrow
    private static final Set<Integer> settingsItemsExpandable = new HashSet<>();

    static {
        //init menu items
        settingsItems.add(R.string.settings_profile);
        settingsItems.add(R.string.settings_tree);
        settingsItems.add(R.string.settings_help);
        settingsItems.add(R.string.settings_logout);
        //init items with arrow
        settingsItemsExpandable.add(R.string.settings_profile);
        settingsItemsExpandable.add(R.string.settings_tree);
        settingsItemsExpandable.add(R.string.settings_help);
    }

    /**
     * Create instance of menu adapter
     * @param context base context
     */
    public SettingsMenuAdapter(Context context) {
        super(context, 0);
        addAll(settingsItems);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView==null){//is empty convert view
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.settings_row,parent,false); //create new one
            holder = new ViewHolder(convertView); //handle views from view row layout
        }
        else{
            holder = (ViewHolder)convertView.getTag();//get view holder reference from view's tag
        }
        //fill the view with data
        holder.build(position);
        return convertView;
    }

    /**
     * Hold row views
     */
    private class ViewHolder{
        //title of the menu item
        private TextView title;
        //arrow of the menu item
        private ImageView arrow;

        /**
         * Create instance of row holder
         * @param view the row view
         */
        public ViewHolder(View view){
            title = (TextView)view.findViewById(R.id.settingsTitle);
            arrow = (ImageView)view.findViewById(R.id.settingsArrow);
            //noinspection deprecation
            arrow.setColorFilter(getContext().getResources().getColor(R.color.dark_gray));
            view.setTag(this); //set this object reference to row tag
        }

        /**
         * Fill row view
         * @param position position of menu
         */
        public void build(int position){
            Integer titleId = getItem(position); //get title id
            boolean isArrowShow = settingsItemsExpandable.contains(titleId); //is need to show arrow?
            title.setText(titleId); //set title
            arrow.setVisibility(isArrowShow?View.VISIBLE:View.GONE); //show or hide the arrow
        }
    }
}
