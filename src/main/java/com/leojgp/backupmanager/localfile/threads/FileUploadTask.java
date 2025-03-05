package com.leojgp.backupmanager.localfile.threads;

import com.leojgp.backupmanager.remotefile.FTPManager;

public class FileUploadTask implements Runnable {

    private FTPManager ftpManager;
    private String localPath;

    public FileUploadTask(FTPManager ftpManager, String localPath) {
        this.ftpManager = ftpManager;
        this.localPath = localPath;
    }

    @Override
    public void run() {
        try {
            System.out.println("Subiendo archivo: " + localPath + " en hilo: " + Thread.currentThread().getName());
            ftpManager.subirFichero(localPath);
        } catch (Exception e) {
            System.err.println("Error subiendo archivo: " + localPath);
            e.printStackTrace();
        }
    }
}