package com.dongah.smartcharger.plc.receive;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceDiscoveryReceive {

    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscoveryReceive.class);

    byte[] data;
    int len;
    DataTransformation dataTransformation = new DataTransformation();

    public ServiceDiscoveryReceive(byte[] data, int len) {
        this.data = data;
        this.len = len;
    }

    /**
     * ServiceDiscovery Report
     * PacketType : 0x83
     * body length : 60
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

            byte[] mReserved2 = new byte[3];
            System.arraycopy(data, 21, mReserved2, 0, mReserved2.length);
            int Reserved2 = dataTransformation.ByteArrayToInt(dataTransformation.changeByteOrder(mReserved2, 1));

        } catch (Exception e) {
            logger.error(" ServiceDiscoveryReceive error : {}", e.getMessage());
        }
    }
}
