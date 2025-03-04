/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
            return enviado;
        }
    }

    public boolean descargarFichero(String ficheroRemoto, String pathLocal) throws IOException {
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(pathLocal))) {
            boolean recibido = clienteFTP.retrieveFile(ficheroRemoto, os);
            return recibido;
        }
    }

    public static void main(String[] args) {
        FTPConnection gestorFTP = new FTPConnection();
        try {
            gestorFTP.conectar();
            System.out.println("Conectado");

            boolean subido = gestorFTP.subirFichero("C:\\FTP\\test.txt");
            if (subido) {
                System.out.println("Fichero subido correctamente");
            } else {
                System.err.println("Ha ocurrido un error al intentar subir el fichero");
            }

            boolean descargado = gestorFTP.descargarFichero("test.txt", "C:/downloads/test.txt");
            if (descargado) {
                System.out.println("Fichero descargado correctamente");
            } else {
                System.err.println("Ha ocurrido un error al intentar descargar el fichero.");
            }

            gestorFTP.desconectar();
            System.out.println("Desconectado");
        } catch (IOException e) {
            System.err.println("Ha ocurrido un error: " + e.getMessage());
        }
    }
}