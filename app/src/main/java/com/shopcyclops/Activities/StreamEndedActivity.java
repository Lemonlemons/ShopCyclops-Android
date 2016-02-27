package com.shopcyclops.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.shopcyclops.R;
import com.shopcyclops.SECRETS;

/**
 * Created by Andrew on 9/20/2015.
 */
public class StreamEndedActivity extends Activity {

    TextView mainLabel, secondaryLabel;
    Button streamEndedBtn;
    boolean isComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streamended);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        isComplete = getIntent().getBooleanExtra(SECRETS.IS_COMPLETE_STREAM, false);

        mainLabel = (TextView) findViewById(R.id.mainText);
        secondaryLabel = (TextView) findViewById(R.id.secordayText);
        streamEndedBtn = (Button) findViewById(R.id.streamEndedButton);

        if (isComplete == false) {
            mainLabel.setText("Stream disconnected");
            secondaryLabel.setText("The stream has disconnected unexpectedly. This probably means that the streamer has closed the app." +
                    " Don't worry you won't be charged for any items.");
            streamEndedBtn.setText("Return to Stream List");
            streamEndedBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(StreamEndedActivity.this, StreamMainActivity.class);
                    startActivity(i);
                }
            });
        }
        else {
            streamEndedBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }
}
