package com.leojgp.backupmanager.localfile.threads;


import com.leojgp.backupmanager.localfile.LocalFileManager;

import java.io.IOException;

public class ReadBytesTask implements Runnable {

    private LocalFileManager localFileManager;
    private String fileName;

    public ReadBytesTask(LocalFileManager localFileManager, String fileName) {
        this.localFileManager = localFileManager;
        this.fileName = fileName;
    }

    @Override
    public void run() {
        try {
            localFileManager.leerBytesFichero(fileName);
        } catch (IOException e) {
            System.err.println("Error al leer el archivo " + fileName + " en hilo: " + Thread.currentThread().getName());
            e.printStackTrace();
        }
    }
}