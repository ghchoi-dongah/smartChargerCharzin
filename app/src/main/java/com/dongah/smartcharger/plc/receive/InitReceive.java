package com.dongah.smartcharger.plc.receive;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitReceive {

    private static final Logger logger = LoggerFactory.getLogger(InitReceive.class);

    byte[] data;
    int len;
    DataTransformation dataTransformation = new DataTransformation();

    public InitReceive(byte[] data, int len) {
        this.data = data;
        this.len = len;
    }

    public void doReceive() {
        try {
            byte errorCode = data[8];
            byte[] reserved =  new byte[3];
            System.arraycopy(data, 9, reserved, 0, reserved.length);
            byte majorVersion = data[12];
            byte minorVersion = data[13];
            byte releaseVersion = data[14];
            byte releaseInfoLen = data[15];
            byte[] mReleaseInfo = new byte[24];
            System.arraycopy(data, 16, mReleaseInfo, 0, mReleaseInfo.length);
            byte[] releaseInfo =  dataTransformation.changeByteOrder(mReleaseInfo, 1);

        } catch (Exception e) {
            logger.error(" InitReceive doReceive Error :  {}", e.getMessage());
        }
    }
}
