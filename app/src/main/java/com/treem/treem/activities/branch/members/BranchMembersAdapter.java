package com.treem.treem.activities.branch.members;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.treem.treem.R;
import com.treem.treem.helpers.recyclerview.AdapterHelper;
import com.treem.treem.models.user.User;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter for branch member screen
 */
public class BranchMembersAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //Types of items
    private static final int ITEM_DATA = 0; //item with data
    private static final int ITEM_LOADING = 1; //item with loader

    //On item click listener
    private OnItemClickListener onItemClickListener;

    //On item checked change listener
    private OnCheckedChangeListener onCheckedChangeListener;


    /**
     * Interface for check change event
     */
    public interface OnCheckedChangeListener{
        /**
         * On checked view click
         * @param user user clicked on
         * @param position position at the adapter
         */
        void onCheckedClick(User user, int position);
    }


    /**
     * Interface for item click listener
     */
    public interface OnItemClickListener {
        /**
         * On item avatar click
         * @param user user clicked on
         * @param position position at the adapter
         */
        void onAvatarClick(User user,int position);
    }

    //Flag is data loading
    private boolean isLoading = false;

    //adapter data
    private List<User> data;

    //layout inflater
    private LayoutInflater inflater;

    //Items checked for add
    private Set<User> checkAdd = new HashSet<>();

    //Items unchecked for remove
    private Set<User> checkRemove = new HashSet<>();

    //Reference for context
    private WeakReference<Context> contextRef;

    //Checked of color
    private int colorBack;

    //Checked on color
    private int colorMark;

    //background color of the checked row
    private int colorBackgroundSelected;

    //is this a main members view
    private final boolean isMainMembersView;


    /**
     * Create new adapter instance
     * @param context base context
     * @param isMainMembers true if this is main members view
     */
    public BranchMembersAdapter(Context context,boolean isMainMembers){
        contextRef = new WeakReference<>(context);
        inflater = LayoutInflater.from(context);
        //noinspection deprecation
        colorBack = context.getResources().getColor(R.color.mid_gray);
        //noinspection deprecation
        colorMark = context.getResources().getColor(R.color.colorAccent);

        //noinspection deprecation
        colorBackgroundSelected = context.getResources().getColor(R.color.selected_row_color);


        isMainMembersView = isMainMembers;
    }

    /**
     * Get item count
     *
     * @return data item count, +1 when loading
     */
    @Override
    public int getItemCount() {
        return (data != null ? data.size() : 0) + (isLoading ? 1 : 0);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_DATA)
            return new ViewHolderData(inflater.inflate(R.layout.members_adapter_data_row, parent, false));
        else
            return new ViewHolderLoading(inflater.inflate(R.layout.adapter_loading_row, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolderData) {
            bindDataViewHolder((ViewHolderData) holder, position);
        }
    }

    /**
     * Bind data to view holder
     *
     * @param holder   view holder
     * @param position position
     */
    private void bindDataViewHolder(final ViewHolderData holder, final int position) {
        final User user = data.get(position); //get data
        Context context = contextRef.get(); //get context
        if (context!=null){
            //show avatar
            Picasso.with(context)
                    .load(user.getAvatarStreamUrl())
                    .placeholder(R.drawable.img_avatar)
                    .error(R.drawable.img_avatar)
                    .into(holder.avatar);
        }

        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener!=null)
                    onItemClickListener.onAvatarClick(user,position);
            }
        });

        //id item viewed or this is selected item - show selected mark
        if (isMainMembersView&&user.getStatus()==User.STATUS_FRIENDS){
            holder.checkMark.setEnabled(false);
            holder.checkMark.setImageResource(R.drawable.ic_check_mark);
            holder.checkMark.setColorFilter(colorMark);
            holder.rowRoot.setBackgroundColor(Color.WHITE);

        }else {
            holder.checkMark.setEnabled(true);
            if (checkAdd.contains(user) || (user.getOnBranch()&&!checkRemove.contains(user))) {
                holder.checkMark.setImageResource(R.drawable.ic_check_mark);
                holder.checkMark.setColorFilter(colorMark);
                holder.rowRoot.setBackgroundColor(colorBackgroundSelected);
            } else {
                holder.checkMark.setImageResource(R.drawable.ic_check_back);
                holder.checkMark.setColorFilter(colorBack);
                holder.rowRoot.setBackgroundColor(Color.WHITE);

            }
        }
        //check mark click listener
        holder.checkMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getOnBranch()){
                    if (checkRemove.contains(user)){
                        checkRemove.remove(user);
                    }
                    else{
                        checkRemove.add(user);
                    }
                }
                else{
                    if (checkAdd.contains(user)){
                        checkAdd.remove(user);
                    }
                    else{
                        checkAdd.add(user);
                    }
                }
                if (onCheckedChangeListener!=null)
                    onCheckedChangeListener.onCheckedClick(user,position);
                notifyItemChanged(position);
            }
        });
        holder.name.setText(user.getName());
        switch (user.getStatus()){
            case User.STATUS_FRIENDS:
                holder.status.setImageResource(R.drawable.img_friends);
                break;
            case User.STATUS_INVITED:
                holder.status.setImageResource(R.drawable.img_pending);
                break;
            case User.STATUS_PENDING:
                holder.status.setImageResource(R.drawable.img_pending);
                break;
            default:
                holder.status.setImageBitmap(null);

        }
        if (user.isTreemUser()){
            if (user.getContact()!=null){
                String contactName = user.getContact().getName();
                if (contactName!=null){
                    contactName = " - "+contactName;
                    holder.contactName.setText(contactName);
                }
            }
            else
                holder.contactName.setText("");
            holder.hexagon.setVisibility(View.VISIBLE);
            holder.username.setText(user.getUsername());
            holder.phone.setVisibility(View.GONE);
            AdapterHelper.fillColors(context,holder.colors,user);
        }
        else {
            holder.contactName.setText("");
            holder.hexagon.setVisibility(View.GONE);
            holder.username.setText("");
            holder.colors.setVisibility(View.GONE);
        }
        if (user.getContact()!=null&&user.getContact().getPhone()!=null) {
            holder.phone.setVisibility(View.VISIBLE);
            holder.phone.setText(user.getContact().getPhone());
        }
        else{
            holder.phone.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemViewType(int position) {
        if (isLoading) {
            if (data == null)
                return ITEM_LOADING;
            else
                return position == data.size() ? ITEM_LOADING : ITEM_DATA;
        } else {
            return ITEM_DATA;
        }
    }

    public static class ViewHolderData extends RecyclerView.ViewHolder {
        private ImageView avatar;
        private ImageView checkMark;
        private TextView name;
        private TextView contactName;
        private ImageView status;
        private ImageView hexagon;
        private TextView username;
        private TextView phone;
        private LinearLayout colors;
        private ViewGroup rowRoot;

        public ViewHolderData(View itemView) {
            super(itemView);
            avatar = (ImageView)itemView.findViewById(R.id.userAvatar);
            checkMark = (ImageView) itemView.findViewById(R.id.checkMark);
            name = (TextView)itemView.findViewById(R.id.memberName);
            contactName = (TextView)itemView.findViewById(R.id.contactName);
            status = (ImageView)itemView.findViewById(R.id.memberIcon);
            hexagon = (ImageView)itemView.findViewById(R.id.hexagon);
            username = (TextView)itemView.findViewById(R.id.memberUsername);
            phone = (TextView)itemView.findViewById(R.id.memberPhone);
            colors = (LinearLayout)itemView.findViewById(R.id.memberColors);
            rowRoot = (ViewGroup)itemView.findViewById(R.id.rowRoot);
        }
    }
    public static class ViewHolderLoading extends RecyclerView.ViewHolder{

        public ViewHolderLoading(View itemView) {
            super(itemView);
        }
    }
    /**
     * Clear data
     */
    public void clear() {
        if (data!=null)
            data.clear();
        notifyDataSetChanged();
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
     * @param users the users list
     */
    public void setItems(List<User> users){
        checkAdd.clear();
        checkRemove.clear();
        if (users==null)
            data = new ArrayList<>();
        else
            data = new ArrayList<>(users);
        notifyDataSetChanged();
    }

    /**
     * Add data to existing
     * @param users new users
     */
    public void addData(List<User> users){
        if (data==null)
            data = new ArrayList<>();
        if (users!=null) {
            for (User user : users) {
                data.add(data.size(), user);
                notifyItemInserted(data.size() - 1);
            }
        }
    }

    /**
     * Update item
     * @param user user to update
     */
    @SuppressWarnings("unused")
    public void updateItem(User user){
        if (user==null)
            return;
        int pos = data.indexOf(user);
        if (pos>=0){
            notifyItemChanged(pos);
        }
    }

    /**
     * Get set of selected users to add
     * @return set of selected users
     */
    public Set<User> getSelectedAdd(){
        return new HashSet<>(checkAdd);
    }
    /**
     * Get set of selected users to remove
     * @return set of selected users
     */
    public Set<User> getSelectedRemove(){
        return new HashSet<>(checkRemove);
    }

    /**
     * Clear selected to add users
     */
    public void clearSelectionAdd(){
        checkAdd.clear();
        if (onCheckedChangeListener!=null)
            onCheckedChangeListener.onCheckedClick(null,-1);

        notifyDataSetChanged();
    }
    /**
     * Clear selected to remove users
     */
    public void clearSelectionRemove(){
        checkRemove.clear();
        if (onCheckedChangeListener!=null)
            onCheckedChangeListener.onCheckedClick(null,-1);

        notifyDataSetChanged();
    }

    /**
     * Set on item click listener
     * @param listener on item click listener
     */
    public void setOnItemClickListener(OnItemClickListener listener){
        onItemClickListener = listener;
    }

    /**
     * Set on item checked change listener
     * @param listener listener
     */
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener){
        onCheckedChangeListener = listener;
    }

    /**
     * Select all possible items
     */
    public void selectAll(){
        checkAdd.clear();
        checkRemove.clear();
        if (data!=null){
            for (User u:data){
                if (isMainMembersView&&u.getStatus()==User.STATUS_FRIENDS)
                    continue;
                if (!u.getOnBranch())
                    checkAdd.add(u);

            }
        }
        if (onCheckedChangeListener!=null)
            onCheckedChangeListener.onCheckedClick(null,-1);
        notifyDataSetChanged();
    }

    /**
     * Unselect all possible items
     */
    public void deselectAll(){
        checkAdd.clear();
        checkRemove.clear();
        if (data!=null){
            for (User u:data){
                if (isMainMembersView&&u.getStatus()==User.STATUS_FRIENDS)
                    continue;
                if (u.getOnBranch())
                    checkRemove.add(u);

            }
        }
        if (onCheckedChangeListener!=null)
            onCheckedChangeListener.onCheckedClick(null,-1);
        notifyDataSetChanged();
    }

    /**
     * Reset selection
     */
    public void reset(){
        checkAdd.clear();
        checkRemove.clear();
        if (onCheckedChangeListener!=null)
            onCheckedChangeListener.onCheckedClick(null,-1);
        notifyDataSetChanged();
    }

    /**
     * Clear on branch flag for selected items and clear remove selection
     */
    public void clearOnBranch() {
        if (checkRemove!=null&&checkRemove.size()>0){
            for (User u:checkRemove)
                u.setOnBranch(false);
            checkRemove.clear();
        }

        notifyDataSetChanged();
    }

    /**
     * Set unbranch and update user status on success add
     */
    public void updateStatusAndOnBranch(){
        if (checkAdd!=null&&checkAdd.size()>0) {
            if (isMainMembersView) {
                for (User u:checkAdd){
                    if (u.isTreemUser()) {
                        switch (u.getStatus()) {
                            case User.STATUS_INVITED:
                                u.setStatus(User.STATUS_FRIENDS);
                                break;
                            case User.STATUS_NOT_FRIENDS:
                                u.setStatus(User.STATUS_PENDING);
                                break;

                        }
                    }
                    else{
                        u.setStatus(User.STATUS_PENDING);
                    }
                }
            } else {
                for (User u:checkAdd){
                    u.setOnBranch(true);
                }
            }
            checkAdd.clear();
        }
        notifyDataSetChanged();
    }

}
