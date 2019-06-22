package com.ece.triplea.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.ece.triplea.R;
import com.stepstone.stepper.BlockingStep;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class LoginFragment extends Fragment implements Response.Listener<JSONArray>, Response.ErrorListener, BlockingStep {

    EditText txtPhoneNumber, txtPassword;
    ProgressBar signinProgress, signupProgress;

    RequestQueue mQueue;

    StepperLayout.OnNextClickedCallback mNextCallback;

    EditText txtName, txtPass, txtEmail, txtGender, txtPhone;

    boolean createAccount = false;

    ViewFlipper viewFlipper;

    Button btnToggleLoginMethodSignIn;
    Button btnToggleLoginMethodSignUp;


    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueue = Volley.newRequestQueue(getContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewFlipper = view.findViewById(R.id.view_flipper);
        viewFlipper.setInAnimation(getContext(), R.anim.slide_up);
        viewFlipper.setOutAnimation(getContext(), R.anim.slide_down);
        txtPhoneNumber = view.findViewById(R.id.signin_phone);
        txtPassword = view.findViewById(R.id.signin_password);
        txtName = view.findViewById(R.id.signup_name);
        txtPhone = view.findViewById(R.id.signup_phone);
        txtPass = view.findViewById(R.id.signup_password);

//        signinProgress = view.findViewById(R.id.signin_progress);
//        signupProgress = view.findViewById(R.id.signup_progress);
        btnToggleLoginMethodSignIn = view.findViewById(R.id.btnToggleLoginMethodSignIn);
        btnToggleLoginMethodSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFlipper.setDisplayedChild(1);
                createAccount = true;
            }
        });
        btnToggleLoginMethodSignUp = view.findViewById(R.id.btnToggleLoginMethodSignUp);
        btnToggleLoginMethodSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFlipper.setDisplayedChild(0);
                createAccount = false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onNextClicked(StepperLayout.OnNextClickedCallback callback) {
        callback.getStepperLayout().showProgress("Operation in progress, please wait...");
        hideKeyboard(Objects.requireNonNull(getActivity()));
        mNextCallback = callback;
        if (createAccount)
            createAccount();
        else signIn();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback callback) {

    }

    @Override
    public void onBackClicked(StepperLayout.OnBackClickedCallback callback) {
        callback.goToPrevStep();
    }

    public void createAccount() {
        //setLoading(true);
//                Toast.makeText(getContext(), "Create Account", Toast.LENGTH_SHORT).show();

        String name = txtName.getText().toString();
        String pass = txtPass.getText().toString();
        String phone = txtPhone.getText().toString();
        String url = getString(R.string.base_url) + "UserAdd.php?" +
                "name=" + name +
                "&pass=" + pass +
                "&email=" + "" +
                "&gender=" + "male" +
                "&phone=" + phone;

// Request a string response from the provided URL.
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            boolean error = jsonObject.getBoolean("error");
                            if (!error) {
                                long userId = jsonObject.getLong("id");
                                String msg = jsonObject.getString("msg");
                                if (userId < 0) showSnackbar(msg);
                                else {
                                    showSnackbar(msg);
                                    saveUserIdAfterCreateAccount(userId);
                                    mNextCallback.getStepperLayout().hideProgress();
                                    //setLoading(false);
                                }
                            } else {
                                showSnackbar("Cannot create account! try another username or phone number.");
                                //setLoading(false);
                                mNextCallback.getStepperLayout().hideProgress();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showSnackbar("Error! " + e.getMessage());
                            //setLoading(false);
                            mNextCallback.getStepperLayout().hideProgress();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showSnackbar("Please check your internet connection!");
                //setLoading(false);
                mNextCallback.getStepperLayout().hideProgress();
            }
        });

// Add the request to the RequestQueue.
        mQueue.add(request);
    }

    private void showSnackbar(String text) {
        Snackbar.make(getView(), text, Snackbar.LENGTH_LONG)
                .setAction(null, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
    }

    private void saveUserIdAfterCreateAccount(long userId) {
        SharedPreferences sharedPreferences = getActivity().getApplicationContext().getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("user_id", userId);
        editor.apply();
//        Intent intent = new Intent(getContext(), ManageChildrenActivity.class);
//        startActivity(intent);
        mNextCallback.goToNextStep();
    }

    private void signIn() {
        //setLoading(true);
        String phone = txtPhoneNumber.getText().toString();
        String pass = txtPassword.getText().toString();

        String url = getString(R.string.base_url) + "UserGetId.php?" +
                "phone=" + phone +
                "&pass=" + pass;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, this, this);
        mQueue.add(request);


    }

    @Override
    public void onResponse(JSONArray response) {
//        setLoading(false);
        mNextCallback.getStepperLayout().hideProgress();
        boolean verified = false;
        long userId = -1;
        String msg;
        try {
            //JSONArray jsonArray = response.getJSONArray(0);
            JSONObject jsonObject = response.getJSONObject(0);
            verified = jsonObject.getBoolean("verified");
            if (verified) userId = jsonObject.getLong("user_id");
            msg = jsonObject.getString("msg");
        } catch (JSONException e) {
            e.printStackTrace();
            showSnackbar("Login Error");
            return;
        }
        if (verified && userId >= 0) {
            SharedPreferences sharedPreferences = getActivity().getApplicationContext().getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("user_id", userId);
            editor.apply();
//            Intent intent = new Intent(getContext(), ManageChildrenActivity.class);
//            startActivity(intent);
            mNextCallback.goToNextStep();
        } else {
            showSnackbar(msg);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
//        setLoading(false);
        showSnackbar("Please check your internet connection!");
        mNextCallback.getStepperLayout().hideProgress();
    }

//    void setLoading(boolean loading) {
//        if (createAccount)
//            signupProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
//        else signinProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
//    }


    @Nullable
    @Override
    public VerificationError verifyStep() {
        if (createAccount) {
            String name = txtName.getText().toString();
            String phone = txtPhone.getText().toString();
            String pass = txtPass.getText().toString();

            if (name.equals(""))
                return new VerificationError("Your name must not be empty!");
            if (phone.equals(""))
                return new VerificationError("Phone number must not be empty!");
            else if (pass.equals(""))
                return new VerificationError("Password must not be empty!");
            return null;
        }
        else {
            String phone = txtPhoneNumber.getText().toString();
            String pass = txtPassword.getText().toString();

            if (phone.equals(""))
                return new VerificationError("Phone Number must not be empty!");
            else if (pass.equals(""))
                return new VerificationError("Try Again!");
            return null;
        }
    }

    @Override
    public void onSelected() {
//        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Login to Your Account");
    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }
}
