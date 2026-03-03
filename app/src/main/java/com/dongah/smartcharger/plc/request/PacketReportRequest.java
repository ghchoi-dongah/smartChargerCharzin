package com.dongah.smartcharger.plc.request;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketReportRequest {

    private static final Logger logger = LoggerFactory.getLogger(PacketReportRequest.class);

    DataTransformation dataTransformation = new DataTransformation();
    byte packetType;
    short bodyLen;
    HeaderReq headerReq;
    byte msgType;


    /**
     * report request
     * @param packetType : * 0x81 SupportedApplicationProtocol ack (EVSE->SECC)
     *                     0x82 SessionSetup ACK (EVSE->SECC)
     *                     0x83 ServiceDiscovery ACK (EVSE->SECC)
     *                     0x84 ServiceDetail ACK (EVSE->SECC)
     *                     0x85 PaymentServiceSelection ACK (EVSE->SECC)
     *                     0x86 PaymentDetails ACK (EVSE->SECC)
     *                     0x87 Authorization ACK (EVSE->SECC)
     *                     0x88 ChargeParameterDiscovery ACK (EVSE->SECC)
     *                     0x89 PowerDelivery ACK (EVSE->SECC)
     *                     0x8A ChargingStatus ACK (without meterInfo) (EVSE->SECC)
     *                     0x8C SessionStop ACK (EVSE->SECC)
     *                     0x8D BatteryInfo ACK (EVSE->SECC)
     * @param bodyLen  6
     * @param msgType 0x02
     */
    public PacketReportRequest(byte packetType, short bodyLen, byte msgType) {
        try {
            this.packetType = packetType;
            this.bodyLen = bodyLen;
            this.msgType = msgType;
            headerReq = new HeaderReq(packetType,
                    dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(bodyLen),1),
                    this.msgType,
                    (byte) 0x01);
        } catch (Exception e) {
            logger.error(" PacketReportRequest init error : {}", e.getMessage());
        }
    }

    public byte[] onMakeRequestData(short duty, short cpVoltage) {
        try {
            byte[] bodyData = new byte[bodyLen];

            bodyData[0] = (byte) 0x01;      //Normal ACK : 1 / AbNormal : 0
            bodyData[1] = (byte) 0x00;

            byte[] dutyByte = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(duty), 1);
            System.arraycopy(dutyByte, 0, bodyData, 2, 2);

            byte[] cpVoltageByte = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(cpVoltage), 1);
            System.arraycopy(cpVoltageByte, 0, bodyData, 4, 2);

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
            logger.error(" PacketReportRequest onMakeRequest error : {}", e.getMessage());
        }
        return null;
    }
}
