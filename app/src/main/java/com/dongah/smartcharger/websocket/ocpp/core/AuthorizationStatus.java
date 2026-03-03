package com.dongah.smartcharger.websocket.ocpp.core;

/**
 * Accepted values {@link IdTagInfo} for {@link AuthorizeConfirmation}
 *
 * @see IdTagInfo
 */
public enum AuthorizationStatus {
    Accepted,
    Blocked,
    Expired,
    Invalid,
    ConcurrentTx
}
