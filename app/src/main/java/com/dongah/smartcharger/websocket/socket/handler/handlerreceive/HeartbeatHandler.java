package com.dongah.smartcharger.websocket.socket.handler.handlerreceive;

import com.dongah.smartcharger.websocket.socket.OcppHandler;

import org.json.JSONObject;

public class HeartbeatHandler implements OcppHandler {

    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {
        String currentTime = payload.getString("currentTime");
    }
}

