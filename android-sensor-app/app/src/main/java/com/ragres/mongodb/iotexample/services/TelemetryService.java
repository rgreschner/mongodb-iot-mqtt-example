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

import com.google.gson.Gson;
import com.ragres.mongodb.iotexample.AndroidApplication;
import com.ragres.mongodb.iotexample.controllers.SendSensorDataController;
import com.ragres.mongodb.iotexample.domain.ConnectionState;
import com.ragres.mongodb.iotexample.domain.dto.SensorDataDTO;
import com.ragres.mongodb.iotexample.domain.dto.payloads.AccelerometerDataPayload;
import com.ragres.mongodb.iotexample.domain.dto.payloads.LocationDataPayload;
import com.ragres.mongodb.iotexample.misc.Logging;
import com.ragres.mongodb.iotexample.serviceClients.BrokerServiceClient;
import com.ragres.mongodb.iotexample.ui.activities.LogListItemPool;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
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
    private BehaviorSubject sensorDataObservable;


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
     * Subscription for logging of sensor data count in last
     * second.
     */
    private Subscription sensorDataInLastSecondSubscription;


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
        this.sensorDataObservable = androidApplication.getSensorDataObservable();
        this.brokerServiceClient = androidApplication.getObjectGraph().get(BrokerServiceClient.class);
        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.accelerometerSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        this.sendSensorDataController = new SendSensorDataController(androidApplication,
                sensorDataObservable, brokerServiceClient,
                androidApplication.getObjectGraph().get(Gson.class),
                androidApplication.getObjectGraph().get(LogListItemPool.class));

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
                    clearSensorDataInLastSecondSubscription();
                    subscribeSensorDataInLastSecond();

                    Log.i(Logging.TAG, "TelemetryService was started.");
                    Looper.loop();
                }
            });
            sensorThread.start();
        }

        return Service.START_NOT_STICKY;
    }

    /**
     * Subscribe on data stream logging the
     * amount of sensor data in the last second.
     */
    private void subscribeSensorDataInLastSecond() {
        sensorDataInLastSecondSubscription = sensorDataObservable
                .map(new Func1<SensorDataDTO, Integer>() {

                    @Override
                    public Integer call(SensorDataDTO sensorDataDTO) {
                        return null == sensorDataDTO ? 0 : 1;
                    }
                }).buffer(1, TimeUnit.SECONDS).map(new Func1<List<Integer>, Integer>() {

                    @Override
                    public Integer call(List<Integer> list) {
                        int sum = 0;
                        for (Integer item : list) {
                            if (null == item)
                                continue;
                            sum += item.intValue();
                        }
                        list.clear();
                        return sum;
                    }
                }).observeOn(Schedulers.computation())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer count) {
                        Log.v(Logging.TAG, "Count of sensor data in last second: " +
                                String.valueOf(count));
                    }
                });
    }

    /**
     * Unsubscribe from sensor data count stream.
     */
    private void clearSensorDataInLastSecondSubscription() {
        if (null != sensorDataInLastSecondSubscription) {
            sensorDataInLastSecondSubscription.unsubscribe();
            sensorDataInLastSecondSubscription = null;
        }
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
        clearSensorDataInLastSecondSubscription();
        if (null != sensorThread && sensorThread.isAlive()) {
            sensorThread.interrupt();
            sensorThread = null;
        }
        this.sensorManager.unregisterListener(this.accelerometerListener);
        this.locationManager.removeUpdates(locationListener);
        Log.i(Logging.TAG, "TelemetryService was destroyed.");
    }
}
