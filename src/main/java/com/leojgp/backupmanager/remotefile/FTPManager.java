package com.leojgp.backupmanager.remotefile;

import com.leojgp.backupmanager.config.FTPConfiguration;
import com.leojgp.backupmanager.encryption.EncryptionManager;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.leojgp.backupmanager.localfile.LocalFileManager;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class FTPManager {

    private FTPClient clienteFTP;
    private FTPConfiguration config;
    private EncryptionManager encryptionManager;
    private LocalFileManager localFileManager;

    public FTPManager(FTPConfiguration config) {
        this.clienteFTP = new FTPClient();
        this.config = config;
        this.encryptionManager = new EncryptionManager(config);
        this.localFileManager = new LocalFileManager(config);
    }

    public void conectar() throws IOException {
        clienteFTP.connect(config.getServidor(), config.getPuerto());
        int respuesta = clienteFTP.getReplyCode();
        if (!FTPReply.isPositiveCompletion(respuesta)) {
            clienteFTP.disconnect();
            throw new IOException("Error al conectar con el servidor FTP");
        }
        boolean credencialesOK = clienteFTP.login(config.getUsuario(), config.getPassword());
        if (!credencialesOK) {
            clienteFTP.disconnect();
            throw new IOException("Error al conectar con el servidor FTP. Credenciales incorrectas.");
        }
        clienteFTP.setFileType(FTP.BINARY_FILE_TYPE);
        System.out.println("Conexión establecida con " + config.getServidor());
    }

    public void desconectar() throws IOException {
        if (clienteFTP.isConnected()) {
            clienteFTP.logout();
            clienteFTP.disconnect();
            System.out.println("Desconectado de " + config.getServidor());
        }
    }

    public boolean subirFichero(String path) throws Exception {
        File ficheroLocal = new File(path);
        try (InputStream is = new FileInputStream(ficheroLocal)) {
            byte[] contenido = is.readAllBytes();
            String contenidoCifrado = encryptionManager.cifrar(new String(contenido));
            InputStream encryptedStream = new ByteArrayInputStream(contenidoCifrado.getBytes());

            System.out.println("Subiendo archivo: " + ficheroLocal.getName());
            boolean enviado = clienteFTP.storeFile(ficheroLocal.getName(), encryptedStream);

            if (enviado) {
                System.out.println("Subida exitosa: " + ficheroLocal.getName());
            } else {
                System.err.println("Fallo al subir: " + ficheroLocal.getName() + " - Código: " + clienteFTP.getReplyCode());
            }
            return enviado;
        }
    }

    public boolean eliminarFichero(String fileName) throws IOException {
        boolean eliminado = clienteFTP.deleteFile(fileName);
        if (eliminado) {
            System.out.println("Eliminado del servidor: " + fileName);
        } else {
            System.err.println("Fallo al eliminar: " + fileName + " - " + clienteFTP.getReplyString());
        }
        return eliminado;
    }

    public boolean descargarFichero(String ficheroRemoto, String pathLocal) throws Exception {
        File localFile = new File(pathLocal);
        try (OutputStream os = new FileOutputStream(localFile)) {
            boolean recibido = clienteFTP.retrieveFile(ficheroRemoto, os);
            if (recibido) {
                System.out.println("Fichero " + ficheroRemoto + " descargado");
                byte[] contenidoCifrado = Files.readAllBytes(Paths.get(pathLocal));
                String contenidoDescifrado = encryptionManager.descifrar(new String(contenidoCifrado));
                Files.write(Paths.get(pathLocal), contenidoDescifrado.getBytes());
                System.out.println("Descargado y descifrado exitosamente: " + ficheroRemoto + " a " + pathLocal);
            } else {
                System.err.println("Fallo al descargar: " + ficheroRemoto + " - " + clienteFTP.getReplyString());
            }
            return recibido;
        }
    }

    public boolean descargarFicheroSinDescifrado(String ficheroRemoto, String pathLocal) throws IOException {
        File localFile = new File(pathLocal);
        File parentDir = localFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
            System.out.println("Carpeta de descarga creada: " + parentDir.getPath());
        }
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(localFile))) {
            boolean recibido = clienteFTP.retrieveFile(ficheroRemoto, os);
            if (recibido) {
                System.out.println("Descargado exitosamente: " + ficheroRemoto + " a " + pathLocal);
            } else {
                System.err.println("Fallo al descargar: " + ficheroRemoto + " - " + clienteFTP.getReplyString());
            }
            return recibido;
        }
    }
}