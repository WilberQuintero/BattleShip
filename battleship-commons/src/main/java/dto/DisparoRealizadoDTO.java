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
// --- DisparoRealizadoDTO (para comunicar un evento de disparo) ---
public class DisparoRealizadoDTO implements Serializable {
    private PosicionDTO posicionAtaque;
    private String nombreJugadorAutor; // Quién realizó el disparo
    // El resultado no se incluye aquí, ya que esto es la ACCIÓN de disparar.
    // El resultado vendría en una respuesta del servidor.
    public DisparoRealizadoDTO() {}
    // Constructor, Getters y Setters...

    public PosicionDTO getPosicionAtaque() {
        return posicionAtaque;
    }

    public void setPosicionAtaque(PosicionDTO posicionAtaque) {
        this.posicionAtaque = posicionAtaque;
    }

    public String getNombreJugadorAutor() {
        return nombreJugadorAutor;
    }

    public void setNombreJugadorAutor(String nombreJugadorAutor) {
        this.nombreJugadorAutor = nombreJugadorAutor;
    }
    
}