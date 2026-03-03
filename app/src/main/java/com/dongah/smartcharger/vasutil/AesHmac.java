package com.dongah.smartcharger.vasutil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesHmac {

    private static final Logger logger = LoggerFactory.getLogger(AesHmac.class);

    public static byte[] encrypt(byte[] data, byte[] aesKey, byte[] macKey, byte[] keyId) throws Exception {
        // 1. IV 생성
        // iv : RFC 5246 규격에 맞추기 위해 사용
        // iv(Initialization Vector) : 초기화 벡터로, AES-CBC 블록 암호 모드에서 첫 블록을 암호화할 때 사용하는 값임.
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16];
        random.nextBytes(iv);

        // 2. AES-CBC 암호화
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(iv));
        byte[] encryptedData = cipher.doFinal(data);

        // 3. HMAC 생성 (keyId + IV + 암호문에 대해)
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(new SecretKeySpec(macKey, "HmacSHA256"));
        hmac.update(keyId);
        hmac.update(iv);
        hmac.update(encryptedData);
        byte[] mac = hmac.doFinal();

        // 4. 최종 메시지 구성 (keyId + IV + 암호문 + MAC)
        return CryptoUtil.buildEncryptedMessage(keyId, iv, encryptedData, mac);
    }
}
