package com.shopcyclops.Fragments.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shopcyclops.Activities.LoginActivity;
import com.shopcyclops.Activities.SignUpActivity;
import com.shopcyclops.R;

/**
 * Created by Andrew on 8/14/2015.
 */
public class DemoChatFragment extends android.support.v4.app.Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_demochat, container, false);

        return rootView;
    }
}

