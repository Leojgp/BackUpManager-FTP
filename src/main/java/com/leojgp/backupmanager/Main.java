package com.leojgp.backupmanager;

import com.leojgp.backupmanager.config.FTPConfiguration;
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

/**
 * Clase principal que monitoriza un directorio local y sincroniza los cambios con un servidor FTP.
 * Utiliza multihilo para mejorar la eficiencia de la sincronización.
 */
public class Main {

    public static void main(String[] args) {
        FTPConfiguration config = new FTPConfiguration();
        FTPManager ftpManager = new FTPManager(config);

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
                                executor.execute(() -> {
                                    try {
                                        System.out.println("Subiendo archivo: " + filePath.toString() + " en hilo: " + Thread.currentThread().getName());
                                        ftpManager.subirFichero(filePath.toString());
                                    } catch (Exception e) {
                                        System.err.println("Error al subir archivo al servidor: " + filePath.getFileName());
                                        e.printStackTrace();
                                    }
                                });;
                                lastModifiedMap.put(filePath, lastModified);
                            }
                        }
                    }

                    Thread.sleep(1000);
                    List<Path> filesToDelete = new ArrayList<>();
                    for (Path filePath : lastModifiedMap.keySet()) {
                        if (!currentFiles.containsKey(filePath)) {
                            System.out.println("Eliminación detectada en: " + filePath);
                            executor.execute(() -> {
                                try {
                                    ftpManager.eliminarFichero(filePath.getFileName().toString());
                                    System.out.println("Archivo eliminado del servidor: " + filePath.getFileName());
                                    lastModifiedMap.remove(filePath);
                                } catch (Exception e) {
                                    System.err.println("Error al eliminar archivo del servidor: " + filePath.getFileName());
                                    e.printStackTrace();
                                }
                            });
                            filesToDelete.add(filePath);
                        }
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
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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