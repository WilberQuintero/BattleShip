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
// --- UnirseAPartidaRequestDTO ---
public class UnirseAPartidaRequestDTO implements Serializable {
    private String idPartida;
    private String nombreJugador2;
    public UnirseAPartidaRequestDTO() {}
    // Constructor, Getters y Setters...

    public String getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(String idPartida) {
        this.idPartida = idPartida;
    }

    public String getNombreJugador2() {
        return nombreJugador2;
    }

    public void setNombreJugador2(String nombreJugador2) {
        this.nombreJugador2 = nombreJugador2;
    }
    
}
