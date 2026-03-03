package com.dongah.smartcharger.websocket.socket.handler.handlerreceive;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.websocket.ocpp.core.DataTransferStatus;
import com.dongah.smartcharger.websocket.socket.OcppHandler;

import org.json.JSONObject;

import java.util.Objects;

public class UnpluggedTransactionHandler implements OcppHandler {
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {

        DataTransferStatus status = DataTransferStatus.valueOf(payload.getString("status"));

        if (Objects.equals(status, DataTransferStatus.Accepted)) {
            MainActivity activity = ((MainActivity) MainActivity.mContext);
            activity.getClassUiProcess().getChargingCurrentData().setTransactionId(0);
        }
    }
}
