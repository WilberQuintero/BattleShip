/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.blackboard; // Asegúrate que el paquete sea el correcto

// --- Importaciones Correctas ---
import ks.CrearSalaKS;
import ks.UnirseSalaKS;
import ks.ConnectionKS;
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


/**
 * El Blackboard adaptado al estilo del ejemplo de Dominó.
 * Almacena el estado principal (clientes conectados, salas) y despacha eventos a las KS.
 * Implementa IBlackboard para exponer su funcionalidad al Server.
 */
public class BlackBoard implements IBlackboard { // Implementa la interfaz de commons

    // --- Estado Principal ---
    private final List<Socket> clientesConectados;
    // Mapa para las salas: Key = ID de sala, Value = Mapa con datos de la sala
    private final Map<String, Map<String, Object>> salas;

    // --- Componentes del Sistema ---
    private Controller controller; // El Controller específico de este proyecto
    private final IServer server; // La interfaz del Server (del proyecto commons)
    // --- CORRECCIÓN 1: Usar KnowledgeSource (sin 'I') ---
    private final List<IKnowledgeSource> knowledgeSources;

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
        this.salas = new ConcurrentHashMap<>(); // Inicializar mapa de salas
        // --- CORRECCIÓN 1: Usar KnowledgeSource (sin 'I') ---
        this.knowledgeSources = new ArrayList<>();
        // El controller se asigna después con setController
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
            return;
        }
        this.controller = controller;
        System.out.println("BLACKBOARD: Controller asignado.");
        registrarFuentesDeConocimiento(); // Registrar KS ahora
    }

    /**
     * Instancia y registra las Knowledge Sources.
     * Asegúrate de que los nombres de clase y paquetes sean correctos.
     * Usa el constructor recomendado para las KS que requiere todas las dependencias.
     */
   private void registrarFuentesDeConocimiento() {
        if (this.controller == null || this.server == null) {
             System.err.println("BLACKBOARD ERROR: No se pueden registrar KS sin Controller o IServer.");
             return;
        }

        System.out.println("BLACKBOARD: Registrando Fuentes de Conocimiento...");

        // 1. ConnectionKS: Siempre necesita las 3 dependencias.
        knowledgeSources.add(new ConnectionKS(this, this.server, this.controller));

        // --- CORRECCIÓN 2: Usar constructor consistente para CrearSalaKS ---
        // Usar el constructor que recibe las 3 dependencias, asumiendo que
        // CrearSalaKS necesita notificar al controller.
        knowledgeSources.add(new CrearSalaKS(this, this.server, this.controller));
        // Eliminar la línea anterior que usaba el constructor de dos argumentos:
        // knowledgeSources.add(new CrearSalaKS(this.server, this)); // <- ESTA LÍNEA DEBERÍA QUITARSE O MODIFICARSE

        // 3. UnirseSalaKS: También necesita las 3 dependencias.
        knowledgeSources.add(new UnirseSalaKS(this, this.server, this.controller));

        // TODO: Añadir aquí el registro de futuras KS (Disparo, ColocarBarcos, etc.)
        // Ejemplo: knowledgeSources.add(new DisparoKS(this, this.server, this.controller));

        System.out.println("BLACKBOARD: Fuentes de conocimiento registradas (" + knowledgeSources.size() + ")");
        knowledgeSources.forEach(ks -> System.out.println("  - " + ks.getClass().getSimpleName()));
    }

    // --- Implementación de IBlackboard ---

    @Override
    public void enviarEventoBlackBoard(Socket cliente, Evento evento) {
        if (evento == null) { /* ... (código existente) ... */ return; }
        String logCliente = (cliente != null && cliente.getInetAddress() != null) ? cliente.getInetAddress().getHostAddress() : "N/A";
        System.out.println("BLACKBOARD: Recibido evento '" + evento.getTipo() + "' para cliente: " + logCliente);

        if (knowledgeSources.isEmpty()) { /* ... (código existente) ... */ return; }

        boolean eventoProcesado = false;
        // --- CORRECCIÓN 1: Usar KnowledgeSource (sin 'I') ---
        List<IKnowledgeSource> sourcesSnapshot = new ArrayList<>(knowledgeSources);

        // --- CORRECCIÓN 1: Usar KnowledgeSource (sin 'I') ---
        for (IKnowledgeSource ks : sourcesSnapshot) {
            try {
                if (ks.puedeProcesar(evento)) {
                    System.out.println("BLACKBOARD: Despachando evento '" + evento.getTipo() + "' a " + ks.getClass().getSimpleName());
                    ks.procesarEvento(cliente, evento);
                    eventoProcesado = true;
                }
            } catch (Exception e) {
                System.err.println("BLACKBOARD ERROR: Excepción al procesar evento por " + ks.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (!eventoProcesado) { /* ... (código existente) ... */ }
        else { /* ... (código existente) ... */ }
    }

    // --- Métodos de Gestión de Salas (Implementación de IBlackboard) ---

    @Override
    public boolean existeSala(String idSala) {
        return salas.containsKey(idSala);
    }

    @Override
    public void agregarSala(String id, Map<String, Object> datosSala) {
        // Usamos putIfAbsent para evitar sobrescribir accidentalmente si existe concurrencia,
        // aunque la lógica en CrearSalaKS ya verifica con existeSala. put normal también valdría.
        if (id != null && datosSala != null) {
             salas.putIfAbsent(id, datosSala);
             // El log se movió a CrearSalaKS, pero podemos añadir uno aquí si queremos
             System.out.println("BLACKBOARD: Sala '" + id + "' agregada al mapa.");
        } else {
             System.err.println("BLACKBOARD ERROR: Intento de agregar sala con ID o datos nulos.");
        }
    }

    @Override
    public Map<String, Object> getDatosSala(String idSala) {
        Map<String, Object> datosOriginales = salas.get(idSala);
        if (datosOriginales != null) {
            // Devolver copia superficial para evitar modificación externa no controlada
            return new HashMap<>(datosOriginales);
        }
        return null;
    }

    @Override
    public void actualizarDatosSala(String idSala, Map<String, Object> nuevosDatosSala) {
        if (idSala != null && nuevosDatosSala != null) {
            // Reemplaza la entrada existente si la clave ya está presente.
            if (salas.containsKey(idSala)) {
                 salas.put(idSala, nuevosDatosSala);
                 System.out.println("BLACKBOARD: Datos actualizados para sala '" + idSala + "'.");
                 // Opcional: Notificar al controller
                 // if (controller != null) controller.notificarCambio("SALA_ACTUALIZADA;" + idSala);
            } else {
                 System.err.println("BLACKBOARD WARN: Intento de actualizar sala inexistente '" + idSala + "'.");
            }
        } else {
             System.err.println("BLACKBOARD ERROR: ID de sala o nuevos datos nulos al intentar actualizar.");
        }
    }

    // --- Métodos de Gestión de Clientes (Implementación de IBlackboard) ---

    @Override
    public void agregarClienteSocket(Socket clienteSocket) {
        // La lógica existente parece correcta
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
         // La lógica existente parece correcta
         if (clienteSocket != null) {
             boolean removed = clientesConectados.remove(clienteSocket);
             if (removed) {
                  System.out.println("BLACKBOARD: Socket de cliente " + clienteSocket.getInetAddress().getHostAddress() + " eliminado de activos.");
                  if (controller != null) {
                      controller.notificarCambio("CLIENTE_ELIMINADO");
                  }
             }
         }
     }

    @Override
    public List<Socket> getClientesConectados() {
        // La lógica existente parece correcta
        synchronized (clientesConectados) {
            return new ArrayList<>(clientesConectados);
        }
    }

    // --- Callback (Implementación de IBlackboard) ---

    @Override
    public void respuestaFuenteC(Socket cliente, Evento eventoRespuesta) {
        // La lógica existente parece correcta (solo logging)
         String logCliente = (cliente != null && cliente.getInetAddress() != null) ? cliente.getInetAddress().getHostAddress() : "N/A";
         System.out.println("BLACKBOARD: KS reporta finalización para cliente " + logCliente + ". Evento: " + eventoRespuesta.getTipo());
    }

    // --- Getters para Dependencias Internas (No parte de IBlackboard) ---
    // Estos son para uso interno o para pasar a las KS durante la construcción.

    public IServer getServer() {
        return server; // Devuelve la interfaz IServer
    }

    public Controller getController() {
        return controller; // Devuelve el Controller concreto
    }

    // --- Inspección (Método de ayuda para depuración) ---
     public void inspect() {
         // La lógica existente parece correcta
        System.out.println("\n--- Inspección del Blackboard (Estilo Dominó) ---");
        System.out.println("Fuentes de Conocimiento Registradas: " + knowledgeSources.size());
        knowledgeSources.forEach(ks -> System.out.println("  - " + ks.getClass().getSimpleName()));
        System.out.println("Clientes Conectados (" + clientesConectados.size() + "):");
        synchronized (clientesConectados) {
            for (Socket s : clientesConectados) {
                if (s!=null && s.getInetAddress()!=null){
                     System.out.println("  - " + s.getInetAddress().getHostAddress() + ":" + s.getPort() + " (Closed: "+s.isClosed()+")");
                } else {
                     System.out.println("  - Socket nulo o inválido en la lista.");
                }
            }
        }
         System.out.println("Salas Existentes (" + salas.size() + "):");
         // Imprimir IDs de las salas para verificar creación/eliminación
         salas.keySet().forEach(idSala -> System.out.println("  - Sala ID: " + idSala));
         // Podríamos imprimir más detalles de las salas si fuera necesario
        System.out.println("------------------------------------------\n");
    }
}