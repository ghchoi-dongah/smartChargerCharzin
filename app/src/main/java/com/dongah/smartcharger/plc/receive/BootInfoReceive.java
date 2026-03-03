package com.dongah.smartcharger.plc.receive;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BootInfoReceive {

    private static final Logger logger = LoggerFactory.getLogger(BootInfoReceive.class);

    byte[] data;
    int len;
    DataTransformation dataTransformation = new DataTransformation();

    public BootInfoReceive(byte[] data, int len) {
        this.data = data;
        this.len = len;
    }

    public void doReceive() {
        try {
            // BOOT INFO
            // 1. 년,월,일,시,분,초 시간 정보 전송
            byte[] bcd = new byte[6];
            System.arraycopy(data, 8, bcd, 0, 6);
            String BCDDate = dataTransformation.BCDtoString(bcd);
            String BCDDate1 = dataTransformation.BCDtoString(dataTransformation.changeByteOrder(bcd, 1));

            // 2. HW 보드 관리용 버전 정보, ex) 10203
            byte[] hwVersion = new byte[2];
            System.arraycopy(data, 14, hwVersion, 0, 2);
            short version = dataTransformation.ByteArrayToShort(hwVersion);


            // 3. OTA 버전 번호, ex) "00.01.02_003"
            byte[] ota = new byte[12];
            System.arraycopy(data, 16, ota, 0, 12);
            String otaVersion = new String(dataTransformation.removeZeros(ota));
            String otaVersion1 = new String(dataTransformation.removeZeros(dataTransformation.changeByteOrder(ota,1)));

            // 4. App 버전 번호, ex) "00.01.02_003"
            byte[] app = new byte[12];
            System.arraycopy(data, 28, app, 0, 12);
            String appVersion = new String(dataTransformation.removeZeros(app));
            String appVersion1 = new String(dataTransformation.removeZeros(dataTransformation.changeByteOrder(app,1)));


            byte flag = data[40];
            byte reserved1 = data[41];

        } catch (Exception e) {
            logger.error(" receiveBootInfo error :  {}", e.getMessage());
        }
    }


}
