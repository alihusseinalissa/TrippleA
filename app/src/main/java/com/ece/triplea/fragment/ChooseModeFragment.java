package com.ece.triplea.fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ece.triplea.R;
import com.stepstone.stepper.BlockingStep;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import lib.kingja.switchbutton.SwitchMultiButton;

public class ChooseModeFragment extends Fragment implements BlockingStep {

    SwitchMultiButton mSwitchMultiButton;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwitchMultiButton = view.findViewById(R.id.modeSwitch);
        createNotificationChannel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_mode, container, false);
    }

    @Override
    public void onNextClicked(StepperLayout.OnNextClickedCallback callback) {
        if (mSwitchMultiButton.getSelectedTab() > -1 && getActivity() != null) {
            SharedPreferences sharedPreferences = getActivity().getApplicationContext().getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("mode", mSwitchMultiButton.getSelectedTab() == 0 ? "parent" : "child");
            editor.apply();
            callback.goToNextStep();
        }
    }

    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback callback) {

    }

    @Override
    public void onBackClicked(StepperLayout.OnBackClickedCallback callback) {
        callback.goToPrevStep();
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
            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        return null;
    }

    @Override
    public void onSelected() {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Choose Application Mode");
    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }
}
