package com.ragres.mongodb.iotexample.ui.activities;

import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ragres.mongodb.iotexample.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LogListAdapter extends ArrayAdapter<LogListItem> {

    private static Map<LogListItemType, Integer> ITEM_TYPE_TO_ICONS_MAPPING
            = new HashMap<>();

    static {
        ITEM_TYPE_TO_ICONS_MAPPING.put(
                LogListItemType.SENSOR_ACCELEROMETER,
                R.drawable.loglistitem_icon_sensor_accelerometer);

        ITEM_TYPE_TO_ICONS_MAPPING.put(
                LogListItemType.SENSOR_GPS,
                R.drawable.loglistitem_icon_sensor_gps);

        ITEM_TYPE_TO_ICONS_MAPPING.put(
                LogListItemType.SEND_SENSOR_DATA,
                R.drawable.loglistitem_icon_send);

        ITEM_TYPE_TO_ICONS_MAPPING.put(
                LogListItemType.DISCONNECTED,
                R.drawable.loglistitem_icon_connected);

        ITEM_TYPE_TO_ICONS_MAPPING.put(
                LogListItemType.CONNECTED,
                R.drawable.loglistitem_icon_disconnected);

    }

    public static final DateFormat FORMAT_DATE_HOUR = new SimpleDateFormat("HH:mm:ss");

    public LogListAdapter(Context context
    ) {
        super(context, R.layout.item_log_list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LogListItem item = this.getItem(position);
        LayoutInflater layoutInflater = (LayoutInflater) super.getContext().getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.item_log_list, parent, false);
        applyView(view, item);
        return view;
    }

    private void applyView(View view, LogListItem item) {

        if (null == item)
            return;

        ImageView itemIcon = (ImageView) view.findViewById(R.id.item_icon);

        Integer resId = getItemIconResource(item);
        if (null != resId)
            itemIcon.setImageResource(resId);

        TextView textView = (TextView) view.findViewById(R.id.text1);
        Date timestamp = item.getTimestamp();
        if (null != timestamp) {
            textView.setText(FORMAT_DATE_HOUR.format(timestamp));
        }
        textView = (TextView) view.findViewById(R.id.text2);
        textView.setText(": " + item.getType().toString());
    }

    private Integer getItemIconResource(LogListItem item) {
        Integer resId = R.drawable.loglistitem_icon_default;
        if (ITEM_TYPE_TO_ICONS_MAPPING.containsKey(item.getType())) {
            resId = ITEM_TYPE_TO_ICONS_MAPPING.get(item.getType());
        }
        return resId;
    }
}
