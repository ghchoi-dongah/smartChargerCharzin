package com.dongah.smartcharger.websocket.ocpp.common.model.validation;

import com.dongah.smartcharger.websocket.ocpp.common.PropertyConstraintException;

public class StringValidator extends Validator<String> {

    private final IValidationRule[] rules;

    public StringValidator(IValidationRule[] rules) {
        this.rules = rules;
    }

    @Override
    public void validate(String value) throws PropertyConstraintException {
        for (IValidationRule rule : rules) {
            rule.validate(value);
        }
    }
}
