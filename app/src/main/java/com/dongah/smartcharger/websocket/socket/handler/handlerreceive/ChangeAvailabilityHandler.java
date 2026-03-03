package com.dongah.smartcharger.websocket.socket.handler.handlerreceive;

import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.GlobalVariables;
import com.dongah.smartcharger.basefunction.UiSeq;
import com.dongah.smartcharger.utils.FileManagement;
import com.dongah.smartcharger.websocket.ocpp.core.AvailabilityStatus;
import com.dongah.smartcharger.websocket.ocpp.core.AvailabilityType;
import com.dongah.smartcharger.websocket.ocpp.core.ChangeAvailabilityConfirmation;
import com.dongah.smartcharger.websocket.socket.OcppHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlersend.StatusNotificationReq;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Objects;

public class ChangeAvailabilityHandler implements OcppHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChangeAvailabilityHandler.class);

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {

        MainActivity activity = (MainActivity) MainActivity.mContext;
        AvailabilityType type = AvailabilityType.valueOf(payload.getString("type"));

        //change availability response
        boolean isCharging = Objects.equals(
                activity.getClassUiProcess().getUiSeq(),
                UiSeq.CHARGING
        );
        AvailabilityStatus result =
                (type == AvailabilityType.Inoperative && isCharging)
                        ? AvailabilityStatus.Scheduled
                        : AvailabilityStatus.Accepted;

        ChangeAvailabilityConfirmation changeAvailabilityConfirmation = new ChangeAvailabilityConfirmation(result);
        activity.getSocketReceiveMessage().onResultSend(changeAvailabilityConfirmation.getActionName(),
                messageId,
                changeAvailabilityConfirmation);
        // ChargerOperate
        GlobalVariables.ChargerOperation[connectorId] = AvailabilityStatus.Accepted == result;
        onChargerOperateSave(connectorId, result == AvailabilityStatus.Accepted);

        if (result == AvailabilityStatus.Accepted) {
            //StatusNotification
            StatusNotificationReq statusNotificationReq = new StatusNotificationReq();
            statusNotificationReq.sendStatusNotification(connectorId);
        } else {
            // Scheduled 인 경우 (충전 중)
            GlobalVariables.setScheduled(true);
        }
    }

    private void onChargerOperateSave(int connectorId, boolean checkType) {
        try {
            FileManagement fileManagement = new FileManagement();
            String rootPath = Environment.getExternalStorageDirectory().toString() + File.separator + "Download";
            String fileName = "ChargerOperate";
            File file = new File(rootPath + File.separator + fileName);

            if (file.exists()) file.delete();
            // connectorId == 0 → 전체 업데이트
            if (connectorId == 0) {
                for (int i = 0; i < GlobalVariables.maxChannel; i++) {
                    GlobalVariables.ChargerOperation[i] = checkType;
                }
            } else {
                // 특정 connector만 업데이트 (배열은 0부터라면 -1 필요할 수도 있음)
                int index = connectorId;
                if (index >= 0 && index < GlobalVariables.maxChannel) {
                    GlobalVariables.ChargerOperation[index] = checkType;
                }
            }

            //  전체 상태를 파일에 저장
            for (int i = 0; i < GlobalVariables.maxChannel; i++) {
                String statusContent = String.valueOf(GlobalVariables.ChargerOperation[i]);
                fileManagement.stringToFileSave(rootPath, fileName, statusContent, true);
            }

        } catch (Exception e) {
            logger.error(" onChargerOperateSave {}", e.getMessage());
        }
    }
}
