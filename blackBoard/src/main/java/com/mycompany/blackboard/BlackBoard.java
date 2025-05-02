/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.blackboard; // Asegúrate que el paquete sea el correcto

// --- Importaciones Correctas ---
import ks.CrearSalaKS;
import ks.UnirseSalaKS;
import ks.ConnectionKS;
import ks.IniciarPartidaKS;
import com.mycompany.battleship.commons.Evento;
import com.mycompany.battleship.commons.IBlackboard;
import com.mycompany.battleship.commons.IServer;


import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap; // Añadido para getDatosSala
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ks.RegistrarUsuarioKS;



/**
 * El Blackboard adaptado al estilo del ejemplo de Dominó.
 * Almacena el estado principal (clientes conectados, salas, nombres) y despacha eventos a las KS.
 * Implementa IBlackboard para exponer su funcionalidad al Server y KSs.
 */
public class BlackBoard implements IBlackboard {

    // --- Estado Principal ---
    private final List<Socket> clientesConectados;
    private final Map<String, Map<String, Object>> salas;
    private final Map<String, Socket> socketPorNombre;
    private final Map<Socket, String> nombrePorSocket;

    // --- Componentes del Sistema ---
    private Controller controller;
    private final IServer server;
    private final List<IKnowledgeSource> knowledgeSources; // Usar el nombre de interfaz correcto

    /**
     * Constructor del BlackBoard.
     * @param server La instancia del servidor (como IServer).
     */
    public BlackBoard(IServer server) {
        if (server == null) {
            throw new IllegalArgumentException("La instancia de IServer no puede ser nula.");
        }
        this.server = server;
        this.clientesConectados = Collections.synchronizedList(new ArrayList<>());
        this.salas = new ConcurrentHashMap<>();
        this.socketPorNombre = new ConcurrentHashMap<>();
        this.nombrePorSocket = new ConcurrentHashMap<>();
        this.knowledgeSources = new ArrayList<>();
    }

    /**
     * Asigna el Controller y registra las KS.
     */
    public void setController(Controller controller) {
        if (controller == null) {
             throw new IllegalArgumentException("La instancia del Controller no puede ser nula.");
        }
        if (this.controller != null) {
            System.out.println("BLACKBOARD WARN: Intentando reasignar el Controller.");
            return;
        }
        this.controller = controller;
        System.out.println("BLACKBOARD: Controller asignado.");
        registrarFuentesDeConocimiento();
    }

    /**
     * Instancia y registra TODAS las Knowledge Sources necesarias.
     */
   private void registrarFuentesDeConocimiento() {
        if (this.controller == null || this.server == null) {
             System.err.println("BLACKBOARD ERROR: No se pueden registrar KS sin Controller o IServer.");
             return;
        }
        System.out.println("BLACKBOARD: Registrando Fuentes de Conocimiento...");
        knowledgeSources.clear();

        // Asegúrate de que los nombres de clase y el constructor sean correctos
        knowledgeSources.add(new ConnectionKS(this, this.server, this.controller));
        knowledgeSources.add(new CrearSalaKS(this, this.server, this.controller));
        knowledgeSources.add(new UnirseSalaKS(this, this.server, this.controller));
        knowledgeSources.add(new RegistrarUsuarioKS(this, this.server, this.controller));

        // TODO: Añadir futuras KS aquí...

        System.out.println("BLACKBOARD: Fuentes de conocimiento registradas (" + knowledgeSources.size() + ")");
        knowledgeSources.forEach(ks -> System.out.println("  - " + ks.getClass().getSimpleName()));
    }

    // --- Implementación COMPLETA de IBlackboard ---

    @Override
    public void enviarEventoBlackBoard(Socket cliente, Evento evento) {
        // Log de depuración añadido previamente
        System.out.println("DEBUG [BB.enviarEvento]: Método ingresado. Tipo=" + (evento != null ? evento.getTipo() : "NULL EVENTO"));

        if (evento == null) {
            System.err.println("BLACKBOARD ERROR: Evento nulo recibido en enviarEventoBlackBoard.");
            return;
        }
        String logCliente = (cliente != null && cliente.getInetAddress() != null) ? cliente.getInetAddress().getHostAddress() : "N/A";
        System.out.println("BLACKBOARD: Recibido evento '" + evento.getTipo() + "' para cliente: " + logCliente);

        if (knowledgeSources.isEmpty()) {
             System.err.println("BLACKBOARD WARN: No hay Knowledge Sources registradas.");
             return;
        }

        boolean eventoProcesado = false;
        // Usar el nombre de interfaz correcto aquí
        List<IKnowledgeSource> sourcesSnapshot = new ArrayList<>(knowledgeSources);

        System.out.println("DEBUG [BB.enviarEvento]: Iniciando bucle de despacho a KSs (" + sourcesSnapshot.size() + " KSs)...");

        // Usar el nombre de interfaz correcto aquí
        for (IKnowledgeSource ks : sourcesSnapshot) {
            try {
                System.out.println("DEBUG [BB.enviarEvento]: Verificando KS: " + ks.getClass().getSimpleName());
                if (ks.puedeProcesar(evento)) {
                    System.out.println("BLACKBOARD: Despachando evento '" + evento.getTipo() + "' a " + ks.getClass().getSimpleName());
                    ks.procesarEvento(cliente, evento);
                    eventoProcesado = true;
                }
            } catch (Exception e) {
                System.err.println("BLACKBOARD ERROR: EXCEPCIÓN AL PROCESAR por " + ks.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("DEBUG [BB.enviarEvento]: Bucle de despacho finalizado.");

        if (!eventoProcesado) {
            System.err.println("BLACKBOARD WARN: Ninguna KS pudo procesar el evento: " + evento.getTipo());
        } else {
            System.out.println("BLACKBOARD: Evento '" + evento.getTipo() + "' procesado por al menos una KS.");
        }
    }

    @Override
    public boolean existeSala(String idSala) {
        // Implementación simple
        return idSala != null && salas.containsKey(idSala);
    }

    @Override
    public void agregarSala(String id, Map<String, Object> datosSala) {
        // Implementación que evita sobrescribir y loguea
        if (id != null && datosSala != null) {
             if (salas.putIfAbsent(id, datosSala) == null) { // putIfAbsent devuelve null si la clave NO existía
                  System.out.println("BLACKBOARD: Sala '" + id + "' agregada al mapa.");
             } else {
                  System.out.println("BLACKBOARD WARN: Intento de agregar sala que ya existe: '" + id + "'. No se sobrescribió.");
             }
        } else {
             System.err.println("BLACKBOARD ERROR: Intento de agregar sala con ID o datos nulos.");
        }
    }

    @Override
    public Map<String, Object> getDatosSala(String idSala) {
        // Implementación que devuelve copia
        Map<String, Object> datosOriginales = salas.get(idSala);
        if (datosOriginales != null) {
            return new HashMap<>(datosOriginales); // Devuelve copia superficial
        }
        return null;
    }

    @Override
    public void actualizarDatosSala(String idSala, Map<String, Object> nuevosDatosSala) {
        // Implementación que verifica existencia antes de actualizar
        if (idSala != null && nuevosDatosSala != null) {
            if (salas.containsKey(idSala)) {
                 salas.put(idSala, nuevosDatosSala); // Sobrescribe
                 System.out.println("BLACKBOARD: Datos actualizados para sala '" + idSala + "'.");
                 // Notificar si es necesario
                 // if (controller != null) controller.notificarCambio("SALA_ACTUALIZADA;" + idSala);
            } else {
                 System.err.println("BLACKBOARD WARN: Intento de actualizar sala inexistente '" + idSala + "'.");
            }
        } else {
             System.err.println("BLACKBOARD ERROR: ID de sala o nuevos datos nulos al intentar actualizar.");
        }
    }

    @Override
    public void agregarClienteSocket(Socket clienteSocket) {
        // Implementación existente
        if (clienteSocket != null && !clientesConectados.contains(clienteSocket)) {
            boolean added = clientesConectados.add(clienteSocket);
            if (added) {
                 System.out.println("BLACKBOARD: Socket de cliente " + clienteSocket.getInetAddress().getHostAddress() + " registrado como activo.");
                 if (controller != null) {
                     controller.notificarCambio("CLIENTE_REGISTRADO");
                 }
            }
        } else if (clienteSocket != null) {
             System.out.println("BLACKBOARD WARN: Intento de agregar cliente ya existente: " + clienteSocket.getInetAddress().getHostAddress());
        }
    }

    @Override
    public void removerClienteSocket(Socket clienteSocket) {
        // Implementación existente (con limpieza de nombres)
        if (clienteSocket != null) {
              String nombreRemovido = nombrePorSocket.remove(clienteSocket);
              if (nombreRemovido != null) {
                   socketPorNombre.remove(nombreRemovido);
                   System.out.println("BLACKBOARD: Mapeo de nombre '" + nombreRemovido + "' eliminado para socket desconectado.");
              } else {
                   System.out.println("BLACKBOARD WARN: Socket desconectado no tenía un nombre registrado.");
              }
              boolean removed = clientesConectados.remove(clienteSocket);
              if (removed) {
                  System.out.println("BLACKBOARD: Socket de cliente " + clienteSocket.getInetAddress().getHostAddress() + " eliminado de activos.");
                  if (controller != null) {
                      controller.notificarCambio("CLIENTE_ELIMINADO"); // Podría incluir nombreRemovido
                  }
              } else {
                    // Si no estaba en clientesConectados, quizás ya fue removido
                    System.out.println("BLACKBOARD INFO: Socket " + clienteSocket.getInetAddress().getHostAddress() + " no encontrado en lista clientesConectados durante remoción (posiblemente ya removido).");
              }
        }
    }

    @Override
    public List<Socket> getClientesConectados() {
        // Implementación existente
        synchronized (clientesConectados) {
            return new ArrayList<>(clientesConectados);
        }
    }

    @Override
    public void respuestaFuenteC(Socket cliente, Evento eventoRespuesta) {
        // Implementación existente
         String logCliente = (cliente != null && cliente.getInetAddress() != null) ? cliente.getInetAddress().getHostAddress() : "N/A";
         System.out.println("BLACKBOARD: KS reporta finalización para cliente " + logCliente + ". Evento: " + (eventoRespuesta != null ? eventoRespuesta.getTipo(): "NULL"));
    }

    // --- NUEVOS Métodos Implementados de IBlackboard ---

    @Override
    public boolean isNombreEnUso(String nombre) {
        // Implementación existente
        if (nombre == null) return false;
        return socketPorNombre.containsKey(nombre);
    }

    @Override
    public void registrarUsuario(Socket cliente, String nombre) {
        // Implementación existente (con logs detallados y manejo de casos)
        if (cliente == null || nombre == null || nombre.isBlank()) { /* ... */ return; }
        String nombreAnterior = nombrePorSocket.get(cliente);
        if (nombreAnterior != null && !nombreAnterior.equals(nombre)) { /* ... */ socketPorNombre.remove(nombreAnterior); }
        Socket socketExistente = socketPorNombre.get(nombre);
        if (socketExistente != null && !socketExistente.equals(cliente)) { /* ... */ return; }

        socketPorNombre.put(nombre, cliente);
        nombrePorSocket.put(cliente, nombre);
        // Log detallado
        System.out.println("*****************************************************");
        System.out.println("BLACKBOARD: >>> Usuario Registrado en Mapas <<<");
        System.out.println("    Socket: " + cliente.getInetAddress().getHostAddress() + ":" + cliente.getPort());
        System.out.println("    Nombre: '" + nombre + "'");
        System.out.println("    (Tablero y Turno se asignarán más adelante)");
        System.out.println("*****************************************************");
    }

    @Override
    public String getNombreDeUsuario(Socket cliente) {
        // Implementación existente
        if (cliente == null) return null;
        return nombrePorSocket.get(cliente);
    }

    @Override
    public Socket getSocketDeUsuario(String nombre) {
        // Implementación existente
        if (nombre == null) return null;
        return socketPorNombre.get(nombre);
    }

    // --- Getters Internos (para uso de KSs si se pasan en constructor) ---
    public IServer getServer() { return server; }
    public Controller getController() { return controller; }

    // --- Inspección (Método de ayuda para depuración) ---
     public void inspect() {
        // Implementación existente (con formato mejorado y mostrando nombres)
        System.out.println("\n--- Inspección del Blackboard ---");
        System.out.println("Fuentes de Conocimiento Registradas: " + knowledgeSources.size());
        knowledgeSources.forEach(ks -> System.out.println("  - " + ks.getClass().getSimpleName()));

        System.out.println("Clientes Conectados y Registrados (" + clientesConectados.size() + "):");
        synchronized (clientesConectados) {
            for (Socket s : clientesConectados) {
                String nombre = nombrePorSocket.get(s);
                String infoNombre = (nombre != null) ? "'" + nombre + "'" : " (REGISTRO PENDIENTE/FALLIDO)";
                String infoTablero = "Tablero=NULL";
                String infoTurno = "enTurno=NULL/FALSE";
                String estadoSocket = "(Closed: "+ (s != null ? s.isClosed() : "true") +")";

                if (s != null && s.getInetAddress() != null) {
                     System.out.println(String.format("  - Socket: %-21s | Nombre: %-25s | %-15s | %-18s | %s",
                                        s.getInetAddress().getHostAddress() + ":" + s.getPort(), infoNombre, infoTablero, infoTurno, estadoSocket));
                } else {
                     System.out.println("  - Socket nulo o inválido en la lista 'clientesConectados'.");
                }
            }
        }

         System.out.println("Salas Existentes (" + salas.size() + "):");
         salas.forEach((idSala, datos) -> {
             String hostInfo = "N/A";
             if (datos != null && datos.get("host") instanceof Socket) {
                  Socket hostSocket = (Socket) datos.get("host");
                  hostInfo = getNombreDeUsuario(hostSocket) != null ? getNombreDeUsuario(hostSocket) : (hostSocket.getInetAddress() != null ? hostSocket.getInetAddress().getHostAddress() : "Socket Inválido");
             }
             String numJugadores = "?";
             if (datos != null && datos.get("jugadores") instanceof List) {
                  numJugadores = String.valueOf(((List<?>) datos.get("jugadores")).size());
             } else if (datos == null) {
                  numJugadores = "Datos Nulos";
             }
             System.out.println("  - Sala ID: " + idSala + " (Host: " + hostInfo + ", Jugadores: " + numJugadores + ")");
         });

         System.out.println("Mapeo Nombre -> Socket (" + socketPorNombre.size() + "):");
         socketPorNombre.forEach((nombre, socket) -> System.out.println(String.format("  - '%-20s' -> %s", nombre, (socket != null && socket.getInetAddress() != null ? socket.getInetAddress().getHostAddress() : "NULL/Inválido"))));
         System.out.println("Mapeo Socket -> Nombre (" + nombrePorSocket.size() + "):");
         nombrePorSocket.forEach((socket, nombre) -> System.out.println(String.format("  - %-21s -> '%s'", (socket != null && socket.getInetAddress() != null ? socket.getInetAddress().getHostAddress() : "NULL/Inválido"), nombre)));
        System.out.println("------------------------------------------\n");
    }
}