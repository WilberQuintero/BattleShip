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
// --- JugadorDTO ---
public class JugadorDTO implements Serializable {
    private String nombre;
    private TableroFlotaDTO tableroFlota; // El tablero propio del jugador (completo) o del oponente (vista parcial)
    private TableroSeguimientoDTO tableroSeguimiento; // El tablero de seguimiento del jugador
    private boolean haConfirmadoTablero;
    // Podría incluirse un identificador único si el nombre no lo es.

    public JugadorDTO() {}
    // Constructor, Getters y Setters...

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TableroFlotaDTO getTableroFlota() {
        return tableroFlota;
    }

    public void setTableroFlota(TableroFlotaDTO tableroFlota) {
        this.tableroFlota = tableroFlota;
    }

    public TableroSeguimientoDTO getTableroSeguimiento() {
        return tableroSeguimiento;
    }

    public void setTableroSeguimiento(TableroSeguimientoDTO tableroSeguimiento) {
        this.tableroSeguimiento = tableroSeguimiento;
    }

    public boolean isHaConfirmadoTablero() {
        return haConfirmadoTablero;
    }

    public void setHaConfirmadoTablero(boolean haConfirmadoTablero) {
        this.haConfirmadoTablero = haConfirmadoTablero;
    }
    
    
}