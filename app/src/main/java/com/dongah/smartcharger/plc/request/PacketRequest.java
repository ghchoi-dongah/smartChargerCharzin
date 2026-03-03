package com.dongah.smartcharger.plc.request;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class PacketRequest {

    private static final Logger logger = LoggerFactory.getLogger(PacketRequest.class);

    DataTransformation dataTransformation = new DataTransformation();
    short bodyLen;
    HeaderReq headerReq;
    byte msgType;

    /**
     *
     * @param packetType
     * @param bodyLen
     * @param msgType 0x73 SLAC ACK Request 0x02
     *                0x74 SDP ACK  Request 0x02
     *                0x7A AuthInfoSetup Request 0x02
     *
     *                나머지 Request 0x00
     */
    public PacketRequest(byte packetType, short bodyLen, byte msgType) {
        try {
            this.bodyLen = bodyLen;
            this.msgType = msgType;
            headerReq = new HeaderReq(packetType,
                    dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(bodyLen),1),
                    this.msgType,
                    (byte) 0x01);
        } catch (Exception e) {
            logger.error(" PacketRequest init error : {}", e.getMessage());
        }
    }

    /** *
     * Using packet
     * 1) 0x71: INIT Request (EVSE->SECC)
     * 2) 0x73: SLAC ACK (EVSE->SECC)
     * 3) 0x74: SDP ACK (EVSE->SECC)
     * 4) 0x77: StopAll ACK (EVSE->SECC)
     * 5) 0x79: ConfigCgheck Request (EVSE->SECC)
     *
     *
     * 소수점 없이
     * @param duty uint16_t : 단위[%], 53.4% = 534
     * @param cpVoltage int16_t : 단위[0.1V], 12.3V = 123, -12.4V = -124
     * @return byte array
     */
    public byte[] onMakeRequestData(short duty, short cpVoltage) {
        try {
            byte[] bodyData = new byte[bodyLen];

            bodyData[0] = (byte) 0x01;      //Normal Request : 1

            byte[] info = new byte[3];
            Arrays.fill(info, (byte) 0x00);
            System.arraycopy(info, 0, bodyData, 1, info.length);

            byte[] dutyByte = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(duty), 1);
            System.arraycopy(dutyByte, 0, bodyData, 4, 2);

            byte[] cpVoltageByte = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(cpVoltage), 1);
            System.arraycopy(cpVoltageByte, 0, bodyData, 6, 2);

            byte checksum = dataTransformation.checkSum(bodyData, bodyData.length);

            //request data 정리
            int packetLen = 8 + bodyLen + 2;
            byte[] requestData = new byte[packetLen];      //8+6+2
            System.arraycopy(headerReq.dataSet(), 0, requestData, 0, 8);
            System.arraycopy(bodyData, 0, requestData, 8, bodyLen);
            requestData[packetLen-2] = (byte) 0xa3;
            requestData[packetLen-1] = checksum;
            return requestData;

        } catch (Exception e) {
            logger.error(" PacketRequest onMakeRequest error : {}", e.getMessage());
        }
        return null;
    }
}
