/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package handlers; // O tu paquete de knowledge sources

import com.mycompany.battleship.commons.Evento;
import com.mycompany.battleship.commons.IServer;
import com.mycompany.blackboard.Controller;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.mycompany.blackboard.IHandler;
import com.mycompany.battleship.commons.IHandlerCommons;

/**
 * Knowledge Source para manejar el evento de un jugador uniéndose a una sala existente.
 */
public class UnirseSalaHandler implements IHandler { // Asegúrate que IHandler esté definida como en tu proyecto

    private final IHandlerCommons handlerCommons;
    private final IServer server;
    private final Controller controller; // Podríamos necesitar notificar al controller

    // Constante para la capacidad máxima de la sala (Battleship = 2 jugadores)
    private static final int MAX_JUGADORES_SALA = 2;

    public UnirseSalaHandler(IHandlerCommons blackboard, IServer server, Controller controller) {
        this.handlerCommons = blackboard;
        this.server = server;
        this.controller = controller;
    }

    @Override
    public boolean puedeProcesar(Evento evento) {
        return evento != null && "UNIRSE_SALA".equalsIgnoreCase(evento.getTipo());
    }

    @Override
    @SuppressWarnings("unchecked") // Necesario por el casting de la lista de jugadores
    public void procesarEvento(Socket cliente, Evento evento) {
        if (cliente == null || evento == null) {
            System.err.println("UNIRSE_SALA_KS: Cliente o Evento nulo.");
            return;
        }

        String idSala = (String) evento.obtenerDato("idSala");
        if (idSala == null || idSala.isBlank()) {
            System.err.println("UNIRSE_SALA_KS: idSala no proporcionado en el evento.");
            // Enviar error al cliente
            enviarRespuestaError(cliente, "ID de sala no válido.");
            return;
        }

        System.out.println("UNIRSE_SALA_KS: Intentando unir cliente " + cliente.getInetAddress().getHostAddress() + " a sala '" + idSala + "'");

        // Obtener datos de la sala del blackboard
        Map<String, Object> datosSala = handlerCommons.getDatosSala(idSala);

        if (datosSala == null) {
            System.err.println("UNIRSE_SALA_KS: Sala '" + idSala + "' no encontrada.");
            enviarRespuestaError(cliente, "La sala '" + idSala + "' no existe.");
            return;
        }

        // Intentar obtener la lista de jugadores (manejo seguro de tipos)
        List<Socket> jugadoresActuales;
        Object jugadoresObj = datosSala.get("jugadores");

        if (jugadoresObj instanceof List<?>) {
            // Intentamos hacer el cast, podría fallar si la lista no contiene Sockets
             try {
                  // Creamos una nueva lista para asegurar que sea mutable si la original no lo era
                  jugadoresActuales = new ArrayList<>((List<Socket>) jugadoresObj);
             } catch (ClassCastException e) {
                 System.err.println("UNIRSE_SALA_KS: Error crítico - La lista 'jugadores' en la sala '" + idSala + "' no contiene Sockets.");
                 enviarRespuestaError(cliente, "Error interno del servidor [Lista Jugadores Inválida].");
                 return;
             }
        } else {
            System.err.println("UNIRSE_SALA_KS: Error crítico - 'jugadores' no es una lista en la sala '" + idSala + "'.");
            enviarRespuestaError(cliente, "Error interno del servidor [Datos Sala Corruptos].");
            return;
        }


        // --- Lógica Principal para Unirse ---

        // Verificar si el jugador ya está en la sala
        if (jugadoresActuales.contains(cliente)) {
            System.out.println("UNIRSE_SALA_KS: Cliente " + cliente.getInetAddress().getHostAddress() + " ya está en la sala '" + idSala + "'.");
            // Enviar mensaje informativo (opcional)
            enviarRespuesta(cliente, "UNIDO_OK", Map.of("mensaje", "Ya estás en esta sala.", "idSala", idSala));
            return; // Ya está dentro, no hacer nada más
        }

        // Verificar si la sala está llena
        if (jugadoresActuales.size() >= MAX_JUGADORES_SALA) {
            System.out.println("UNIRSE_SALA_KS: Sala '" + idSala + "' está llena (" + jugadoresActuales.size() + "/" + MAX_JUGADORES_SALA + ").");
            enviarRespuestaError(cliente, "La sala '" + idSala + "' está llena.");
            return;
        }

        // --- El jugador puede unirse ---
        jugadoresActuales.add(cliente);
        System.out.println("UNIRSE_SALA_KS: Cliente " + cliente.getInetAddress().getHostAddress() + " añadido a la sala '" + idSala + "'.");

        // Actualizar la lista de jugadores en el mapa de datos de la sala
        datosSala.put("jugadores", jugadoresActuales); // Actualiza la referencia en el mapa local

        // Persistir el cambio en el Blackboard
        handlerCommons.actualizarDatosSala(idSala, datosSala);
        System.out.println("UNIRSE_SALA_KS: Datos de sala '" + idSala + "' actualizados en Blackboard.");

        // Notificar al jugador que se unió
        enviarRespuesta(cliente, "UNIDO_OK", Map.of("mensaje", "Te has unido a la sala '" + idSala + "'.", "idSala", idSala));

        // Notificar a los otros jugadores en la sala (si los hay)
        String jugadorUnidoHost = cliente.getInetAddress().getHostAddress(); // O usar un nombre si lo tuvieras
        Evento notificacionOtroJugador = new Evento("NUEVO_JUGADOR_EN_SALA");
        notificacionOtroJugador.agregarDato("idSala", idSala);
        notificacionOtroJugador.agregarDato("jugadorInfo", "Jugador " + jugadorUnidoHost); // Info del jugador que se unió

        for (Socket otroJugadorSocket : jugadoresActuales) {
            if (!otroJugadorSocket.equals(cliente)) { // No enviar al que acaba de unirse
                System.out.println("UNIRSE_SALA_KS: Notificando a " + otroJugadorSocket.getInetAddress().getHostAddress() + " sobre nuevo jugador.");
                server.enviarEventoACliente(otroJugadorSocket, notificacionOtroJugador);
            }
        }

        // Verificar si la sala está llena ahora para iniciar el juego
        if (jugadoresActuales.size() == MAX_JUGADORES_SALA) {
            System.out.println("UNIRSE_SALA_KS: ¡Sala '" + idSala + "' llena! Notificando para iniciar partida.");
            // Notificar al controller o directamente enviar un evento para iniciar la partida
             controller.notificarCambio("SALA_LLENA;" + idSala); // Pasar ID de sala al controller
               Evento iniciarPartida = new Evento("INICIAR_PARTIDA_SALA");
            iniciarPartida.agregarDato("idSala", idSala);
            handlerCommons.enviarEventoBlackBoard(cliente, iniciarPartida);

            // O podrías crear un nuevo evento y enviarlo al blackboard
             // Evento iniciarPartidaEvento = new Evento("INICIAR_PARTIDA_SALA");
             // iniciarPartidaEvento.agregarDato("idSala", idSala);
             // blackboard.enviarEventoBlackBoard(null, iniciarPartidaEvento); // Null como cliente origen
        }

        // --- Verificar si la sala está llena y disparar inicio de colocación ---
        if (jugadoresActuales.size() == MAX_JUGADORES_SALA) {
            System.out.println("UNIRSE_SALA_KS: ¡Sala '" + idSala + "' llena! Creando evento INICIAR_FASE_COLOCACION.");

            // Crear el evento para que otra KS inicie la fase de colocación
            Evento eventoInicioColocacion = new Evento("INICIAR_FASE_COLOCACION");
            eventoInicioColocacion.agregarDato("idSala", idSala);

            // Enviar el evento al blackboard para que lo procese la KS correspondiente
            // Usamos null como cliente origen porque es un evento interno del sistema.
            handlerCommons.enviarEventoBlackBoard(null, eventoInicioColocacion);

            // Ya NO notificamos "SALA_LLENA" al controller directamente,
            // la nueva KS se encargará de la lógica de inicio.
            // // controller.notificarCambio("SALA_LLENA;" + idSala); // <- QUITAR O COMENTAR

        } else {
             System.out.println("UNIRSE_SALA_KS: Sala '" + idSala + "' tiene " + jugadoresActuales.size() + " jugadores. Esperando más.");
        }

        // Indicar finalización del evento UNIRSE_SALA
        handlerCommons.respuestaFuenteC(cliente, evento);
    
    }

  

    private void enviarRespuestaError(Socket cliente, String mensajeError) {
        Evento respuesta = new Evento("ERROR_UNIRSE_SALA");
        respuesta.agregarDato("error", mensajeError);
        server.enviarEventoACliente(cliente, respuesta);
    }

    private void enviarRespuesta(Socket cliente, String tipoRespuesta, Map<String, Object> datos) {
        Evento respuesta = new Evento(tipoRespuesta);
        if (datos != null) {
            datos.forEach(respuesta::agregarDato);
        }
        server.enviarEventoACliente(cliente, respuesta);
    }
} // O tu paquete de knowledge sources // O tu paquete de knowledge sources
