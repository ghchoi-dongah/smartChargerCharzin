package com.dongah.smartcharger.websocket.socket.handler.handlerreceive;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.ChargingCurrentData;
import com.dongah.smartcharger.websocket.ocpp.core.AuthorizationStatus;
import com.dongah.smartcharger.websocket.ocpp.core.ChargePointStatus;
import com.dongah.smartcharger.websocket.ocpp.core.Reason;
import com.dongah.smartcharger.websocket.socket.OcppHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlersend.StatusNotificationReq;

import org.json.JSONObject;

import java.util.Objects;

public class StopTransactionHandler implements OcppHandler {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {

        StatusNotificationReq statusNotificationReq;
        MainActivity activity = (MainActivity) MainActivity.mContext;
        ChargingCurrentData chargingCurrentData = activity.getClassUiProcess().getChargingCurrentData();


        JSONObject idTagInfo = payload.getJSONObject("idTagInfo");

        AuthorizationStatus status = AuthorizationStatus.valueOf(idTagInfo.getString("status"));
        String parentIdTag = idTagInfo.has("parentIdTag") ? idTagInfo.getString("parentIdTag") : null;

        //StatusNotification(Finish)
        //accept continue
        if (Objects.equals(status, AuthorizationStatus.Accepted)) {
            statusNotificationReq = new StatusNotificationReq();
            chargingCurrentData.setChargePointStatus(ChargePointStatus.Finishing);
            //Status Notification
            statusNotificationReq.sendStatusNotification(connectorId, ChargePointStatus.Finishing);
            // EVDisconnected 인 경우
            if (Objects.equals(chargingCurrentData.getStopReason(), Reason.EVDisconnected)) {
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.postDelayed(() -> {
                    chargingCurrentData.setChargePointStatus(ChargePointStatus.Available);
                    statusNotificationReq.sendStatusNotification(connectorId, ChargePointStatus.Available);
                }, 3000);
            }
        }
    }
}
