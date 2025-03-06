package com.leojgp.backupmanager;

import com.leojgp.backupmanager.config.FTPConfiguration;
import com.leojgp.backupmanager.remotefile.FTPManager;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.IOException;

public class DownloadAndDecrypt {

    public static void main(String[] args) {
        FTPConfiguration config = new FTPConfiguration();
        FTPManager ftpManager = new FTPManager(config);
        String localDir = "descargas/";

        File localDirFile = new File(localDir);
        if (!localDirFile.exists()) {
            localDirFile.mkdirs();
            System.out.println("Directorio 'descargas' creado.");
        }

        try {
            ftpManager.conectar();
            FTPFile[] files = ftpManager.listarArchivos("history/");

            if (files != null) {
                for (FTPFile file : files) {
                    if (file.isFile()) {
                        String remoteFilePath = "history/" + file.getName();
                        String localFilePath = localDir + file.getName().replace(".cifrado", "");
                        descargarYDescifrar(ftpManager, remoteFilePath, localFilePath);
                    }
                }
            }

            ftpManager.desconectar();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error al descifrar");
            e.printStackTrace();
        }
    }

    private static void descargarYDescifrar(FTPManager ftpManager, String remoteFilePath, String localFilePath) throws Exception {
        boolean descargado = ftpManager.descargarFichero(remoteFilePath, localFilePath);
        if (descargado) {
            System.out.println("Archivo descargado y descifrado: " + remoteFilePath + " a " + localFilePath);
        } else {
            System.out.println("Error al descargar: " + remoteFilePath);
        }
    }
}