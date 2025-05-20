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
import javax.swing.SwingUtilities;



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
    // Dentro de ServerComunicacion.java

    public synchronized void conectar() {
        // --- RECOMENDADO: Verificar estado al inicio ---
        if (conectado) {
            System.out.println("[Comunicacion][conectar] Ya está conectado.");
            // Opcional: notificar al listener que ya está conectado si es útil
            // if(listener != null) listener.onConectado();
            return;
        }
        if (intentandoConectar) { // Añade esta variable 'intentandoConectar' si no la tienes
            System.out.println("[Comunicacion][conectar] Ya hay un intento de conexión en curso.");
            return;
        }
        // --- FIN VERIFICACIÓN ---

        intentandoConectar = true; // Marcar que estamos intentando
        System.out.println("[Comunicacion] Intentando conectar a " + host + ":" + port + "...");

        // Realizar conexión en un hilo separado
        new Thread(() -> {
            System.out.println("[Comunicacion][Thread Connect]: Iniciando intento de conexión...");
            Socket tempSocket = null; // Socket temporal
            PrintWriter tempOut = null;
            BufferedReader tempIn = null;

            try {
                // --- ¡¡NO LLAMAR A desconectar() AQUÍ!! ---
                // desconectar(); // <--- ¡¡ELIMINAR O COMENTAR ESTA LÍNEA!!

                System.out.println("[Comunicacion][Thread Connect]: Llamando a new Socket(...)");
                tempSocket = new Socket(host, port);
                System.out.println("[Comunicacion][Thread Connect]: Socket creado: " + tempSocket);

                System.out.println("[Comunicacion][Thread Connect]: Creando PrintWriter...");
                tempOut = new PrintWriter(new OutputStreamWriter(tempSocket.getOutputStream(), StandardCharsets.UTF_8), true);
                System.out.println("[Comunicacion][Thread Connect]: PrintWriter creado.");

                System.out.println("[Comunicacion][Thread Connect]: Creando BufferedReader...");
                tempIn = new BufferedReader(new InputStreamReader(tempSocket.getInputStream(), StandardCharsets.UTF_8));
                System.out.println("[Comunicacion][Thread Connect]: BufferedReader creado.");

                // --- Asignar a variables de instancia y marcar como conectado SOLO si todo fue bien ---
                // Usar bloque synchronized para asegurar atomicidad al cambiar estado y streams
                synchronized(this) {
                    // Asignar solo si aún estamos en estado "intentando conectar"
                    if (intentandoConectar) {
                        this.socket = tempSocket;
                        this.out = tempOut;
                        this.in = tempIn;
                        this.conectado = true; // Marcar como conectado
                        System.out.println("[Comunicacion][Thread Connect]: Estado 'conectado' establecido a true.");
                    } else {
                         // Si 'intentandoConectar' es false, significa que desconectar() fue llamado mientras conectábamos.
                         System.out.println("[Comunicacion][Thread Connect]: Conexión abortada durante el proceso. Cerrando socket temporal.");
                         try { if (tempSocket != null) tempSocket.close(); } catch (IOException e) {}
                         // No notificar onConectado
                         return; // Salir del hilo
                    }
                } // Fin synchronized


                // Notificar al listener (fuera del bloque synchronized)
                if (listener != null) {
                     System.out.println("[Comunicacion][Thread Connect]: Notificando listener.onConectado()...");
                     // Usar invokeLater si el listener modifica UI directamente
                     // SwingUtilities.invokeLater(() -> listener.onConectado());
                     listener.onConectado(); // Llamada directa si el listener maneja hilo
                }

                // Iniciar hilo listener DESPUÉS de asignar variables y notificar
                startListeningThread();

            } catch (UnknownHostException e) {
                 System.err.println("[Comunicacion][Thread Connect] ERROR: Host desconocido: " + host);
                 if (listener != null) SwingUtilities.invokeLater(()->listener.onError("Host '" + host + "' no encontrado."));
                 limpiarRecursos(); // Limpia si hay error
            } catch (IOException e) {
                 System.err.println("[Comunicacion][Thread Connect] ERROR: No se pudo conectar al servidor: " + e.getMessage());
                 if (listener != null) SwingUtilities.invokeLater(()->listener.onError("No se pudo conectar al servidor."));
                 limpiarRecursos(); // Limpia si hay error
            } catch (Exception e) { // Otros errores inesperados
                 System.err.println("[Comunicacion][Thread Connect] ERROR INESPERADO: " + e.getMessage());
                 e.printStackTrace();
                 if (listener != null) SwingUtilities.invokeLater(()->listener.onError("Error inesperado durante la conexión."));
                 limpiarRecursos(); // Limpia si hay error
            } finally {
                 System.out.println("[Comunicacion][Thread Connect]: Bloque finally alcanzado.");
                 intentandoConectar = false; // Marcar que el intento terminó (sea éxito o fracaso)
            }
        }).start(); // Iniciar el hilo de conexión
    }
    /**
     * Cierra la conexión y detiene el hilo listener.
     */
    public synchronized void desconectar() {
        System.out.println("[Comunicacion][desconectar]: Iniciando desconexión. Estado actual conectado=" + conectado);
    if (!conectado && !intentandoConectar) {
         System.out.println("[Comunicacion][desconectar]: Ya desconectado o no se intentó conectar.");
         return;
    }

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

        System.out.println("[Comunicacion][desconectar]: Desconexión completada.");
     // La notificación al listener se hace al final
     if (listener != null) {
         // SwingUtilities.invokeLater(() -> listener.onDesconectado("Desconexión procesada.")); // Cuidado con llamar a UI desde aquí si ya estás en hilo UI
         listener.onDesconectado("Desconexión procesada."); // O llamar directamente
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
     System.out.println("[Comunicacion][Thread Listener]: Hilo listener iniciado.");
     try {
         String lineaDelServidor;
         System.out.println("[Comunicacion][Thread Listener]: Entrando al bucle while (conectado=" + conectado + ")");
         while (conectado && (lineaDelServidor = in.readLine()) != null) {
             System.out.println("[Comunicacion][Thread Listener]: Línea recibida: " + lineaDelServidor);
             procesarMensajeServidor(lineaDelServidor);
         }
         // Si sale del bucle, significa que conectado es false o readLine devolvió null
         System.out.println("[Comunicacion][Thread Listener]: Saliendo del bucle while (conectado=" + conectado + ")");
     } catch (IOException e) {
         if (conectado) {
             System.err.println("[Comunicacion][Thread Listener]: IOException en readLine (conectado=true): " + e.getMessage());
             if (listener != null) SwingUtilities.invokeLater(()->listener.onError("Se perdió la conexión con el servidor."));
         } else {
              System.out.println("[Comunicacion][Thread Listener]: IOException esperada en readLine debido a desconexión (conectado=false).");
         }
     } catch (Exception e) { // Otros errores
         System.err.println("[Comunicacion][Thread Listener]: ERROR INESPERADO en listener: " + e.getMessage());
         e.printStackTrace();
         if (conectado && listener != null) SwingUtilities.invokeLater(()->listener.onError("Error inesperado en la comunicación con el servidor."));
     } finally {
          System.out.println("[Comunicacion][Thread Listener]: Bloque finally alcanzado (conectado=" + conectado + ")");
          // Si el hilo termina y aún se suponía que estábamos conectados, forzar desconexión
          if (conectado) {
               System.out.println("[Comunicacion][Thread Listener]: Terminó inesperadamente, llamando a desconectar...");
               desconectar(); // Llama a desconectar para limpieza y notificación
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
    System.out.println("[Comunicacion][enviarMensaje]: Intentando enviar. Conectado=" + conectado + ", out=" + (out != null) + ", socket=" + (socket != null ? !socket.isClosed() : "null"));
    if (!conectado || out == null || socket == null || socket.isClosed()) { // Chequeo más robusto
        System.err.println("[Comunicacion][enviarMensaje]: No conectado o stream/socket inválido. No se puede enviar: " + mensaje);
        if (listener != null) listener.onError("No estás conectado al servidor."); // Notificar error
        return;
    }
     System.out.println("[Comunicacion][enviarMensaje]: Enviando: " + mensaje);
     out.println(mensaje);
     boolean errorOcurrido = out.checkError(); // Guarda el resultado
     System.out.println("[Comunicacion][enviarMensaje]: out.checkError() devolvió: " + errorOcurrido);
     if (errorOcurrido) {
         System.err.println("[Comunicacion][enviarMensaje]: Error detectado por checkError(). Desconectando.");
         if (listener != null) listener.onError("Error de conexión al enviar datos.");
         // Llamar a desconectar en otro hilo para evitar deadlock si 'desconectar' es synchronized y este método también lo es.
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
    
    /**
     * NUEVO MÉTODO PÚBLICO:
     * Envía un evento de juego genérico o preformateado al servidor.
     * Internamente utiliza el método privado enviarMensaje.
     * @param eventoCompleto El string completo del evento a enviar.
     */
    public void enviarEventoJuego(String eventoCompleto) {
        if (!validarEstadoConexion("enviar evento de juego")) {
            // validarEstadoConexion ya notifica al listener si hay error
            return;
        }
        // Llama a tu método privado existente para enviar el mensaje
        enviarMensaje(eventoCompleto);
    }
    
    

    /**
     * Envía el nombre de usuario al servidor para registrarlo.
     * Asume que la conexión ya está establecida.
     * @param nombreUsuario El nombre a registrar.
     */
    public void enviarRegistroUsuario(String nombreUsuario) {
         // Usamos validarEstadoConexion para asegurar que estamos conectados
         if (!validarEstadoConexion("registrar usuario")) return;
         if (nombreUsuario == null || nombreUsuario.isBlank()) {
              System.err.println("[Comunicacion] Nombre de usuario para registro es inválido.");
              // Notificar error localmente
              if(listener != null) listener.onError("Nombre de usuario inválido para registrar.");
              return;
         }
        // Formato del nuevo evento
        String mensaje = "EVENTO;TIPO=REGISTRAR_USUARIO;nombre=" + nombreUsuario;
        enviarMensaje(mensaje);
    }

   
}