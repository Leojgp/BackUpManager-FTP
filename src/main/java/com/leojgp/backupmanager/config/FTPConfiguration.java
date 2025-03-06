package com.leojgp.backupmanager.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Clase para cargar y proporcionar la configuración del servidor FTP y parámetros de cifrado.
 * Lee la configuración desde el archivo "config.properties".
 */
public class FTPConfiguration {

    private static final String CONFIG_FILE = "config.properties";

    private String servidor;
    private int puerto;
    private String usuario;
    private String password;
    private String localDir;
    private String aesPassword;
    private int aesKeyLength;

    public FTPConfiguration() {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
            this.servidor = properties.getProperty("SERVIDOR");
            this.puerto = Integer.parseInt(properties.getProperty("PUERTO"));
            this.usuario = properties.getProperty("USUARIO");
            this.password = properties.getProperty("PASSWORD");
            this.localDir = properties.getProperty("LOCAL_DIR");
            this.aesPassword = properties.getProperty("AES_PASSWORD");
            this.aesKeyLength = Integer.parseInt(properties.getProperty("AES_KEY_LENGTH"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getServidor() {
        return servidor;
    }

    public int getPuerto() {
        return puerto;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getPassword() {
        return password;
    }

    public String getLocalDir() {
        return localDir;
    }

    public String getAesPassword() {
        return aesPassword;
    }

    public int getAesKeyLength() {
        return aesKeyLength;
    }
}