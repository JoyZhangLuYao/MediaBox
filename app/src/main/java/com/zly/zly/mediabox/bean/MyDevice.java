package com.zly.zly.mediabox.bean;

import android.bluetooth.BluetoothDevice;

/**
 * Created by ZhangLuyao on 2017/3/30.
 */

public class MyDevice {
    public MyDevice() {
    }

    public MyDevice(BluetoothDevice bluetoothDevice, boolean isLink) {
        this.bluetoothDevice = bluetoothDevice;
        this.isLink = isLink;
    }

    BluetoothDevice bluetoothDevice;
    boolean isLink;

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public boolean isLink() {
        return isLink;
    }

    public void setLink(boolean link) {
        isLink = link;
    }

    @Override
    public String toString() {
        return "MyDevice{" +
                "bluetoothDevice=" + bluetoothDevice +
                ", isLink=" + isLink +
                '}';
    }
}
