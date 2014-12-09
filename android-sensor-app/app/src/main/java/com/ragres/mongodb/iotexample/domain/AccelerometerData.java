package com.ragres.mongodb.iotexample.domain;


import java.util.Date;

/**
 * Class representing accelerometer
 * sensor data.
 */
public class AccelerometerData {
    /**
     * Value on X axis.
     */
    private float x;
    /**
     * Value on Y axis.
     */
    private float y;
    /**
     * Value on Z axis.
     */
    private float z;

    /**
     * Unix timestamp in milliseconds.
     */
    private long timestamp;

    /**
     * Default constructor.
     */
    public AccelerometerData() {
        Date now = new Date();
        timestamp = now.getTime();
    }

    /**
     * Factory method which creates an AccelerometerData
     * instance from sensor values array.
     *
     * @param values Sensor values array.
     * @return AccelerometerData instance from sensor values array.
     */
    public static AccelerometerData fromArray(float[] values) {
        AccelerometerData data = new AccelerometerData();

        float x = values[0];
        float y = values[1];
        float z = values[2];

        data.setX(x);
        data.setY(y);
        data.setZ(z);

        return data;
    }

    /**
     * Get X value.
     *
     * @return X value:
     */
    public float getX() {
        return x;
    }

    /**
     * Set X value.
     *
     * @param x X value.
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * Get Y value.
     *
     * @return Y value:
     */
    public float getY() {
        return y;
    }

    /**
     * Set Y value.
     *
     * @param y Y value.
     */
    public void setY(float y) {
        this.y = y;
    }

    /**
     * Get Z value.
     *
     * @return Z value:
     */
    public float getZ() {
        return z;
    }

    /**
     * Set Z value.
     *
     * @param z Z value.
     */
    public void setZ(float z) {
        this.z = z;
    }


    /**
     * Get timestamp of measurement.
     *
     * @return Timestamp of measurement.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Set timestamp of measurement.
     *
     * @param timestamp Timestamp of measurement..
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
