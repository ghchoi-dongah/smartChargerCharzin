package com.dongah.smartcharger.plc.receive;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigCheckReceive {

    private static final Logger logger = LoggerFactory.getLogger(ConfigCheckReceive.class);

    byte[] data;
    int len;
    DataTransformation dataTransformation = new DataTransformation();


    public ConfigCheckReceive(byte[] data, int len) {
        this.data = data;
        this.len = len;
    }

    /**
     * ConfigCheck Response
     * PacketType : 0x79
     * body length : 28
     */
    public void doReceive() {
        try {
            byte ChargerMode = data[8];                 // 0: EIM (default fix)   1: PnC (*option)
            byte ChargerState = data[9];                // Charging State, 0: No   1: Yes
            byte PowerRecovery = data[10];              // 정전복귀 여부, 0: No (default) / 1: Yes (*option)
            byte AuthFinished = data[11];               // Authorization, 0: Not yet / 1: OK
            byte EnergyType = data[12];                 // 0: AC_single_phase_core (default)   1:AC_three_phase_core (*option)
            byte Reserved1 = data[13];

            byte[] mRCD = new byte[4];
            System.arraycopy(data, 14, mRCD, 0, mRCD.length);
            int RCD = dataTransformation.ByteArrayToInt(dataTransformation.changeByteOrder(mRCD, 1));

            byte NotificationMaxDelay = data[18];
            byte EVSENotification = data[19];           // 0:iso1EVSENotificationType_None, 1:iso1EVSENotificationType_StopCharging, 2:iso1EVSENotificationType_ReNegotiation

            byte EvsePMaxMultiple = data[20];           // 예) 10 ^ 3 = 1000
            byte EvsePMaxUnit = data[21];               // iso1unitSymbolType_W = 5,
            byte[] mEvsePMaxValue = new byte[2];        //에) 7x1000 = 7kW
            System.arraycopy(data, 22, mEvsePMaxValue, 0, mEvsePMaxValue.length);
            short EvsePMaxValue = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mEvsePMaxValue, 1));

            byte EvseMaxVoltageMultiple = data[24];     // 예) 10 ^ 1 = 10
            byte EvseMaxVoltageUnit = data[25];
            byte[] mEvseMaxVoltageValue = new byte[2];
            System.arraycopy(data, 26, mEvseMaxVoltageValue, 0, mEvseMaxVoltageValue.length);
            short EvseMaxVoltageValue = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mEvseMaxVoltageValue, 1));

            byte EvseMaxCurrentMultiple =  data[28];        // 예) 10 ^ 0 = 1
            byte EvseMaxCurrentUinit = data[29];            //iso1unitSymbolType_A = 3,
            byte[] mEvseMaxCurrentValue = new byte[2];
            System.arraycopy(data, 30, mEvseMaxCurrentValue, 0, mEvseMaxCurrentValue.length);
            short EvseMaxCurrentValue = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mEvseMaxCurrentValue, 1));

            byte EvseMinCurrentMultiple = data[32];
            byte EvseMinCurrentUnit = data[33];
            byte[] mEvseMinCurrentValue = new byte[2];
            System.arraycopy(data, 34, mEvseMinCurrentValue, 0, mEvseMinCurrentValue.length);
            short EvseMinCurrentValue = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mEvseMinCurrentValue, 1));

        } catch (Exception e) {
            logger.error(" ConfigCheckReceive error :  {}", e.getMessage());
        }
    }
}
