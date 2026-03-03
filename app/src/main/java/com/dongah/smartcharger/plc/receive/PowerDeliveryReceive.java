package com.dongah.smartcharger.plc.receive;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PowerDeliveryReceive {

    private static final Logger logger = LoggerFactory.getLogger(PowerDeliveryReceive.class);


    byte[] data;
    int len;
    DataTransformation dataTransformation = new DataTransformation();

    public PowerDeliveryReceive(byte[] data, int len) {
        this.data = data;
        this.len = len;
    }

    /**
     * PowerDelivery Report
     * PacketType : 0x89
     * body length : 24
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

            byte EVSEStatus = data[24];
            byte Progress = data[25];           //0:Start, 1:Stop, 2:Renegotiate 상태 값을 전달 (20240925 내용 변경 및 추가)

            byte[] mEVSEStatus = new byte[2];
            System.arraycopy(data, 26, mEVSEStatus, 0, mEVSEStatus.length);
            short EVSEStatusNotification = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mEVSEStatus, 1));

            byte[] mEVSEStatusRCD = new byte[4];
            System.arraycopy(data, 28, mEVSEStatusRCD, 0, mEVSEStatusRCD.length);
            int EVSEStatusRCD = dataTransformation.ByteArrayToInt(dataTransformation.changeByteOrder(mEVSEStatusRCD, 1));

        } catch (Exception e) {
            logger.error(" PowerDeliveryReceive error :{}", e.getMessage());
        }
    }
}
