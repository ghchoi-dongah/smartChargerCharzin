package com.dongah.smartcharger.plc.request;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BootInfoReq  {

    private static final Logger logger = LoggerFactory.getLogger(BootInfoReq.class);

    DataTransformation dataTransformation = new DataTransformation();
    byte packetType;
    short bodyLen;
    HeaderReq headerReq;
    byte msgType;


    public BootInfoReq(byte packetType, short bodyLen, byte msgType) {
        try {
            this.packetType = packetType;
            this.bodyLen = bodyLen;
            this.msgType = msgType;
            headerReq = new HeaderReq(packetType,
                    dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(bodyLen),1),
                    this.msgType,
                    (byte) 0x01);
        } catch (Exception e) {
            logger.error(" BootInfoRequest init error : {}", e.getMessage());
        }
    }


    public byte[] makeBootInfoReq(String cmd) {
        try {
            byte[] bodyData = new byte[bodyLen];

            bodyData = cmd.getBytes();

            byte checksum = dataTransformation.checkSum(bodyData, bodyData.length);

            //request data 정리   body length = 6
            int packetLen = 8 + bodyLen + 2;
            byte[] requestData = new byte[packetLen];      //8+6+2
            System.arraycopy(headerReq.dataSet(), 0, requestData, 0, 8);
            System.arraycopy(bodyData, 0, requestData, 8, bodyLen);

            requestData[packetLen-2] = (byte) 0xa3;
            requestData[packetLen-1] = checksum;
            return requestData;
        } catch (Exception e) {
            logger.error(" makeBootInfoReq error :  {}", e.getMessage());
        }
        return null;
    }

}
