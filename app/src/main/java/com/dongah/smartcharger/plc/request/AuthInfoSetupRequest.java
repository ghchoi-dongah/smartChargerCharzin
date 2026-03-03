package com.dongah.smartcharger.plc.request;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class AuthInfoSetupRequest {

    private static final Logger logger = LoggerFactory.getLogger(AuthInfoSetupRequest.class);

    DataTransformation dataTransformation = new DataTransformation();
    short bodyLen;
    HeaderReq headerReq;
    byte msgType;




    public AuthInfoSetupRequest(byte packetType, short bodyLen, byte msgType) {
        try {
            this.bodyLen = bodyLen;
            headerReq = new HeaderReq(packetType,
                    dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(bodyLen),1),
                    this.msgType,
                    (byte) 0x01);

        } catch (Exception e) {
            logger.error(" AuthInfoSetupRequest init : {}", e.getMessage());
        }
    }

    public byte[] makeAuthInfoSetupRequest(short duty, short cpVoltage) {
        try {
            byte[] authInfoBody = new byte[bodyLen];
            authInfoBody[0] = (byte) 0x01;              //Authorization, 1: OK / 0: No
            byte[] info = new byte[3];
            Arrays.fill(info, (byte) 0x00);
            System.arraycopy(info, 0, authInfoBody, 1, info.length);
            byte[] dutyByte = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(duty), 1);
            System.arraycopy(dutyByte, 0, authInfoBody, 4, 2);              //uint16_t : 단위[%], 53.4% = 534

            byte[] cpVoltageByte = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(cpVoltage), 1);
            System.arraycopy(cpVoltageByte, 0, authInfoBody, 6, 2);    //int16_t : 단위[0.1V], 12.3V = 123, -12.4V = -124
            byte checksum = dataTransformation.checkSum(authInfoBody, authInfoBody.length);

            //request data 정리
            int packetLen = 8 + bodyLen + 2;
            byte[] requestData = new byte[packetLen];      //8+6+2
            System.arraycopy(headerReq.dataSet(), 0, requestData, 0, 8);
            System.arraycopy(authInfoBody, 0, requestData, 8, bodyLen);
            requestData[packetLen-2] = (byte) 0xa3;
            requestData[packetLen-1] = checksum;
            return requestData;
        } catch (Exception e) {
            logger.error(" makeAuthInfoSetupRequest error : {}", e.getMessage());
        }
        return null;
    }
}
