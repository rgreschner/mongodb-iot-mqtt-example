package com.ragres.mongodb.iotexample.controllers;


import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.ragres.mongodb.iotexample.AndroidApplication;
import com.ragres.mongodb.iotexample.R;
import com.ragres.mongodb.iotexample.domain.ConnectionState;
import com.ragres.mongodb.iotexample.domain.dto.WillDTO;
import com.ragres.mongodb.iotexample.misc.DeviceSubTopics;
import com.ragres.mongodb.iotexample.misc.Logging;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.inject.Inject;

import rx.subjects.BehaviorSubject;

/**
 * Controller responsible for connectivity concerns.
 */
public class ConnectivityController {

    public static final int DISCONNECT_TIMEOUT = 10 * 1000;
    public static final int CONNECT_TIMEOUT = 10 * 1000;
    /**
     * Connection state.
     */
    private ConnectionState connectionState = ConnectionState.DISCONNECTED;

    /**
     * Android application instance.
     */
    private AndroidApplication application;

    /**
     * Server address.
     */
    private String serverAddress;

    /**
     * Android application context.
     */
    private Context context;

    /**
     * JSON serializer.
     */
    private Gson gson;


    /**
     * Connection state changed observable.
     */
    private BehaviorSubject<ConnectionState> connectionStateChangedSubject =
            BehaviorSubject.create();

    /**
     * Connection error observable.
     */
    private BehaviorSubject<String> connectionErrorSubject = BehaviorSubject.create();
    /**
     * MQTT client instance.
     */
    private MqttAndroidClient mqttClient;

    /**
     * Public constructor.
     *
     * @param application Android application instance.
     */
    @Inject
    public ConnectivityController(AndroidApplication application, Gson gson) {
        this.application = application;
        this.context = application.getApplicationContext();
        this.serverAddress = getDefaultBrokerAddress();
        this.gson = gson;
    }

    /**
     * Get default broker address.
     *
     * @return Default MQTT broker address.
     */
    private String getDefaultBrokerAddress() {
        String defaultAddress = application.getString(R.string.value_default_mqtt_server);
        return defaultAddress;
    }

    private void cleanupAfterDisconnect() {
        mqttClient = null;
    }

    /**
     * Get connection error observable.
     *
     * @return Connection error observable.
     */
    public BehaviorSubject<String> getConnectionErrorSubject() {
        return connectionErrorSubject;
    }

    /**
     * Get connection state changed observable.
     *
     * @return Connection state changed observable.
     */
    public BehaviorSubject<ConnectionState> getConnectionStateChangedSubject() {
        return connectionStateChangedSubject;
    }

    private void updateConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
        connectionStateChangedSubject.onNext(connectionState);
    }

    /**
     * Check if server address is valid.
     *
     * @param serverAddress Server address is valid.
     * @return
     */
    private boolean isServerAddressValid(String serverAddress) {
        // TODO: Implement.
        // Validate input server address using regex for domain scheme,
        // IPv4 and IPv6.
        // ASSUMPTION: MQTT broker needs to start with protocol 'tcp://'
        // in front.
        return true;
    }


    /**
     * Actually connect to server.
     */
    public void connectToServer(final String serverAddress) {

        this.serverAddress = serverAddress;

        Log.i(Logging.TAG, "Connecting to MQTT broker...");

        updateConnectionState(ConnectionState.CONNECTING);


        // Don't attempt connection on invalid addrss.
        if (!isServerAddressValid(serverAddress)) {
            connectionErrorSubject.onNext("MQTT broker address is invalid.");
            updateConnectionState(ConnectionState.DISCONNECTED);
            return;
        }


        String clientId = generateClientId();
        MemoryPersistence persistence = new MemoryPersistence();
        final MqttAndroidClient localMqttClient = new MqttAndroidClient(context, serverAddress, clientId, persistence);

        MqttConnectOptions connectionOpts = getBrokerConnectionOptions();
        try {
            localMqttClient.connect(connectionOpts, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    localMqttClient.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable throwable) {

                            if (null != throwable)
                                throwable.printStackTrace();

                            cleanupAfterDisconnect();
                            updateConnectionState(ConnectionState.DISCONNECTED);
                        }

                        @Override
                        public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                        }
                    });
                    ConnectivityController.this.mqttClient = localMqttClient;
                    updateConnectionState(ConnectionState.CONNECTED);

                    Log.i(Logging.TAG, "Connected to MQTT broker: " + serverAddress);

                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable e) {
                    // Handle connection errors.
                    // TODO: To general?
                    Log.e(Logging.TAG, e.toString());
                    cleanupAfterDisconnect();
                    updateConnectionState(ConnectionState.DISCONNECTED);
                    connectionErrorSubject.onNext(e.toString());

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }

    /**
     * Get connection options for broker client.
     *
     * @return Connection options.
     */
    private MqttConnectOptions getBrokerConnectionOptions() {
        MqttConnectOptions connectionOpts = new MqttConnectOptions();
        connectionOpts.setConnectionTimeout(CONNECT_TIMEOUT);
        connectionOpts.setWill(application.getDeviceSubTopic(DeviceSubTopics.SUBTOPIC_WILL),
                getWillPayloadJson().getBytes(), 0, true);
        return connectionOpts;
    }

    /**
     * Get JSON payload for will message.
     *
     * @return
     */
    private String getWillPayloadJson() {
        String json = gson.toJson(new WillDTO());
        return json;
    }

    /**
     * Generate client id for broker.
     *
     * @return Client id for broker.
     */
    private String generateClientId() {
        return "com.ragres.mongodb.iotexample-" + application.getDeviceName();
    }

    /**
     * Disconnect from server.
     */
    public void disconnectFromServer() {

        Log.i(Logging.TAG, "Disconnecting from MQTT broker...");

        updateConnectionState(ConnectionState.DISCONNECTING);

        // Gracefully allow transmissions
        // to be finished.
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Log.e(Logging.TAG, e.toString());
        }


        if (null != getMqttClient()) {

            try {
                getMqttClient().disconnect(DISCONNECT_TIMEOUT, this, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken iMqttToken) {
                        cleanupAfterDisconnect();
                        updateConnectionState(ConnectionState.DISCONNECTED);
                    }

                    @Override
                    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                        cleanupAfterDisconnect();
                        updateConnectionState(ConnectionState.DISCONNECTED);
                    }
                });
            } catch (MqttException e) {
                Log.e(Logging.TAG, e.toString());
            }

        } else {

            // ASSUMPTION: if mqttClient is null, application realizes that it is disconnected.
            cleanupAfterDisconnect();
            updateConnectionState(ConnectionState.DISCONNECTED);
        }
    }

    /**
     * MQTT broker client.
     */
    public MqttAndroidClient getMqttClient() {
        return mqttClient;
    }

    /**
     * Get address of connected server.
     */
    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * Set address of connected server.
     *
     * @param serverAddress Address of connected server.
     */
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    /**
     * Get connection state.
     *
     * @return Connection state.
     */
    public ConnectionState getConnectionState() {
        return connectionState;
    }
}

