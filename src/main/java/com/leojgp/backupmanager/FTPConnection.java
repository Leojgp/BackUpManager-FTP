package com.leojgp.backupmanager;

import java.io.*;
import java.nio.file.*;
import java.security.Key;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
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
    private static final String AES_PASSWORD = "PasswordInquebrantableAntiRobosYAntiCopiadas";
    private static final int AES_KEY_LENGTH = 32;

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
        System.out.println("Conexión establecida con " + SERVIDOR);
    }

    public void desconectar() throws IOException {
        if (clienteFTP.isConnected()) {
            clienteFTP.logout();
            clienteFTP.disconnect();
            System.out.println("Desconectado de " + SERVIDOR);
        }
    }

    public boolean subirFichero(String path) throws IOException {
        File ficheroLocal = new File(path);
        try (InputStream is = new FileInputStream(ficheroLocal)) {
            byte[] contenido = is.readAllBytes();
            String contenidoCifrado = null;
            try {
                contenidoCifrado = cifrar(new String(contenido), AES_PASSWORD);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            InputStream encryptedStream = new ByteArrayInputStream(contenidoCifrado.getBytes());
            boolean enviado = clienteFTP.storeFile(ficheroLocal.getName(), encryptedStream);
            if (enviado) {
                System.out.println("Subida exitosa: " + ficheroLocal.getName());
            } else {
                System.err.println("Fallo al subir: " + ficheroLocal.getName() + " - " + clienteFTP.getReplyString());
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

    public boolean descargarFichero(String ficheroRemoto, String pathLocal) throws IOException {
        File localFile = new File(pathLocal);
        try (OutputStream os = new FileOutputStream(localFile)) {
            boolean recibido = clienteFTP.retrieveFile(ficheroRemoto, os);
            if (recibido) {
                byte[] contenidoCifrado = Files.readAllBytes(localFile.toPath());
                String contenidoDescifrado = null;
                try {
                    contenidoDescifrado = descifrar(new String(contenidoCifrado), AES_PASSWORD);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                Files.write(localFile.toPath(), contenidoDescifrado.getBytes());
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

    public void crearFichero(String fileName) throws IOException {
        File dir = new File(LOCAL_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("Carpeta creada: " + LOCAL_DIR);
        }
        File fichero = new File(LOCAL_DIR + "\\" + fileName);
        try (FileWriter writer = new FileWriter(fichero)) {
            writer.write("Otura es el mejor pueblo de Granada");
            System.out.println("Archivo creado o modificado: " + fileName);
        }
    }

    public void modificarFichero(String fileName) throws IOException {
        File fichero = new File(LOCAL_DIR + "\\" + fileName);
        try (FileWriter writer = new FileWriter(fichero)) {
            writer.write("Contenido modificado del archivo");
            System.out.println("Archivo modificado localmente: " + fileName);
        }
    }

    public void borrarFicheroLocal(String fileName) {
        File fichero = new File(LOCAL_DIR + "\\" + fileName);
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

    private static String cifrar(String textoEnClaro, String password) throws Exception {
        Key key = new SecretKeySpec(password.getBytes(), 0, AES_KEY_LENGTH, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cipherText = cipher.doFinal(textoEnClaro.getBytes());
        return Base64.getEncoder().encodeToString(cipherText);
    }

    private static String descifrar(String textoCifrado, String password) throws Exception {
        Key key = new SecretKeySpec(password.getBytes(), 0, AES_KEY_LENGTH, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(textoCifrado));
        return new String(plainText);
    }

    public static void main(String[] args) {
        FTPConnection gestorFTP = new FTPConnection();
        try {
            gestorFTP.conectar();

            String fileName = "test.txt";
            String localPath = LOCAL_DIR + "\\" + fileName;

            // Creo y subo
            gestorFTP.crearFichero(fileName);
            gestorFTP.subirFichero(localPath);

            gestorFTP.descargarFichero(fileName, "C:\\Users\\leona\\Documents\\2DAM\\ProyectoProcesos\\downloadsFromServer\\test_downloaded.txt");

            gestorFTP.descargarFicheroSinDescifrado(fileName, "C:\\Users\\leona\\Documents\\2DAM\\ProyectoProcesos\\downloadsFromServer\\test_downloaded_sin_descifrar.txt");

            gestorFTP.eliminarFichero(fileName);
            gestorFTP.borrarFicheroLocal(fileName);

            gestorFTP.desconectar();

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
