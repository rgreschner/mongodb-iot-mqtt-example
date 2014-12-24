package com.ragres.mongodb.iotexample.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.ragres.mongodb.iotexample.AndroidApplication;
import com.ragres.mongodb.iotexample.controllers.ConnectivityController;
import com.ragres.mongodb.iotexample.domain.dto.SensorDataDTO;
import com.ragres.mongodb.iotexample.domain.dto.payloads.AccelerometerDataPayload;
import com.ragres.mongodb.iotexample.domain.dto.payloads.LocationDataPayload;
import com.ragres.mongodb.iotexample.misc.DeviceSubTopics;
import com.ragres.mongodb.iotexample.misc.Logging;
import com.ragres.mongodb.iotexample.serviceClients.BrokerServiceClient;

/**
 * Service for gathering telemetry data.
 * TODO: Gathering telemtry data on queue and sending
 * it in bulk.
 */
public class TelemetryService extends Service {

    public static final int LOCATION_PROVIDER_UPDATE_INTERVAL = 5000;
    public static final int LOCATION_PROVIDER_MIN_DISTANCE = 0;

    /**
     * Android application instance.
     */
    private AndroidApplication androidApplication;

    /**
     * Broker service client.
     */
    private BrokerServiceClient brokerServiceClient;

    /**
     * Is logging of sensor data enabled?
     * If true, accelerometer data is written to log.
     */
    private static boolean LOG_SENSOR_DATA = false;

    /**
     * Handler for UI operations.
     */
    private Handler handler = new Handler(Looper.getMainLooper());

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
                brokerServiceClient.sendSensorData(sensorDataDTO, DeviceSubTopics.SUBTOPIC_ACCELEROMETER);
            }


        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Unused.
        }
    };


    /**
     * Listener for location provider.
     */
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (isSendSensorDataEnabled() && null != getConnectivityController().getMqttClient()) {

                LocationDataPayload locationData = LocationDataPayload.fromLocation(location);
                SensorDataDTO sensorDataDTO = SensorDataDTO.
                        createWithPayload(locationData);

                brokerServiceClient.sendSensorData(sensorDataDTO, DeviceSubTopics.SUBTOPIC_LOCATION);

            }

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            // Unused.
        }

        @Override
        public void onProviderEnabled(String s) {
            // Unused.
        }

        @Override
        public void onProviderDisabled(String s) {
            // Unused.
        }
    };

    /**
     * Location manager.
     */
    private LocationManager locationManager;
    private Thread sensorThread;

    /**
     * Public constructor.
     */
    public TelemetryService() {
    }


    /**
     * Create service.
     */
    @Override
    public void onCreate() {
        this.androidApplication = (AndroidApplication) this.getApplication();
        this.brokerServiceClient = new BrokerServiceClient(androidApplication);
        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.accelerometerSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * Start service.
     *
     * @param intent  Starting intent.
     * @param flags   Service flags.
     * @param startId Start id.
     * @return Start service result.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (null == sensorThread || !sensorThread.isAlive()) {
            sensorThread = new Thread(new Runnable() {
                /**
                 * Run sensor listener operations in own thread so
                 * service thread gets not blocked.
                 */
                @Override
                public void run() {
                    Looper.prepare();
                    Handler handler = new Handler();
                    sensorManager.registerListener(accelerometerListener, accelerometerSensor,
                            SensorManager.SENSOR_DELAY_NORMAL, handler);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            LOCATION_PROVIDER_UPDATE_INTERVAL, LOCATION_PROVIDER_MIN_DISTANCE, locationListener);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            LOCATION_PROVIDER_UPDATE_INTERVAL, LOCATION_PROVIDER_MIN_DISTANCE, locationListener);
                    Log.i(Logging.TAG, "TelemetryService was started.");
                    Looper.loop();
                }
            });
            sensorThread.start();
        }

        return Service.START_NOT_STICKY;
    }

    /**
     * Bind service to intent.
     *
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
     * Destroy service.
     */
    public void onDestroy() {
        if (null != sensorThread && sensorThread.isAlive()) {
            sensorThread.interrupt();
            sensorThread = null;
        }
        this.sensorManager.unregisterListener(this.accelerometerListener);
        this.locationManager.removeUpdates(locationListener);
        Log.i(Logging.TAG, "TelemetryService was destroyed.");
    }
}
