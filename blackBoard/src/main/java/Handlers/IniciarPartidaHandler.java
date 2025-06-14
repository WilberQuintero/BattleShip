/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Handlers;
import com.mycompany.battleship.commons.Evento;
import com.mycompany.battleship.commons.IServer;
import com.mycompany.blackboard.Controller;

import java.net.Socket;
import java.util.*;
import com.mycompany.blackboard.IHandler;
import com.mycompany.battleship.commons.IHandlerCommons;
/**
 *
 * @author Hector
 */
public class IniciarPartidaHandler implements IHandler{
    
    // Dependencias (final hace que deban asignarse en el constructor)
    private final IServer server;
    private final IHandlerCommons blackboard;
    private final Controller controller; // Hacerlo final si siempre se requiere

    public IniciarPartidaHandler(IHandlerCommons blackboard, IServer server, Controller controller) {
          // Es buena práctica verificar que las dependencias no sean nulas
        if (blackboard == null || server == null || controller == null) {
            throw new IllegalArgumentException("Las dependencias (Blackboard, Server, Controller) no pueden ser nulas.");
        }
        this.blackboard = blackboard;
        this.server = server;
        this.controller = controller;
    }

    @Override
    public boolean puedeProcesar(Evento evento) {
        return evento != null && "INICIAR_PARTIDA_SALA".equalsIgnoreCase(evento.getTipo());
    }

    @Override
    public void procesarEvento(Socket cliente, Evento evento) {
        String idSala = (String) evento.obtenerDato("idSala");
        System.err.println("IniciarPartidaHandler INICIADA.");
        if (idSala == null) {
            System.err.println("IniciarPartidaHandler: idSala nulo.");
            return;
        }

        Map<String, Object> datosSala = blackboard.getDatosSala(idSala);
        if (datosSala == null) {
            System.err.println("IniciarPartidaHandler: Sala no encontrada.");
            return;
        }

        List<Socket> jugadores = (List<Socket>) datosSala.get("jugadores");
        if (jugadores == null || jugadores.size() != 2) {
            System.err.println("IniciarPartidaHandler: La sala no tiene exactamente 2 jugadores.");
            return;
        }

        // Simulación de barcos (nombre y posiciones fijas o aleatorias)
        for (Socket jugador : jugadores) {
            List<String> barcos = generarBarcos();
            System.out.println("Jugador: " + jugador.getInetAddress().getHostAddress());
            System.out.println("Socket: " + jugador);
            System.out.println("Barcos asignados:");
            barcos.forEach(b -> System.out.println(" - " + b));
            System.out.println("-----------");
        }

        // Se puede guardar los barcos en el mapa de datos de la sala si se desea continuar con lógica futura
        // datosSala.put("barcos_jugador_1", barcosJugador1);
        // datosSala.put("barcos_jugador_2", barcosJugador2);
        // blackboard.actualizarDatosSala(idSala, datosSala);

        blackboard.respuestaFuenteC(cliente, evento);
    }

    private List<String> generarBarcos() {
        return Arrays.asList(
            "Portaaviones A1-A4",
            "Crucero B1-B3",
            "Submarino C1-C2",
            "Barco D1-D1"
        );
    }
}
