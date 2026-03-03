package com.dongah.smartcharger.plc.receive;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlacReceive {

    private static final Logger logger = LoggerFactory.getLogger(SlacReceive.class);


    byte[] data;
    int len;
    DataTransformation dataTransformation = new DataTransformation();

    public SlacReceive(byte[] data, int len) {
        this.data = data;
        this.len = len;
    }

    /**
     * SLAC Report (SECC->EVSE)
     * PacketType : 0x73
     * body length : 12
     */
    public void doReceive() {
        try {
            byte errorCode = data[8];
            byte[] reserved1 = new byte[3];
            System.arraycopy(data, 9, reserved1, 0, reserved1.length);
            byte averageAttenuation = data[12];
            byte reserved2 = data[13];
            byte[] pevMAC = new byte[6];
            System.arraycopy(data, 14, pevMAC, 0, pevMAC.length);

        } catch (Exception e) {
            logger.error(" Slac Receive error : {}", e.getMessage());
        }
    }
}
