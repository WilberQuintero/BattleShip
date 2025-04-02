/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.blackboard;

import com.mycompany.battleship.commons.IBlackboard;
import com.mycompany.battleship.commons.IServer;



/**
 *
 * @author caarl
 */

// No necesita Evento directamente si solo reacciona a notificaciones

/**
 * Controller adaptado.
 * Actúa como intermediario para notificaciones y acciones globales,
 * comunicándose con el Server a través de la interfaz IServer.
 */
public class Controller {

    private IBlackboard blackboard; // Usa la interfaz IBlackboard
    private final IServer server;   // Usa la interfaz IServer

    /**
     * Constructor del Controller.
     * @param server La instancia del servidor (como IServer).
     */
    public Controller(IServer server) { // Recibe IServer
        if (server == null) {
           throw new IllegalArgumentException("La instancia de IServer no puede ser nula.");
        }
        this.server = server;
        // El blackboard se asignará mediante setBlackboard
    }

    /**
     * Asigna la instancia del BlackBoard (como IBlackboard) al Controller.
     * @param blackboard La instancia del BlackBoard.
     */
    public void setBlackboard(IBlackboard blackboard) { // Recibe IBlackboard
         if (blackboard == null) {
           throw new IllegalArgumentException("La instancia de IBlackboard no puede ser nula.");
         }
        this.blackboard = blackboard;
        System.out.println("CONTROLLER: IBlackboard asignado.");
    }

    /**
     * Método llamado por el BlackBoard o las KS cuando ocurre un cambio
     * relevante que requiere una notificación al/los cliente(s) vía Server.
     * @param tipoNotificacion Un string que identifica el tipo de cambio/notificación.
     */
    public void notificarCambio(String tipoNotificacion) {
        System.out.println("CONTROLLER: Notificación recibida: " + tipoNotificacion);

        // Lógica para determinar qué hacer basado en la notificación
        switch (tipoNotificacion) {
            case "CLIENTE_REGISTRADO":
                // Un cliente completó la conexión inicial y fue registrado por la KS.
                System.out.println("CONTROLLER: Cliente registrado en BlackBoard.");
                // Podríamos pedir al servidor que envíe la lista actualizada de jugadores/salas.
                // Ejemplo: server.enviarMensajeATodos("Update;Jugadores=" + obtenerListaJugadores());
                // O enviar un mensaje de bienvenida sólo al nuevo (necesitaríamos el Socket)
                // Esto muestra la necesidad de pasar más contexto en notificarCambio o
                // que la KS envíe directamente mensajes específicos.
                break;

            case "CLIENTE_ELIMINADO":
                // Un cliente se desconectó y fue eliminado del BlackBoard.
                System.out.println("CONTROLLER: Cliente eliminado del BlackBoard.");
                // Notificar a los demás jugadores.
                // Ejemplo: server.enviarMensajeATodos("Update;JugadorDesconectado=" + idJugador);
                break;

            case "PARTIDA_INICIADA":
                 System.out.println("CONTROLLER: Partida iniciada.");
                 // Notificar a todos los involucrados en la partida.
                 // server.enviarMensajeAParticipantes(partidaId, "MensajeInicioPartida");
                 break;

            // Añadir más casos según los eventos y cambios de estado del juego...

            default:
                System.out.println("CONTROLLER: Notificación tipo '" + tipoNotificacion + "' sin acción definida.");
                break;
        }
    }

    // --- Métodos de ayuda (ejemplos) ---

    // Podrías tener métodos aquí que obtengan datos formateados del blackboard
    // para enviarlos a través del servidor, aunque esto podría hacerlo también la KS.
    /*
    private String obtenerListaJugadores() {
        if (blackboard == null) return "[]";
        // Necesitaríamos una forma de obtener Jugadores del IBlackboard si fuera necesario
        // IBlackboard necesitaría un método como getJugadores() -> List<PlayerData>
        // List<PlayerData> jugadores = blackboard.getJugadores();
        // return formatearListaJugadores(jugadores); // Método para convertir a String
        return "[Stub Player List]";
    }
    */

    // Otros métodos que el Controller podría orquestar...
}