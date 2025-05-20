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
// --- ConfirmarTableroRequestDTO ---
public class ConfirmarTableroRequestDTO implements Serializable {
    private String idPartida;
    private String nombreJugador;
    // No necesita más datos si los barcos ya se enviaron con ColocarBarcosRequestDTO
    // O si ColocarBarcosRequestDTO implícitamente confirma.
    public ConfirmarTableroRequestDTO() {}
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
    
}