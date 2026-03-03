package com.dongah.smartcharger.websocket.socket.handler.handlersend;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.websocket.ocpp.core.AuthorizeRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorizeReq {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizeReq.class);

    private final int connectorId ;

    public AuthorizeReq(int connectorId) {
        this.connectorId = connectorId;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendAuthorize(String idTag) {
        try {
            MainActivity activity = (MainActivity) MainActivity.mContext;
            AuthorizeRequest authorizeRequest = new AuthorizeRequest(idTag);
            activity.getSocketReceiveMessage().onSend(getConnectorId(), authorizeRequest.getActionName(), authorizeRequest);
        } catch (Exception e) {
            logger.error("sendAuthorize error :  {}", e.getMessage());
        }
    }

    public int getConnectorId() {
        return connectorId;
    }
}
