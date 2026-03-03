package com.dongah.smartcharger.websocket.ocpp.core.datatransfer.chargzin;

import androidx.annotation.NonNull;

import com.dongah.smartcharger.websocket.ocpp.common.model.Confirmation;
import com.dongah.smartcharger.websocket.ocpp.core.DataTransferStatus;
import com.dongah.smartcharger.websocket.ocpp.utilities.MoreObjects;

import java.util.Objects;

public class ExtendedRemoteStartTransactionConfirm implements Confirmation {


    private static final String ACTION_NAME = "ExtendedRemoteStartTransaction";
    private DataTransferStatus status;
    private String data;


    public String getActionName() {
        return ACTION_NAME;
    }

    public DataTransferStatus getStatus() {
        return status;
    }

    public void setStatus(DataTransferStatus status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public boolean validate() {
        return status != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtendedRemoteStartTransactionConfirm that = (ExtendedRemoteStartTransactionConfirm) o;
        return status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status);
    }

    @NonNull
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("status", status)
                .add("isValid", validate())
                .toString();
    }
}
