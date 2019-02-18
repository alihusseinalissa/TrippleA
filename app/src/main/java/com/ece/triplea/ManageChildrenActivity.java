package com.ece.triplea;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;

public class ManageChildrenActivity extends AppCompatActivity {

    final ArrayList<Child> mChildren = new ArrayList<>();
    ListView mListView;
    ChildrenListAdapter mAdapter;
    TextView txtNoChildren;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_children);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChildren.add(new Child(0, "ali", "123"));
                mAdapter.notifyDataSetChanged();
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


//        mChildren.add(new Child(0, "ali", "123"));
//        mChildren.add(new Child(1, "ahmed", "456"));
//        mChildren.add(new Child(2, "mohammad", "789"));

        txtNoChildren = findViewById(R.id.txtNoChildren);

        mAdapter = new ChildrenListAdapter(mChildren);
        mListView = findViewById(R.id.listChildren);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();



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

            return view;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            if (mChildren.size()<=0) {
                txtNoChildren.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
            } else {
                txtNoChildren.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
            }
        }
    }

}
