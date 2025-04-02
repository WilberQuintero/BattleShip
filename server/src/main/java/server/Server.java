/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

/**
 *
 * @author caarl
 */
import com.mycompany.battleship.commons.Evento;
import com.mycompany.battleship.commons.IBlackboard;
import com.mycompany.battleship.commons.IServer;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servidor principal adaptado al estilo Dominó.
 * Acepta conexiones y las pasa al BlackBoard como eventos a través de la interfaz IBlackboard.
 * Implementa IServer para exponer sus funcionalidades al sistema Blackboard.
 * También provee métodos para enviar mensajes a clientes.
 */
public class Server implements Runnable, IServer { // Implementa IServer

    private final int port;
    private IBlackboard blackboard; // Usa la interfaz IBlackboard
    private ServerSocket serverSocket;
    private volatile boolean running = true;
    // Mapa para mantener writers de clientes activos
    private final Map<Socket, PrintWriter> clientWriters = new ConcurrentHashMap<>();

    /**
     * Constructor.
     * @param port Puerto en el que escuchará el servidor.
     */
    public Server(int port) {
        this.port = port;
    }

    /**
     * Asigna la instancia del BlackBoard (como IBlackboard) al Server.
     * Debe llamarse antes de iniciar el hilo del servidor.
     * @param blackboard La instancia del BlackBoard que implementa IBlackboard.
     */
    public void setBlackboard(IBlackboard blackboard) {
        if (blackboard == null) {
            throw new IllegalArgumentException("La instancia de IBlackboard no puede ser nula.");
        }
        this.blackboard = blackboard;
        System.out.println("SERVER: IBlackboard asignado.");
    }

    @Override
    public void run() {
        if (this.blackboard == null) {
             System.err.println("SERVER CRITICAL ERROR: IBlackboard no asignado antes de iniciar. Deteniendo.");
             return;
        }

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("SERVER: Iniciado (Estilo Dominó) en puerto " + port + ".");
            System.out.println("SERVER: Esperando conexiones...");

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("SERVER: Conexión aceptada de: " + clientSocket.getInetAddress().getHostAddress());

                    // Crear PrintWriter para este cliente
                    try {
                         PrintWriter writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
                         clientWriters.put(clientSocket, writer);
                         System.out.println("SERVER: Writer creado para cliente " + clientSocket.getInetAddress().getHostAddress());
                    } catch (IOException e) {
                         System.err.println("SERVER ERROR: No se pudo crear PrintWriter para " + clientSocket.getInetAddress().getHostAddress() + ". Cerrando socket.");
                         try { clientSocket.close(); } catch (IOException ioex) { /* Ignorar */ }
                         continue; // Saltar este cliente
                    }

                    // Crear el Evento de conexión
                    Evento eventoConexion = new Evento("CONECTAR_USUARIO_SERVER"); // Usa la clase Evento de commons

                    // Enviar el Evento al BlackBoard usando la interfaz
                    blackboard.enviarEventoBlackBoard(clientSocket, eventoConexion);

                } catch (SocketException se) {
                    if (!running) {
                        System.out.println("SERVER: ServerSocket cerrado intencionalmente.");
                    } else {
                         System.err.println("SERVER ERROR: Error de Socket al esperar conexión: " + se.getMessage());
                         // Considerar logging más robusto
                    }
                } catch (IOException e) {
                     if (running) {
                         System.err.println("SERVER ERROR: Error de E/S al aceptar conexión: " + e.getMessage());
                         // Considerar logging más robusto
                     }
                 } catch (Exception e) { // Captura genérica para errores inesperados
                      System.err.println("SERVER ERROR: Error inesperado en el bucle principal: " + e.getMessage());
                      e.printStackTrace(); // Loguear stacktrace completo
                 }
             } // Fin while
        } catch (IOException e) {
            if (running) {
                System.err.println("SERVER CRITICAL ERROR: No se pudo iniciar el servidor en el puerto " + port + ". " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            System.out.println("SERVER: Deteniendo servidor...");
            closeServerResources();
            System.out.println("SERVER: Servidor detenido completamente.");
        }
    }

    // --- Implementación de métodos de IServer ---

    @Override
    public void enviarMensajeACliente(Socket cliente, String mensaje) {
        if (cliente == null || mensaje == null) {
            System.err.println("SERVER WARN: Intento de enviar mensaje nulo o a cliente nulo.");
            return;
        }
        PrintWriter writer = clientWriters.get(cliente);
        if (writer != null) {
            // System.out.println("SERVER: Enviando a " + cliente.getInetAddress().getHostAddress() + ": " + mensaje);
            writer.println(mensaje);
            if(writer.checkError()) { // Verifica si ocurrió un error durante println
                 System.err.println("SERVER ERROR: Error al enviar mensaje a " + cliente.getInetAddress().getHostAddress() + ". Puede que el cliente se haya desconectado.");
                 // Considerar manejar la desconexión aquí
                 clienteDesconectado(cliente);
            }
        } else {
             System.err.println("SERVER WARN: No se encontró PrintWriter para enviar mensaje a " + cliente.getInetAddress().getHostAddress());
             // Podría ser que el cliente se desconectó justo antes.
        }
    }

    @Override
    public void enviarMensajeATodos(String mensaje) {
        if (mensaje == null) return;
        System.out.println("SERVER: Enviando a todos ("+ clientWriters.size() +"): " + mensaje);
        // Iterar sobre una copia de las keys para evitar ConcurrentModificationException si clienteDesconectado es llamado
        for (Socket cliente : clientWriters.keySet()) {
            enviarMensajeACliente(cliente, mensaje);
        }
    }

    @Override
    public void enviarEventoACliente(Socket cliente, Evento evento) {
        if (evento == null) return;
        // Convertir evento a string (ej. formato simple, JSON sería mejor)
        // TODO: Implementar una serialización robusta (ej. JSON)
        StringBuilder sb = new StringBuilder("EVENTO;TIPO=");
        sb.append(evento.getTipo());
        for (Map.Entry<String, Object> entry : evento.getDatos().entrySet()) {
            sb.append(";").append(entry.getKey()).append("=").append(entry.getValue());
        }
        enviarMensajeACliente(cliente, sb.toString());
    }

    @Override
    public void clienteDesconectado(Socket cliente) {
        if (cliente == null) return;
        System.out.println("SERVER: Manejando desconexión de: " + cliente.getInetAddress().getHostAddress());
        PrintWriter writer = clientWriters.remove(cliente);
        if (writer != null) {
            writer.close();
        }
        try {
            if (!cliente.isClosed()) {
                cliente.close();
            }
        } catch (IOException e) {
             System.err.println("SERVER ERROR: Error cerrando socket de cliente desconectado: " + e.getMessage());
        }
        // Notificar al blackboard sobre la desconexión para que actualice el estado del juego
        if (blackboard != null) {
             Evento eventoDesconexion = new Evento("DESCONECTAR_USUARIO");
             // Pasamos null como cliente "origen" porque el cliente ya no está activo
             // La información relevante (quién se desconectó) debe deducirse
             // por la KS que procese este evento, quizás buscando el socket en sus datos.
             // O podríamos añadir el socket como dato al evento si fuera útil.
             eventoDesconexion.agregarDato("socketDesconectado", cliente.toString()); // Añadir info del socket
             blackboard.enviarEventoBlackBoard(null, eventoDesconexion);
        }
    }

    // --- Métodos de Control del Servidor ---

    public void stopServer() {
        System.out.println("SERVER: Solicitud de detención recibida.");
        running = false; // Detener el bucle de accept

        // Cerrar todas las conexiones de clientes activas
        System.out.println("SERVER: Cerrando conexiones de clientes...");
        // Iterar sobre una copia de las keys para evitar problemas al modificar el mapa en clienteDesconectado
        List<Socket> clientesACerrar = new ArrayList<>(clientWriters.keySet());
        for (Socket cliente : clientesACerrar) {
            // No llamar a clienteDesconectado aquí para evitar bucles si stopServer es llamado desde allí
             PrintWriter writer = clientWriters.remove(cliente);
             if (writer != null) writer.close();
             try {
                 if (!cliente.isClosed()) cliente.close();
             } catch (IOException e) { /* Ignorar */ }
        }
        clientWriters.clear();

        // Cerrar el ServerSocket para interrumpir el accept()
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                 System.out.println("SERVER: Cerrando ServerSocket...");
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("SERVER ERROR: Error al cerrar el ServerSocket: " + e.getMessage());
        }
         System.out.println("SERVER: Señal de detención procesada.");
    }

    private void closeServerResources() {
        // La limpieza principal se hace en stopServer
        System.out.println("SERVER: Recursos del servidor liberados.");
    }

    // --- Métodos STUB (a implementar según lógica de Battleship) ---
    // Estos métodos ahora son parte de la interfaz IServer y deben estar aquí,
    // pero su lógica interna puede variar o incluso delegar a otros componentes.

    // Nota: Estos métodos podrían no ser necesarios si el Controller/KS usan
    // enviarMensajeACliente/enviarMensajeATodos para comunicar los cambios.
    // Mantenerlos si el estilo Dominó los requiere explícitamente.

    public void registrarJugadores(Map<String, Object> jugadores) {
        System.out.println("SERVER STUB: registrarJugadores llamado. Datos: " + jugadores);
        // Implementación futura: podría usar esta info para mapear ID a Socket, etc.
    }
     public void registrarSalas(Map<String, Object> salas) {
        System.out.println("SERVER STUB: registrarSalas llamado. Datos: " + salas);
    }
     public void actualizarSala(Object sala) {
        System.out.println("SERVER STUB: actualizarSala llamado. Datos: " + sala);
    }
     public void registrarPartidas(Map<String, Object> partidas) {
        System.out.println("SERVER STUB: registrarPartidas llamado. Datos: " + partidas);
    }
     public void enviarMensajeATodosLosClientes(String mensaje) { // Método legacy del controller de Domino
         enviarMensajeATodos(mensaje);
     }
}