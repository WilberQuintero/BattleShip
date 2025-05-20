/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;
import enums.*;
import java.io.Serializable;
import java.util.List;
import java.util.Set; // O List para JSON más simple si Set da problemas con alguna librería
import java.util.Map; // O una estructura de lista para JSON
/**
 *
 * @author Hector
 */
// --- PartidaDTO ---
public class PartidaDTO implements Serializable {
    private String idPartida; // Un identificador único para la partida
    private JugadorDTO jugador1;
    private JugadorDTO jugador2;
    private String nombreJugadorEnTurno;
    private EstadoPartida estado;
    // Podrías añadir una lista de últimos eventos o mensajes si es necesario

    public PartidaDTO() {}
    // Constructor, Getters y Setters...
 public PartidaDTO(String idPartida, JugadorDTO jugador1, EstadoPartida estadoInicial) {
        this.idPartida = idPartida;
        this.jugador1 = jugador1;
        this.estado = estadoInicial;
        this.jugador2 = null;
        this.nombreJugadorEnTurno = null;
    }
    public String getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(String idPartida) {
        this.idPartida = idPartida;
    }

    public JugadorDTO getJugador1() {
        return jugador1;
    }

    public void setJugador1(JugadorDTO jugador1) {
        this.jugador1 = jugador1;
    }

    public JugadorDTO getJugador2() {
        return jugador2;
    }

    public void setJugador2(JugadorDTO jugador2) {
        this.jugador2 = jugador2;
    }

    public String getNombreJugadorEnTurno() {
        return nombreJugadorEnTurno;
    }

    public void setNombreJugadorEnTurno(String nombreJugadorEnTurno) {
        this.nombreJugadorEnTurno = nombreJugadorEnTurno;
    }

    public EstadoPartida getEstado() {
        return estado;
    }

    public void setEstado(EstadoPartida estado) {
        this.estado = estado;
    }
    
}

