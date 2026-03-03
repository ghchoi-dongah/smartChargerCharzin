package com.dongah.smartcharger.websocket.ocpp.security;

public enum MessageTrigger {
    BootNotification,
    LogStatusNotification,
    FirmwareStatusNotification,
    Heartbeat,
    MeterValues,
    SignChargePointCertificate,
    StatusNotification;
}
