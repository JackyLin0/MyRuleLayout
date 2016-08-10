package com.jacky.myrulelayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private  ScaleBar scrollBar;
    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scrollBar=(ScaleBar)findViewById(R.id.scrollBar);
        tv=(TextView)findViewById(R.id.tv);
        scrollBar.setOnScrollListener(new ScaleBar.OnScrollListener() {
            @Override
            public void onScrollScale(int scale) {
                tv.setText(""+scale);
            }
        });
    }
}
