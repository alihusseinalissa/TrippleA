package com.ece.triplea;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SignInFragment.OnNextClickedListener} interface
 * to handle interaction events.
 * Use the {@link SignInFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignInFragment extends Fragment implements Response.Listener<JSONArray>, Response.ErrorListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnNextClickedListener mListener;

    EditText txtPhoneNumber, txtPassword;
    Button btnLogin;
    ProgressBar signinProgress;

    RequestQueue mQueue;

    public SignInFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignInFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SignInFragment newInstance(String param1, String param2) {
        SignInFragment fragment = new SignInFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueue = Volley.newRequestQueue(getContext());
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onNextPressed() {
        if (mListener != null) {
            mListener.onSigninClicked();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNextClickedListener) {
            mListener = (OnNextClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnNextClickedListener {


        // TODO: Update argument type and name
        void onSigninClicked();

    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txtPhoneNumber = getView().findViewById(R.id.signin_phone);
        txtPassword = getView().findViewById(R.id.signin_password);
        signinProgress = getView().findViewById(R.id.signin_progress);
        btnLogin = getView().findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();

            }
        });
    }

    private void showSnackbar(String text){
        Snackbar.make(getView(), text, Snackbar.LENGTH_LONG)
                .setAction(null, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
    }

    private void login() {
        setLoading(true);
        String phone = txtPhoneNumber.getText().toString();
        String pass = txtPassword.getText().toString();

        if (phone.equals("") || pass.equals("")) {
            showSnackbar("Phone number or password can't be empty");
            setLoading(false);
        } else {
            String url = getString(R.string.base_url) + "UserGetId.php?" +
                    "phone=" + phone +
                    "&pass=" + pass;
            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, this, this);
            mQueue.add(request);
        }

    }

    @Override
    public void onResponse(JSONArray response) {
        setLoading(false);
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
        if (verified && userId >= 0){
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("user_id", userId);
            editor.apply();
            Intent intent = new Intent(getContext(), ManageChildrenActivity.class);
            startActivity(intent);
            onNextPressed();
        } else {
            showSnackbar(msg);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        setLoading(false);
        showSnackbar("Please check your internet connection!");

    }



    void setLoading(boolean loading){
        btnLogin.setEnabled(!loading);
        signinProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
