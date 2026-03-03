package com.dongah.smartcharger.plc.request;

import com.dongah.smartcharger.basefunction.GlobalVariables;
import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;

public class OtaOrderRequest {

    private static final Logger logger = LoggerFactory.getLogger(OtaOrderRequest.class);

    private static final short UNIT_SIZE = 1024;
    private static final String FILE_NAME = "secc_OTA_v0.12.13.bin";
    private static final short MAX_WAIT = 300;

    private static final String FW_PLATFORM = "00.01.02_003";
    private static final String FW_MAIN_APP = "00.01.02_003";

    DataTransformation dataTransformation = new DataTransformation();
    short bodyLen;
    HeaderReq headerReq;
    byte msgType;


    public OtaOrderRequest(byte packetType) {
        try {
            //packetType = 0x18;
            this.bodyLen = 48;
            this.msgType = (byte) 0x01;
            headerReq = new HeaderReq(packetType,
                    dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(this.bodyLen),1),
                    this.msgType,
                    (byte) 0x01);

        } catch (Exception e) {
            logger.error( "OtaOrderRequest init {}", e.getMessage());
        }
    }

    long realSize = 0;
    public byte[] makeOtaOrderRequest() {
        try {
            //body length
            byte[] otaOrderBody = new byte[bodyLen];

            byte[] fileSize = new byte[4];
            Arrays.fill(fileSize, (byte) 0x00);
            File file = new File(GlobalVariables.getRootPath() + File.separator + FILE_NAME);
            if (file.exists()) {
                realSize = file.length();
                fileSize =  dataTransformation.changeByteOrder(longTo4Bytes(file.length()), 1);
            }

            System.arraycopy(fileSize, 0, otaOrderBody, 0, fileSize.length);     //UnixTimeValue(optional)
            byte[] unitSize = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(UNIT_SIZE), 1);
            System.arraycopy(unitSize, 0, otaOrderBody, 4, 2);

            short totalPackets = (short) Math.ceil((double) file.length() / UNIT_SIZE);
            GlobalVariables.setTotalPackets(totalPackets);
            byte[] count = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(totalPackets),1);
            System.arraycopy(count, 0, otaOrderBody, 6, 2);

            short lastPacketSize = (short) (file.length() % UNIT_SIZE) ;
            if (lastPacketSize == 0) lastPacketSize = UNIT_SIZE;
            byte[] lastSize = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(lastPacketSize),1);
            System.arraycopy(lastSize, 0, otaOrderBody, 8, 2);

            byte[] maxWait = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(MAX_WAIT),1);
            System.arraycopy(maxWait, 0, otaOrderBody, 10, 2);

            short mHWV = 100;
            byte[] hwv = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(mHWV),1);
            System.arraycopy(hwv, 0, otaOrderBody, 12, 2);

            byte[] fwPlatform = new byte[12];
            fwPlatform = FW_PLATFORM.getBytes();
            System.arraycopy(fwPlatform, 0, otaOrderBody, 14, 12);

            byte[] version = new byte[12];
            version = FW_MAIN_APP.getBytes();
            System.arraycopy(version, 0, otaOrderBody, 26, 12);

            byte[] syncKey = new byte[8];
            Arrays.fill(syncKey, (byte) 0x00);
            System.arraycopy(syncKey, 0, otaOrderBody, 38, 8);

            otaOrderBody[46] = (byte) 0x00;
            otaOrderBody[47] = (byte) 0x00;

            byte checksum = dataTransformation.checkSum(otaOrderBody, otaOrderBody.length);

            //request data 정리
            int packetLen = 8 + bodyLen + 2;
            byte[] requestData = new byte[packetLen];      //8+16+2
            System.arraycopy(headerReq.dataSet(), 0, requestData, 0, 8);
            System.arraycopy(otaOrderBody, 0, requestData, 8, bodyLen);
            requestData[packetLen-2] = (byte) 0xa3;
            requestData[packetLen-1] = checksum;
            return requestData;


        } catch (Exception e) {
            logger.error(" makeOtaOrderRequest error : {}", e.getMessage());
        }
        return null;
    }

    public byte[] longTo4Bytes(long value) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (value >> 24);
        bytes[1] = (byte) (value >> 16);
        bytes[2] = (byte) (value >> 8);
        bytes[3] = (byte) (value);
        return bytes;
    }


}
