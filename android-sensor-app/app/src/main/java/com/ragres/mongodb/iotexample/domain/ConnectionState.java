package com.ragres.mongodb.iotexample.domain;

/**
 * Enum representing connection state to
 * MQTT broker.
 */
public enum ConnectionState {
    // Connection state is unknown.
    // ASSUMPTION: this should not happen during
    // normal operation.
    UNKNOWN,
    // Connecting to broker.
    CONNECTING,
    // Connected to broker.
    CONNECTED,
    // Disconnecting from broker.
    DISCONNECTING,
    // Disconnected from broker.
    DISCONNECTED
}
