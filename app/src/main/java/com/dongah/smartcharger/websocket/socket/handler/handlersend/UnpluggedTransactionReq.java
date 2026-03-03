package com.dongah.smartcharger.websocket.socket.handler.handlersend;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.ChargerConfiguration;
import com.dongah.smartcharger.basefunction.ChargingCurrentData;
import com.dongah.smartcharger.websocket.ocpp.core.datatransfer.chargzin.UnpluggedTransactionData;
import com.dongah.smartcharger.websocket.ocpp.core.datatransfer.chargzin.UnpluggedTransactionRequest;
import com.dongah.smartcharger.websocket.ocpp.utilities.ZonedDateTimeConvert;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class UnpluggedTransactionReq {

    private static final Logger logger = LoggerFactory.getLogger(UnpluggedTransactionRequest.class);

    private final int connectorId ;
    final ZonedDateTimeConvert zonedDateTimeConvert = new ZonedDateTimeConvert();

    public UnpluggedTransactionReq(int connectorId) {
        this.connectorId = connectorId;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendUnpluggedTransactionReq() {
        try {
            MainActivity activity = (MainActivity) MainActivity.mContext;
            ChargerConfiguration chargerConfiguration = activity.getChargerConfiguration();

            UnpluggedTransactionData unpluggedTransactionData = createUnpluggedTransactionData(connectorId);

            UnpluggedTransactionRequest unpluggedTransactionRequest= new UnpluggedTransactionRequest();
            unpluggedTransactionRequest.setVendorId(chargerConfiguration.getChargerPointVendor());
            unpluggedTransactionRequest.setMessageId("UnpluggedTransaction");
            Gson gson = new Gson();
            unpluggedTransactionRequest.setData(gson.toJson(unpluggedTransactionData));

            activity.getSocketReceiveMessage().onSend(
                    connectorId,
                    unpluggedTransactionRequest.getActionName(),
                    unpluggedTransactionRequest
            );
        } catch (Exception e) {
            logger.error(" sendUnpluggedTransactionReq error : {}", e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private UnpluggedTransactionData createUnpluggedTransactionData(int connectorId) {
        try {
            MainActivity activity = (MainActivity) MainActivity.mContext;
            ChargingCurrentData chargingCurrentData = activity.getClassUiProcess().getChargingCurrentData();
            UnpluggedTransactionData unpluggedTransactionData = new UnpluggedTransactionData();
            unpluggedTransactionData.setConnectorId(connectorId);
            unpluggedTransactionData.setTransactionId(chargingCurrentData.getTransactionId());
            if (Objects.equals(chargingCurrentData.getUnplugTime(), "")) {
                ZonedDateTimeConvert zonedDateTimeConvert = new ZonedDateTimeConvert();
                String timestamp = zonedDateTimeConvert.doZonedDateTimeToString();
                unpluggedTransactionData.setEventTime(timestamp);
            } else {
                unpluggedTransactionData.setEventTime(chargingCurrentData.getUnplugTime());
            }
            return unpluggedTransactionData;
        } catch (Exception e) {
            logger.error(" createUnpluggedTransactionData error : {}", e.getMessage());
        }
        return null;
    }
}
