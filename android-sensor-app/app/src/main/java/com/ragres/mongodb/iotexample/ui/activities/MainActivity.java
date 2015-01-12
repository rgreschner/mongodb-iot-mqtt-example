package com.ragres.mongodb.iotexample.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.mikephil.charting.charts.LineChart;
import com.ragres.mongodb.iotexample.AndroidApplication;
import com.ragres.mongodb.iotexample.R;
import com.ragres.mongodb.iotexample.domain.ConnectionState;
import com.ragres.mongodb.iotexample.ui.ConnectivityButtonStates;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Application main UI.
 */
public class MainActivity extends ActionBarActivity {


    /**
     * Mapping of connectivity state to button enabled / visible
     * states.
     * Key: Connection state as ordinal.
     * Value: Button states for connection states.
     */
    private static SparseArray<ConnectivityButtonStates> CONNECTIVITY_BUTTON_STATES
            = new SparseArray<>(ConnectionState.values().length);

    /**
     * Static initializer.
     */
    static {
        initializeButtonStates();
    }

    /**
     * Put button states for connectivity states.
     * @param connectionState
     * @param states
     */
    private static void putConnectivityButtonState(ConnectionState connectionState,
                                                   ConnectivityButtonStates states) {
        int connectionStateOrdinal = connectionState.ordinal();
        CONNECTIVITY_BUTTON_STATES.put(connectionStateOrdinal, states);
    }

    /**
     * Initialize connectivity button states.
     */
    private static void initializeButtonStates() {
        ConnectivityButtonStates buttonStates = null;

        buttonStates = new ConnectivityButtonStates();
        buttonStates.setConnectToServerEnabled(false);
        buttonStates.setDisconnectToServerEnabled(false);
        buttonStates.setTestMqttEnabled(false);
        buttonStates.setConnectToServerVisible(false);
        buttonStates.setDisconnectToServerVisible(false);
        putConnectivityButtonState(ConnectionState.UNKNOWN, buttonStates);

        buttonStates = new ConnectivityButtonStates();
        buttonStates.setConnectToServerEnabled(false);
        buttonStates.setDisconnectToServerEnabled(false);
        buttonStates.setTestMqttEnabled(false);
        buttonStates.setConnectToServerVisible(false);
        buttonStates.setDisconnectToServerVisible(true);
        putConnectivityButtonState(ConnectionState.DISCONNECTING, buttonStates);

        buttonStates = new ConnectivityButtonStates();
        buttonStates.setConnectToServerEnabled(true);
        buttonStates.setDisconnectToServerEnabled(false);
        buttonStates.setTestMqttEnabled(false);
        buttonStates.setConnectToServerVisible(true);
        buttonStates.setDisconnectToServerVisible(false);
        putConnectivityButtonState(ConnectionState.DISCONNECTED, buttonStates);

        buttonStates = new ConnectivityButtonStates();
        buttonStates.setConnectToServerEnabled(false);
        buttonStates.setDisconnectToServerEnabled(true);
        buttonStates.setTestMqttEnabled(true);
        buttonStates.setConnectToServerVisible(false);
        buttonStates.setDisconnectToServerVisible(true);
        putConnectivityButtonState(ConnectionState.CONNECTED, buttonStates);

        buttonStates = new ConnectivityButtonStates();
        buttonStates.setConnectToServerEnabled(false);
        buttonStates.setDisconnectToServerEnabled(false);
        buttonStates.setTestMqttEnabled(false);
        buttonStates.setConnectToServerVisible(true);
        buttonStates.setDisconnectToServerVisible(false);
        putConnectivityButtonState(ConnectionState.CONNECTING, buttonStates);
    }


    private Subscription getUpdateUIForConnectionStateObservableSubscription;
    private Subscription getServerAddressObservableSubscription;
    private Subscription getCollapseFloatingActionsMenuObservableSubscription;
    private Subscription getShowLocationSettingsDialogObservableSubscription;

    /**
     * Test MQTT connection button.
     */
    @InjectView(R.id.btn_test_mqtt)
    FloatingActionButton btnTestMQTT;

    /**
     * About button.
     */
    @InjectView(R.id.btn_about)
    FloatingActionButton btnAbout;

    /**
     * Sensor data chart.
     */
    @InjectView(R.id.sensor_data_chart)
    LineChart sensorDataChart;

    /**
     * Label for connection status.
     */
    @InjectView(R.id.label_connection_status_value)
    TextView labelConnectionStatusValue;

    /**
     * Label for device name.
     */
    @InjectView(R.id.label_device_name_value)
    TextView labelDeviceNameValue;

    /**
     * Label for connection status.
     */
    @InjectView(R.id.floating_actions)
    FloatingActionsMenu floatingActionsMenu;

    /**
     * Connect to server button.
     */
    @InjectView(R.id.label_server_address)
    TextView labelServerAddress;


    /**
     * Toolbar.
     */
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    /**
     * Toolbar.
     */
    @InjectView(R.id.log_list)
    ListView logList;

    /**
     * Menu.
     */
    private Menu menu;

    /**
     * Activity presenter.
     */
    private MainActivityPresenter mainActivityPresenter;

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
     * Function to show settings alert dialog
     */
    public void showLocationSettingsDialog() {
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
     * On activity create.
     *
     * @param savedInstanceState Saved data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Setup view content.
        setContentView(R.layout.activity_main);

        // Do view injection.
        ButterKnife.inject(this);

        String deviceName = getAndroidApplication().getDeviceName();
        labelDeviceNameValue.setText(deviceName);

        // Setup event listener for test button.
        btnTestMQTT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivityPresenter.onTestButtonClick();
            }
        });

        // Setup event listener for about button.
        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivityPresenter.onAboutButtonClick();
            }
        });

        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.greeny_green));

        setUpLineChart();
        setUpPresenter();

    }

    private void setUpPresenter() {
        mainActivityPresenter = getAndroidApplication().getObjectGraph().get(MainActivityPresenter.class);

        mainActivityPresenter.onCreate(this);
        mainActivityPresenter.setLogListAdapter(logList);

        getUpdateUIForConnectionStateObservableSubscription =
                mainActivityPresenter.getUpdateUIForConnectionStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ConnectionState>() {
                    @Override
                    public void call(ConnectionState value) {
                        updateUIForConnectionState(value);
                    }
                });

        getServerAddressObservableSubscription =
                mainActivityPresenter.getServerAddressObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String value) {
                        labelServerAddress.setText(value);
                    }
                });

        getCollapseFloatingActionsMenuObservableSubscription =
                mainActivityPresenter.getCollapseFloatingActionsMenuObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String value) {
                        floatingActionsMenu.collapse();
                    }
                });

        getShowLocationSettingsDialogObservableSubscription =
                mainActivityPresenter.getShowLocationSettingsDialogObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1() {
                    @Override
                    public void call(Object value) {
                        showLocationSettingsDialog();
                    }
                });

        mainActivityPresenter.setUpLineChart();
    }

    private void setUpLineChart() {
        getSensorDataChart().setDrawLegend(false);
        getSensorDataChart().setTouchEnabled(false);
        getSensorDataChart().setHighlightEnabled(false);
        getSensorDataChart().setDescription("");
    }


    /**
     * Handle activity resume.
     */
    @Override
    public void onResume() {
        super.onResume();
        mainActivityPresenter.forceUpdateUIForConnectionState();
        mainActivityPresenter.checkAndEnableGps();
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
    private void updateUIForConnectionState(ConnectionState connectionState) {
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
        mainActivityPresenter.forceUpdateUIForConnectionState();
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
                mainActivityPresenter.showConnectToMqttDialog();
                break;
            case R.id.action_disconnect_mqtt:
                mainActivityPresenter.disconnectFromServer();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * Get connectivity button states for connection state.
     * @param connectionState Connection state to get button states for.
     * @return Button states for connectivity states.
     */
    private ConnectivityButtonStates getConnectivityButtonStates(ConnectionState connectionState){
        ConnectivityButtonStates buttonStates
                = CONNECTIVITY_BUTTON_STATES.get(ConnectionState.UNKNOWN.ordinal());
        if (null != connectionState) {
            int connectionStateOrdinal = connectionState.ordinal();
            if (CONNECTIVITY_BUTTON_STATES.indexOfKey(connectionStateOrdinal) >= 0) {
                buttonStates = CONNECTIVITY_BUTTON_STATES.get(connectionStateOrdinal);
            }
        }
        return buttonStates;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mainActivityPresenter.onConfigurationChanged(newConfig);
    }

    /**
     * Set buttons' enabled state for new connection state.
     *
     * @param connectionState New connection state.
     */
    private void setButtonsEnabledForConnectionState(ConnectionState connectionState) {

        ConnectivityButtonStates buttonStates =
                getConnectivityButtonStates(connectionState);

        btnTestMQTT.setEnabled(buttonStates.isTestMqttEnabled());
        btnTestMQTT.setVisibility(buttonStates.isTestMqttEnabled() ? View.VISIBLE : View.GONE);

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


    }

    /**
     * On activity destroy.
     */
    @Override
    public void onDestroy() {

        super.onDestroy();

        mainActivityPresenter.onDestroy();

        if (null != getCollapseFloatingActionsMenuObservableSubscription){
            getCollapseFloatingActionsMenuObservableSubscription.unsubscribe();
            getCollapseFloatingActionsMenuObservableSubscription = null;
        }

        if (null != getServerAddressObservableSubscription){
            getServerAddressObservableSubscription.unsubscribe();
            getServerAddressObservableSubscription = null;
        }

        if (null != getShowLocationSettingsDialogObservableSubscription){
            getShowLocationSettingsDialogObservableSubscription.unsubscribe();
            getShowLocationSettingsDialogObservableSubscription = null;
        }

        if (null != getUpdateUIForConnectionStateObservableSubscription){
            getUpdateUIForConnectionStateObservableSubscription.unsubscribe();
            getUpdateUIForConnectionStateObservableSubscription = null;
        }

    }


    /**
     * Get sensor data chart.
     */
    public LineChart getSensorDataChart() {
        return sensorDataChart;
    }
}
