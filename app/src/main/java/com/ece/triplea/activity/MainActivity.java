package com.ece.triplea.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ece.triplea.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button buChild, buParent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buChild = findViewById(R.id.buChild);
        buParent = findViewById(R.id.buParent);

        buChild.setOnClickListener(this);
        buParent.setOnClickListener(this);

        createNotificationChannel();
    }


    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.buChild:
                intent = new Intent(MainActivity.this, ChildActivity.class);
                break;
            case R.id.buParent:
                intent = new Intent(MainActivity.this, MapsActivity.class);
                break;
            default:
                return;
        }
        startActivity(intent);
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
