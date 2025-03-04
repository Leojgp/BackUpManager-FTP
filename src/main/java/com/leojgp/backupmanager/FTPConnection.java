package com.leojgp.backupmanager;

import java.io.*;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class FTPConnection {
    private FTPClient clienteFTP;
    private static final String SERVIDOR = "192.168.1.26";
    private static final int PUERTO = 21;
    private static final String USUARIO = "ftpadminLeo";
    private static final String PASSWORD = "password123";
    private static final String LOCAL_DIR = "C:\\Users\\leona\\Documents\\2DAM\\ProyectoProcesos\\syncFTPServer";

    public FTPConnection() {
        clienteFTP = new FTPClient();
    }

    public void conectar() throws IOException {
        clienteFTP.connect(SERVIDOR, PUERTO);
        int respuesta = clienteFTP.getReplyCode();
        if (!FTPReply.isPositiveCompletion(respuesta)) {
            clienteFTP.disconnect();
            throw new IOException("Error al conectar con el servidor FTP");
        }
        boolean credencialesOK = clienteFTP.login(USUARIO, PASSWORD);
        if (!credencialesOK) {
            throw new IOException("Error al conectar con el servidor FTP. Credenciales incorrectas.");
        }
        clienteFTP.setFileType(FTP.BINARY_FILE_TYPE);
    }

    public void desconectar() throws IOException {
        if (clienteFTP.isConnected()) {
            clienteFTP.logout();
            clienteFTP.disconnect();
        }
    }

    public boolean subirFichero(String path) throws IOException {
        File ficheroLocal = new File(path);
        try (InputStream is = new FileInputStream(ficheroLocal)) {
            boolean enviado = clienteFTP.storeFile(ficheroLocal.getName(), is);
            if (enviado) {
                System.out.println("He subido el archivo " + ficheroLocal.getName() + " al servidor");
            } else {
                System.err.println("No pude subir el archivo " + ficheroLocal.getName());
            }
            return enviado;
        }
    }

    public void crearFichero(String fileName) throws IOException {
        File dir = new File(LOCAL_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("He creado la carpeta " + LOCAL_DIR + " porque no existía");
        }
        File fichero = new File(LOCAL_DIR + "\\" + fileName);
        if (!fichero.exists()) {
            try (FileWriter writer = new FileWriter(fichero)) {
                writer.write("Este es un archivo de prueba para el FTP");
                System.out.println("He creado el archivo " + fileName + " en " + LOCAL_DIR);
            }
        } else {
            System.out.println("El archivo " + fileName + " ya existe, no lo vuelvo a crear");
        }
    }

    public static void main(String[] args) {
        FTPConnection gestorFTP = new FTPConnection();
        try {
            gestorFTP.conectar();
            System.out.println("Me he conectado al servidor FTP");

            String fileName = "test.txt";
            gestorFTP.crearFichero(fileName);
            gestorFTP.subirFichero(LOCAL_DIR + "\\" + fileName);

            gestorFTP.desconectar();
            System.out.println("Me he desconectado del servidor FTP");
        } catch (IOException e) {
            System.err.println("Ha ocurrido un error: " + e.getMessage());
        }
    }
}