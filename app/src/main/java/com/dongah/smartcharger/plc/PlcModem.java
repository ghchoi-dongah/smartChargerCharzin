package com.dongah.smartcharger.plc;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.basefunction.GlobalVariables;
import com.dongah.smartcharger.plc.receive.AuthorizationReceive;
import com.dongah.smartcharger.plc.receive.BatteryInfoReceive;
import com.dongah.smartcharger.plc.receive.BootInfoReceive;
import com.dongah.smartcharger.plc.receive.ChargeParameterDiscoveryReceive;
import com.dongah.smartcharger.plc.receive.ChargingStatusReceive;
import com.dongah.smartcharger.plc.receive.ConfigCheckReceive;
import com.dongah.smartcharger.plc.receive.FileDataAnswerReceive;
import com.dongah.smartcharger.plc.receive.InitReceive;
import com.dongah.smartcharger.plc.receive.OtaOrderReceive;
import com.dongah.smartcharger.plc.receive.PacketReceive;
import com.dongah.smartcharger.plc.receive.PaymentDetailsReceive;
import com.dongah.smartcharger.plc.receive.PaymentServiceSelectionReceive;
import com.dongah.smartcharger.plc.receive.PowerDeliveryReceive;
import com.dongah.smartcharger.plc.receive.SDPReceive;
import com.dongah.smartcharger.plc.receive.ServiceDetailReceive;
import com.dongah.smartcharger.plc.receive.ServiceDiscoveryReceive;
import com.dongah.smartcharger.plc.receive.SessionSetupReceive;
import com.dongah.smartcharger.plc.receive.SessionStopReceive;
import com.dongah.smartcharger.plc.receive.SlacReceive;
import com.dongah.smartcharger.plc.receive.SupportedAppReceive;
import com.dongah.smartcharger.plc.request.FileSendDataRequest;
import com.dongah.smartcharger.plc.request.PacketReportRequest;
import com.dongah.smartcharger.plc.request.PacketRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

import android_serialport_api.SerialPort;

public class PlcModem implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PlcModem.class);


    public static final int HEAD_SIZE = 7;
    public static final byte STX = (byte) 0xA2;
    public static final byte START_CODE = (byte) 0xF2;
    public static final byte ETX = (byte) 0xA3;
    public static final short UNIT_SIZE = (short) 512;

    boolean isOpen = false;


    // serial
    SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;
    Thread plcThread;

    // buffer
    byte[] rxBuffer = new byte[512];
    byte[] txBuffer = new byte[512];

    byte[] BODY_LENGTH = new byte[2];

    DataTransformation dataTransformation;
    /** Listener register*/
    PlcListener plcListener;
    public void setPlcListener(PlcListener plcListener) {
        this.plcListener = plcListener;
    }
    public void setPlcListenerStop() {
        this.plcListener = null;
    }

    short duty , cpVoltage, packetNow = 1 ;
    byte[] report;
    FileSendDataRequest fileSendDataRequest;

    //
    PacketRequest packetRequest;
    PacketReportRequest packetReportRequest;

    public PlcModem(String comPort) {
        try {
            serialPort = new SerialPort(new File(comPort), 115200, 0);
            isOpen = true;

            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();

            dataTransformation = new DataTransformation();

            plcThread = new Thread(this);
            plcThread.start();

        } catch (Exception e) {
            logger.error(" PlcModem create error : {} ", e.getMessage());
        }

    }

    byte[] completeData;
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Arrays.fill(rxBuffer, (byte) 0x00);
                int readCnt = inputStream.read(rxBuffer);
                if (readCnt >= HEAD_SIZE) {
                    byte[] receivedData = Arrays.copyOfRange(rxBuffer, 0, readCnt);
                    buffer.write(receivedData, 0, readCnt);
                    Log.d("PLC", String.join(" ", bytesToHex(buffer.toByteArray())));
                    if (Objects.equals(receivedData[readCnt-2], (byte)0xa3)) {
                        // complete data PACKET

                        completeData = buffer.toByteArray();
                        if (completeData[0] == STX) {
                            System.arraycopy(completeData,2, BODY_LENGTH, 0, 2);
                            short bodyLength = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(BODY_LENGTH, 1));
                            processData(completeData, bodyLength);
                        }
                        buffer.reset();
                    } else if (Objects.equals(receivedData[0], START_CODE)) {
                        //F2 22 00 00 02 00 70 04 01 A7
                        completeData = buffer.toByteArray();
                        processData(completeData, 0);
                    }
                    // listener interface
                    if (plcListener != null) plcListener.onReceiveBuffer(completeData);
                }
            } catch (Exception e) {
                buffer.reset();
                logger.error(" plc serial thread error : {}", e.getMessage());
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void processData(byte[] data, int len) {
        try {
            byte packetType = data[1];
            switch (packetType) {
                case 0x12:
                    BootInfoReceive bootInfoReceive = new BootInfoReceive(data, len);
                    bootInfoReceive.doReceive();
                    break;
                case 0x18:
                    //OTA Order
                    OtaOrderReceive otaOrderReceive = new OtaOrderReceive(data, len);
                    otaOrderReceive.doReceive();
                    // file send data start 0x21
                    fileSendDataRequest = new FileSendDataRequest();
                    report = fileSendDataRequest.makeFileSendDataRequest(packetNow, GlobalVariables.getTotalPackets());
                    onSend(report);
                    break;
                case 0x22:
                    FileDataAnswerReceive fileDataAnswerReceive = new FileDataAnswerReceive(data, len);
                    short[] packetNext = fileDataAnswerReceive.doReceive();
                    //FileDataAnswer
                    if (packetNext[0] < GlobalVariables.getTotalPackets()) {
                        if (packetNext[1] != 2) {
                            report = fileSendDataRequest.makeFileSendDataRequest(packetNext[0], GlobalVariables.getTotalPackets());
                            onSend(report);
                        }
                    }
                    break;
                case 0x71:
                    //INIT Response (SECC -> EVSE)
                    InitReceive initReceive = new InitReceive(data, len);
                    initReceive.doReceive();
                    break;
                case 0x73:
                    //SLAC Report (SECC -> EVSE)
                    SlacReceive slacReceive = new SlacReceive(data, len);
                    slacReceive.doReceive();
                    //response
                    packetRequest = new PacketRequest(packetType, (short) 8, (byte) 0x00);
                    duty = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsPwmDuty();
                    cpVoltage = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsCpVoltage();
                    report = packetRequest.onMakeRequestData(duty, cpVoltage);
                    onSend(report);
                    break;
                case 0x74:          //SDP Report (SECC->EVSE)
                    SDPReceive sdpReceive = new SDPReceive(data, len);
                    sdpReceive.doReceive();
                    // start 처리
                    packetRequest = new PacketRequest(packetType, (short) 8, (byte) 0x00);
                    duty = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsPwmDuty();
                    cpVoltage = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsCpVoltage();
                    report = packetRequest.onMakeRequestData(duty, cpVoltage);
                    onSend(report);
                    break;
                case 0x70:          //ConfigSetup Response (SECC -> EVSE)
                case 0x72:          //Start Response (SECC -> EVCC)
                case 0x76:          //StopAll Request (EVSE->SECC)
                case 0x77:          //StopAll Report (SECC->EVSE)
                case 0x7A:          //AuthInfoSetup Response (SECC->EVSE)
                case 0x78:          //PowerSetup Response (SECC->EVSE)
                    //응답 값을 받는다...
                    PacketReceive packetReceive = new PacketReceive(data, len);
                    packetReceive.doReceive();
                    break;
                case 0x79:
                    ConfigCheckReceive configCheckReceive = new ConfigCheckReceive(data,len);
                    configCheckReceive.doReceive();
                    break;
                case (byte) 0x81:
                    SupportedAppReceive supportedAppReceive = new SupportedAppReceive(data,len);
                    supportedAppReceive.doReceive();
                    //response
                    packetReportRequest = new PacketReportRequest((byte) 0x81, (short) 6, (byte) 0x01);
                    duty = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsPwmDuty();
                    cpVoltage = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsCpVoltage();
                    report = packetReportRequest.onMakeRequestData(duty, cpVoltage);
                    onSend(report);
                    break;
                case (byte) 0x82:
                    SessionSetupReceive sessionSetupReceive = new SessionSetupReceive(data,len);
                    sessionSetupReceive.doReceive();
                    //response
                    packetReportRequest = new PacketReportRequest((byte) 0x82, (short) 6, (byte) 0x01);
                    duty = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsPwmDuty();
                    cpVoltage = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsCpVoltage();
                    report = packetReportRequest.onMakeRequestData(duty, cpVoltage);
                    onSend(report);
                    break;
                case (byte) 0x83:
                    ServiceDiscoveryReceive serviceDiscoveryReceive = new ServiceDiscoveryReceive(data,len);
                    serviceDiscoveryReceive.doReceive();
                    //response
                    packetReportRequest = new PacketReportRequest((byte) 0x83, (short) 6, (byte) 0x01);
                    duty = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsPwmDuty();
                    cpVoltage = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsCpVoltage();
                    report = packetReportRequest.onMakeRequestData(duty, cpVoltage);
                    onSend(report);
                    break;
                case (byte) 0x84:
                    ServiceDetailReceive serviceDetailReceive = new ServiceDetailReceive(data, len);
                    serviceDetailReceive.doReceive();
                    //response
                    packetReportRequest = new PacketReportRequest((byte) 0x84, (short) 6, (byte) 0x01);
                    duty = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsPwmDuty();
                    cpVoltage = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsCpVoltage();
                    report = packetReportRequest.onMakeRequestData(duty, cpVoltage);
                    onSend(report);
                    break;
                case (byte) 0x85:
                    PaymentServiceSelectionReceive paymentServiceSelectionReceive = new PaymentServiceSelectionReceive(data,len);
                    paymentServiceSelectionReceive.doReceive();
                    //response
                    packetReportRequest = new PacketReportRequest((byte) 0x85, (short) 6, (byte) 0x01);
                    duty = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsPwmDuty();
                    cpVoltage = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsCpVoltage();
                    report = packetReportRequest.onMakeRequestData(duty, cpVoltage);
                    onSend(report);
                    break;
                case (byte) 0x86:
                    PaymentDetailsReceive paymentDetailsReceive = new PaymentDetailsReceive(data,len);
                    paymentDetailsReceive.doReceive();
                    //response
                    packetReportRequest = new PacketReportRequest((byte) 0x86, (short) 6, (byte) 0x01);
                    duty = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsPwmDuty();
                    cpVoltage = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsCpVoltage();
                    report = packetReportRequest.onMakeRequestData(duty, cpVoltage);
                    onSend(report);
                    break;
                case (byte) 0x87:
                    AuthorizationReceive authorizationReceive = new AuthorizationReceive(data, len);
                    authorizationReceive.doReceive();
                    //response
                    packetReportRequest = new PacketReportRequest((byte) 0x87, (short) 6, (byte) 0x01);
                    duty = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsPwmDuty();
                    cpVoltage = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsCpVoltage();
                    report = packetReportRequest.onMakeRequestData(duty, cpVoltage);
                    onSend(report);
//                    /// charging
//                    ChargerConfiguration chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
//                    ClassUiProcess classUiProcess = ((MainActivity) MainActivity.mContext).getClassUiProcess();
//                    if (Objects.equals(classUiProcess.getUiSeq(), UiSeq.CONNECT_CHECK) &&
//                            !Objects.equals(chargerConfiguration.getAuthMode(), "0")) {
//                        classUiProcess.setUiSeq(UiSeq.CHARGING);
//                        ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(UiSeq.CHARGING, "CHARGING", null);
//                    }
                    break;
                case (byte) 0x88:
                    ChargeParameterDiscoveryReceive chargeParameterDiscoveryReceive = new ChargeParameterDiscoveryReceive(data,len);
                    chargeParameterDiscoveryReceive.doReceive();
                    //response
                    packetReportRequest = new PacketReportRequest((byte) 0x88, (short) 6, (byte) 0x01);
                    duty = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsPwmDuty();
                    cpVoltage = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsCpVoltage();
                    report = packetReportRequest.onMakeRequestData(duty, cpVoltage);
                    onSend(report);
                    break;
                case (byte) 0x89:
                    PowerDeliveryReceive powerDeliveryReceive = new PowerDeliveryReceive(data,len);
                    powerDeliveryReceive.doReceive();
                    //response
                    packetReportRequest = new PacketReportRequest((byte) 0x89, (short) 6, (byte) 0x01);
                    duty = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsPwmDuty();
                    cpVoltage = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsCpVoltage();
                    report = packetReportRequest.onMakeRequestData(duty, cpVoltage);
                    onSend(report);
                    break;
                case (byte) 0x8a:
                    ChargingStatusReceive chargingStatusReceive = new ChargingStatusReceive(data,len);
                    chargingStatusReceive.doReceive();
                    //response
                    packetReportRequest = new PacketReportRequest((byte) 0x8a, (short) 6, (byte) 0x01);
                    duty = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsPwmDuty();
                    cpVoltage = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsCpVoltage();
                    report = packetReportRequest.onMakeRequestData(duty, cpVoltage);
                    onSend(report);
                    break;
                case (byte) 0x8c:
                    SessionStopReceive sessionStopReceive = new SessionStopReceive(data,len);
                    sessionStopReceive.doReceive();
                    //response
                    packetReportRequest = new PacketReportRequest((byte) 0x8c, (short) 6, (byte) 0x01);
                    duty = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsPwmDuty();
                    cpVoltage = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsCpVoltage();
                    report = packetReportRequest.onMakeRequestData(duty, cpVoltage);
                    onSend(report);
                    break;
                case (byte) 0x8d:
                    BatteryInfoReceive batteryInfoReceive = new BatteryInfoReceive(data, len);
                    batteryInfoReceive.doReceive();
                    //response
                    packetReportRequest = new PacketReportRequest((byte) 0x8d, (short) 6, (byte) 0x01);
                    duty = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsPwmDuty();
                    cpVoltage = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().getCsCpVoltage();
                    report = packetReportRequest.onMakeRequestData(duty, cpVoltage);
                    onSend(report);
                    break;
            }

        } catch (Exception e) {
            logger.error(" processData  error :  {}", e.getMessage());
        }
    }

    // byte array send
    public boolean onSend(byte[] data) {
        try {
            if (isOpen) {
                outputStream.write(data);
                // listener interface
                Log.d("PLC", String.join(" ", bytesToHex(data)));
                if (plcListener != null) plcListener.onSendBuffer(data);
                return true;
            }
        } catch (Exception e) {
            logger.error("onSend Error : {}", e.getMessage());
        }
        return false;
    }


    private byte[] onValidation(byte[] data) {
        try {
            for (int i = 0; i < data.length; i++) {
                if (data[i] == (byte) 0Xa2) {
                    System.arraycopy(data,i+2, BODY_LENGTH, 0, 2);
                    short bodyLen = dataTransformation.ByteArrayToShort(dataTransformation.changeByteOrder(BODY_LENGTH, 1));

                    byte[] bodyData = new byte[bodyLen];
                    System.arraycopy(data,i+8, bodyData, 0, bodyLen);
                    byte checkSum = data[bodyLen +10 + i - 1];
                    if (dataTransformation.confirmCheckSum(bodyData, bodyLen, checkSum)) {
                        byte[] resultByte = new byte[bodyLen+10];
                        System.arraycopy(data,i, resultByte, 0, bodyLen+10);
                        return resultByte;
                    }
                    break;
                }
            }

        } catch (Exception e) {
            logger.error(" onValidation error :  {}", e.getMessage());
        }
        return data;
    }

    private int findIndex(byte[] data, byte value) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == value) return i;
        }
        return -1;
    }

    private static void processPacket(byte[] packet) {
        Log.d("PLC", "받은 패킷 : " + bytesToHex(packet));
    }


    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }




    public byte[] getRxBuffer() {
        return rxBuffer;
    }

    public void setRxBuffer(byte[] rxBuffer) {
        this.rxBuffer = rxBuffer;
    }

    public byte[] getTxBuffer() {
        return txBuffer;
    }

    public void setTxBuffer(byte[] txBuffer) {
        this.txBuffer = txBuffer;
    }
}
