/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model.entidades;

import java.io.IOException;

import enums.*;

/**
 *
 * @author javie
 */
public class Jugador {
    private String nombre;
    private TableroFlota tableroFlota;
    private TableroSeguimiento tableroSeguimiento;
    private boolean haConfirmadoTablero; // Estado de confirmación del tablero

    public Jugador(String nombre, int dimensionTablero) {
        this.nombre = nombre;
        this.tableroFlota = new TableroFlota(dimensionTablero);
        this.tableroSeguimiento = new TableroSeguimiento(dimensionTablero);
        this.haConfirmadoTablero = false;
    }

    // Este método sería llamado por la Partida, que le pasaría el tablero del oponente.
    // No es que el jugador "dispare" directamente a un tablero que no conoce.
    // La Partida orquesta esto. Por ahora, solo la firma.
    public ResultadoDisparo realizarDisparo(Posicion pos, TableroFlota tableroOponente) {
        // Lógica para registrar el disparo en el tablero de seguimiento PROPIO
        // y obtener el resultado del tablero del OPONENTE.
        // Este método como tal podría no existir en el jugador si la Partida lo maneja todo.
        // O, si el jugador tiene una referencia al tablero del oponente para disparar (lo cual no es típico).
        // Para el diagrama, mantendremos la firma.
        // La partida llamaría a tableroOponente.recibirDisparo(pos)
        // y luego este jugador actualizaría su tableroSeguimiento.
        // Por ahora, lo dejamos conceptual.
        if (tableroSeguimiento.yaSeDisparoEn(pos)) {
             // Manejar error o advertencia: ya se disparó aquí
            return tableroSeguimiento.obtenerResultadoEn(pos); // Devolver el resultado anterior
        }
        
        ResultadoDisparo resultado = tableroOponente.recibirDisparo(pos);
        tableroSeguimiento.marcarDisparo(pos, resultado);
        return resultado;
    }

    public boolean colocarBarco(TipoNave tipo, Posicion inicio, Orientacion orientacion) {
        if (haConfirmadoTablero) {
            // Opcional: lanzar excepción o devolver false si el tablero ya está confirmado
            System.err.println("El tablero de " + nombre + " ya está confirmado. No se pueden añadir más barcos.");
            return false;
        }
        Barco nuevoBarco = new Barco(tipo, inicio, orientacion);
        return tableroFlota.agregarBarco(nuevoBarco);
    }

    public boolean todosLosBarcosHundidos() {
        return tableroFlota.estanTodosBarcosHundidos();
    }
   // --- NUEVOS SETTERS para los tableros ---
    /**
     * Establece el tablero de flota del jugador.
     * Usar con precaución, principalmente para reconstrucción desde DTOs.
     * @param tableroFlota El nuevo tablero de flota.
     */
    public void setTableroFlota(TableroFlota tableroFlota) {
        this.tableroFlota = tableroFlota;
    }

    /**
     * Establece el tablero de seguimiento del jugador.
     * Usar con precaución, principalmente para reconstrucción desde DTOs.
     * @param tableroSeguimiento El nuevo tablero de seguimiento.
     */
    public void setTableroSeguimiento(TableroSeguimiento tableroSeguimiento) {
        this.tableroSeguimiento = tableroSeguimiento;
    }
    // Getters y Setters
    public String getNombre() { return nombre; }
    public TableroFlota getTableroFlota() { return tableroFlota; }
    public TableroSeguimiento getTableroSeguimiento() { return tableroSeguimiento; }
    public boolean haConfirmadoTablero() { return haConfirmadoTablero; }
    public void setHaConfirmadoTablero(boolean haConfirmadoTablero) { this.haConfirmadoTablero = haConfirmadoTablero; }
    public void setNombre(String nombre) { this.nombre = nombre; } // Si se permite cambiar nombre
    // No se deberían reemplazar los tableros directamente una vez creados con el jugador.
}

