package com.dongah.smartcharger.plc;

import android.annotation.SuppressLint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class DataTransformation {

    private static final Logger logger = LoggerFactory.getLogger(DataTransformation.class);

    //1. check SUm
    public byte checkSum(byte[] data, int length) {
        try {
            byte xor = 0x00;
            for (int i = 0; i < length - 1; i++) {
                xor ^= data[i];
            }
            return xor;
        } catch (Exception e) {
            logger.error( " check sum error : {}", e.getMessage());
        }
        return 0x00;
    }

    //2. check sum : receive data validation
    public boolean confirmCheckSum(byte[] data, short length, byte value) {
        try {
            return Objects.equals(checkSum(data, length), value);
        } catch (Exception e) {
            logger.error(" confirmCheckSum error :{}", e.getMessage());
        }
        return false;
    }

    //3. BCD to String
    public String BCDtoString(byte[] bcd) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bcd) {
            sb.append(BCDtoString(b));
        }
        return sb.toString();
    }

    public String BCDtoString(byte bcd) {
        StringBuilder sb = new StringBuilder();
        byte high = (byte) (bcd & 0xf0);
        high >>>= (byte) 4;
        high = (byte) (high & 0x0f);
        byte low = (byte) (bcd & 0x0f);
        sb.append(high);
        sb.append(low);
        return sb.toString();
    }

    // order : 1 = little endian   / 2 = big endian
    public byte[] changeByteOrder(byte[] data, int order){
        int idx = data.length;
        byte[] result = new byte[idx];
        try {
            switch (order) {
                case 1:
                    for (int i = 0; i < idx; i++) {
                        result[i] = data[idx - (i+1)];
                    }
                    break;
                case 2:
                    result = data;
                    break;
            }
        } catch (Exception e) {
            logger.error(" changeByteOrder error {}", e.getMessage());
        }
        return result;
    }

    public byte[] removeZeros(byte[] data){
        if (data == null) return new byte[0];

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (byte b : data) {
            if (b != 0) {  // 0이 아닌 값만 저장
                outputStream.write(b);
            }
        }

        return outputStream.toByteArray();


    }


    // byte Array short
    public byte[] ShortArrayToByteArray(short[] value) {
        byte[] resultData = new byte[value.length * 2];
        int c = 0;
        try {
            for (short item : value) {
                resultData[c++] = (byte) (item & 0xFF);
                resultData[c++] = (byte) ((item >>> 8) & 0xFF);
            }
        } catch (Exception e) {
            logger.error(" ShortArrayToByteArray error {}", e.getMessage());
        }
        return resultData;
    }

    public short ByteArrayToShort(byte[] value) {
        short newValue = 0;
        try {
            newValue |= (short) ((((int)value[0])<<8)&0xFF00);
            newValue |= (short) ((((int)value[1]))&0xFF);
        } catch (Exception e) {
            logger.error(" ByteArrayToShort error {}", e.getMessage());
        }
        return newValue;
    }

    // char array to byte array
    public byte[] CharArrayToByteArray(char[] value) {
        if (value == null) return null;
        byte[] byteArray = new byte[value.length];
        for (int i = 0; i < value.length; i++) {
            byteArray[i] = (byte) (value[i]);
        }
        return byteArray;
    }

    /**
     * byte array -> char array
     * @param value byte array
     * @return char array
     */
    public char[] ByteArrayToCharArray(byte[] value) {
        if (value == null) return null;
        char[] chars = new char[value.length];
        for (int i = 0; i < value.length; i++) {
            chars[i] = (char) value[i];
        }
        return chars;
    }

    // short to byteArray
    public byte[] ShortToByteArray(short value) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) ((value >> 8) & 0xff);
        byteArray[1] = (byte) (value & 0xff);
        return byteArray;
    }

    // int to byte array
    public byte[] IntToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }

    //byte array to int
    public int ByteArrayToInt(byte[] value) {
        return (value[0] << 24) + ((value[1] & 0xFF) << 16) +
                ((value[2] & 0xFF) << 8) + (value[3] & 0xFF);

    }


    public String[] bytesToHexArray(byte[] bytes) {
        String[] hexArray = new String[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            hexArray[i] = String.format("%02X", bytes[i]); // 2자리 16진수 변환
        }
        return hexArray;
    }


    public String hexToString(String hex) {
        if (hex == null || hex.isEmpty()) return "";

        // 홀수 길이 방지
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex 문자열 길이는 반드시 짝수여야 합니다.");
        }

        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }

        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }


    public String stringToHex(String input) {
        StringBuilder hex = new StringBuilder();
        byte[] bytes = input.getBytes(); // 기본 UTF-8 인코딩
        for (byte b : bytes) {
            hex.append(String.format("%02x", b)); // 2자리 hex로 변환
        }
        return hex.toString();
    }


    @SuppressLint("NewApi")
    public String getConvertUTC(long timestampMs) {
        try {
            Instant instant = Instant.ofEpochMilli(timestampMs);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .withZone(ZoneId.of("UTC"));
            return formatter.format(instant);
        } catch (Exception e) {
            logger.error(" getConvertUTC error : {}", e.getMessage());
        }
        return null;

    }


    public byte[] getTimeToByteArray() {
        try {
            Date now = new Date();
            // YYMMDDhhmmss 포맷으로 변환
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String timeStr = sdf.format(now);
            // 6바이트 배열로 변환
            byte[] timeBytes = new byte[6];
            for (int i = 0; i < 6; i++) {
                // 2자리 숫자를 파싱하여 byte로 변환
                timeBytes[i] = (byte) Integer.parseInt(timeStr.substring(i * 2, i * 2 + 2));
            }

            return timeBytes;
        } catch (Exception e) {
            logger.error(" getTimeToByteArray error :{}", e.getMessage());
        }
        return null;
    }

}
