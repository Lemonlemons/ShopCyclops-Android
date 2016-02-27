package com.shopcyclops.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.shopcyclops.Fragments.Stream.StreamGridFragment;
import com.shopcyclops.Fragments.Stream.StreamListFragment;
import com.shopcyclops.R;

import java.util.Locale;

/**
 * Created by Andrew on 9/26/2015.
 */
public class StreamMainAdapter extends FragmentPagerAdapter {

    StreamGridFragment gridy;
    StreamListFragment listy;

    public StreamMainAdapter(FragmentManager fm) {
        super(fm);

        gridy = new StreamGridFragment();
        listy = new StreamListFragment();
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) {
            case 0:
                //Top Rated fragment activity
                return gridy;
            case 1:
                // Games fragment activity
                return listy;
        }

        return null;
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 2;
    }
}
