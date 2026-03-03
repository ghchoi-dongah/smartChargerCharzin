package com.dongah.smartcharger.plc.receive;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChargeParameterDiscoveryReceive {

    private static final Logger logger = LoggerFactory.getLogger(ChargeParameterDiscoveryReceive.class);

    byte[] data;
    int len;
    DataTransformation dataTransformation = new DataTransformation();

    public ChargeParameterDiscoveryReceive(byte[] data, int len) {
        this.data = data;
        this.len = len;
    }

    /**
     * ChargeParameterDiscovery Report
     * PacketType : 0x88
     * body length : 36
     */
    public void doReceive() {
        try {
            byte SessionIDLength = data[8];
            byte[] mReserved1 = new byte[3];
            System.arraycopy(data, 9, mReserved1, 0, mReserved1.length);
            int Reserved1 = dataTransformation.ByteArrayToInt(dataTransformation.changeByteOrder(mReserved1, 1));
            byte[] mSessionID = new byte[8];
            System.arraycopy(data, 12, mSessionID, 0, mSessionID.length);

            byte EVRequestedEnergyTransferType = data[20];          //0: AC_single_phase_core, 1:AC_three_phase_core
            byte ResponseCode = data[21];

            byte[] mMaxEntriesSAScheduleTuple = new byte[2];
            System.arraycopy(data, 22, mMaxEntriesSAScheduleTuple, 0, mMaxEntriesSAScheduleTuple.length);
            short MaxEntriesSAScheduleTuple = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mMaxEntriesSAScheduleTuple, 1));

            byte[] mDepartureTime = new byte[4];            //0: not used, 0<Value : 완충 예상 시간[sec]
            System.arraycopy(data, 24, mDepartureTime, 0, mDepartureTime.length);
            int DepartureTime = dataTransformation.ByteArrayToInt(dataTransformation.changeByteOrder(mDepartureTime, 1));

            byte EamountMultiple = data[28];            // 예) 10 ^ 3 = 1000
            byte EamountUnit = data[29];                //iso1unitSymbolType_W = 5,
            byte[] mEamountValue = new byte[2];         //에) 7x1000 = 7kW
            System.arraycopy(data, 30, mEamountValue, 0, mEamountValue.length);
            short EamountValue = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mEamountValue, 1));

            byte EVMaxVoltageMultiple = data[32];       // 예) 10 ^ 1 = 10
            byte EVMaxVoltageUnit = data[33];           //iso1unitSymbolType_V = 4,
            byte[] mEVMaxVoltageValue = new byte[2];    //예) 22 x 10 = 220V
            System.arraycopy(data, 34, mEVMaxVoltageValue, 0, mEVMaxVoltageValue.length);
            short EVMaxVoltageValue = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mEVMaxVoltageValue, 1));

            byte EVMaxCurrentMultiple = data[36];           //예) 10 ^ 0 = 1
            byte EVMaxCurrentUinit = data[37];              //iso1unitSymbolType_A = 3,
            byte[] mEVMaxCurrentValue = new byte[2];        //예) 32 x 1 = 32A / 32x220 = 7040 (약 7kW)
            System.arraycopy(data, 38, mEVMaxCurrentValue, 0, mEVMaxCurrentValue.length);
            short EVMaxCurrentValue = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mEVMaxCurrentValue, 1));

            byte EVMinCurrentMultiple = data[40];           //예) 100 = 1 *)EvseMinCurrent 항목은 옵션
            byte EVMinCurrentUnit = data[41];               //iso1unitSymbolType_A = 3,
            byte[] mEVMinCurrentValue = new byte[2];        //예) 1 x 1 = 1A / 1x220 = 220W
            System.arraycopy(data, 42, mEVMinCurrentValue, 0, mEVMinCurrentValue.length);
            short EVMinCurrentValue = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mEVMinCurrentValue, 1));


        } catch (Exception e) {
            logger.error(" ChargeParameterDiscoveryReceive error {}", e.getMessage());
        }
    }
}
