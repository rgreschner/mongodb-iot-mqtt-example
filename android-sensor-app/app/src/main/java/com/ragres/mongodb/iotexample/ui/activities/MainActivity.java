package com.ragres.mongodb.iotexample.ui.activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ragres.mongodb.iotexample.AndroidApplication;
import com.ragres.mongodb.iotexample.R;
import com.ragres.mongodb.iotexample.controllers.ConnectivityController;
import com.ragres.mongodb.iotexample.domain.ConnectionState;
import com.ragres.mongodb.iotexample.misc.Logging;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Date;

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
public class MainActivity extends Activity {


    /**
     * Connect to server button.
     */
    @InjectView(R.id.btnConnectToServer)
    Button btnConnectToServer;

    /**
     * Test MQTT connection.
     */
    @InjectView(R.id.btnTestMQTT)
    Button btnTestMQTT;

    /**
     * Connect to server button.
     */
    @InjectView(R.id.btnDisconnectFromServer)
    Button btnDisconnectFromServer;

    /**
     * Label for connection status.
     */
    @InjectView(R.id.labelConnectionStatusValue)
    TextView labelConnectionStatusValue;

    /**
     * Connect to server button.
     */
    @InjectView(R.id.inputServerAddress)
    EditText inputServerAddress;


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
     * Handle clicks on connect button.
     * Connects to supplied MQTT server instance.
     */
    @OnClick(R.id.btnConnectToServer)
    void onBtnConnectToServerClick() {

        final String serverAddress = inputServerAddress.getText().toString();

        AsyncTask connectTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                getConnectivityController().connectToServer(serverAddress);
                return null;
            }
        };
        connectTask.execute();

    }

    /**
     * Handle clicks on connect button.
     * Connects to supplied MQTT server instance.
     */
    @OnClick(R.id.btnTestMQTT)
    void onBtnTestMQTTClick() {


        AsyncTask connectTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                byte[] payload = new Date().toString().getBytes();
                MqttMessage mqttMessage = new MqttMessage(payload);
                try {
                    getConnectivityController().getMqttClient().publish(getAndroidApplication().
                            getDeviceSubTopic(AndroidApplication.SUBTOPIC_DEBUG), mqttMessage);
                } catch (MqttException e) {
                    Log.e(Logging.TAG, e.toString());
                }
                return null;
            }
        };
        connectTask.execute();

    }


    /**
     * Handle clicks on disconnect button.
     * Disconnects from connected MQTT server instance.
     */
    @OnClick(R.id.btnDisconnectFromServer)
    void onBtnDisconnectFromServerClick() {

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
        setContentView(R.layout.activity_main);


        // Wire up event listeners.
        ButterKnife.inject(this);

        updateUIForConnectionState(ConnectionState.DISCONNECTED);

        BehaviorSubject<ConnectionState> connectionStateChangedSubject = getConnectivityController().
                getConnectionStateChangedSubject();

        // Handle UI logic for connection state change.
        connectionStateChangedSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ConnectionState>() {
                    @Override
                    public void call(ConnectionState connectionState) {
                        updateUIForConnectionState(connectionState);
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
     * Refresh UI components for connection states.
     *
     * @param connectionState New connection state.
     */
    private void updateUIForConnectionState(ConnectionState connectionState) {
        setButtonsEnabledForConnectionState(connectionState);
        labelConnectionStatusValue.setText(connectionState.toString());
    }

    /**
     * Set buttons' enabled state for new connection state.
     *
     * @param connectionState New connection state.
     */
    private void setButtonsEnabledForConnectionState(ConnectionState connectionState) {

        // TODO: Storage of button states in a map with connection state as key
        // and own datastructure representing values might be better.

        switch (connectionState) {
            case CONNECTED:
                btnConnectToServer.setEnabled(false);
                btnDisconnectFromServer.setEnabled(true);
                btnTestMQTT.setEnabled(true);
                inputServerAddress.setEnabled(false);
                break;
            case CONNECTING:
                btnConnectToServer.setEnabled(false);
                btnDisconnectFromServer.setEnabled(false);
                btnTestMQTT.setEnabled(false);
                inputServerAddress.setEnabled(false);
                break;
            case DISCONNECTING:
                btnConnectToServer.setEnabled(false);
                btnDisconnectFromServer.setEnabled(false);
                btnTestMQTT.setEnabled(false);
                inputServerAddress.setEnabled(false);
                break;
            case DISCONNECTED:
            default:
                btnConnectToServer.setEnabled(true);
                btnDisconnectFromServer.setEnabled(false);
                btnTestMQTT.setEnabled(false);
                inputServerAddress.setEnabled(true);
                break;

        }
    }


}
