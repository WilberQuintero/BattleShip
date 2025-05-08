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
// --- RegistroDisparoSeguimientoDTO (para TableroSeguimientoDTO) ---
public class RegistroDisparoSeguimientoDTO implements Serializable {
    private PosicionDTO posicion;
    private ResultadoDisparo resultado;

    public RegistroDisparoSeguimientoDTO() {}
    public RegistroDisparoSeguimientoDTO(PosicionDTO posicion, ResultadoDisparo resultado) {
        this.posicion = posicion;
        this.resultado = resultado;
    }
    // Getters y Setters...

    public PosicionDTO getPosicion() {
        return posicion;
    }

    public void setPosicion(PosicionDTO posicion) {
        this.posicion = posicion;
    }

    public ResultadoDisparo getResultado() {
        return resultado;
    }

    public void setResultado(ResultadoDisparo resultado) {
        this.resultado = resultado;
    }
    
}
