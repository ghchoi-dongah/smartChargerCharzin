package com.dongah.smartcharger.rfcard;

public interface RfCardReaderListener {
    void onRfCardDataReceive(String cardNum, boolean value);
}
