package com.dongah.smartcharger.plc.receive;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentDetailsReceive {

    private static final Logger logger = LoggerFactory.getLogger(PaymentDetailsReceive.class);


    byte[] data;
    int len;
    DataTransformation dataTransformation = new DataTransformation();


    public PaymentDetailsReceive(byte[] data, int len) {
        this.data = data;
        this.len = len;
    }

    /**
     * PaymentDetails Report
     * PacketType : 0x86
     * body length : 36
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

            byte[] mGenChallenge = new byte[16];
            System.arraycopy(data, 24, mGenChallenge, 0, mGenChallenge.length);

            byte[] mEVSETimeStamp = new byte[4];         //TimeStamp
            System.arraycopy(data, 40, mEVSETimeStamp, 0, mEVSETimeStamp.length);
            int EVSETimeStamp = dataTransformation.ByteArrayToInt(dataTransformation.changeByteOrder(mEVSETimeStamp, 1));

        } catch (Exception e) {
            logger.error(" PaymentDetailsReceive error : {} ", e.getMessage());
        }
    }
}
