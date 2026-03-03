package com.dongah.smartcharger.websocket.ocpp.common.model.validation;

import com.dongah.smartcharger.websocket.ocpp.common.PropertyConstraintException;

public interface IValidationRule {
    void validate(String value) throws PropertyConstraintException;
}
