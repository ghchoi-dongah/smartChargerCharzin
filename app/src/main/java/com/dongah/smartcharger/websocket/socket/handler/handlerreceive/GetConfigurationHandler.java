package com.dongah.smartcharger.websocket.socket.handler.handlerreceive;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.GlobalVariables;
import com.dongah.smartcharger.utils.FileManagement;
import com.dongah.smartcharger.websocket.ocpp.core.GetConfigurationConfirmation;
import com.dongah.smartcharger.websocket.ocpp.core.KeyValueType;
import com.dongah.smartcharger.websocket.socket.OcppHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GetConfigurationHandler implements OcppHandler {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {

        MainActivity activity = ((MainActivity) MainActivity.mContext);
        JSONArray requestKeys = payload.getJSONArray("key");
        // ConfigurationKey 파일 읽기
        FileManagement fileManagement = new FileManagement();
        String configStr = fileManagement.getStringFromFile(
                GlobalVariables.getRootPath() + File.separator + "configurationKey");


        JSONArray fileValues = new JSONObject(configStr).getJSONArray("values");

        List<KeyValueType> keyValueList = new ArrayList<>();
        List<String> unknownKeyList = new ArrayList<>();
        for (int i = 0; i < requestKeys.length(); i++) {

            String reqKey = requestKeys.getString(i);
            boolean found = false;

            for (int j = 0; j < fileValues.length(); j++) {
                JSONObject item  = fileValues.getJSONObject(j);

                if (reqKey.equals(item.get("key"))) {
                    KeyValueType keyValueType = new KeyValueType();
                    keyValueType.setKey(item.getString("key"));
                    keyValueType.setReadonly(item.getBoolean("readonly"));
                    keyValueType.setValue(item.getString("value"));

                    keyValueList.add(keyValueType);
                    found = true;
                    break;
                }
            }
            if (!found) {
                unknownKeyList.add(reqKey);
            }
        }

        // List → Array 변환
        KeyValueType[] keyValueTypes = null;
        if (!keyValueList.isEmpty()) {
            keyValueTypes = keyValueList.toArray(new KeyValueType[0]);
        }

        String[] unknownKeys = null;
        if (!unknownKeyList.isEmpty()) {
            unknownKeys = unknownKeyList.toArray(new String[0]);
        }

        GetConfigurationConfirmation getConfigurationConfirmation = new GetConfigurationConfirmation();
        if (keyValueTypes != null) {
            getConfigurationConfirmation.setConfigurationKey(keyValueTypes);
        }

        if (unknownKeys != null) {
            getConfigurationConfirmation.setUnknownKey(unknownKeys);
        }
        activity.getSocketReceiveMessage().onResultSend(
                getConfigurationConfirmation.getActionName(),
                messageId,
                getConfigurationConfirmation
        );

    }
}
