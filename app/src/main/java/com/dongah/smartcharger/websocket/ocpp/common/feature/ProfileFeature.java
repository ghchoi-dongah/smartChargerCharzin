package com.dongah.smartcharger.websocket.ocpp.common.feature;

import com.dongah.smartcharger.websocket.ocpp.common.feature.profile.Profile;
import com.dongah.smartcharger.websocket.ocpp.common.model.Confirmation;
import com.dongah.smartcharger.websocket.ocpp.common.model.Request;

import java.util.UUID;

public abstract class ProfileFeature {

    private Profile profile;

    public ProfileFeature(Profile ownerProfile) {
        profile = ownerProfile;
    }

    public Confirmation handleRequest(UUID sessionIndex, Request request) {
        return profile.handleRequest(sessionIndex, request);
    }

}
