package com.dongah.smartcharger.websocket.socket;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.FragmentChange;
import com.dongah.smartcharger.utils.FileManagement;
import com.dongah.smartcharger.utils.LogDataSave;
import com.dongah.smartcharger.websocket.ocpp.common.JSONCommunicator;
import com.dongah.smartcharger.websocket.ocpp.common.OccurenceConstraintException;
import com.dongah.smartcharger.websocket.ocpp.common.model.Confirmation;
import com.dongah.smartcharger.websocket.ocpp.common.model.Message;
import com.dongah.smartcharger.websocket.ocpp.common.model.Request;
import com.dongah.smartcharger.websocket.ocpp.utilities.Stopwatch;
import com.dongah.smartcharger.websocket.ocpp.utilities.ZonedDateTimeConvert;
import com.dongah.smartcharger.websocket.socket.handler.handlerreceive.AuthorizeHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlerreceive.BootNotificationHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlerreceive.ChangeAvailabilityHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlerreceive.ChangeConfigurationHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlerreceive.ExtendedRemoteStartTransactionHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlerreceive.FirmwareStatusNotificationHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlerreceive.GetConfigurationHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlerreceive.GetLocalListVersionHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlerreceive.HeartbeatHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlerreceive.RemoteStartTransactionHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlerreceive.RemoteStopTransactionHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlerreceive.ResetHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlerreceive.SendLocalListHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlerreceive.StartTransactionHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlerreceive.StatusNotificationHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlerreceive.StopTransactionHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlerreceive.UnpluggedTransactionHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlerreceive.UpdateFirmwareHandler;
import com.dongah.smartcharger.websocket.socket.handler.handlersend.StatusNotificationReq;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import okhttp3.WebSocket;

public class SocketReceiveMessage extends JSONCommunicator implements SocketInterface {

    private static final Logger logger = LoggerFactory.getLogger(SocketReceiveMessage.class);


    private final Map<String, OcppHandler> handlerMap = new HashMap<>();
    private final Map<String, OcppHandler> dataTransferHandlerMap = new HashMap<>();

    int connectorId = 0;
    /**
     * Okhttp3 web-socket
     */
    WebSocket webSocket = null;
    /**
     * user define socket blue-networks socket
     */
    Socket socket = null;
    String url;
    Message message = null;
    /**
     * message send/receive list (UUID,Action)
     */
    HashMap<String, String> hashMapUuid = null;
    HashMap<String, Object> newHashMapUuid = null;
    HashMap<Integer, Integer> getConnectorIdHashMap;

    /**
     * LogData save class
     */
    LogDataSave logDataSave = new LogDataSave("log");
    /**
     * dump data save actions
     */
    String[] actionNames = {"StartTransaction", "StopTransaction"};
    ArrayList<String> actionList = new ArrayList<>();
    LogDataSave logDataSaveDump = new LogDataSave("dump");
    StatusNotificationReq statusNotificationReq;


    FragmentChange fragmentChange;
    FileManagement fileManagement;
    ZonedDateTimeConvert zonedDateTimeConvert;
    /**
     * web socket listener register
     */
    SocketMessageListener socketMessageListener;


    /**
     * web socket debug listener register
     */
    private SocketMessageListener socketMessageDebugListener;

    public void setSocketMessageDebugListenerStop() {
        this.socketMessageDebugListener = null;
    }
    /**
     * socket getter
     */
    public Socket getSocket() {
        return socket;
    }



    /**
     * socket constructor
     */
    public SocketReceiveMessage(String url) {
        this.url = url;
        fragmentChange = ((MainActivity) MainActivity.mContext).getFragmentChange();
        zonedDateTimeConvert = new ZonedDateTimeConvert();
        fileManagement = new FileManagement();

        initHandlers(); // 핸들러 등록
        Collections.addAll(actionList, actionNames);
        onSocketInitialize();
    }

    public void onSocketInitialize() {
        try {

            if (socket != null) {
                socket.fullClose();   // 아래에서 정의
                socket = null;
            }
            // request  ==> UUID, ActionName  hashmap 저장
            // response ==> hashmap find uuid 삭제
            // (key:UUID, value:Action) ==> hashMap
            if (hashMapUuid != null) hashMapUuid = null;
            hashMapUuid = new HashMap<String, String>();
            if (newHashMapUuid != null) newHashMapUuid = null;
            newHashMapUuid = new HashMap<String, Object>();
            // connectorId to channel (remoteStart ==> remoteStop)
            if (getConnectorIdHashMap != null) getConnectorIdHashMap = null;
            getConnectorIdHashMap = new HashMap<Integer, Integer>();

            socket = new Socket(url);
            socket.setState(SocketState.OPENING);
            socket.getInstance(this);

        } catch (Exception e) {
            logger.error(" socket receive message  : {}", e.getMessage());
        }
    }



    private void initHandlers() {
        // 신규 기능 추가 시 핸들러 클래스만 만들어서 여기에 한 줄 추가하면 끝입니다.
        handlerMap.put("BootNotification", new BootNotificationHandler());
        handlerMap.put("Heartbeat", new HeartbeatHandler());

        handlerMap.put("GetLocalListVersion", new GetLocalListVersionHandler());
        handlerMap.put("SendLocalList", new SendLocalListHandler());
        handlerMap.put("ChangeConfiguration", new ChangeConfigurationHandler());
        handlerMap.put("StatusNotification", new StatusNotificationHandler());
        handlerMap.put("ChangeAvailability", new ChangeAvailabilityHandler());
        handlerMap.put("Authorize", new AuthorizeHandler());

        handlerMap.put("GetConfiguration", new GetConfigurationHandler());
        handlerMap.put("StartTransaction", new StartTransactionHandler());
        handlerMap.put("StopTransaction", new StopTransactionHandler());
        handlerMap.put("RemoteStartTransaction", new RemoteStartTransactionHandler());
        handlerMap.put("RemoteStopTransaction", new RemoteStopTransactionHandler());
        handlerMap.put("Reset", new ResetHandler());
        handlerMap.put("UpdateFirmware", new UpdateFirmwareHandler());
        handlerMap.put("FirmwareStatusNotification", new FirmwareStatusNotificationHandler());

        // DataTransfer messageId별 핸들러
        handlerMap.put("ExtendedRemoteStartTransaction", new ExtendedRemoteStartTransactionHandler());
        handlerMap.put("UnpluggedTransaction", new UnpluggedTransactionHandler());
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onOpen(WebSocket webSocket) {
        this.webSocket = webSocket;
        socket.setState(SocketState.OPEN);
//
//        //미전송 데이터 확인
//        try {
//            DumpDataSend dumpDataSend = new DumpDataSend();
//            dumpDataSend.onDumpSend();
//        } catch (Exception e) {
//            logger.error(" bootNotification Dump error : {}", e.getMessage());
//        }
//
//        if (!GlobalVariables.isConnectRetry()) {
//            //bootNotification
//            bootNotificationThread.start();
//        } else {
//            // heart Bit
//            statusNotificationReq.sendStatusNotification(0);
//        }
    }

    @Override
    public void onGetMessage(WebSocket webSocket, String text) throws JSONException {
        try {
            message = parse(text);
            String actionName = "";
            int resultType = message.getResultType();

            /*
             * 1. Call(Type=2) 인 경우 → message.action 사용
             * 2. CallResult(Type=3) 인 경우 → UUID로 action 조회
             */
            if (resultType == 2) {
                actionName = message.getAction();
            } else if (resultType == 3) {
                SendHashMapObject obj = (SendHashMapObject) newHashMapUuid.get(message.getId());
                if (obj != null) {
                    actionName = obj.getActionName();
                    this.connectorId = obj.getConnectorId();
                } else {
                    logger.warn("No stored request for uuid={}", message.getId());
                    return;
                }
            }

            JSONObject payload = new JSONObject(message.getPayload().toString());
            OcppHandler handler = null;
            //log data save
            logDataSave.makeLogDate(actionName, text);
            /**
             * DataTransfer 분기 처리
             */
            if ("DataTransfer".equals(actionName)) {
                // payload 안의 messageId 추출
                String messageId = payload.optString("messageId", "");
                if (messageId.isEmpty()) {
                    logger.warn("No messageId in DataTransfer message");
                    return;
                }

                // DataTransfer 전용 handlerMap 사용
                handler = dataTransferHandlerMap.get(messageId);
                if (handler == null) {
                    logger.warn("No handler for messageId={}", messageId);
                    return;
                }
                // DataTransfer handler 호출
                handler.handle(payload, connectorId, message.getId());
            } else {
                /**
                 * 일반 OCPP Action 처리
                 */
                handler = handlerMap.get(actionName);
                if (handler == null) {
                    logger.warn("No handler for action={}", actionName);
                    return;
                }
                handler.handle(payload, connectorId, message.getId());
            }

            // Call (Type 2) 또는 CallResult (Type 3) 판별
            if (message.getResultType() == 2) {
                actionName = message.getAction();
            } else if (message.getResultType() == 3) {
                SendHashMapObject obj = (SendHashMapObject) newHashMapUuid.get(message.getId());
                if (obj != null) {
                    actionName = obj.getActionName();
                    this.connectorId = obj.getConnectorId();
                }
            }
            // 공통 처리 (ID 삭제 등)
            newHashMapUuid.remove(message.getId());
        } catch (Exception e) {
            logger.error(" onGetMessage error : {}", e.getMessage());
        }
    }

    @Override
    public void onGetFailure(WebSocket webSocket, Throwable t) {
        this.webSocket = webSocket;
        socket.setState(SocketState.RECONNECT_ATTEMPT);
        logger.error(t.toString());
    }



    /**
     * single connector Id
     *
     * @param actionName action name
     * @param request    request
     * @throws OccurenceConstraintException
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSend(String actionName, Request request) throws OccurenceConstraintException {
        if (!request.validate()) {
            logger.error("Can't send request: not validated. Payload {}: ", request);
            throw new OccurenceConstraintException();
        }
        try {
            String id = store(request);
            Object payload = packPayload(request);
            Object call = makeCall(id, actionName, payload);
            String actionNameCompare = null;
            if (call != null) {
                try {
                    this.webSocket.send(call.toString());
                    if (Objects.equals(actionName, "DataTransfer")) {
                        // DataTransfer 종류가 많아 ACTION_NAME 대신 MESSAGE_ID 를 Key 값으로 정의
                        // message_id 별로 parsing 해야 하는 부분이 있음.
                        JSONObject jsonObject = new JSONObject(payload.toString());
                        actionNameCompare = jsonObject.getString("messageId");
                        hashMapUuid.put(id, jsonObject.getString("messageId"));
                        logDataSave.makeLogDate(jsonObject.getString("messageId"), call.toString());
                    } else {
                        actionNameCompare = actionName;
                        hashMapUuid.put(id, actionName);
                        logDataSave.makeLogDate(actionName, call.toString());
                    }
                    // debug event listener register
                    if (socketMessageDebugListener != null) {
                        socketMessageDebugListener.onMessageReceiveDebugEvent(2, call.toString(), actionName);
                    }
                    logger.trace("Send a message : {}", call);
                } catch (Exception e) {
                    //dump data
                    if (actionList.contains(actionNameCompare)) {
                        logDataSaveDump.makeDump(call.toString());
                    }
                    logDataSave.makeLogDate("<<send fail>>" + actionName, call.toString());
                    logger.error("send error  : {} ", e.toString());

                }
            }
        } catch (Exception e) {
            logger.error("onSend error  : {} ", e.toString());
        }
    }

    /**
     * multi connectorId
     *
     * @param connectorId connector id
     * @param actionName  action name
     * @param request     request
     * @throws OccurenceConstraintException
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSend(int connectorId, String actionName, Request request) throws OccurenceConstraintException {
        if (!request.validate()) {
            logger.error("multi Can't send request: not validated. Payload {}: ", request);
            throw new OccurenceConstraintException();
        }
        try {
            String id = store(request);
            Object payload = packPayload(request);
            Object call = makeCall(id, actionName, payload);
            String actionNameCompare = null;
            if (call != null) {
                try {
                    this.webSocket.send(call.toString());
                    SendHashMapObject sendHashMapObject = new SendHashMapObject();
                    sendHashMapObject.setConnectorId(connectorId);
                    if (Objects.equals(actionName, "DataTransfer")) {
                        // DataTransfer 종류가 많아 ACTION_NAME 대신 MESSAGE_ID 를 Key 값으로 정의
                        // message_id 별로 parsing 해야 하는 부분이 있음.
                        JSONObject jsonObject = new JSONObject(payload.toString());
                        actionNameCompare = jsonObject.getString("messageId");
                        sendHashMapObject.setActionName(jsonObject.getString("messageId"));
                        newHashMapUuid.put(id, sendHashMapObject);
                        logDataSave.makeLogDate(jsonObject.getString("messageId"), call.toString());
                    } else {
                        actionNameCompare = actionName;
                        sendHashMapObject.setActionName(actionName);
                        newHashMapUuid.put(id, sendHashMapObject);
                        logDataSave.makeLogDate(actionName, call.toString());
                    }
                    // debug event listener register
//                    if (socketMessageDebugListener != null) {
//                        socketMessageDebugListener.onMessageReceiveDebugEvent(2, call.toString(), actionName);
//                    }
                    logger.trace("Send a message: {}", call);
                } catch (Exception e) {
                    //dump data
                    if (actionList.contains(actionNameCompare)) {
                        logDataSaveDump.makeDump(call.toString());
                    }
                    logDataSave.makeLogDate("<<send fail>>" + actionName, call.toString());
                    logger.error("send error : {} ", e.toString());
                }
            }
        } catch (Exception e) {
            logger.error("onSend error : {} ", e.toString());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResultSend(String actionName, String uuid, Confirmation confirmation) throws OccurenceConstraintException {
        if (!confirmation.validate()) {
            logger.error("Can't send request:  not validated. Payload {}: ", confirmation);
            throw new OccurenceConstraintException();
        }
        try {
            Object call = makeCallResult(uuid, actionName, packPayload(confirmation));
            if (call != null) {
                this.webSocket.send(call.toString());
                logDataSave.makeLogDate(actionName, call.toString());
                logger.trace(" Send a message: {}", call);
            }
        } catch (Exception e) {
            logger.error("onResultSend : {}", e.getMessage());
        }
    }

    @Override
    public void onCall(String id, String action, Object payload) {
        logger.trace("Send a message: id : {}, action : {}, payload : {}", id, action, payload.toString());
    }

    /**
     * Dump data send (미전송 데이터)
     *
     * @param text json string
     */
    public void onSend(String text) {
        try {
            this.webSocket.send(text);
            Message message = parse(text);
            String uuid = message.getId();
            String actionName = message.getAction();
            if (Objects.equals(actionName, "DataTransfer")) {
                JSONObject jsonObject = new JSONObject(message.getPayload().toString());
                actionName = jsonObject.getString("messageId");
                hashMapUuid.put(uuid, actionName);
            } else {
                hashMapUuid.put(uuid, actionName);
            }
            LogDataSave logDataSave = new LogDataSave("log");
            logDataSave.makeLogDate(actionName, text);
            logger.trace(" Send a message : {}", message);
        } catch (Exception e) {
            logger.error(" onSend error : {} ", e.toString());
        }
    }

    public String store(Request request) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        return UUID.randomUUID().toString();
    }

    /**
     * handler --> message send
     *
     * @param messageType message Type
     * @param connectorId connector Id ( 0 : All  1: 1채널  2:2채널)
     * @param delayTime   delay time
     * @param idTag       idTag (idTag, smsTele)
     * @param Uuid        UUID
     * @param result      RESULT (결제성공여부), remote start/stop connectorId check,
     * @return msg
     */
    public android.os.Message onMakeHandlerMessage(int messageType, int connectorId, int delayTime, String idTag, String Uuid, String alarmCode, Boolean result) {
        try {
            android.os.Message msg = new android.os.Message();
            Bundle bundle = new Bundle();
            bundle.putInt("connectorId", connectorId);
            bundle.putInt("delay", delayTime);
            bundle.putString("idTag", idTag);
            bundle.putString("uuid", Uuid);
            bundle.putString("alarmCode", alarmCode);
            bundle.putBoolean("result", result);
            msg.setData(bundle);
            msg.what = messageType;
            return msg;
        } catch (Exception e) {
            logger.error("onMakeHandlerMessage error : {}", e.getMessage());
        }
        return null;
    }








//==============삭제 ==================================//////////////////////////////////////////////////
//================================================
//================================================
//================================================





    public SocketMessageListener getSocketMessageListener() {
        return socketMessageListener;
    }

    public void setSocketMessageListener(SocketMessageListener socketMessageListener) {
        this.socketMessageListener = socketMessageListener;
    }



    public HashMap<String, Object> getNewHashMapUuid() {
        return newHashMapUuid;
    }

    public void setNewHashMapUuid(String uuid, SendHashMapObject newHashMapUuid) {
        this.newHashMapUuid.put(uuid, newHashMapUuid);
    }


}
