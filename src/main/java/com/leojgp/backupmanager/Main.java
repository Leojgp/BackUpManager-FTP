package com.leojgp.backupmanager;

import com.leojgp.backupmanager.config.FTPConfiguration;
import com.leojgp.backupmanager.remotefile.FTPManager;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        FTPConfiguration config = new FTPConfiguration();
        FTPManager ftpManager = new FTPManager(config);
        String downloadPath = "C:\\Users\\leona\\Documents\\2DAM\\ProyectoProcesos\\downloadsFromServer\\test_downloaded.txt";
        String downloadSinCifrado = "C:\\Users\\leona\\Documents\\2DAM\\ProyectoProcesos\\downloadsFromServer\\test_sin_cifrado.txt";
        try {
            ftpManager.conectar();

            String fileName = "test.txt";
            String localPath = config.getLocalDir() + "\\" + fileName;

            // Crear y subir
            ftpManager.crearFichero(fileName);
            try {
                ftpManager.subirFichero(localPath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Descargar y descifrar
            try {
                ftpManager.descargarFichero(fileName, downloadPath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Descargar sin descifrar
            ftpManager.descargarFicheroSinDescifrado(fileName, downloadSinCifrado);

            ftpManager.desconectar();

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}