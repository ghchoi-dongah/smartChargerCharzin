package com.dongah.smartcharger.basefunction;

public enum PaymentType {
    NONE(0),
    MEMBER(1),
    CREDIT(2),
    QR(3),
    TEST(4),
    FREE(5);

    PaymentType(int value) {
        this.value = value;
    }

    private final int value;

    public int value() {
        return value;
    }
}
