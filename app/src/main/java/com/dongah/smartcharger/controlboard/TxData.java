package com.dongah.smartcharger.controlboard;

import com.dongah.smartcharger.utils.BitUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxData {
    private static final Logger logger = LoggerFactory.getLogger(TxData.class);

    private static final int TX_DATA_CNT = 10;
    public short[] rawData = new short[TX_DATA_CNT];


    public boolean IsBoardRest = false;     //3 bit : OFF(0), 리셋시 ON(1) 한번만 전송
    public boolean IsMainMC = false;        //5 bit : default : OFF(0), 충전시 ON(1)
    public boolean IsCPRelay = true;        //6 bit : ON(1), 정상 상황에서는 사용 안 함

    //
    public short pwmDuty = 100;             // 대기 100%,
                                            //  충전 시작시 변경 :  50%/30A,  40%/24A,   30%/25A,   16%/9.6A   10%/6
    public short uiSequence = 1;            // 1:대기, 2:충전중, 3:종료
    public short runCount = 0;

    public long powerMeter = 0;
    public short highPowerMeter = 0;
    public short lowPowerMeter = 0;
    public short outVoltage = 0;            // 0.1V
    public short outCurrent = 0;            // 0.1A


    public void setInit() {
        try {
            IsBoardRest = IsMainMC = false;
            IsCPRelay = true;
            pwmDuty = 100;
            uiSequence = 1;
        } catch (Exception e) {
            logger.error(" TxData - setInit error : {}", e.getMessage());
        }
    }

    public short[] Encode() {
        try {
            // 제어 순서 : pwmDuty ->  IsMainMC
            rawData[0] = BitUtilities.setBit(rawData[0], 3, IsBoardRest);
            rawData[0] = BitUtilities.setBit(rawData[0], 5, IsMainMC);
            rawData[0] = BitUtilities.setBit(rawData[0], 6, IsCPRelay);

            rawData[1] = pwmDuty;
            rawData[2] = uiSequence;
            rawData[3] = runCount++;
            rawData[4] = highPowerMeter;
            rawData[5] = lowPowerMeter;
            rawData[6] = outVoltage;
            rawData[7] = outCurrent;
            rawData[8] = 0;
            rawData[9] = 0;
            return rawData;
        } catch (Exception e) {
            logger.error(" TxData - Encode error :  {}", e.getMessage());
        }
        return null;
    }


    public boolean isBoardRest() {
        return IsBoardRest;
    }

    public void setBoardRest(boolean boardRest) {
        IsBoardRest = boardRest;
    }

    public boolean isMainMC() {
        return IsMainMC;
    }

    public void setMainMC(boolean mainMC) {
        IsMainMC = mainMC;
    }

    public boolean isCPRelay() {
        return IsCPRelay;
    }

    public void setCPRelay(boolean CPRelay) {
        IsCPRelay = CPRelay;
    }

    public short getPwmDuty() {
        return pwmDuty;
    }

    public void setPwmDuty(short pwmDuty) {
        this.pwmDuty = pwmDuty;
    }

    public short getUiSequence() {
        return uiSequence;
    }

    public void setUiSequence(short uiSequence) {
        this.uiSequence = uiSequence;
    }

    public short getRunCount() {
        return runCount;
    }

    public void setRunCount(short runCount) {
        this.runCount = runCount;
    }

    public long getPowerMeter() {
        return powerMeter;
    }

    public void setPowerMeter(long powerMeter) {
        this.powerMeter = powerMeter;
    }

    public short getHighPowerMeter() {
        return highPowerMeter;
    }

    public void setHighPowerMeter(short highPowerMeter) {
        this.highPowerMeter = highPowerMeter;
    }

    public short getLowPowerMeter() {
        return lowPowerMeter;
    }

    public void setLowPowerMeter(short lowPowerMeter) {
        this.lowPowerMeter = lowPowerMeter;
    }

    public short getOutVoltage() {
        return outVoltage;
    }

    public void setOutVoltage(short outVoltage) {
        this.outVoltage = outVoltage;
    }

    public short getOutCurrent() {
        return outCurrent;
    }

    public void setOutCurrent(short outCurrent) {
        this.outCurrent = outCurrent;
    }
}
