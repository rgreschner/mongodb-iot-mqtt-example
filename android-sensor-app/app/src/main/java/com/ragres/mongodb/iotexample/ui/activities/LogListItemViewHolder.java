package com.ragres.mongodb.iotexample.ui.activities;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ragres.mongodb.iotexample.R;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LogListItemViewHolder {

    @InjectView(R.id.item_icon)
    ImageView itemIcon;

    @InjectView(R.id.text1)
    TextView text1;

    @InjectView(R.id.text2)
    TextView text2;

    public void injectFromView(View view){
        ButterKnife.inject(this, view);
    }

}
