package com.ragres.mongodb.iotexample.ui;

/**
 * Structure representing button states in UI
 * (according to connection state).
 */
public class ConnectivityButtonStates {

    private boolean connectToServerEnabled;
    private boolean connectToServerVisible;
    private boolean disconnectToServerEnabled;
    private boolean disconnectToServerVisible;
    private boolean testMqttEnabled;

    public boolean isConnectToServerEnabled() {
        return connectToServerEnabled;
    }

    public void setConnectToServerEnabled(boolean connectToServerEnabled) {
        this.connectToServerEnabled = connectToServerEnabled;
    }

    public boolean isDisconnectToServerEnabled() {
        return disconnectToServerEnabled;
    }

    public void setDisconnectToServerEnabled(boolean disconnectToServerEnabled) {
        this.disconnectToServerEnabled = disconnectToServerEnabled;
    }

    public boolean isTestMqttEnabled() {
        return testMqttEnabled;
    }

    public void setTestMqttEnabled(boolean testMqttEnabled) {
        this.testMqttEnabled = testMqttEnabled;
    }

    public boolean isConnectToServerVisible() {
        return connectToServerVisible;
    }

    public void setConnectToServerVisible(boolean connectToServerVisible) {
        this.connectToServerVisible = connectToServerVisible;
    }

    public boolean isDisconnectToServerVisible() {
        return disconnectToServerVisible;
    }

    public void setDisconnectToServerVisible(boolean disconnectToServerVisible) {
        this.disconnectToServerVisible = disconnectToServerVisible;
    }
}
