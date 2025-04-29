/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author javie
 */
public class Barco {
    private String tipo;
    private int tamanio;
    private List<Posicion> posiciones;
    private List<Posicion> posicionesImpactadas;

    public Barco(String tipo, int tamanio) {
        this.tipo = tipo;
        this.tamanio = tamanio;
        this.posiciones = new ArrayList<>();
        this.posicionesImpactadas = new ArrayList<>();
    }

    public void colocar(Posicion inicio, boolean horizontal) {
        posiciones.clear();
        for (int i = 0; i < tamanio; i++) {
            int x = inicio.getX() + (horizontal ? i : 0);
            int y = inicio.getY() + (horizontal ? 0 : i);
            posiciones.add(new Posicion(x, y));
        }
    }

    public boolean contiene(Posicion posicion) {
        return posiciones.contains(posicion);
    }

    public void registrarImpacto(Posicion posicion) {
        if (contiene(posicion) && !posicionesImpactadas.contains(posicion)) {
            posicionesImpactadas.add(posicion);
        }
    }

    public boolean estaHundido() {
        return posicionesImpactadas.containsAll(posiciones);
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getTamanio() {
        return tamanio;
    }

    public void setTamanio(int tamanio) {
        this.tamanio = tamanio;
    }

    public List<Posicion> getPosiciones() {
        return posiciones;
    }

    public void setPosiciones(List<Posicion> posiciones) {
        this.posiciones = posiciones;
    }

    public List<Posicion> getPosicionesImpactadas() {
        return posicionesImpactadas;
    }

    public void setPosicionesImpactadas(List<Posicion> posicionesImpactadas) {
        this.posicionesImpactadas = posicionesImpactadas;
    }
}
