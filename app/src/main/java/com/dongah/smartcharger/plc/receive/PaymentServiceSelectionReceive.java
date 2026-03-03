package com.dongah.smartcharger.plc.receive;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentServiceSelectionReceive {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceSelectionReceive.class);

    byte[] data;
    int len;
    DataTransformation dataTransformation = new DataTransformation();

    public PaymentServiceSelectionReceive(byte[] data, int len) {
        this.data = data;
        this.len = len;
    }


    /**
     * PaymentServiceSelection Report
     * PacketType : 0x85
     * body length : 16
     */
    public void doReceive() {
        try {
            byte SessionIDLength = data[8];
            byte[] mReserved1 = new byte[3];            //SessionSetupRes의 SessionIDValue
            System.arraycopy(data, 9, mReserved1, 0, mReserved1.length);
            int Reserved1 = dataTransformation.ByteArrayToInt(dataTransformation.changeByteOrder(mReserved1, 1));

            byte[] mSessionID = new byte[8];
            System.arraycopy(data, 12, mSessionID, 0, mSessionID.length);

            byte ResponseCode = data[20];               //

            byte[] mReserved2 = new byte[3];
            System.arraycopy(data, 21, mReserved2, 0, mReserved2.length);


        } catch (Exception e) {
            logger.error(" PaymentServiceSelectionReceive error : {}", e.getMessage());

        }
    }
}
