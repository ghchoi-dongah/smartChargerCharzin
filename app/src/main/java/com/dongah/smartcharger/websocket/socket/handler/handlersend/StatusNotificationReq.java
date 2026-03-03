package com.dongah.smartcharger.websocket.socket.handler.handlersend;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.GlobalVariables;
import com.dongah.smartcharger.basefunction.UiSeq;
import com.dongah.smartcharger.controlboard.ControlBoard;
import com.dongah.smartcharger.controlboard.RxData;
import com.dongah.smartcharger.websocket.ocpp.core.ChargePointErrorCode;
import com.dongah.smartcharger.websocket.ocpp.core.ChargePointStatus;
import com.dongah.smartcharger.websocket.ocpp.core.StatusNotificationRequest;
import com.dongah.smartcharger.websocket.ocpp.utilities.ZonedDateTimeConvert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;

public class StatusNotificationReq {

    private static final Logger logger = LoggerFactory.getLogger(StatusNotificationReq.class);


    public StatusNotificationReq() {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendStatusNotification(int connectorId) {
        try {
            int startConnectorId, endConnectorId;
            if (connectorId == 0) {
                startConnectorId = 0;
                endConnectorId = GlobalVariables.maxPlugCount;
            } else {
                startConnectorId = connectorId;
                endConnectorId = connectorId + 1;
            }

            //응답 대기 시간을 반영 순차적 보냄
            for (int i = startConnectorId; i < endConnectorId; i++) {
                final int rConnectorId = i;
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    sendSingleStatusNotification(rConnectorId);
                }, 2000);
            }

        } catch (Exception e) {
            logger.error("sendStatusNotification error :  {}", e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendStatusNotification(int connectorId, ChargePointStatus chargePointStatus) {
        try {
            MainActivity activity = (MainActivity) MainActivity.mContext;
            ZonedDateTimeConvert zonedDateTimeConvert = new ZonedDateTimeConvert();
            ZonedDateTime timestamp = zonedDateTimeConvert.doZonedDateTimeToDatetime();

            StatusNotificationRequest statusNotificationRequest = new StatusNotificationRequest(timestamp);
            statusNotificationRequest.setConnectorId(connectorId);
            ControlBoard controlBoard = activity.getControlBoard();
            RxData rxData = controlBoard.getRxData();
            ChargePointErrorCode errorCode = (!controlBoard.isConnected() ? ChargePointErrorCode.EVCommunicationError :
                    rxData.isCsEmergency() ? ChargePointErrorCode.OtherError : ChargePointErrorCode.NoError);
            statusNotificationRequest.setErrorCode(errorCode);
            statusNotificationRequest.setStatus(chargePointStatus);

            activity.getSocketReceiveMessage().onSend(
                    connectorId,
                    statusNotificationRequest.getActionName(),
                    statusNotificationRequest
            );
        } catch (Exception e) {
            logger.error("each sendStatusNotification error :  {}", e.getMessage());
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendSingleStatusNotification(int connectorId) {
        try {
            MainActivity activity = (MainActivity) MainActivity.mContext;
            ZonedDateTimeConvert zonedDateTimeConvert = new ZonedDateTimeConvert();
            ZonedDateTime timestamp = zonedDateTimeConvert.doZonedDateTimeToDatetime();
            StatusNotificationRequest statusNotificationRequest = new StatusNotificationRequest(timestamp);

            statusNotificationRequest.setConnectorId(connectorId);
            ControlBoard controlBoard = activity.getControlBoard();
            RxData rxData = controlBoard.getRxData();

            ChargePointErrorCode errorCode = (!controlBoard.isConnected() ? ChargePointErrorCode.EVCommunicationError :
                    rxData.isCsEmergency() ? ChargePointErrorCode.OtherError : ChargePointErrorCode.NoError);
            statusNotificationRequest.setErrorCode(errorCode);

            UiSeq uiSeq = activity.getClassUiProcess().getUiSeq();
            ChargePointStatus chargePointStatus = rxData.isCsFault() ? ChargePointStatus.Faulted :
                    !GlobalVariables.ChargerOperation[connectorId] ? ChargePointStatus.Unavailable :
                            connectorId == 0 ? ChargePointStatus.Available : convertUiSeqToStatus(uiSeq);
            statusNotificationRequest.setStatus(rxData.isCsFault() ? ChargePointStatus.Faulted :
                    !GlobalVariables.ChargerOperation[connectorId]  ? ChargePointStatus.Unavailable :
                            connectorId == 0 ? ChargePointStatus.Available : chargePointStatus);

            activity.getSocketReceiveMessage().onSend(
                    connectorId,
                    statusNotificationRequest.getActionName(),
                    statusNotificationRequest
            );
        } catch (Exception e) {
            logger.error(" sendSingleStatusNotification {}", e.getMessage());
        }
    }

    private ChargePointStatus convertUiSeqToStatus(UiSeq uiSeq) {
        ChargePointStatus mappedStatus = mapUiSeqToStatus(uiSeq);
        if (mappedStatus != null) return mappedStatus;

        return getStatusFromHardware();
    }


    private ChargePointStatus mapUiSeqToStatus(UiSeq uiSeq) {
        switch (uiSeq) {
            case CHARGING:
                return ChargePointStatus.Charging;
            case PLUG_CHECK:
            case CONNECT_CHECK:
                return ChargePointStatus.Preparing;
            case FINISH:
                return ChargePointStatus.Finishing;
            default:
                return null;
        }
    }

    private ChargePointStatus getStatusFromHardware() {
        MainActivity activity = (MainActivity) MainActivity.mContext;
        if (activity == null) return ChargePointStatus.Available;

        RxData rxData = activity.getControlBoard().getRxData();
        if (rxData != null && rxData.isCsPilot()) {
            return ChargePointStatus.Preparing;
        }
        return ChargePointStatus.Available;
    }


}
