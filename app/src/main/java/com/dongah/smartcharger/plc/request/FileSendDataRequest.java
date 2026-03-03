package com.dongah.smartcharger.plc.request;

import com.dongah.smartcharger.basefunction.GlobalVariables;
import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileSendDataRequest {

    private static final Logger logger = LoggerFactory.getLogger(FileSendDataRequest.class);

    private static final short UNIT_SIZE = 1024;
    private static final String FILE_NAME = "secc_OTA_v0.12.13.bin";
    DataTransformation dataTransformation = new DataTransformation();
    short dataLen;
    byte[] fileBytes = null;
    int offset = 0;

    public FileSendDataRequest() {
        try {
            fileBytes = readFileToByteArray(GlobalVariables.ROOT_PATH + File.separator + FILE_NAME);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public byte[] makeFileSendDataRequest(short packetNow, short packetAll) {
        try {
            byte[] bodyData = new byte[10];

            bodyData[0] = (byte) 0xf2;
            bodyData[1] = (byte) 0x21;
            dataLen = (short) Math.min(UNIT_SIZE, fileBytes.length - offset);
//            byte[] mDataLen = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray((short) (dataLen + bodyData.length)), 1);

            byte[] mDataLen = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(dataLen), 1);

            System.arraycopy(mDataLen, 0, bodyData, 2, 2);

            byte[] mPacketNow = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(packetNow), 1);
            System.arraycopy(mPacketNow, 0, bodyData, 4, 2);

            byte[] mPacketAll = dataTransformation.changeByteOrder(dataTransformation.ShortToByteArray(packetAll), 1);
            System.arraycopy(mPacketAll, 0, bodyData, 6, 2);

            bodyData[8] = (byte) 0x01;

            ///read size
            int length = Math.min(UNIT_SIZE, fileBytes.length - offset);
            byte[] chunk = new byte[length];
            System.arraycopy(fileBytes, offset, chunk, 0, length);

            bodyData[9] = dataTransformation.checkSum(chunk, chunk.length+1);

            offset += length;

            // body data + file byte data  합치기
            byte[] result = new byte[bodyData.length + chunk.length];
            System.arraycopy(bodyData, 0, result, 0, bodyData.length);
            System.arraycopy(chunk, 0, result, bodyData.length, chunk.length);

            return result;

        } catch (Exception e){
            logger.error(" makeFileSendDataRequest error : {}", e.getMessage());
        }

        return null;
    }


    public byte[] readFileToByteArray(String filePath) throws IOException {
        byte[] data = null;
        FileInputStream fis = null;
        try {
            File file = new File(filePath);
            fis = new FileInputStream(file);
            data = new byte[(int) file.length()];
            int read = fis.read(data);
        } catch (Exception e) {
            logger.error(" readFileToByteArray error : {}", e.getMessage());
        } finally {
            if (fis != null) fis.close();
        }
        return data;
    }

}
