package com.shopcyclops.Adapters;

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
import com.shopcyclops.Fragments.Cart.CartItem;
import com.shopcyclops.R;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Andrew on 9/27/2015.
 */
public class DeliveryListAdapter extends ArrayAdapter<CartItem> {

    public static final int LAYOUT_ID = R.layout.stream_list_item;
    private Context activity;
    private List data;
    private static LayoutInflater inflater=null;

    public DeliveryListAdapter(final Context context, List<CartItem> objects) {
        super(context, LAYOUT_ID, objects);
        activity = context;
        data = objects;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void refresh(ListView listView, List<CartItem> items) {
        Parcelable state = listView.onSaveInstanceState();
        clear();
        addAll(items);
        notifyDataSetChanged();
        listView.onRestoreInstanceState(state);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null) {
            vi = inflater.inflate(R.layout.delivery_list_item, null);
        }

        TextView title = (TextView)vi.findViewById(R.id.DeliveryTitle); // title
        TextView price = (TextView)vi.findViewById(R.id.priceDeliveryText); // artist name
        TextView quantity = (TextView)vi.findViewById(R.id.quantityDeliveryText);
        TextView status = (TextView)vi.findViewById(R.id.statusDeliveryText);
        TextView listposition = (TextView)vi.findViewById(R.id.DeliveryListPosition);
//        ImageView thumb_image = (ImageView)vi.findViewById(R.id.list_image); // thumb image

        CartItem item = (CartItem) data.get(position);

        // Setting all values in listview
        title.setText(item.getItemname());
        price.setText(String.valueOf(item.getPrice()));
        quantity.setText(String.valueOf(item.getQuantity()));
        status.setText(item.getStatus());
        listposition.setText(String.valueOf(position + 1));
//        if (item.getImageurl() != null && item.getImageurl().compareTo("") != 0) {
//            Glide.with(getContext())
//                    .load(item.getImageurl())
//                    .placeholder(R.drawable.play)
//                    .error(R.drawable.play)
//                    .skipMemoryCache(true)
//                    .into(thumb_image);
//        } else {
//            thumb_image.setImageResource(R.drawable.play);
//        }

        return vi;
    }
}
