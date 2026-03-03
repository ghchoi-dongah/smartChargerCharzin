package com.dongah.smartcharger.plc.request;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ConfigSetupReq {

    private static final Logger logger = LoggerFactory.getLogger(ConfigSetupReq.class);

    DataTransformation dataTransformation = new DataTransformation();
    short bodyLen;
    HeaderReq headerReq;
    byte[] RCD = new byte[4];                     // (4byte)   0:false(누설 전류 없음), 1:true(누설 전류 탐지) [V2G2-359] 참조
    byte[] eEVSEPMaxValue = new byte[2];          //(2byte)  에) 7x1000 = 7kW
    byte[] eVSEMaxVoltageValue = new byte[2];     // (2byte) 예) 22 x 10 = 220V
    byte[] eVSEeMaxCurrentValue = new byte[2];    // (2byte) 예) 32 x 1 = 32A / 32x220 = 7040 (약 7kW)
    byte[] eVSEMinCurrentValue = new byte[2];;    // (2byte) 예) 1 x 1 = 1A / 1x220 = 220W
    byte[] eVSEid = new byte[7];                  // (7byte) 예) "KRG53E1"
    byte checksum;


    public ConfigSetupReq(byte packetType, short bodyLen) {
        try {
            this.bodyLen = bodyLen;
            headerReq = new HeaderReq(packetType,
                    dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(bodyLen),1),
                    (byte) 0x02,
                    (byte) 0x01);

        } catch (Exception e) {
            logger.error(" ConfigSetupReq init : {}", e.getMessage());
        }
    }

    public byte[] makeConfigSetupReq() {
        try {
            Arrays.fill(RCD, (byte) 0x00);
            Arrays.fill(eEVSEPMaxValue, (byte) 0x00);
            Arrays.fill(eVSEMaxVoltageValue, (byte) 0x00);
            Arrays.fill(eVSEeMaxCurrentValue, (byte) 0x00);
            Arrays.fill(eVSEMinCurrentValue, (byte) 0x00);
            Arrays.fill(eVSEid, (byte) 0x00);
            // body len
            byte[] configSetup = new byte[bodyLen];

            configSetup[0] = 0x00;          // 0: EIM (default fix) / 1: PnC (*option)
            configSetup[1] = 0x00;          // Charging State, 0: No / 1: Yes (0x00)
            configSetup[2] = 0x00;          // 정전 복귀 여부, 0: No (default) / 1: Yes (*option)
            configSetup[3] = 0x00;          // 0: Finished / 1: Ongoing / 2: Ongoing_WaitingForCustomerInteraction
            configSetup[4] = 0x00;          // 0: AC_single_phase_core (default) / 1:AC_three_phase_core (*option),
            configSetup[5] = 0x00;          // Body Reserved1
            RCD = dataTransformation.changeByteOrder(dataTransformation.IntToByteArray((int) 0),1);
            System.arraycopy(RCD, 0, configSetup, 6, RCD.length);
            configSetup[10] = 0x00;         // NotificationMaxDelay
            configSetup[11] = 0x00;         // 0:iso1EVSENotificationType_None, 1:iso1EVSENotificationType_StopCharging, 2:iso1EVSENotificationType_ReNegotiation
            configSetup[12] = 0x02;         //  예) 10 ^ 3 = 1000
            configSetup[13] = 0x05;         // iso1unitSymbolType_W = 5
            eEVSEPMaxValue = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray((short) 70),1);

            System.arraycopy(eEVSEPMaxValue, 0, configSetup, 14, eEVSEPMaxValue.length);        //에) 7x1000 = 7kW
            configSetup[16] = 0x00;         // 예) 10 ^ 1 = 10
            configSetup[17] = 0x04;         // iso1unitSymbolType_V = 4
            eVSEMaxVoltageValue = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray((short) 220),1);
            System.arraycopy(eVSEMaxVoltageValue, 0, configSetup, 18, eVSEMaxVoltageValue.length);        // 예) 22 x 10 = 220V
            configSetup[20] = 0x00;         // 예) 10 ^ 0 = 1
            configSetup[21] = 0x03;         // iso1unitSymbolType_A = 3,
            eVSEeMaxCurrentValue =  dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray((short) 32),1);
            System.arraycopy(eVSEeMaxCurrentValue, 0, configSetup, 22, eVSEeMaxCurrentValue.length);        // 예) 32 x 1 = 32A / 32x220 = 7040 (약 7kW)
            configSetup[24] = 0x00;         //예) 100 = 1 *) EvseMinCurrent 항목은 옵션
            configSetup[25] = 0x03;         // iso1unitSymbolType_A = 3
            eVSEMinCurrentValue = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray((short) 1),1);
            System.arraycopy(eVSEMinCurrentValue, 0, configSetup, 26, eVSEMinCurrentValue.length);        // 예) 1 x 1 = 1A / 1x220 = 220W

            //충전기 ID
            String chargerId = ((MainActivity) MainActivity.mContext).getChargerConfiguration().getChargerId();
            chargerId = "KRG53E1";
            String cid = chargerId.substring(0, chargerId.length() - 2);
//            eVSEid = dataTransformation.changeByteOrder(cid.getBytes(),1);
            eVSEid = cid.getBytes();
            System.arraycopy(eVSEid, 0, configSetup, 28, eVSEid.length);
            configSetup[35] = 0x00;     // Reserved

            checksum = dataTransformation.checkSum(configSetup, configSetup.length);

            //request data 정리
            int packetLen = 8 + bodyLen + 2;
            byte[] requestData = new byte[packetLen];      //8+36+2
            System.arraycopy(headerReq.dataSet(), 0, requestData, 0, 8);
            System.arraycopy(configSetup, 0, requestData, 8, bodyLen);
            requestData[packetLen - 2] = (byte) 0xa3;
            requestData[packetLen - 1] = checksum;
            return requestData;
        } catch (Exception e) {
            logger.error(" makeConfigSetupReq error : {}", e.getMessage());
        }
        return null;
    }
}
