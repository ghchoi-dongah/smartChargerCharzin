package com.dongah.smartcharger.vasutil;

import android.os.Build;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtil {

    private static final Logger logger = LoggerFactory.getLogger(CryptoUtil.class);


    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static KeyPair generateECKeyPair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", "BC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        return kpg.generateKeyPair();
    }

    public static byte[] generateSharedSecret(PrivateKey privKey, PublicKey pubKey) throws Exception {
        KeyAgreement ka = KeyAgreement.getInstance("ECDH", "BC");
        ka.init(privKey);
        ka.doPhase(pubKey, true);
        return ka.generateSecret(); // 32 bytes
    }

    // Concatenation KDF (SHA-384), outLen=48 bytes
    public static byte[] deriveKey(byte[] sharedSecret, byte algorithmId, byte idU, byte idV) throws Exception {
        final int outLenBytes = 48;
        ByteArrayOutputStream other = new ByteArrayOutputStream();
        other.write(new byte[] { algorithmId }); // 0x01
        other.write(new byte[] { idU }); // 0x55
        other.write(new byte[] { idV }); // 0x56
        // SuppPubInfo: keydatalen in bits (384 = 0x00000180)
        other.write(new byte[] { 0x00, 0x00, 0x01, (byte) 0x80 });

        MessageDigest sha384 = MessageDigest.getInstance("SHA-384");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int counter = 1;
        byte[] otherInfo = other.toByteArray();

        while (out.size() < outLenBytes) {
            sha384.reset();
            byte[] ctr = new byte[] {
                    (byte) (counter >>> 24),
                    (byte) (counter >>> 16),
                    (byte) (counter >>> 8),
                    (byte) counter
            };
            sha384.update(ctr);
            sha384.update(sharedSecret);
            sha384.update(otherInfo);
            byte[] block = sha384.digest();
            int need = Math.min(block.length, outLenBytes - out.size());
            out.write(block, 0, need);
            counter++;
        }
        return out.toByteArray();
    }

    public static byte[] getAesKey(byte[] derivedKey) {
        return Arrays.copyOfRange(derivedKey, 0, 16);
    }

    public static byte[] getMacKey(byte[] derivedKey) {
        return Arrays.copyOfRange(derivedKey, 16, 48);
    }

    public static byte[] aesCbcEncrypt(byte[] plaintext, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(plaintext);
    }

    public static byte[] generateHmacSha256(byte[] data, byte[] macKey) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(macKey, "HmacSHA256");
        mac.init(keySpec);
        return mac.doFinal(data);
    }

    public static byte[] buildEncryptedMessage(byte[] keyId, byte[] iv, byte[] encryptedData, byte[] mac) {
        // keyId 길이(2바이트) + keyId + IV + 암호문 + MAC
        byte[] message = new byte[2 + keyId.length + iv.length + encryptedData.length + mac.length];

        // keyId 길이 추가 (2바이트)
        message[0] = (byte) ((keyId.length >> 8) & 0xFF);
        message[1] = (byte) (keyId.length & 0xFF);

        // keyId 복사
        System.arraycopy(keyId, 0, message, 2, keyId.length);

        // IV 복사
        System.arraycopy(iv, 0, message, 2 + keyId.length, iv.length);

        // 암호문 복사
        System.arraycopy(encryptedData, 0, message, 2 + keyId.length + iv.length, encryptedData.length);

        // MAC 복사
        System.arraycopy(mac, 0, message, 2 + keyId.length + iv.length + encryptedData.length, mac.length);

        return message;
    }

    public static KeyPair loadKeyPairFromPem(String certPath, String keyPath) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // 1. Load public key from certificate
//        byte[] certBytes = Files.readAllBytes(Paths.get(certPath));
        File file = new File(certPath);
        byte[] certBytes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            int readBytes = fis.read(certBytes);
            if (readBytes != certBytes.length) {
                throw new IOException("File read incomplete: " + readBytes + "/" + certBytes.length);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        String certPem = new String(certBytes, StandardCharsets.UTF_8)
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replaceAll("\\s+", "");
//        byte[] certDecoded = Base64.getDecoder().decode(certPem);
        byte[] certDecoded = Base64.decode(certPem, Base64.DEFAULT);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certDecoded));
        PublicKey pubKey = cert.getPublicKey();

        // 2. Load private key from PEM
//        byte[] keyBytes = Files.readAllBytes(Paths.get(keyPath));
        byte[] keyBytes = readFileToBytes(keyPath);

        String keyPem = new String(keyBytes, StandardCharsets.UTF_8)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
//        byte[] keyDecoded = java.util.Base64.getDecoder().decode(keyPem);
        byte[] keyDecoded = Base64.decode(keyPem, Base64.DEFAULT);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyDecoded);
        KeyFactory kf = KeyFactory.getInstance("EC");
        PrivateKey privKey = kf.generatePrivate(keySpec);

        return new KeyPair(pubKey, privKey);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static PublicKey loadPublicKeyFromCertPem(String certPemPath) throws Exception {
        // Files.readAllBytes() 대체 코드
        InputStream is = Files.newInputStream(Paths.get(certPemPath));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int nRead;
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        is.close();

        // PEM → DER 변환
        String certPem = new String(buffer.toByteArray(), StandardCharsets.UTF_8)
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replaceAll("\\s+", "");

        // Base64 디코딩 (API 24 대응)
        byte[] certDer;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            certDer = java.util.Base64.getDecoder().decode(certPem);
        } else {
            certDer = android.util.Base64.decode(certPem, android.util.Base64.DEFAULT);
        }

        // X.509 인증서 파싱
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certDer));

        // 공개키 반환
        return cert.getPublicKey();
    }

    public static byte[] aesCbcDecrypt(byte[] ciphertext, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(ciphertext);
    }

    public static class EncryptedMessage {
        public byte[] keyId;
        public byte[] iv;
        public byte[] encryptedData;
        public byte[] mac;

        // 기본 생성자 추가
        public EncryptedMessage() {
        }

        // 기존 생성자도 유지 (필요한 경우)
        public EncryptedMessage(byte[] iv, byte[] encryptedData, byte[] mac) {
            this.iv = iv;
            this.encryptedData = encryptedData;
            this.mac = mac;
        }

        // keyId를 포함한 새로운 생성자 추가 (필요한 경우)
        public EncryptedMessage(byte[] keyId, byte[] iv, byte[] encryptedData, byte[] mac) {
            this.keyId = keyId;
            this.iv = iv;
            this.encryptedData = encryptedData;
            this.mac = mac;
        }
    }

    public static EncryptedMessage parseEncryptedMessage(byte[] message) {
        EncryptedMessage result = new EncryptedMessage();

        // keyId 길이 읽기 (처음 2바이트)
        int keyIdLength = ((message[0] & 0xFF) << 8) | (message[1] & 0xFF);

        // keyId 추출
        result.keyId = new byte[keyIdLength];
        System.arraycopy(message, 2, result.keyId, 0, keyIdLength);

        // 나머지 데이터 파싱
        int currentPos = 2 + keyIdLength;

        // IV (16바이트)
        result.iv = new byte[16];
        System.arraycopy(message, currentPos, result.iv, 0, 16);
        currentPos += 16;

        // MAC은 마지막 32바이트
        result.mac = new byte[32];
        System.arraycopy(message, message.length - 32, result.mac, 0, 32);

        // 암호문
        int encryptedLength = message.length - currentPos - 32;
        result.encryptedData = new byte[encryptedLength];
        System.arraycopy(message, currentPos, result.encryptedData, 0, encryptedLength);

        return result;
    }

    public static boolean verifyHmacSha256(byte[] keyId, byte[] iv, byte[] encryptedData, byte[] macKey,
                                           byte[] expectedMac) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(new SecretKeySpec(macKey, "HmacSHA256"));
        hmac.update(keyId);
        hmac.update(iv);
        hmac.update(encryptedData);
        byte[] calculatedMac = hmac.doFinal();

        if (calculatedMac.length != expectedMac.length)
            return false;
        int diff = 0;
        for (int i = 0; i < calculatedMac.length; i++) {
            diff |= (calculatedMac[i] ^ expectedMac[i]);
        }
        return diff == 0;
    }

    public static byte[] readFileToBytes(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] bytes = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file)) {
            int readBytes = fis.read(bytes);
            if (readBytes != bytes.length) {
                throw new IOException("File read incomplete: " + readBytes + "/" + bytes.length);
            }
        }
        return bytes;
    }



}
