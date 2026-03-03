package com.dongah.smartcharger.websocket.socket.handler.handlersend;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.ClassUiProcess;
import com.dongah.smartcharger.basefunction.UiSeq;
import com.dongah.smartcharger.utils.LogDataSave;
import com.dongah.smartcharger.websocket.ocpp.common.OccurenceConstraintException;
import com.dongah.smartcharger.websocket.ocpp.core.HeartbeatRequest;
import com.dongah.smartcharger.websocket.socket.SocketReceiveMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartbeatThread {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatThread.class);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final int intervalSec;
    private boolean isRunning = false;

    private MainActivity activity;
    private SocketReceiveMessage socketReceiveMessage;
    private final HeartbeatRequest heartbeatRequest = new HeartbeatRequest();
    private final LogDataSave logDataSave = new LogDataSave("log");

    public HeartbeatThread(int intervalSec) {
        this.intervalSec = intervalSec;

        activity = (MainActivity) MainActivity.mContext;
        if (activity != null) {
            socketReceiveMessage = activity.getSocketReceiveMessage();
        }
    }

    public void start() {
        isRunning = true;
        handler.post(heartbeatRunnable);
    }

    public void stop() {
        isRunning = false;
        handler.removeCallbacks(heartbeatRunnable);
    }

    private final Runnable heartbeatRunnable = new Runnable() {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            if (!isRunning) return;

            try {
                sendHeartbeatIfPossible();
            } catch (Exception e) {
                logger.error("HeartbeatThread error : {}", e.getMessage());
            }
            // 다음 Heartbeat 예약
            handler.postDelayed(this, intervalSec * 1000L);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendHeartbeatIfPossible() throws OccurenceConstraintException {
        if (activity == null) return;

        ClassUiProcess classUiProcess = activity.getClassUiProcess();
        // 충전 중이면 전송 안 함
        if (classUiProcess.getUiSeq() == UiSeq.CHARGING) return;
        socketReceiveMessage.onSend(
                100,
                heartbeatRequest.getActionName(),
                heartbeatRequest
        );

        // 30일 이상 로그 삭제
        logDataSave.removeLogData();
    }
}
