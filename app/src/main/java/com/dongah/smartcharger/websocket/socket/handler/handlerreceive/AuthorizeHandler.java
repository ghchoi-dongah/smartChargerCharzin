package com.dongah.smartcharger.websocket.socket.handler.handlerreceive;

import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.ChargingCurrentData;
import com.dongah.smartcharger.basefunction.FragmentChange;
import com.dongah.smartcharger.basefunction.GlobalVariables;
import com.dongah.smartcharger.basefunction.UiSeq;
import com.dongah.smartcharger.websocket.ocpp.core.AuthorizationStatus;
import com.dongah.smartcharger.websocket.ocpp.core.ChargePointStatus;
import com.dongah.smartcharger.websocket.socket.OcppHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlersend.StatusNotificationReq;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.Objects;

public class AuthorizeHandler implements OcppHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizeHandler.class);

    private final String FILE_NAME = "localList.dongah";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {

        FragmentChange fragmentChange = new FragmentChange();
        MainActivity activity = (MainActivity) MainActivity.mContext;
        ChargingCurrentData chargingCurrentData = activity.getClassUiProcess().getChargingCurrentData();

        JSONObject idTagInfo = payload.getJSONObject("idTagInfo");
        AuthorizationStatus status = AuthorizationStatus.valueOf(idTagInfo.getString("status"));
        String parentIdTag = idTagInfo.has("parentIdTag") ? idTagInfo.getString("parentIdTag") : "";
        String expiryDate = idTagInfo.has("expiryDate") ? idTagInfo.getString("expiryDate") : "";

        if (AuthorizationStatus.Accepted.equals(status)) {
            chargingCurrentData.setParentIdTag(parentIdTag);
            if (activity.getSocketReceiveMessage() != null) {
                // AuthorizationCacheEnabled == true ? localList.dongah 저장
                if (GlobalVariables.isAuthorizationCacheEnabled()) {
                    saveIdTagToFile(chargingCurrentData.getIdTag(), idTagInfo);
                }
                chargingCurrentData.setPowerUnitPrice(GlobalVariables.getMemberPriceUnit());
                if (Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Available)) {
                    chargingCurrentData.setChargePointStatus(ChargePointStatus.Preparing);
                    StatusNotificationReq statusNotificationReq = new StatusNotificationReq();
                    statusNotificationReq.sendStatusNotification(connectorId);
                }
                activity.getClassUiProcess().setUiSeq(UiSeq.PLUG_CHECK);
                fragmentChange.onFragmentChange(UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
            }
        } else {
            String certificationReason = status.name();
            activity.getClassUiProcess().getChargingCurrentData().setAuthorizeResult(false);
            activity.getClassUiProcess().onHome();
            ((MainActivity) MainActivity.mContext).runOnUiThread(() -> {
                Toast.makeText(
                        activity,
                        "인증 실패 : " + certificationReason,
                        Toast.LENGTH_SHORT
                ).show();
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveIdTagToFile(String idTag, JSONObject idTagInfo) {
        try {

            File file = new File(
                    GlobalVariables.getRootPath()
                            + File.separator
                            + FILE_NAME
            );

            // 1. 파일 존재 여부
            JSONArray listArray;
            if (file.exists()) {
                String jsonStr = new String(Files.readAllBytes(file.toPath()));
                listArray = new JSONArray(jsonStr);
            } else {
                listArray = new JSONArray();
            }

            // 2. 기존 idTag 검색
            boolean found = false;
            for (int i = 0; i < listArray.length(); i++) {
                JSONObject item = listArray.getJSONObject(i);
                String savedIdTag = item.optString("idTag");

                if (savedIdTag.equals(idTag)) {
                    // 👉 있으면 수정
                    item.put("idTagInfo", idTagInfo);
                    found = true;
                    break;
                }
            }
            if (!found) {
                JSONObject newItem = new JSONObject();
                newItem.put("idTag", idTag);
                newItem.put("idTagInfo", idTagInfo);
                listArray.put(newItem);
            }

            // 4. 파일 저장
            FileWriter fw = new FileWriter(file, false);
            fw.write(listArray.toString(4));
            fw.flush();
            fw.close();

        } catch (Exception e) {
            logger.error("saveIdTagToFile error : {} ", e.getMessage());
        }
    }


}
