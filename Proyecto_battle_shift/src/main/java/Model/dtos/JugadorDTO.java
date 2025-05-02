/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model.dtos;

import Model.entidades.Jugador;
import Model.entidades.Tablero;
import server.Server;

/**
 *
 * @author javie
 */
public class JugadorDTO {
    private String nombre;
    private TableroDTO tablero;
    private boolean enTurno;
    private Server servidor;

    public JugadorDTO() {
    }

    public JugadorDTO(String nombre, TableroDTO tablero, boolean enTurno, Server servidor) {
        this.nombre = nombre;
        this.tablero = tablero;
        this.enTurno = enTurno;
        this.servidor = servidor;
    }
    
    
    
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TableroDTO getTablero() {
        return tablero;
    }

    public void setTablero(TableroDTO tablero) {
        this.tablero = tablero;
    }

    public boolean isEnTurno() {
        return enTurno;
    }

    public void setEnTurno(boolean enTurno) {
        this.enTurno = enTurno;
    }

    public Server getServidor() {
        return servidor;
    }

    public void setServidor(Server servidor) {
        this.servidor = servidor;
    }
    
    public static JugadorDTO convertir(Jugador jugador) {
        TableroDTO tableroDTO = TableroDTO.convertir(jugador.getTablero());
        return new JugadorDTO(
            jugador.getNombre(),
            tableroDTO,
            jugador.isEnTurno(),
            jugador.getServidor()
        );
    }
    
    public static Jugador convertir(JugadorDTO jugadorDTO) {
        Tablero tablero = TableroDTO.convertir(jugadorDTO.getTablero());
        Jugador jugador = new Jugador(jugadorDTO.getNombre());
        jugador.setTablero(tablero);
        jugador.setEnTurno(jugadorDTO.isEnTurno());
        jugador.setServidor(jugadorDTO.getServidor());
        return jugador;
    }
    
    
}
