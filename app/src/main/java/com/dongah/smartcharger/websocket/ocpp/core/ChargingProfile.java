package com.dongah.smartcharger.websocket.ocpp.core;

import androidx.annotation.NonNull;

import com.dongah.smartcharger.websocket.ocpp.common.PropertyConstraintException;
import com.dongah.smartcharger.websocket.ocpp.common.model.Validatable;
import com.dongah.smartcharger.websocket.ocpp.utilities.MoreObjects;

import java.time.ZonedDateTime;
import java.util.Objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(propOrder = {
        "chargingProfileId",
        "transactionId",
        "stackLevel",
        "chargingProfilePurpose",
        "chargingProfileKind",
        "recurrencyKind",
        "validFrom",
        "validTo",
        "chargingSchedule"
})
public class ChargingProfile implements Validatable {

    private Integer chargingProfileId;
    private Integer transactionId;
    private Integer stackLevel;
    private ChargingProfilePurposeType chargingProfilePurpose;
    private ChargingProfileKindType chargingProfileKind;
    private RecurrencyKindType recurrencyKind;
    private ZonedDateTime validFrom;
    private ZonedDateTime validTo;
    private ChargingSchedule chargingSchedule;

    /**
     * @deprecated use {@link #ChargingProfile(Integer, Integer, ChargingProfilePurposeType,
     * ChargingProfileKindType, ChargingSchedule)} to be sure to set required fields
     */
    @Deprecated
    public ChargingProfile() {
    }

    /**
     * Handle required values
     *
     * @param chargingProfileId      Integer, see {@link #setChargingProfileId(Integer)}
     * @param stackLevel             Integer, see {@link #setStackLevel(Integer)}
     * @param chargingProfilePurpose the {@link ChargingProfilePurposeType}, see {@link
     *                               #setChargingProfilePurpose(ChargingProfilePurposeType)}
     * @param chargingProfileKind    the {@link ChargingProfileKindType}, see {@link
     *                               #setChargingProfileKind(ChargingProfileKindType)}
     * @param chargingSchedule       the {@link ChargingSchedule}, see {@link
     *                               #setChargingSchedule(ChargingSchedule)}
     */
    public ChargingProfile(Integer chargingProfileId,
                           Integer stackLevel,
                           ChargingProfilePurposeType chargingProfilePurpose,
                           ChargingProfileKindType chargingProfileKind,
                           ChargingSchedule chargingSchedule) {
        this.chargingProfileId = chargingProfileId;
        this.stackLevel = stackLevel;
        this.chargingProfilePurpose = chargingProfilePurpose;
        this.chargingProfileKind = chargingProfileKind;
        this.chargingSchedule = chargingSchedule;
    }

    /**
     * Handle required values
     *
     * @param chargingProfileId      Integer, see {@link #setChargingProfileId(Integer)}
     * @param stackLevel             Integer, see {@link #setStackLevel(Integer)}
     * @param chargingProfilePurpose the {@link ChargingProfilePurposeType}, see {@link
     *                               #setChargingProfilePurpose(ChargingProfilePurposeType)}
     * @param chargingProfileKind    the {@link ChargingProfileKindType}, see {@link
     *                               #setChargingProfileKind(ChargingProfileKindType)}
     * @deprecated use {@link #ChargingProfile(Integer, Integer, ChargingProfilePurposeType,
     * ChargingProfileKindType, ChargingSchedule)} to be sure to set required fields
     */
    @Deprecated
    public ChargingProfile(
            Integer chargingProfileId,
            Integer stackLevel,
            ChargingProfilePurposeType chargingProfilePurpose,
            ChargingProfileKindType chargingProfileKind) {
        this.chargingProfileId = chargingProfileId;
        this.stackLevel = stackLevel;
        this.chargingProfilePurpose = chargingProfilePurpose;
        this.chargingProfileKind = chargingProfileKind;
    }

    public Integer getChargingProfileId() {
        return chargingProfileId;
    }

    /**
     * Required. Unique identifier for this profile
     * 프로파일 유일 식별자
     *
     * @param chargingProfileId Integer
     */
    @XmlElement
    public void setChargingProfileId(Integer chargingProfileId) {
        if (chargingProfileId == null) {
            throw new PropertyConstraintException(null, "chargingProfileId must be present");
        }
        this.chargingProfileId = chargingProfileId;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    /**
     * Optional. Only valid if ChargingProfilePurpose is set to TxProfile, the transactionId May be
     * used to match the profile to a specific transaction.
     * ChargeProfilePurpose 가 TxProfile 로 설정된 경우에만 유호
     * TransactionId는 프로파일 특정 트랜잭션과 일치
     *
     * @param transactionId Integer
     */
    @XmlElement
    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getStackLevel() {
        return stackLevel;
    }

    /**
     * Required. Value determining level in hierarchy stack of profiles. Higher values have precedence
     * over lower values. Lowest level is 0.
     * 프로필 계측 스택에서 값을 결정. 높은 값이 값보다 우서. 가장 낮은 수준 == 0.
     *
     * @param stackLevel Integer
     */
    @XmlElement
    public void setStackLevel(Integer stackLevel) {
        if (stackLevel == null || stackLevel < 0) {
            throw new PropertyConstraintException(stackLevel, "stackLevel must be >= 0");
        }
        this.stackLevel = stackLevel;
    }

    public ChargingProfilePurposeType getChargingProfilePurpose() {
        return chargingProfilePurpose;
    }

    /**
     * Required. Unique identifier for this profile.
     *
     * @param chargingProfilePurpose the {@link ChargingProfilePurposeType}
     */
    @XmlElement
    public void setChargingProfilePurpose(ChargingProfilePurposeType chargingProfilePurpose) {
        this.chargingProfilePurpose = chargingProfilePurpose;
    }

    /**
     * Unique identifier for this profile.
     *
     * @return identifier for this profile
     */
    @Deprecated
    public ChargingProfilePurposeType objChargingProfilePurpose() {
        return chargingProfilePurpose;
    }

    public ChargingProfileKindType getChargingProfileKind() {
        return chargingProfileKind;
    }

    /**
     * Required. Indicates the kind of schedule
     * schedule 의 종류
     *
     * @param chargingProfileKind the {@link ChargingProfileKindType}
     */
    @XmlElement
    public void setChargingProfileKind(ChargingProfileKindType chargingProfileKind) {
        this.chargingProfileKind = chargingProfileKind;
    }

    public RecurrencyKindType getRecurrencyKind() {
        return recurrencyKind;
    }

    /**
     * Required. Indicates the kind of schedule.
     *
     * @param recurrencyKind the {@link RecurrencyKindType}
     */
    @XmlElement
    public void setRecurrencyKind(RecurrencyKindType recurrencyKind) {
        this.recurrencyKind = recurrencyKind;
    }

    /**
     * Indicates the start point of a recurrence.
     *
     * @return start point of a recurrency
     */
    @Deprecated
    public RecurrencyKindType objRecurrencyKind() {
        return recurrencyKind;
    }

    /**
     * Indicates the kind of schedule.
     *
     * @return kind of schedule
     */
    @Deprecated
    public ChargingProfileKindType objChargingProfileKind() {
        return chargingProfileKind;
    }

    public ZonedDateTime getValidFrom() {
        return validFrom;
    }

    /**
     * Optional. Point in time at which the profile starts to be valid. If absent, the profile is
     * valid as soon as it is received by the Charge Point.
     *
     * @param validFrom the {@link ZonedDateTime}
     */
    public void setValidFrom(ZonedDateTime validFrom) {
        this.validFrom = validFrom;
    }

    /**
     * Point in time at which the profile starts to be valid. If absent, the profile is valid as soon
     * as it is received by the Charge Point.
     * 프로파일 유효기간. 프로파일이 없는 경유 Charge Point 받는 즉시 유효
     *
     * @return Point in time at which the profile starts to be valid
     */
    @Deprecated
    public ZonedDateTime objValidFrom() {
        return this.validFrom;
    }


    public ZonedDateTime getValidTo() {
        return validTo;
    }

    /**
     * Optional. Point in time at which the profile stops to be valid. If absent, the profile is valid
     * until it is replaced by another profile
     *
     * @param validTo the {@link ZonedDateTime}
     */
    @XmlElement
    public void setValidTo(ZonedDateTime validTo) {
        this.validTo = validTo;
    }

    /**
     * Point in time at which the profile stops to be valid. If absent, the profile is valid until it
     * is replaced by another profile
     * 프로파일이 유효하지 않게 중지되는 시점. 프로파일이 없는 경우 다른 프로필로 대체될 때까지 유효
     *
     * @return Point in time at which the profile stops to be valid
     */
    @Deprecated
    public ZonedDateTime objValidTo() {
        return validTo;
    }

    public ChargingSchedule getChargingSchedule() {
        return chargingSchedule;
    }

    /**
     * Required. Contains limits for the available power or current over time.
     * 시간경과에 따른 사용 가능한 전력 또는 전류 재한
     *
     * @param chargingSchedule the {@link ChargingSchedule}
     */
    @XmlElement
    public void setChargingSchedule(ChargingSchedule chargingSchedule) {
        this.chargingSchedule = chargingSchedule;
    }

    @Override
    public boolean validate() {
        boolean valid = chargingProfileId != null;
        valid &= (stackLevel != null && stackLevel >= 0);
        valid &= (transactionId == null || chargingProfilePurpose == ChargingProfilePurposeType.TxProfile);
        valid &= chargingProfileKind != null;
        valid &= (chargingSchedule != null && chargingSchedule.validate());
        return valid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChargingProfile that = (ChargingProfile) o;
        return Objects.equals(chargingProfileId, that.chargingProfileId)
                && Objects.equals(transactionId, that.transactionId)
                && Objects.equals(stackLevel, that.stackLevel)
                && chargingProfilePurpose == that.chargingProfilePurpose
                && chargingProfileKind == that.chargingProfileKind
                && recurrencyKind == that.recurrencyKind
                && Objects.equals(validFrom, that.validFrom)
                && Objects.equals(validTo, that.validTo)
                && Objects.equals(chargingSchedule, that.chargingSchedule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                chargingProfileId,
                transactionId,
                stackLevel,
                chargingProfilePurpose,
                chargingProfileKind,
                recurrencyKind,
                validFrom,
                validTo,
                chargingSchedule);
    }

    @NonNull
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("chargingProfileId", chargingProfileId)
                .add("transactionId", transactionId)
                .add("stackLevel", stackLevel)
                .add("chargingProfilePurpose", chargingProfilePurpose)
                .add("chargingProfileKind", chargingProfileKind)
                .add("recurrencyKind", recurrencyKind)
                .add("validFrom", validFrom)
                .add("validTo", validTo)
                .add("chargingSchedule", chargingSchedule)
                .add("isValid", validate())
                .toString();
    }

}
