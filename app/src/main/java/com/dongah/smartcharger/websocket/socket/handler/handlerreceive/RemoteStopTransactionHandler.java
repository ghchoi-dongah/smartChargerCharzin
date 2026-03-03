package com.dongah.smartcharger.websocket.socket.handler.handlerreceive;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.ChargingCurrentData;
import com.dongah.smartcharger.basefunction.UiSeq;
import com.dongah.smartcharger.websocket.ocpp.core.Reason;
import com.dongah.smartcharger.websocket.ocpp.core.RemoteStartStopStatus;
import com.dongah.smartcharger.websocket.ocpp.core.RemoteStopTransactionConfirmation;
import com.dongah.smartcharger.websocket.socket.OcppHandler;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class RemoteStopTransactionHandler implements OcppHandler {

    private static final Logger logger = LoggerFactory.getLogger(RemoteStopTransactionHandler.class);

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {

        int transactionId = payload.has("transactionId") ? payload.getInt("transactionId") : 0;

        // 응답
        sendResponse(connectorId, messageId, transactionId);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendResponse(int connectorId, String messageId, int transactionId) {
        try {
            MainActivity activity = ((MainActivity) MainActivity.mContext);
            ChargingCurrentData chargingCurrentData = activity.getClassUiProcess().getChargingCurrentData();

            RemoteStartStopStatus status = !Objects.equals(chargingCurrentData.getTransactionId(), transactionId) ?
                    RemoteStartStopStatus.Rejected : RemoteStartStopStatus.Accepted;

            RemoteStopTransactionConfirmation remoteStopTransactionConfirmation =
                    new RemoteStopTransactionConfirmation(status);
            activity.getSocketReceiveMessage().onResultSend(
                    remoteStopTransactionConfirmation.getActionName(),
                    messageId,
                    remoteStopTransactionConfirmation
            );

            if (Objects.equals(status, RemoteStartStopStatus.Accepted)) {
                UiSeq uiSeq = activity.getClassUiProcess().getUiSeq();
                if (Objects.equals(uiSeq, UiSeq.CHARGING)) {
                    activity.getClassUiProcess().onRemoteTransactionStop(Reason.Remote);
                }
            }
        } catch (Exception e) {
            logger.error(" RemoteStopTransaction sendResponse error : {}", e.getMessage());
        }
    }
}
