package com.dongah.smartcharger.websocket.socket;

import org.json.JSONObject;

public interface OcppHandler {
    void handle(JSONObject payload, int connectorId, String messageId) throws Exception;
}
