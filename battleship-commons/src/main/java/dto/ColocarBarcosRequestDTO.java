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
// --- ColocarBarcosRequestDTO ---
public class ColocarBarcosRequestDTO implements Serializable {
    private String idPartida;
    private String nombreJugador;
    private List<BarcoDTO> barcosColocados; // Cliente envía las posiciones y tipo de sus barcos
    public ColocarBarcosRequestDTO() {}
    // Constructor, Getters y Setters...

    public String getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(String idPartida) {
        this.idPartida = idPartida;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public void setNombreJugador(String nombreJugador) {
        this.nombreJugador = nombreJugador;
    }

    public List<BarcoDTO> getBarcosColocados() {
        return barcosColocados;
    }

    public void setBarcosColocados(List<BarcoDTO> barcosColocados) {
        this.barcosColocados = barcosColocados;
    }
    
}