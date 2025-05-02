/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model.entidades;

import java.util.List;

/**
 *
 * @author javie
 */
public class Disparo {
    private Posicion posicion;
    private boolean impacto;

    public Disparo(Posicion posicion) {
        this.posicion = posicion;
        this.impacto = false;
    }

    public Disparo(Posicion posicion, boolean impacto) {
        this.posicion = posicion;
        this.impacto = impacto;
    }
    
    public void evaluarImpacto(List<Barco> barcos) {
        for (Barco barco : barcos) {
            if (barco.contiene(posicion)) {
                barco.registrarImpacto(posicion);
                impacto = true;
                break;
            }
        }
    }

    public boolean isImpacto() {
        return impacto;
    }

    public Posicion getPosicion() {
        return posicion;
    }

    public void setPosicion(Posicion posicion) {
        this.posicion = posicion;
    }
    
    
}
