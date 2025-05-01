/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;


import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author javie
 */
public class Partida {
    private List<Jugador> jugadores;
    private EstadoPartida estado;
    private int turnoActual;

    public Partida() {
        jugadores = new ArrayList<>();
        estado = EstadoPartida.ESPERANDO_JUGADORES;
        turnoActual = 0;
    }

    public boolean estaCompleta() {
        return jugadores.size() == 2;
    }

    public Jugador obtenerJugadorEnTurno() {
        return jugadores.get(turnoActual);
    }

    public void cambiarTurno() {
        turnoActual = (turnoActual + 1) % jugadores.size();
    }

    public List<Jugador> getJugadores() {
        return jugadores;
    }

    public void setJugadores(List<Jugador> jugadores) {
        this.jugadores = jugadores;
    }

    public EstadoPartida getEstado() {
        return estado;
    }

    public void setEstado(EstadoPartida estado) {
        this.estado = estado;
    }

    public int getTurnoActual() {
        return turnoActual;
    }

    public void setTurnoActual(int turnoActual) {
        this.turnoActual = turnoActual;
    }
    
    
}
