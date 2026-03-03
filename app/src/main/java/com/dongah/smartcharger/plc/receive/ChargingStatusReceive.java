package com.dongah.smartcharger.plc.receive;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChargingStatusReceive {

    private static final Logger logger = LoggerFactory.getLogger(ChargingStatusReceive.class);


    byte[] data;
    int len;
    DataTransformation dataTransformation = new DataTransformation();

    public ChargingStatusReceive(byte[] data, int len) {
        this.data = data;
        this.len = len;
    }


    /**
     * ChargingStatus Report
     * PacketType : 0x8A
     * body length : 32
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
            byte MeterInfoIsUsed = data[21];
            byte ReceiptRequired = data[22];            //0: MeteringReciptMessage 미전송, 1: MeteringReciptMessage 전송
            byte Reserved2 = data[23];

            byte EVSEStatusNotification = data[24];
            byte Reserved3 = data[25];
            byte[] mEVSEStatusMaxDelay = new byte[2];
            System.arraycopy(data, 26, mEVSEStatusMaxDelay, 0, mEVSEStatusMaxDelay.length);
            short EVSEStatusMaxDelay = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mEVSEStatusMaxDelay, 1));

            byte[] mRCD = new byte[4];
            System.arraycopy(data, 28, mRCD, 0, mRCD.length);
            int RCD = dataTransformation.ByteArrayToInt(dataTransformation.changeByteOrder(mRCD, 1));

            byte[] mEVSEMaxCurrent = new byte[4];
            System.arraycopy(data, 32, mEVSEMaxCurrent, 0, mEVSEMaxCurrent.length);
            int EVSEMaxCurrent = dataTransformation.ByteArrayToInt(dataTransformation.changeByteOrder(mEVSEMaxCurrent, 1));

            byte[] mSAScheduleTupleID = new byte[2];
            System.arraycopy(data, 36, mSAScheduleTupleID, 0, mSAScheduleTupleID.length);
            int SAScheduleTupleID = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mSAScheduleTupleID, 1));

            byte[] mReserved4 = new byte[2];
            System.arraycopy(data, 38, mReserved1, 0, mReserved4.length);
            int Reserved4 = dataTransformation.ByteArrayToInt(dataTransformation.changeByteOrder(mReserved4, 1));

        } catch (Exception e) {
            logger.error(" ChargingStatusReceive error : {}", e.getMessage());
        }
    }
}
