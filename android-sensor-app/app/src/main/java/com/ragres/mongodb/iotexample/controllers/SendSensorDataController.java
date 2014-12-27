package com.ragres.mongodb.iotexample.controllers;


import android.util.Log;

import com.google.gson.Gson;
import com.ragres.mongodb.iotexample.AndroidApplication;
import com.ragres.mongodb.iotexample.domain.dto.SensorDataDTO;
import com.ragres.mongodb.iotexample.domain.dto.payloads.AbstractPayload;
import com.ragres.mongodb.iotexample.domain.dto.payloads.AccelerometerDataPayload;
import com.ragres.mongodb.iotexample.domain.dto.payloads.LocationDataPayload;
import com.ragres.mongodb.iotexample.misc.DeviceSubTopics;
import com.ragres.mongodb.iotexample.misc.Logging;
import com.ragres.mongodb.iotexample.serviceClients.BrokerServiceClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

/**
 * Controller responsible for listening
 * for new sensor data events on observable
 * and sending it to broker.
 */
public class SendSensorDataController {

    /**
     * Is batching using a buffer enabled?
     */
    public static final boolean SEND_BUFFER_ENABLED = true;
    /**
     * Buffer event timeout.
     */
    public static final int SEND_BUFFER_TIMEOUT = 1000;
    /**
     * Unit for buffer event timeout.
     */
    public static final TimeUnit SEND_BUFFER_TIMEOUT_UNIT = TimeUnit.MILLISECONDS;
    /**
     * Android application instance.
     */
    private AndroidApplication androidApplication;

    /**
     * JSON serializer.
     */
    private Gson gson = new Gson();

    /**
     * Broker service client.
     */
    private BrokerServiceClient brokerServiceClient;

    /**
     * Observable for sensor data.
     */
    private BehaviorSubject sensorDataObservable;

    /**
     * Is logging of sensor data enabled?
     * If true, sensor data is written to log.
     */
    private static boolean LOG_SENSOR_DATA = true;

    /**
     * Subscription for sending data
     * on sensor data observable.
     */
    public Subscription sendDataSubscription;

    /**
     * Executor service for running fixed
     * number of send threads.
     */
    private ExecutorService sendDataExecutor = Executors.newFixedThreadPool(1);

    /**
     * Rx Scheduler for send data.
     */
    private Scheduler sendDataScheduler;

    /**
     * Public constructor.
     */
    public SendSensorDataController(AndroidApplication androidApplication, BehaviorSubject sensorDataObservable, BrokerServiceClient brokerServiceClient) {
        this.androidApplication = androidApplication;
        this.sensorDataObservable = sensorDataObservable;
        this.sendDataScheduler = Schedulers.from(sendDataExecutor);
        this.brokerServiceClient = brokerServiceClient;

    }

    /**
     * Unsubscribe from sensor observable.
     */
    public void unsubscribe() {
        clearSendDataSubscription();
    }

    /**
     * Clear subscription on sensor
     * data observable.
     */
    private void clearSendDataSubscription() {
        if (null != sendDataSubscription) {
            sendDataSubscription.unsubscribe();
            sendDataSubscription = null;
        }
    }


    /**
     * Subscribe on sensor data observable.
     */
    public void subscribe() {

        Observable intermediateObservable = null;

        if (SEND_BUFFER_ENABLED) {

            // Buffer sensor data events.

            intermediateObservable = sensorDataObservable.buffer(SEND_BUFFER_TIMEOUT
                    , SEND_BUFFER_TIMEOUT_UNIT);
        } else {

            // Map single sensor data events to list
            // in order to allow same application
            // logic to be used.

            intermediateObservable = sensorDataObservable.map(new Func1<SensorDataDTO, List<SensorDataDTO>>() {
                @Override
                public List<SensorDataDTO> call(SensorDataDTO input) {
                    List<SensorDataDTO> list = new ArrayList<>(1);
                    if (null != input)
                        list.add(input);
                    return list;
                }
            });
        }

        sendDataSubscription = intermediateObservable.observeOn(sendDataScheduler)
                .subscribe(new Action1<List<SensorDataDTO>>() {
                    @Override
                    public void call(List<SensorDataDTO> sensorDataDTOs) {
                        onSensorDataDTOsAvailable(sensorDataDTOs);
                    }
                });
    }

    /**
     * Event handler for processing list of sensor data.
     * @param sensorDataDTOs List of sensor data.
     */
    private void onSensorDataDTOsAvailable(List<SensorDataDTO> sensorDataDTOs) {

        // Exit when no sensor data available.
        if (1 > sensorDataDTOs.size())
            return;

        for (SensorDataDTO sensorDataDTO : sensorDataDTOs) {

            if (LOG_SENSOR_DATA) {
                Log.v(Logging.TAG, "Sensor data: " + gson.toJson(sensorDataDTO));
            }


            // ASSUMPTION: If mqttClient is != null, it is connected
            if (isSendSensorDataEnabled() && null != getConnectivityController().getMqttClient()) {
                sendSensorDataDTO(sensorDataDTO);
            }

        }
    }

    /**
     * Send single sensor data DTO.
     * @param sensorDataDTO Sensor data DTO to send.
     */
    private void sendSensorDataDTO(SensorDataDTO sensorDataDTO) {

        // Get broker topic for sensor data payload.
        String sensorSubTopic = getSubTopicForSensorDataDTO(sensorDataDTO);

        // If no topic available, exit.
        if (null == sensorSubTopic)
            return;

        brokerServiceClient.sendSensorData(sensorDataDTO, sensorSubTopic);
    }

    /**
     * Return subtopic for sensor data DTO.
     * @param sensorDataDTO Sensor data DTO to get subtopic for.
     * @return Subtopic for sensor data DTO.
     */
    private String getSubTopicForSensorDataDTO(SensorDataDTO sensorDataDTO) {
        AbstractPayload payload = sensorDataDTO.getPayload();
        String sensorSubTopic = null;
        if (null != payload) {
            sensorSubTopic = getSubTopicForSensorDataPayload(payload);
        }
        return sensorSubTopic;
    }

    /**
     * Return subtopic for sensor data payload.
     * @param payload Sensor data payload to get subtopic for.
     * @return Subtopic for sensor data payload.
     */
    private String getSubTopicForSensorDataPayload(AbstractPayload payload) {

        String sensorSubTopic = null;

        // TODO: Remove 'if's because of increased complexity.

        if (payload instanceof AccelerometerDataPayload) {
            sensorSubTopic = DeviceSubTopics.SUBTOPIC_ACCELEROMETER;
        }

        if (payload instanceof LocationDataPayload) {
            sensorSubTopic = DeviceSubTopics.SUBTOPIC_LOCATION;
        }

        return sensorSubTopic;
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
}
