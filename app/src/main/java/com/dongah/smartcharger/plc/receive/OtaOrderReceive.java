package com.dongah.smartcharger.plc.receive;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class OtaOrderReceive {

    private static final Logger logger = LoggerFactory.getLogger(OtaOrderReceive.class);

    byte[] data;
    int len;
    DataTransformation dataTransformation = new DataTransformation();


    public OtaOrderReceive(byte[] data, int len) {
        this.data = data;
        this.len = len;
    }


    public void doReceive() {
        try {
            byte[] mFileSize = new byte[4];
            System.arraycopy(data, 8, mFileSize, 0, mFileSize.length);
            int fileSize = dataTransformation.ByteArrayToInt(dataTransformation.changeByteOrder(mFileSize,1));

            byte[] mValue = new byte[2];
            System.arraycopy(data, 12, mValue, 0, mValue.length);
            short unitSize  = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mValue,1));

            System.arraycopy(data, 14, mValue, 0, mValue.length);
            short count  = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mValue,1));

            System.arraycopy(data, 16, mValue, 0, mValue.length);
            short lastSize  = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mValue,1));

            System.arraycopy(data, 18, mValue, 0, mValue.length);
            short maxWait  = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mValue,1));

            System.arraycopy(data, 20, mValue, 0, mValue.length);
            short hwv  = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(mValue,1));

            byte[] mPlatform = new byte[12];
            System.arraycopy(data, 22, mPlatform, 0, mPlatform.length);
            String platform = new String(mPlatform, StandardCharsets.UTF_8);

            byte[] mVersion = new byte[12];
            System.arraycopy(data, 34, mVersion, 0, mVersion.length);
            String version = new String(mVersion, StandardCharsets.UTF_8);

            byte[] mSyncKey = new byte[8];
            System.arraycopy(data, 46, mSyncKey, 0, mSyncKey.length);
            String syncKey = new String(mSyncKey, StandardCharsets.UTF_8);

            byte flag = data[55];
            byte reserved1 = data[56];

        } catch (Exception e) {
            logger.error(" OtaOrderReceive error : {}", e.getMessage());
        }
    }
}
