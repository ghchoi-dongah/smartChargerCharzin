package com.dongah.smartcharger.plc.request;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class StartRequest {

    private static final Logger logger = LoggerFactory.getLogger(StartRequest.class);


    DataTransformation dataTransformation = new DataTransformation();
    short bodyLen;
    HeaderReq headerReq;
    byte msgType;



    byte checksum;

    public StartRequest(byte packetType, short bodyLen, byte msgType)  {
        try {
            this.bodyLen = bodyLen;
            this.msgType = msgType;
            headerReq = new HeaderReq(packetType,
                    dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(bodyLen),1),
                    this.msgType,
                    (byte) 0x01);


        } catch (Exception e) {
            logger.error(" StartRequest init : {}", e.getMessage());
        }
    }

    public byte[] makeStartRequest(byte pWMDuty) {
        try {
            //body length
            byte[] startBody = new byte[bodyLen];
            startBody[0] = (byte) 0x01;                 // 0: 61851 / 1: ISO15118-2 AC / 2: ISO15118-2 DC
            startBody[1] = (byte) 0x00;                 // 0: OFF / 1: ON
            startBody[2] = (byte) 0x01;                 // 1: AC
            startBody[3] = pWMDuty;                     // Start Duty Value (EV: 5%, Test:53%)
            byte[] timeValue = new byte[4];
            Arrays.fill(timeValue, (byte) 0x00);
            System.arraycopy(timeValue, 0, startBody, 4, timeValue.length);     //UnixTimeValue(optional)
            byte[] reserved = new byte[8];
            Arrays.fill(reserved, (byte) 0x00);
            System.arraycopy(reserved, 0, startBody, 8, reserved.length);     //UnixTimeValue(optional)
            checksum = dataTransformation.checkSum(startBody, startBody.length);

            //request data 정리
            int packetLen = 8 + bodyLen + 2;
            byte[] requestData = new byte[packetLen];      //8+16+2
            System.arraycopy(headerReq.dataSet(), 0, requestData, 0, 8);
            System.arraycopy(startBody, 0, requestData, 8, bodyLen);
            requestData[packetLen-2] = (byte) 0xa3;
            requestData[packetLen-1] = checksum;
            return requestData;

        } catch (Exception e) {
            logger.error(" makeStartRequest error : {}", e.getMessage());
        }
        return null;
    }
}
