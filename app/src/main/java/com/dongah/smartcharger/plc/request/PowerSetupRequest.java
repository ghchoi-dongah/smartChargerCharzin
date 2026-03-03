package com.dongah.smartcharger.plc.request;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PowerSetupRequest {

    private static final Logger logger = LoggerFactory.getLogger(PowerSetupRequest.class);

    DataTransformation dataTransformation = new DataTransformation();
    short bodyLen;
    HeaderReq headerReq;
    byte msgType;


    final byte NotificationMaxDelay = 0x00;
    /**
     * 0:iso1EVSENotificationType_None,
     * 1:iso1EVSENotificationType_StopCharging,
     * 2:iso1EVSENotificationType_ReNegotiation
     */
    final byte EVSENotification = 0x00;
    final byte EvsePMaxMultiple = 0x03;
    final byte EvsePMaxUnit = 0x05;                 // iso1unitSymbolType_W = 5,
    final short EvsePMaxValue = 7;                  // 예) 7x1000 = 7kW
    final byte EvseMaxVoltageMultiple = 0x01;       // 예)10 ^ 3 = 1000
    final byte EvseMaxVoltageUnit = 0x04;           // iso1unitSymbolType_V = 4,
    final short EvseMaxVoltageValue = 22;           // 예) 22 x 10 = 220V
    final byte EvseMaxCurrentMultiple = 0x00;       // // 예)10 ^ 0 = 1
    final byte EvseMaxCurrentUinit = 0x03;          // iso1unitSymbolType_A = 3,
    final short EvseMaxCurrentValue = 32;           // 예) 32 x 1 = 32A / 32x220 = 7040 (약 7kW)
    final byte EvseMinCurrentMultiple = 0x00;       // 예) 100 = 1   * EvseMinCurrent 항목은 옵션
    final byte EvseMinCurrentUnit = 0x03;           // iso1unitSymbolType_A = 3
    final short EvseMinCurrentValue = 1;            // 예) 1 x 1 = 1A / 1x220 = 220W

    /**
     *
     * @param packetType 0x78
     * @param bodyLen 22
     * @param msgType 0x00
     */
    public PowerSetupRequest(byte packetType, short bodyLen, byte msgType) {
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

    public byte[] onMakeRequestData() {
        try {
            byte[] bodyData = new byte[bodyLen];

            byte[] RCD = dataTransformation.IntToByteArray(0);
            System.arraycopy(RCD, 0, bodyData, 0, 4);
            bodyData[4] = NotificationMaxDelay;
            bodyData[5] = EVSENotification;
            bodyData[6] = EvsePMaxMultiple;
            bodyData[7] = EvsePMaxUnit;

            byte[] maxKw = new byte[2];
            maxKw = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(EvsePMaxValue), 1);
            System.arraycopy(maxKw, 0, bodyData, 8, 2);
            bodyData[10] = EvseMaxVoltageMultiple;
            bodyData[11] = EvseMaxVoltageUnit;
            byte[] maxVoltage = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(EvseMaxVoltageValue), 1);
            System.arraycopy(maxVoltage, 0, bodyData, 12, 2);
            bodyData[14] = EvseMaxCurrentMultiple;
            bodyData[15] = EvseMaxCurrentUinit;
            byte[] maxCurrent = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(EvseMaxCurrentValue), 1);
            System.arraycopy(maxCurrent, 0, bodyData, 16, 2);
            bodyData[18] = EvseMinCurrentMultiple;
            bodyData[19] = EvseMinCurrentUnit;
            byte[] minCurrent = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(EvseMinCurrentValue), 1);
            System.arraycopy(minCurrent, 0, bodyData, 20, 2);
            byte checksum = dataTransformation.checkSum(bodyData, bodyData.length);

            //request data 정리
            int packetLen = 8 + bodyLen + 2;
            byte[] requestData = new byte[packetLen];      //8+22+2
            System.arraycopy(headerReq.dataSet(), 0, requestData, 0, 8);
            System.arraycopy(bodyData, 0, requestData, 8, bodyLen);
            requestData[packetLen-2] = (byte) 0xa3;
            requestData[packetLen-1] = checksum;
            return requestData;

//            byte[] requestData = new byte[32];      //22+8+2
//            System.arraycopy(headerReq.dataSet(), 0, requestData, 0, 8);
//            System.arraycopy(bodyData, 0, requestData, 8, bodyLen);
//            requestData[30] = (byte) 0xa3;
//            requestData[31] = checksum;
//            return requestData;


        } catch (Exception e) {
            logger.error(" {}", e.getMessage());
        }
        return null;
    }
}
