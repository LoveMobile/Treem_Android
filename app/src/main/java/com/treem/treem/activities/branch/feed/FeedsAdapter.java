package com.treem.treem.activities.branch.feed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.treem.treem.R;
import com.treem.treem.helpers.TimestampUtils;
import com.treem.treem.helpers.recyclerview.AdapterHelper;
import com.treem.treem.models.post.Post;
import com.treem.treem.models.post.Reaction;
import com.treem.treem.models.user.User;
import com.treem.treem.widget.RatioFrameLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter to show posts
 */
public class FeedsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_UNKNOWN = -1; //unknown type
    private static final int VIEW_TYPE_LOADING = 0; //loading indicator view
    private static final int VIEW_TYPE_TEXT = 1; //text post
    private static final int VIEW_TYPE_IMAGE = 2; //image post
    private static final int VIEW_TYPE_VIDEO = 3; //video post
    private static final int VIEW_TYPE_LINK = 4; //link post

    /**
     * Interface to handle post clicks
     */
    public interface OnFeedClickListener{
        /**
         * On profile image click
         * @param v profile image view
         * @param post clicked post
         * @param user clicked user
         */
        void onProfileClick(View v,Post post,User user);

        /**
         * On post image click
         * @param v clicked image
         * @param post clicked post
         */
        void onImageClick(View v,Post post);

        /**
         * On post video click
         * @param v clicked video thumbnail
         * @param post clicked post
         */
        void onVideoClick(View v,Post post);

        /**
         * On link preview click
         * @param v clicked preview view
         * @param post clicked post
         */
        void onLinkPreviewClick(View v, Post post);

        /**
         * On button options click
         * @param v clicked view
         * @param post clicked post
         * @param isShare is share options click
         */
        void onOptionsClick(View v,Post post,boolean isShare);
    }

    //Is data loading flag
    private boolean isLoading = false;

    //Is loading indicator need to show
    private boolean isShowIndicator = false;

    //Data
    private List<Post> data;

    //Feeds users. Keys - user ids, values - users
    private Map<Long,User> users = new HashMap<>();

    //Reference to context
    private WeakReference<Context> contextRef;

    //Layout inflater
    private LayoutInflater inflater;

    //On post clicked listener
    private OnFeedClickListener onClickListener;

    //View holder for loading indicator
    public static class ViewHolderLoading extends RecyclerView.ViewHolder{

        public ViewHolderLoading(View itemView) {
            super(itemView);
        }
    }

    //View holder for data posts
    public class ViewHolderData extends RecyclerView.ViewHolder{
        //User avatar
        private ImageView avatar;

        //User name
        private TextView name;

        //post text
        private TextView text;

        //Post data
        private TextView date;

        //Options button
        @SuppressWarnings("unused")
        private ViewGroup options;

        //Comment button
        @SuppressWarnings("unused")
        private ViewGroup commentsButton;

        //Comment button text
        private TextView commentsText;

        //React button
        @SuppressWarnings("unused")
        private ViewGroup reactButton;

        //Rect button text
        @SuppressWarnings("unused")
        private TextView reactText;

        //Share button
        private ViewGroup shareButton;

        //Share button text
        private TextView shareText;

        //Layout to show user's reacts
        private LinearLayout layoutReacts;

        //Root view for post
        private ViewGroup rootPost;

        //Root view for share header
        private ViewGroup rootShareHeader;

        //Share user avatar
        private ImageView postShareAvatar;

        //Share user name
        private TextView postShareName;

        //Share date
        private TextView postShareDate;

        //Share text
        private TextView postShareText;

        //Share options button
        @SuppressWarnings("unused")
        private ViewGroup postShareOptions;

        //Layout to show branch colors
        private LinearLayout colors;

        //Layout to show share branch colors
        private LinearLayout shareColors;

        //Layout with tagged icon
        private ViewGroup tagged;

        public ViewHolderData(View itemView) {
            super(itemView);
            avatar = (ImageView)itemView.findViewById(R.id.post_avatar);
            name = (TextView)itemView.findViewById(R.id.post_name);
            text = (TextView)itemView.findViewById(R.id.postText);
            options = (ViewGroup) itemView.findViewById(R.id.post_options);
            date = (TextView)itemView.findViewById(R.id.post_date);
            commentsButton = (ViewGroup)itemView.findViewById(R.id.buttonComments);
            commentsText = (TextView)itemView.findViewById(R.id.textComments);
            reactButton = (ViewGroup)itemView.findViewById(R.id.buttonReact);
            reactText = (TextView)itemView.findViewById(R.id.textReact);
            shareButton = (ViewGroup)itemView.findViewById(R.id.buttonShare);
            shareText = (TextView)itemView.findViewById(R.id.textShare);
            layoutReacts = (LinearLayout)itemView.findViewById(R.id.layoutReact);
            rootPost = (ViewGroup)itemView.findViewById(R.id.layoutPost);
            rootShareHeader = (ViewGroup)itemView.findViewById(R.id.shareRoot);
            postShareAvatar = (ImageView)itemView.findViewById(R.id.share_avatar);
            postShareName = (TextView)itemView.findViewById(R.id.share_name);
            postShareText = (TextView)itemView.findViewById(R.id.share_text);
            postShareDate = (TextView)itemView.findViewById(R.id.share_date);
            postShareOptions = (ViewGroup)itemView.findViewById(R.id.share_options);
            colors = (LinearLayout)itemView.findViewById(R.id.postColors);
            shareColors = (LinearLayout)itemView.findViewById(R.id.shareColors);
            tagged = (ViewGroup)itemView.findViewById(R.id.tagged);
        }

        /**
         * Bind data to views
         * @param post post to bind
         */
        public void bind(final Post post) {
            if (post.isSharedPost()){ //is post shared
                //Show shared data
                rootShareHeader.setVisibility(View.VISIBLE);
                rootPost.setBackgroundResource(R.drawable.share_post_background); //set post background to gray

                loadAvatar(postShareAvatar,post.getShareUserId());
                postShareName.setText(getShareName(post.getUserId(),post.getShareUserId()));
                postShareDate.setText(TimestampUtils.friendlyFormatDateTime(getContext(), TimestampUtils.parseDate(post.getShareDate())));
                if (!TextUtils.isEmpty(post.getShareText())) {
                    postShareText.setVisibility(View.VISIBLE);
                    postShareText.setText(post.getShareText());
                }
                else {
                    postShareText.setVisibility(View.GONE);
                }
                if (post.getShareUserId()!=null)
                    AdapterHelper.fillColors(contextRef.get(),shareColors,users.get(post.getShareUserId()));
                colors.setVisibility(View.GONE);
                postShareAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onClickListener!=null)
                            onClickListener.onProfileClick(v,fillPost(post),post.getShareUserId()!=null?users.get(post.getShareUserId()):null);
                    }
                });
            }
            else{
                //Hide share header and set background to white
                rootShareHeader.setVisibility(View.GONE);
                rootPost.setBackgroundColor(Color.WHITE);
                if (post.getUserId()!=null)
                    AdapterHelper.fillColors(contextRef.get(),colors,users.get(post.getUserId()));
            }
            //show tagged layout if user tagged
            tagged.setVisibility(post.isUserTagged()?View.VISIBLE:View.GONE);
            loadAvatar(avatar,post.getUserId());
            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener!=null)
                        onClickListener.onProfileClick(v,fillPost(post),post.getUserId()!=null?users.get(post.getUserId()):null);
                }
            });

            name.setText(getUserName(post.getUserId()));
            date.setText(TimestampUtils.friendlyFormatDateTime(getContext(), TimestampUtils.parseDate(post.getDate())));
            if (!TextUtils.isEmpty(post.getText())) {
                text.setVisibility(View.VISIBLE);
                text.setText(post.getText());
            }
            else {
                text.setVisibility(View.GONE);
            }
            //Update footer buttons
            Context context = getContext();
            if (post.getCommentsCount()>0&&context!=null){
                String comment = context.getString(R.string.comment);
                comment+=" ("+post.getCommentsCount()+")";
                commentsText.setText(comment);
            }
            else{
                commentsText.setText(R.string.comment);
            }
            if (post.getSharesCount()>0&&context!=null){
                shareButton.setVisibility(View.VISIBLE);
                String shares = context.getString(R.string.share);
                shares+=" ("+post.getSharesCount()+")";
                shareText.setText(shares);
            }
            else{
                if (post.isShareable()) {
                    shareButton.setVisibility(View.VISIBLE);
                    shareText.setText(R.string.share);
                }
                else{
                    shareButton.setVisibility(View.GONE);
                }
            }
            //Fill react layout
            fillReactLayout(post.getReacts());
            options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener!=null)
                        onClickListener.onOptionsClick(v,fillPost(post),false);
                }
            });
            postShareOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener!=null)
                        onClickListener.onOptionsClick(v,fillPost(post),true);
                }
            });

        }
        /**
         * Fill react layout
         * @param reacts the list of user's reacts
         */
        @SuppressLint("SetTextI18n")
        private void fillReactLayout(List<Reaction> reacts) {
            if (reacts==null||reacts.size()==0){ //hide react layout on empty reacts
                layoutReacts.setVisibility(View.GONE);
            }
            else { //show and fill if not empty
                layoutReacts.setVisibility(View.VISIBLE);
                layoutReacts.removeAllViews();
                for (Reaction reaction:reacts){
                    int imgRes = getReactImage(reaction.getReact());
                    if (imgRes==0)
                        continue;
                    int color = getReactColor(reaction.getReact());
                    View v = inflater.inflate(R.layout.layout_react,layoutReacts,false);
                    ImageView img = (ImageView)v.findViewById(R.id.react);
                    img.setImageResource(imgRes);
                    TextView txt = (TextView)v.findViewById(R.id.react_count);
                    txt.setText(Integer.toString(reaction.getCount()));
                    txt.setTextColor(color);
                    layoutReacts.addView(v);
                }
            }
        }

        /**
         * Get react image
         * @param react index of react
         * @return resource drawable for react index
         */
        private int getReactImage(int react) {
            switch (react){
                case 0:
                    return R.drawable.ic_react_happy;
                case 2:
                    return R.drawable.ic_react_angry;
                case 3:
                    return R.drawable.ic_react_sad;
                case 6:
                    return R.drawable.ic_react_funny;
                case 7:
                    return R.drawable.ic_react_amazed;
                case 8:
                    return R.drawable.ic_react_confused;
                default:
                    return R.drawable.ic_react_neutral;
            }

        }

        /**
         * Get react color
         * @param react react index
         * @return react color
         */
        private int getReactColor(int react) {
            switch (react){
                case 0:
                    return Color.rgb(120,160,40);
                case 2:
                    return Color.rgb(162,32,34);
                case 3:
                    return Color.rgb(114,182,219);
                case 6:
                    return Color.rgb(243,112,50);
                case 7:
                    return Color.rgb(149,142,192);
                case 8:
                    return Color.rgb(104,67,25);
                default:
                    return Color.rgb(61,67,60);
            }
        }

        /**
         * Get user name for selected user
         * @param userId user id
         * @return user name
         */
        private String getUserName(Long userId) {
            if (userId!=null){
                User user = users.get(userId);
                if (user!=null)
                    return user.getName();
            }
            return "";
        }

        /**
         * Get share user name for selected user and share user
         * @param userId post user
         * @param shareUserId share post user
         * @return text for share name
         */
        private String getShareName(Long userId, Long shareUserId) {
            Context context = getContext();
            if (userId!=null&&shareUserId!=null&&context!=null) {
                User user = users.get(userId);
                User sUser = users.get(shareUserId);
                return context.getString(R.string.post_share_name,user.getName(),sUser.getName());
            }
            return "";
        }

        /**
         * Load user avatar
         * @param view image view to load avatar
         * @param userId user id
         */
        private void loadAvatar(ImageView view, Long userId) {
            Context context = getContext();
            if (userId==null||context==null){
                view.setImageBitmap(null);
                return;
            }
            User user = users.get(userId);
            String url = null;
            if (user!=null)
                url = user.getAvatarStreamUrl();

            Picasso.with(context)
                    .load(url)
                    .placeholder(R.drawable.img_avatar)
                    .error(R.drawable.img_avatar)
                    .into(view);

        }
    }

    /**
     * View holder for media posts
     */
    public class ViewHolderDataMedia extends ViewHolderData{
        //thumbnail view
        protected ImageView thumbnail;

        //empty image view
        protected ImageView emptyView;

        //Ratio layout for view
        protected RatioFrameLayout frameThumbnail;
        public ViewHolderDataMedia(View itemView) {
            super(itemView);
            thumbnail = (ImageView)itemView.findViewById(R.id.thumbnail);
            frameThumbnail = (RatioFrameLayout)itemView.findViewById(R.id.thumbnailFrame);
            emptyView = (ImageView)itemView.findViewById(R.id.empty_view);
        }

        @Override
        public void bind(Post post) {
            super.bind(post);
            Context context = getContext();
            int w = post.getContentWidth();
            int h = post.getContentHeight();
            if (w==0||h==0){ //if width or height==0 - set ratio to zero - standard behaviour for frame layout
                frameThumbnail.setRatio(RatioFrameLayout.ZERO_RATIO);
            }
            else{
                float ratio = (float)w/(float)h; //calc ratio
                frameThumbnail.setRatio(ratio); //set ratio
            }
            if (context!=null) { //load image
                emptyView.setVisibility(View.VISIBLE);
                thumbnail.setVisibility(View.GONE);
                Picasso.with(context)
                        .load(post.getContentStreamUrl())
                        .placeholder(android.R.color.transparent)
                        .into(thumbnail, new Callback() {
                            @Override
                            public void onSuccess() {
                                emptyView.setVisibility(View.GONE);
                                thumbnail.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError() {

                            }
                        });
            }
        }
    }

    /**
     * View holder for video posts
     */
    public class ViewHolderDataVideo extends ViewHolderDataMedia {
        public ViewHolderDataVideo(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(final Post post) {
            super.bind(post);
            //Handle clicks
            frameThumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener!=null){
                        onClickListener.onVideoClick(thumbnail,fillPost(post));
                    }
                }
            });
        }
    }

    /**
     * View holder for images posts
     */
    public class ViewHolderDataImage extends ViewHolderDataMedia {
        public ViewHolderDataImage(View itemView) {
            super(itemView);
        }
        @Override
        public void bind(final Post post) {
            super.bind(post);
            //Handle clicks
            frameThumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener!=null){
                        onClickListener.onImageClick(thumbnail,fillPost(post));
                    }
                }
            });
        }

    }

    /**
     * Add user and share user to users for the post
     * @param post post to fill
     * @return post with filled users
     */
    private Post fillPost(Post post){
        if (post.getUserId()!=null)
            post.addUser(users.get(post.getUserId()));
        if (post.getShareUserId()!=null)
            post.addUser(users.get(post.getShareUserId()));
        return post;
    }

    /**
     * View holder for links posts
     */
    public class ViewHolderDataLink extends ViewHolderData{
        //link title
        private TextView title;

        //link description
        private TextView description;

        //lik image
        private ImageView image;

        //root view for link preview
        private CardView root;

        public ViewHolderDataLink(View itemView) {
            super(itemView);
            root = (CardView)itemView.findViewById(R.id.linkRoot);
            title = (TextView)itemView.findViewById(R.id.linkTitle);
            description = (TextView)itemView.findViewById(R.id.linkDescription);
            image = (ImageView)itemView.findViewById(R.id.linkImage);
        }

        @Override
        public void bind(final Post post) {
            super.bind(post);
            //set link data
            if (post.getLinkTitle()!=null) {
                title.setVisibility(View.VISIBLE);
                title.setText(post.getLinkTitle());
            }
            else{
                title.setVisibility(View.GONE);
            }
            if (post.getLinkDescription()!=null) {
                description.setVisibility(View.VISIBLE);
                description.setText(post.getLinkDescription());
            }
            else{
                description.setVisibility(View.GONE);
            }
            image.setVisibility(View.GONE);
            Context context = getContext();
            //load image
            if (post.getLinkImage()!=null&&context!=null){
                Picasso.with(context)
                        .load(post.getLinkImage())
                        .placeholder(android.R.color.transparent)
                        .into(image, new Callback() {
                            @Override
                            public void onSuccess() {
                                image.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError() {

                            }
                        });

            }

            //handle clicks
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener!=null)
                        onClickListener.onLinkPreviewClick(v,fillPost(post));
                }
            });
        }
    }

    /**
     * Create new instance of feed adapter
     * @param context base context
     * @param listener click listener
     */
    public FeedsAdapter(Context context,OnFeedClickListener listener) {
        contextRef = new WeakReference<>(context);
        inflater = LayoutInflater.from(context);
        onClickListener = listener;
    }

    /**
     * Get context from weak reference
     * @return context
     */
    private Context getContext(){
        return contextRef.get();
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_LOADING:
                return new ViewHolderLoading(inflater.inflate(R.layout.adapter_loading_row, parent, false));
            case VIEW_TYPE_IMAGE:
                return new ViewHolderDataImage(inflater.inflate(R.layout.adapter_post_image,parent,false));
            case VIEW_TYPE_VIDEO:
                return new ViewHolderDataVideo(inflater.inflate(R.layout.adapter_post_video,parent,false));
            case VIEW_TYPE_LINK:
                return new ViewHolderDataLink(inflater.inflate(R.layout.adapter_post_link,parent,false));
            default:
                return new ViewHolderData(inflater.inflate(R.layout.adapter_post_text,parent,false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolderData){
            Post post = data.get(position);
            ((ViewHolderData)holder).bind(post);
        }
    }

    @Override
    public int getItemCount() {
        int add = isLoading&&isShowIndicator?1:0; //add +1 when showing loading indicator
        return data==null?add:data.size()+add;
    }

    /**
     * Is adapter in loading state
     * @return true when adapter in loading state
     */
    public boolean isLoading() {
        return isLoading;
    }

    /**
     * Set state to loading
     * @param isShowIndicator is need to show loading indicator
     */
    public void startLoading(boolean isShowIndicator) {
        isLoading = true;
        this.isShowIndicator = isShowIndicator;
        if (isShowIndicator)
            notifyItemInserted(data!=null?data.size():0);
    }

    /**
     * Set state to not loading
     */
    public void stopLoading() {
        isLoading = false;
        if (isShowIndicator) //hide indicator if needed
            notifyItemRemoved(data!=null?data.size():0);
    }

    /**
     * Set new data to adapter
     * @param posts list of new posts
     */
    public void setData(List<Post> posts) {
        if (posts!=null){
            for (Post post:posts){
                if (post.getUsers()!=null){ //get users from post and add to map
                    for (User user:post.getUsers()){
                        users.put(user.getId(),user);
                    }
                }
            }
        }
        //set data and refresh
        data = posts;
        notifyDataSetChanged();
    }

    /**
     * Add new posts to existing
     * @param posts list new posts
     */
    public void addNewData(List<Post> posts) {
        if (posts==null)
            return;
        for (Post post:posts){
            if (post.getUsers()!=null){ //get posts users and add them to map
                for (User user:post.getUsers()){
                    users.put(user.getId(),user);
                }
            }
        }
        //add data
        if (data==null)
            data = new ArrayList<>();
        data.addAll(data.size()-(isLoading&&isShowIndicator?1:0),posts);
        notifyItemRangeInserted(data.size()-posts.size()-(isLoading&&isShowIndicator?1:0),posts.size());
    }

    @Override
    public int getItemViewType(int position) {
        if (position==getItemCount()-1&&isLoading&&isShowIndicator)
            return VIEW_TYPE_LOADING;
        else{
            if (data==null||data.size()<position)
                return VIEW_TYPE_UNKNOWN;
            Post post = data.get(position);
            if (post.isImageContent())
                return VIEW_TYPE_IMAGE;
            else if (post.isVideoContent())
                return VIEW_TYPE_VIDEO;
            else if (post.isLinkPost()){
                return VIEW_TYPE_LINK;
            }
            else
                return VIEW_TYPE_TEXT;
        }
    }
}
