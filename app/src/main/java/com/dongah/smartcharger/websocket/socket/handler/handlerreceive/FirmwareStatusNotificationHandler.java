package com.dongah.smartcharger.websocket.socket.handler.handlerreceive;

import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.ChargerConfiguration;
import com.dongah.smartcharger.basefunction.GlobalVariables;
import com.dongah.smartcharger.utils.FileManagement;
import com.dongah.smartcharger.websocket.ocpp.firmware.FirmwareStatus;
import com.dongah.smartcharger.websocket.ocpp.firmware.FirmwareStatusNotificationRequest;
import com.dongah.smartcharger.websocket.socket.OcppHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlersend.StatusNotificationReq;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class FirmwareStatusNotificationHandler implements OcppHandler {

    private static final Logger logger = LoggerFactory.getLogger(FirmwareStatusNotificationHandler.class);

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {

        boolean check;
        MainActivity activity = ((MainActivity) MainActivity.mContext);
        ChargerConfiguration chargerConfiguration = activity.getChargerConfiguration();

        if (Objects.equals(chargerConfiguration.getFirmwareStatus(), FirmwareStatus.Downloading)) {
            check = true;
        } else if (Objects.equals(chargerConfiguration.getFirmwareStatus(), FirmwareStatus.Downloaded)) {
            chargerConfiguration.setFirmwareStatus(FirmwareStatus.Installing);
            FirmwareStatusNotificationRequest firmwareStatusNotificationRequest =
                    new FirmwareStatusNotificationRequest(chargerConfiguration.getFirmwareStatus());
            activity.getSocketReceiveMessage().onSend(
                    connectorId,
                    firmwareStatusNotificationRequest.getActionName(),
                    firmwareStatusNotificationRequest);
        } else if (Objects.equals(chargerConfiguration.getFirmwareStatus(), FirmwareStatus.Installing)) {
            // FirmwareStatusNotification file create
            String fileName = "FirmwareStatusNotification";
            FileManagement fileManagement = new FileManagement();
            check = fileManagement.fileCreate(fileName, "Firmware-Installed");
            activity.onRebooting("Hard");
        } else if (Objects.equals(chargerConfiguration.getFirmwareStatus(), FirmwareStatus.Installed)) {
            // update firmware 다운 전에 GlobalVariables.ChargerOperation[] = true ==> Unavailable
            Arrays.fill(GlobalVariables.ChargerOperation, true);
            onChargerOperateSave();
            chargerConfiguration.setFirmwareStatus(FirmwareStatus.Idle);
        } else if (Objects.equals(chargerConfiguration.getFirmwareStatus(), FirmwareStatus.DownloadFailed) ||
                Objects.equals(chargerConfiguration.getFirmwareStatus(), FirmwareStatus.InstallationFailed)) {
            // update firmware 다운 전에 GlobalVariables.ChargerOperation[] = false ==> Unavailable
            Arrays.fill(GlobalVariables.ChargerOperation, true);
            onChargerOperateSave();
            //Status Notification - all
            StatusNotificationReq statusNotificationReq = new StatusNotificationReq();
            statusNotificationReq.sendStatusNotification(0);
        }
    }

    private void onChargerOperateSave() {
        try {
            boolean check;
            String rootPath = Environment.getExternalStorageDirectory().toString() + File.separator + "Download";
            File file = new File(rootPath + File.separator + "ChargerOperate");
            if (file.exists()) check = file.delete();

            FileManagement fileManagement = new FileManagement();
            for (int i = 0; i < GlobalVariables.maxPlugCount; i++) {
                String statusContent = String.valueOf(GlobalVariables.ChargerOperation[i]);
                fileManagement.stringToFileSave(rootPath, "ChargerOperate", statusContent, true);
            }

        } catch (Exception e) {
            logger.error("onChargerOperateSave error : {}", e.getMessage());
        }
    }
}
