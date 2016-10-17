package com.treem.treem.activities.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ListView;

import com.treem.treem.R;

/**
 * Fragment with settings menu
 */
public class SettingsMenuFragment extends SettingsBaseFragment{
    public static final String TAG = SettingsMenuFragment.class.getSimpleName();

    // List view with menu items
    private ListView listSettings;

    //Adapter for list view with menu items
    private SettingsMenuAdapter menuAdapter;

    public SettingsMenuFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_menu, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        listSettings = (ListView)view.findViewById(R.id.listSettings);
        createAdapter();
    }

    /**
     * Create menu adapter
     */
    private void createAdapter() {
        menuAdapter = new SettingsMenuAdapter(getContext());
        listSettings.setAdapter(menuAdapter);
        listSettings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int itemId = menuAdapter.getItem(position);
                menuSelected(itemId);
            }
        });
    }


    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.toolbar_title_settings);
        setBackVisible(false); //hide back button for main fragment
    }

    /**
     * Override to disable transition animation on revert fragment
     */
    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (isDisableAnimation()){ //is animation disabled
            Animation a = new Animation() {}; //create empty animation with zero duration
            a.setDuration(0);
            return a;
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }
}
