package com.ragres.mongodb.iotexample.controllers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.mbientlab.metawear.api.controller.Accelerometer.SamplingConfig.FullScaleRange;
import com.mbientlab.metawear.api.controller.Accelerometer.SamplingConfig.OutputDataRate;

import com.mbientlab.metawear.api.MetaWearBleService;
import com.mbientlab.metawear.api.MetaWearController;
import com.mbientlab.metawear.api.Module;
import com.mbientlab.metawear.api.controller.Accelerometer;
import com.mbientlab.metawear.api.controller.LED;
import com.mbientlab.metawear.api.controller.Temperature;
import com.ragres.mongodb.iotexample.AndroidApplication;
import com.ragres.mongodb.iotexample.misc.Logging;

import java.util.concurrent.Semaphore;

import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MetaWearTestController {

    private Semaphore deviceLock = new Semaphore(1);

    private  final short RISE_TIME= 500, HIGH_TIME= 500, FALL_TIME= 500, DURATION= 2000;

    protected MetaWearController mwController;

    private LED ledController;

    private AndroidApplication application;

    private LED.ColorChannel currentChannel= LED.ColorChannel.BLUE;

    private Handler handler = new Handler(Looper.getMainLooper());
    private MetaWearBleService metaWearService;
    private Accelerometer accelCtrl;
    private Temperature tempController;

    public MetaWearTestController(AndroidApplication application, MetaWearBleService metaWearService) {
        this.application = application;
        this.metaWearService = metaWearService;
    }

    private Temperature.Callbacks tCallback = new Temperature.Callbacks() {
        @Override
        public void receivedTemperature(float degrees) {
            Log.d(Logging.TAG, "MetaWear Temperature " + degrees);
        }


    };



    private Accelerometer.Callbacks mCallback = new Accelerometer.Callbacks() {


        @Override
        public void receivedDataValue(short x, short y, short z) {
            super.receivedDataValue(x, y, z);
            Log.d(Logging.TAG, "MetaWear accel data.");
        }

        @Override
        public void movementDetected(Accelerometer.MovementData moveData) {
            super.movementDetected(moveData);
            Log.d(Logging.TAG, "MetaWear accel movement.");
        }

        @Override
        public void shakeDetected(Accelerometer.MovementData moveData) {
            super.shakeDetected(moveData);
            Log.d(Logging.TAG, "MetaWear accel shake.");
        }


    };

    private void testLed() {

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

    public void run() {



        final BluetoothAdapter a = BluetoothAdapter.getDefaultAdapter();
        final BluetoothLeScanner leScanner = a.getBluetoothLeScanner();
        leScanner.startScan(new ScanCallback() {

            @Override
            public void onScanResult(int callbackType, ScanResult scanResult){
                BluetoothDevice bluetoothDevice = scanResult.getDevice();
                if (!("MetaWear".equals(bluetoothDevice.getName())))
                    return ;
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
                accelCtrl = (Accelerometer) mwController.getModuleController(Module.ACCELEROMETER);
                tempController= (Temperature) mwController.getModuleController(Module.TEMPERATURE);


                mwController.addDeviceCallback(new MetaWearController.DeviceCallbacks() {
                    @Override
                    public void connected() {

                        Log.d(Logging.TAG, "MetaWear connected.");


                        testLed();

                        application.getBlinkLEDObservable()
                                .observeOn(Schedulers.newThread())
                                .subscribe(new Action1() {

                            @Override
                            public void call(Object o) {
                                testLed();
                            }
                        });

                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                while(true){
                                    if (!mwController.isConnected())
                                    {
                                        mwController.reconnect(false);
                                    }
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException e) {

                                    }
                                }
                            }
                        }).start();


                    }

                    @Override
                    public void disconnected() {
                        Log.d(Logging.TAG, "MetaWear disconnected.");
                    }
                });


                addModuleCallbacks();
                initializeSensors();

                Log.d(Logging.TAG, "MetaWear ready.");

                mwController.connect();
            }
        });


    }

    private void initializeSensors() {
        accelCtrl.resetAll();
        accelCtrl.enableShakeDetection(Accelerometer.Axis.X).withThreshold(0.1f);
        accelCtrl.enableMotionDetection(Accelerometer.Axis.values()).withThreshold(0.1f);
        Accelerometer.SamplingConfig config= accelCtrl.enableXYZSampling();
        config.withFullScaleRange(FullScaleRange.values()[0])
                .withOutputDataRate(OutputDataRate.values()[0]);

        tempController.enableSampling()
                .withSampingPeriod(100)
                .withTemperatureDelta(0.1f)
                .withTemperatureBoundary(0f, 42f)
                .commit();


        accelCtrl.startComponents();
    }

    private void addModuleCallbacks() {
        mwController.addModuleCallback(mCallback);
        mwController.addModuleCallback(tCallback);
    }

}
