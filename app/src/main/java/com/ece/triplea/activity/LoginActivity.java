package com.ece.triplea.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.ece.triplea.adapter.LoginTabAdapter;
import com.ece.triplea.R;
import com.ece.triplea.fragment.SignInFragment;
import com.ece.triplea.fragment.SignUpFragment;

public class LoginActivity extends AppCompatActivity implements SignUpFragment.OnNextClickedListener, SignInFragment.OnNextClickedListener {

    private LoginTabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    SignInFragment signInFragment = new SignInFragment();
    SignUpFragment signUpFragment = new SignUpFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
        long userId = sharedPreferences.getLong("user_id", -1);
        boolean initMode = sharedPreferences.getBoolean("init", true);
        if (userId > 0) {
            Intent intent;
            if (initMode) intent = new Intent(this, ManageChildrenActivity.class);
            else intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
            this.finish();
        }
        setContentView(R.layout.activity_login);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        adapter = new LoginTabAdapter(getSupportFragmentManager());
        adapter.addFragment(signInFragment, "Login");
        adapter.addFragment(signUpFragment, "Create Account");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onSignupClicked() {
        Log.v("signin","ok");
        this.finish();
    }

    @Override
    public void onSigninClicked() {
        Log.v("signup","ok");
        this.finish();
    }
}
