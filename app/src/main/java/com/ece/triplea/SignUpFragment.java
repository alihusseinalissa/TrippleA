package com.ece.triplea;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SignUpFragment.OnNextClickedListener} interface
 * to handle interaction events.
 * Use the {@link SignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnNextClickedListener mListener;

    RequestQueue queue;

    Button buCreateAccount;
    EditText txtName, txtPass, txtEmail, txtGender, txtPhone;



    public SignUpFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignUpFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SignUpFragment newInstance(String param1, String param2) {
        SignUpFragment fragment = new SignUpFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        queue = Volley.newRequestQueue(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onSignupClicked();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        buCreateAccount = getView().findViewById(R.id.btnCreateAccount);
        buCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getContext(), "Create Account", Toast.LENGTH_SHORT).show();

                txtName = getView().findViewById(R.id.signup_name);
                        txtPass = getView().findViewById(R.id.signup_password);
//                txtEmail = getView().findViewById(R.id.signup_);
//                        txtGender = getView().findViewById(R.id.signup_gender);
                txtPhone = getView().findViewById(R.id.signup_phone);

                String name = txtName.getText().toString();
                String pass = txtPass.getText().toString();
                String phone = txtPhone.getText().toString();
                String url = getString(R.string.local_ip) + "UserAdd.php?"+
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
                                            loginAfterSignup(userId);
                                        }
                                    } else showSnackbar("Cannot create account! try another username or phone number.");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    showSnackbar("Error! " + e.getMessage());
                                }

                                onButtonPressed();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showSnackbar("Please check your internet connection!");
                    }
                });

// Add the request to the RequestQueue.
                queue.add(request);
            }
        });
    }

    private void loginAfterSignup(long userId) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("user_id", userId);
        editor.apply();
        Intent intent = new Intent(getContext(), ManageChildrenActivity.class);
        startActivity(intent);
        onButtonPressed();
    }

    private void showSnackbar(String text){
        Snackbar.make(getView(), text, Snackbar.LENGTH_LONG)
                .setAction(null, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
    }

    @Override
    public void onAttach(final Context context) {
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
        void onSignupClicked();
    }



}
