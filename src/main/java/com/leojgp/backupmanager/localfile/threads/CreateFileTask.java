package com.leojgp.backupmanager.localfile.threads;

import com.leojgp.backupmanager.localfile.LocalFileManager;

import java.io.IOException;

public class CreateFileTask implements Runnable {

    private LocalFileManager localFileManager;
    private String fileName;
    private String content;

    public CreateFileTask(LocalFileManager localFileManager, String fileName, String content) {
        this.localFileManager = localFileManager;
        this.fileName = fileName;
        this.content = content;
    }

    @Override
    public void run() {
        try {
            localFileManager.crearFichero(fileName, content);
        } catch (IOException e) {
            System.err.println("Error al crear el archivo " + fileName + " en hilo: " + Thread.currentThread().getName());
            e.printStackTrace();
        }
    }
}