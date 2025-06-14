/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.blackboard; 

// --- Importaciones Correctas ---
import dto.*;
import Handlers.CrearSalaHandler;
import Handlers.UnirseSalaHandler;
import Handlers.ConnectionHandler;
import com.mycompany.battleship.commons.Evento;
import com.mycompany.battleship.commons.IServer;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap; 
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import Handlers.DisparoHandler;
import Handlers.IniciarColocacionHandler;
import Handlers.IniciarCombateHandler;
import Handlers.RegistrarUsuarioHandler;
import com.mycompany.battleship.commons.IHandlerCommons;



/**
 * Almacena el estado principal (clientes conectados, salas, nombres) y despacha eventos a las KS.
 * Implementa IHandler para exponer su funcionalidad al Server y KSs.
 */
public class HandlerChain implements IHandlerCommons {

    // --- Estado Principal ---
    private final List<Socket> clientesConectados;
    private final Map<String, Map<String, Object>> salas;
     private final Map<String, PartidaDTO> partidasActivas; // NUEVO: idSala -> PartidaDTO
    private final Map<String, Socket> socketPorNombre;
    private final Map<Socket, String> nombrePorSocket;
   private final Map<Socket, JugadorDTO> jugadorDTOPorSocket;
    private final Map<String, JugadorDTO> jugadorDTOPorNombre; 
    // --- Componentes del Sistema ---
    private Controller controller;
    private final IServer server;
    private final List<IHandler> knowledgeSources; // Usar el nombre de interfaz correcto

    /**
     * Constructor del BlackBoard.
     * @param server La instancia del servidor (como IServer).
     */
    public HandlerChain(IServer server) {
        if (server == null) {
            throw new IllegalArgumentException("La instancia de IServer no puede ser nula.");
        }
        this.server = server;
        this.clientesConectados = Collections.synchronizedList(new ArrayList<>());
        this.salas = new ConcurrentHashMap<>();
        this.socketPorNombre = new ConcurrentHashMap<>();
        this.nombrePorSocket = new ConcurrentHashMap<>();
         this.partidasActivas = new ConcurrentHashMap<>(); // NUEVO
        // Inicializar los nuevos mapas
        this.jugadorDTOPorSocket = new ConcurrentHashMap<>();
        this.jugadorDTOPorNombre = new ConcurrentHashMap<>();
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
            System.out.println("CHAIN-RESPONSABILITY WARN: Intentando reasignar el Controller.");
            return;
        }
        this.controller = controller;
        System.out.println("CHAIN-RESPONSABILITY: Controller asignado.");
        registrarFuentesDeConocimiento();
    }

    /**
     * Instancia y registra TODAS las Knowledge Sources necesarias.
     */
   private void registrarFuentesDeConocimiento() {
        if (this.controller == null || this.server == null) {
             System.err.println("CHAIN-RESPONSABILITY ERROR: No se pueden registrar KS sin Controller o IServer.");
             return;
        }
        System.out.println("CHAIN-RESPONSABILITY: Registrando Fuentes de Conocimiento...");
        knowledgeSources.clear();

        // Asegúrate de que los nombres de clase y el constructor sean correctos
        knowledgeSources.add(new ConnectionHandler(this, this.server, this.controller));
        knowledgeSources.add(new CrearSalaHandler(this, this.server, this.controller));
        knowledgeSources.add(new UnirseSalaHandler(this, this.server, this.controller));
        knowledgeSources.add(new RegistrarUsuarioHandler(this, this.server, this.controller));
        knowledgeSources.add(new IniciarColocacionHandler(this, this.server, this.controller));
        knowledgeSources.add(new IniciarCombateHandler(this, this.server, this.controller));
         knowledgeSources.add(new DisparoHandler(this, this.server, this.controller));

        // TODO: Añadir futuras KS aquí...

        System.out.println("CHAIN-RESPONSABILITY: Fuentes de conocimiento registradas (" + knowledgeSources.size() + ")");
        knowledgeSources.forEach(ks -> System.out.println("  - " + ks.getClass().getSimpleName()));
    }

    // --- Implementación COMPLETA de IHandlerCommons ---

    @Override
    public void enviarEventoBlackBoard(Socket cliente, Evento evento) {
        // Log de depuración añadido previamente
        System.out.println("DEBUG [BB.enviarEvento]: Método ingresado. Tipo=" + (evento != null ? evento.getTipo() : "NULL EVENTO"));

        if (evento == null) {
            System.err.println("CHAIN-RESPONSABILITY ERROR: Evento nulo recibido en enviarEventoBlackBoard.");
            return;
        }
        String logCliente = (cliente != null && cliente.getInetAddress() != null) ? cliente.getInetAddress().getHostAddress() : "N/A";
        System.out.println("CHAIN-RESPONSABILITY: Recibido evento '" + evento.getTipo() + "' para cliente: " + logCliente);

        if (knowledgeSources.isEmpty()) {
             System.err.println("CHAIN-RESPONSABILITY WARN: No hay Knowledge Sources registradas.");
             return;
        }

        boolean eventoProcesado = false;
        // Usar el nombre de interfaz correcto aquí
        List<IHandler> sourcesSnapshot = new ArrayList<>(knowledgeSources);

        System.out.println("DEBUG [BB.enviarEvento]: Iniciando bucle de despacho a Handlers (" + sourcesSnapshot.size() + " KSs)...");

        // Usar el nombre de interfaz correcto aquí
        for (IHandler ks : sourcesSnapshot) {
            try {
                System.out.println("DEBUG [BB.enviarEvento]: Verificando Handler: " + ks.getClass().getSimpleName());
                if (ks.puedeProcesar(evento)) {
                    System.out.println("CHAIN-RESPONSABILITY: Despachando evento '" + evento.getTipo() + "' a " + ks.getClass().getSimpleName());
                    ks.procesarEvento(cliente, evento);
                    eventoProcesado = true;
                }
            } catch (Exception e) {
                System.err.println("CHAIN-RESPONSABILITY ERROR: EXCEPCIÓN AL PROCESAR por " + ks.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("DEBUG [BB.enviarEvento]: Bucle de despacho finalizado.");

        if (!eventoProcesado) {
            System.err.println("CHAIN-RESPONSABILITY WARN: Ninguna KS pudo procesar el evento: " + evento.getTipo());
        } else {
            System.out.println("CHAIN-RESPONSABILITY: Evento '" + evento.getTipo() + "' procesado por al menos una KS.");
        }
    }

       @Override
    public boolean existeSala(String idSala) { // Ahora verifica en partidasActivas
        return idSala != null && partidasActivas.containsKey(idSala);
    }

    
    /**
     * Agrega una nueva PartidaDTO al CHAIN-RESPONSABILITY.
     * @param partida La PartidaDTO a agregar.
     * @return true si se agregó, false si ya existía una partida con ese ID.
     */
     @Override
    public boolean agregarPartida(PartidaDTO partida) { // NUEVO método específico
        if (partida == null || partida.getIdPartida() == null) {
            System.err.println("CHAIN-RESPONSABILITY ERROR: Intento de agregar PartidaDTO nula o sin ID.");
            return false;
        }
        // putIfAbsent devuelve null si la clave no existía (y se insertó), o el valor existente si ya estaba.
        if (partidasActivas.putIfAbsent(partida.getIdPartida(), partida) == null) {
            System.out.println("CHAIN-RESPONSABILITY: Partida '" + partida.getIdPartida() + "' agregada. Estado: " + partida.getEstado());
            return true;
        } else {
            System.out.println("CHAIN-RESPONSABILITY WARN: Intento de agregar partida que ya existe: '" + partida.getIdPartida() + "'. No se sobrescribió.");
            return false;
        }
    }

    /**
     * Obtiene los datos de una PartidaDTO.
     * @param idSala El ID de la sala/partida.
     * @return La PartidaDTO, o null si no existe.
     */
     @Override
    public PartidaDTO getPartidaDTO(String idSala) { // NUEVO método específico
        return idSala != null ? partidasActivas.get(idSala) : null;
    }

    
    /**
     * Actualiza una PartidaDTO existente en el CHAIN-RESPONSABILITY.
     * @param partida La PartidaDTO con los datos actualizados.
     * @return true si se actualizó, false si la partida no existía.
     */
     @Override
    public boolean actualizarPartida(PartidaDTO partida) { // NUEVO método específico
        if (partida == null || partida.getIdPartida() == null) {
            System.err.println("CHAIN-RESPONSABILITY ERROR: Intento de actualizar PartidaDTO nula o sin ID.");
            return false;
        }
        if (partidasActivas.containsKey(partida.getIdPartida())) {
            partidasActivas.put(partida.getIdPartida(), partida); // Sobrescribe
            System.out.println("CHAIN-RESPONSABILITY: Partida '" + partida.getIdPartida() + "' actualizada. Nuevo estado: " + partida.getEstado());
            return true;
        } else {
            System.err.println("CHAIN-RESPONSABILITY WARN: Intento de actualizar partida inexistente '" + partida.getIdPartida() + "'.");
            return false;
        }
    }
    
    /**
     * Elimina una partida del CHAIN-RESPONSABILITY.
     * @param idSala El ID de la sala/partida a eliminar.
     */
     @Override
    public void eliminarPartida(String idSala) {
        if (idSala != null) {
            PartidaDTO partidaEliminada = partidasActivas.remove(idSala);
            if (partidaEliminada != null) {
                System.out.println("CHAIN-RESPONSABILITY: Partida '" + idSala + "' eliminada.");
            } else {
                System.out.println("CHAIN-RESPONSABILITY WARN: Intento de eliminar partida inexistente '" + idSala + "'.");
            }
        }
    }
    
    
    @Override
    public void agregarSala(String id, Map<String, Object> datosSala) {
        // Implementación que evita sobrescribir y loguea
        if (id != null && datosSala != null) {
             if (salas.putIfAbsent(id, datosSala) == null) { // putIfAbsent devuelve null si la clave NO existía
                  System.out.println("CHAIN-RESPONSABILITY: Sala '" + id + "' agregada al mapa.");
             } else {
                  System.out.println("CHAIN-RESPONSABILITY WARN: Intento de agregar sala que ya existe: '" + id + "'. No se sobrescribió.");
             }
        } else {
             System.err.println("CHAIN-RESPONSABILITY ERROR: Intento de agregar sala con ID o datos nulos.");
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
                 System.out.println("CHAIN-RESPONSABILITY: Datos actualizados para sala '" + idSala + "'.");
                 // Notificar si es necesario
                 // if (controller != null) controller.notificarCambio("SALA_ACTUALIZADA;" + idSala);
            } else {
                 System.err.println("CHAIN-RESPONSABILITY WARN: Intento de actualizar sala inexistente '" + idSala + "'.");
            }
        } else {
             System.err.println("CHAIN-RESPONSABILITY ERROR: ID de sala o nuevos datos nulos al intentar actualizar.");
        }
    }

    @Override
    public void agregarClienteSocket(Socket clienteSocket) {
        // Implementación existente
        if (clienteSocket != null && !clientesConectados.contains(clienteSocket)) {
            boolean added = clientesConectados.add(clienteSocket);
            if (added) {
                 System.out.println("CHAIN-RESPONSABILITY: Socket de cliente " + clienteSocket.getInetAddress().getHostAddress() + " registrado como activo.");
                 if (controller != null) {
                     controller.notificarCambio("CLIENTE_REGISTRADO");
                 }
            }
        } else if (clienteSocket != null) {
             System.out.println("CHAIN-RESPONSABILITY WARN: Intento de agregar cliente ya existente: " + clienteSocket.getInetAddress().getHostAddress());
        }
    }

    @Override
    public void removerClienteSocket(Socket clienteSocket) {
        // Implementación existente (con limpieza de nombres)
        if (clienteSocket != null) {
              String nombreRemovido = nombrePorSocket.remove(clienteSocket);
              if (nombreRemovido != null) {
                   socketPorNombre.remove(nombreRemovido);
                   System.out.println("CHAIN-RESPONSABILITY: Mapeo de nombre '" + nombreRemovido + "' eliminado para socket desconectado.");
              } else {
                   System.out.println("CHAIN-RESPONSABILITY WARN: Socket desconectado no tenía un nombre registrado.");
              }
              boolean removed = clientesConectados.remove(clienteSocket);
              if (removed) {
                  System.out.println("CHAIN-RESPONSABILITY: Socket de cliente " + clienteSocket.getInetAddress().getHostAddress() + " eliminado de activos.");
                  if (controller != null) {
                      controller.notificarCambio("CLIENTE_ELIMINADO"); // Podría incluir nombreRemovido
                  }
              } else {
                    // Si no estaba en clientesConectados, quizás ya fue removido
                    System.out.println("CHAIN-RESPONSABILITY INFO: Socket " + clienteSocket.getInetAddress().getHostAddress() + " no encontrado en lista clientesConectados durante remoción (posiblemente ya removido).");
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
         System.out.println("CHAIN-RESPONSABILITY: KS reporta finalización para cliente " + logCliente + ". Evento: " + (eventoRespuesta != null ? eventoRespuesta.getTipo(): "NULL"));
    }

    // --- NUEVOS Métodos Implementados de IHandlerCommons ---

/**
     * Verifica si un nombre de jugador ya está en uso (registrado en jugadorDTOPorNombre).
     * @param nombre El nombre a verificar.
     * @return true si el nombre está en uso, false en caso contrario.
     */
    @Override
    public boolean isNombreEnUso(String nombre) { // Implementación actualizada
        if (nombre == null || nombre.isBlank()) return false;
        return jugadorDTOPorNombre.containsKey(nombre);
    }
    
    /**
     * Registra un JugadorDTO asociándolo a un Socket.
     * Gestiona la actualización si el socket o el nombre ya existían.
     *
     * @param cliente El Socket del cliente.
     * @param nuevoJugadorDTO El objeto JugadorDTO a registrar.
     */
    @Override
    public synchronized void registrarUsuario(Socket cliente, JugadorDTO nuevoJugadorDTO) { // CAMBIO: Acepta JugadorDTO
        if (cliente == null || nuevoJugadorDTO == null) {
            System.err.println("CHAIN-RESPONSABILITY ERROR: registrarUsuario: Cliente o JugadorDTO nulo.");
            return;
        }
        String nombreNuevoJugador = nuevoJugadorDTO.getNombre();

        System.out.println("CHAIN-RESPONSABILITY [registrarUsuario]: Solicitud para registrar JugadorDTO: " + nombreNuevoJugador +
                           " con Socket: " + cliente.getInetAddress().getHostAddress() + ":" + cliente.getPort());

        // 1. Manejar si el Socket ya está asociado con otro JugadorDTO
        JugadorDTO jugadorAnteriorDelSocket = jugadorDTOPorSocket.get(cliente);
        if (jugadorAnteriorDelSocket != null) {
            System.out.println("CHAIN-RESPONSABILITY [registrarUsuario]: Socket ya asociado con JugadorDTO '" +
                               jugadorAnteriorDelSocket.getNombre() + "'.");
            if (!jugadorAnteriorDelSocket.getNombre().equals(nombreNuevoJugador)) {
                System.out.println("CHAIN-RESPONSABILITY [registrarUsuario]: El nombre cambió. Eliminando mapeo antiguo nombre '" +
                                   jugadorAnteriorDelSocket.getNombre() + "' de jugadorDTOPorNombre.");
                jugadorDTOPorNombre.remove(jugadorAnteriorDelSocket.getNombre());
            }
        }

        // 2. Manejar si el nombre ya está en uso por otro JugadorDTO (y potencialmente otro socket)
        JugadorDTO jugadorExistenteConEseNombre = jugadorDTOPorNombre.get(nombreNuevoJugador);
        if (jugadorExistenteConEseNombre != null) {
            System.out.println("CHAIN-RESPONSABILITY [registrarUsuario]: Nombre '" + nombreNuevoJugador +
                               "' ya asociado a JugadorDTO: " + jugadorExistenteConEseNombre.getNombre());
            // Buscar el socket asociado a este jugador existente
            Socket socketDelJugadorConNombreExistente = null;
            for (Map.Entry<Socket, JugadorDTO> entry : jugadorDTOPorSocket.entrySet()) {
                // Usamos el nombre para comparar DTOs, asumiendo que el nombre es el identificador único aquí
                if (entry.getValue().getNombre().equals(jugadorExistenteConEseNombre.getNombre())) {
                    socketDelJugadorConNombreExistente = entry.getKey();
                    break;
                }
            }

            if (socketDelJugadorConNombreExistente != null && !socketDelJugadorConNombreExistente.equals(cliente)) {
                // El nombre está en uso por un socket diferente. La KS debería haberlo prevenido.
                // Si se decide permitir que la nueva conexión "robe" el nombre:
                System.err.println("CHAIN-RESPONSABILITY WARN [registrarUsuario]: Nombre '" + nombreNuevoJugador +
                                   "' usado por OTRO socket (" + socketDelJugadorConNombreExistente.getInetAddress().getHostAddress() +
                                   "). Se desvinculará el socket antiguo.");
                jugadorDTOPorSocket.remove(socketDelJugadorConNombreExistente);
            }
        }

        // 3. Realizar el registro/actualización con el DTO
        jugadorDTOPorSocket.put(cliente, nuevoJugadorDTO);
        jugadorDTOPorNombre.put(nombreNuevoJugador, nuevoJugadorDTO);

        System.out.println("*****************************************************");
        System.out.println("CHAIN-RESPONSABILITY: >>> Usuario Registrado (con JugadorDTO) <<<");
        System.out.println("   Socket: " + cliente.getInetAddress().getHostAddress() + ":" + cliente.getPort());
        System.out.println("   JugadorDTO: " + nuevoJugadorDTO.toString()); // Usa el toString() de JugadorDTO
        System.out.println("   Total jugadorDTOPorSocket: " + jugadorDTOPorSocket.size());
        System.out.println("   Total jugadorDTOPorNombre: " + jugadorDTOPorNombre.size());
        System.out.println("*****************************************************");
    }
    
    /**
     * Obtiene el objeto JugadorDTO asociado a un Socket.
     * @param cliente El socket del cliente.
     * @return El JugadorDTO, o null si no hay asociación.
     */
    public JugadorDTO getJugadorDTO(Socket cliente) { // NUEVO método
        if (cliente == null) return null;
        return jugadorDTOPorSocket.get(cliente);
    }
    
    

  /**
     * Obtiene el nombre del jugador (desde el DTO) asociado a un Socket.
     * @param cliente El socket del cliente.
     * @return El nombre del jugador, o null si no hay asociación.
     */
    @Override
    public String getNombreDeUsuario(Socket cliente) { // Implementación actualizada
        JugadorDTO jugadorDTO = jugadorDTOPorSocket.get(cliente);
        return (jugadorDTO != null) ? jugadorDTO.getNombre() : null;
    }
  /**
     * Obtiene el Socket asociado a un nombre de jugador.
     * Busca en el mapa jugadorDTOPorNombre y luego encuentra el socket correspondiente.
     * @param nombre El nombre del jugador.
     * @return El Socket, o null si el nombre no está registrado.
     */
    @Override
    public Socket getSocketDeUsuario(String nombre) { // Implementación actualizada
        if (nombre == null || nombre.isBlank()) return null;
        JugadorDTO jugadorDTO = jugadorDTOPorNombre.get(nombre);
        if (jugadorDTO != null) {
            for (Map.Entry<Socket, JugadorDTO> entry : jugadorDTOPorSocket.entrySet()) {
                // Comparamos por nombre, ya que el DTO podría no tener un equals robusto
                if (entry.getValue().getNombre().equals(jugadorDTO.getNombre())) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    
    
    
    public void registrarUsuarioAnterior(Socket cliente, String nombre) {
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
        System.out.println("CHAIN-RESPONSABILITY: >>> Usuario Registrado en Mapas <<<");
        System.out.println("    Socket: " + cliente.getInetAddress().getHostAddress() + ":" + cliente.getPort());
        System.out.println("    Nombre: '" + nombre + "'");
        System.out.println("    (Tablero y Turno se asignarán más adelante)");
        System.out.println("*****************************************************");
    }
    // --- Getters Internos (para uso de KSs si se pasan en constructor) ---
    public IServer getServer() { return server; }
    public Controller getController() { return controller; }

    // --- Inspección (Método de ayuda para depuración) ---
     public void inspect() {
        // Implementación existente (con formato mejorado y mostrando nombres)
        System.out.println("\n--- Inspección del CHAIN-RESPONSABILITY ---");
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
     
     
      /**
     * Elimina las asociaciones de un JugadorDTO cuando se desconecta el cliente.
     * @param cliente El Socket del cliente desconectado.
     */
    public synchronized void usuarioDesconectado(Socket cliente) { // Renombrado/Añadido
        if (cliente == null) return;

        System.out.println("CHAIN-RESPONSABILITY: Procesando desconexión (usuarioDesconectado) para socket " + cliente.getInetAddress().getHostAddress());
        JugadorDTO jugadorDesconectadoDTO = jugadorDTOPorSocket.remove(cliente);

        if (jugadorDesconectadoDTO != null) {
            System.out.println("CHAIN-RESPONSABILITY: JugadorDTO '" + jugadorDesconectadoDTO.getNombre() + "' estaba asociado al socket. Eliminando de jugadorDTOPorNombre.");
            jugadorDTOPorNombre.remove(jugadorDesconectadoDTO.getNombre());
            System.out.println("CHAIN-RESPONSABILITY: JugadorDTO '" + jugadorDesconectadoDTO.getNombre() + "' eliminado completamente.");
            // Aquí iría la lógica para limpiar la presencia del jugador en las salas, etc.
        } else {
            System.out.println("CHAIN-RESPONSABILITY: No se encontró JugadorDTO asociado al socket " + cliente.getInetAddress().getHostAddress() + " para eliminar.");
        }
        clientesConectados.remove(cliente);
        System.out.println("CHAIN-RESPONSABILITY: Limpieza de usuarioDesconectado completada para socket " + cliente.getInetAddress().getHostAddress());
    }

}

