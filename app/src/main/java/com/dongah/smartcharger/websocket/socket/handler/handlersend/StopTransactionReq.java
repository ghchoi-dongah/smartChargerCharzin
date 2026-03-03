package com.dongah.smartcharger.websocket.socket.handler.handlersend;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.ChargingCurrentData;
import com.dongah.smartcharger.basefunction.UiSeq;
import com.dongah.smartcharger.utils.LogDataSave;
import com.dongah.smartcharger.websocket.ocpp.common.OccurenceConstraintException;
import com.dongah.smartcharger.websocket.ocpp.core.StopTransactionRequest;
import com.dongah.smartcharger.websocket.ocpp.utilities.ZonedDateTimeConvert;
import com.dongah.smartcharger.websocket.socket.SocketState;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.UUID;

public class StopTransactionReq {

    private static final Logger logger = LoggerFactory.getLogger(StopTransactionReq.class);


    final ZonedDateTimeConvert zonedDateTimeConvert = new ZonedDateTimeConvert();

    public StopTransactionReq() {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendStopTransactionReq(int connectorId) {
        try {
            MainActivity activity = (MainActivity) MainActivity.mContext;
            ChargingCurrentData chargingCurrentData = activity.getClassUiProcess().getChargingCurrentData();
            ZonedDateTime timestamp = zonedDateTimeConvert.doZonedDateTimeToDatetime(chargingCurrentData.getChargingEndTime());

            StopTransactionRequest stopTransactionRequest = new StopTransactionRequest(
                    chargingCurrentData.getPowerMeterStop(),
                    timestamp,
                    chargingCurrentData.getTransactionId(),
                    chargingCurrentData.getStopReason()
            );

            SocketState socketState = activity.getSocketReceiveMessage().getSocket().getState();
            if (socketState.equals(SocketState.OPEN)) {
                //send
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        activity.getSocketReceiveMessage().onSend(
                                connectorId,
                                stopTransactionRequest.getActionName(),
                                stopTransactionRequest);
                    } catch (OccurenceConstraintException e) {
                        throw new RuntimeException(e);
                    }
                }, 1000);

            } else {
                String uuid = UUID.randomUUID().toString();
                saveFullStartTransaction(connectorId, uuid, stopTransactionRequest);
                activity.getClassUiProcess().setUiSeq(UiSeq.FINISH_WAIT);
            }
        } catch (Exception e) {
            logger.error(" sendStopTransactionReq error : {}", e.getMessage());
        }
    }

    private void saveFullStartTransaction(
            int connectorId,
            String uniqueId,
            StopTransactionRequest req) {
        try {
            JSONArray frame = new JSONArray();

            frame.put(2); // CALL
            frame.put(uniqueId);
            frame.put(req.getActionName());

            JSONObject payload = new JSONObject();
            payload.put("idTag", req.getIdTag());
            payload.put("meterStop", req.getMeterStop());
            payload.put("timestamp", req.getTimestamp().toString());
            payload.put("transactionId", req.getTransactionId());
            payload.put("reason", req.getReason());

            frame.put(payload);

            LogDataSave logDataSave = new LogDataSave();
            logDataSave.makeDump(frame.toString());

        } catch (Exception e) {
            logger.error(" saveFullStartTransaction error : {}", e.getMessage());
        }
    }
}
