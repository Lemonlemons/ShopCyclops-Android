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
import com.shopcyclops.Fragments.Broadcast.Stream;
import com.shopcyclops.Fragments.Delivery.Order;
import com.shopcyclops.R;

import java.text.NumberFormat;
import java.util.List;

/**
 * Created by Andrew on 10/24/2015.
 */
public class OrderListAdapter extends ArrayAdapter<Order> {
    public static final int LAYOUT_ID = R.layout.order_list_item;
    private Context activity;
    private List data;
    private static LayoutInflater inflater=null;
    NumberFormat format = NumberFormat.getCurrencyInstance();

    public OrderListAdapter(final Context context, List<Order> objects) {
        super(context, LAYOUT_ID, objects);
        activity = context;
        data = objects;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void refresh(ListView listView, List<Order> orders) {
        Parcelable state = listView.onSaveInstanceState();
        clear();
        addAll(orders);
        notifyDataSetChanged();
        listView.onRestoreInstanceState(state);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null) {
            vi = inflater.inflate(R.layout.order_list_item, null);
        }

        TextView title = (TextView)vi.findViewById(R.id.orderTitle);
        TextView totalQuantity = (TextView)vi.findViewById(R.id.orderQuantity);
        TextView totalPrice = (TextView)vi.findViewById(R.id.orderPrice);
        ImageView statusLight = (ImageView)vi.findViewById(R.id.statusCircle);

        Order order = (Order) data.get(position);

        // Setting all values in listview
        title.setText(order.getStream_title());
        totalQuantity.setText(String.valueOf(order.getTotalQuantity()));
        totalPrice.setText(format.format(order.getTotalprice()));

        if (order.isIs_delivered() == true) {
            statusLight.setImageResource(R.drawable.red_circle);
        } else {
            statusLight.setImageResource(R.drawable.green_circle);
        }

        return vi;
    }
}
