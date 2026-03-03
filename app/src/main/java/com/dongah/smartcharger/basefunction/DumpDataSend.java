package com.dongah.smartcharger.basefunction;

import android.os.Handler;
import android.os.Looper;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.websocket.ocpp.common.JSONCommunicator;
import com.dongah.smartcharger.websocket.ocpp.common.model.Message;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class DumpDataSend extends JSONCommunicator {

    private static final Logger logger = LoggerFactory.getLogger(DumpDataSend.class);

    Handler handler = new Handler(Looper.getMainLooper());

    static class DumpTransaction {
        String startLine;
        ArrayList<String> bodyLines = new ArrayList<>();
        String stopLine;
    }

    Queue<DumpTransaction> dumpQueue = new LinkedList<>();
    DumpTransaction currentTx = null;
    MainActivity activity;

    public void onDumpSend() {
        activity = (MainActivity) MainActivity.mContext;

        String path = GlobalVariables.getRootPath() + File.separator + "dump" + File.separator + "dump";
        File file = new File(path);
        if (!file.exists()) {
            GlobalVariables.setDumpSending(false);
            return;
        }

        dumpQueue.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;
            DumpTransaction tx = null;
            while ((line = br.readLine()) != null) {
                Message msg = parse(line);
                if (Objects.equals(msg.getAction(), "StartTransaction")) {
                    tx = new DumpTransaction();
                    tx.startLine = line;
                } else if (Objects.equals(msg.getAction(), "StopTransaction")) {
                    if (tx != null) {
                        tx.stopLine = line;
                        dumpQueue.add(tx);
                        tx = null;
                    }
                } else {
                    if (tx != null) {
                        tx.bodyLines.add(line);
                    }
                }
            }
//            file.delete();
        } catch (IOException e) {
            logger.error("Dump load error", e);
            return;
        }
        sendNextTransaction();
    }

    private void sendNextTransaction() {
        if (dumpQueue.isEmpty()) {
            GlobalVariables.setDumpSending(false);
            stopTask();
            return;
        }

        currentTx = dumpQueue.poll();

        logger.info("Dump StartTransaction send");
        activity.getSocketReceiveMessage().onSend(currentTx.startLine);
    }

    /** StartTransaction 응답에서 transactionId 받으면 호출  */
    public void onReceiveStartTransactionConf(int transactionId) {
        GlobalVariables.dumpTransactionId = transactionId;
        sendMeterValues();
    }

    private void sendMeterValues() {
        handler.postDelayed(new Runnable() {
            int index = 0;
            @Override
            public void run() {
                if (index < currentTx.bodyLines.size()) {
                    String line = currentTx.bodyLines.get(index);
                    Message msg = parse(line);

                    JsonArray arr = JsonParser.parseString(line).getAsJsonArray();

                    if (Objects.equals(msg.getAction(), "MeterValues")) {
                        arr.get(3).getAsJsonObject()
                                .addProperty("transactionId", GlobalVariables.dumpTransactionId);
                        activity.getSocketReceiveMessage().onSend(arr.toString());
                        index++;
                        handler.postDelayed(this, 500);
                    }
                } else {
                    sendStopTransaction();
                }
            }
        }, 500);
    }

    private void sendStopTransaction() {
        JsonArray arr = JsonParser.parseString(currentTx.stopLine).getAsJsonArray();
        arr.get(3).getAsJsonObject()
                .addProperty("transactionId", GlobalVariables.dumpTransactionId);

        logger.info("Dump StopTransaction send");
        activity.getSocketReceiveMessage().onSend(arr.toString());
        handler.postDelayed(this::sendNextTransaction, 1000);

    }

    public void stopTask() {
        handler.removeCallbacksAndMessages(null);
    }

}
