package com.dongah.smartcharger.plc.request;

import com.dongah.smartcharger.plc.DataTransformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class HeaderReq {

    private static final Logger logger = LoggerFactory.getLogger(HeaderReq.class);

    DataTransformation dataTransformation = new DataTransformation();

    byte stx;
    byte packetType;
    byte[] bodyLen;
    byte mgType;
    byte sender;
    byte[] reserved;


    public HeaderReq(byte packetType, byte[] bodyLen, byte msgType, byte sender) {
        this.packetType = packetType;
        this.bodyLen = bodyLen;
        this.sender = sender;
        //
        this.stx = (byte) 0xa2;
        this.mgType = msgType;
        reserved = new byte[2];
        Arrays.fill(reserved, (byte) 0x00);
    }

    public byte[] dataSet() {
        try {
            byte[] result = new byte[8];
            result[0] = stx;
            result[1] = packetType;
            System.arraycopy(bodyLen, 0, result, 2,2);
            result[4] = mgType;
            result[5] = sender;
            System.arraycopy(reserved, 0, result, 6,2);
            return  result;
        } catch (Exception e) {
            logger.error("HeaderReq dataSet error : {}", e.getMessage());
        }
        return null;
    }


    public byte getStx() {
        return stx;
    }

    public void setStx(byte stx) {
        this.stx = stx;
    }

    public byte getPacketType() {
        return packetType;
    }

    public void setPacketType(byte packetType) {
        this.packetType = packetType;
    }

    public byte[] getBodyLen() {
        return bodyLen;
    }

    public void setBodyLen(byte[] bodyLen) {
        this.bodyLen = bodyLen;
    }

    public byte getMgType() {
        return mgType;
    }

    public void setMgType(byte mgType) {
        this.mgType = mgType;
    }

    public byte getSender() {
        return sender;
    }

    public void setSender(byte sender) {
        this.sender = sender;
    }

    public byte[] getReserved() {
        return reserved;
    }

    public void setReserved(byte[] reserved) {
        this.reserved = reserved;
    }
}
