package com.leojgp.backupmanager.localfile;

import com.leojgp.backupmanager.config.FTPConfiguration;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LocalFileManager {

    private FTPConfiguration config;

    public LocalFileManager(FTPConfiguration config) {
        this.config = config;
    }

    public void crearFichero(String fileName, String content) throws IOException {
        File dir = new File(config.getLocalDir());
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("Carpeta creada: " + config.getLocalDir());
        }
        File fichero = new File(config.getLocalDir() + "\\" + fileName);
        try (FileWriter writer = new FileWriter(fichero)) {
            writer.write(content);
            System.out.println("Archivo creado o modificado: " + fileName);
        }
    }

    public void modificarFichero(String fileName, String content) throws IOException {
        File fichero = new File(config.getLocalDir() + "\\" + fileName);
        try (FileWriter writer = new FileWriter(fichero)) {
            writer.write(content);
            System.out.println("Archivo modificado localmente: " + fileName);
        }
    }

    public void borrarFicheroLocal(String fileName) {
        File fichero = new File(config.getLocalDir() + "\\" + fileName);
        if (fichero.exists()) {
            if (fichero.delete()) {
                System.out.println("Archivo borrado localmente: " + fileName);
            } else {
                System.err.println("No pude borrar localmente: " + fileName);
            }
        } else {
            System.out.println("El archivo " + fileName + " no existe localmente");
        }
    }

    public byte[] leerBytesFichero(String fileName) throws IOException {
        return Files.readAllBytes(Paths.get(config.getLocalDir(), fileName));
    }

    public void escribirBytesFichero(String fileName, byte[] content) throws IOException {
        Files.write(Paths.get(config.getLocalDir(), fileName), content);
    }
}