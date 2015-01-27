package io.github.data4all.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * this class represent the model for OrientationListener
 * 
 * @author: Steeve, fkirchge
 */
public class DeviceOrientation implements Parcelable {

    /** rotation around the Z axis */
    private float azimuth;
    /** rotation around the X axis */
    private float pitch;
    /** rotation around Y axis */
    private float roll;

    private long timestamp;

    /**
     * CREATOR that generates instances of {@link DeviceOrientation} from a
     * Parcel
     */
    public static final Parcelable.Creator<DeviceOrientation> CREATOR = new Parcelable.Creator<DeviceOrientation>() {
        public DeviceOrientation createFromParcel(Parcel in) {
            return new DeviceOrientation(in);
        }

        public DeviceOrientation[] newArray(int size) {
            return new DeviceOrientation[size];
        }
    };

    public DeviceOrientation(float azimuth, float pitch, float roll,
            long timestamp) {
        this.azimuth = azimuth;
        this.pitch = pitch;
        this.roll = roll;
        this.timestamp = timestamp;
    }

    /**
     * Constructor to create a node from a parcel
     * 
     * @param in
     */
    private DeviceOrientation(Parcel in) {
        azimuth = in.readFloat();
        pitch = in.readFloat();
        roll = in.readFloat();
        timestamp = in.readLong();
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        return (o == this)
                || ((o instanceof DeviceOrientation)
                        && azimuth == ((DeviceOrientation) o).getAzimuth()
                        && pitch == ((DeviceOrientation) o).getPitch()
                        && roll == ((DeviceOrientation) o).getRoll() && timestamp == ((DeviceOrientation) o)
                        .getTimestamp());
    }

    public boolean equalsTo(float azimmuth, float pitch, float roll,
            long timestamp) {
        return this.azimuth == azimuth && this.pitch == pitch
                && this.timestamp == timestamp;
    }

    /** all getter and setter Method */
    public float getAzimuth() {
        return azimuth;
    }

    public float getPitch() {
        return pitch;
    }

    public float getRoll() {
        return roll;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(azimuth);
        dest.writeFloat(pitch);
        dest.writeFloat(roll);
        dest.writeLong(timestamp);
    }
}
