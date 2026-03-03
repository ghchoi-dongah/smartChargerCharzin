package com.dongah.smartcharger.plc.receive;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileDataAnswerReceive {

    private static final Logger logger = LoggerFactory.getLogger(FileDataAnswerReceive.class);

    byte[] data;
    int len;
    DataTransformation dataTransformation = new DataTransformation();

    public FileDataAnswerReceive(byte[] data, int len) {
        this.data = data;
        this.len = len;
    }

    public short[] doReceive() {
        short[] packetInfo = new short[2];
        try {
            byte[] mPacketNow = new byte[2];
            System.arraycopy(data, 4, mPacketNow, 0, mPacketNow.length);
            short packetNow = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mPacketNow, 1));
            packetInfo[0] = packetNow;
            //전체 패킷 수
            byte[] mPacketAll = new byte[2];
            System.arraycopy(data, 6, mPacketAll, 0, mPacketAll.length);
            short packetAllData =dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mPacketAll, 1));
            //code (0: Process Stop, 1: Run OK, 2: OTA Finish, 3~ : Error Code)
            packetInfo[1] = data[8];
        } catch (Exception e) {
            logger.error(" FileDataAnswerReceive error : {}", e.getMessage());
        }
        return packetInfo;
    }
}
