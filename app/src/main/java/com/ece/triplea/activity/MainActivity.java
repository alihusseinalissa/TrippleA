package com.ece.triplea.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ece.triplea.R;

import lib.kingja.switchbutton.SwitchMultiButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button buNext;
    SwitchMultiButton mSwitchMultiButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buNext = findViewById(R.id.buNext);
        buNext.setOnClickListener(this);

        mSwitchMultiButton = findViewById(R.id.modeSwitch);
        mSwitchMultiButton.setOnSwitchListener(new SwitchMultiButton.OnSwitchListener() {
            @Override
            public void onSwitch(int position, String tabText) {
                if (position > -1)
                    buNext.setEnabled(true);
            }
        });

        createNotificationChannel();
    }


    @Override
    public void onClick(View v) {
        Intent intent;
        intent = new Intent(MainActivity.this, mSwitchMultiButton.getSelectedTab() == 0 ? MapsActivity.class : ChildActivity.class);
        startActivity(intent);
        this.finish();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Upload Locations";
            String description = "This notification must be shown always to ensure that the location" +
                    "is being uploaded to the database";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("UploadLocations", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
