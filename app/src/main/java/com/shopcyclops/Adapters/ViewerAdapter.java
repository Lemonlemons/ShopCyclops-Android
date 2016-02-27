package com.shopcyclops.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import com.shopcyclops.Fragments.Cart.DemoCartFragment;
import com.shopcyclops.Fragments.Cart.ViewerCartFragment;
import com.shopcyclops.Fragments.Chat.DemoChatFragment;
import com.shopcyclops.Fragments.Chat.ViewerChatFragment;
import com.shopcyclops.Fragments.Subscribe.SubscribeFragment;

/**
 * Created by Andrew on 8/15/2015.
 */
public class ViewerAdapter extends FragmentPagerAdapter {

    SubscribeFragment subscriby;
    DemoChatFragment demochat;
    DemoCartFragment democart;
    ViewerChatFragment viewerchat;
    ViewerCartFragment viewercart;
    int level;

    public ViewerAdapter(android.support.v4.app.FragmentManager fm, int level, String mediaUrl) {
        // level 1 means they are a customer and they haven't signed up at all
        // level 2 means they have signed up for the service
        // level 3 means they have signed up to be a "cyclops"

        super(fm);
        this.level = level;
        subscriby = new SubscribeFragment();
        demochat = new DemoChatFragment();
        democart = new DemoCartFragment();
        viewerchat = new ViewerChatFragment();
        viewercart = new ViewerCartFragment();
    }

    @Override
    public Fragment getItem(int index) {

        switch (level) {
            case 1: {
                switch (index) {
                    case 0:
                        return demochat;
                    case 1:
                        return subscriby;
                    case 2:
                        return democart;
                }
            }
            case 2: {
                switch (index) {
                    case 0:
                        return viewerchat;
                    case 1:
                        return subscriby;
                    case 2:
                        return viewercart;
                }
            }
        }

        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }
}
