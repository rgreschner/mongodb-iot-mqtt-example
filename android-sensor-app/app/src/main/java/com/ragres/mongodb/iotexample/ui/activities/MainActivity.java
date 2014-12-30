package com.ragres.mongodb.iotexample.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.github.mikephil.charting.charts.LineChart;
import com.ragres.mongodb.iotexample.AndroidApplication;
import com.ragres.mongodb.iotexample.R;
import com.ragres.mongodb.iotexample.domain.ConnectionState;
import com.ragres.mongodb.iotexample.ui.ConnectivityButtonStates;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

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


    @InjectView(R.id.chart)
    LineChart lineChart;

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
     * Toolbar.
     */
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

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

        // Setup event listener for test button.
        btnTestMQTT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivityPresenter.onTestButtonClick();
            }
        });

        setSupportActionBar(toolbar);

        setUpLineChart();
        setUpPresenter();

    }

    private void setUpPresenter() {
        mainActivityPresenter = getAndroidApplication().getObjectGraph().get(MainActivityPresenter.class);

        mainActivityPresenter.onCreate();

        mainActivityPresenter.getUpdateUIForConnectionStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ConnectionState>() {
                    @Override
                    public void call(ConnectionState value) {
                        updateUIForConnectionState(value);
                    }
                });

        mainActivityPresenter.getServerAddressObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String value) {
                        labelServerAddress.setText(value);
                    }
                });

        mainActivityPresenter.getShowLocationSettingsDialogObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1() {
                    @Override
                    public void call(Object value) {
                        showLocationSettingsDialog();
                    }
                });

        mainActivityPresenter.setUpLineChart(lineChart);
    }

    private void setUpLineChart() {
        lineChart.setDrawLegend(false);
        lineChart.setTouchEnabled(false);
        lineChart.setHighlightEnabled(false);
        lineChart.setDescription("");
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
                mainActivityPresenter.showConnectToMqttDialog(this);
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


    }


}
