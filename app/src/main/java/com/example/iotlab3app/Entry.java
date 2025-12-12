//EF test

package com.example.iotlab3app;

public class Entry {
    protected SensorType sensorType;
    protected String timeStamp; //Kan sedan brytas ut till HH:MM (eventuellt double)
    protected String value;
    protected boolean outOfRange; // true if value is outside reference interval

    public Entry(SensorType sensorType, String timeStamp, String value) {
        this.sensorType = sensorType;
        this.timeStamp = timeStamp;
        this.value = value;

        try {
            double v = Double.parseDouble(value);
            this.outOfRange = sensorType.isWarning(v);
        } catch (NumberFormatException e) {
            this.outOfRange = false;
        }
    }
}
