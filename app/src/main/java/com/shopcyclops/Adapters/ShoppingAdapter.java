package com.shopcyclops.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import com.shopcyclops.Fragments.Cart.StreamerCartFragment;
import com.shopcyclops.Fragments.Chat.StreamerChatFragment;
import com.shopcyclops.Fragments.Broadcast.BroadcastFragment;

/**
 * Created by Andrew on 7/20/2015.
 */
public class ShoppingAdapter extends FragmentPagerAdapter {

    BroadcastFragment streamy;
    StreamerChatFragment chatty;
    StreamerCartFragment cartty;

    public ShoppingAdapter(android.support.v4.app.FragmentManager fm)
    {
        // level 1 means they are a customer and they haven't signed up at all
        // level 2 means they have signed up for the service
        // level 3 means they have signed up to be a "cyclops"

        super(fm);
        streamy = new BroadcastFragment();
        chatty = new StreamerChatFragment();
        cartty = new StreamerCartFragment();
    }

    @Override
    public Fragment getItem(int index)
    {

        switch (index) {
            case 0:
                //Top Rated fragment activity
                return chatty;
            case 1:
                // Games fragment activity
                return streamy;
            case 2:
                //Movies fragment activity
                return cartty;
        }

        return null;
    }

    @Override
    public int getCount()
    {
        // get item count - equal to number of tabs
        return 3;
    }

}
