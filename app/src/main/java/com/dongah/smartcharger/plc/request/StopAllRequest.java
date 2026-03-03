package com.dongah.smartcharger.plc.request;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopAllRequest {

    private static final Logger logger = LoggerFactory.getLogger(StopAllRequest.class);

    DataTransformation dataTransformation = new DataTransformation();
    short bodyLen;
    HeaderReq headerReq;
    byte msgType;


    public StopAllRequest(byte packetType, short bodyLen, byte msgType) {
        try {
            this.bodyLen = bodyLen;
            this.msgType = msgType;
            headerReq = new HeaderReq(packetType,
                    dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(bodyLen),1),
                    this.msgType,
                    (byte) 0x01);
        } catch (Exception e) {
            logger.error(" StopAllRequest init : {}", e.getMessage());
        }
    }

    /**
     * *) CP전압, PWM 상태값을 포함하여 명령 전송, CP전압, PWM 정보에 따라 SECC의 동작 결정
     * *) CP전압 12V, PWM OFF 상태로 명령을 받으면, 곧바로 종료, StartReq 명령 대기 상태로 복귀
     *  @param info         "STOP" : Normal Stop, "HRST" : Board Reset Stop
     * @param duty          uint16_t : 단위[%], 53.4% = 534
     * @param cpVoltage     int16_t : 단위[0.1V], 12.3V = 123, -12.4V = -124
     * @return byte array
     */
    public byte[] makeStopAllRequest(String  info, short duty, short cpVoltage) {
        try {
            byte[] stopBody = new byte[bodyLen];
            byte[] mInfo = new byte[4];
            mInfo = info.getBytes();
            System.arraycopy(mInfo, 0, stopBody, 0, mInfo.length);
            byte[] dutyByte = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(duty), 1);
            System.arraycopy(dutyByte, 0, stopBody, 4, 2);

            byte[] cpVoltageByte = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(cpVoltage), 1);
            System.arraycopy(cpVoltageByte, 0, stopBody, 6, 2);
            byte checksum = dataTransformation.checkSum(stopBody, stopBody.length);

            //request data 정리
            int packetLen = 8 + bodyLen + 2;
            byte[] requestData = new byte[packetLen];      //8+8+2
            System.arraycopy(headerReq.dataSet(), 0, requestData, 0, 8);
            System.arraycopy(stopBody, 0, requestData, 8, bodyLen);
            requestData[packetLen-2] = (byte) 0xa3;
            requestData[packetLen-1] = checksum;
            return requestData;
        } catch (Exception e) {
            logger.error(" makeStopAllRequest error : {}", e.getMessage());
        }
        return null;
    }
}
