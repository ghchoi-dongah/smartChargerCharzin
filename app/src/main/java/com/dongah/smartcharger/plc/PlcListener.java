package com.dongah.smartcharger.plc;

public interface PlcListener {

    void onReceiveBuffer(byte[] date);
    void onSendBuffer(byte[] data);

}
