package com.dongah.smartcharger.websocket.ocpp.security;

public enum UpdateFirmwareStatus {
    Accepted,
    Rejected,
    AcceptedCanceled,
    InvalidCertificate,
    RevokedCertificate;
}
