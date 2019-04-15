package com.ece.triplea.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ece.triplea.R;
import com.stepstone.stepper.Step;

public class SplashActivity extends AppCompatActivity {

    boolean canceled = true;

    @Override
    protected void onPause() {
        canceled = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        canceled = false;
        scheduleSplashScreen();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
// Remember that you should never show the action bar if the
// status bar is hidden, so hide that too if necessary.
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void scheduleSplashScreen() {
        long splashScreenDuration = getSplashScreenDuration();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!canceled) {
                    routeToAppropriatePage();
                    finish();
                }
            }
        }, splashScreenDuration);
    }

    private long getSplashScreenDuration(){
        return 3000L;
    }

    private void routeToAppropriatePage() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("GLOBAL", MODE_PRIVATE);
        Class c = null;
        if (sharedPreferences.getBoolean("init", true)){
            c = StepperActivity.class;
        } else {
            String mode = sharedPreferences.getString("mode", "unefined");
            if (mode.equals("parent"))
                c = MapsActivity.class;
            else if (mode.equals("child"))
                c = ChildActivity.class;
        }
        if (c != null) {
            Intent intent = new Intent(this, c);
            startActivity(intent);
        }
    }
}
