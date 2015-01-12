package com.ragres.mongodb.iotexample.ui.activities;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ragres.mongodb.iotexample.R;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Holder for LogListItem adapter view recycling.
 */
public class LogListItemViewHolder {

    /**
     * Corresponding data.
     */
    private LogListItem item;

    @InjectView(R.id.item_icon)
    ImageView itemIcon;

    @InjectView(R.id.text1)
    TextView text1;

    @InjectView(R.id.text2)
    TextView text2;

    /**
     * Inject user controls from supplied view.
     * @param view View to get controls for.
     */
    public void injectFromView(View view){
        ButterKnife.inject(this, view);
    }

    /**
     * Get corresponding data.
     * @return Data for list item.
     */
    public LogListItem getItem() {
        return item;
    }

    /**
     * Set corresponding data.
     * @param item
     */
    public void setItem(LogListItem item) {
        this.item = item;
    }
}
