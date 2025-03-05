package com.leojgp.backupmanager.localfile.threads;

import com.leojgp.backupmanager.localfile.LocalFileManager;

import java.io.IOException;

public class WriteBytesTask implements Runnable {

    private LocalFileManager localFileManager;
    private String fileName;
    private byte[] content;

    public WriteBytesTask(LocalFileManager localFileManager, String fileName, byte[] content) {
        this.localFileManager = localFileManager;
        this.fileName = fileName;
        this.content = content;
    }

    @Override
    public void run() {
        try {
            localFileManager.escribirBytesFichero(fileName, content);
        } catch (IOException e) {
            System.err.println("Error al escribir el archivo " + fileName + " en hilo: " + Thread.currentThread().getName());
            e.printStackTrace();
        }
    }
}