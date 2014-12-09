package com.ragres.mongodb.iotexample.controllers;


import android.content.Context;
import android.util.Log;

import com.ragres.mongodb.iotexample.AndroidApplication;
import com.ragres.mongodb.iotexample.domain.ConnectionState;
import com.ragres.mongodb.iotexample.misc.Logging;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Date;
import java.util.concurrent.Semaphore;

import rx.subjects.BehaviorSubject;

/**
 * Controller responsible for connectivity concerns.
 */
public class ConnectivityController {

    /**
     * Android application instance.
     */
    private AndroidApplication application;

    /**
     * Android application context.
     */
    private Context context;

    /**
     * Lock on MQTT client to pretect from
     * concurrent access and thus occuring exceptions.
     */
    private Semaphore mqttClientLock = new Semaphore(1);

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
    public ConnectivityController(AndroidApplication application) {
        this.application = application;
        this.context = application.getApplicationContext();
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
    public void connectToServer(String serverAddress) {

        Log.i(Logging.TAG, "Connecting to MQTT broker...");

        connectionStateChangedSubject.onNext(ConnectionState.CONNECTING);

        // Don't attempt connection on invalid addrss.
        if (!isServerAddressValid(serverAddress)) {
            connectionErrorSubject.onNext("MQTT broker address is invalid.");
            connectionStateChangedSubject.onNext(ConnectionState.DISCONNECTED);
            return;
        }

        try {
            mqttClientLock.acquire();
        } catch (InterruptedException e) {
            Log.e(Logging.TAG, e.toString());
        }

        String clientId = generateClientId();
        mqttClient = new MqttAndroidClient(context, serverAddress, clientId);
        try {
            IMqttToken connectToken = getMqttClient().connect();
            // TODO: wait for completion is probably not the best way
            // to handle connect. Use listener?
            connectToken.waitForCompletion();
        } catch (MqttException e) {
            // Handle connection errors.
            // TODO: To general?
            Log.e(Logging.TAG, e.toString());
            connectionStateChangedSubject.onNext(ConnectionState.DISCONNECTED);
            connectionErrorSubject.onNext(e.toString());
            mqttClientLock.release();
            return;
        }

        mqttClientLock.release();

        connectionStateChangedSubject.onNext(ConnectionState.CONNECTED);

        Log.i(Logging.TAG, "Connected to MQTT broker: " + serverAddress);

        byte[] payload = new Date().toString().getBytes();
        MqttMessage mqttMessage = new MqttMessage(payload);
        try {
            String topic = application.getDeviceSubTopic(AndroidApplication.SUBTOPIC_CONNECTED);
            getMqttClient().publish(topic, mqttMessage);
        } catch (MqttException e) {
            Log.e(Logging.TAG, e.toString());
        }

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

        connectionStateChangedSubject.onNext(ConnectionState.DISCONNECTING);

        // Gracefully allow transmissions
        // to be finished.
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Log.e(Logging.TAG, e.toString());
        }

        try {
            mqttClientLock.acquire();
        } catch (InterruptedException e) {
            Log.e(Logging.TAG, e.toString());
        }

        if (null != getMqttClient()) {

            try {
                IMqttToken disconnectToken = getMqttClient().disconnect();
                disconnectToken.waitForCompletion();
                Log.i(Logging.TAG, "Disconnected from MQTT broker.");
            } catch (MqttException e) {
                Log.e(Logging.TAG, e.toString());
            }

        }

        // ASSUMPTION: if mqttClient is null, application realizes that it is disconnected.
        mqttClient = null;

        mqttClientLock.release();

        connectionStateChangedSubject.onNext(ConnectionState.DISCONNECTED);
    }

    /**
     * MQTT broker client.
     */
    public MqttAndroidClient getMqttClient() {
        return mqttClient;
    }
}
