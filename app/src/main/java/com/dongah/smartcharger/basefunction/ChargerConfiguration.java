package com.dongah.smartcharger.basefunction;

import android.os.Environment;
import android.text.TextUtils;

import com.dongah.smartcharger.utils.FileManagement;
import com.dongah.smartcharger.websocket.ocpp.firmware.DiagnosticsStatus;
import com.dongah.smartcharger.websocket.ocpp.firmware.FirmwareStatus;
import com.dongah.smartcharger.websocket.ocpp.security.SignedFirmwareStatus;
import com.dongah.smartcharger.websocket.ocpp.security.UploadLogStatus;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ChargerConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ChargerConfiguration.class);

    public static final String CONFIG_FILE_NAME = "config";
    FileManagement fileManagement;


    public String rootPath = "";

    /**
     * TLS3800 id
     */
    public String MID = "KIOSK1114915545";

    /**
     * Max Channel Count
     */
    public int maxChannel = 1;

    /**
     * 충전기 ID, 충전기 No
     */
    public String chargerId = "";


    /**
     * server connection string
     */
    public String serverConnectingString = "ws://119.65.249.4";
    public int serverPort = 17000;

    /**
     * 충전기 타입
     */
    public int chargerType = 2;

    /**
     * 0:credit+member  1:member
     */
    public String selectPayment = "0";
    public int selectPaymentId;

    /**
     * 0:server 2:auto 3:test 4:PowerMetertest
     */
    public String authMode = "0";
    public int authModeId;

    /**
     * device serial port
     */
    public String controlCom = "/dev/ttyS0";
    public String rfCom = "/dev/ttyS5";     //not used
    public String PlcCom = "/dev/ttyS3";

    /**
     * 테스트 충전 단가
     */
    public String testPrice = "313.0";
    /**
     * 전류 제한 A
     *  100% 기본 대기
     *   0 : 50% 30A  /  1: 40% 24A    /  2: 30%  18A
     *   3 : 25% 15A  /  4: 16% 9.6A   /  5: 10%  6A
     */
    public int dr = 50;
    public int drCode = 0;  //position

    /**
     * charging point configuration setting
     */
    public String chargerPointVendor = "Dongah";
    public String chargerPointModel = "DEVW007";
    public int chargerPointModelCode = 0;
    public String unitPriceVendorCode = "kr.co.dongah";
    public String chargerPointSerialNumber = "";
    public String firmwareVersion = "";
    public String iccid = "";                         //this contains the ICCID of modem's SIM card
    public String imsi = "";                          //this contains the IMSI of modem's SIM card

    public String gpsX = "";
    public String gpsY = "";

    public String m2mTel = "";
    public String rfMaker = "";
    public int statusTime = 300;

    public int statusNotificationDelay = 300;
    public int targetChargingTime = 0;
    public boolean StopConfirm;
    public boolean signed = true ;
    public boolean chargerPowerType;
    public boolean usedPLC;
    public FirmwareStatus firmwareStatus = FirmwareStatus.Idle;
    public SignedFirmwareStatus signedFirmwareStatus = SignedFirmwareStatus.Idle;
    public DiagnosticsStatus diagnosticsStatus = DiagnosticsStatus.Idle;
    public UploadLogStatus uploadLogStatus = UploadLogStatus.Idle;

    //Battery info data count
    public int configCnt = 10;
    int targetSoc = 80;


    public ChargerConfiguration() {
        setRootPath(Environment.getExternalStorageDirectory().toString() + File.separator + "Download");
        fileManagement = new FileManagement();
    }

    public void onLoadConfiguration() {
        try {
            File targetFile = new File(GlobalVariables.ROOT_PATH + File.separator + CONFIG_FILE_NAME);
            String configurationString;
            if (!targetFile.exists()) onSaveConfiguration();

            //get file context json string
            configurationString = fileManagement.getStringFromFile(GlobalVariables.ROOT_PATH  + File.separator + CONFIG_FILE_NAME);
            if (!TextUtils.isEmpty(configurationString)) {
                JSONObject obj = new JSONObject(configurationString);
                setChargerType(obj.getInt("CHARGER_TYPE"));
                setChargerPointModelCode(obj.getInt("CHARGER_POINT_MODEL_CODE"));
                setChargerId(obj.getString("CHARGER_ID"));
                setServerConnectingString(obj.getString("SERVER_CONNECTING_STRING"));
                setServerPort(obj.getInt("SERVER_PORT"));
                setControlCom(obj.getString("CONTROL_COM"));
                setRfCom(obj.getString("RF_COM"));
                setPlcCom(obj.getString("PLC_COM"));
                setDuty(obj.getInt("DUTY"));
                setChargerPointVendor(obj.getString("CHARGER_POINT_VENDOR"));
                setMID(obj.getString("MID"));
                setChargerPointSerialNumber(obj.getString("CHARGER_POINT_SERIAL_NUMBER"));
                setFirmwareVersion(obj.getString("FIRMWARE_VERSION"));
                setImsi(obj.getString("IMSI"));
                setTestPrice(obj.getString("TEST_PRICE"));
                setTargetSoc(obj.getInt("TARGET_SOC"));
                setM2mTel(obj.getString("M2M_TEL"));
                setAuthMode(obj.getString("AUTH_MODE"));
                setSelectPayment(obj.getString("SELECT_PAYMENT"));
                setStopConfirm(obj.getBoolean("STOP_CONFIRM"));
                setSigned(obj.getBoolean("SIGNED"));
            }
        } catch (Exception e) {
            logger.error("configuration load fail : {}", e.getMessage());
        }
    }

    public void onSaveConfiguration() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("CHARGER_TYPE", getChargerType());
            obj.put("CHARGER_POINT_MODEL_CODE", getChargerPointModelCode());
            obj.put("CHARGER_ID", getChargerId());
            obj.put("SERVER_CONNECTING_STRING", getServerConnectingString());
            obj.put("SERVER_PORT", getServerPort());
            obj.put("CONTROL_COM", getControlCom());
            obj.put("RF_COM", getRfCom());
            obj.put("PLC_COM", getPlcCom());
            obj.put("DUTY", getDuty());
            obj.put("CHARGER_POINT_VENDOR", getChargerPointVendor());
            obj.put("MID", getMID());
            obj.put("CHARGER_POINT_SERIAL_NUMBER", getChargerPointSerialNumber());
            obj.put("FIRMWARE_VERSION", getFirmwareVersion());
            obj.put("IMSI", getImsi());
            obj.put("TEST_PRICE", getTestPrice());
            obj.put("TARGET_SOC", getTargetSoc());
            obj.put("M2M_TEL", getM2mTel());

            obj.put("AUTH_MODE", getAuthMode());
            obj.put("SELECT_PAYMENT", getSelectPayment());

            obj.put("STOP_CONFIRM", isStopConfirm());
            obj.put("SIGNED", isSigned());
            fileManagement.stringToFileSave(GlobalVariables.ROOT_PATH, CONFIG_FILE_NAME, obj.toString(), false);
        } catch (Exception e) {
            logger.error("configuration save fail :  {}", e.getMessage());
        }
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getMID() {
        return MID;
    }

    public void setMID(String MID) {
        this.MID = MID;
    }

    public String getChargerId() {
        return chargerId;
    }

    public void setChargerId(String chargerId) {
        this.chargerId = chargerId;
    }

    public int getMaxChannel() {
        return maxChannel;
    }

    public void setMaxChannel(int maxChannel) {
        this.maxChannel = maxChannel;
    }

    public String getServerConnectingString() {
        return serverConnectingString;
    }

    public void setServerConnectingString(String serverConnectingString) {
        this.serverConnectingString = serverConnectingString;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getChargerType() {
        return chargerType;
    }

    public void setChargerType(int chargerType) {
        this.chargerType = chargerType;
    }

    public String getSelectPayment() {
        return selectPayment;
    }

    public void setSelectPayment(String selectPayment) {
        this.selectPayment = selectPayment;
    }

    public int getSelectPaymentId() {
        return selectPaymentId;
    }

    public void setSelectPaymentId(int selectPaymentId) {
        this.selectPaymentId = selectPaymentId;
    }

    public String getAuthMode() {
        return authMode;
    }

    public void setAuthMode(String authMode) {
        this.authMode = authMode;
    }

    public int getAuthModeId() {
        return authModeId;
    }

    public void setAuthModeId(int authModeId) {
        this.authModeId = authModeId;
    }

    public String getControlCom() {
        return controlCom;
    }

    public void setControlCom(String controlCom) {
        this.controlCom = controlCom;
    }

    public String getRfCom() {
        return rfCom;
    }

    public void setRfCom(String rfCom) {
        this.rfCom = rfCom;
    }

    public String getPlcCom() {
        return PlcCom;
    }

    public void setPlcCom(String plcCom) {
        PlcCom = plcCom;
    }

    public String getTestPrice() {
        return testPrice;
    }

    public void setTestPrice(String testPrice) {
        this.testPrice = testPrice;
    }

    public int getDr() {
        return dr;
    }

    public void setDr(int dr) {
        this.dr = dr;
    }

    public int getDrCode() {
        return drCode;
    }

    public void setDrCode(int drCode) {
        this.drCode = drCode;
    }

    public String getChargerPointVendor() {
        return chargerPointVendor;
    }

    public void setChargerPointVendor(String chargerPointVendor) {
        this.chargerPointVendor = chargerPointVendor;
    }

    public String getChargerPointModel() {
        return chargerPointModel;
    }

    public void setChargerPointModel(String chargerPointModel) {
        this.chargerPointModel = chargerPointModel;
    }

    public int getChargerPointModelCode() {
        return chargerPointModelCode;
    }

    public void setChargerPointModelCode(int chargerPointModelCode) {
        this.chargerPointModelCode = chargerPointModelCode;
    }

    public String getUnitPriceVendorCode() {
        return unitPriceVendorCode;
    }

    public void setUnitPriceVendorCode(String unitPriceVendorCode) {
        this.unitPriceVendorCode = unitPriceVendorCode;
    }

    public String getChargerPointSerialNumber() {
        return chargerPointSerialNumber;
    }

    public void setChargerPointSerialNumber(String chargerPointSerialNumber) {
        this.chargerPointSerialNumber = chargerPointSerialNumber;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getGpsX() {
        return gpsX;
    }

    public void setGpsX(String gpsX) {
        this.gpsX = gpsX;
    }

    public String getGpsY() {
        return gpsY;
    }

    public void setGpsY(String gpsY) {
        this.gpsY = gpsY;
    }

    public String getM2mTel() {
        return m2mTel;
    }

    public void setM2mTel(String m2mTel) {
        this.m2mTel = m2mTel;
    }

    public String getRfMaker() {
        return rfMaker;
    }

    public void setRfMaker(String rfMaker) {
        this.rfMaker = rfMaker;
    }

    public int getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(int statusTime) {
        this.statusTime = statusTime;
    }

    public int getStatusNotificationDelay() {
        return statusNotificationDelay;
    }

    public void setStatusNotificationDelay(int statusNotificationDelay) {
        this.statusNotificationDelay = statusNotificationDelay;
    }
    public int getTargetChargingTime() {
        return targetChargingTime;
    }

    public void setTargetChargingTime(int targetChargingTime) {
        this.targetChargingTime = targetChargingTime;
    }

    public boolean isStopConfirm() {
        return StopConfirm;
    }

    public void setStopConfirm(boolean stopConfirm) {
        StopConfirm = stopConfirm;
    }

    public boolean isSigned() {
        return signed;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
    }

    public boolean isChargerPowerType() {
        return chargerPowerType;
    }

    public void setChargerPowerType(boolean chargerPowerType) {
        this.chargerPowerType = chargerPowerType;
    }

    public boolean isUsedPLC() {
        return usedPLC;
    }

    public void setUsedPLC(boolean usedPLC) {
        this.usedPLC = usedPLC;
    }

    public FirmwareStatus getFirmwareStatus() {
        return firmwareStatus;
    }

    public void setFirmwareStatus(FirmwareStatus firmwareStatus) {
        this.firmwareStatus = firmwareStatus;
    }

    public DiagnosticsStatus getDiagnosticsStatus() {
        return diagnosticsStatus;
    }

    public void setDiagnosticsStatus(DiagnosticsStatus diagnosticsStatus) {
        this.diagnosticsStatus = diagnosticsStatus;
    }

    public UploadLogStatus getUploadLogStatus() {
        return uploadLogStatus;
    }

    public void setUploadLogStatus(UploadLogStatus uploadLogStatus) {
        this.uploadLogStatus = uploadLogStatus;
    }

    public SignedFirmwareStatus getSignedFirmwareStatus() {
        return signedFirmwareStatus;
    }

    public void setSignedFirmwareStatus(SignedFirmwareStatus signedFirmwareStatus) {
        this.signedFirmwareStatus = signedFirmwareStatus;
    }

    public int getConfigCnt() {
        return configCnt;
    }

    public void setConfigCnt(int configCnt) {
        this.configCnt = configCnt;
    }


    /**
     * Duty 50%, 25%
     */
    public int duty = 50;

    public int getDuty() {
        return duty;
    }

    public void setDuty(int duty) {
        this.duty = duty;
    }

    public int getTargetSoc() {
        return targetSoc;
    }

    public void setTargetSoc(int targetSoc) {
        this.targetSoc = targetSoc;
    }
}
