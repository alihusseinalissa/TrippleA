package com.ece.triplea.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ece.triplea.R;

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
        // Example routing
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
