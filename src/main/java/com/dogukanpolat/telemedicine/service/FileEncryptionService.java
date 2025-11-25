package com.dogukanpolat.telemedicine.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@Slf4j
public class FileEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    @Value("${file.encryption.secret}")
    private String encryptionSecret;

    public byte[] encrypt(byte[] data) throws Exception {
        SecretKey key = getKeyFromPassword();
        Cipher cipher = Cipher.getInstance(ALGORITHM);

        // Generate random IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
        byte[] encryptedData = cipher.doFinal(data);

        // Combine IV and encrypted data
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedData.length);
        byteBuffer.put(iv);
        byteBuffer.put(encryptedData);

        log.debug("Data encrypted successfully");
        return byteBuffer.array();
    }

    public byte[] decrypt(byte[] encryptedData) throws Exception {
        SecretKey key = getKeyFromPassword();
        Cipher cipher = Cipher.getInstance(ALGORITHM);

        // Extract IV and encrypted data
        ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);
        byte[] iv = new byte[GCM_IV_LENGTH];
        byteBuffer.get(iv);
        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        log.debug("Data decrypted successfully");
        return cipher.doFinal(cipherText);
    }

    private SecretKey getKeyFromPassword() throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(encryptionSecret.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(key, "AES");
    }

    public String generateSecureFileName(String originalFileName) {
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String random = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(new SecureRandom().generateSeed(16));

            String extension = "";
            int lastDot = originalFileName.lastIndexOf('.');
            if (lastDot > 0) {
                extension = originalFileName.substring(lastDot);
            }

            return timestamp + "_" + random + extension;
        } catch (Exception e) {
            log.error("Error generating secure file name", e);
            return System.currentTimeMillis() + "_" + originalFileName;
        }
    }
}