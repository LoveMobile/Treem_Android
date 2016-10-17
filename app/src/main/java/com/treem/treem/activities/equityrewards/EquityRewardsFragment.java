package com.treem.treem.activities.equityrewards;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.Size;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.util.DisplayDimensions;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.treem.treem.R;
import com.treem.treem.activities.main.PageBaseFragment;
import com.treem.treem.activities.users.UserProfileActivity;
import com.treem.treem.application.CurrentTreeSettings;
import com.treem.treem.helpers.NotificationHelper;
import com.treem.treem.helpers.TimestampUtils;
import com.treem.treem.helpers.security.ProgressBar.LoadingProgressBar;
import com.treem.treem.models.rollout.Rollout;
import com.treem.treem.models.user.Historical;
import com.treem.treem.services.Treem.TreemEquityService;
import com.treem.treem.services.Treem.TreemService;
import com.treem.treem.services.Treem.TreemServiceRequest;
import com.treem.treem.services.Treem.TreemServiceResponseCode;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;


/**
 * Rewards page
 */
public class EquityRewardsFragment extends PageBaseFragment {
    @SuppressWarnings("unused")
    private static final String TAG = EquityRewardsFragment.class.getSimpleName();

    /**
     * Limit of top friends
     */
    private static final int limitTopFriends = 5;

    //Color reward bar
    private RewardsBar barReward;

    //Hexagon view
    private ImageView imageHexagon;

    //Chart view
    private XYPlot rewardChart;

    //Text with percents
    private TextView textPercent;

    //Text with points and word points
    private TextView textPointsValue;
    private TextView textPoints;

    //Points description text
    private TextView textDescription;

    //Friends layout
    private LinearLayout layoutFriends;

    //Root view layout
    private ViewGroup rootLayout;

    //Failed load data layout
    private View layoutFailedLoad;

    //load progress bar
    private LoadingProgressBar loadProgressBar;

    /**
     * Chart period selection
     */
    private RadioButton radio30Days;

    //Waiting history layout
    private ViewGroup waitingHistory;

    //Reference to base activity
    @SuppressWarnings("unused")
    private OnRewardsFragmentInteractionListener mListener;

    //Loaded user data
    private Rollout userData;

    //Loaded friends data
    @SuppressWarnings("unused")
    private List<Rollout> friendsData;

    List<Number> historyDates = new ArrayList<>();
    List<Number> historyValues = new ArrayList<>();

    private boolean isRolloutRequestCompleted = false;
    private boolean isFriendRequestCompleted = false;
    private boolean isHistoryRequestCompleted = false;
    private boolean isFailedRequestExists = false;
    private TreemService.NetworkRequestTask historyTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //inflate failed to load layout
        layoutFailedLoad = inflater.inflate(R.layout.layout_rewards_failed_load,container,false);
        return inflater.inflate(R.layout.fragment_equityrewards, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        barReward = (RewardsBar)view.findViewById(R.id.bar_reward);
        imageHexagon = (ImageView)view.findViewById(R.id.img_hexagon);
        rewardChart = (XYPlot)view.findViewById(R.id.rewardsChart);
        textPercent = (TextView)view.findViewById(R.id.textRewardPercent);
        textPoints = (TextView)view.findViewById(R.id.textRewardPointsWord);
        textPointsValue = (TextView)view.findViewById(R.id.textRewardPoints);
        textDescription = (TextView)view.findViewById(R.id.textRewardDescription);
        layoutFriends = (LinearLayout)view.findViewById(R.id.layoutFriends);
        rootLayout = (ViewGroup)view.findViewById(R.id.layoutRewardsRoot);
        loadProgressBar = new LoadingProgressBar(rootLayout);
        radio30Days = (RadioButton)view.findViewById(R.id.radio_30_days);
        radio30Days.setChecked(true);

        RadioGroup groupPeriod = (RadioGroup) view.findViewById(R.id.groupPeriod);
        waitingHistory = (ViewGroup)view.findViewById(R.id.historyWaiting);

        //handle reload data button
        Button reload = (Button)layoutFailedLoad.findViewById(R.id.buttonReload);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadValues();
            }
        });
        groupPeriod.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                requestHistoryData();
            }
        });
        //check is page primary - load data if true
        if (isFragmentPrimary())
            loadValues();
    }

    /**
     * Load user and friends data
     */
    private void loadValues() {
        isFriendRequestCompleted = false;
        isRolloutRequestCompleted = false;
        isFailedRequestExists = false;
        if (!loadProgressBar.isShown())
            loadProgressBar.toggleProgressBar(true);
        showFailed(false);
        requestUserData();
        requestHistoryData();
        requestFriendsData();
    }

    private void requestHistoryData() {
        //Load history data
        TreemServiceRequest requestHistory = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                historyTask = null;
                isHistoryRequestCompleted = true;
                if (!isFragmentPrimary())
                    return;
                List<Historical> history = new ArrayList<>();
                if (data!=null){
                    try{
                        history = new Gson().fromJson(data,Historical.LIST_TYPE);
                    }
                    catch (JsonSyntaxException e){
                        e.printStackTrace();
                    }
                }
                updateHistoricalData(history);
                if (userData!=null)
                    fillChart(); //fill friends layout
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                isHistoryRequestCompleted = true;
                if (error==TreemServiceResponseCode.CANCELED)
                    return;
                updateHistoricalData(null);
                if (userData!=null)
                    fillChart();
            }
        };
        if (historyTask!=null&&!historyTask.isCancelled())
            historyTask.cancel(true);
        isHistoryRequestCompleted = false;
        Calendar calendar = GregorianCalendar.getInstance();
        String endDate = TimestampUtils.getFormattedDate(calendar.getTime());
        String startDate = null;
        if (radio30Days.isChecked()){
            calendar.add(Calendar.DAY_OF_YEAR,-30+1);
            startDate = TimestampUtils.getFormattedDate(calendar.getTime());
        }
        waitingHistory.setVisibility(View.VISIBLE);
        //start load friends
        historyTask = TreemEquityService.getHistoricalData(
                requestHistory,
                TreemService.scaleDay,
                startDate,
                endDate,
                CurrentTreeSettings.SHARED_INSTANCE.treeSession);

    }

    private void updateHistoricalData(List<Historical> data) {
        historyDates = new ArrayList<>();
        historyValues = new ArrayList<>();
        if (data==null){
            Calendar c = GregorianCalendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY,0);
            c.set(Calendar.MINUTE,0);
            c.set(Calendar.SECOND,0);
            c.set(Calendar.MILLISECOND,0);
            historyDates.add(c.getTimeInMillis());
            historyValues.add(0);
            c.add(Calendar.DAY_OF_YEAR,-29);
            historyDates.add(c.getTimeInMillis());
            historyValues.add(0);
        }
        else {
            for (int i = 0; i < data.size(); i++) {
                Historical h = data.get(i);
                Date dt = TimestampUtils.parseDate(h.period_start);
                historyDates.add(dt.getTime());
                historyValues.add((int)h.point_sum);
            }
            if (historyDates.size()==1){
                long dt = historyDates.get(0).longValue();
                Calendar c = GregorianCalendar.getInstance();
                c.setTimeInMillis(dt);
                c.add(Calendar.DAY_OF_YEAR,-1);
                historyDates.add(c.getTimeInMillis());
                historyValues.add(0);
            }
        }
    }

    private void requestFriendsData() {
        //Load friends data
        TreemServiceRequest requestFriends = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                if (isFriendRequestCompleted)
                    return;
                isFriendRequestCompleted = true;
                checkCompletion();
                if (!isFragmentPrimary())
                    return;
                if (data!=null&&!"\"\"".equals(data)){
                    try{
                        friendsData = new Gson().fromJson(data,Rollout.LIST_TYPE);
                        fillFriends(); //fill friends layout
                    }
                    catch (JsonSyntaxException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                isFriendRequestCompleted = true;
                isFailedRequestExists = true;
                checkCompletion();
            }
        };
        //start load friends
        TreemEquityService.getTopFriends(requestFriends,limitTopFriends, CurrentTreeSettings.SHARED_INSTANCE.treeSession);
    }

    private void requestUserData() {

        //Start to load user data first
        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                if (isRolloutRequestCompleted)
                    return;
                isRolloutRequestCompleted = true;
                checkCompletion();
                if (!isFragmentPrimary()) { //if not primary - return
                    return;
                }
                if (data!=null){
                    try{
                        //set user data
                        userData = new Gson().fromJson(data,Rollout.class);
                    }
                    catch (JsonSyntaxException e){
                        e.printStackTrace();
                        NotificationHelper.showError(getContext(),R.string.failed_parse_server_answer);
                    }
                }
                else{
                    NotificationHelper.showError(getContext(),R.string.failed_load_user_equity_data);
                }
                fillViews(); //fill views
                if (isHistoryRequestCompleted)
                    fillChart();
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                isRolloutRequestCompleted = true;
                isFailedRequestExists = true;
                checkCompletion();
                if (!isFragmentPrimary())
                    return;
                fillViews();
                if (isHistoryRequestCompleted)
                    fillChart();
            }
        };
        //start load user data
        TreemEquityService.getUserRollout(request, CurrentTreeSettings.SHARED_INSTANCE.treeSession);
    }

    private void checkCompletion() {
        if (!isAdded())
            return;
        if (isFriendRequestCompleted&&isRolloutRequestCompleted){
            loadProgressBar.toggleProgressBar(false);
            if (isFailedRequestExists)
                showFailed(true);
        }
    }

    /**
     * Show failed load layout
     * @param isShow is need to show or hide the layout
     */
    private void showFailed(boolean isShow) {
        if (isShow)
            rootLayout.addView(layoutFailedLoad);
        else
            rootLayout.removeView(layoutFailedLoad);
    }


    /**
     * Fill top rated friends
     */
    private void fillFriends() {
        if (friendsData!=null&&friendsData.size()>0){ //if friends loaded
            layoutFriends.removeAllViews(); //remove all views
            LayoutInflater inflater = LayoutInflater.from(getContext());
            for (Rollout user:friendsData){ //and fill with friends
                View v = inflater.inflate(R.layout.layout_reward_friend,layoutFriends,false);
                fillFriend(v,user);
                layoutFriends.addView(v);
            }
        }
    }

    /**
     * Inflate user layout and fill with data
     * @param v friend user layout
     * @param user friend data
     */
    private void fillFriend(View v, final Rollout user) {
        TextView text = (TextView)v.findViewById(R.id.friend_name);
        text.setText(user.getUserName());
        text = (TextView)v.findViewById(R.id.friend_points);
        text.setText(getString(R.string.reward_friend_points,Math.round(user.getPoints())));
        text = (TextView)v.findViewById(R.id.friend_percent);
        text.setText(getString(R.string.reward_friend_percents,Math.round(user.gerPercent())));
        text.setTextColor(getPercentColor(user.gerPercent())); //set text according percent status
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfileActivity.showUserProfile(getActivity(),user.getUserId());
            }
        });
    }

    /**
     * Fill fragment views
     */
    private void fillViews() {
        if (userData!=null&&isAdded()) { //user data loaded?
            barReward.setPointer(userData.gerPercent());
            imageHexagon.setColorFilter(getPercentColor(userData.gerPercent()));
            String percent = Long.toString(Math.round(userData.gerPercent()));
            textPercent.setText(percent);
            String points = Long.toString(Math.round(userData.getPoints()));
            textPointsValue.setText(points);
            textPointsValue.setTextColor(getPercentColor(userData.gerPercent()));
            textPoints.setText(getResources().getQuantityText(R.plurals.rewards_points, (int) userData.getPoints()));
            textDescription.setText(getDescription());
        }
    }

    /**
     * Get description about percents and points
     * @return string description
     */
    private String getDescription() {
        if (userData.gerPercent()<80)
            return getString(R.string.reward_not_money,userData.gerPercent());
        else
            return getString(R.string.reward_in_money,userData.gerPercent());
    }

    /**
     * Get color according percent
     * @param percent percent value
     * @return color for percent
     */
    @SuppressWarnings("deprecation")
    private int getPercentColor(double percent){
        if (percent<=20)
            return getResources().getColor(R.color.reward_bar_segment_1);
        else if (percent<=40)
            return getResources().getColor(R.color.reward_bar_segment_2);
        else if (percent<=60)
            return getResources().getColor(R.color.reward_bar_segment_3);
        else if (percent<=80)
            return getResources().getColor(R.color.reward_bar_segment_4);
        else
            return getResources().getColor(R.color.reward_bar_segment_5);

    }

    /**
     * Fill chart with values
     */
    private void fillChart() {
        waitingHistory.setVisibility(View.GONE);
        if (historyValues.size()==0)
            return;
        Number min = historyValues.get(0);
        Number max = historyValues.get(0);
        for (Number n:historyValues){
            if (n.intValue()<min.intValue()){
                min = n;
            }
            else if (n.intValue()>max.intValue()) {
                max = n;
            }
        }
        if (max.intValue()-min.intValue()<10){
            min = max.intValue()-10;
        }
        if (min.intValue()<0){
            min = 0;
            max = 10;
        }
        //Create data values series for a chat
        XYSeries series = new SimpleXYSeries(
                historyDates,
                historyValues,
                "Rewards");
        //noinspection deprecation
        int lineColor = (userData!=null?getPercentColor(userData.gerPercent()):getResources().getColor(R.color.dark_gray));
        int fillColor = Color.argb(30,
                Color.red(lineColor),
                Color.green(lineColor),
                Color.blue(lineColor));
        //noinspection deprecation
        LineAndPointFormatter formatter = new LineAndPointFormatter(
                lineColor
                , 0, fillColor, null
        );

        //clear previously data
        rewardChart.clear();
        //add new data
        rewardChart.addSeries(series, formatter);

        Number n1 = historyDates.get(0);
        Number n2 = historyDates.get(historyDates.size()-1);
        int days =(int)((n2.longValue()-n1.longValue())/1000.0/60.0/60.0/24.0+0.5)+1;
        if (Math.abs(days)>10)
            days = 10;
        //set domain legend
        rewardChart.setDomainStep(XYStepMode.SUBDIVIDE, days);

        rewardChart.setRangeStep(XYStepMode.SUBDIVIDE, 6);
        rewardChart.setRangeBoundaries(min, max, BoundaryMode.FIXED);

        //set value format for a range
        rewardChart.setRangeValueFormat(new DecimalFormat("0"));
        //set domain value format to custom format
        rewardChart.setDomainValueFormat(new Format() {

            // create a simple date format that draws on the year portion of our timestamp.
            // see http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
            // for a full description of SimpleDateFormat.
            private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());

            @Override
            public StringBuffer format(Object obj, @NonNull StringBuffer toAppendTo, @NonNull FieldPosition pos) {

                long timestamp = ((Number) obj).longValue();
                Date date = new Date(timestamp);
                return dateFormat.format(date, toAppendTo, pos);
            }

            @Override
            public Object parseObject(String source, @NonNull ParsePosition pos) {
                return null;

            }
        });
        //set background color to white
        rewardChart.setBackgroundColor(Color.WHITE);

        //This is the magic!

        //get graph widget
        XYGraphWidget widget = rewardChart.getGraphWidget();
        //update it's position and size
        widget.position(-0.5f, XLayoutStyle.RELATIVE_TO_RIGHT,
                -0.5f, YLayoutStyle.RELATIVE_TO_BOTTOM,
                AnchorPosition.CENTER);

        widget.setSize(new Size(
                0, SizeLayoutType.FILL,
                0, SizeLayoutType.FILL));

        //Create the rect to fill the whole widget
        RectF rect = new RectF(0,0,rewardChart.getWidth(),rewardChart.getHeight());
        //set graph widget layout to this rect
        widget.layout(new DisplayDimensions(rect,rect,rect));
        //refresh layout
        rewardChart.getLayoutManager().remove(rewardChart.getLegendWidget());
        rewardChart.getLayoutManager().remove(rewardChart.getDomainLabelWidget());
        rewardChart.getLayoutManager().remove(rewardChart.getRangeLabelWidget());
        rewardChart.getLayoutManager().remove(rewardChart.getTitleWidget());
        widget.refreshLayout(); //refresh layout with new rect
        rewardChart.redraw(); //redraw the widget
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRewardsFragmentInteractionListener) {
            mListener = (OnRewardsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRewardsFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }


    public interface OnRewardsFragmentInteractionListener{
    }

    /**
     * Called on set fragment primary
     * @param isPrimary true if page selected or false otherwise
     */
    @Override
    protected void setFragmentPrimary(boolean isPrimary) {
        super.setFragmentPrimary(isPrimary);
        if (!isPrimary) { //is not primary
            userData = null; //clear data
            friendsData = null;
        }
        else //load data on primary
            loadValues();
    }
}
