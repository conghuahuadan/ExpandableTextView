package com.chhd.expandabletextview.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.chhd.expandabletextview.ExpandableTextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ExpandableTextView tv = findViewById(R.id.tv);
    }
}
