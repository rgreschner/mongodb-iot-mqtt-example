package com.ragres.mongodb.iotexample.domain.dto.payloads;

/**
 * Class representing accelerometer
 * sensor data.
 */
public class AccelerometerDataPayload extends AbstractPayload {
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
     * Constant identifier for payload type.
     */
    public static final String TYPE = "accelerometer";


    /**
     * Factory method which creates an AccelerometerData
     * instance from sensor values array.
     *
     * @param values Sensor values array.
     * @return AccelerometerData instance from sensor values array.
     */
    public static AccelerometerDataPayload fromArray(float[] values) {
        AccelerometerDataPayload data = new AccelerometerDataPayload();

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

}
