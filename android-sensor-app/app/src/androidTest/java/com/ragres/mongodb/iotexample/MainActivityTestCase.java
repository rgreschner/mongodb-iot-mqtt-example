package com.ragres.mongodb.iotexample;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.ragres.mongodb.iotexample.ui.activities.MainActivity;

public class MainActivityTestCase extends ActivityInstrumentationTestCase2<MainActivity> {
    private MainActivity activity;

    public MainActivityTestCase() {
        super(MainActivity.class);
    }

    public MainActivityTestCase(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(false);

        activity = getActivity();


    }


    public void testButtonNotNull() {
        View button = activity.findViewById(R.id.btnTestMQTT);
        assertNotNull(button);
    }

    public void testServerAddressIsDefault() {
        TextView label = (TextView) activity.findViewById(R.id.labelServerAddress);
        assertEquals(activity.getResources().getString(R.string.value_default_mqtt_server), label.getText());
    }

}
