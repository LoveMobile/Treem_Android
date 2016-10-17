package com.treem.treem.activities.alerts;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.treem.treem.R;
import com.treem.treem.helpers.TimestampUtils;
import com.treem.treem.models.alert.Alert;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Recycler adapter to show alerts
 */
public class AlertsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //Types of items
    private static final int ITEM_DATA = 0; //item with data
    private static final int ITEM_LOADING = 1; //item with loader

    //data
    private List<Alert> data;

    private Set<Alert> checked = new HashSet<>();

    //Flag is data loading
    private boolean isLoading = false;

    //layout inflater
    private LayoutInflater inflater;

    //Reference for context
    private WeakReference<Context> contextRef;

    //Is need to show buttons (accept/decline)
    private boolean isButtonsShow = false;

    //Checked of color
    private int colorBack;
    //Checked on color
    private int colorMark;

    //Item clicks listeners
    //Checked view click listener
    private OnCheckedChangeListener onCheckedChangeListener;

    //Buttons click listener
    private OnButtonsClickListener onButtonClickListener;

    //Item click listener
    private OnItemClickListener onItemClickListener;

    /**
     * Mark selected items as read
     */
    public void markSelectedAsRead() {
        for (int i=0;i<data.size();i++){
            Alert idxAlert = data.get(i);
            if (checked.contains(idxAlert)){
                idxAlert.setViewed(true);
                checked.remove(idxAlert);
                notifyItemChanged(i);
            }
        }
    }

    /**
     * Remove selected items
     */
    public void removeSelected() {
        for (int idx = data.size()-1;idx>=0;idx--){
            Alert idxAlert = data.get(idx);
            if (checked.contains(idxAlert)){
                data.remove(idx);
                checked.remove(idxAlert);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Remove an alert
     * @param alert the alert to remove
     */
    public void removeAlert(Alert alert) {
        for (int idx = data.size()-1;idx>=0;idx--){
            Alert idxAlert = data.get(idx);
            if (idxAlert.getAlertId()==alert.getAlertId()){
                data.remove(idx);
                checked.remove(idxAlert);
                notifyItemRemoved(idx);
                return;
            }
        }

    }

    public void removeAlerts(Set<Alert> alerts) {
        if (alerts==null||alerts.size()==0)
            return;
        for (int idx = data.size()-1;idx>=0;idx--){
            Alert idxAlert = data.get(idx);
            for (Alert alert:alerts){
                if (alert.getAlertId()==idxAlert.getAlertId()){
                    data.remove(idx);
                    checked.remove(idxAlert);
                    break;
                }
            }
        }
        notifyDataSetChanged();

    }

    /**
     * Interface for check change event
     */
    public interface OnCheckedChangeListener{
        /**
         * On checked view click
         * @param alert alert clicked on
         * @param position position at the adapter
         * @param totalChecked - total checked alerts
         */
        void onCheckedClick(Alert alert, int position,int totalChecked);
    }

    /**
     * Interface for buttons click events
     */
    public interface OnButtonsClickListener{

        /**
         * On accept button click
         * @param alert alert clicked on
         * @param position position of the alert
         */
        void onAcceptButtonClick(Alert alert,int position);

        /**
         * On decline button click
         * @param alert alert clicked on
         * @param position the position of the alert
         */
        void onDeclineButtonClick(Alert alert,int position);
    }

    /**
     * Interface for item click listener
     */
    public interface OnItemClickListener {
        /**
         * On item click
         * @param alert alert clicked on
         * @param position position of the alert
         */
        void onItemClick(Alert alert,int position);

        /**
         * On item avatar click
         * @param alert alert clicked on
         * @param position position on the alert
         */
        void onAvatarClick(Alert alert,int position);
    }

    /**
     * Create new adapter instance
     * @param context base context
     * @param isButtonsShow is need to show buttons at this adapter
     */
    public AlertsAdapter(Context context,boolean isButtonsShow){
        contextRef = new WeakReference<>(context);
        inflater = LayoutInflater.from(context);
        //noinspection deprecation
        colorBack = context.getResources().getColor(R.color.mid_gray);
        //noinspection deprecation
        colorMark = context.getResources().getColor(R.color.colorAccent);
        this.isButtonsShow = isButtonsShow;
    }

    /**
     * Set on checked item click listener
     * @param listener listener
     */
    public void setOnCheckedClickListener(OnCheckedChangeListener listener){
        onCheckedChangeListener = listener;
    }

    /**
     * Set on button click listener
     * @param listener the listener
     */
    @SuppressWarnings("unused")
    public void setOnButtonClickListener(OnButtonsClickListener listener){
        onButtonClickListener = listener;
    }

    /**
     * Set on item click listener
     * @param listener listener
     */
    @SuppressWarnings("unused")
    public void setOnItemClickListener(OnItemClickListener listener){
        onItemClickListener = listener;
        notifyDataSetChanged(); //need notify adapter about changes
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType==ITEM_DATA)
            return new ViewHolderData(inflater.inflate(R.layout.alert_adapter_data_row,parent,false));
        else
            return new ViewHolderLoading(inflater.inflate(R.layout.adapter_loading_row,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolderData){
            bindDataViewHolder((ViewHolderData)holder,position);
        }
    }

    /**
     * Bind data to view holder
     * @param holder view holder
     * @param position position
     */
    private void bindDataViewHolder(final ViewHolderData holder, final int position) {
        final Alert alert = data.get(position); //get data
        Context context = contextRef.get(); //get context
        if (context!=null&&alert.getUserFrom()!=null){
            holder.avatar.setVisibility(View.VISIBLE);
                //show avatar
                Picasso.with(context)
                        .load(alert.getUserFrom().getAvatarStreamUrl())
                        .placeholder(R.drawable.img_avatar)
                        .error(R.drawable.img_avatar)
                        .into(holder.avatar);
        }
        else{
            holder.avatar.setVisibility(View.INVISIBLE);
        }
        //set date text
        holder.date.setText(TimestampUtils.friendlyFormatDate(context,TimestampUtils.parseDate(alert.getCreated())));
        //show read mark
        holder.readMark.setVisibility(alert.isViewed()?View.GONE:View.VISIBLE);

        //id item viewed or this is selected item - show selected mark
        if (checked.contains(alert)){
            holder.checkMark.setImageResource(R.drawable.ic_check_mark);
            holder.checkMark.setColorFilter(colorMark);
        }
        else{
            holder.checkMark.setImageResource(R.drawable.ic_check_back);
            holder.checkMark.setColorFilter(colorBack);

        }

        //check mark click listener
        holder.checkMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checked.contains(alert))
                    checked.remove(alert);
                else
                    checked.add(alert);
                notifyItemChanged(position);
                if (onCheckedChangeListener!=null)
                    onCheckedChangeListener.onCheckedClick(alert,position,checked.size());
            }
        });
        holder.description.setText(getDescription(context,alert));
        if (isButtonsShow){
            holder.buttons.setVisibility(View.VISIBLE);
            holder.buttonAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onButtonClickListener!=null)
                        onButtonClickListener.onAcceptButtonClick(alert,position);
                }
            });
            holder.buttonDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onButtonClickListener!=null)
                        onButtonClickListener.onDeclineButtonClick(alert,position);
                }
            });
        }
        else
            holder.buttons.setVisibility(View.GONE);
        if (onItemClickListener!=null){
            holder.avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener!=null)
                        onItemClickListener.onAvatarClick(alert,position);
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener!=null)
                        onItemClickListener.onItemClick(alert,position);
                }
            });
        }
    }

    /**
     * Get alert description
     * @param context context
     * @param alert alert
     * @return description for alert
     */
    private Spanned getDescription(Context context, Alert alert) {
        int resId=0;
        switch (alert.getReasonInt()){
            case Alert.REASON_ACCEPTED_FRIEND_INVITE:
                resId = R.string.alert_accepted_friend_invite;
                break;
            case Alert.REASON_ACCEPTED_FRIEND_REQUEST:
                resId = R.string.alert_accepted_friend_request;
                break;
            case Alert.REASON_BRANCH_SHARE:
                resId = R.string.alert_branch_share;
                break;
            case Alert.REASON_CHAT_UPLOAD_FINISHED:
                resId = R.string.alert_chat_upload_finished;
                break;
            case Alert.REASON_COMMENT_REPLY:
                resId = R.string.alert_comment_reply;
                break;
            case Alert.REASON_NON_REQUEST_ALERTS:
                resId = R.string.alert_non_request;
                break;
            case Alert.REASON_PENDING_FRIEND_REQUEST:
                resId = R.string.alert_pending_friend_request;
                break;
            case Alert.REASON_POST_ABUSE_REVOKED:
                resId = R.string.alert_post_abuse_revoked;
                break;
            case Alert.REASON_POST_ABUSE_SENT:
                resId = R.string.alert_post_abuse_sent;
                break;
            case Alert.REASON_POST_REACTION:
                resId = R.string.alert_post_reaction;
                break;
            case Alert.REASON_POST_REPLY:
                resId = R.string.alert_post_reply;
                break;
            case Alert.REASON_POST_REPLY_REACTION:
                resId = R.string.alert_post_reply_reaction;
                break;
            case Alert.REASON_POST_SHARE:
                resId = R.string.alert_post_share;
                break;
            case Alert.REASON_POST_UPLOAD_FINISHED:
                resId = R.string.alert_post_upload_finished;
                break;
            case Alert.REASON_REPLY_UPLOAD_FINISHED:
                resId = R.string.alert_reply_upload_finished;
                break;
            case Alert.REASON_TAGGED_POST:
                resId = R.string.alert_tagged_post;
                break;
        }
        return Html.fromHtml(context.getString(resId,alert.getUserFrom()!=null?alert.getUserFrom().getFirst()+" "+alert.getUserFrom().getLast():""));
    }

    /**
     * Get item count
     *
     * @return data item count, +1 when loading
     */
    @Override
    public int getItemCount() {
        return (data!=null?data.size():0)+(isLoading?1:0);
    }

    /**
     * Clear data
     */
    public void clear() {
        if (data!=null)
            data.clear();
        notifyDataSetChanged();
    }

    public static class ViewHolderData extends RecyclerView.ViewHolder{
        private ImageView avatar;
        private TextView date;
        private TextView readMark;
        private ImageView checkMark;
        private TextView description;
        private ViewGroup buttons;
        private AppCompatButton buttonAccept;
        private AppCompatButton buttonDecline;

        public ViewHolderData(View itemView) {
            super(itemView);
            avatar = (ImageView)itemView.findViewById(R.id.alertAvatar);
            date = (TextView)itemView.findViewById(R.id.alertDate);
            readMark = (TextView)itemView.findViewById(R.id.alertReadMark);
            checkMark = (ImageView) itemView.findViewById(R.id.alertCheckMark);
            description = (TextView)itemView.findViewById(R.id.alertDescription);
            buttons = (ViewGroup)itemView.findViewById(R.id.layoutAlertButtons);
            buttonAccept = (AppCompatButton) itemView.findViewById(R.id.alertButtonAccept);
            buttonDecline = (AppCompatButton) itemView.findViewById(R.id.alertButtonDecline);
        }
    }
    public static class ViewHolderLoading extends RecyclerView.ViewHolder{

        public ViewHolderLoading(View itemView) {
            super(itemView);
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (isLoading){
            if (data==null)
                return ITEM_LOADING;
            else
                return position==data.size()?ITEM_LOADING:ITEM_DATA;
        }
        else{
            return ITEM_DATA;
        }
    }

    /**
     * Show loading widget
     */
    public void startLoading(){
        if (isLoading)
            return;
        isLoading = true;
        notifyItemInserted(getItemCount());
    }

    /**
     * Hide loading widget
     */
    public void stopLoading(){
        if (!isLoading)
            return;
        isLoading = false;
        notifyItemRemoved(getItemCount()+1);
    }

    /**
     * Set new data
     * @param alerts the alerts list
     */
    public void setItems(List<Alert> alerts){
        checked.clear();
        data = new ArrayList<>(alerts);
        notifyDataSetChanged();
    }

    /**
     * Add data to existing
     * @param alerts new alerts
     */
    public void addData(List<Alert> alerts){
        if (data==null)
            data = new ArrayList<>();
        for (Alert alert:alerts){
            data.add(data.size(),alert);
            notifyItemInserted(data.size()-1);
        }
    }

    /**
     * Update item
     * @param alert alert to update
     */
    @SuppressWarnings("unused")
    public void updateItem(Alert alert){
        if (alert==null)
            return;
        int pos = data.indexOf(alert);
        if (pos>=0){
            notifyItemChanged(pos);
        }
    }

    /**
     * Get set of selected alerts
     * @return set of selected alerts
     */
    public Set<Alert> getSelected(){
        return new HashSet<>(checked);
    }

    /**
     * Clear selected alerts
     */
    public void clearSelection(){
        checked.clear();
        notifyDataSetChanged();
    }
}
