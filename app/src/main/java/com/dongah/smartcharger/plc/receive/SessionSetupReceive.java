package com.dongah.smartcharger.plc.receive;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionSetupReceive {

    private static final Logger logger = LoggerFactory.getLogger(SessionSetupReceive.class);

    byte[] data;
    int len;
    DataTransformation dataTransformation = new DataTransformation();


    public SessionSetupReceive(byte[] data, int len) {
        this.data = data;
        this.len = len;
    }

    /**
     * SessionSetup Report
     * PacketType : 0x82
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

            byte[] mDateTimeNow = new byte[4];
            System.arraycopy(data, 24, mDateTimeNow, 0, mDateTimeNow.length);
            String DateTimeNow = dataTransformation.BCDtoString(mDateTimeNow);
            String DateTimeNow1 = dataTransformation.BCDtoString(dataTransformation.changeByteOrder(mDateTimeNow,1));

            byte EVSEIDLength = data[28];
            byte Reserved3 = data[29];

            byte[] mEVSEID = new byte[38];
            System.arraycopy(data, 30, mEVSEID, 0, mEVSEID.length);

        } catch (Exception e) {
            logger.error(" SessionSetupReceive error : {}", e.getMessage());
        }
    }
}
