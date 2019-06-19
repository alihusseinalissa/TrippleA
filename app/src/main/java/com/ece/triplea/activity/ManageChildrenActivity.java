package com.ece.triplea.activity;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;


import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ece.triplea.R;
import com.ece.triplea.model.Child;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ManageChildrenActivity extends AppCompatActivity implements Response.Listener<JSONArray>, Response.ErrorListener, View.OnClickListener, AdapterView.OnItemClickListener {

    final ArrayList<Child> mChildren = new ArrayList<>();
    ListView mListView;
    ChildrenListAdapter mAdapter;
    TextView txtNoChildren;
    ViewFlipper viewFlipper;
    RequestQueue volleyQueue;
    FloatingActionButton fab;
    //    Button btnNext;
    BottomSheetDialog mBottomSheetDialog;
    View sheetView;
    LinearLayout optionEdit, optionDelete;
    long selectedChildId = -1;
    long userId = -1;
    boolean initMode;
    private int PAGE_LOADING = 0;
    private int PAGE_ERROR = 1;
    private int PAGE_NO_CHILDREN = 2;
    private int PAGE_LIST = 3;

    ImageView imgPicked;
    String imagePath;
    Uri imageUri;
    //String url = getString(R.string.base_url) + "ChildrenAdd.php";
    private String UPLOAD_URL;

    public void uploadMultipart(String childName, String childPhone) {
        String caption = "CAPTION";

        //getting the actual path of the image
        String path = getPath(imageUri);

        //Uploading code
        try {
            String uploadId = UUID.randomUUID().toString();

            //Creating a multi part request
            new MultipartUploadRequest(this, uploadId, UPLOAD_URL)
                    .addFileToUpload(path, "image") //Adding file
                    .addParameter("caption", caption) //Adding text parameter to the request
                    .setMaxRetries(5)
                    .addParameter("user_id", String.valueOf(userId))
                    .addParameter("child_name", childName)
                    .addParameter("child_phone", childPhone)
                    .startUpload();
        } catch (Exception exc) {
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_children);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        UPLOAD_URL = getString(R.string.base_url) + "ImageUpload.php";
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
        userId = sharedPreferences.getLong("user_id", -1);
        initMode = sharedPreferences.getBoolean("init", true);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ManageChildrenActivity.this);
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
                        viewFlipper.setDisplayedChild(PAGE_LOADING);
                        fab.hide();
                        mChildren.clear();

                        Bitmap bitmap = null;
//                        try {
//                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
//                            final Bitmap profilePicture = Bitmap.createScaledBitmap(bitmap, 120, 120, false);
//                            saveToInternalStorage(txtChildName.getText().toString(), profilePicture);
//
//                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                            profilePicture.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
//                            byte[] byteArray = byteArrayOutputStream.toByteArray();
//
//                            final String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

                        uploadMultipart(txtChildName.getText().toString(), txtChildPhone.getText().toString());
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                makeRequest(); //Do something after 100ms
                            }
                        }, 5000);


//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

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


            }
        });

        viewFlipper = findViewById(R.id.manage_children_flipper);

        volleyQueue = Volley.newRequestQueue(this);


//        mChildren.add(new Child(0, "ali", "123"));
//        mChildren.add(new Child(1, "ahmed", "456"));
//        mChildren.add(new Child(2, "mohammad", "789"));


        mAdapter = new ChildrenListAdapter(mChildren);
        mListView = findViewById(R.id.listChildren);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();


        makeRequest();

//        btnNext = findViewById(R.id.btnNext);
//        btnNext.setOnClickListener(this);

        if (!initMode) {
//            btnNext.setVisibility(View.GONE);
            CoordinatorLayout.LayoutParams lp = new CoordinatorLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.BOTTOM | Gravity.END;
            lp.setMargins(32, 32, 32, 32);
            fab.setLayoutParams(lp);
        }

        mListView.setOnItemClickListener(this);

        loadOptionsMenu();

        Button btnRetry = findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeRequest();
            }
        });

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        0);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }

    }

    private String saveToInternalStorage(String childName, Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, childName + ".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private void loadImageFromStorage(String path) {

        try {
            File f = new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            ImageView img = new ImageView(this);
            img.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void loadOptionsMenu() {
        mBottomSheetDialog = new BottomSheetDialog(this);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(ManageChildrenActivity.this);
                builder.setMessage("Are you sure you want to delete this child?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        viewFlipper.setDisplayedChild(PAGE_LOADING);
                        fab.hide();
                        mChildren.clear();
                        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                                getString(R.string.base_url) + "ChildrenDelete.php"
                                        + "?user_id=" + userId
                                        + "&child_id=" + selectedChildId,
                                null,
                                ManageChildrenActivity.this,
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
        fab.hide();
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getString(R.string.base_url) + "ChildrenGet.php?user_id=" + userId, null, this, this);
        volleyQueue.add(request);

    }

    @Override
    public void onResponse(JSONArray response) {
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
            fab.show();


        } catch (JSONException e) {
            e.printStackTrace();
            showError();
        }
    }

    private void showError() {
        viewFlipper.setDisplayedChild(PAGE_ERROR);
        fab.hide();
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
    public void onClick(View v) {
        SharedPreferences sharedPreferences = getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("init", false);
        editor.apply();
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedChildId = id;
        mBottomSheetDialog.show();
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.child_row, null);

            TextView txtChildName = view.findViewById(R.id.txtChildName);
            TextView txtChildPhone = view.findViewById(R.id.txtChildPhone);
            final ImageView imgChild = view.findViewById(R.id.imgChild);
            txtChildName.setText(items.get(position).getChildName());
            txtChildPhone.setText(items.get(position).getChildPhone());
            Glide.with(ManageChildrenActivity.this)
                    .load(getChildImageUrl(items.get(position).getChildImage()))
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgChild);
//                    .into(new SimpleTarget<Drawable>() {
//                        @Override
//                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//                            Bitmap bitmap = convertToBitmap(resource, 300, 300);
//                            String path = saveToInternalStorage(items.get(position).getChildId(), bitmap);
//                            imgChild.setImageBitmap(bitmap);
//                            SharedPreferences sharedPreferences = getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
//                            SharedPreferences.Editor editor = sharedPreferences.edit();
//                            editor.putString("imagesPath", path);
//                            editor.apply();
//                        }
//                    });

            return view;
        }

        public Bitmap convertToBitmap(Drawable drawable, int widthPixels, int heightPixels) {
            Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mutableBitmap);
            drawable.setBounds(0, 0, widthPixels, heightPixels);
            drawable.draw(canvas);

            return mutableBitmap;
        }

        private String saveToInternalStorage(long childId, Bitmap bitmapImage) {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            // Create imageDir
            File mypath = new File(directory, childId + ".jpg");

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mypath);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return directory.getAbsolutePath();
        }

        private String getChildImageUrl(String childImage) {
            return getString(R.string.images_url) + childImage;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            if (mChildren.size() <= 0) {
                viewFlipper.setDisplayedChild(PAGE_NO_CHILDREN);
                fab.show();
            } else {
                viewFlipper.setDisplayedChild(PAGE_LIST);
                fab.show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                imagePath = selectedImage.toString();
                imageUri = selectedImage;
                Bitmap ThumbImage;
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
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
