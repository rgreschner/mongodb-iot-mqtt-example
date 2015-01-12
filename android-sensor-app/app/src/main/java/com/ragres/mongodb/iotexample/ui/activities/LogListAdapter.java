package com.ragres.mongodb.iotexample.ui.activities;

import android.app.Service;
import android.content.Context;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ragres.mongodb.iotexample.R;
import com.ragres.mongodb.iotexample.misc.Logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapter for log list items.
 */
public class LogListAdapter extends ArrayAdapter<LogListItem> {

    public static final int DEFAULT_ICON = R.drawable.loglistitem_icon_default;
    /**
     * Mappings of log list item type to icon resource.
     * Key: Log list item type as ordinal.
     * Value: Resource id for item type icon.
     */
    private static SparseIntArray ITEM_TYPE_TO_ICONS_MAPPING
            = new SparseIntArray(LogListItemType.values().length);

    /**
     * Static constructor.
     */
    static {
        initializeIconMappings();

    }

    /**
     * Put icon mapping for log item type.
     * @param type Log item type to put icon for.
     * @param resId Icon resource id.
     */
    private static void putIconMapping(LogListItemType type, int resId){
        int typeOrdinal = type.ordinal();
        ITEM_TYPE_TO_ICONS_MAPPING.put(typeOrdinal, resId);
    }

    /**
     * Initialize icon mappings for log list item types.
     */
    private static void initializeIconMappings() {
        putIconMapping(
                LogListItemType.SENSOR_ACCELEROMETER,
                R.drawable.loglistitem_icon_sensor_accelerometer);

        putIconMapping(
                LogListItemType.SENSOR_GPS,
                R.drawable.loglistitem_icon_sensor_gps);

        putIconMapping(
                LogListItemType.SEND_SENSOR_DATA,
                R.drawable.loglistitem_icon_send);

        putIconMapping(
                LogListItemType.CONNECTED,
                R.drawable.loglistitem_icon_connected);

        putIconMapping(
                LogListItemType.DISCONNECTED,
                R.drawable.loglistitem_icon_disconnected);
    }

    /**
     * Format for date formatting.
     */
    public static final DateFormat FORMAT_DATE_SENSOR_TIMESTAMP
            = new SimpleDateFormat("HH:mm:ss.SSS");

    /**
     * Layout inflater.
     */
    private final LayoutInflater layoutInflater;


    private LogListItemPool logListItemPool;

    /**
     * Public constructor.
     */
    public LogListAdapter(Context context, LogListItemPool logListItemPool) {
        super(context, R.layout.item_log_list);
        this.logListItemPool = logListItemPool;
        layoutInflater = (LayoutInflater) super.getContext().getSystemService(Service.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Get view for item.
     * @return View for item.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LogListItem item = this.getItem(position);
        View view = convertView;
        LogListItemViewHolder logListItemViewHolder = null;
        if (null == view) {
            view = layoutInflater.inflate(R.layout.item_log_list, parent, false);
            logListItemViewHolder = new LogListItemViewHolder();
            logListItemViewHolder.injectFromView(view);
            logListItemViewHolder.setItem(item);
            view.setTag(logListItemViewHolder);
        } else {
            logListItemViewHolder = (LogListItemViewHolder) view.getTag();
        }

        if (null == logListItemViewHolder){
            Log.w(Logging.TAG, "LogListItemViewHolder is null.");
            return view;
        }

        resetViewData(view, logListItemViewHolder);
        applyViewData(view, item, logListItemViewHolder);

        return view;
    }

    /**
     * Apply data to view.
     * @param view View to apply data to.
     * @param item Item with data.
     * @param logListItemViewHolder
     */
    private void applyViewData(View view, LogListItem item, LogListItemViewHolder logListItemViewHolder) {

        if (null == item){
            return;
        }

        ImageView itemIcon = logListItemViewHolder.itemIcon;

        Integer resId = getItemIconResource(item);
        if (null != resId) {
            itemIcon.setImageResource(resId);
        }

        TextView text1 = logListItemViewHolder.text1;
        Date timestamp = item.getTimestamp();
        if (null != timestamp) {
            text1.setText(FORMAT_DATE_SENSOR_TIMESTAMP.format(timestamp));
        }
        TextView text2 = logListItemViewHolder.text2;
        LogListItemType itemType = item.getType();
        if (null != itemType) {
            text2.setText(itemType.toString());
        }
    }

    /**
     * Reset displayed data on view.
     * @param view View.
     * @param logListItemViewHolder View holder with controls.
     */
    private void resetViewData(View view, LogListItemViewHolder logListItemViewHolder) {
        logListItemViewHolder.itemIcon.setImageResource(DEFAULT_ICON);
        logListItemViewHolder.text1.setText("");
        logListItemViewHolder.text2.setText("");
    }

    /**
     * Get item icon resource for item.
     * @param item Item to get icon for.
     * @return Resource id of item icon.
     */
    private int getItemIconResource(LogListItem item) {
        LogListItemType type = item.getType();
        int typeOrdinal = -1;
        if (null != type) {
            typeOrdinal = type.ordinal();
        }
        int resId = ITEM_TYPE_TO_ICONS_MAPPING.get(typeOrdinal, DEFAULT_ICON);
        return resId;
    }
}
