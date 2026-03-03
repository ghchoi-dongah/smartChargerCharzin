package com.dongah.smartcharger.websocket.socket.handler.handlerreceive;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.ChargingCurrentData;
import com.dongah.smartcharger.basefunction.GlobalVariables;
import com.dongah.smartcharger.basefunction.PaymentType;
import com.dongah.smartcharger.basefunction.UiSeq;
import com.dongah.smartcharger.websocket.ocpp.core.ChargePointStatus;
import com.dongah.smartcharger.websocket.ocpp.core.RemoteStartStopStatus;
import com.dongah.smartcharger.websocket.ocpp.core.RemoteStartTransactionConfirmation;
import com.dongah.smartcharger.websocket.socket.OcppHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlersend.AuthorizeReq;
import com.dongah.smartcharger.websocket.socket.handler.handlersend.StatusNotificationReq;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class RemoteStartTransactionHandler implements OcppHandler {

    private static final Logger logger = LoggerFactory.getLogger(RemoteStartTransactionHandler.class);


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {
        MainActivity activity = ((MainActivity) MainActivity.mContext);
        ChargingCurrentData chargingCurrentData = activity.getClassUiProcess().getChargingCurrentData();

        boolean remoteStatus = GlobalVariables.ChargerOperation[connectorId] && connectorId != 0;

        if (remoteStatus) {
            chargingCurrentData.setConnectorId(payload.getInt("connectorId"));
            chargingCurrentData.setIdTag(payload.getString("idTag"));
            chargingCurrentData.setPaymentType(PaymentType.MEMBER);
        }
        // 응답
        sendResponse(connectorId, messageId, chargingCurrentData.getIdTag(), remoteStatus);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendResponse(int connectorId, String messageId, String idTag, boolean remoteStatus) {
        try {
            MainActivity activity = ((MainActivity) MainActivity.mContext);
            UiSeq uiSeq = activity.getClassUiProcess().getUiSeq();

            RemoteStartStopStatus status = !Objects.equals(uiSeq, UiSeq.INIT) ? RemoteStartStopStatus.Rejected
                    : connectorId == 0 ? RemoteStartStopStatus.Rejected
                    : !remoteStatus ? RemoteStartStopStatus.Rejected
                    : RemoteStartStopStatus.Accepted;
            RemoteStartTransactionConfirmation remoteStartTransactionConfirmation =
                    new RemoteStartTransactionConfirmation(status);
            activity.getSocketReceiveMessage().onResultSend(
                    remoteStartTransactionConfirmation.getActionName(),
                    messageId,
                    remoteStartTransactionConfirmation
            );

            if (!Objects.equals(status, RemoteStartStopStatus.Accepted)) {
                //Authorize
                AuthorizeReq authorizeReq = new AuthorizeReq(connectorId);
                authorizeReq.sendAuthorize(idTag);
                //status Notification
                ChargingCurrentData chargingCurrentData = activity.getClassUiProcess().getChargingCurrentData();
                if (!Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Preparing)) {
                    chargingCurrentData.setChargePointStatus(ChargePointStatus.Preparing);
                    StatusNotificationReq statusNotificationReq = new StatusNotificationReq();
                    statusNotificationReq.sendStatusNotification(connectorId);
                }
            }

        } catch (Exception e) {
            logger.error(" sendResponse error : {}", e.getMessage());
        }
    }
}
