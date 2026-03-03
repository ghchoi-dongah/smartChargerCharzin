package com.dongah.smartcharger.plc.receive;

import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.plc.DataTransformation;
import com.dongah.smartcharger.utils.FileManagement;
import com.dongah.smartcharger.websocket.ocpp.core.datatransfer.vas.BatteryData;
import com.dongah.smartcharger.websocket.ocpp.core.datatransfer.vas.BatteryInfoDataSend;
import com.dongah.smartcharger.websocket.ocpp.utilities.Base64Util;
import com.dongah.smartcharger.websocket.ocpp.utilities.ZonedDateTimeConvert;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BatteryInfoReceive {

    private static final Logger logger = LoggerFactory.getLogger(BatteryInfoReceive.class);
    private static final String FILE_PATH = Environment.getExternalStorageDirectory().toString() + File.separator + "Download";
    private static final String FILE_NAME = "batteryInfo.dongah";
    private final Base64Util base64Util = new Base64Util();

    byte[] data;
    int len;
    DataTransformation dataTransformation = new DataTransformation();
    ZonedDateTimeConvert zonedDateTimeConvert = new ZonedDateTimeConvert();
    FileManagement fileManagement = new FileManagement();
    Gson gson = new Gson();

    public BatteryInfoReceive(byte[] data, int len) {
        this.data = data;
        this.len = len;
    }

    /**
     * BatteryInfo Report
     * PacketType : 0x8d
     * body length : 256
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void doReceive() {
        try {
            //TimeStamp Tag (A1) : UTC
            byte timeStampCode = data[8];
            byte timeStampLen = data[9];
            byte[] mTimeStamp = new byte[timeStampLen];        //4
            System.arraycopy(data, 10, mTimeStamp, 0, mTimeStamp.length);
            String timeStamp = dataTransformation.getConvertUTC(dataTransformation.ByteArrayToInt(mTimeStamp));

            timeStamp = zonedDateTimeConvert.doGetUtcDatetimeAsStringBatteryInfo();

            //VIN Tag (A2) :ASCII
            byte vinCode = data[14];
            byte vinLen = data[15];
            byte[] mVIN = new byte[vinLen];     //17
            System.arraycopy(data, 16, mVIN, 0, mVIN.length);
            for (int i = 0; i < mVIN.length; i++) {
                if ((mVIN[i] & 0xFF) > 0x7F) {
                    mVIN[i] = 0x20;
                }
            }
            String vin = new String(mVIN, StandardCharsets.US_ASCII);

            //SoC Tag (A3)
            byte socCode = data[33];
            byte socLen = data[34];         //1
            byte bSoc = data[35];            // soc * 0.5
            double SoC = bSoc;
            ((MainActivity) MainActivity.mContext).getClassUiProcess().getChargingCurrentData().setSoc((int) SoC);

            //SoH Tag (A4)
            byte sohCode = data[36];
            byte sohLen = data[37];        //1
            byte Soh = data[38];           // soh * 1

            //BatteryPack current (A5)
            byte packCurrentCode = data[39];
            byte packCurrentLen = data[40];         //2
            byte[] mPackCurrent = new byte[packCurrentLen];       // current * 0.1
            System.arraycopy(data, 41, mPackCurrent, 0, packCurrentLen);
            short sPackCurrent = dataTransformation.ByteArrayToShort(mPackCurrent);
            double packCurrent = sPackCurrent * 0.1;

            //BatteryPack voltage(A6)
            byte packVoltageCode = data[43];
            byte packVoltageLen = data[44];
            byte[] mPackVoltage = new byte[packVoltageLen];
            System.arraycopy(data, 45, mPackVoltage, 0, packVoltageLen);
            short sPackVoltage = dataTransformation.ByteArrayToShort(mPackVoltage);
            double packVoltage = sPackVoltage * 0.1;

            //Battery Cell Voltage(A7)
            byte batteryVoltageCode = data[47];
            byte[] mBatteryCellLen = new byte[2];
            System.arraycopy(data, 48, mBatteryCellLen, 0, mBatteryCellLen.length);
            short batteryCellLen = dataTransformation.ByteArrayToShort(mBatteryCellLen);

            JSONArray batteryVoltageArray = new JSONArray();
            List<Map<String, String>> batteryVoltageList = new ArrayList<>();
            for (int i = 0; i < batteryCellLen ; i++) {
                Map<String, String> mapVoltage = new HashMap<>();
                mapVoltage.put("bsv" + i, String.valueOf(data[50+i] * 0.01));
                batteryVoltageList.add(mapVoltage);
            }
            String jsonVoltage = gson.toJson(batteryVoltageList);

            //Battery Cell Current (A8)
            byte batteryTempCode = data[241];
            byte batteryTempLen = data[242];

            List<Map<String, String>> batteryTempList = new ArrayList<>();
            for (int i = 0; i < batteryTempLen; i++) {
                Map<String, String> mapTemp = new HashMap<>();
                mapTemp.put("bmt" + i, String.valueOf(data[243+i]));
                batteryTempList.add(mapTemp);
            }
            /**
             * BatteryInfoDataSend - BatteryData
             */
            BatteryData batteryData = new BatteryData();
            batteryData.setVin(vin);
            batteryData.setSoc(String.valueOf(SoC));
            batteryData.setSoh(String.valueOf(Soh));

            batteryData.setBsv(batteryVoltageList);
            batteryData.setBmt(batteryTempList);

            BatteryInfoDataSend batteryInfoDataSend = new BatteryInfoDataSend();
            batteryInfoDataSend.setTimeStamp(timeStamp);
//            batteryInfoDataSend.setBattery(batteryData);
            batteryInfoDataSend.setBattery(base64Util.encode(gson.toJson(batteryData)));

            /**
             *
             */


            String result = gson.toJson(batteryInfoDataSend);
            fileManagement.stringToFileSave(FILE_PATH, FILE_NAME, result, true);
        } catch (Exception e) {
            logger.error(" BatteryInfoReceive error : {}", e.getMessage());
        }
    }
}
