package com.dongah.smartcharger.websocket.socket.handler.handlersend;

import android.os.Build;

import androidx.annotation.RequiresApi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusNotificationThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(StatusNotificationThread.class);

    private volatile boolean stopped = false;
    private int count = 0;

    // 분 단위
    private static final int NORMAL_INTERVAL_MIN = 60;
    private static final int FAULT_INTERVAL_MIN = 30;
    private int connectorId;
    private volatile boolean isFault = false; // 현재 상태

    StatusNotificationReq statusNotificationReq;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public StatusNotificationThread() {
        statusNotificationReq = new StatusNotificationReq();
        statusNotificationReq.sendStatusNotification(0);
    }

    public void setFault(boolean fault, int connectorId) {
        this.isFault = fault;
        this.connectorId = connectorId;
        logger.info("Status state changed. isFault={}", fault);
    }


    public void stopThread() {
        stopped = true;
        interrupt();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        logger.info("StatusNotificationThread start");
        while (!stopped && !isInterrupted()) {
            try {
                Thread.sleep(1000);
                count++;

                int intervalSec = (isFault ? FAULT_INTERVAL_MIN : NORMAL_INTERVAL_MIN) * 60;

                if (count >= intervalSec) {
                    count = 0;
                    statusNotificationReq.sendStatusNotification(isFault ? connectorId : 0);
                }
            } catch (Exception e) {
                logger.error("StatusNotificationThread error : {}", e.getMessage());
            }
        }
        logger.info("StatusNotificationThread terminated");
    }
}
