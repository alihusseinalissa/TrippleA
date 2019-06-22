package com.ece.triplea.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ece.triplea.R;
import com.ece.triplea.activity.ChildActivity;
import com.ece.triplea.activity.MapsActivity;
import com.ece.triplea.activity.StepperActivity;
import com.ece.triplea.model.Child;
import com.stepstone.stepper.BlockingStep;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import net.gotev.uploadservice.MultipartUploadRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class ManageChildrenFragment extends Fragment implements Response.Listener<JSONArray>, Response.ErrorListener, AdapterView.OnItemClickListener, BlockingStep, AdapterView.OnItemLongClickListener {

    ArrayList<Child> mChildren;
    GridView mListView;
    ChildrenListAdapter mAdapter;
    TextView txtNoChildren;
    TextView txtTitle;
    ViewFlipper viewFlipper;
    RequestQueue volleyQueue;
    FloatingActionButton fab;
    BottomSheetDialog mBottomSheetDialog;
    View sheetView;
    LinearLayout optionEdit, optionDelete;
    long selectedChildId = -1;
    String selectedChildName = "";
    long userId = -1;
    boolean init;
    String mode;
    private int PAGE_LOADING = 0;
    private int PAGE_ERROR = 1;
    private int PAGE_NO_CHILDREN = 2;
    private int PAGE_LIST = 3;
    SharedPreferences sharedPreferences;
    StepperLayout stepperLayout;
    boolean canProceed = false;

    Toolbar toolbar;

    ImageView imgPicked;
    String imagePath;
    Uri imageUri;
//    String url = getString(R.string.base_url) + "ChildrenAdd.php";
    private String UPLOAD_URL;

    public void uploadMultipart(String childName, String childPhone) {
        String caption = "CAPTION";

        //getting the actual path of the image
        String path = getPath(imageUri);

        //Uploading code
        try {
            String uploadId = UUID.randomUUID().toString();

            //Creating a multi part request
            new MultipartUploadRequest(getContext(), uploadId, UPLOAD_URL)
                    .addFileToUpload(path, "image") //Adding file
                    .addParameter("caption", caption) //Adding text parameter to the request
                    .setMaxRetries(5)
                    .addParameter("user_id", String.valueOf(userId))
                    .addParameter("child_name", childName)
                    .addParameter("child_phone", childPhone)
                    .startUpload();
        } catch (Exception exc) {
            Toast.makeText(getContext(), exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public String getPath(Uri uri) {
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getActivity().getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.manage_children_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_child:
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_new_child, null);
                builder.setView(dialogView);
                builder.setTitle("Add New Child:");
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                final EditText txtChildName = dialogView.findViewById(R.id.txtChildName);
                final EditText txtChildPhone = dialogView.findViewById(R.id.txtChildPhone);

                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String childName = txtChildName.getText().toString();
                        String childPhone = txtChildPhone.getText().toString();

                        if (childName.equals("")){
                            showSnackbar("Child name must not be empty");
                            return;
                        } else if (childPhone.equals("")) {
                            showSnackbar("Child phone number must not be empty");
                            return;
                        } else if (imagePath == null || imagePath.equals("")) {
                            showSnackbar("Sorry, you must select an image for your child");
                            return;
                        }

//                        fab.hide();
                        viewFlipper.setDisplayedChild(PAGE_LOADING);
                        mChildren.clear();
                        uploadMultipart(childName, childPhone);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                makeRequest(); //Do something after 100ms
                            }
                        }, 5000);


                    }
                });

                imgPicked = dialogView.findViewById(R.id.imagePicked);
                Button buPick = dialogView.findViewById(R.id.buPick);
                Button buClear = dialogView.findViewById(R.id.buClear);
                buPick.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto, 1);//one can be replaced with any action code

                    }
                });
                buClear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imgPicked.setImageURI(Uri.parse(""));
                        imagePath = "";
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//
//        editor.putString("User" + i + "Image", imagePath);
//
//        editor.commit();


                    }
                });

                builder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        UPLOAD_URL = getString(R.string.base_url) + "ImageUpload.php";
        mChildren = new ArrayList<>();
        sharedPreferences = getActivity().getApplicationContext().getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
        userId = sharedPreferences.getLong("user_id", -1);

        fab = view.findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//
//            }
//        });

        viewFlipper = view.findViewById(R.id.manage_children_flipper);
        volleyQueue = Volley.newRequestQueue(getContext());

        stepperLayout = view.findViewById(R.id.stepperLayout);

        toolbar = view.findViewById(R.id.toolbar);
        txtTitle = view.findViewById(R.id.txtTitle);

//        mChildren.add(new Child(0, "ali", "123"));
//        mChildren.add(new Child(1, "ahmed", "456"));
//        mChildren.add(new Child(2, "mohammad", "789"));


        mAdapter = new ChildrenListAdapter(mChildren);
        mListView = view.findViewById(R.id.listChildren);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();


        makeRequest();


//        if (!init) {
//            CoordinatorLayout.LayoutParams lp = new CoordinatorLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//            lp.gravity = Gravity.BOTTOM | Gravity.END;
//            lp.setMargins(32, 32, 32, 32);
//            fab.setLayoutParams(lp);
//        }


        mListView.setOnItemLongClickListener(this);

        loadOptionsMenu();

        Button btnRetry = view.findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeRequest();
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.content_manage_children, container, false);
    }

    private void loadOptionsMenu() {
        mBottomSheetDialog = new BottomSheetDialog(getContext());
        sheetView = getLayoutInflater().inflate(R.layout.children_options, null);
        mBottomSheetDialog.setContentView(sheetView);

        optionEdit = sheetView.findViewById(R.id.option_chat);
        optionDelete = sheetView.findViewById(R.id.option_delete);

        optionEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Edit code here;
            }
        });

        optionDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Are you sure you want to delete this child?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        viewFlipper.setDisplayedChild(PAGE_LOADING);
//                        fab.hide();
                        mChildren.clear();
                        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                                getString(R.string.base_url) + "ChildrenDelete.php"
                                        + "?user_id=" + userId
                                        + "&child_id=" + selectedChildId,
                                null,
                                ManageChildrenFragment.this,
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.v("error", "cannot delete");
                                    }
                                });
                        volleyQueue.add(request);
                    }
                });
                builder.setNegativeButton("No", null);
                builder.show();

            }
        });
    }

    private void makeRequest() {
        viewFlipper.setDisplayedChild(PAGE_LOADING);
//        fab.hide();
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getString(R.string.base_url) + "ChildrenGet.php?user_id=" + userId, null, this, this);
        volleyQueue.add(request);

    }

    @Override
    public void onResponse(JSONArray response) {
        mChildren.clear();
        try {
            JSONArray jsonArray = response.getJSONArray(0);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                long childId = jsonObject.getLong("child_id");
                String childName = jsonObject.getString("child_name");
                String childPhone = jsonObject.getString("child_phone");
                String childImage = jsonObject.getString("child_image");
                mChildren.add(new Child(childId, childName, childPhone, childImage));
            }
            mAdapter.notifyDataSetChanged();
            viewFlipper.setDisplayedChild(PAGE_LIST);
//            fab.show();


        } catch (JSONException e) {
            e.printStackTrace();
            showError();
        }
    }

    private void showError() {
        viewFlipper.setDisplayedChild(PAGE_ERROR);
//        fab.hide();
        Snackbar.make(mListView, "Please check your internet connection", Snackbar.LENGTH_LONG)
                .setAction("Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        makeRequest();
                    }
                }).show();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        showError();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {
        selectedChildId = id;
        selectedChildName = mChildren.get(position).getChildName();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Confirmation");
        builder.setMessage("This phone/watch will be specified for your child: " + "\n"
                + mChildren.get((int) position).getChildName()
                + "\n" + "This option cannot be undone.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences sharedPreferences = getContext().getApplicationContext().getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong("child_id", (int) id);
                editor.putString("child_name", selectedChildName);
                editor.putBoolean("init", false);
                editor.apply();
                canProceed = true;
                ((StepperActivity) getActivity()).mStepperLayout.proceed();
                Intent intent = new Intent(getContext(), ChildActivity.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        selectedChildId = id;
        mBottomSheetDialog.show();
        return true;
    }

    @Override
    public void onNextClicked(StepperLayout.OnNextClickedCallback callback) {

    }

    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback callback) {
        if (sharedPreferences.getString("mode", "undefined").equals("parent")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("init", false);
            editor.apply();
            canProceed = true;
            Intent intent = new Intent(getActivity(), MapsActivity.class);
            startActivity(intent);
        } else {
            showSnackbar("Please select a child");
        }
        if (canProceed) {
            callback.complete();
        }
    }

    private void showSnackbar(String text) {
        Snackbar.make(getView(), text, Snackbar.LENGTH_LONG)
                .setAction(null, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
    }

    @Override
    public void onBackClicked(StepperLayout.OnBackClickedCallback callback) {
        callback.goToPrevStep();
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        return null;
    }

    @Override
    public void onSelected() {
        init = sharedPreferences.getBoolean("init", true);
        mode = sharedPreferences.getString("mode", "undefined");
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("");
        txtTitle.setText(mode.equals("child") ? "Choose a Child" : "Manage Your Children");
        mListView.setOnItemClickListener(mode.equals("child") ? this : null);
    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }

    public class ChildrenListAdapter extends BaseAdapter {

        ArrayList<Child> items = new ArrayList<>();

        ChildrenListAdapter(ArrayList<Child> list) {
            this.items = list;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return items.get(position).getChildId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.child_row, null);

            TextView txtChildName = view.findViewById(R.id.txtChildName);
            TextView txtChildPhone = view.findViewById(R.id.txtChildPhone);
            ImageView imgChild = view.findViewById(R.id.imgChild);
            txtChildName.setText(items.get(position).getChildName());
            txtChildPhone.setText(items.get(position).getChildPhone());
            Glide
                    .with(getContext())
                    .load(getChildImageUrl(items.get(position).getChildImage()))
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .skipMemoryCache(true)
                    .into(imgChild);

            return view;
        }

        private String getChildImageUrl(String childImage) {
            return getString(R.string.images_url) + childImage;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            if (mChildren.size() <= 0) {
                viewFlipper.setDisplayedChild(PAGE_NO_CHILDREN);
//                fab.show();
            } else {
                viewFlipper.setDisplayedChild(PAGE_LIST);
//                fab.show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                imagePath = selectedImage.toString();
                imageUri = selectedImage;
                Bitmap ThumbImage;
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                    ThumbImage = ThumbnailUtils.extractThumbnail(bitmap,
                            300, 300);
                    imgPicked.setImageBitmap(ThumbImage);
                } catch (IOException e) {
                    imgPicked.setImageURI(selectedImage);
                    e.printStackTrace();
                }


            }
        }


    }

}
