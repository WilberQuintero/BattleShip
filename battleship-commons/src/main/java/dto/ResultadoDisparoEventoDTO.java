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
// --- ResultadoDisparoEventoDTO (para comunicar el resultado de un disparo) ---
public class ResultadoDisparoEventoDTO implements Serializable {
    private PosicionDTO posicionAtacada;
    private ResultadoDisparo resultado; // AGUA, IMPACTO, HUNDIDO
    private String nombreJugadorAtacante;
    private String nombreJugadorAtacado;
    private EstadoNave estadoNaveImpactada; // Si fue IMPACTO o HUNDIDO
    private TipoNave tipoNaveImpactada; // Si fue HUNDIDO, qué tipo de barco fue
    private boolean turnoExtra; // Si el atacante tiene otro turno

    public ResultadoDisparoEventoDTO() {}
    // Constructor, Getters y Setters...

    public PosicionDTO getPosicionAtacada() {
        return posicionAtacada;
    }

    public void setPosicionAtacada(PosicionDTO posicionAtacada) {
        this.posicionAtacada = posicionAtacada;
    }

    public ResultadoDisparo getResultado() {
        return resultado;
    }

    public void setResultado(ResultadoDisparo resultado) {
        this.resultado = resultado;
    }

    public String getNombreJugadorAtacante() {
        return nombreJugadorAtacante;
    }

    public void setNombreJugadorAtacante(String nombreJugadorAtacante) {
        this.nombreJugadorAtacante = nombreJugadorAtacante;
    }

    public String getNombreJugadorAtacado() {
        return nombreJugadorAtacado;
    }

    public void setNombreJugadorAtacado(String nombreJugadorAtacado) {
        this.nombreJugadorAtacado = nombreJugadorAtacado;
    }

    public EstadoNave getEstadoNaveImpactada() {
        return estadoNaveImpactada;
    }

    public void setEstadoNaveImpactada(EstadoNave estadoNaveImpactada) {
        this.estadoNaveImpactada = estadoNaveImpactada;
    }

    public TipoNave getTipoNaveImpactada() {
        return tipoNaveImpactada;
    }

    public void setTipoNaveImpactada(TipoNave tipoNaveImpactada) {
        this.tipoNaveImpactada = tipoNaveImpactada;
    }

    public boolean isTurnoExtra() {
        return turnoExtra;
    }

    public void setTurnoExtra(boolean turnoExtra) {
        this.turnoExtra = turnoExtra;
    }
    
}
