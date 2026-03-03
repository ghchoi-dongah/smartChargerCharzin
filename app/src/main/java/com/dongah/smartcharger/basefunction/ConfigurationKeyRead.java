package com.dongah.smartcharger.basefunction;

import com.dongah.smartcharger.utils.FileManagement;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Objects;

public class ConfigurationKeyRead {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationKeyRead.class);
    final String fileName = "configurationKey";
    String configurationString;


    public ConfigurationKeyRead() {
    }


    public void onRead() {
        try {
            File file = new File(GlobalVariables.getRootPath() + File.separator + fileName);
            if (file.exists()) {
                FileManagement fileManagement = new FileManagement();
                configurationString = fileManagement.getStringFromFile(GlobalVariables.getRootPath() + File.separator + fileName);
                JSONObject jsonObjectData = new JSONObject(configurationString);
                JSONArray jsonArrayContent = jsonObjectData.getJSONArray("values");
                for (int i = 0; i < jsonArrayContent.length(); i++) {
                    JSONObject contDetail = jsonArrayContent.getJSONObject(i);
                    if (Objects.equals("ConnectionTimeOut", contDetail.getString("key"))) {
                        GlobalVariables.setConnectionTimeOut(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("MeterValueSampleInterval", contDetail.getString("key"))) {
                        GlobalVariables.setMeterValueSampleInterval(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("HeartbeatInterval", contDetail.getString("key"))) {
                        GlobalVariables.setHeartBeatInterval(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("AuthorizationKey", contDetail.getString("key"))) {
                        GlobalVariables.setAuthorizationKey(contDetail.getString("value"));
                    } else if (Objects.equals("SecurityProfile", contDetail.getString("key"))) {
                        GlobalVariables.setSecurityProfile(contDetail.getString("value"));
                    } else if (Objects.equals("ServerURL", contDetail.getString("key"))) {
                        GlobalVariables.setServerURL(contDetail.getString("value"));
                    } else if (Objects.equals("MemberPrice", contDetail.getString("key"))) {
                        String[] parts = contDetail.getString("value").split(",");
                        GlobalVariables.setMemberPriceUnit(Double.parseDouble(parts[0]));
                        GlobalVariables.setMemberPricePowerUnit(parts[1]);
                        GlobalVariables.setMemberPriceMoneyUnit(parts[2]);
                    } else if (Objects.equals("NonMemberPrice", contDetail.getString("key"))) {
                        String[] parts = contDetail.getString("value").split(",");
                        GlobalVariables.setNonMemberPriceUnit(Double.parseDouble(parts[0]));
                        GlobalVariables.setNonMemberPricePowerUnit(parts[1]);
                        GlobalVariables.setNonMemberPriceMoneyUnit(parts[2]);
                    } else if (Objects.equals("MaxChargingTime", contDetail.getString("key"))) {
                        GlobalVariables.setMaxChargingTime(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("MaxChargingRate", contDetail.getString("key"))) {
                        GlobalVariables.setMaxChargingRate(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("WebSocketPingInterval", contDetail.getString("key"))) {
                        GlobalVariables.setWebsocketPingInterval(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("LocalPreAuthorize", contDetail.getString("key"))) {
                        GlobalVariables.setLocalPreAuthorize(Boolean.parseBoolean(contDetail.getString("value")));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("ConfigurationKeyRead onRead error {}", e.getMessage());
        }
    }

    public void onUpdateConfigByKey(String targetKey, Object newValue) {
        try {
            File file = new File(GlobalVariables.getRootPath() + File.separator + fileName);
            if (file.exists()) {
                FileManagement fileManagement = new FileManagement();
                configurationString = fileManagement.getStringFromFile(GlobalVariables.getRootPath() + File.separator + fileName);
                JSONObject jsonObjectData = new JSONObject(configurationString);
                if (jsonObjectData.has(targetKey)) {
                    Object oldValue = jsonObjectData.get(targetKey);
                    jsonObjectData.put(targetKey, newValue);
                }
                fileManagement.stringToFileSave(GlobalVariables.ROOT_PATH, fileName, jsonObjectData.toString(), false);
            }
        } catch (Exception e) {
            logger.error("ConfigurationKeyRead onUpdateConfigByKey error {}", e.getMessage());
        }
    }
}
