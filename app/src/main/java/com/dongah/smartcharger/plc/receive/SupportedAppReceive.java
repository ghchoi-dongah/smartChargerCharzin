package com.dongah.smartcharger.plc.receive;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SupportedAppReceive {

    private static final Logger logger = LoggerFactory.getLogger(SupportedAppReceive.class);


    byte[] data;
    int len;
    DataTransformation dataTransformation = new DataTransformation();


    public SupportedAppReceive(byte[] data, int len) {
        this.data = data;
        this.len = len;
    }

    /**
     * SupportedApplicationProtocol Report
     * PacketType : 0x81
     * body length : 4
     */
    public void doReceive() {
        try {
            byte SelectedSchemaID = data[8];
            byte ErrorCode = data[9];
            byte SelectedProtocol = data[10];               //ISO15118-2
            byte TLSHandshake = data[11];                   //0: not done, 1: complete
        } catch (Exception e) {
            logger.error( " SupportedAppReceive error : {}", e.getMessage());
        }
    }
}
