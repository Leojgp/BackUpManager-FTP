package com.leojgp.backupmanager.remotefile;

import com.leojgp.backupmanager.config.FTPConfiguration;
import com.leojgp.backupmanager.encryption.EncryptionManager;
import java.io.*;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FTPManager {

    private FTPClient clienteFTP;
    private FTPConfiguration config;
    private EncryptionManager encryptionManager;
    private Set<String> archivosEliminados = new HashSet<>();
    public FTPManager(FTPConfiguration config) {
        this.clienteFTP = new FTPClient();
        this.config = config;
        this.encryptionManager = new EncryptionManager(config);
    }

    public FTPFile[] listarArchivos(String directorio) throws IOException {
        return clienteFTP.listFiles(directorio);
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
            String remoteFileName = ficheroLocal.getName();
            //Registro historico
            if(clienteFTP.listFiles( "history/"+ remoteFileName).length == 1){
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                String timestamp = dateFormat.format(new Date());
                String historyFileName = "history/" + remoteFileName + "_" + timestamp;
                clienteFTP.rename(remoteFileName, historyFileName);
                System.out.println("Archivo movido a history: " + historyFileName);
            }

            System.out.println("Subiendo archivo: " + remoteFileName);
            boolean enviado = clienteFTP.storeFile(remoteFileName, encryptedStream);

            if (enviado) {
                System.out.println("Subida exitosa: " + remoteFileName);
            } else {
                System.err.println("Fallo al subir: " + remoteFileName + " - Código: " + clienteFTP.getReplyCode());
            }
            return enviado;
        }
    }

    public boolean eliminarFichero(String nombreFichero) throws Exception {
        if (archivosEliminados.contains(nombreFichero)) {
            System.out.println("Archivo ya intentado eliminar: " + nombreFichero);
            return true;
        }
            try {
                if (clienteFTP.listFiles("history/" + nombreFichero).length == 0) {
                    clienteFTP.makeDirectory("history");
                    clienteFTP.rename(nombreFichero, "history/" + nombreFichero);
                    System.out.println("Archivo movido a history: history/" + nombreFichero);
                } else {
                    System.out.println("Archivo ya se encuentra en history: history/" + nombreFichero);
                }
                if (clienteFTP.listFiles(nombreFichero).length > 0) {
                    if (clienteFTP.deleteFile(nombreFichero)) {
                        System.out.println("Archivo eliminado del servidor: " + nombreFichero);
                    } else {
                        System.err.println("Fallo al eliminar: " + nombreFichero + " - " + clienteFTP.getReplyString());
                    }
                } else {
                    System.out.println("El archivo " + nombreFichero + " ya no existe en la carpeta raíz.");
                }
                archivosEliminados.add(nombreFichero);
                return true;
            } catch (SocketException e) {
                System.err.println("Error al eliminar " + e.getMessage());
                try {
                    clienteFTP.disconnect();
                    conectar();
                } catch (IOException ex) {
                    System.err.println("Error al reconectar: " + ex.getMessage());
                }
            }
        System.err.println("Fallo al eliminar después de varios intentos: " + nombreFichero);
        return false;
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
}