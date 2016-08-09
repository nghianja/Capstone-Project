package com.udacity.nanodegree.nghianja.capstone;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

/**
 * References:
 * [1] https://github.com/lurbas/PlaceholderUI/blob/master/app/src/main/java/com/lucasurbas/placeholderui/PlaceholderUiActivity.java
 */
public class PlaceholderUiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
    }
}
