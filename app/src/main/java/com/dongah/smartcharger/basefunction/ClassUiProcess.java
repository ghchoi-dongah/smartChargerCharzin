package com.dongah.smartcharger.basefunction;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.R;
import com.dongah.smartcharger.controlboard.ControlBoard;
import com.dongah.smartcharger.controlboard.RxData;
import com.dongah.smartcharger.controlboard.TxData;
import com.dongah.smartcharger.pages.FaultFragment;
import com.dongah.smartcharger.rfcard.RfCardReaderListener;
import com.dongah.smartcharger.rfcard.RfCardReaderReceive;
import com.dongah.smartcharger.utils.FileManagement;
import com.dongah.smartcharger.websocket.ocpp.core.ChargePointErrorCode;
import com.dongah.smartcharger.websocket.ocpp.core.ChargePointStatus;
import com.dongah.smartcharger.websocket.ocpp.core.Reason;
import com.dongah.smartcharger.websocket.ocpp.core.ResetType;
import com.dongah.smartcharger.websocket.ocpp.utilities.ZonedDateTimeConvert;
import com.dongah.smartcharger.websocket.socket.SocketReceiveMessage;
import com.dongah.smartcharger.websocket.socket.SocketState;
import com.dongah.smartcharger.websocket.socket.handler.handlersend.MeterValuesReq;
import com.dongah.smartcharger.websocket.socket.handler.handlersend.StartTransactionReq;
import com.dongah.smartcharger.websocket.socket.handler.handlersend.StatusNotificationReq;
import com.dongah.smartcharger.websocket.socket.handler.handlersend.StopTransactionReq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Timer;

public class ClassUiProcess implements RfCardReaderListener {

    private static final Logger logger = LoggerFactory.getLogger(ClassUiProcess.class);

    @SuppressLint("SimpleDateFormat")
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    int connectorId;
    UiSeq uiSeq,oSeq;;
    ZonedDateTimeConvert zonedDateTimeConvert;

    ChargerConfiguration chargerConfiguration;
    ChargingCurrentData chargingCurrentData;
    FragmentChange fragmentChange;
    ControlBoard controlBoard;
    NotifyFaultCheck notifyFaultCheck;
    SocketReceiveMessage socketReceiveMessage;
    Timer eventTimer;

    private Handler eventHandler;
    private Runnable eventRunnable;

    RfCardReaderReceive rfCardReaderReceive;

    MeterValuesReq meterValuesReq;

    double powerUnitPrice = 0f;
    int powerMeterCheck = 0;
    ZonedDateTime startTime;

    /** OCPP     */
    StatusNotificationReq statusNotificationReq;




    public UiSeq getUiSeq() {
        return uiSeq;
    }

    public void setUiSeq(UiSeq uiSeq) {
        this.uiSeq = uiSeq;
    }

    public UiSeq getoSeq() {
        return oSeq;
    }

    public void setoSeq(UiSeq oSeq) {
        this.oSeq = oSeq;
    }

    public int getPowerMeterCheck() {
        return powerMeterCheck;
    }

    public void setPowerMeterCheck(int powerMeterCheck) {
        this.powerMeterCheck = powerMeterCheck;
    }

    public ChargingCurrentData getChargingCurrentData() {
        return chargingCurrentData;
    }

    public ClassUiProcess(int connectorId) {
        try {
            this.connectorId = connectorId;
            setUiSeq(UiSeq.INIT);
            zonedDateTimeConvert = new ZonedDateTimeConvert();
            //rf card
            rfCardReaderReceive = ((MainActivity) MainActivity.mContext).getRfCardReaderReceive();
            rfCardReaderReceive.setRfCardReaderListener(this);
            // configuration
             chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
            //charging current data
            chargingCurrentData = new ChargingCurrentData();
            chargingCurrentData.onCurrentDataClear();
            //socket receive message
            socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
            //fragment change
            fragmentChange = ((MainActivity) MainActivity.mContext).getFragmentChange();
            //control board
            controlBoard = ((MainActivity) MainActivity.mContext).getControlBoard();
            chargingCurrentData.setChargePointStatus(controlBoard.getRxData().isCsPilot() ? ChargePointStatus.Preparing : ChargePointStatus.Available);
            // alarm check
            notifyFaultCheck = new NotifyFaultCheck(connectorId);
            // Meter Value
            meterValuesReq = new MeterValuesReq(connectorId);
            //
            statusNotificationReq = new StatusNotificationReq();

            //loop
            startEventLoop();

//            eventTimer = new Timer();
//            eventTimer.schedule(new TimerTask() {
//                @RequiresApi(api = Build.VERSION_CODES.O)
//                @Override
//                public void run() {
//                    onEventAction();
//                }
//            }, 3000, 800);

        } catch (Exception e) {
            logger.error( "ClassUiProcess create error : {}", e.getMessage());
        }
    }


    /**
     * charging sequence loop
     * server data send : 서버와 연결이 안된 경우 ProcessHandler dump data save
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void onEventAction() {
        try {
            RxData rxData = controlBoard.getRxData();
            TxData txData = controlBoard.getTxData();
            //현재 전력량 값
            chargingCurrentData.setIntegratedPower(rxData.getActiveEnergy());
            // fault check
            if (getUiSeq().getValue() < 17) {
                onFaultCheck(rxData);
            }

            switch (getUiSeq()) {
                case NONE:
                case INIT:
                    handleInit(txData);
                    break;

                case REBOOTING:
                    handleRebooting();
                    break;

                case MEMBER_CARD:
                case MEMBER_CARD_WAIT:
                case CREDIT_CARD:
                case CREDIT_CARD_WAIT:
                    break;

                case PLUG_CHECK:
                    handlePlugCheck(rxData, txData);
                    break;

                case CONNECT_CHECK:
                    handleConnectCheck(rxData, txData);
                    break;

                case CHARGING:
                    handleCharging(rxData, txData);
                    break;

                case FINISH_WAIT:
                    handleFinishWait(rxData, txData);
                    break;

                case FINISH:
                    onFinish();
                    break;

                case FAULT:
                    handleFault(rxData, txData);
                    break;
            }
        } catch (Exception e) {
            logger.error(" onEventAction error : {}", e.getMessage());
        }
    }


    public void onHome() {
        setUiSeq(UiSeq.INIT);
        fragmentChange.onFragmentChange(UiSeq.INIT,"INIT",null);
        rfCardReaderReceive.rfCardReadRelease();
    }

    private void onFinish() {
        //충전 완료
        if (chargingCurrentData.isReBoot()) {
            setUiSeq(UiSeq.INIT);
        }
        // 원격 충전인 경우 ExtendedRemoteStart
        if (chargingCurrentData.isExtendedRemoteStart()) {
            chargingCurrentData.setExtendedRemoteStart(false);
        }
    }


    public void onStopData() {
        try {
            meterValuesReq.stopMeterValues();
        } catch (Exception e) {
            logger.error(" onStop Data {}", e.getMessage());
        }
    }


    /**
     * 충전 사용량 계산
     *
     * @param rxData power meter raw data pick
     */
    private void onUsePowerMeter(RxData rxData) {
        try {
            long gapPower;
            double gapPay;
            if (rxData.getActiveEnergy() > 0) {
                //current power meter --> chargingCurrentData .powerKwh
                //전력량 변화 여부 체크
                gapPower = rxData.getActiveEnergy() - chargingCurrentData.getPowerMeterCalculate();
                gapPower = (gapPower <= 0) ? 0 : (gapPower > 100) ? 1 : gapPower;
                //전력량 변화 여부 체크 892 = 8.92kW
                powerMeterCheck = gapPower == 0 ? powerMeterCheck + 1 : 0;
                chargingCurrentData.setPowerMeterUse(chargingCurrentData.getPowerMeterUse() + gapPower);
                gapPay = gapPower * 0.01 * powerUnitPrice;

                chargingCurrentData.setPowerMeterUsePay(chargingCurrentData.getPowerMeterUsePay() + gapPay);
                chargingCurrentData.setPowerMeterCalculate(rxData.getActiveEnergy());
            }
            chargingCurrentData.setOutPutCurrent(rxData.getCurrent());  //출력전류
            chargingCurrentData.setOutPutVoltage(rxData.getVoltage());  //출력전압
            chargingCurrentData.setPowerMeter(rxData.getActiveEnergy());  //전력량
            chargingCurrentData.setFrequency(rxData.getFrequency() * 0.01);    //주파수
//            chargingCurrentData.setChargingRemainTime(rxData.getRemainTime());  //충전 남은 시간
//            chargingCurrentData.setSoc(rxData.getSoc());
        } catch (Exception e) {
            logger.error("power meter calculate error : {}", e.getMessage());
        }
    }

    /**
     * 현재 Fragment 찾기
     *
     * @return fragment;
     */
    private Fragment getCurrentFragment() {
        return ((MainActivity) MainActivity.mContext).getSupportFragmentManager().findFragmentById(R.id.frameBody);
    }


    /**
     * Remote Transaction stop
     */
    public void onRemoteTransactionStop(Reason reason) {
        try {
            UiSeq uiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess().getUiSeq();
            if (Objects.equals(uiSeq, UiSeq.CHARGING)) {
                controlBoard = ((MainActivity) MainActivity.mContext).getControlBoard();
                chargingCurrentData = ((MainActivity) MainActivity.mContext).getClassUiProcess().getChargingCurrentData();

                controlBoard.getTxData().setMainMC(false);
                controlBoard.getTxData().setPwmDuty((short) 100);

                chargingCurrentData.setUserStop(false);
                chargingCurrentData.setStopReason(reason);
            }
        } catch (Exception e) {
            logger.error("remote stop error : {} ", e.getMessage());
        }
    }

    public void onResetStop(ResetType resetType) {
        try {
            UiSeq uiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess().getUiSeq();
            if (Objects.equals(uiSeq, UiSeq.CHARGING)) {
                controlBoard.getTxData().setMainMC(false);
                controlBoard.getTxData().setPwmDuty((short) 100);
                chargingCurrentData.setUserStop(false);
                chargingCurrentData.setStopReason(resetType == ResetType.Hard ? Reason.HardReset : Reason.SoftReset);
                setUiSeq(UiSeq.FINISH_WAIT);
            }
        } catch (Exception e) {
            logger.error("reset stop error : {} ", e.getMessage());
        }
    }

    private boolean onRebootCheck() {
        boolean result = false;
        try {
            UiSeq uiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess().getUiSeq();
            result = Objects.equals(UiSeq.REBOOTING, uiSeq) || Objects.equals(UiSeq.INIT, uiSeq);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return result;
    }

    public void onStop() {
        try {
            UiSeq uiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess().getUiSeq();
            if (Objects.equals(uiSeq, UiSeq.CHARGING)) {
                controlBoard = ((MainActivity) MainActivity.mContext).getControlBoard();
                //충전기 정지
                controlBoard.getTxData().setMainMC(false);
                controlBoard.getTxData().setPwmDuty((short) 100);
                chargerConfiguration =((MainActivity) MainActivity.mContext).getChargerConfiguration();
            }
        } catch (Exception e) {
            logger.error(" onStop error : {}", e.getMessage());
        }
    }


    // charger point error check
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void onFaultCheck(RxData rxData) {
        try {
            //충전중 일 때 fault 가 발생한 경우
            if (!controlBoard.isConnected() || rxData.csFault) {
                if (Objects.equals(getUiSeq(), UiSeq.CHARGING)) {
                    controlBoard.getTxData().setMainMC(false);
                    controlBoard.getTxData().setPwmDuty((short) 100);

                    chargingCurrentData.setChargePointStatus(ChargePointStatus.Faulted);
                    chargingCurrentData.setChargePointErrorCode(ChargePointErrorCode.OtherError);
                    //비회원 충전 요금 단가 조정을 한다.
                    if (Objects.equals(chargingCurrentData.getPaymentType().value(), 2) &&
                            chargingCurrentData.getPrePayment() <= chargingCurrentData.getPowerMeterUsePay()) {
                        chargingCurrentData.setPowerMeterUsePay(chargingCurrentData.getPrePayment());
                    }
                }
                // fault 발생하기 전에 충전 스퀀스 저장
                if (getUiSeq() != UiSeq.FAULT) setoSeq(getUiSeq());
                setUiSeq(UiSeq.FAULT);
            }
            notifyFaultCheck.onErrorMessageMake(rxData);
        } catch (Exception e) {
            logger.error("onFaultCheck error.... : {}", e.toString());
        }
    }

    /**
     * Rf CARD reader
     * @param cardNum card number
     * @param value boolean
     */
    @Override
    public void onRfCardDataReceive(String cardNum, boolean value) {
        try {
            if (cardNum.isEmpty() || Objects.equals(cardNum,"0000000000000000")) {
                setUiSeq(UiSeq.INIT);
                fragmentChange.onFragmentChange(UiSeq.INIT,null,null);
                ((MainActivity) MainActivity.mContext).runOnUiThread(() -> {
                    Toast.makeText(
                            ((MainActivity) MainActivity.mContext),
                            "카드 리더기에서 응답이 없습니다.",
                            Toast.LENGTH_SHORT
                    ).show();
                });
            } else {
                onRfCardDataReceiveEvent(cardNum, true);
            }
        } catch (Exception e) {
            logger.error("onRfCardDataReceive error : {} ", e.getMessage());
        }
    }

    private void onRfCardDataReceiveEvent(String cardNum, boolean b) {
        if (b) {
            try {
                if (b) {
                    //
                    if (Objects.equals(cardNum,"0000000000000000")) {
                        rfCardReaderReceive.rfCardReadRequest();
                    } else if (!cardNum.isEmpty()) {
                        if (GlobalVariables.isMemberResister()) {
                            // 저장
                            FileManagement fileManagement = new FileManagement();
                            fileManagement.saveIdTagIfNotExists(cardNum);
                            GlobalVariables.setMemberResister(false);
                            ((MainActivity) MainActivity.mContext).runOnUiThread(() -> {
                                Toast.makeText(
                                        ((MainActivity) MainActivity.mContext),
                                        "회원카드 등록 완료!.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            });
                        } else {
                            //서버에 회원카드 정보를 보내여 인증을 취득하면 전역변수에 저장한다.
                            setUiSeq(UiSeq.MEMBER_CARD_WAIT);
                            chargingCurrentData.setIdTag(cardNum);
                            fragmentChange.onFragmentChange(UiSeq.MEMBER_CARD_WAIT,null,null);
                        }
                        rfCardReaderReceive.rfCardReadRelease();
                    }
                }
            } catch (Exception e) {
                logger.error("onRfCardDataReceiveEvent error : {} ", e.getMessage());
            }
        }
    }

    private void startEventLoop() {
        eventHandler = new Handler(Looper.getMainLooper());
        eventRunnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                onEventAction();
                eventHandler.postDelayed(this, 800);
            }
        };
        eventHandler.postDelayed(eventRunnable, 3000);
    }

    public void stopEventLoop() {
        if (eventHandler != null && eventRunnable != null) {
            eventHandler.removeCallbacks(eventRunnable);
        }
    }

    //init
    private void handleInit(TxData txData) {
        setoSeq(UiSeq.INIT);
        setPowerMeterCheck(0);
        // meter values stop
        meterValuesReq.stopMeterValues();
        txData.setPwmDuty((short) 100);
        txData.setUiSequence((short) 2);
        if (chargingCurrentData.isReBoot() && onRebootCheck()) {
            setUiSeq(UiSeq.REBOOTING);
        }
        chargingCurrentData.setUserStop(false);
    }

    //Rebooting
    private void handleRebooting() {
        if (!(getCurrentFragment() instanceof FaultFragment)) {
            fragmentChange.onFragmentChange(
                    UiSeq.REBOOTING,
                    "REBOOTING",
                    chargingCurrentData.getStopReason() == Reason.HardReset ? "Hard" : "Soft"
            );
        }
    }

    //plug check
    private void handlePlugCheck(RxData rxData, TxData txData) {
        if (rxData.isCsPilot()) {
            txData.setPwmDuty((short) chargerConfiguration.getDuty());
            setUiSeq(UiSeq.CONNECT_CHECK);
        }
    }

    //connect check
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleConnectCheck(RxData rxData, TxData txData) {
        if (Objects.equals(rxData.getCsCPStatus(), (short) 2)) {
            txData.setPwmDuty((short) chargerConfiguration.getDuty());
            txData.setMainMC(true);

            chargingCurrentData.setChargePointStatus(ChargePointStatus.Charging);

            powerUnitPrice = Objects.equals(chargerConfiguration.getAuthMode(), "0")
                    ? chargingCurrentData.getPowerUnitPrice()
                    : Double.parseDouble(chargerConfiguration.getTestPrice());

            chargingCurrentData.setPowerMeterStart(rxData.getActiveEnergy());
            chargingCurrentData.setPowerMeterCalculate(rxData.getActiveEnergy());
            chargingCurrentData.setChargingStartTime(zonedDateTimeConvert.getStringCurrentTimeZone());
            startTime = zonedDateTimeConvert.doZonedDateTimeToDatetime(
                    chargingCurrentData.getChargingStartTime()
            );

            if (Objects.equals(chargerConfiguration.getAuthMode(), "0")) {
                StartTransactionReq startTransactionReq = new StartTransactionReq(connectorId);
                startTransactionReq.sendStartTransactionReq(chargingCurrentData.getIdTag());

                new Handler(Looper.getMainLooper()).postDelayed(
                        () -> meterValuesReq.startMeterValues(),
                        10000
                );
            }

            setUiSeq(UiSeq.CHARGING);
            fragmentChange.onFragmentChange(UiSeq.CHARGING, "CHARGING", null);
        }
    }

    //charging
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleCharging(RxData rxData, TxData txData) {
        try {
            onUsePowerMeter(rxData);
            txData.setUiSequence((short) 2);

            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

            if (chargingCurrentData.isExtendedRemoteStart()) {
                checkExtendedStop(now);
            } else {
                checkNormalStop(rxData, now);
            }

        } catch (Exception e) {
            logger.error("charging error : {}", e.getMessage());
        }
    }

    //finish wait
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleFinishWait(RxData rxData, TxData txData) {
        try {
            txData.setUiSequence((short) 3);
            meterValuesReq.stopMeterValues();

            chargingCurrentData.setStopReason(
                    chargingCurrentData.isUserStop() ? Reason.Local : chargingCurrentData.getStopReason()
            );

            chargingCurrentData.setPowerMeterStop(rxData.getActiveEnergy());
            chargingCurrentData.setChargingEndTime(zonedDateTimeConvert.getStringCurrentTimeZone());

            if (Objects.equals(chargerConfiguration.getAuthMode(), "0")) {
                StopTransactionReq stopTransactionReq = new StopTransactionReq();
                stopTransactionReq.sendStopTransactionReq(connectorId);
            }

            setUiSeq(UiSeq.FINISH);
            fragmentChange.onFragmentChange(UiSeq.FINISH, "FINISH", null);

        } catch (Exception e) {
            logger.error("FINISH_WAIT error : {}", e.getMessage());
        }
    }

    //fault
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleFault(RxData rxData, TxData txData) {
        if (getUiSeq().getValue() < 15) {
            if (!(getCurrentFragment() instanceof FaultFragment)) {

                if (Objects.equals(chargerConfiguration.getAuthMode(), "0") &&
                        Objects.equals(getoSeq(), UiSeq.CHARGING)) {

                    meterValuesReq.stopMeterValues();

                    txData.setMainMC(false);
                    txData.setPwmDuty((short) 100);
                    chargingCurrentData.setUserStop(false);
                    chargingCurrentData.setPowerMeterStop(rxData.getActiveEnergy());
                    chargingCurrentData.setChargingEndTime(zonedDateTimeConvert.getStringCurrentTimeZone());
                    chargingCurrentData.setChargePointStatus(ChargePointStatus.Finishing);

                    SocketState state = socketReceiveMessage.getSocket().getState();
                    if (Objects.equals(state.getValue(), 7)) {
                        StopTransactionReq stopTransactionReq = new StopTransactionReq();
                        stopTransactionReq.sendStopTransactionReq(connectorId);
                        statusNotificationReq.sendStatusNotification(connectorId);
                    }
                }
                fragmentChange.onFragmentChange(UiSeq.FAULT, "FAULT", "1");
            }
        }

        if (controlBoard.isConnected() && !rxData.isCsFault()) {
            if (Objects.equals(getoSeq(), UiSeq.CHARGING)) {
                txData.setUiSequence((short) 3);
                chargingCurrentData.setChargePointStatus(ChargePointStatus.Finishing);
                chargingCurrentData.setChargePointErrorCode(ChargePointErrorCode.NoError);
                setUiSeq(UiSeq.FINISH);
                fragmentChange.onFragmentChange(UiSeq.FINISH, "FINISH", null);
            } else {
                if (Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Preparing)
                        && !rxData.isCsPilot()) {

                    chargingCurrentData.setChargePointStatus(ChargePointStatus.Available);

                    SocketState state = socketReceiveMessage.getSocket().getState();
                    if (Objects.equals(state.getValue(), 7)
                            && Objects.equals(chargerConfiguration.getAuthMode(), "0")) {
                        statusNotificationReq.sendStatusNotification(connectorId);
                    }
                }
                onHome();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkExtendedStop(ZonedDateTime now) {
        String unit = chargingCurrentData.getExtendedUnit();
        long limitValue = chargingCurrentData.getExtendedValue();

        if ("Wh".equalsIgnoreCase(unit)) {
            if (getPowerMeterCheck() >= limitValue) {
                goFinishWait();
            }
        } else if ("min".equalsIgnoreCase(unit)) {
            long elapsedMin = Duration.between(startTime, now).toMinutes();
            if (elapsedMin >= limitValue) {
                goFinishWait();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkNormalStop(RxData rxData, ZonedDateTime now) {
        long maxChargingTime = Duration.between(startTime, now).toMinutes();

        boolean maxCheck =
                GlobalVariables.getMaxChargingTime() <= maxChargingTime ||
                        GlobalVariables.getMaxChargingRate() <= rxData.getCsSoc();

        boolean stopCheck =
                rxData.isCsStop() ||
                        !rxData.isCsPilot() ||
                        chargingCurrentData.isUserStop() ||
                        getPowerMeterCheck() >= 60 ||
                        maxCheck;

        if (stopCheck) {
            onStop();

            if (!rxData.isCsPilot()) {
                chargingCurrentData.setStopReason(Reason.EVDisconnected);
                chargingCurrentData.setUnplugTime(zonedDateTimeConvert.doZonedDateTimeToString());
            } else if (rxData.isCsEmergency()) {
                chargingCurrentData.setStopReason(Reason.EmergencyStop);
            } else if (maxCheck) {
                chargingCurrentData.setUserStop(true);
            }

            goFinishWait();
        } else if (chargingCurrentData.isPrePaymentResult()
                && chargingCurrentData.getPrePayment() <= chargingCurrentData.getPowerMeterUsePay()) {

            chargingCurrentData.setPowerMeterUsePay(chargingCurrentData.getPrePayment());
            chargingCurrentData.setStopReason(Reason.Other);
            goFinishWait();
        }
    }


    private void goFinishWait() {
        chargingCurrentData.setChargePointStatus(ChargePointStatus.Finishing);
        chargingCurrentData.setUserStop(true);
        setUiSeq(UiSeq.FINISH_WAIT);
    }

}
