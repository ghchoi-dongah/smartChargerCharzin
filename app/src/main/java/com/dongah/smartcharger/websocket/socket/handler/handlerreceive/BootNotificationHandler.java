package com.dongah.smartcharger.websocket.socket.handler.handlerreceive;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.DumpDataSend;
import com.dongah.smartcharger.basefunction.GlobalVariables;
import com.dongah.smartcharger.websocket.ocpp.core.RegistrationStatus;
import com.dongah.smartcharger.websocket.socket.OcppHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlersend.HeartbeatThread;
import com.dongah.smartcharger.websocket.socket.handler.handlersend.StatusNotificationReq;

import org.json.JSONObject;

import java.util.Objects;

public class BootNotificationHandler implements OcppHandler {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {

        RegistrationStatus status = RegistrationStatus.valueOf(payload.getString("status"));
        int interval = payload.getInt("interval");

        MainActivity activity = (MainActivity) MainActivity.mContext;
        if (Objects.equals(RegistrationStatus.Accepted, status)) {
            //  BootNotificationThread 종료
            if (activity.getSocketReceiveMessage().getSocket().getBootNotificationThread() != null) {
                activity.getSocketReceiveMessage()
                        .getSocket()
                        .getBootNotificationThread()
                        .stopThread();
            }

            //status notification
            StatusNotificationReq statusNotificationReq = new StatusNotificationReq();
            statusNotificationReq.sendStatusNotification(0);

            //Heart Beat
            int delay = GlobalVariables.getHeartBeatInterval();
            HeartbeatThread heartbeatThread = new HeartbeatThread(interval);
            heartbeatThread.start();

            //dump data
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                GlobalVariables.setDumpSending(true);
                DumpDataSend dumpDataSend = new DumpDataSend();
                dumpDataSend.onDumpSend();
            }, 8000);

        } else if ((Objects.equals(status, RegistrationStatus.Rejected)) ||
                (Objects.equals(status, RegistrationStatus.Pending))) {
            activity.getSocketReceiveMessage().getSocket().getBootNotificationThread().start();
            GlobalVariables.setReconnectCheck(false);
        }
    }
}
