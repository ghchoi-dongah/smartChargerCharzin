package com.dongah.smartcharger.controlboard;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.utils.BitUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RxData {

    private static final Logger logger = LoggerFactory.getLogger(RxData.class);
    private static final int RX_DATA_CNT = 10;
    public short[] rawData = new short[RX_DATA_CNT];

    //400 ~ 404
    public short csCPStatus = 0;        // STATE_A(12V), STATE_B(9V), STATE_C(6V), STATE_D(3V), STATE_E(0V), STATE_F(-12V)
    public short csPwmDuty = 100;
    public short csCpVoltage = 0; // csCpVoltage * 0.01
    public short csFirmwareVersion = 0;
    public short csRunCount = 0;
    //405
    public boolean csEmergency = false; //0 bit
    //406
    public boolean csMcStatus = false;  //0 bit
    //407
    public short csSequenceStatus = 0;  //
    public short reserve0 = 0;
    public short reserve1 = 0;

    public long voltage = 0;               //전압 x0.01  V
    public long current = 0;               //전류 x0.001 A
    public long ActiveEnergy = 0;         //전력량 x0.01 kWh
    public long ActivePower = 0;          //전력 x0.1 w
    public short Frequency = 0;            //주파수   0.01 Hz

    public boolean csPilot = false;         // plugCheck
    public boolean csStart = false;         // start
    public boolean csStop = false;          // stop
    public boolean csFault = false;         // fault
    public boolean csOVR = false;           // OVR
    public boolean csUVR = false;           // UVR
    public boolean csOCR = false;           // OCR

    public int csSoc = 0;


    ControlBoard controlBoard;

    public RxData() {
        controlBoard = ((MainActivity) MainActivity.mContext).getControlBoard();
    }

    public void Decode(short[] data) {
        try {
            csCPStatus = data[0];
            csPwmDuty = data[1];
            csCpVoltage = data[2];
            csFirmwareVersion = data[3];
            csRunCount = data[4];
            csEmergency = BitUtilities.getBitBoolean(data[5], 0);
            csMcStatus = BitUtilities.getBitBoolean(data[6], 0);
            csSequenceStatus = data[7];
            reserve0 = data[8];
            reserve1 = data[9];

            csPilot = csCPStatus == 1 || csCPStatus == 2;
            csStart = csCPStatus == 2;
            csStop = csCPStatus != 2;
//            if (csStop) {
//                controlBoard.getTxData().setMainMC(false);
//                controlBoard.getTxData().setPwmDuty((short) 100);
//            }
        } catch (Exception e) {
            logger.error(" RxData - Decode {}", e.getMessage());
        }
    }


    public short getCsCPStatus() {
        return csCPStatus;
    }

    public void setCsCPStatus(short csCPStatus) {
        this.csCPStatus = csCPStatus;
    }

    public short getCsPwmDuty() {
        return csPwmDuty;
    }

    public void setCsPwmDuty(short csPwmDuty) {
        this.csPwmDuty = csPwmDuty;
    }

    public short getCsCpVoltage() {
        return csCpVoltage;
    }

    public void setCsCpVoltage(short csCpVoltage) {
        this.csCpVoltage = csCpVoltage;
    }

    public short getCsFirmwareVersion() {
        return csFirmwareVersion;
    }

    public void setCsFirmwareVersion(short csFirmwareVersion) {
        this.csFirmwareVersion = csFirmwareVersion;
    }

    public short getCsRunCount() {
        return csRunCount;
    }

    public void setCsRunCount(short csRunCount) {
        this.csRunCount = csRunCount;
    }

    public boolean isCsEmergency() {
        return csEmergency;
    }

    public void setCsEmergency(boolean csEmergency) {
        this.csEmergency = csEmergency;
    }

    public boolean isCsMcStatus() {
        return csMcStatus;
    }

    public void setCsMcStatus(boolean csMcStatus) {
        this.csMcStatus = csMcStatus;
    }

    public short getCsSequenceStatus() {
        return csSequenceStatus;
    }

    public void setCsSequenceStatus(short csSequenceStatus) {
        this.csSequenceStatus = csSequenceStatus;
    }

    public short getReserve0() {
        return reserve0;
    }

    public void setReserve0(short reserve0) {
        this.reserve0 = reserve0;
    }

    public short getReserve1() {
        return reserve1;
    }

    public void setReserve1(short reserve1) {
        this.reserve1 = reserve1;
    }

    public long getVoltage() {
        return voltage;
    }

    public void setVoltage(long voltage) {
        this.voltage = voltage;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getActiveEnergy() {
        return ActiveEnergy;
    }

    public void setActiveEnergy(long activeEnergy) {
        ActiveEnergy = activeEnergy;
    }

    public long getActivePower() {
        return ActivePower;
    }

    public void setActivePower(long activePower) {
        ActivePower = activePower;
    }

    public short getFrequency() {
        return Frequency;
    }

    public void setFrequency(short frequency) {
        Frequency = frequency;
    }

    public boolean isCsPilot() {
        return csPilot;
    }

    public void setCsPilot(boolean csPilot) {
        this.csPilot = csPilot;
    }

    public boolean isCsStart() {
        return csStart;
    }

    public void setCsStart(boolean csStart) {
        this.csStart = csStart;
    }

    public boolean isCsStop() {
        return csStop;
    }

    public void setCsStop(boolean csStop) {
        this.csStop = csStop;
    }

    public boolean isCsFault() {
        return csFault;
    }

    public void setCsFault(boolean csFault) {
        this.csFault = csFault;
    }

    public boolean isCsOVR() {
        return csOVR;
    }

    public void setCsOVR(boolean csOVR) {
        this.csOVR = csOVR;
    }

    public boolean isCsUVR() {
        return csUVR;
    }

    public void setCsUVR(boolean csUVR) {
        this.csUVR = csUVR;
    }

    public boolean isCsOCR() {
        return csOCR;
    }

    public void setCsOCR(boolean csOCR) {
        this.csOCR = csOCR;
    }

    public int getCsSoc() {
        return csSoc;
    }

    public void setCsSoc(int csSoc) {
        this.csSoc = csSoc;
    }
}
