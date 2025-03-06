package com.leojgp.backupmanager.encryption;

import com.leojgp.backupmanager.config.FTPConfiguration;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

/**
 * Clase para gestionar el cifrado y descifrado de datos utilizando AES.
 * Utiliza la configuración proporcionada para obtener la clave y los parámetros de cifrado.
 */
public class EncryptionManager {

    private FTPConfiguration config;

    public EncryptionManager(FTPConfiguration config) {
        this.config = config;
    }

    /**
     * Cifra un texto plano utilizando el algoritmo AES.
     *
     * @param plainText Texto plano a cifrar.
     * @return Texto cifrado en formato Base64.
     * @throws Exception Si ocurre un error durante el cifrado.
     */
    public String cifrar(String plainText) throws Exception {
        Key key = new SecretKeySpec(config.getAesPassword().getBytes(), 0, config.getAesKeyLength(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cipherText = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(cipherText);
    }

    /**
     * Descifra un texto cifrado utilizando el algoritmo AES.
     *
     * @param cipherText Texto cifrado en formato Base64.
     * @return Texto plano descifrado.
     * @throws Exception Si ocurre un error durante el descifrado.
     */
    public String descifrar(String cipherText) throws Exception {
        Key key = new SecretKeySpec(config.getAesPassword().getBytes(), 0, config.getAesKeyLength(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(plainText);
    }
}