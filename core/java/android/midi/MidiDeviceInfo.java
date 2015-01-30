/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.midi;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class contains information to describe a MIDI device.
 * For now we only have information that can be retrieved easily for USB devices,
 * but we will probably expand this in the future.
 *
 * This class is just an immutable object to encapsulate the MIDI device description.
 * Use the MidiDevice class to actually communicate with devices.
 *
 * CANDIDATE FOR PUBLIC API
 * @hide
 */
public class MidiDeviceInfo implements Parcelable {

    private static final String TAG = "MidiDeviceInfo";

    /**
     * Constant representing USB MIDI devices for {@link #getType}
     */
    public static final int TYPE_USB = 1;

    /**
     * Constant representing virtual (software based) MIDI devices for {@link #getType}
     */
    public static final int TYPE_VIRTUAL = 2;

    private final int mType;    // USB or virtual
    private final int mId;      // unique ID generated by MidiService
    private final int mInputPortCount;
    private final int mOutputPortCount;
    private final Bundle mProperties;

    /**
     * Bundle key for the device's manufacturer name property.
     * Used with the {@link android.os.Bundle} returned by {@link #getProperties}.
     * Matches the USB device manufacturer name string for USB MIDI devices.
     */
    public static final String PROPERTY_MANUFACTURER = "manufacturer";

    /**
     * Bundle key for the device's model name property.
     * Used with the {@link android.os.Bundle} returned by {@link #getProperties}
     * Matches the USB device product name string for USB MIDI devices.
     */
    public static final String PROPERTY_MODEL = "model";

    /**
     * Bundle key for the device's serial number property.
     * Used with the {@link android.os.Bundle} returned by {@link #getProperties}
     * Matches the USB device serial number for USB MIDI devices.
     */
    public static final String PROPERTY_SERIAL_NUMBER = "serial_number";

    /**
     * Bundle key for the device's {@link android.hardware.usb.UsbDevice}.
     * Only set for USB MIDI devices.
     * Used with the {@link android.os.Bundle} returned by {@link #getProperties}
     */
    public static final String PROPERTY_USB_DEVICE = "usb_device";

    /**
     * Bundle key for the device's ALSA card number.
     * Only set for USB MIDI devices.
     * Used with the {@link android.os.Bundle} returned by {@link #getProperties}
     */
    public static final String PROPERTY_ALSA_CARD = "alsa_card";

    /**
     * Bundle key for the device's ALSA device number.
     * Only set for USB MIDI devices.
     * Used with the {@link android.os.Bundle} returned by {@link #getProperties}
     */
    public static final String PROPERTY_ALSA_DEVICE = "alsa_device";

    /**
     * MidiDeviceInfo should only be instantiated by MidiService implementation
     * @hide
     */
    public MidiDeviceInfo(int type, int id, int numInputPorts, int numOutputPorts,
            Bundle properties) {
        mType = type;
        mId = id;
        mInputPortCount = numInputPorts;
        mOutputPortCount = numOutputPorts;
        mProperties = properties;
    }

    /**
     * Returns the type of the device.
     *
     * @return the device's type
     */
    public int getType() {
        return mType;
    }

    /**
     * Returns the ID of the device.
     * This ID is generated by the MIDI service and is not persistent across device unplugs.
     *
     * @return the device's ID
     */
    public int getId() {
        return mId;
    }

    /**
     * Returns the device's number of input ports.
     *
     * @return the number of input ports
     */
    public int getInputPortCount() {
        return mInputPortCount;
    }

    /**
     * Returns the device's number of output ports.
     *
     * @return the number of output ports
     */
    public int getOutputPortCount() {
        return mOutputPortCount;
    }

    /**
     * Returns the {@link android.os.Bundle} containing the device's properties.
     *
     * @return the device's properties
     */
    public Bundle getProperties() {
        return mProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MidiDeviceInfo) {
            return (((MidiDeviceInfo)o).mId == mId);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return mId;
    }

    @Override
    public String toString() {
        return ("MidiDeviceInfo[mType=" + mType +
                ",mInputPortCount=" + mInputPortCount +
                ",mOutputPortCount=" + mOutputPortCount +
                ",mProperties=" + mProperties);
    }

    public static final Parcelable.Creator<MidiDeviceInfo> CREATOR =
        new Parcelable.Creator<MidiDeviceInfo>() {
        public MidiDeviceInfo createFromParcel(Parcel in) {
            int type = in.readInt();
            int id = in.readInt();
            int inputPorts = in.readInt();
            int outputPorts = in.readInt();
            Bundle properties = in.readBundle();
            return new MidiDeviceInfo(type, id, inputPorts, outputPorts, properties);
        }

        public MidiDeviceInfo[] newArray(int size) {
            return new MidiDeviceInfo[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(mType);
        parcel.writeInt(mId);
        parcel.writeInt(mInputPortCount);
        parcel.writeInt(mOutputPortCount);
        parcel.writeBundle(mProperties);
   }
}