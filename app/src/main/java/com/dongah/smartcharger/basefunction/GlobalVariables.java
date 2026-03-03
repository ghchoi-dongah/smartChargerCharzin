package com.dongah.smartcharger.basefunction;

import android.os.Environment;

import com.dongah.smartcharger.websocket.ocpp.localauthlist.UpdateStatus;

import java.io.File;

public class GlobalVariables {

    //storage/emulated/0/download
    public static String ROOT_PATH = Environment.getExternalStorageDirectory().toString() + File.separator + "Download";

    public static String VERSION = "DEVW 1.0.0";
    public static String FW_VERSION = "1.0.0";

    /**
     * Max plug count
     */
    public static int maxChannel = 1;
    public static int maxPlugCount = 2;
    public static boolean[] ChargerOperation = new boolean[maxPlugCount];
    // 환경 member card register
    public static boolean memberResister = false;
    /**
     * Handler message type
     */
//    public static final int MESSAGE_HANDLER_BOOT_NOTIFICATION = 100;
//    public static final int MESSAGE_HANDLER_HEART_BEAT = 101;
//    public static final int MESSAGE_HANDLER_STATUS_NOTIFICATION_BOOT = 102;
//    public static final int MESSAGE_HANDLER_STATUS_NOTIFICATION = 103;
//    public static final int MESSAGE_HANDLER_GET_PRICE = 104;
//    public static final int MESSAGE_HANDLER_START_TRANSACTION = 106;
//    public static final int MESSAGE_HANDLER_PAY_INFO = 107;
//    public static final int MESSAGE_HANDLER_PARTIAL_CANCEL = 108;
//    public static final int MESSAGE_HANDLER_STOP_TRANSACTION = 109;
//    public static final int MESSAGE_HANDLER_REMOTE_START_TRANSACTION = 110;
//    public static final int MESSAGE_HANDLER_REMOTE_STOP_TRANSACTION = 111;
//    public static final int MESSAGE_HANDLER_RESET = 112;
//    public static final int MESSAGE_HANDLER_AUTHORIZE = 113;
//    public static final int MESSAGE_HANDLER_RESULT_PRICE = 114;
//    public static final int MESSAGE_HANDLER_SMS_MESSAGE = 115;
//    public static final int MESSAGE_HANDLER_ANNOUNCE = 116;
//    public static final int MESSAGE_HANDLER_TRIGGER_MESSAGE = 117;
//    public static final int MESSAGE_CHANGE_CONFIGURATION = 121;
//    public static final int MESSAGE_CHANGE_AVAILABILITY = 122;
//    public static final int MESSAGE_GET_CONFIGURATION = 123;
//    public static final int MESSAGE_CLEAR_CACHE = 124;
//    public static final int MESSAGE_HANDLER_CHANGE_AVAILABILITY = 125;
//    public static final int MESSAGE_SEND_LOCAL_LIST = 126;
//    public static final int MESSAGE_CLEAR_CHARGING_PROFILE = 127;
//
//    public static final int MESSAGE_HANDLER_LOCAL_LIST = 128;
//    public static final int MESSAGE_HANDLER_FIRMWARE_STATUS = 129;

    /**
     * ocpp
     */



    ///2026.02.11 차지인 Configuration Key

    public static boolean reconnectCheck = false;
    public static String configurationKey = "";
    public static boolean notSupportedKey = false;
    public static UpdateStatus updateStatus;

    public static int WebsocketPingInterval = 300;
    public static int HeartBeatInterval = 360;
    public static int MeterValueSampleInterval = 900;
    public static int ConnectionTimeOut = 60;
    public static boolean LocalPreAuthorize = false;
    public static boolean AuthorizationCacheEnabled = false;

    public static String ServerURL;
    public static double  MemberPriceUnit = 292.0;
    public static String MemberPricePowerUnit = "kWh";
    public static String MemberPriceMoneyUnit = "Won";
    public static double  NonMemberPriceUnit = 225.0;
    public static String NonMemberPricePowerUnit = "kWh";
    public static String NonMemberPriceMoneyUnit = "Won";
    public static int MaxChargingTime = 780;
    public static int MaxChargingRate = 90;


    public static int dumpTransactionId = 0;
    public static boolean dumpSending = false;
    public static String AuthorizationKey = "";
    public static String SecurityProfile = "1";
    public static boolean Scheduled = false;

    public static int requestId;


    //
    public static short totalPackets;


    public static String getRootPath() {
        return ROOT_PATH;
    }

    public static void setRootPath(String rootPath) {
        ROOT_PATH = rootPath;
    }

    public static String getVERSION() {
        return VERSION;
    }

    public static void setVERSION(String VERSION) {
        GlobalVariables.VERSION = VERSION;
    }

    public static String getFwVersion() {
        return FW_VERSION;
    }

    public static void setFwVersion(String fwVersion) {
        FW_VERSION = fwVersion;
    }

    public static int getMaxChannel() {
        return maxChannel;
    }

    public static void setMaxChannel(int maxChannel) {
        GlobalVariables.maxChannel = maxChannel;
    }

    public static int getMaxPlugCount() {
        return maxPlugCount;
    }

    public static void setMaxPlugCount(int maxPlugCount) {
        GlobalVariables.maxPlugCount = maxPlugCount;
    }

    public static boolean isMemberResister() {
        return memberResister;
    }

    public static void setMemberResister(boolean memberResister) {
        GlobalVariables.memberResister = memberResister;
    }

    public static boolean isReconnectCheck() {
        return reconnectCheck;
    }

    public static void setReconnectCheck(boolean reconnectCheck) {
        GlobalVariables.reconnectCheck = reconnectCheck;
    }

    public static String getConfigurationKey() {
        return configurationKey;
    }

    public static void setConfigurationKey(String configurationKey) {
        GlobalVariables.configurationKey = configurationKey;
    }

    public static boolean isNotSupportedKey() {
        return notSupportedKey;
    }

    public static void setNotSupportedKey(boolean notSupportedKey) {
        GlobalVariables.notSupportedKey = notSupportedKey;
    }

    public static int getConnectionTimeOut() {
        return ConnectionTimeOut;
    }

    public static void setConnectionTimeOut(int connectionTimeOut) {
        ConnectionTimeOut = connectionTimeOut;
    }

    public static boolean isLocalPreAuthorize() {
        return LocalPreAuthorize;
    }

    public static void setLocalPreAuthorize(boolean localPreAuthorize) {
        LocalPreAuthorize = localPreAuthorize;
    }

    public static boolean isAuthorizationCacheEnabled() {
        return AuthorizationCacheEnabled;
    }

    public static void setAuthorizationCacheEnabled(boolean authorizationCacheEnabled) {
        AuthorizationCacheEnabled = authorizationCacheEnabled;
    }

    public static int getMeterValueSampleInterval() {
        return MeterValueSampleInterval;
    }

    public static void setMeterValueSampleInterval(int meterValueSampleInterval) {
        MeterValueSampleInterval = meterValueSampleInterval;
    }

    public static int getHeartBeatInterval() {
        return HeartBeatInterval;
    }

    public static void setHeartBeatInterval(int heartBeatInterval) {
        HeartBeatInterval = heartBeatInterval;
    }

    public static int getDumpTransactionId() {
        return dumpTransactionId;
    }

    public static void setDumpTransactionId(int dumpTransactionId) {
        GlobalVariables.dumpTransactionId = dumpTransactionId;
    }

    public static boolean isDumpSending() {
        return dumpSending;
    }

    public static void setDumpSending(boolean dumpSending) {
        GlobalVariables.dumpSending = dumpSending;
    }

    public static String getAuthorizationKey() {
        return AuthorizationKey;
    }

    public static void setAuthorizationKey(String authorizationKey) {
        AuthorizationKey = authorizationKey;
    }


    public static int getRequestId() {
        return requestId;
    }

    public static void setRequestId(int requestId) {
        GlobalVariables.requestId = requestId;
    }


    public static short getTotalPackets() {
        return totalPackets;
    }

    public static void setTotalPackets(short totalPackets) {
        GlobalVariables.totalPackets = totalPackets;
    }

    public static String getSecurityProfile() {
        return SecurityProfile;
    }

    public static void setSecurityProfile(String securityProfile) {
        SecurityProfile = securityProfile;
    }

    public static boolean isScheduled() {
        return Scheduled;
    }

    public static void setScheduled(boolean scheduled) {
        Scheduled = scheduled;
    }

    public static int getWebsocketPingInterval() {
        return WebsocketPingInterval;
    }

    public static void setWebsocketPingInterval(int websocketPingInterval) {
        WebsocketPingInterval = websocketPingInterval;
    }

    public static String getServerURL() {
        return ServerURL;
    }

    public static void setServerURL(String serverURL) {
        ServerURL = serverURL;
    }

    public static double getMemberPriceUnit() {
        return MemberPriceUnit;
    }

    public static void setMemberPriceUnit(double  memberPriceUnit) {
        MemberPriceUnit = memberPriceUnit;
    }

    public static String getMemberPricePowerUnit() {
        return MemberPricePowerUnit;
    }

    public static void setMemberPricePowerUnit(String memberPricePowerUnit) {
        MemberPricePowerUnit = memberPricePowerUnit;
    }

    public static String getMemberPriceMoneyUnit() {
        return MemberPriceMoneyUnit;
    }

    public static void setMemberPriceMoneyUnit(String memberPriceMoneyUnit) {
        MemberPriceMoneyUnit = memberPriceMoneyUnit;
    }

    public static double  getNonMemberPriceUnit() {
        return NonMemberPriceUnit;
    }

    public static void setNonMemberPriceUnit(double  nonMemberPriceUnit) {
        NonMemberPriceUnit = nonMemberPriceUnit;
    }

    public static String getNonMemberPricePowerUnit() {
        return NonMemberPricePowerUnit;
    }

    public static void setNonMemberPricePowerUnit(String nonMemberPricePowerUnit) {
        NonMemberPricePowerUnit = nonMemberPricePowerUnit;
    }

    public static String getNonMemberPriceMoneyUnit() {
        return NonMemberPriceMoneyUnit;
    }

    public static void setNonMemberPriceMoneyUnit(String nonMemberPriceMoneyUnit) {
        NonMemberPriceMoneyUnit = nonMemberPriceMoneyUnit;
    }

    public static int getMaxChargingTime() {
        return MaxChargingTime;
    }

    public static void setMaxChargingTime(int maxChargingTime) {
        MaxChargingTime = maxChargingTime;
    }

    public static int getMaxChargingRate() {
        return MaxChargingRate;
    }

    public static void setMaxChargingRate(int maxChargingRate) {
        MaxChargingRate = maxChargingRate;
    }
}
