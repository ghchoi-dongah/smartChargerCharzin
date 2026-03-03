package com.dongah.smartcharger.websocket.socket.handler.handlerreceive;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.ChargingCurrentData;
import com.dongah.smartcharger.basefunction.UiSeq;
import com.dongah.smartcharger.websocket.ocpp.core.ChargePointStatus;
import com.dongah.smartcharger.websocket.ocpp.core.DataTransferStatus;
import com.dongah.smartcharger.websocket.ocpp.core.datatransfer.chargzin.ExtendedRemoteStartTransactionConfirm;
import com.dongah.smartcharger.websocket.socket.OcppHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlersend.StatusNotificationReq;

import org.json.JSONObject;

public class ExtendedRemoteStartTransactionHandler implements OcppHandler {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {

        MainActivity activity = ((MainActivity) MainActivity.mContext);
        String vendorId = payload.getString("vendorId");
        String dataStr = payload.getString("data");
        JSONObject dataJson = new JSONObject(dataStr);

        String idTag = dataJson.getString("idTag");
        String value = dataJson.getString("value");
        String unit = dataJson.getString("unit");
        int cId = dataJson.getInt("connectorId");

        // 응답
        ExtendedRemoteStartTransactionConfirm extendedRemoteStartTransactionConfirm =
                new ExtendedRemoteStartTransactionConfirm();
        UiSeq uiSeq = activity.getClassUiProcess().getUiSeq();
        extendedRemoteStartTransactionConfirm.setStatus(
                uiSeq == UiSeq.INIT ? DataTransferStatus.Accepted
                        : DataTransferStatus.Rejected);
        activity.getSocketReceiveMessage().onResultSend(
                extendedRemoteStartTransactionConfirm.getActionName(),
                messageId,
                extendedRemoteStartTransactionConfirm
        );

        if (uiSeq == UiSeq.INIT) {
            ChargingCurrentData chargingCurrentData = activity.getClassUiProcess().getChargingCurrentData();
            chargingCurrentData.setChargePointStatus(ChargePointStatus.Preparing);
            chargingCurrentData.setIdTag(idTag);
            chargingCurrentData.setConnectorId(cId);
            chargingCurrentData.setExtendedRemoteStart(true);
            chargingCurrentData.setExtendedUnit(unit);
            long extendedValue = Long.parseLong(value);
            chargingCurrentData.setExtendedValue(extendedValue);
            //status notification
            StatusNotificationReq statusNotificationReq = new StatusNotificationReq();
            statusNotificationReq.sendStatusNotification(cId, ChargePointStatus.Preparing);
            //화면 전환
            activity.getClassUiProcess().setUiSeq(UiSeq.PLUG_CHECK);
            activity.getFragmentChange().onFragmentChange(UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
        }


    }
}
