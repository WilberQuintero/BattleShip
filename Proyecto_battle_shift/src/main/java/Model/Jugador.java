/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author javie
 */
public class Jugador {
    private String nombre;
    private Tablero tablero;
    private boolean enTurno;

    public Jugador(String nombre) {
        this.nombre = nombre;
        this.tablero = new Tablero();
        this.enTurno = false;
    }

    public boolean todosLosBarcosHundidos() {
        return tablero.getBarcos().stream().allMatch(Barco::estaHundido);
    }

    public Barco obtenerBarcoEnPosicion(Posicion posicion) {
        return tablero.getBarcos().stream()
                .filter(b -> b.contiene(posicion))
                .findFirst().orElse(null);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Tablero getTablero() {
        return tablero;
    }

    public void setTablero(Tablero tablero) {
        this.tablero = tablero;
    }

    public boolean isEnTurno() {
        return enTurno;
    }

    public void setEnTurno(boolean enTurno) {
        this.enTurno = enTurno;
    }
    
    
}

