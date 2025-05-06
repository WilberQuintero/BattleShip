/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package handlers;

/**
 *
 * @author caarl
 */
import com.mycompany.battleship.commons.Evento;
import com.mycompany.battleship.commons.IServer;
import com.mycompany.blackboard.Controller; // Controller del backend

import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.ArrayList; // Para crear la lista de flota
import com.mycompany.blackboard.IHandler;
import com.mycompany.battleship.commons.IHandlerCommons;

/**
 * Knowledge Source que se activa cuando una sala está llena
 * para iniciar la fase de colocación de barcos por parte de los jugadores.
 */
public class IniciarColocacionHandler implements IHandler { // O IHandler

    private final IHandlerCommons handlerCommons;
    private final IServer server;
    private final Controller controller;

    public IniciarColocacionHandler(IHandlerCommons blackboard, IServer server, Controller controller) {
        this.handlerCommons = blackboard;
        this.server = server;
        this.controller = controller;
    }

    @Override
    public boolean puedeProcesar(Evento evento) {
        return evento != null && "INICIAR_FASE_COLOCACION".equalsIgnoreCase(evento.getTipo());
    }

    @Override
    @SuppressWarnings("unchecked") // Por el casting de la lista de jugadores del Map
    public void procesarEvento(Socket clienteOrigen, Evento evento) { // clienteOrigen será null aquí
        String idSala = (String) evento.obtenerDato("idSala");
        if (idSala == null || idSala.isBlank()) {
            System.err.println("INICIAR_COLOCACION_KS: Evento no contenía idSala válido.");
            return;
        }

        System.out.println("INICIAR_COLOCACION_KS: Procesando inicio de colocación para sala: " + idSala);

        Map<String, Object> datosSala = handlerCommons.getDatosSala(idSala);
        if (datosSala == null) {
            System.err.println("INICIAR_COLOCACION_KS: No se encontraron datos para la sala " + idSala);
            return;
        }

        // Obtener los sockets de los jugadores en la sala
        List<Socket> jugadoresSockets = null;
        Object jugObj = datosSala.get("jugadores");
        if (jugObj instanceof List) {
            try {
                 jugadoresSockets = (List<Socket>) jugObj;
            } catch (ClassCastException e) { /* Ignorar o loguear */ }
        }

        if (jugadoresSockets == null || jugadoresSockets.size() != 2) {
            System.err.println("INICIAR_COLOCACION_KS: No se encontraron 2 jugadores válidos en la sala " + idSala + ". Jugadores encontrados: " + (jugadoresSockets != null ? jugadoresSockets.size() : "null"));
            // Podríamos intentar corregir el estado de la sala o simplemente no iniciar
            return;
        }

        // --- Definir la Flota Estándar ---
        // Podrías tener esto en una clase de configuración o constantes
        // Formato: "Nombre:Tamaño"
        List<String> flotaParaColocar = new ArrayList<>();
        flotaParaColocar.add("Portaaviones:4");
        flotaParaColocar.add("Crucero:3");
        flotaParaColocar.add("Submarino:2");
        flotaParaColocar.add("Barco:1");
        // Crear un string simple para enviar (JSON sería mejor a futuro)
        String flotaString = String.join(",", flotaParaColocar); // "Portaaviones:4,Crucero:3,..."


        // --- Actualizar Estado de la Sala en Blackboard ---
        datosSala.put("estado", "COLOCANDO_BARCOS");
        // Podríamos inicializar campos para guardar los tableros JSON aquí
        datosSala.put("tableroJsonJugador1", null); // Placeholder
        datosSala.put("tableroJsonJugador2", null); // Placeholder
        datosSala.put("jugador1ListoColocar", false); // Flags para saber cuándo ambos terminaron
        datosSala.put("jugador2ListoColocar", false);
        handlerCommons.actualizarDatosSala(idSala, datosSala);
        System.out.println("INICIAR_COLOCACION_KS: Estado de sala " + idSala + " actualizado a COLOCANDO_BARCOS.");


        // --- Enviar evento a los clientes ---
        Evento eventoCliente = new Evento("INICIAR_COLOCACION");
        eventoCliente.agregarDato("idSala", idSala);
        eventoCliente.agregarDato("flota", flotaString); // Enviar la lista de barcos a colocar

        System.out.println("INICIAR_COLOCACION_KS: Enviando evento INICIAR_COLOCACION a jugadores de sala " + idSala);
        for (Socket jugadorSocket : jugadoresSockets) {
            server.enviarEventoACliente(jugadorSocket, eventoCliente);
        }

        System.out.println("INICIAR_COLOCACION_KS: Proceso para sala " + idSala + " completado.");
        // No llamamos a respuestaFuenteC porque este evento fue interno (clienteOrigen es null)
    }
}