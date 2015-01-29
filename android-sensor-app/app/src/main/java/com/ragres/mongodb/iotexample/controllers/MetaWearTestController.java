package com.ragres.mongodb.iotexample.controllers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.mbientlab.metawear.api.GATT;
import com.mbientlab.metawear.api.characteristic.Battery;
import com.mbientlab.metawear.api.characteristic.DeviceInformation;
import com.mbientlab.metawear.api.controller.Accelerometer.SamplingConfig.FullScaleRange;
import com.mbientlab.metawear.api.controller.Accelerometer.SamplingConfig.OutputDataRate;

import com.mbientlab.metawear.api.MetaWearBleService;
import com.mbientlab.metawear.api.MetaWearController;
import com.mbientlab.metawear.api.Module;
import com.mbientlab.metawear.api.controller.Accelerometer;
import com.mbientlab.metawear.api.controller.LED;
import com.mbientlab.metawear.api.controller.Temperature;
import com.ragres.mongodb.iotexample.AndroidApplication;
import com.ragres.mongodb.iotexample.domain.dto.SensorDataDTO;
import com.ragres.mongodb.iotexample.domain.dto.payloads.TemperatureDataPayload;
import com.ragres.mongodb.iotexample.misc.Logging;

import java.util.Locale;
import java.util.concurrent.Semaphore;

import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class MetaWearTestController {

    public static final String METAWEAR_DEVICE_NAME = "MetaWear";
    private static final boolean IS_ACCELEROMETER_ENABLED = false;
    private static final boolean BLINK_LED_ON_CONNECT = false;
    private final BehaviorSubject sensorDataObservable;

    private Semaphore deviceLock = new Semaphore(1);

    private  final short RISE_TIME= 500, HIGH_TIME= 2000, FALL_TIME= 500, DURATION= 3000;

    protected MetaWearController mwController;

    private LED ledController;

    private AndroidApplication application;

    private LED.ColorChannel currentChannel= LED.ColorChannel.BLUE;

    private Handler handler = new Handler(Looper.getMainLooper());
    private MetaWearBleService metaWearService;
    private Accelerometer accelerometerController;
    private Temperature temperatureController;

    private Gson gson = new Gson();
    private MetaWearController.DeviceCallbacks deviceCallbacks;

    public BehaviorSubject<Boolean> getMetaWearConnectionStateChangedObservable() {
        return metaWearConnectionStateChangedObservable;
    }

    private BehaviorSubject<Boolean> metaWearConnectionStateChangedObservable =
            BehaviorSubject.create();

    private void publishNewMetaWearConnectionState(boolean isConnected){
        metaWearConnectionStateChangedObservable.onNext(isConnected);
    }

    public MetaWearTestController(final AndroidApplication application) {
        this.application = application;

        this.sensorDataObservable = application.getSensorDataObservable();

        deviceCallbacks = new MetaWearController.DeviceCallbacks() {
            @Override
            public void connected() {

                Log.d(Logging.TAG, "MetaWear connected.");

                mwController.readDeviceInformation();
                mwController.readBatteryLevel();

                if (BLINK_LED_ON_CONNECT)
                    blinkLED();

                application.getBlinkLEDObservable()
                        .observeOn(Schedulers.newThread())
                        .subscribe(new Action1() {

                            @Override
                            public void call(Object o) {
                                blinkLED();
                            }
                        });

                publishNewMetaWearConnectionState(true);

            }

            @Override
            public void receivedGATTCharacteristic(
                    GATT.GATTCharacteristic characteristic, byte[] data) {
                if (characteristic == Battery.BATTERY_LEVEL) {
                    String batteryLevel = String.format(Locale.US, "%s", data[0]);
                    Log.d(Logging.TAG, "MetaWear battery level: " + batteryLevel);
                    return ;
                }
                if (characteristic == DeviceInformation.SERIAL_NUMBER) {
                    String serialNo = new String(data);
                    Log.d(Logging.TAG, "MetaWear serial number: " + serialNo);
                    return ;
                }
            }

            @Override
            public void disconnected() {

                Log.d(Logging.TAG, "MetaWear disconnected.");

                if (null == mwController){
                    publishNewMetaWearConnectionState(false);
                    return ;
                }

                mwController.removeDeviceCallback(deviceCallbacks);
                if (IS_ACCELEROMETER_ENABLED) {
                    mwController.removeModuleCallback(accelerometerCallbacks);
                }
                mwController.removeModuleCallback(temperatureCallbacks);

                publishNewMetaWearConnectionState(false);
            }
        };

        this.application.getConnectBluetoothObservable().subscribe(new Action1(){

            @Override
            public void call(Object o) {
                connect();
            }
        });

        this.application.getDisconnectBluetoothObservable().subscribe(new Action1(){

            @Override
            public void call(Object o) {
                disconnect();
            }
        });
    }

    public void init(final MetaWearBleService metaWearService){
        this.metaWearService = metaWearService;
    }

    private void disconnect() {
        try {
            deviceLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        hasDevice = false;
        deviceLock.release();
        Log.d(Logging.TAG, "MetaWear disconnect request.");
        if (null == mwController)
            return ;

        mwController.close(true);
    }

    private Temperature.Callbacks temperatureCallbacks = new Temperature.Callbacks() {
        @Override
        public void receivedTemperature(float degrees) {
            Log.d(Logging.TAG, "MetaWear Temperature: " + degrees);
            final SensorDataDTO sensorDataDTO = new SensorDataDTO();
            TemperatureDataPayload temperatureDataPayload = new TemperatureDataPayload();
            temperatureDataPayload.setDegrees(degrees);
            sensorDataDTO.setPayload(temperatureDataPayload);
            sensorDataDTO.setSubDevice("MetaWear");


            Log.v(Logging.TAG, "Sending temperature data: " + gson.toJson(sensorDataDTO));
            sensorDataObservable.onNext(sensorDataDTO);
        }

    };



    private Accelerometer.Callbacks accelerometerCallbacks = new Accelerometer.Callbacks() {

        @Override
        public void receivedDataValue(short x, short y, short z) {
            super.receivedDataValue(x, y, z);
            Log.d(Logging.TAG, "MetaWear Accel Raw: " + x + " " + y + " " + z);
        }

    };


    private void blinkLED() {

        Log.d(Logging.TAG, "MetaWear testing LED.");

        ledController.setColorChannel(currentChannel).withRiseTime(RISE_TIME).withHighTime(HIGH_TIME).withFallTime(FALL_TIME)
                .withPulseDuration(DURATION).commit();
        ledController.play(true);

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                resetLed();
            }
        };

        handler.postDelayed(runnable, 5*1000);
    }

    private void resetLed() {
        ledController.pause();
        ledController.stop(true);
    }


    private boolean hasDevice = false;

    private void connect() {



        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (null == bluetoothAdapter){
            Log.w(Logging.TAG, "BluetoothAdapter is null.");
            return ;
        }

        final BluetoothLeScanner leScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (null == leScanner){
            Log.w(Logging.TAG, "BluetoothLeScanner is null.");
            return ;
        }

        leScanner.startScan(new ScanCallback() {

            @Override
            public void onScanResult(int callbackType, ScanResult scanResult){
                BluetoothDevice bluetoothDevice = scanResult.getDevice();
                if (!(METAWEAR_DEVICE_NAME.equals(bluetoothDevice.getName())))
                    return ;
                leScanner.stopScan(this);
                try {
                    deviceLock.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (hasDevice)
                {
                    deviceLock.release();
                    return ;
                }
                hasDevice = true;
                deviceLock.release();

                Log.d(Logging.TAG, "MetaWear found device.");

                mwController = metaWearService.getMetaWearController(bluetoothDevice);

                ledController = (LED) mwController.getModuleController(Module.LED);
                accelerometerController = (Accelerometer) mwController.getModuleController(Module.ACCELEROMETER);
                temperatureController = (Temperature) mwController.getModuleController(Module.TEMPERATURE);

                mwController.addDeviceCallback(deviceCallbacks);
                addModuleCallbacks();

                initializeSensors();

                Log.d(Logging.TAG, "MetaWear ready.");

                mwController.connect();
            }
        });


    }

    private void initializeSensors() {

        if (IS_ACCELEROMETER_ENABLED) {
            accelerometerController.resetAll();
            accelerometerController.enableShakeDetection(Accelerometer.Axis.X).withThreshold(0.1f);
            accelerometerController.enableMotionDetection(Accelerometer.Axis.values()).withThreshold(0.1f);
            Accelerometer.SamplingConfig config = accelerometerController.enableXYZSampling();
            config.withFullScaleRange(FullScaleRange.values()[0])
                    .withOutputDataRate(OutputDataRate.values()[0]);
            accelerometerController.startComponents();
        }
        temperatureController.enableSampling()
                .withSampingPeriod(100)
                .withTemperatureDelta(0.1f)
                .withTemperatureBoundary(0f, 42f)
                .commit();


    }

    private void addModuleCallbacks() {
        if (IS_ACCELEROMETER_ENABLED) {
            mwController.addModuleCallback(accelerometerCallbacks);
        }
        mwController.addModuleCallback(temperatureCallbacks);
    }

    public boolean isConnected() {
        if (null == mwController)
            return false;
        return mwController.isConnected();
    }
}
