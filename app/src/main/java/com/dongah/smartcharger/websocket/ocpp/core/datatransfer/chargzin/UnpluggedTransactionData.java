package com.dongah.smartcharger.websocket.ocpp.core.datatransfer.chargzin;

import androidx.annotation.NonNull;

import com.dongah.smartcharger.websocket.ocpp.utilities.MoreObjects;

import java.util.Objects;

public class UnpluggedTransactionData {


    private int transactionId;
    private int connectorId;
    private String eventTime;


    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(int connectorId) {
        this.connectorId = connectorId;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnpluggedTransactionData that = (UnpluggedTransactionData) o;
        return Objects.equals(transactionId, that.transactionId) && Objects.equals(connectorId, that.connectorId) &&
                Objects.equals(eventTime, that.eventTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, connectorId, eventTime);
    }

    @NonNull
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("transactionId", transactionId)
                .add("connectorId", connectorId)
                .add("eventTime", eventTime)
                .toString();
    }
}
