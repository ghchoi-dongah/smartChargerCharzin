package com.dongah.smartcharger.websocket.socket.handler.handlersend;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.ChargerConfiguration;
import com.dongah.smartcharger.basefunction.GlobalVariables;
import com.dongah.smartcharger.websocket.ocpp.common.OccurenceConstraintException;
import com.dongah.smartcharger.websocket.ocpp.core.BootNotificationRequest;
import com.dongah.smartcharger.websocket.ocpp.firmware.FirmwareStatus;
import com.dongah.smartcharger.websocket.ocpp.firmware.FirmwareStatusNotificationRequest;
import com.dongah.smartcharger.websocket.ocpp.security.SecurityEventNotificationRequest;
import com.dongah.smartcharger.websocket.ocpp.security.SignedFirmwareStatus;
import com.dongah.smartcharger.websocket.ocpp.security.SignedFirmwareStatusNotificationRequest;
import com.dongah.smartcharger.websocket.ocpp.utilities.ZonedDateTimeConvert;
import com.dongah.smartcharger.websocket.socket.SocketReceiveMessage;
import com.dongah.smartcharger.websocket.socket.SocketState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.ZonedDateTime;


public class BootNotificationThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(BootNotificationThread.class);

    private volatile boolean stopped = false;
    private final int delayTime;
    private int count = 0;

    private ChargerConfiguration chargerConfiguration;
    private SocketReceiveMessage socketReceiveMessage;

    public BootNotificationThread(int delayTime) {
        this.delayTime = delayTime;
    }

    public void stopThread() {
        stopped = true;
        interrupt(); // sleep 깨우기
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        logger.info("BootNotificationThread started");
        while (!stopped && !isInterrupted()) {
            try {
                Thread.sleep(1000);
                count++;
                if (count >= delayTime) {
                    count = 0;
                    processBootNotification();
                }
            } catch (InterruptedException e) {
                logger.info("BootNotificationThread interrupted");
                break;
            } catch (Exception e) {
                logger.error("BootNotificationThread error : {}", e.getMessage());
            }
        }
        logger.info("BootNotificationThread terminated");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void processBootNotification() throws OccurenceConstraintException {
        MainActivity activity = (MainActivity) MainActivity.mContext;
        if (activity == null) return;

        chargerConfiguration = activity.getChargerConfiguration();
        socketReceiveMessage = activity.getSocketReceiveMessage();

        handleFirmwareStatusFile();
        BootNotificationRequest bootNotificationRequest;
        if (socketReceiveMessage.getSocket().getState() == SocketState.OPEN) {
            bootNotificationRequest = new BootNotificationRequest(
                    chargerConfiguration.getChargerPointVendor(),
                    chargerConfiguration.getChargerPointModel());
            bootNotificationRequest.setFirmwareVersion(GlobalVariables.VERSION);
            bootNotificationRequest.setImsi(chargerConfiguration.getImsi());
//            bootNotificationRequest.setChargePointSerialNumber(chargerConfiguration.getChargerPointSerialNumber());     //c충전기시리얼
            bootNotificationRequest.setChargePointSerialNumber(chargerConfiguration.getChargerId());

            bootNotificationRequest.setIccid("");

            socketReceiveMessage.onSend(
                    100,
                    bootNotificationRequest.getActionName(),
                    bootNotificationRequest
            );
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleFirmwareStatusFile() {

        File firmwareFile = new File(GlobalVariables.getRootPath(), "FirmwareStatusNotification");

        if (!firmwareFile.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(firmwareFile))) {
            String line;

            while ((line = br.readLine()) != null) {
                processFirmwareLine(line);
            }

            boolean deleted = firmwareFile.delete();
        } catch (Exception e) {
            logger.error("FirmwareStatus file error", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void processFirmwareLine(String line) {
        try {
            String[] resultStatus = line.split("-");
            if ("SignedFirmware".equals(resultStatus[0])) {
                SignedFirmwareStatus status = SignedFirmwareStatus.valueOf(resultStatus[1]);

                if (status == SignedFirmwareStatus.Installed) {
                    ZonedDateTime timestamp = new ZonedDateTimeConvert().doZonedDateTimeToDatetime();
                    SecurityEventNotificationRequest req =
                            new SecurityEventNotificationRequest("FirmwareUpdated", timestamp);
                    socketReceiveMessage.onSend(100, req.getActionName(), req);
                }

                SignedFirmwareStatusNotificationRequest req =
                        new SignedFirmwareStatusNotificationRequest(status);
                req.setRequestId(Integer.parseInt(getSignedRequestId()));
                socketReceiveMessage.onSend(100, req.getActionName(), req);
                chargerConfiguration.setSignedFirmwareStatus(status);
            } else if ("Firmware".equals(resultStatus[0])) {

                FirmwareStatus status = FirmwareStatus.valueOf(resultStatus[1]);
                FirmwareStatusNotificationRequest req =
                        new FirmwareStatusNotificationRequest(status);
                socketReceiveMessage.onSend(100, req.getActionName(), req);
                chargerConfiguration.setFirmwareStatus(status);
            }


        } catch (Exception e) {
            logger.error("processFirmwareLine error: {}", line, e);
        }
    }

    private String getSignedRequestId() {

        File file = new File(GlobalVariables.getRootPath(), "SignedRequestId");

        if (!file.exists()) return "0";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            return br.readLine();
        } catch (Exception e) {
            logger.error("getSignedRequestId error", e);
        }
        return "0";
    }

    @Override
    public void interrupt() {
        super.interrupt();
    }
}
