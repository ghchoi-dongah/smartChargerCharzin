package com.dongah.smartcharger.plc.receive;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SDPReceive {

    private static final Logger logger = LoggerFactory.getLogger(SDPReceive.class);

    byte[] data;
    int len;
    DataTransformation dataTransformation = new DataTransformation();


    public SDPReceive(byte[] data, int len) {
        this.data = data;
        this.len = len;
    }

    /**
     * SDP  Report
     * PacketType : 0x74
     * body length : 8
     */
    public void doReceive() {
        try {
            /** body parsing */
            byte Security = data[8];            //SECC Response, 0x10 : No TLS, 0x00 : TLS
            byte TransportProtocol = data[9];
            byte ErrorCode = data[10];

            byte[] mReserved = new byte[5];
            System.arraycopy(data, 11, mReserved, 0, mReserved.length);


        } catch (Exception e) {
            logger.error(" SDPReceive error : {}", e.getMessage());
        }
    }

}
