/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.servercomunicacion;

/**
 *
 * @author caarl
 */


import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap; // Para parsear datos de respuesta
import java.util.Map;     // Para parsear datos de respuesta
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;



/**
 * Clase responsable de manejar la comunicación (socket) con el servidor backend
 * desde la aplicación cliente. Actúa como un Facade/Proxy para la capa de Vista.
 */
public class ServerComunicacion {

    private final String host;
    private final int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ExecutorService listenerExecutor; // Para el hilo que escucha al servidor
    private volatile boolean conectado = false; // Indica si estamos activamente conectados
    private volatile boolean intentandoConectar = false;

    private ServerEventListener listener; // La Vista o Controlador que escuchará los eventos

    public ServerComunicacion(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Asigna el listener que recibirá notificaciones del servidor.
     * @param listener La instancia que implementa ServerEventListener (normalmente la Vista o su Controlador).
     */
    public void setListener(ServerEventListener listener) {
        this.listener = listener;
    }

    /**
     * Intenta establecer la conexión con el servidor.
     * Inicia un hilo para escuchar respuestas si tiene éxito.
     * Es no bloqueante (la conexión ocurre en segundo plano).
     */
    public synchronized void conectar() {
        if (conectado || intentandoConectar) {
            System.out.println("[Comunicacion] Ya conectado o intentando conectar.");
            return;
        }
        intentandoConectar = true;
        System.out.println("[Comunicacion] Intentando conectar a " + host + ":" + port + "...");

        // Realizar conexión en un hilo separado para no bloquear UI
        new Thread(() -> {
            try {
                desconectar(); // Asegura cerrar conexión previa si existiera
                socket = new Socket(host, port);
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                conectado = true;
                intentandoConectar = false;
                System.out.println("[Comunicacion] Conexión establecida.");
                if (listener != null) {
                    // Ejecutar en el hilo de la UI si es necesario (ej. SwingUtilities.invokeLater)
                    listener.onConectado();
                }
                startListeningThread(); // Iniciar hilo listener DESPUÉS de conectar

            } catch (UnknownHostException e) {
                System.err.println("[Comunicacion] Host desconocido: " + host);
                if (listener != null) listener.onError("Host '" + host + "' no encontrado.");
                limpiarRecursos();
            } catch (IOException e) {
                System.err.println("[Comunicacion] No se pudo conectar al servidor: " + e.getMessage());
                if (listener != null) listener.onError("No se pudo conectar al servidor. ¿Está encendido?");
                limpiarRecursos();
            } finally {
                 intentandoConectar = false; // Termina el intento
            }
        }).start();
    }

    /**
     * Cierra la conexión y detiene el hilo listener.
     */
    public synchronized void desconectar() {
        if (!conectado && !intentandoConectar) return; // Ya desconectado o nunca se intentó

        System.out.println("[Comunicacion] Iniciando proceso de desconexión...");
        conectado = false; // Señal para detener el listener
        intentandoConectar = false; // Cancelar intento si estaba en curso

        // Detener el hilo listener de forma segura
        if (listenerExecutor != null && !listenerExecutor.isShutdown()) {
            listenerExecutor.shutdown(); // Permite que termine tareas actuales (leer línea)
            try {
                 // Espera un poco a que cierre limpiamente
                if (!listenerExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                    listenerExecutor.shutdownNow(); // Forzar si no cierra
                }
            } catch (InterruptedException e) {
                listenerExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // Cerrar streams y socket
        limpiarRecursos();

        System.out.println("[Comunicacion] Desconexión completada.");
        if (listener != null) {
             // Podrías querer pasar un motivo, pero por ahora es genérico
            listener.onDesconectado("Desconexión iniciada por el cliente.");
        }
    }

    /**
     * Cierra los recursos de red (streams, socket) de forma segura.
     */
    private void limpiarRecursos() {
        try { if (out != null) out.close(); } catch (Exception e) { /* Ignorar */ }
        try { if (in != null) in.close(); } catch (Exception e) { /* Ignorar */ }
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException e) { /* Ignorar */ }
        socket = null;
        out = null;
        in = null;
        conectado = false;
    }


    /**
     * Inicia un hilo dedicado para escuchar continuamente los mensajes del servidor.
     */
    private void startListeningThread() {
         listenerExecutor = Executors.newSingleThreadExecutor();
         listenerExecutor.submit(() -> {
             System.out.println("[Comunicacion] Hilo listener iniciado para recibir del servidor.");
             try {
                 String lineaDelServidor;
                 // El bucle termina si 'conectado' es false o si readLine devuelve null (desconexión)
                 while (conectado && (lineaDelServidor = in.readLine()) != null) {
                     System.out.println("[Comunicacion] Recibido: " + lineaDelServidor);
                     // Procesar el mensaje en el mismo hilo listener
                     procesarMensajeServidor(lineaDelServidor);
                     // Si necesitaras actualizar UI, deberías usar el mecanismo
                     // apropiado (ej. SwingUtilities.invokeLater, Platform.runLater)
                     // dentro de los métodos del listener.
                 }
             } catch (IOException e) {
                 if (conectado) { // Si el error no fue por una desconexión intencional
                     System.err.println("[Comunicacion] Error leyendo del servidor: " + e.getMessage());
                     if (listener != null) listener.onError("Se perdió la conexión con el servidor.");
                 }
             } finally {
                  System.out.println("[Comunicacion] Hilo listener terminado.");
                  // Si el hilo termina (por error o desconexión del server), limpiar
                  if (conectado) { // Si terminó inesperadamente
                     desconectar(); // Llama a desconectar para notificar al listener y limpiar
                  }
             }
         });
    }

    /**
     * Parsea un mensaje recibido del servidor y notifica al listener.
     * @param mensaje El mensaje crudo recibido del servidor.
     */
    private void procesarMensajeServidor(String mensaje) {
        // Implementa aquí tu lógica para parsear los mensajes que ENVÍA el servidor.
        // Asumiremos que el servidor también envía mensajes con formato "EVENTO;TIPO=...;K=V..."
        // Puedes adaptar esto al formato real que use tu servidor para las respuestas.

        if (mensaje == null || !mensaje.startsWith("EVENTO;")) {
             System.out.println("[Comunicacion] Mensaje del servidor no reconocido: " + mensaje);
             // Quizás notificar un mensaje genérico
             // if (listener != null) listener.onMensajeGenerico(mensaje);
             return;
        }
         String contenido = mensaje.substring("EVENTO;".length());
         String[] partes = contenido.split(";");
         if (partes.length == 0) return;

         String tipo = null;
         Map<String, Object> datos = new HashMap<>();
         try {
             String[] tipoPart = partes[0].split("=", 2);
             if ("TIPO".equals(tipoPart[0])) tipo = tipoPart[1]; else return;
         } catch (Exception e) { System.err.println("[Com] Error parseando TIPO: "+partes[0]); return; }

         for (int i = 1; i < partes.length; i++) {
             try { String[] kv = partes[i].split("=", 2); if (kv.length == 2) datos.put(kv[0], kv[1]); } catch (Exception e) { /* Ignorar campo inválido */ }
         }

        // Notificar al listener apropiado
        if (listener != null && tipo != null) {
            // Usar el método genérico del listener
            listener.onMensajeServidor(tipo, datos);

            // O si prefieres métodos específicos en el listener:
            /*
            switch (tipo) {
                case "SALA_CREADA_OK":
                    listener.onSalaCreada((String)datos.get("idSala"));
                    break;
                case "UNIDO_OK":
                    listener.onUnidoASala(true, (String)datos.get("mensaje"));
                    break;
                case "ERROR_CREAR_SALA":
                case "ERROR_UNIRSE_SALA":
                    listener.onError("Error de sala: " + datos.get("error"));
                    break;
                // ... otros casos ...
                default:
                    System.out.println("[Comunicacion] Evento del servidor tipo '" + tipo + "' recibido pero no manejado específicamente.");
                    break;
            }
            */
        }
    }

    // --- Métodos Públicos para ser llamados por la Vista/Controlador ---

    /**
     * Envía una solicitud al servidor para crear una sala.
     * @param idSala El nombre/ID deseado para la sala.
     */
    public void crearSala(String idSala) {
        if (!validarEstadoConexion("crear sala")) return;
        if (idSala == null || idSala.isBlank()) {
             if (listener != null) listener.onError("Nombre de sala inválido.");
             return;
        }
        // Construye el mensaje según el protocolo acordado
        String mensaje = "EVENTO;TIPO=CREAR_SALA;idSala=" + idSala.trim();
        enviarMensaje(mensaje);
    }

    /**
     * Envía una solicitud al servidor para unirse a una sala existente.
     * @param idSala El ID de la sala a la que unirse.
     */
    public void unirseASala(String idSala) {
         if (!validarEstadoConexion("unirse a sala")) return;
         if (idSala == null || idSala.isBlank()) {
             if (listener != null) listener.onError("ID de sala inválido.");
             return;
        }
        String mensaje = "EVENTO;TIPO=UNIRSE_SALA;idSala=" + idSala.trim();
        enviarMensaje(mensaje);
    }

    /**
     * Envía un evento de disparo al servidor.
     * @param x Coordenada X.
     * @param y Coordenada Y.
     */
    public void enviarDisparo(int x, int y) {
         if (!validarEstadoConexion("enviar disparo")) return;
         String mensaje = "EVENTO;TIPO=DISPARAR;x=" + x + ";y=" + y; // Asegúrate que el servidor espere "DISPARAR"
         enviarMensaje(mensaje);
    }

    /**
     * Envía un mensaje de chat genérico.
     * @param texto Mensaje a enviar.
     */
    public void enviarMensajeChat(String texto) {
        if (!validarEstadoConexion("enviar chat")) return;
        if (texto == null || texto.isBlank()) return;
        String mensaje = "EVENTO;TIPO=CHAT;mensaje=" + texto; // Asegúrate que el servidor espere "CHAT"
        enviarMensaje(mensaje);
    }

    // Añadir más métodos para otras acciones: COLOCAR_BARCOS, ABANDONAR_PARTIDA, etc.

    /**
     * Método privado para enviar un mensaje formateado al servidor.
     * Verifica si la conexión está activa.
     * @param mensaje El mensaje completo a enviar.
     */
    private synchronized void enviarMensaje(String mensaje) {
        if (!conectado || out == null) {
            System.err.println("[Comunicacion] No conectado. No se puede enviar: " + mensaje);
            if (listener != null) listener.onError("No estás conectado al servidor.");
            return; // No enviar si no estamos conectados
        }
         System.out.println("[Comunicacion] Enviando: " + mensaje);
         out.println(mensaje);
         // Verificar error inmediatamente después de enviar
         if (out.checkError()) {
             System.err.println("[Comunicacion] Error detectado al enviar mensaje. Desconectando.");
             if (listener != null) listener.onError("Error de conexión al enviar datos.");
             // Llamar a desconectar en otro hilo para evitar deadlock si desconectar necesita sincronización
             new Thread(this::desconectar).start();
         }
    }

     /**
     * Verifica si la conexión está activa antes de intentar enviar un mensaje.
     * Notifica al listener si no está conectado.
     * @param accion Descripción de la acción que se intenta realizar.
     * @return true si está conectado, false en caso contrario.
     */
    private boolean validarEstadoConexion(String accion) {
        if (!conectado || out == null) {
             System.err.println("[Comunicacion] No conectado. No se puede " + accion + ".");
             if (listener != null) listener.onError("No estás conectado para poder " + accion + ".");
             return false;
        }
        return true;
    }

    /**
     * Verifica si el cliente está actualmente conectado al servidor.
     * @return true si hay una conexión activa, false en caso contrario.
     */
    public boolean isConectado() {
         return conectado && socket != null && socket.isConnected() && !socket.isClosed();
    }
}