package com.ragres.mongodb.iotexample.ui.activities;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.ragres.mongodb.iotexample.AndroidApplication;
import com.ragres.mongodb.iotexample.R;
import com.ragres.mongodb.iotexample.controllers.ConnectivityController;
import com.ragres.mongodb.iotexample.domain.ConnectionState;
import com.ragres.mongodb.iotexample.misc.Logging;
import com.ragres.mongodb.iotexample.serviceClients.BrokerServiceClient;
import com.ragres.mongodb.iotexample.ui.ConnectivityButtonStates;
import com.ragres.mongodb.iotexample.ui.dialogs.ConnectMqttDialogFragment;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

/**
 * Application main UI.
 */
public class MainActivity extends ActionBarActivity {


    /**
     * Map containings mappings for button states according to connectivity
     * state.
     * Key: Connection state.
     * Value: Button states for connection states.
     */
    private static Map<ConnectionState, ConnectivityButtonStates> connectivityButtonStateList
            = new HashMap<>();

    /**
     * Static initializer.
     */
    static {
        ConnectivityButtonStates buttonStates = null;

        buttonStates = new ConnectivityButtonStates();
        buttonStates.setConnectToServerEnabled(false);
        buttonStates.setDisconnectToServerEnabled(false);
        buttonStates.setTestMqttEnabled(false);
        buttonStates.setConnectToServerVisible(false);
        buttonStates.setDisconnectToServerVisible(false);
        connectivityButtonStateList.put(ConnectionState.UNKNOWN, buttonStates);

        buttonStates = new ConnectivityButtonStates();
        buttonStates.setConnectToServerEnabled(false);
        buttonStates.setDisconnectToServerEnabled(false);
        buttonStates.setTestMqttEnabled(false);
        buttonStates.setConnectToServerVisible(false);
        buttonStates.setDisconnectToServerVisible(true);
        connectivityButtonStateList.put(ConnectionState.DISCONNECTING, buttonStates);

        buttonStates = new ConnectivityButtonStates();
        buttonStates.setConnectToServerEnabled(true);
        buttonStates.setDisconnectToServerEnabled(false);
        buttonStates.setTestMqttEnabled(false);
        buttonStates.setConnectToServerVisible(true);
        buttonStates.setDisconnectToServerVisible(false);
        connectivityButtonStateList.put(ConnectionState.DISCONNECTED, buttonStates);

        buttonStates = new ConnectivityButtonStates();
        buttonStates.setConnectToServerEnabled(false);
        buttonStates.setDisconnectToServerEnabled(true);
        buttonStates.setTestMqttEnabled(true);
        buttonStates.setConnectToServerVisible(false);
        buttonStates.setDisconnectToServerVisible(true);
        connectivityButtonStateList.put(ConnectionState.CONNECTED, buttonStates);

        buttonStates = new ConnectivityButtonStates();
        buttonStates.setConnectToServerEnabled(false);
        buttonStates.setDisconnectToServerEnabled(false);
        buttonStates.setTestMqttEnabled(false);
        buttonStates.setConnectToServerVisible(true);
        buttonStates.setDisconnectToServerVisible(false);
        connectivityButtonStateList.put(ConnectionState.CONNECTING, buttonStates);

    }


    /**
     * Test MQTT connection button.
     */
    @InjectView(R.id.btnTestMQTT)
    FloatingActionButton btnTestMQTT;


    /**
     * Label for connection status.
     */
    @InjectView(R.id.labelConnectionStatusValue)
    TextView labelConnectionStatusValue;

    /**
     * Connect to server button.
     */
    @InjectView(R.id.labelServerAddress)
    TextView labelServerAddress;


    /**
     * Location manager.
     */
    private LocationManager locationManager;

    /**
     * Toolbar.
     */
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    /**
     * Menu.
     */
    private Menu menu;
    private BrokerServiceClient brokerServiceClient;


    /**
     * Get application instance.
     *
     * @return Application instance.
     */
    private AndroidApplication getAndroidApplication() {
        AndroidApplication application = (AndroidApplication) this.getApplication();
        return application;
    }

    /**
     * Get connectivity controller instance.
     *
     * @return Connectivity controller instance.
     */
    private ConnectivityController getConnectivityController() {
        ConnectivityController connectivityController = getAndroidApplication().
                getConnectivityController();
        return connectivityController;
    }

    /**
     * Function to show settings alert dialog
     */
    public void showLocationSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle(getString(R.string.show_location_settings_dialog_title));

        alertDialog.setMessage(getString(R.string.show_location_settings_dialog_message));

        // Okay button.
        alertDialog.setPositiveButton(getString(R.string.show_location_settings_dialog_okay), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(settingsIntent);
            }
        });

        // Cancel button.
        alertDialog.setNegativeButton(getString(R.string.show_location_settings_dialog_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }


    /**
     * Show connect to MQTT broker dialog.
     */
    private void showConnectToMqttDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (null != prev) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);


        DialogFragment newFragment = ConnectMqttDialogFragment.newInstance();
        newFragment.show(ft, "dialog");
    }

    /**
     * Disconnect from MQTT broker.
     */
    private void disconnectFromServer() {

        AsyncTask disconnectTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                getConnectivityController().disconnectFromServer();
                return null;
            }
        };
        disconnectTask.execute();

    }

    /**
     * On activity create.
     *
     * @param savedInstanceState Saved data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.brokerServiceClient = this.getAndroidApplication().getObjectGraph()
                .get(BrokerServiceClient.class);

        setContentView(R.layout.activity_main);

        // Wire up event listeners.
        ButterKnife.inject(this);

        // Setup event listener for test button.
        btnTestMQTT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTestButtonClick();
            }
        });

        setSupportActionBar(toolbar);
        labelServerAddress.setText(getConnectivityController().getServerAddress());

        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        BehaviorSubject<ConnectionState> connectionStateChangedSubject = getConnectivityController().
                getConnectionStateChangedSubject();

        // Handle UI logic for connection state change.
        connectionStateChangedSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ConnectionState>() {
                    @Override
                    public void call(ConnectionState connectionState) {
                        updateUIForConnectionState();
                    }
                });

        // Handle business logic for connection state change.
        connectionStateChangedSubject
                .observeOn(Schedulers.immediate())
                .subscribe(new Action1<ConnectionState>() {
                    @Override
                    public void call(ConnectionState connectionState) {
                        if (ConnectionState.DISCONNECTING == connectionState) {
                            getAndroidApplication().setSendSensorData(false);
                        }
                        if (ConnectionState.CONNECTED == connectionState) {
                            getAndroidApplication().setSendSensorData(true);
                        }
                    }
                });

    }

    /**
     * Handle clicks to test button.
     */
    private void onTestButtonClick() {
        AsyncTask sendTestTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                brokerServiceClient.sendTest();
                return null;
            }
        };
        sendTestTask.execute();
    }

    /**
     * Handle activity resume.
     */
    @Override
    public void onResume() {
        super.onResume();
        updateUIForConnectionState();
        try {
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            Log.i(Logging.TAG, "isGPSEnabled: " + String.valueOf(isGPSEnabled));
            if (!isGPSEnabled) {
                //showLocationSettingsAlert();
            }
        } catch (Exception ex0) {
            Log.e(Logging.TAG, "Error: " + ex0.toString());
        }

    }

    /**
     * Get color for connection state value.
     *
     * @param connectionState Connection state to get color for.
     * @return Color for connection state.
     */
    private int getColorForConnectionState(ConnectionState connectionState) {
        int colorId = R.color.color_connection_state_intermediate;

        if (ConnectionState.CONNECTED == connectionState) {
            colorId = R.color.color_connection_state_connected;
        }

        if (ConnectionState.DISCONNECTED == connectionState) {
            colorId = R.color.color_connection_state_disconnected;
        }

        int color = getResources().getColor(colorId);
        return color;
    }

    /**
     * Refresh UI components for connection states.
     */
    private void updateUIForConnectionState() {
        ConnectionState connectionState = getConnectivityController().getConnectionState();
        setButtonsEnabledForConnectionState(connectionState);
        labelConnectionStatusValue.setText(connectionState.toString());

        int color = getColorForConnectionState(connectionState);
        labelConnectionStatusValue.setTextColor(color);

    }

    /**
     * Create options menu.
     *
     * @param menu Menu instance for options inflate.
     * @return Operation handled.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        updateUIForConnectionState();
        return true;
    }

    /**
     * Handle menu options item select.
     *
     * @param item Selected item.
     * @return Operation handled.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_connect_mqtt:
                showConnectToMqttDialog();
                break;
            case R.id.action_disconnect_mqtt:
                disconnectFromServer();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * Set buttons' enabled state for new connection state.
     *
     * @param connectionState New connection state.
     */
    private void setButtonsEnabledForConnectionState(ConnectionState connectionState) {

        ConnectivityButtonStates buttonStates =
                connectivityButtonStateList.get(connectionState);

        if (null == buttonStates) {
            buttonStates = connectivityButtonStateList.get(ConnectionState.UNKNOWN);
        }

        btnTestMQTT.setEnabled(buttonStates.isTestMqttEnabled());

        if (null != menu) {
            menu.findItem(R.id.action_connect_mqtt)
                    .setEnabled(buttonStates.isConnectToServerEnabled());
            menu.findItem(R.id.action_disconnect_mqtt)
                    .setEnabled(buttonStates.isDisconnectToServerEnabled());

            menu.findItem(R.id.action_connect_mqtt)
                    .setVisible(buttonStates.isConnectToServerVisible());
            menu.findItem(R.id.action_disconnect_mqtt)
                    .setVisible(buttonStates.isDisconnectToServerVisible());
        }

        labelServerAddress.setText(getConnectivityController().getServerAddress());
    }


}
