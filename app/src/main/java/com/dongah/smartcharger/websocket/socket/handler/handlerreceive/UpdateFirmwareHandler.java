package com.dongah.smartcharger.websocket.socket.handler.handlerreceive;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.ChargerConfiguration;
import com.dongah.smartcharger.basefunction.FileTransType;
import com.dongah.smartcharger.basefunction.FtpRxJava;
import com.dongah.smartcharger.basefunction.GlobalVariables;
import com.dongah.smartcharger.websocket.ocpp.firmware.FirmwareStatus;
import com.dongah.smartcharger.websocket.ocpp.firmware.FirmwareStatusNotificationRequest;
import com.dongah.smartcharger.websocket.ocpp.firmware.UpdateFirmwareConfirmation;
import com.dongah.smartcharger.websocket.socket.OcppHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlersend.StatusNotificationReq;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class UpdateFirmwareHandler implements OcppHandler {

    private static final Logger logger = LoggerFactory.getLogger(UpdateFirmwareHandler.class);

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {

        String location = payload.has("location") ? payload.getString("location") : "";
        int retries = payload.has("retries") ? payload.getInt("retries") : 1;

        MainActivity activity = ((MainActivity) MainActivity.mContext);
        //응답
        UpdateFirmwareConfirmation updateFirmwareConfirmation = new UpdateFirmwareConfirmation();
        activity.getSocketReceiveMessage().onResultSend(
                updateFirmwareConfirmation.getActionName(),
                messageId,
                updateFirmwareConfirmation
        );

        // 1. firmware status : Downloading
        ChargerConfiguration chargerConfiguration = activity.getChargerConfiguration();
        FirmwareStatusNotificationRequest firmwareStatusNotificationRequest =
                new FirmwareStatusNotificationRequest(FirmwareStatus.Downloading);
        chargerConfiguration.setFirmwareStatus(FirmwareStatus.Downloading);
        activity.getSocketReceiveMessage().onSend(
                connectorId,
                firmwareStatusNotificationRequest.getActionName(),
                firmwareStatusNotificationRequest
        );

        // update firmware 다운 전에 GlobalVariables.ChargerOperation[] = false ==> Unavailable
        Arrays.fill(GlobalVariables.ChargerOperation, false);
        onChargerOperateSave();

        // Status Notification - all
        StatusNotificationReq statusNotificationReq = new StatusNotificationReq();
        statusNotificationReq.sendStatusNotification(0);

        //Ftp
        // location = "ftp://ftpuser@dongahtest.p-e.kr/fw/DEVS100D12.apk";
        // private String USER_NAME = "ftpuser";
        // private static final String PASSWORD = "dev!1q2w3e";
        FtpRxJava ftpRxJava = new FtpRxJava(FileTransType.FIRMWARE, location);
        ftpRxJava.downloadTask();
    }

    private void onChargerOperateSave() {
    }
}
