package com.dongah.smartcharger.websocket.ocpp.common.feature.profile;

import com.dongah.smartcharger.websocket.ocpp.common.feature.ProfileFeature;
import com.dongah.smartcharger.websocket.ocpp.common.model.Confirmation;
import com.dongah.smartcharger.websocket.ocpp.common.model.Request;

import java.util.UUID;

public interface Profile {
    ProfileFeature[] getFeatureList();

    Confirmation handleRequest(UUID sessionIndex, Request request);
}
