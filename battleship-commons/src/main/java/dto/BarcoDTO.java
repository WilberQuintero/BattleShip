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
// --- BarcoDTO ---
public class BarcoDTO implements Serializable {
    private TipoNave tipo;
    private int longitud; // Podría obtenerse de TipoNave, pero explícito es más simple para DTO
    private List<PosicionDTO> posicionesOcupadas;
    private Set<PosicionDTO> posicionesImpactadas; // Estado de los impactos
    private EstadoNave estado;
    private Orientacion orientacion; // Orientación original
    // Podrías añadir PosicionDTO posicionInicio si fuera necesario para reconstruir la orientación visualmente.

    public BarcoDTO() {}
    // Constructor, Getters y Setters...

    public TipoNave getTipo() {
        return tipo;
    }

    public void setTipo(TipoNave tipo) {
        this.tipo = tipo;
    }

    public int getLongitud() {
        return longitud;
    }

    public void setLongitud(int longitud) {
        this.longitud = longitud;
    }

    public List<PosicionDTO> getPosicionesOcupadas() {
        return posicionesOcupadas;
    }

    public void setPosicionesOcupadas(List<PosicionDTO> posicionesOcupadas) {
        this.posicionesOcupadas = posicionesOcupadas;
    }

    public Set<PosicionDTO> getPosicionesImpactadas() {
        return posicionesImpactadas;
    }

    public void setPosicionesImpactadas(Set<PosicionDTO> posicionesImpactadas) {
        this.posicionesImpactadas = posicionesImpactadas;
    }

    public EstadoNave getEstado() {
        return estado;
    }

    public void setEstado(EstadoNave estado) {
        this.estado = estado;
    }

    public Orientacion getOrientacion() {
        return orientacion;
    }

    public void setOrientacion(Orientacion orientacion) {
        this.orientacion = orientacion;
    }
    
}
