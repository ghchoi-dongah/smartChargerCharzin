package com.dongah.smartcharger.websocket.socket.handler.handlerreceive;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.ConfigurationKeyRead;
import com.dongah.smartcharger.basefunction.GlobalVariables;
import com.dongah.smartcharger.plc.DataTransformation;
import com.dongah.smartcharger.utils.FileManagement;
import com.dongah.smartcharger.websocket.ocpp.core.ChangeConfigurationConfirmation;
import com.dongah.smartcharger.websocket.ocpp.core.ConfigurationStatus;
import com.dongah.smartcharger.websocket.socket.OcppHandler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.Objects;

public class ChangeConfigurationHandler implements OcppHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChangeConfigurationHandler.class);

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {

        boolean result, serverUrlReq = false;
        MainActivity activity = (MainActivity) MainActivity.mContext;
        GlobalVariables.setNotSupportedKey(false);
        String key = payload.has("key") ? payload.getString("key") : "";
        String value = payload.has("value") ? payload.getString("value") : "0";


        if (Objects.equals(key, "MeterValueSampleInterval") && Integer.parseInt(value) == -1) {
            result = false;
        } else if (Objects.equals(key, "SecurityProfile")) {
            result = Integer.parseInt(GlobalVariables.getSecurityProfile()) <= Integer.parseInt(value);
            if (result) setConfigurationValue(key, value);
        } else {
            serverUrlReq = Objects.equals(key, "ServerURL");
            result = setConfigurationValue(key, value);
            if (result) {
                // ServerURL  변경 ==> chargerId 변경
                ConfigurationKeyRead configurationKeyRead = ((MainActivity) MainActivity.mContext).getConfigurationKeyRead();
                configurationKeyRead.onUpdateConfigByKey(key, getChargerId(value));
            }
        }

        if (result) ((MainActivity) MainActivity.mContext).getConfigurationKeyRead().onRead();
        //response
        ConfigurationStatus configurationStatus = GlobalVariables.isNotSupportedKey() ? ConfigurationStatus.NotSupported :
                serverUrlReq ? ConfigurationStatus.RebootRequired :
                        result ? ConfigurationStatus.Accepted : ConfigurationStatus.Rejected;
        ChangeConfigurationConfirmation changeConfigurationConfirmation = new ChangeConfigurationConfirmation(configurationStatus);
        activity.getSocketReceiveMessage().onResultSend(changeConfigurationConfirmation.getActionName(), messageId, changeConfigurationConfirmation);
    }


    public boolean setConfigurationValue(String key, String value) {
        boolean result = false;

        try {
            FileManagement fileManagement = new FileManagement();
            String configurationString =
                    fileManagement.getStringFromFile(
                            GlobalVariables.getRootPath() + File.separator + "ConfigurationKey");

            JSONArray jsonArrayContent = new JSONObject(configurationString).getJSONArray("values");
            JSONArray jsonArray = new JSONArray();

            boolean notFound = true;

            for (int i = 0; i < jsonArrayContent.length(); i++) {
                JSONObject contDetail = jsonArrayContent.getJSONObject(i);

                if (Objects.equals(contDetail.getString("key"), key)) {

                    notFound = false;

                    // readonly면 기존 값 유지
                    if (contDetail.getBoolean("readonly")) {
                        jsonArray.put(contDetail);
                    } else {
                        JSONObject obj = new JSONObject();
                        obj.put("key", key);
                        obj.put("readonly", false);
                        obj.put("value", doAuthorizationKeyConvert(key, value));
                        jsonArray.put(obj);
                    }

                } else {
                    jsonArray.put(contDetail);
                }
            }

            GlobalVariables.setNotSupportedKey(notFound);

            JSONObject sObject = new JSONObject();
            sObject.put("values", jsonArray);

            result = fileManagement.stringToFileSave(
                    GlobalVariables.getRootPath(),
                    "ConfigurationKey",
                    sObject.toString(),
                    false
            );

        } catch (Exception e) {
            logger.error("SetConfigurationValue {}", e.getMessage(), e);
        }

        return result;
    }

    private String doAuthorizationKeyConvert(String key, String value) {
        try {
            if (Objects.equals(key, "AuthorizationKey")) {
                DataTransformation dataTransformation = new DataTransformation();
                return dataTransformation.hexToString(value);
            } else {
                return value;
            }
        } catch (Exception e) {
            logger.error(" doAuthorizationKeyConvert error : {}", e.getMessage());
            return "";
        }
    }

    private String getChargerId(String serverURL) {
        try {
            URI uri = URI.create(serverURL);
            String path = uri.getPath();
            return path.substring(1);
        } catch (Exception e) {
            logger.error(" getChargerId error : {}", e.getMessage());
        }
        return null;
    }
}
