//EF test

package com.example.iotlab3app;

public enum SensorType {
    TEMPERATURE(15.0, 24.0, "°C"),
    HUMIDITY(10.0, 50.0, "%"),
    LUX(0.0, 600.0, "lux");

    public final double min;
    public final double max;
    public final String unit;

    SensorType(double min, double max, String unit) {
        this.min = min;
        this.max = max;
        this.unit = unit;
    }

    public boolean isWarning(double value) {
        return value < min || value > max;
    }
}
