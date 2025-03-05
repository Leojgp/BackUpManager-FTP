package com.leojgp.backupmanager.localfile.threads;

import com.leojgp.backupmanager.localfile.LocalFileManager;

public class DeleteFileTask implements Runnable {

    private LocalFileManager localFileManager;
    private String fileName;

    public DeleteFileTask(LocalFileManager localFileManager, String fileName) {
        this.localFileManager = localFileManager;
        this.fileName = fileName;
    }

    @Override
    public void run() {
        localFileManager.borrarFicheroLocal(fileName);
    }
}