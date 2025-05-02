/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model.entidades;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author javie
 */
public class Tablero {
    private List<Barco> barcos;
    private List<Disparo> disparosRecibidos;

    public Tablero() {
        barcos = new ArrayList<>();
        disparosRecibidos = new ArrayList<>();
    }

    public boolean colocarBarco(Barco barco) {
        for (Barco b : barcos) {
            for (Posicion p : b.getPosiciones()) {
                if (barco.contiene(p)) return false; 
            }
        }
        barcos.add(barco);
        return true;
    }

    public void registrarDisparo(Disparo disparo) {
        disparo.evaluarImpacto(barcos);
        disparosRecibidos.add(disparo);
    }

    public boolean verificarImpacto(Posicion posicion) {
        return barcos.stream().anyMatch(b -> b.contiene(posicion));
    }

    public List<Barco> getBarcos() {
        return barcos;
    }

    public void setBarcos(List<Barco> barcos) {
        this.barcos = barcos;
    }

    public List<Disparo> getDisparosRecibidos() {
        return disparosRecibidos;
    }

    public void setDisparosRecibidos(List<Disparo> disparosRecibidos) {
        this.disparosRecibidos = disparosRecibidos;
    }
    
    
}
