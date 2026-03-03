package com.dongah.smartcharger.websocket.socket.handler.handlerreceive;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.UiSeq;
import com.dongah.smartcharger.websocket.ocpp.core.Reason;
import com.dongah.smartcharger.websocket.ocpp.core.ResetConfirmation;
import com.dongah.smartcharger.websocket.ocpp.core.ResetStatus;
import com.dongah.smartcharger.websocket.ocpp.core.ResetType;
import com.dongah.smartcharger.websocket.socket.OcppHandler;

import org.json.JSONObject;

import java.util.Objects;

public class ResetHandler implements OcppHandler {


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {

        MainActivity activity = ((MainActivity) MainActivity.mContext);
        UiSeq uiSeq;

        ResetType type = ResetType.valueOf(payload.getString("type"));
        ResetConfirmation resetConfirmation = new ResetConfirmation(ResetStatus.Accepted);
        activity.getSocketReceiveMessage().onResultSend(
                resetConfirmation.getActionName(),
                messageId,
                resetConfirmation
        );

        //charging ==> Stop
        uiSeq = activity.getClassUiProcess().getUiSeq();
        if (Objects.equals(uiSeq, UiSeq.CHARGING)) {
            activity.getClassUiProcess().onResetStop(type);
        }
        activity.getClassUiProcess().getChargingCurrentData().setStopReason(type == ResetType.Hard ?
                Reason.HardReset : Reason.SoftReset);
        activity.getClassUiProcess().getChargingCurrentData().setReBoot(true);
    }
}