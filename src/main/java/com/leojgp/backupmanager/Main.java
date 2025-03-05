package com.leojgp.backupmanager;

import com.leojgp.backupmanager.config.FTPConfiguration;
import com.leojgp.backupmanager.localfile.LocalFileManager;
import com.leojgp.backupmanager.localfile.threads.FileUploadTask;
import com.leojgp.backupmanager.remotefile.FTPManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Main {

    public static void main(String[] args) {
        FTPConfiguration config = new FTPConfiguration();
        FTPManager ftpManager = new FTPManager(config);
        LocalFileManager localFileManager = new LocalFileManager(config);

        int numThreads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        Map<Path, Long> lastModifiedMap = new HashMap<>();

        try {
            ftpManager.conectar();

            Path localDirectory = Paths.get("C:\\Users\\leona\\Documents\\2DAM\\ProyectoProcesos\\syncFTPServer");

            System.out.println("Iniciando monitorización de cambios en: " + localDirectory);

            while (true) {
                File[] files = localDirectory.toFile().listFiles();
                if (files != null) {
                    Map<Path, Long> currentFiles = new HashMap<>();
                    for (File file : files) {
                        if (file.isFile()) {
                            Path filePath = file.toPath();
                            long lastModified = file.lastModified();

                            currentFiles.put(filePath, lastModified);

                            if (!lastModifiedMap.containsKey(filePath) || lastModified > lastModifiedMap.get(filePath)) {
                                System.out.println("Cambio detectado en: " + filePath);
                                executor.execute(new FileUploadTask(ftpManager, filePath.toString()));
                                lastModifiedMap.put(filePath, lastModified);
                            }
                        }
                    }

                    // Detectar eliminaciones
                    List<Path> filesToDelete = new ArrayList<>();
                    for (Path filePath : lastModifiedMap.keySet()) {
                        if (!currentFiles.containsKey(filePath)) {
                            System.out.println("Eliminación detectada en: " + filePath);
                            executor.execute(() -> {
                                try {
                                    ftpManager.eliminarFichero(filePath.getFileName().toString());
                                    System.out.println("Archivo eliminado del servidor: " + filePath.getFileName());
                                } catch (Exception e) {
                                    System.err.println("Error al eliminar archivo del servidor: " + filePath.getFileName());
                                    e.printStackTrace();
                                }
                            });
                            filesToDelete.add(filePath);
                        }
                    }
                    for (Path filePath : filesToDelete) {
                        lastModifiedMap.remove(filePath);
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                ftpManager.desconectar();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}