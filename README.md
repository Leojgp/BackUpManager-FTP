# BackUpManager ⚙️

[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/) [![FTP](https://img.shields.io/badge/FTP-Protocol-blue)](https://es.wikipedia.org/wiki/Protocolo_de_transferencia_de_archivos)

## Descripción ️

BackUpManager es una aplicación Java que sincroniza una carpeta local con un servidor FTP, con seguridad y gestión de versiones. La aplicación monitoriza un directorio local y sincroniza los cambios con el servidor FTP, incluyendo la subida de nuevos archivos, la actualización de archivos modificados y la eliminación de archivos borrados localmente. Además, permite descargar y descifrar todos los archivos almacenados en el servidor FTP, manteniendo un registro histórico de los cambios.

## Funcionalidades 

* **Sincronización Automática:** Monitoriza un directorio local y sincroniza los cambios con un servidor FTP en tiempo real. Detecta y aplica cambios como creación, modificación y eliminación de archivos.
* **Seguridad:** Aplica cifrado AES para proteger los archivos almacenados en el servidor FTP, con la opción de cifrar todo tipo de archivos.
* **Histórico de Cambios:** Mantiene un registro completo de cambios en el servidor FTP, incluyendo archivos borrados y versiones anteriores de archivos modificados, almacenados en una carpeta "history".
* **Descarga y Descifrado:** Proporciona funcionalidades para descargar y descifrar archivos individuales y sus versiones, con manejo de errores y mensajes informativos.

## Concurrencia 

* **ExecutorService:** Se utiliza un `ExecutorService` con un pool de hilos fijo para ejecutar tareas de sincronización de forma concurrente.
* **Tareas:**
    * `FileUploadTask`: Sube archivos al servidor FTP.
    * Tareas anónimas: Eliminan archivos del servidor FTP y descargan/descifran archivos del directorio "history".
* **Beneficios:**
    * Mejora el rendimiento al realizar operaciones en paralelo.
    * Evita bloqueos y latencias al no esperar a que las operaciones se completen secuencialmente.

## Otros Puntos Clave 

* **Monitorización de Cambios:** La aplicación utiliza un bucle infinito para monitorizar los cambios en el directorio local.
* **Gestión de Versiones:** Se mantiene un registro completo de cambios en el servidor FTP, incluyendo archivos borrados y versiones anteriores de archivos modificados.
* **Configuración:** La configuración del servidor FTP y el directorio local se realiza a través de la clase `FTPConfiguration` y la clase `Main`, respectivamente.

## Uso 

1.  Configura `FTPConfiguration.java` con los detalles de tu servidor FTP.
2.  Configura el directorio local que deseas monitorizar en la clase `Main.java`.
3.  Ejecuta `Main.java` para iniciar la monitorización, sincronización, descarga y descifrado.

## Dependencias 

* `commons-net`
