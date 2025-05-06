/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.blackboard;

import com.mycompany.battleship.commons.IServer;
import com.mycompany.battleship.commons.IHandlerCommons;



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

    private IHandlerCommons handlerCommons; // Usa la interfaz IHandlerCommons
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
        // El handlerCommons se asignará mediante setBlackboard
    }

    /**
     * Asigna la instancia del BlackBoard (como IHandlerCommons) al Controller.
     * @param handlerCommons La instancia del BlackBoard.
     */
    public void setHandlerCommons(IHandlerCommons handlerCommons) { // Recibe IHandlerCommons
         if (handlerCommons == null) {
           throw new IllegalArgumentException("La instancia de IBlackboard no puede ser nula.");
         }
        this.handlerCommons = handlerCommons;
        System.out.println("CONTROLLER: IBlackboard asignado.");
    }

/**
     * Método llamado por el BlackBoard o las KS cuando ocurre un cambio
     * relevante que requiere una notificación al/los cliente(s) vía Server.
     * @param tipoNotificacion Un string que identifica el tipo de cambio/notificación,
     * puede contener un payload separado por ';'. Ej: "SALA_LLENA;idDeLaSala"
     */
    public void notificarCambio(String tipoNotificacion) {
        System.out.println("CONTROLLER: Notificación recibida: " + tipoNotificacion);

        // Separar el tipo base de la notificación y el posible payload (ej. ID de sala)
        String tipoBase = tipoNotificacion;
        String payload = null;
        if (tipoNotificacion != null && tipoNotificacion.contains(";")) {
            String[] parts = tipoNotificacion.split(";", 2);
            tipoBase = parts[0];
            payload = parts[1]; // Contendrá el ID de la sala u otra información
        }

        // Lógica para determinar qué hacer basado en la notificación
        switch (tipoBase) {
            case "CLIENTE_REGISTRADO":
                // Un cliente completó la conexión inicial y fue registrado por la KS.
                System.out.println("CONTROLLER: Cliente registrado en BlackBoard.");
                // Podríamos pedir al servidor que envíe la lista actualizada de jugadores/salas.
                // server.enviarMensajeATodos("Update;Tipo=ListaSalas;Datos=" + obtenerListaSalas());
                break;

            case "CLIENTE_ELIMINADO":
                // Un cliente se desconectó y fue eliminado del BlackBoard.
                System.out.println("CONTROLLER: Cliente eliminado del BlackBoard.");
                // Notificar a los demás jugadores relevantes (ej. en la misma sala/partida).
                // server.enviarMensajeATodos("Update;JugadorDesconectado=" + payload); // payload podría ser ID del jugador
                break;

            // --- NUEVO CASO ---
            case "SALA_LLENA":
                // La KS UnirseSalaKS notificó que una sala alcanzó la capacidad máxima.
                // El payload debería ser el ID de la sala.
                System.out.println("CONTROLLER: Sala '" + payload + "' está llena y lista para iniciar partida.");
                // Aquí el controller podría orquestar el inicio de la partida.
                // Opción 1: Enviar un evento "INICIAR_PARTIDA" al Blackboard para que una KS lo maneje.
                // Evento eventoInicio = new Evento("INICIAR_PARTIDA");
                // eventoInicio.agregarDato("idSala", payload);
                // if (handlerCommons != null) {
                //     handlerCommons.enviarEventoBlackBoard(null, eventoInicio); // null como cliente origen
                // }
                // Opción 2: Llamar a un método específico del IServer o IHandlerCommons si existe.
                // Opción 3: Directamente enviar mensajes a los clientes de la sala.
                //           (Necesitaría obtener los sockets de los clientes de esa sala desde el handlerCommons)
                // List<Socket> jugadoresSala = handlerCommons.getJugadoresEnSala(payload); // Necesitaría este método en IHandlerCommons
                // server.enviarMensajeAClientes(jugadoresSala, "Mensaje;Tipo=PartidaLista;Sala="+payload); // Necesitaría este método en IServer
                break;

             // --- NUEVO CASO ---
             case "SALA_ACTUALIZADA":
                  // Una KS actualizó los datos de una sala (ej. se unió/salió un jugador).
                  // El payload debería ser el ID de la sala.
                  System.out.println("CONTROLLER: Datos de la sala '" + payload + "' actualizados.");
                  // Podría ser útil reenviar el estado actualizado de la sala a los clientes en ella.
                  // Map<String, Object> datosSala = handlerCommons.getDatosSala(payload); // Necesitaría este método en IHandlerCommons
                  // List<Socket> clientesEnSala = (List<Socket>) datosSala.get("jugadores"); // Asumiendo estructura
                  // String datosJson = convertirMapaAJson(datosSala); // Necesitaría un método para convertir
                  // server.enviarMensajeAClientes(clientesEnSala, "Update;Tipo=EstadoSala;Datos=" + datosJson);
                  break;

            case "PARTIDA_INICIADA": // Este caso ya existía
                 // Una KS (quizás una 'IniciarPartidaKS') notificó que la partida comenzó.
                 // El payload podría ser el ID de la partida o sala.
                 System.out.println("CONTROLLER: Partida iniciada para sala/partida: " + payload);
                 // Notificar a todos los involucrados en la partida.
                 // List<Socket> jugadoresPartida = handlerCommons.getJugadoresEnPartida(payload); // Necesitaría método en IHandlerCommons
                 // server.enviarMensajeAClientes(jugadoresPartida, "Mensaje;Tipo=PartidaIniciada;Detalles=...");
                 break;

            // Añadir más casos según los eventos y cambios de estado del juego...
            // Ej: TURNO_CAMBIADO, DISPARO_REALIZADO, NAVE_HUNDIDA, JUEGO_TERMINADO, etc.

            default:
                System.out.println("CONTROLLER: Notificación tipo '" + tipoBase + "' sin acción específica definida.");
                break;
        }
    }

    // --- Métodos de ayuda (ejemplos) ---

    // Podrías tener métodos aquí que obtengan datos formateados del handlerCommons
    // para enviarlos a través del servidor, aunque esto podría hacerlo también la KS.
    /*
    private String obtenerListaJugadores() {
        if (handlerCommons == null) return "[]";
        // Necesitaríamos una forma de obtener Jugadores del IHandlerCommons si fuera necesario
        // IHandlerCommons necesitaría un método como getJugadores() -> List<PlayerData>
        // List<PlayerData> jugadores = handlerCommons.getJugadores();
        // return formatearListaJugadores(jugadores); // Método para convertir a String
        return "[Stub Player List]";
    }
    */

    // Otros métodos que el Controller podría orquestar...
}