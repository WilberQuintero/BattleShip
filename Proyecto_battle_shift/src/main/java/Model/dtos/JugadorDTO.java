/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model.dtos;

import Model.entidades.Jugador;
import Model.entidades.Tablero;

/**
 *
 * @author javie
 */
public class JugadorDTO {
    private String nombre;
    private TableroDTO tablero;
    private boolean enTurno;
    

    public JugadorDTO() {
    }

    public JugadorDTO(String nombre, TableroDTO tablero, boolean enTurno) {
        this.nombre = nombre;
        this.tablero = tablero;
        this.enTurno = enTurno;
        
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

   
    
    public static JugadorDTO convertir(Jugador jugador) {
        TableroDTO tableroDTO = TableroDTO.convertir(jugador.getTablero());
        return new JugadorDTO(
            jugador.getNombre(),
            tableroDTO,
            jugador.isEnTurno()
            
        );
    }
    
    public static Jugador convertir(JugadorDTO jugadorDTO) {
        Tablero tablero = TableroDTO.convertir(jugadorDTO.getTablero());
        Jugador jugador = new Jugador(jugadorDTO.getNombre());
        jugador.setTablero(tablero);
        jugador.setEnTurno(jugadorDTO.isEnTurno());
      
        return jugador;
    }
    
    
}
