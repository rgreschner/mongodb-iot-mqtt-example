package com.ragres.mongodb.iotexample;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.mbientlab.metawear.api.MetaWearBleService;
import com.ragres.mongodb.iotexample.controllers.ConnectivityController;
import com.ragres.mongodb.iotexample.controllers.MetaWearTestController;
import com.ragres.mongodb.iotexample.modules.MainModule;
import com.ragres.mongodb.iotexample.services.TelemetryService;
import com.ragres.mongodb.iotexample.ui.activities.LogListItem;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import dagger.ObjectGraph;
import rx.subjects.BehaviorSubject;

/**
 * Android application class.
 */
@ReportsCrashes(formKey = "",
        mailTo = "",
        mode = ReportingInteractionMode.NOTIFICATION,
        resNotifTickerText = R.string.crash_notif_ticker_text,
        resNotifTitle = R.string.crash_notif_title,
        resNotifText = R.string.crash_notif_text,
        resDialogText = R.string.crash_dialog_text,
        resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
        resDialogOkToast = R.string.crash_dialog_ok_toast
)
public class AndroidApplication extends Application implements ServiceConnection {

    private MetaWearBleService metaWearService;
    private MetaWearTestController metaWearCtrlr;

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

    public BehaviorSubject getBlinkLEDObservable() {
        return blinkLEDObservable;
    }

    private BehaviorSubject blinkLEDObservable =
            BehaviorSubject.create();

    public BehaviorSubject getConnectBluetoothObservable() {
        return connectBluetoothObservable;
    }

    private BehaviorSubject connectBluetoothObservable =
            BehaviorSubject.create();

    public BehaviorSubject getDisconnectBluetoothObservable() {
        return disconnectBluetoothObservable;
    }

    private BehaviorSubject disconnectBluetoothObservable =
            BehaviorSubject.create();

    private BehaviorSubject<LogListItem> logListItemObservable =
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
        fullTopic = fullTopic.replace(" ", "_");
        return fullTopic;
    }

    public void handleUncaughtException (Thread thread, Throwable e)
    {
        e.printStackTrace();
        ACRA.getErrorReporter().handleException(e);
        System.exit(1);
    }

    /**
     * On application create.
     */
    public void onCreate() {
        super.onCreate();

        initCrashReporting();

        // Initialize dagger dependency container.
        objectGraph = ObjectGraph.create(new MainModule(this));

        this.deviceName = android.os.Build.MODEL;

        this.connectivityController = objectGraph.get(ConnectivityController.class);
        this.metaWearCtrlr = objectGraph.get(MetaWearTestController.class);

        registerReceiver(metaWearUpdateReceiver, MetaWearBleService.getMetaWearIntentFilter());

        Intent startServiceIntent = new Intent(this, TelemetryService.class);
        this.startService(startServiceIntent);

        this.bindService(new Intent(this, MetaWearBleService.class),
                this, Context.BIND_AUTO_CREATE);

        this.startService(new Intent(this, MetaWearBleService.class));

    }

    private void initCrashReporting() {
        ACRA.init(this);
        String recipient = this.getResources().getString(R.string.value_crash_report_mail_recipient);
        ACRA.getConfig().setMailTo(recipient);
        ACRA.getErrorReporter().setDefaultReportSenders();

        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                handleUncaughtException(thread, e);
            }
        });
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


    /**
     * Observable for sensor data events.
     */
    public BehaviorSubject<LogListItem> getLogListItemObservable() {
        return logListItemObservable;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        String className = componentName.getClassName();
        if ("com.mbientlab.metawear.api.MetaWearBleService".equals(className)){
            onMetaWearServiceConnected(service);
        }
    }

    private static final BroadcastReceiver metaWearUpdateReceiver
            = MetaWearBleService.getMetaWearBroadcastReceiver();

    private void onMetaWearServiceConnected(IBinder service) {
        metaWearService = ((MetaWearBleService.LocalBinder) service).getService();

        metaWearCtrlr.init(metaWearService);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
}
