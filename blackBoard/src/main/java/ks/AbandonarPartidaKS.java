/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ks;

import com.mycompany.battleship.commons.Evento;
import com.mycompany.battleship.commons.IBlackboard;
import com.mycompany.battleship.commons.IServer;
import com.mycompany.blackboard.IKnowledgeSource;
import dto.JugadorDTO;
import dto.PartidaDTO;
import enums.EstadoPartida;
import java.net.Socket;

/**
 *
 * @author Wilber
 */
public class AbandonarPartidaKS implements IKnowledgeSource {

    private final IBlackboard blackboard;
    private final IServer server;

    public AbandonarPartidaKS(IBlackboard blackboard, IServer server) { // Asumo que no necesita Controller del backend
        this.blackboard = blackboard;
        this.server = server;
    }

    @Override
    public boolean puedeProcesar(Evento evento) {
        return evento != null && "ABANDONAR_PARTIDA".equalsIgnoreCase(evento.getTipo());
    }

    @Override
    public void procesarEvento(Socket clienteQueAbandona, Evento evento) {
        String idSala = (String) evento.obtenerDato("idSala");
        String nombreJugadorQueAbandona = (String) evento.obtenerDato("nombreJugador");

        if (idSala == null || nombreJugadorQueAbandona == null) {
            System.err.println("ABANDONAR_PARTIDA_KS: Faltan idSala o nombreJugador en el evento.");
            // Podrías enviar un error solo al cliente que intentó abandonar si es necesario
            return;
        }

        PartidaDTO partida = blackboard.getPartidaDTO(idSala);
        if (partida == null) {
            System.err.println("ABANDONAR_PARTIDA_KS: Partida no encontrada: " + idSala);
            return;
        }

        // No procesar si la partida ya terminó por otra razón
        if (partida.getEstado() == EstadoPartida.FINALIZADA || partida.getEstado() == EstadoPartida.ABANDONADA) {
            System.out.println("ABANDONAR_PARTIDA_KS: Partida '" + idSala + "' ya estaba finalizada o abandonada.");
            return;
        }

        JugadorDTO j1 = partida.getJugador1();
        JugadorDTO j2 = partida.getJugador2();
        String nombreGanadorPorAbandono = null;
        Socket socketOponente = null;

        if (j1 != null && j1.getNombre().equals(nombreJugadorQueAbandona)) {
            if (j2 != null) {
                nombreGanadorPorAbandono = j2.getNombre();
                socketOponente = blackboard.getSocketDeUsuario(nombreGanadorPorAbandono);
            }
        } else if (j2 != null && j2.getNombre().equals(nombreJugadorQueAbandona)) {
            if (j1 != null) {
                nombreGanadorPorAbandono = j1.getNombre();
                socketOponente = blackboard.getSocketDeUsuario(nombreGanadorPorAbandono);
            }
        } else {
            System.err.println("ABANDONAR_PARTIDA_KS: Jugador '" + nombreJugadorQueAbandona + "' no encontrado en partida '" + idSala + "'.");
            return; // Jugador no es parte de la partida
        }

        partida.setEstado(EstadoPartida.ABANDONADA); // O FINALIZADA
        partida.setNombreJugadorEnTurno(null); // Nadie más juega
        // Podrías añadir un campo al PartidaDTO para indicar quién abandonó si es relevante
        blackboard.actualizarPartida(partida);

        System.out.println("ABANDONAR_PARTIDA_KS: Jugador '" + nombreJugadorQueAbandona + "' abandonó. Ganador: '" + nombreGanadorPorAbandono + "' en sala '" + idSala + "'.");

        // Notificar a ambos clientes (o al que queda) sobre el fin de la partida
        Evento eventoFin = new Evento("FIN_PARTIDA"); // Usamos un tipo de evento consistente
        eventoFin.agregarDato("idSala", idSala);
        eventoFin.agregarDato("partidaTerminada", "true");
        if (nombreGanadorPorAbandono != null) {
            eventoFin.agregarDato("ganador", nombreGanadorPorAbandono);
        }
        eventoFin.agregarDato("motivo", "Jugador " + nombreJugadorQueAbandona + " abandonó la partida.");

        // Enviar al jugador que abandonó (para que vea la pantalla de derrota)
        if (clienteQueAbandona != null) {
            server.enviarEventoACliente(clienteQueAbandona, eventoFin);
        }
        // Enviar al oponente (el ganador)
        if (socketOponente != null && !socketOponente.equals(clienteQueAbandona)) { // No enviar dos veces si es el mismo socket
            server.enviarEventoACliente(socketOponente, eventoFin);
        } else if (socketOponente == null && nombreGanadorPorAbandono != null) {
            System.err.println("ABANDONAR_PARTIDA_KS: No se encontró socket para el ganador '" + nombreGanadorPorAbandono + "' para notificarle.");
        }

        blackboard.respuestaFuenteC(clienteQueAbandona, evento); // Notificar al blackboard
    }
}
