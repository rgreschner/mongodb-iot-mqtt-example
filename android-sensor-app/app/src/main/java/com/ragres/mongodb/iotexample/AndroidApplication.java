package com.ragres.mongodb.iotexample;

import android.app.Application;
import android.content.Intent;

import com.ragres.mongodb.iotexample.controllers.ConnectivityController;
import com.ragres.mongodb.iotexample.modules.MainModule;
import com.ragres.mongodb.iotexample.services.TelemetryService;

import javax.inject.Inject;

import dagger.ObjectGraph;
import rx.subjects.BehaviorSubject;

/**
 * Android application class.
 */
public class AndroidApplication extends Application {

    public ObjectGraph getObjectGraph() {
        return objectGraph;
    }

    private ObjectGraph objectGraph;

    /**
     * Device name.
     * ASSUMPTION: this is usable as MQTT identifier.
     */
    private String deviceName;

    /**
     * Observable for sensor data events.
     */
    private BehaviorSubject sensorDataObservable =
            BehaviorSubject.create();

    /**
     * Connectivity controller.
     */
    private ConnectivityController connectivityController;

    /**
     * Is transmission of sensor data over MQTT
     * enabled?
     */
    private boolean sendSensorData = false;

    /**
     * Get subtopic on device.
     *
     * @param relativePath Relative path for subtopic identification.
     * @return Full topic for device component.
     */
    public String getDeviceSubTopic(String relativePath) {
        String fullTopic = "device/" + getDeviceName() + relativePath;
        return fullTopic;
    }

    /**
     * On application create.
     */
    public void onCreate() {
        super.onCreate();

        // Initialize dagger dependency container.
        objectGraph = ObjectGraph.create(new MainModule(this));

        this.deviceName = android.os.Build.MODEL;

        this.connectivityController = objectGraph.get(ConnectivityController.class);

        Intent startServiceIntent = new Intent(this, TelemetryService.class);
        this.startService(startServiceIntent);
    }

    /**
     * Get connectivity controller instance.
     *
     * @return Connectivity controller instance.
     */
    public ConnectivityController getConnectivityController() {
        return connectivityController;
    }

    /**
     * Is sending of sensor data enabled?
     */
    public boolean isSendSensorDataEnabled() {
        return sendSensorData;
    }

    /**
     * Set sending of sensor data enabled.
     *
     * @param sendSensorData Send sensor data value.
     */
    public void setSendSensorData(boolean sendSensorData) {
        this.sendSensorData = sendSensorData;
    }

    /**
     * Get device name.
     *
     * @return
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * Observable for sensor data events.
     */
    public BehaviorSubject getSensorDataObservable() {
        return sensorDataObservable;
    }


}
