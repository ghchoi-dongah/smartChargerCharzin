package com.dongah.smartcharger.plc.receive;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceDetailReceive {

    private static final Logger logger = LoggerFactory.getLogger(ServiceDetailReceive.class);

    byte[] data;
    int len;
    DataTransformation dataTransformation = new DataTransformation();

    public ServiceDetailReceive(byte[] data, int len) {
        this.data = data;
        this.len = len;
    }

    /**
     * ServiceDetail Report
     * PacketType : 0x84
     * body length : 60
     */
    public void doReceive() {
        try {
            byte SessionIDLength = data[8];
            byte[] mReserved1 = new byte[3];
            System.arraycopy(data, 9, mReserved1, 0, mReserved1.length);

            byte[] mSessionID = new byte[8];
            System.arraycopy(data, 12, mSessionID, 0, mSessionID.length);

            byte ResponseCode = data[20];

            byte[] mReserved2 = new byte[3];
            System.arraycopy(data, 21, mReserved2, 0, mReserved2.length);

            byte[] mServiceID = new byte[2];
            System.arraycopy(data, 24, mServiceID, 0, mServiceID.length);
            short ServiceID = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mServiceID, 1));

            byte[] mReserved3 = new byte[3];
            System.arraycopy(data, 26, mReserved3, 0, mReserved3.length);

        } catch (Exception e) {
            logger.error(" ServiceDetailReceive error : {}", e.getMessage());
        }
    }
}
