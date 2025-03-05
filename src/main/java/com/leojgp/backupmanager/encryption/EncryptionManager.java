package com.leojgp.backupmanager.encryption;

import com.leojgp.backupmanager.config.FTPConfiguration;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

public class EncryptionManager {

    private FTPConfiguration config;

    public EncryptionManager(FTPConfiguration config) {
        this.config = config;
    }

    public String cifrar(String plainText) throws Exception {
        Key key = new SecretKeySpec(config.getAesPassword().getBytes(), 0, config.getAesKeyLength(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cipherText = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(cipherText);
    }

    public String descifrar(String cipherText) throws Exception {
        Key key = new SecretKeySpec(config.getAesPassword().getBytes(), 0, config.getAesKeyLength(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(plainText);
    }
}