package com.dongah.smartcharger.websocket.socket.handler.handlerreceive;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.ChargingCurrentData;
import com.dongah.smartcharger.basefunction.FragmentChange;
import com.dongah.smartcharger.basefunction.GlobalVariables;
import com.dongah.smartcharger.basefunction.UiSeq;
import com.dongah.smartcharger.controlboard.TxData;
import com.dongah.smartcharger.websocket.ocpp.core.AuthorizationStatus;
import com.dongah.smartcharger.websocket.ocpp.core.ChargePointStatus;
import com.dongah.smartcharger.websocket.socket.OcppHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlersend.StatusNotificationReq;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class StartTransactionHandler implements OcppHandler {

    private static final Logger logger = LoggerFactory.getLogger(StartTransactionHandler.class);

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {

        MainActivity activity = ((MainActivity) MainActivity.mContext);
        ChargingCurrentData chargingCurrentData = activity.getClassUiProcess().getChargingCurrentData();
        //서버에서 transactionId 받음 ==> stopTransaction 계속하여 사용.

        if (GlobalVariables.isDumpSending()) {
            // dump 재전송용 StartTransaction 응답
            logger.info("Dump StartTransaction Conf 수신: {}", payload.getInt("transactionId"));
            GlobalVariables.setDumpTransactionId(payload.getInt("transactionId"));
            activity.getDumpDataSend().onReceiveStartTransactionConf(payload.getInt("transactionId"));
            return;
        }

        AuthorizationStatus status;
        String parentIdTag, expiryDate;
        chargingCurrentData.setTransactionId(payload.getInt("transactionId"));
        JSONObject idTagInfo = payload.has("idTagInfo") ? payload.getJSONObject("idTagInfo") : null;
        if (idTagInfo != null) {
            status = AuthorizationStatus.valueOf(idTagInfo.getString("status"));
            parentIdTag = idTagInfo.has("parentIdTag") ? idTagInfo.getString("parentIdTag") : "";
            expiryDate =  idTagInfo.has("expiryDate") ? idTagInfo.getString("expiryDate") : "";
        } else {
            status = AuthorizationStatus.Accepted;
        }

        //accept continue
        if (Objects.equals(status, AuthorizationStatus.Accepted)) {
            chargingCurrentData.setChargePointStatus(ChargePointStatus.Charging);
            //Status Notification
            StatusNotificationReq statusNotificationReq = new StatusNotificationReq();
            statusNotificationReq.sendStatusNotification(connectorId, ChargePointStatus.Charging);

            activity.getClassUiProcess().setUiSeq(UiSeq.CHARGING);
            FragmentChange fragmentChange = new FragmentChange();
            fragmentChange.onFragmentChange(UiSeq.CHARGING, "CHARGING", null);
        } else {
            TxData txData = activity.getControlBoard().getTxData();
            txData.setUiSequence((short) 3);
            txData.setMainMC(false);
            txData.setPwmDuty((short) 100);
            // DataTransfer Meter data......중지
            activity.getClassUiProcess().onHome();
        }
    }
}
