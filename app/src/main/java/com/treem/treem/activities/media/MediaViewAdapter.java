package com.treem.treem.activities.media;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.treem.treem.R;
import com.treem.treem.models.content.Media;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Media view adapter
 */
public class MediaViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final OnMediaClickListener clickListener;


    /**
     * Interface to handle items click
     */
    public interface OnMediaClickListener{
        /**
         * On item clicked
         * @param media media object
         * @param v clicked view (image view)
         */
        void onMediaClick(Media media,View v);
    }
    //Types of items
    private static final int ITEM_DATA = 0; //item with data
    private static final int ITEM_LOADING = 1; //item with loader

    //Flag is data loading
    private boolean isLoading = false;

    /**
     * Layout inflater
     */
    private LayoutInflater inflater;

    /**
     * Data
     */
    private List<Media> data;

    /**
     * Reference to base context
     */
    private WeakReference<Context> contextRef;

    /**
     * Create new instance of the object
     * @param context base context
     * @param listener on item click listener
     */
    public MediaViewAdapter(Context context,OnMediaClickListener listener){
        contextRef = new WeakReference<>(context);
        inflater = LayoutInflater.from(context);
        clickListener = listener;
    }

    /**
     * Get base context
     * @return base context or null
     */
    @Nullable
    private Context getContext(){
        return contextRef.get();
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType==ITEM_DATA)
            return new MediaViewHolder(inflater.inflate(R.layout.media_item,parent,false));
        else
            return new ViewHolderLoading(inflater.inflate(R.layout.adapter_loading_row,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MediaViewHolder){
            bindDataViewHolder((MediaViewHolder)holder,position);
        }
    }

    /**
     * Bind data to data holder
     * @param holder view holder
     * @param position position of the holder
     */
    private void bindDataViewHolder(final MediaViewHolder holder, int position) {
        final Media media = data.get(position);
        Context context = getContext();
        if (context==null)
            return;
        holder.thumbnail.setImageResource(R.drawable.img_image);
        holder.loadingBar.setVisibility(View.VISIBLE);
        Picasso.with(context)
                .load(media.getStreamUrl())
                .error(R.drawable.img_image)
                .placeholder(R.drawable.img_image)
                .into(holder.thumbnail, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.loadingBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        holder.loadingBar.setVisibility(View.GONE);
                    }
                });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener!=null)
                    clickListener.onMediaClick(media,holder.thumbnail);
            }
        });

        if (media.getType()==Media.mediaTypeImage){ //handle image media
            holder.videoMark.setVisibility(View.GONE);
        }
        else { //handle other media. should be updated later
            holder.videoMark.setVisibility(View.VISIBLE);
        }
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

    public static class MediaViewHolder extends RecyclerView.ViewHolder{
        public ImageView thumbnail;
        public ImageView videoMark;
        public ProgressBar loadingBar;
        public MediaViewHolder(View itemView) {
            super(itemView);
            thumbnail = (ImageView)itemView.findViewById(R.id.thumbnail);
            videoMark = (ImageView)itemView.findViewById(R.id.videoMark);
            loadingBar = (ProgressBar)itemView.findViewById(R.id.progressLoad);
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
     * @param medias the medias list
     */
    public void setItems(List<Media> medias){
        data = new ArrayList<>(medias);
        notifyDataSetChanged();
    }

    /**
     * Add data to existing
     * @param medias new alerts
     */
    public void addData(List<Media> medias){
        if (data==null)
            data = new ArrayList<>();
        for (Media alert:medias){
            data.add(data.size(),alert);
            notifyItemInserted(data.size()-1);
        }
    }
    public void deleteItemId(long id) {
        for (int i=0;i<data.size();i++){
            if (data.get(i).getId()==id){
                data.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

}
