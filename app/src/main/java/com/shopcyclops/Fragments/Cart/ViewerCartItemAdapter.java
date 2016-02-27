package com.shopcyclops.Fragments.Cart;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.shopcyclops.R;
import com.shopcyclops.SECRETS;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created by Andrew on 9/4/2015.
 */
public class ViewerCartItemAdapter extends BaseAdapter implements View.OnClickListener {

    /*********** Declare Used Variables *********/
    private Activity activity;
    private ArrayList data;
    private static LayoutInflater inflater=null;
    public Resources res;
    CartItem tempValues=null;
    String token;
    String user_email;
    int user_id;

    /*************  CustomAdapter Constructor *****************/
    public ViewerCartItemAdapter(Activity a, ArrayList d, Resources resLocal) {

        /********** Take passed values **********/
        activity = a;
        data=d;
        res = resLocal;

        final SharedPreferences prefs = activity.getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        token = prefs.getString(SECRETS.TOKEN_KEY, null);
        user_email = prefs.getString(SECRETS.EMAIL_KEY, null);
        user_id = prefs.getInt(SECRETS.USER_ID_KEY, 0);

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
        public TextView price;
        public TextView status;
        public ImageView image;
        public ImageButton cancelbutton;

    }

    /****** Depends upon data size called for each row , Create each ListView row *****/
    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        ViewHolder holder;

        if(convertView==null){

            /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
            vi = inflater.inflate(R.layout.viewer_cart_list_item, null);

            /****** View Holder Object to contain tabitem.xml file elements ******/

            holder = new ViewHolder();
            holder.contents = (TextView) vi.findViewById(R.id.contentsView);
            holder.quantity = (TextView) vi.findViewById(R.id.viewerquantityText);
            holder.price = (TextView) vi.findViewById(R.id.viewerpriceText);
            holder.status = (TextView) vi.findViewById(R.id.viewerstatusText);
            holder.cancelbutton = (ImageButton) vi.findViewById(R.id.closebutton);

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
            tempValues=null;
            tempValues = (CartItem) data.get( position );

            if (tempValues.getProgress() == 1) {
                holder.contents.setText(tempValues.getItemname());
                holder.quantity.setText(tempValues.getQuantity().toString());
                holder.price.setText("");
                holder.status.setText(tempValues.getStatus());
                if (tempValues.getViewer_id() == user_id) {
                    holder.cancelbutton.setVisibility(View.VISIBLE);
                    holder.cancelbutton.setOnClickListener(new OnCancelButtonListener( tempValues ));
                }
            }
            else if (tempValues.getProgress() == 2) {
                NumberFormat format = NumberFormat.getCurrencyInstance();
                holder.contents.setText(tempValues.getItemname());
                holder.quantity.setText(tempValues.getQuantity().toString());
                holder.price.setText(format.format(tempValues.getPrice()).toString());
                holder.status.setText(tempValues.getStatus());
                if (tempValues.getViewer_id() == user_id) {
                    holder.cancelbutton.setVisibility(View.VISIBLE);
                    holder.cancelbutton.setOnClickListener(new OnCancelButtonListener( tempValues ));
                }
            }
            else if (tempValues.getProgress() == 3) {
                NumberFormat format = NumberFormat.getCurrencyInstance();
                holder.contents.setText(tempValues.getItemname());
                holder.quantity.setText(tempValues.getQuantity().toString());
                holder.price.setText(format.format(tempValues.getPrice()).toString());
                holder.status.setText(tempValues.getStatus());
                if (tempValues.getViewer_id() == user_id) {
                    holder.cancelbutton.setVisibility(View.GONE);
                }
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

    private class OnCancelButtonListener implements View.OnClickListener {
        private CartItem mItem;

        OnCancelButtonListener(CartItem item){
            mItem = item;
        }

        @Override
        public void onClick(View view) {
            try {
                AsyncHttpClient client = new AsyncHttpClient();
                PersistentCookieStore myCookieStore = new PersistentCookieStore(activity);
                client.setCookieStore(myCookieStore);
                client.addHeader("Accept", "application/json");
                client.addHeader("X-User-Token", token);
                client.addHeader("X-User-Email", user_email);
                client.delete(activity, SECRETS.BASE_URL + "/items/" + mItem.getId(), new JsonHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject json) {
                        Toast.makeText(activity, throwable.toString(), Toast.LENGTH_LONG).show();
                        System.out.println(json.toString());
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
}
