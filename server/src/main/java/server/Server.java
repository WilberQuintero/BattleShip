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
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import com.mycompany.battleship.commons.IHandlerCommons;

/**
 * Servidor principal adaptado al estilo Dominó.
 * Implementa IServer para exponer sus funcionalidades al sistema HandlerChain.
 * También provee métodos para enviar mensajes a clientes.
 */
public class Server implements Runnable, IServer { // Implementa IServer

private final int port;
    private IHandlerCommons blackboard; // Usa la interfaz IHandlerCommons
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
     * Asigna la instancia del HandlerChain (como IHandler) al Server.
     * Debe llamarse antes de iniciar el hilo del servidor.
     * @param blackboard La instancia del BlackBoard que implementa IHandlerCommons.
     */
    public void setBlackboard(IHandlerCommons blackboard) {
        if (blackboard == null) {
            throw new IllegalArgumentException("La instancia de IHandler no puede ser nula.");
        }
        this.blackboard = blackboard;
        System.out.println("SERVER: IHandler asignado.");
    }

    // Método parsearEvento (Confirmado que funciona)
    private Evento parsearEvento(String linea) {
        if (linea == null || !linea.startsWith("EVENTO;")) {
            return null;
        }
        String contenido = linea.substring("EVENTO;".length());
        String[] partes = contenido.split(";");
        if (partes.length == 0) { return null; }

        String tipo = null;
        try {
            String[] tipoPart = partes[0].split("=", 2);
            if (!"TIPO".equals(tipoPart[0])) {
                 System.err.println("Formato inválido: El primer campo debe ser TIPO");
                 return null;
            }
            tipo = tipoPart[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Formato inválido para el TIPO del evento");
            return null;
        }

        Evento evento = new Evento(tipo);
        for (int i = 1; i < partes.length; i++) {
            try {
                String[] kv = partes[i].split("=", 2);
                if (kv.length == 2) {
                    evento.agregarDato(kv[0], kv[1]);
                } else { System.err.println("Formato inválido en campo: " + partes[i]); }
            } catch (Exception e) { System.err.println("Error procesando campo: " + partes[i]); }
        }
        // Log de depuración que ya tenías
        System.out.println("DEBUG [parsearEvento]: Evento creado -> Tipo: " + evento.getTipo() + ", Datos: " + evento.getDatos());
        return evento;
    }


    @Override
    public void run() {
        if (this.blackboard == null) {
             System.err.println("SERVER CRITICAL ERROR: HandlerChain no asignado antes de iniciar. Deteniendo.");
             return;
        }

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("SERVER: Iniciado (Estilo Dominó) en puerto " + port + ".");
            System.out.println("SERVER: Esperando conexiones...");

            while (running) {
                Socket clientSocket = null;
                try {
                    clientSocket = serverSocket.accept();
                    System.out.println("SERVER: Conexión aceptada de: " + clientSocket.getInetAddress().getHostAddress());

                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
                    clientWriters.put(clientSocket, writer);
                    System.out.println("SERVER: Writer creado y guardado para cliente " + clientSocket.getInetAddress().getHostAddress());

                    final Socket currentClientSocket = clientSocket;
                    new Thread(() -> {
                        System.out.println("SERVER [Hilo Cliente " + currentClientSocket.getPort() + "]: Iniciado.");
                        try (Scanner scanner = new Scanner(currentClientSocket.getInputStream(), StandardCharsets.UTF_8)) {
                            while (running && scanner.hasNextLine()) {
                                String linea = scanner.nextLine();
                                System.out.println("SERVER [Hilo Cliente " + currentClientSocket.getPort() + "]: Mensaje recibido: " + linea);

                                Evento evento = parsearEvento(linea);

                                // --- ¡¡ESTA ES LA PARTE CORREGIDA Y CLAVE!! ---
                                if (evento != null && blackboard != null) {
                                    System.out.println("SERVER [Hilo Cliente " + currentClientSocket.getPort() + "]: Enviando evento '" + evento.getTipo() + "' al Blackboard.");
                                    blackboard.enviarEventoBlackBoard(currentClientSocket, evento); // Envía el evento parseado
                                } else if (evento == null) {
                                    System.err.println("SERVER [Hilo Cliente " + currentClientSocket.getPort() + "]: Mensaje no pudo ser parseado: " + linea);
                                }
                                // --------------------------------------------

                            }
                             System.out.println("SERVER [Hilo Cliente " + currentClientSocket.getPort() + "]: Bucle while terminado (hasNextLine=" + scanner.hasNextLine() + ", running="+running+").");

                        } catch (IOException e) {
                             if (running) { /* ... (logging) ... */ }
                        } catch (NoSuchElementException e){
                             if (running) { /* ... (logging) ... */ }
                        } catch (Exception e) {
                             if (running) { /* ... (logging) ... */ }
                        } finally {
                             // --- ASEGURA LA LIMPIEZA Y NOTIFICACIÓN ---
                             System.out.println("SERVER [Hilo Cliente " + currentClientSocket.getPort() + "]: Hilo terminando. Notificando desconexión...");
                             clienteDesconectado(currentClientSocket);
                             // ----------------------------------------
                        }
                    }, "ClientHandler-" + clientSocket.getPort()).start();

                    // --- EVENTO CONECTAR_USUARIO_SERVER ELIMINADO ---

                } catch (SocketException se) { /* ... (manejo de error) ... */ }
                  catch (IOException e) { /* ... (manejo de error) ... */ }
                  catch (Exception e) { /* ... (manejo de error) ... */ }
            } // Fin while(running)
        } catch (IOException e) { /* ... (manejo de error) ... */ }
          finally { /* ... (cierre limpio) ... */ }
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