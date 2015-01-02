package com.ragres.mongodb.iotexample.serviceClients;

import android.util.Log;

import com.google.gson.Gson;
import com.ragres.mongodb.iotexample.AndroidApplication;
import com.ragres.mongodb.iotexample.controllers.ConnectivityController;
import com.ragres.mongodb.iotexample.domain.dto.ConnectedDTO;
import com.ragres.mongodb.iotexample.domain.dto.SensorDataDTO;
import com.ragres.mongodb.iotexample.domain.dto.TestMessageDTO;
import com.ragres.mongodb.iotexample.misc.DeviceSubTopics;
import com.ragres.mongodb.iotexample.misc.Logging;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import javax.inject.Inject;

/**
 * Service client for broker connectivity.
 */
public class BrokerServiceClient {

    /**
     * Android application instance.
     */
    private AndroidApplication androidApplication;

    /**
     * JSON serializer.
     */
    private Gson gson;

    /**
     * Public constructor.
     */
    @Inject
    public BrokerServiceClient(AndroidApplication androidApplication, Gson gson) {
        this.androidApplication = androidApplication;
        this.gson = gson;
    }

    /**
     * Get subtopic on device.
     *
     * @param relativePath Relative path for subtopic identification.
     * @return Full topic for device component.
     */
    public String getDeviceSubTopic(String relativePath) {
        return androidApplication.getDeviceSubTopic(relativePath);
    }

    /**
     * Get connectivity controller instance.
     *
     * @return Connectivity controller instance.
     */
    private ConnectivityController getConnectivityController() {
        return androidApplication.getConnectivityController();
    }

    /**
     * Send MQTT message.
     *
     * @param mqttMessage Message to send.
     * @param topic       Topic to publish message to.
     * @return True if successful, false if not.
     */
    private boolean sendMqttMessage(MqttMessage mqttMessage, String topic) {

        MqttAndroidClient mqttClient = getConnectivityController().getMqttClient();
        if (null == mqttClient)
            return false;
        try {

            mqttClient.publish(
                    topic,
                    mqttMessage);
        } catch (MqttException e) {
            Log.e(Logging.TAG, e.toString());
            return false;
        }
        return true;
    }

    /**
     * Send sensor data to broker.
     *
     * @param sensorDataDTO Sensor data to send.
     * @param subTopic      Sensor data subtopic.
     * @return True if successful, false if not.
     */
    public boolean sendSensorData(SensorDataDTO sensorDataDTO, String subTopic) {
        String jsonData = gson.toJson(sensorDataDTO);
        MqttMessage mqttMessage = new MqttMessage(jsonData.getBytes());
        return sendMqttMessage(mqttMessage, getDeviceSubTopic(subTopic));
    }

    /**
     * Send test message.
     *
     * @return True if successful, false if not.
     */
    public boolean sendTest() {
        TestMessageDTO testMessageDTO = new TestMessageDTO();
        String topic = getDeviceSubTopic(DeviceSubTopics.SUBTOPIC_TEST);
        String jsonData = gson.toJson(testMessageDTO);
        MqttMessage mqttMessage = new MqttMessage(jsonData.getBytes());
        return sendMqttMessage(mqttMessage, topic);
    }

    /**
     * Send connected message.
     *
     * @return True if successful, false if not.
     */
    public boolean sendConnected() {
        ConnectedDTO connectedDTO = new ConnectedDTO();
        String topic = getDeviceSubTopic(DeviceSubTopics.SUBTOPIC_CONNECTED);
        String jsonData = gson.toJson(connectedDTO);
        MqttMessage mqttMessage = new MqttMessage(jsonData.getBytes());
        return sendMqttMessage(mqttMessage, topic);
    }
}
