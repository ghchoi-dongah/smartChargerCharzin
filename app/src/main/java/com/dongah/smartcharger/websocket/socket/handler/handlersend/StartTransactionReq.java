package com.dongah.smartcharger.websocket.socket.handler.handlersend;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.ChargingCurrentData;
import com.dongah.smartcharger.utils.LogDataSave;
import com.dongah.smartcharger.websocket.ocpp.common.OccurenceConstraintException;
import com.dongah.smartcharger.websocket.ocpp.core.StartTransactionRequest;
import com.dongah.smartcharger.websocket.ocpp.utilities.ZonedDateTimeConvert;
import com.dongah.smartcharger.websocket.socket.SocketState;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.UUID;

public class StartTransactionReq {

    private static final Logger logger = LoggerFactory.getLogger(StartTransactionReq.class);

    private final int connectorId ;

    final ZonedDateTimeConvert zonedDateTimeConvert = new ZonedDateTimeConvert();

    public int getConnectorId() {
        return connectorId;
    }

    public StartTransactionReq(int connectorId) {
        this.connectorId = connectorId;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void  sendStartTransactionReq(String idTag) {

        try {
            MainActivity activity = (MainActivity) MainActivity.mContext;
            ChargingCurrentData chargingCurrentData = activity.getClassUiProcess().getChargingCurrentData();

            double meterStart = chargingCurrentData.getPowerMeterStart();

            ZonedDateTime timestamp = zonedDateTimeConvert.doZonedDateTimeToDatetime(chargingCurrentData.getChargingStartTime());
            StartTransactionRequest startTransactionRequest = new StartTransactionRequest(getConnectorId(), idTag, (long) (meterStart), timestamp);

            SocketState socketState = activity.getSocketReceiveMessage().getSocket().getState();
            if (socketState.equals(SocketState.OPEN)) {
                //send
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        activity.getSocketReceiveMessage().onSend(
                                connectorId,
                                startTransactionRequest.getActionName(),
                                startTransactionRequest);
                    } catch (OccurenceConstraintException e) {
                        throw new RuntimeException(e);
                    }
                }, 1000);

            } else {
                //통신이 안되면 저장
                String uuid = UUID.randomUUID().toString();
                saveFullStartTransaction(connectorId, uuid, startTransactionRequest);
//                //화면 전환
//                activity.getClassUiProcess().setUiSeq(UiSeq.CHARGING);
//                FragmentChange fragmentChange = new FragmentChange();
//                fragmentChange.onFragmentChange(UiSeq.CHARGING, "CHARGING", null);
            }
        } catch (Exception e) {
            logger.error(" sendStartTransactionReq error :  {}", e.getMessage());
        }
    }


    private void saveFullStartTransaction(
            int connectorId,
            String uniqueId,
            StartTransactionRequest req) {
        try {
            JSONArray frame = new JSONArray();

            frame.put(2); // CALL
            frame.put(uniqueId);
            frame.put(req.getActionName());

            JSONObject payload = new JSONObject();
            payload.put("connectorId", req.getConnectorId());
            payload.put("idTag", req.getIdTag());
            payload.put("meterStart", req.getMeterStart());
            payload.put("timestamp", req.getTimestamp().toString());

            if (req.getReservationId() != null) {
                payload.put("reservationId", req.getReservationId());
            }

            frame.put(payload);

            LogDataSave logDataSave = new LogDataSave();
            logDataSave.makeDump(frame.toString());

        } catch (Exception e) {
            logger.error(" saveFullStartTransaction error : {}", e.getMessage());
        }
    }


}
