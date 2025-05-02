/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model.dtos;

import Model.entidades.EstadoPartida;
import Model.entidades.Jugador;
import Model.entidades.Partida;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author javie
 */
public class PartidaDTO {
    private List<JugadorDTO> jugadoresDTO;
    private EstadoPartidaDTO estadoDTO;
    private int turnoActual;
    
    public PartidaDTO(){  
    }

    public PartidaDTO(List<JugadorDTO> jugadoresDTO, EstadoPartidaDTO estadoDTO, int turnoActual) {
        this.jugadoresDTO = jugadoresDTO;
        this.estadoDTO = estadoDTO;
        this.turnoActual = turnoActual;
    }
    
    public List<JugadorDTO> getJugadoresDTO() {
        return jugadoresDTO;
    }

    public void setJugadoresDTO(List<JugadorDTO> jugadoresDTO) {
        this.jugadoresDTO = jugadoresDTO;
    }

    public EstadoPartidaDTO getEstadoDTO() {
        return estadoDTO;
    }

    public void setEstadoDTO(EstadoPartidaDTO estadoDTO) {
        this.estadoDTO = estadoDTO;
    }

    public int getTurnoActual() {
        return turnoActual;
    }

    public void setTurnoActual(int turnoActual) {
        this.turnoActual = turnoActual;
    }
    
    public static PartidaDTO convertir(Partida partida) {
        List<JugadorDTO> jugadoresDTO = new ArrayList<>();
        for (Jugador jugador : partida.getJugadores()) {
            jugadoresDTO.add(JugadorDTO.convertir(jugador));
        }

        EstadoPartidaDTO estadoDTO = EstadoPartidaDTO.valueOf(partida.getEstado().name());

        return new PartidaDTO(jugadoresDTO, estadoDTO, partida.getTurnoActual());
    }
    
    public static Partida convertir(PartidaDTO partidaDTO) {
        List<Jugador> jugadores = new ArrayList<>();
        for (JugadorDTO jugadorDTO : partidaDTO.getJugadoresDTO()) {
            jugadores.add(JugadorDTO.convertir(jugadorDTO));
        }

        EstadoPartida estado = EstadoPartida.valueOf(partidaDTO.getEstadoDTO().name());

        Partida partida = new Partida();
        partida.setJugadores(jugadores);
        partida.setEstado(estado);
        partida.setTurnoActual(partidaDTO.getTurnoActual());

        return partida;
    }
    
}
