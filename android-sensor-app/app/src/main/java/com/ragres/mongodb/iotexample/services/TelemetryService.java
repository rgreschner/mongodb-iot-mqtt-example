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
import com.ragres.mongodb.iotexample.controllers.SendSensorDataController;
import com.ragres.mongodb.iotexample.domain.ConnectionState;
import com.ragres.mongodb.iotexample.domain.dto.SensorDataDTO;
import com.ragres.mongodb.iotexample.domain.dto.payloads.AccelerometerDataPayload;
import com.ragres.mongodb.iotexample.domain.dto.payloads.LocationDataPayload;
import com.ragres.mongodb.iotexample.misc.DeviceSubTopics;
import com.ragres.mongodb.iotexample.misc.Logging;
import com.ragres.mongodb.iotexample.serviceClients.BrokerServiceClient;

import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

/**
 * Service for gathering telemetry data.
 */
public class TelemetryService extends Service {

    public static final int LOCATION_PROVIDER_UPDATE_INTERVAL = 5000;
    public static final int LOCATION_PROVIDER_MIN_DISTANCE = 0;

    /**
     * Observable for sensor data events.
     */
    private BehaviorSubject sensorDataObservable =
            BehaviorSubject.create();


    /**
     * Android application instance.
     */
    private AndroidApplication androidApplication;


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
     * Location manager.
     */
    private LocationManager locationManager;

    /**
     * Thread for handling sensor data.
     */
    private Thread sensorThread;

    /**
     * Controller for sending sensor data.
     */
    private SendSensorDataController sendSensorDataController;

    /**
     * Broker service client.
     */
    private BrokerServiceClient brokerServiceClient;


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

            AccelerometerDataPayload accelerometerData = AccelerometerDataPayload.fromArray(event.values);
            SensorDataDTO sensorDataDTO = SensorDataDTO.
                    createWithPayload(accelerometerData);
            sensorDataObservable.onNext(sensorDataDTO);

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

            LocationDataPayload locationData = LocationDataPayload.fromLocation(location);
            SensorDataDTO sensorDataDTO = SensorDataDTO.
                    createWithPayload(locationData);
            sensorDataObservable.onNext(sensorDataDTO);

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
        this.sendSensorDataController = new SendSensorDataController(androidApplication,
                sensorDataObservable, brokerServiceClient);

        this.androidApplication.getConnectivityController().getConnectionStateChangedSubject()
                .observeOn(Schedulers.newThread())
                .subscribe(new Action1<ConnectionState>() {

                    @Override
                    public void call(ConnectionState connectionState) {
                        if (ConnectionState.CONNECTED.equals(connectionState)) {
                            brokerServiceClient.sendConnected();
                        }
                    }
                });
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

                    sendSensorDataController.unsubscribe();

                    sensorManager.registerListener(accelerometerListener, accelerometerSensor,
                            SensorManager.SENSOR_DELAY_NORMAL, handler);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            LOCATION_PROVIDER_UPDATE_INTERVAL, LOCATION_PROVIDER_MIN_DISTANCE, locationListener);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            LOCATION_PROVIDER_UPDATE_INTERVAL, LOCATION_PROVIDER_MIN_DISTANCE, locationListener);

                    sendSensorDataController.subscribe();

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
     * Destroy service.
     */
    public void onDestroy() {
        sendSensorDataController.unsubscribe();
        if (null != sensorThread && sensorThread.isAlive()) {
            sensorThread.interrupt();
            sensorThread = null;
        }
        this.sensorManager.unregisterListener(this.accelerometerListener);
        this.locationManager.removeUpdates(locationListener);
        Log.i(Logging.TAG, "TelemetryService was destroyed.");
    }
}
