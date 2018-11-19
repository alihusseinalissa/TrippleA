package com.ece.triplea;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button buChild, buParent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buChild = findViewById(R.id.buChild);
        buParent = findViewById(R.id.buParent);

        buChild.setOnClickListener(this);
        buParent.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.buChild:
                intent = new Intent(MainActivity.this, ChildActivity.class);
                break;
            case R.id.buParent:
                intent = new Intent(MainActivity.this, MapsActivity.class);
                break;
            default:
                return;
        }
        startActivity(intent);
    }
}
