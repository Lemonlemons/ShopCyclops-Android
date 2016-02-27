package com.shopcyclops.Adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shopcyclops.Fragments.Broadcast.Stream;
import com.shopcyclops.R;

public class StreamListAdapter extends ArrayAdapter<Stream> {

    public static final int LAYOUT_ID = R.layout.stream_list_item;
    private Context activity;
    private List data;
    private static LayoutInflater inflater=null;

    public StreamListAdapter(final Context context, List<Stream> objects) {
        super(context, LAYOUT_ID, objects);
        activity = context;
        data = objects;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void refresh(ListView listView, List<Stream> streams) {
        Parcelable state = listView.onSaveInstanceState();
        clear();
        addAll(streams);
        notifyDataSetChanged();
        listView.onRestoreInstanceState(state);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null) {
            vi = inflater.inflate(R.layout.stream_list_item, null);
        }

        TextView title = (TextView)vi.findViewById(R.id.streamTitle); // title
        TextView description = (TextView)vi.findViewById(R.id.streamDescription); // artist name
        ImageView thumb_image = (ImageView)vi.findViewById(R.id.list_image); // thumb image

        Stream stream = (Stream) data.get(position);

        // Setting all values in listview
        title.setText(stream.getTitle());
        description.setText(stream.getDescription());
        if (stream.getThumbnailUrl() != null && stream.getThumbnailUrl().compareTo("") != 0) {
            Glide.with(getContext())
                    .load(stream.getThumbnailUrl())
                    .placeholder(R.drawable.play)
                    .error(R.drawable.play)
                    .skipMemoryCache(true)
                    .into(thumb_image);
        } else {
            thumb_image.setImageResource(R.drawable.play);
        }

        return vi;
    }
}