package com.dongah.smartcharger.plc.receive;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorizationReceive {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationReceive.class);

    byte[] data;
    int len;
    DataTransformation dataTransformation = new DataTransformation();

    public AuthorizationReceive(byte[] data, int len) {
        this.data = data;
        this.len = len;
    }

    /**
     * Authorization Report
     * PacketType : 0x87
     * body length : 16
     */
    public void doReceive() {
        try {
            byte SessionIDLength = data[8];
            byte[] mReserved1 = new byte[3];
            System.arraycopy(data, 9, mReserved1, 0, mReserved1.length);
            int Reserved1 = dataTransformation.ByteArrayToInt(dataTransformation.changeByteOrder(mReserved1, 1));
            byte[] mSessionID = new byte[8];
            System.arraycopy(data, 12, mSessionID, 0, mSessionID.length);

            byte ResponseCode = data[20];
            byte EVSEProcessing = data[21];

            byte[] mReserved2 = new byte[2];
            System.arraycopy(data, 22, mReserved2, 0, mReserved2.length);

        } catch (Exception e) {
            logger.error(" AuthorizationReceive error : {}", e.getMessage());
        }
    }
}
