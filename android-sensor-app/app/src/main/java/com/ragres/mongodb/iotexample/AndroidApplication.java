package com.ragres.mongodb.iotexample;

import android.app.Application;
import android.content.Intent;

import com.ragres.mongodb.iotexample.controllers.ConnectivityController;
import com.ragres.mongodb.iotexample.services.AccelerometerTelemetryService;

/**
 * Android application class.
 */
public class AndroidApplication extends Application {

    public static final String SUBTOPIC_ACCELEROMETER = "/accelerometer";
    public static final String SUBTOPIC_DEBUG = "/debug";
    public static final String SUBTOPIC_CONNECTED = "/connected";

    /**
     * Device name.
     * ASSUMPTION: this is usable as MQTT identifier.
     */
    private String deviceName;
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

        this.deviceName = android.os.Build.MODEL;

        this.connectivityController = new ConnectivityController(this);

        Intent startServiceIntent = new Intent(this, AccelerometerTelemetryService.class);
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
}
