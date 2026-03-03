package com.dongah.smartcharger.websocket.ocpp.common.model;

public interface Request extends Validatable {
    boolean transactionRelated();
}
