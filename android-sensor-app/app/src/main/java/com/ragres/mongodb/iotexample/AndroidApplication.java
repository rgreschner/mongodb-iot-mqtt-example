package com.ragres.mongodb.iotexample;

import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.google.gson.Gson;
import com.ragres.mongodb.iotexample.controllers.ConnectivityController;
import com.ragres.mongodb.iotexample.domain.dto.SensorDataDTO;
import com.ragres.mongodb.iotexample.domain.dto.payloads.AccelerometerDataPayload;
import com.ragres.mongodb.iotexample.misc.Logging;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Android application class.
 */
public class AndroidApplication extends Application {

    public static final String SUBTOPIC_ACCELEROMETER = "/accelerometer";
    public static final String SUBTOPIC_DEBUG = "/debug";
    public static final String SUBTOPIC_CONNECTED = "/connected";
    /**
     * Is logging of sensor data enabled?
     * If true, accelerometer data is written to log.
     */
    private static boolean LOG_SENSOR_DATA = false;
    /**
     * Listener for accelerometer sensor data.
     */
    private SensorEventListener accelerometerListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            // ASSUMPTION: we only want to handle accelerometer sensor data.
            if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
                return;
            }

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if (LOG_SENSOR_DATA) {
                Log.v(Logging.TAG, "Accelerometer data: x=" + String.valueOf(x) +
                        ", y=" + String.valueOf(y) + ", z=" + String.valueOf(z));
            }

            // ASSUMPTION: If mqttClient is != null, it is connected
            // (as defined by connection handling code).
            // TODO/DISCUSSION: methods from outer class are accessed without 'this'.
            if (isSendSensorDataEnabled() && null != getConnectivityController().getMqttClient()) {

                AccelerometerDataPayload accelerometerData = AccelerometerDataPayload.fromArray(event.values);
                SensorDataDTO sensorDataDTO = SensorDataDTO.
                        createWithPayload(accelerometerData);
                String jsonData = gson.toJson(sensorDataDTO);

                MqttMessage mqttMessage = new MqttMessage(jsonData.getBytes());
                try {
                    String topic = getDeviceSubTopic(SUBTOPIC_ACCELEROMETER);
                    getConnectivityController().getMqttClient().publish(
                            topic,
                            mqttMessage);
                } catch (MqttException e) {
                    Log.e(Logging.TAG, e.toString());
                }
            }


        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Unused.
        }
    };
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
     * JSON serializer.
     */
    private Gson gson = new Gson();
    /**
     * Android sensor manager.
     */
    private SensorManager sensorManager;
    /**
     * Accelerometer sensor.
     */
    private Sensor accelerometerSensor;
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

        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.accelerometerSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.sensorManager.registerListener(this.accelerometerListener, this.accelerometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
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
