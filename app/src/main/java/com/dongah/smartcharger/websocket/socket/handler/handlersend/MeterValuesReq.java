package com.dongah.smartcharger.websocket.socket.handler.handlersend;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.ChargingCurrentData;
import com.dongah.smartcharger.basefunction.GlobalVariables;
import com.dongah.smartcharger.websocket.ocpp.core.ChargePointStatus;
import com.dongah.smartcharger.websocket.ocpp.core.MeterValue;
import com.dongah.smartcharger.websocket.ocpp.core.MeterValuesRequest;
import com.dongah.smartcharger.websocket.ocpp.core.SampledValue;
import com.dongah.smartcharger.websocket.ocpp.utilities.ZonedDateTimeConvert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MeterValuesReq {

    private static final Logger logger = LoggerFactory.getLogger(MeterValuesReq.class);

    private final int connectorId ;

    DecimalFormat voltageFormatter = new DecimalFormat("######0.00");
    DecimalFormat powerFormatter = new DecimalFormat("######0.00");

    /**  meter values handler */
    private final Handler meterHandler  = new Handler(Looper.getMainLooper());
    private Runnable meterRunnable;
    private boolean isMeterRunning = false;
    private int lastIntervalSec = -1;


    public int getConnectorId() {
        return connectorId;
    }

    public MeterValuesReq(int connectorId) {
        this.connectorId = connectorId;
    }

    /** MeterValues 시작 */
    public void startMeterValues() {

        int intervalSec = GlobalVariables.getMeterValueSampleInterval();
        /// 이미 실행 중이고 interval 같으면 무시
        if (isMeterRunning && intervalSec == lastIntervalSec) {
            return;
        }

        stopMeterValues(); // 기존 중지
        lastIntervalSec = intervalSec;
        isMeterRunning = true;

        meterRunnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                if (!isMeterRunning) return;
                try {
                    sendMeterValues();
                } catch (Exception e) {
                    logger.error(" meterRunnable error : {}", e.getMessage());
                }
                meterHandler.postDelayed(this, intervalSec * 1000L);
            }
        };
        meterHandler.post(meterRunnable); // 즉시 1회 실행
    }

    public void stopMeterValues() {
        isMeterRunning = false;
        if (meterRunnable != null) {
            meterHandler.removeCallbacks(meterRunnable);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendMeterValues() throws Exception {
        MainActivity activity = (MainActivity) MainActivity.mContext;
        if (activity == null) return;
        // 충전 상테기 아니면 중지
        ChargingCurrentData chargingCurrentData = activity.getClassUiProcess().getChargingCurrentData();
        if (!Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Charging)) return;

        ZonedDateTimeConvert zonedDateTimeConvert = new ZonedDateTimeConvert();
        // 1. sampledValueList
        List<SampledValue> sampledValueList = new ArrayList<>();
        sampledValueList.add(createSampledValueObj("Voltage",
                voltageFormatter.format(chargingCurrentData.getOutPutVoltage() * 0.01),
                "V"));
        sampledValueList.add(createSampledValueObj("Current.Import",
                powerFormatter.format(chargingCurrentData.getOutPutCurrent() * 0.001),
                "A"));
        sampledValueList.add(createSampledValueObj("Power.Active.Import",
                powerFormatter.format(chargingCurrentData.getOutPutCurrent() * 0.001 * chargingCurrentData.getOutPutVoltage() * 0.01),
                "W"));
        sampledValueList.add(createSampledValueObj("Energy.Active.Import.Register",
                powerFormatter.format(chargingCurrentData.getPowerMeter() * 10),
                "W"));

        // 2. SampledValue[] 변환
        SampledValue[] sampledValues =
                sampledValueList.toArray(new SampledValue[0]);

        // 3. MeterValue
        ZonedDateTime timestamp = zonedDateTimeConvert.doZonedDateTimeToDatetime();
        MeterValue meterValue = new MeterValue(
                timestamp,
                sampledValues
        );

        // 4. MeterValue[] 구성
        MeterValue[] meterValues = new MeterValue[] {meterValue};

        // 5. Request 생성
        MeterValuesRequest meterValuesRequest = new MeterValuesRequest(getConnectorId());
        meterValuesRequest.setTransactionId(chargingCurrentData.getTransactionId());
        meterValuesRequest.setMeterValue(meterValues);
        activity.getSocketReceiveMessage().onSend(
                getConnectorId(),
                meterValuesRequest.getActionName(),
                meterValuesRequest
        );
    }

    private SampledValue createSampledValueObj(String measurand, String value, String unit) {
        SampledValue sv = new SampledValue();
        sv.setContext("Sample.Periodic");
        sv.setMeasurand(measurand);
        sv.setValue(value);
        sv.setUnit(unit);
        return sv;
    }
}
