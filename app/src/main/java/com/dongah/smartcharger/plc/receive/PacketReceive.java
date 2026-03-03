package com.dongah.smartcharger.plc.receive;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketReceive {

    private static final Logger logger = LoggerFactory.getLogger(PacketReceive.class);


    byte[] data;
    int len;
    DataTransformation dataTransformation = new DataTransformation();


    public PacketReceive(byte[] data, int len) {
        this.data = data;
        this.len = len;
    }

    /**
     * using packet
     * 1) 0x70: ConfigSetup Response (SECC -> EVSE) **
     * 2) 0x72: Start Response (SECC -> EVCC) **
     * 3) 0x76: StopAll Request (EVSE->SECC) **
     * 4) 0x77: StopAll Report (SECC->EVSE) **
     * 5) 0x78: PowerSetup Response (SECC->EVSE) **
     * 6) 0x7A: AuthInfoSetup Response (SECC->EVSE) **
     */
    public void doReceive() {
        try {
            /** Header */
            // 패킷 타입
            byte packetType = data[1];
            // Body의 길이
            byte[] len = new byte[2];
            System.arraycopy(data, 2, len, 0, len.length);
            short bodyLength = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(len, 1));     //Big -> LittleEndian
            //MgType (0: General , 2: ISO1(ISO15118-2) , 1: Reserved , 3: Reserved)
            byte mgType = data[4];
            byte Sender = data[5];
            byte[] mReserved = new byte[2];
            System.arraycopy(data, 6, mReserved, 0, mReserved.length);
            short Reserved = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mReserved, 1));

            /** body parsing */
            // ack (Normal ACK : 1 , AbNormal : 0)
            byte ack = data[8];
            byte[] info = new byte[3];
            System.arraycopy(data, 9, info, 0, info.length);
            // duty (uint16_t : 단위[%], 53.4% = 534)
            byte[] mDuty = new byte[2];
            System.arraycopy(data, 12, mDuty, 0, mDuty.length);
            short duty = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mDuty, 1));                      //LittleEndian  -> BigEndian
            // duty (int16_t : 단위[0.1V], 12.3V = 123, -12.4V = -124)
            byte[] mCpVoltage = new byte[2];
            System.arraycopy(data, 14, mCpVoltage, 0, mCpVoltage.length);
            short cpVoltage = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mCpVoltage, 1));            //LittleEndian  -> BigEndian

        } catch (Exception e) {
            logger.error(" PacketReceive doReceive error : {}", e.getMessage());
        }
    }

}
