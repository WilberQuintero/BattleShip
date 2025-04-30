/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.blackboard;

/**
 *
 * @author caarl
 */




import com.mycompany.battleship.commons.Evento;
import com.mycompany.battleship.commons.IBlackboard;
import com.mycompany.battleship.commons.IServer;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
// Quitamos los Map<> de Dominio por ahora, nos centramos en la conexión

/**
 * El Blackboard adaptado al estilo del ejemplo de Dominó.
 * Almacena el estado principal (clientes conectados) y despacha eventos a las KS.
 * Implementa IBlackboard para exponer su funcionalidad al Server.
 */
public class BlackBoard implements IBlackboard { // Implementa la interfaz

    // --- Estado Principal ---
    // Lista sincronizada de sockets de clientes cuya conexión fue procesada por una KS
    private final List<Socket> clientesConectados;
    private final Map<String, Map<String, Object>> salas = new ConcurrentHashMap<>();
    // Podríamos añadir Map<Socket, PlayerData> o Map<String, Game> aquí más adelante

    // --- Componentes del Sistema ---
    private Controller controller; // Se asigna vía setter
    private final IServer server; // Usa la interfaz IServer
    private final List<IKnowledgeSource> knowledgeSources;

    /**
     * Constructor del BlackBoard.
     * @param server La instancia del servidor (como IServer).
     */
    public BlackBoard(IServer server) { // Recibe IServer
        if (server == null) {
            throw new IllegalArgumentException("La instancia de IServer no puede ser nula.");
        }
        this.server = server;
        this.clientesConectados = Collections.synchronizedList(new ArrayList<>());
        this.knowledgeSources = new ArrayList<>();
        // El controller se asigna después con setController
    }
        public void agregarSala(String id, Map<String, Object> datosSala) {
            salas.put(id, datosSala);
            System.out.println("BLACKBOARD: Sala " + id + " registrada.");
        }
    /**
     * Asigna el Controller al BlackBoard. Llama a registrarFuentesDeConocimiento.
     * @param controller La instancia del Controller.
     */
    public void setController(Controller controller) {
        if (controller == null) {
             throw new IllegalArgumentException("La instancia del Controller no puede ser nula.");
        }
        if (this.controller != null) {
            System.out.println("BLACKBOARD WARN: Intentando reasignar el Controller.");
            return; // Evitar registrar KS múltiples veces si se llama de nuevo
        }
        this.controller = controller;
        System.out.println("BLACKBOARD: Controller asignado.");
        registrarFuentesDeConocimiento(); // Registrar KS ahora que todo está listo
    }

    /**
     * Instancia y registra las Knowledge Sources.
     */
    private void registrarFuentesDeConocimiento() {
        if (this.controller == null || this.server == null) {
             System.err.println("BLACKBOARD ERROR: No se pueden registrar KS sin Controller o IServer.");
             return;
        }
        // Crear y registrar KS para conexiones (pasando interfaces y controller)
        knowledgeSources.add(new ConnectionKS(this, this.server, this.controller));

        // TODO: Registrar otras KS (Disparos, Chat, EstadoJuego, etc.) aquí...
        // knowledgeSources.add(new DisparoKS(this, this.server, this.controller));

        System.out.println("BLACKBOARD: Fuentes de conocimiento registradas (" + knowledgeSources.size() + ")");
        knowledgeSources.forEach(ks -> System.out.println("  - " + ks.getClass().getSimpleName()));
    }

    // --- Implementación de IBlackboard ---

    @Override
    public void enviarEventoBlackBoard(Socket cliente, Evento evento) {
        if (evento == null) {
            System.err.println("BLACKBOARD ERROR: Evento nulo recibido en enviarEventoBlackBoard.");
            return;
        }
         // Si el evento es de desconexión, el socket 'cliente' podría ser null
        String logCliente = (cliente != null) ? cliente.getInetAddress().getHostAddress() : "N/A";
        System.out.println("BLACKBOARD: Recibido evento '" + evento.getTipo() + "' para cliente: " + logCliente);

        if (knowledgeSources.isEmpty()) {
             System.err.println("BLACKBOARD WARN: No hay Knowledge Sources registradas.");
             return;
        }

        boolean eventoProcesado = false;
        List<IKnowledgeSource> sourcesSnapshot = new ArrayList<>(knowledgeSources); // Copia para iteración segura

        for (IKnowledgeSource ks : sourcesSnapshot) {
            try {
                if (ks.puedeProcesar(evento)) {
                    System.out.println("BLACKBOARD: Despachando evento '" + evento.getTipo() + "' a " + ks.getClass().getSimpleName());
                    ks.procesarEvento(cliente, evento); // Pasar el socket y el evento
                    eventoProcesado = true;
                    // Considerar un 'break;' si solo una KS debe manejar el evento primario
                }
            } catch (Exception e) {
                System.err.println("BLACKBOARD ERROR: Excepción al procesar evento por " + ks.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (!eventoProcesado) {
            System.err.println("BLACKBOARD WARN: Ninguna KS pudo procesar el evento: " + evento.getTipo());
        } else {
            System.out.println("BLACKBOARD: Evento '" + evento.getTipo() + "' procesado por al menos una KS.");
        }
    }

    // --- Métodos de Callback y Estado ---

    /**
     * Callback usado por las KS para indicar finalización (estilo Dominó).
     */
    public void respuestaFuenteC(Socket cliente, Evento eventoRespuesta) {
         String logCliente = (cliente != null) ? cliente.getInetAddress().getHostAddress() : "N/A";
         System.out.println("BLACKBOARD: KS reporta finalización para cliente " + logCliente + ". Evento: " + eventoRespuesta.getTipo());
         // Aquí se podría añadir lógica si el BB necesita reaccionar a respuestas de KS
    }

    /**
     * Añade un socket a la lista de clientes cuya conexión fue procesada.
     * Es llamado por la KS correspondiente (ConnectionKS).
     */
    public void agregarClienteSocket(Socket clienteSocket) {
        if (clienteSocket != null && !clientesConectados.contains(clienteSocket)) {
            // Sincronización implícita por usar Collections.synchronizedList
            boolean added = clientesConectados.add(clienteSocket);
            if (added) {
                 System.out.println("BLACKBOARD: Socket de cliente " + clienteSocket.getInetAddress().getHostAddress() + " registrado como activo.");
                 // Notificar al controller que la lista de clientes cambió
                 if (controller != null) {
                     // Usamos un tipo de evento específico para esto
                     controller.notificarCambio("CLIENTE_REGISTRADO");
                 }
            }
        } else if (clienteSocket != null) {
             System.out.println("BLACKBOARD WARN: Intento de agregar cliente ya existente: " + clienteSocket.getInetAddress().getHostAddress());
        }
    }

     /**
      * Elimina un socket de la lista de clientes activos.
      * Debería ser llamado por una KS que maneje desconexiones.
      * @param clienteSocket El socket a eliminar.
      */
     public void removerClienteSocket(Socket clienteSocket) {
         if (clienteSocket != null) {
              // Sincronización implícita
             boolean removed = clientesConectados.remove(clienteSocket);
             if (removed) {
                  System.out.println("BLACKBOARD: Socket de cliente " + clienteSocket.getInetAddress().getHostAddress() + " eliminado de activos.");
                  if (controller != null) {
                      // Notificar cambio si es relevante para otros clientes
                      controller.notificarCambio("CLIENTE_ELIMINADO");
                  }
             }
         }
     }

public boolean existeSala(String idSala) {
    return salas.containsKey(idSala);
}
     /**
     * Obtiene una copia de la lista de sockets de clientes conectados.
     */
    public List<Socket> getClientesConectados() {
        synchronized (clientesConectados) { // Sincronizar para asegurar copia consistente
            return new ArrayList<>(clientesConectados);
        }
    }

    // --- Getters para dependencias (usados por KSs) ---
    public IServer getServer() { // Devuelve la interfaz
        return server;
    }

    public Controller getController() {
        return controller;
    }

    // --- Inspección ---
     public void inspect() {
        System.out.println("\n--- Inspección del Blackboard (Estilo Dominó) ---");
        System.out.println("Fuentes de Conocimiento Registradas: " + knowledgeSources.size());
        knowledgeSources.forEach(ks -> System.out.println("  - " + ks.getClass().getSimpleName()));
        System.out.println("Clientes Conectados (" + clientesConectados.size() + "):");
        // Sincronizar al iterar para obtener una vista consistente
        synchronized (clientesConectados) {
            for (Socket s : clientesConectados) {
                if (s!=null && s.getInetAddress()!=null){
                     System.out.println("  - " + s.getInetAddress().getHostAddress() + ":" + s.getPort() + " (Closed: "+s.isClosed()+")");
                } else {
                     System.out.println("  - Socket nulo o sin dirección en la lista.");
                }
            }
        }
        System.out.println("------------------------------------------\n");
    }
}