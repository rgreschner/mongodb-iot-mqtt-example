package com.ragres.mongodb.iotexample.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.ragres.mongodb.iotexample.AndroidApplication;
import com.ragres.mongodb.iotexample.controllers.ConnectivityController;
import com.ragres.mongodb.iotexample.domain.dto.SensorDataDTO;
import com.ragres.mongodb.iotexample.domain.dto.payloads.AccelerometerDataPayload;
import com.ragres.mongodb.iotexample.misc.Logging;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class AccelerometerTelemetryService extends Service {

    /**
     * Android application instance.
     */
    private AndroidApplication androidApplication;

    /**
     * Is logging of sensor data enabled?
     * If true, accelerometer data is written to log.
     */
    private static boolean LOG_SENSOR_DATA = false;

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
                    String topic = getDeviceSubTopic(AndroidApplication.SUBTOPIC_ACCELEROMETER);
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
     * Public constructor.
     */
    public AccelerometerTelemetryService() {
    }

    /**
     * Create service.
     */
    @Override
    public void onCreate() {
        this.androidApplication = (AndroidApplication) this.getApplication();
        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.accelerometerSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    /**
     * Start service.
     * @param intent Starting intent.
     * @param flags Service flags.
     * @param startId Start id.
     * @return Start service result.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.sensorManager.registerListener(this.accelerometerListener, this.accelerometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        Log.i(Logging.TAG, "AccelerometerTelemetryService was started.");
        return Service.START_NOT_STICKY;
    }

    /**
     * Bind service to intent.
     * @param intent Intent to bind to.
     * @return Binder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Get connectivity controller instance.
     *
     * @return Connectivity controller instance.
     */
    public ConnectivityController getConnectivityController() {
        return androidApplication.getConnectivityController();
    }

    /**
     * Is transmission of sensor data over MQTT
     * enabled?
     */
    private boolean isSendSensorDataEnabled() {
        return androidApplication.isSendSensorDataEnabled();
    }

    /**
     * Get subtopic on device.
     *
     * @param relativePath Relative path for subtopic identification.
     * @return Full topic for device component.
     */
    public String getDeviceSubTopic(String relativePath){
        return androidApplication.getDeviceSubTopic(relativePath);
    }

    /**
     * Destroy service.
     */
    public void onDestroy() {
        this.sensorManager.unregisterListener(this.accelerometerListener);
        Log.i(Logging.TAG, "AccelerometerTelemetryService was destroyed.");
    }
}
