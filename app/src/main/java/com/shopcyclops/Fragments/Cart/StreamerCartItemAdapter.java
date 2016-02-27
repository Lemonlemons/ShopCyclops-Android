package com.shopcyclops.Fragments.Cart;

/**
 * Created by Andrew on 7/29/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.shopcyclops.R;
import com.shopcyclops.SECRETS;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;

/********* Adapter class extends with BaseAdapter and implements with OnClickListener ************/
public class StreamerCartItemAdapter extends BaseAdapter implements View.OnClickListener {

    /*********** Declare Used Variables *********/
    private Activity activity;
    private ArrayList data;
    private static LayoutInflater inflater=null;
    public Resources res;
    CartItem tempValues=null;
    int i=0;
    TextView placeholdertext;
    NumberFormat format;

    /*************  CustomAdapter Constructor *****************/
    public StreamerCartItemAdapter(Activity a, ArrayList d, Resources resLocal) {

        /********** Take passed values **********/
        activity = a;
        data=d;
        res = resLocal;
        format = NumberFormat.getCurrencyInstance();

        /***********  Layout inflator to call external xml layout () ***********/
        inflater = ( LayoutInflater )activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    /******** What is the size of Passed Arraylist Size ************/
    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    /********* Create a holder Class to contain inflated xml file elements *********/
    public static class ViewHolder{

        public TextView contents;
        public TextView quantity;
        public EditText price;
        public TextView status;
        public ImageView image;
        public ImageButton cancelbutton;
        public ImageButton cartbutton;

    }

    /****** Depends upon data size called for each row , Create each ListView row *****/
    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        ViewHolder holder;

        if(convertView==null){

            /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
            vi = inflater.inflate(R.layout.streamer_cart_list_item, null);

            /****** View Holder Object to contain tabitem.xml file elements ******/

            holder = new ViewHolder();
            holder.contents = (TextView) vi.findViewById(R.id.textView);
            holder.quantity = (TextView) vi.findViewById(R.id.quantityText);
            holder.status = (TextView) vi.findViewById(R.id.statusText);
            holder.price = (EditText) vi.findViewById(R.id.editpriceText);
            holder.cancelbutton = (ImageButton) vi.findViewById(R.id.xbutton);
            holder.cartbutton = (ImageButton) vi.findViewById(R.id.cartbutton);
            /************  Set holder with LayoutInflater ************/
            vi.setTag( holder );
        }
        else {
            holder = (ViewHolder) vi.getTag();
        }

        if(data.size()<=0)
        {
//            holder.itemname.setText("No Data");
//            holder.quantity.setText("1");
        }
        else
        {
            /***** Get each Model object from Arraylist ********/
            tempValues = null;
            tempValues = (CartItem) data.get( position );

            if (tempValues.getProgress() == 1) {
                holder.contents.setText( tempValues.getItemname() );
                holder.quantity.setText( tempValues.getQuantity().toString() );
                holder.status.setText( tempValues.getStatus() );
                holder.price.setText("");
                holder.price.setEnabled(true);
                holder.cancelbutton.setVisibility(View.VISIBLE);
                holder.cancelbutton.setOnClickListener(new OnCancelButtonListener( tempValues ));
                holder.cartbutton.setOnClickListener(new CartedButtonListener(tempValues, holder.price, holder.quantity));
                holder.cartbutton.setBackgroundColor(res.getColor(R.color.CyclopsDarkRed));
                holder.cartbutton.setImageResource(R.drawable.shopping_cart);
            }
            else if (tempValues.getProgress() == 2) {
                holder.contents.setText( tempValues.getItemname() );
                holder.quantity.setText( tempValues.getQuantity().toString() );
                holder.status.setText( tempValues.getStatus() );
                holder.price.setText( format.format(tempValues.getPrice()) );
                holder.price.setEnabled(false);
                holder.price.setTextColor(res.getColor(android.R.color.black));
                holder.cancelbutton.setVisibility(View.VISIBLE);
                holder.cancelbutton.setOnClickListener(new OnCancelButtonListener( tempValues ));
                holder.cartbutton.setOnClickListener(new OnCartedButtonListener());
                holder.cartbutton.setBackgroundColor(res.getColor(R.color.CyclopsDarkRed));
                holder.cartbutton.setImageResource(R.drawable.shopping_cart_filled);
            }
            else if (tempValues.getProgress() == 3) {
                holder.contents.setText( tempValues.getItemname() );
                holder.quantity.setText( tempValues.getQuantity().toString() );
                holder.status.setText( tempValues.getStatus() );
                holder.price.setText( format.format(tempValues.getPrice()) );
                holder.price.setEnabled(false);
                holder.price.setTextColor(res.getColor(android.R.color.black));
                holder.cancelbutton.setVisibility(View.GONE);
                holder.cartbutton.setOnClickListener(new OnPaidButtonListener());
                holder.cartbutton.setBackgroundColor(res.getColor(R.color.CyclopsDarkRed));
                holder.cartbutton.setImageResource(R.drawable.moneybag);
            }

            /******** Set Item Click Listner for LayoutInflater for each row *******/

            vi.setOnClickListener(new OnItemClickListener( position ));
        }
        return vi;
    }

    @Override
    public void onClick(View v) {
        Log.v("CustomAdapter", "=====Row button clicked=====");
    }

    /********* Called when Item click in ListView ************/
    private class OnItemClickListener  implements View.OnClickListener {
        private int mPosition;

        OnItemClickListener(int position){
            mPosition = position;
        }

        @Override
        public void onClick(View arg0) {


            //CustomListViewAndroidExample sct = (CustomListViewAndroidExample)activity;

            /****  Call  onItemClick Method inside CustomListViewAndroidExample Class ( See Below )****/

            //sct.onItemClick(mPosition);
        }
    }

    private class OnCancelButtonListener implements View.OnClickListener {
        private CartItem mItem;

        OnCancelButtonListener(CartItem item){
            mItem = item;
        }

        @Override
        public void onClick(View view) {
            try {
                final SharedPreferences prefs = activity.getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
                String token = prefs.getString(SECRETS.TOKEN_KEY, null);
                String user_email = prefs.getString(SECRETS.EMAIL_KEY, null);
                AsyncHttpClient client = new AsyncHttpClient();
                client.addHeader("Accept", "application/json");
                client.addHeader("X-User-Token", token);
                client.addHeader("X-User-Email", user_email);
                client.delete(activity, SECRETS.BASE_URL + "/items/" + mItem.getId(), new JsonHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable) {
                        System.out.println(throwable.toString());
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray json) {
                        System.out.println(json.toString());
                    }
                });
            }
            catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }

    private class CartedButtonListener implements View.OnClickListener {
        private CartItem mItem;
        private EditText mPrice;
        private TextView mQuantity;

        CartedButtonListener(CartItem item, EditText pricefield, TextView quantity){
            mItem = item;
            mPrice = pricefield;
            mQuantity = quantity;
        }

        @Override
        public void onClick(View view) {

            if (!(mPrice.getText().toString().equals(""))) {
                try {
                    float pricer = Float.parseFloat(mPrice.getText().toString());
                    int quantity = Integer.parseInt(mQuantity.getText().toString());
                    final SharedPreferences prefs = activity.getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
                    String token = prefs.getString(SECRETS.TOKEN_KEY, null);
                    String user_email = prefs.getString(SECRETS.EMAIL_KEY, null);
                    AsyncHttpClient client = new AsyncHttpClient();
                    client.addHeader("Accept", "application/json");
                    client.addHeader("X-User-Token", token);
                    client.addHeader("X-User-Email", user_email);
                    JSONObject wrapper = new JSONObject();
                    JSONObject jsonParams = new JSONObject();
                    jsonParams.put("status", "In Cart");
                    jsonParams.put("progress", 2);
                    jsonParams.put("price", pricer);
                    jsonParams.put("totalprice", ((int)(pricer * quantity * 100)));
                    wrapper.put("item", jsonParams);
                    StringEntity entity = new StringEntity(wrapper.toString());
                    client.put(activity, SECRETS.BASE_URL + "/items/" + mItem.getId(), entity, "application/json", new JsonHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable) {
                            System.out.println(throwable.toString());
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray json) {
                            System.out.println(json.toString());
                        }
                    });
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
            else {
                Toast.makeText(activity, "You need to enter an amount for the price of the item", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class OnCartedButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Toast.makeText(activity, "Waiting for the viewer to pay for this item.", Toast.LENGTH_LONG).show();
        }
    }

    private class OnPaidButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Toast.makeText(activity, "The viewer has paid, this item is now safe to checkout.", Toast.LENGTH_LONG).show();
        }
    }
}
