package com.ece.triplea.adapter;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.ece.triplea.fragment.ChooseModeFragment;
import com.ece.triplea.fragment.ManageChildrenFragment;
import com.ece.triplea.fragment.LoginFragment;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;
import com.stepstone.stepper.viewmodel.StepViewModel;

public class MyStepperAdapter extends AbstractFragmentStepAdapter {

    public MyStepperAdapter(FragmentManager fm, Context context) {
        super(fm, context);
    }

    @Override
    public Step createStep(int position) {
        switch (position) {
            case 0:
                return new LoginFragment();
            case 1:
                return new ChooseModeFragment();
            case 2:
                return new ManageChildrenFragment();
            default:
                return null;
        }
//        final SignInFragmentStep step = new SignInFragmentStep();
//        Bundle b = new Bundle();
//        b.putInt("CURRENT_STEP_POSITION_KEY", position);
//        step.setArguments(b);
//        return step;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @NonNull
    @Override
    public StepViewModel getViewModel(@IntRange(from = 0) int position) {
        //Override this method to set Step title for the Tabs, not necessary for other stepper types
        return new StepViewModel.Builder(context)
                .setTitle("Sign In") //can be a CharSequence instead
                .create();
    }
}