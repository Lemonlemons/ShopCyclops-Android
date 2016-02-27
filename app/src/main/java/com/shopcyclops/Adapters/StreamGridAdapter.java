package com.shopcyclops.Adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shopcyclops.Fragments.Broadcast.Stream;
import com.shopcyclops.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * StreamAdapter connects a List of Streams
 * to an Adapter backed view like ListView or GridView
 */
public class StreamGridAdapter extends ArrayAdapter<Stream> {
    public static final int LAYOUT_ID = R.layout.stream_grid_item;
    private StreamAdapterActionListener mActionListener;
    private String mUsername;

    public StreamGridAdapter(final Context context, List<Stream> objects) {
        super(context, LAYOUT_ID, objects);

    }

    /**
     * Refresh the entire data structure underlying this adapter,
     * resuming the precise scroll state.
     *
     * @param listView
     * @param streams
     */
    public void refresh(AbsListView listView, List<Stream> streams) {
        Parcelable state = listView.onSaveInstanceState();
        clear();
        addAll(streams);
        notifyDataSetChanged();
        listView.onRestoreInstanceState(state);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Stream stream = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(LAYOUT_ID, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.image);
            holder.titleView = (TextView) convertView.findViewById(R.id.title);
            holder.liveBannerView = (TextView) convertView.findViewById(R.id.liveLabel);
            holder.rightTitleView = (TextView) convertView.findViewById(R.id.rightTitle);
            holder.overflowBtn = (ImageButton) convertView.findViewById(R.id.overflowBtn);
            holder.actions = convertView.findViewById(R.id.actions);
            convertView.setTag(holder);
            convertView.findViewById(R.id.overflowBtn).setOnClickListener(mOverflowBtnClickListener);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //// Hide the stream actions panel
        holder.actions.setVisibility(View.GONE);
        holder.overflowBtn.setTag(position);

        holder.liveBannerView.setVisibility(View.VISIBLE);
        holder.rightTitleView.setText(stream.getDescription());

        if (stream.getThumbnailUrl() != null && stream.getThumbnailUrl().compareTo("") != 0) {
            Glide.with(getContext())
                .load(stream.getThumbnailUrl())
                .placeholder(R.drawable.play)
                .error(R.drawable.play)
                .skipMemoryCache(true)
                .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.play);
        }
        holder.titleView.setText(stream.getTitle());

        return convertView;
    }

    public static class ViewHolder {
        ImageView imageView;
        TextView titleView;
        TextView liveBannerView;
        TextView rightTitleView;
        ImageButton overflowBtn;
        View actions;
    }

    private View.OnClickListener mOverflowBtnClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View overflowBtn) {
            // Toggle the Action container's visibility and set Tags on its subviews
            View listItemParent = ((View)overflowBtn.getParent());
            if (isActionContainerVisible(listItemParent)) {
                hideActionContainer(listItemParent);
            } else {
                View actionContainer = listItemParent.findViewById(R.id.actions);
                showActionContainer(listItemParent);
                // Transfer the overflowBtn tag to the two newly revealed buttons
                actionContainer.findViewById(R.id.shareBtn).setTag(overflowBtn.getTag());
                actionContainer.findViewById(R.id.shareBtn).setOnClickListener(mShareBtnClick);
            }
        }
    };

    //private View.OnClickListener mFlagBtnClick = new View.OnClickListener(){
    //
    //    @Override
    //    public void onClick(View flagBtn) {
    //        mActionListener.onFlagButtonClick(getItem((Integer) flagBtn.getTag()));
    //        hideActionContainer((View) flagBtn.getParent().getParent());
    //    }
    //};

    private View.OnClickListener mShareBtnClick = new View.OnClickListener(){

        @Override
        public void onClick(View shareBtn) {
            mActionListener.onShareButtonClick(getItem((Integer) shareBtn.getTag()));
            hideActionContainer((View) shareBtn.getParent().getParent());
        }
    };

    private boolean isActionContainerVisible(View listItemParent) {
        return listItemParent.findViewById(R.id.actions).getVisibility() == View.VISIBLE;
    }

    private void showActionContainer(View listItemParent) {
        View actionContainer = listItemParent.findViewById(R.id.actions);

        ObjectAnimator imageWashOut = ObjectAnimator.ofFloat(listItemParent.findViewById(R.id.image), "alpha", 1f, 0.4f);
        imageWashOut.setDuration(250);
        imageWashOut.start();

        actionContainer.setVisibility(View.VISIBLE);
    }

    private void hideActionContainer(View listItemParent) {
        View actionContainer = listItemParent.findViewById(R.id.actions);

        ObjectAnimator imageWashOut = ObjectAnimator.ofFloat(listItemParent.findViewById(R.id.image), "alpha", 0.4f, 1.0f);
        imageWashOut.setDuration(250);
        imageWashOut.start();

        actionContainer.setVisibility(View.GONE);
    }

    public static interface StreamAdapterActionListener {
        public void onShareButtonClick(Stream stream);
    }

}
