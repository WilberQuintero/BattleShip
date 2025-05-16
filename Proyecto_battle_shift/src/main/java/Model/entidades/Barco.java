/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model.entidades;

import enums.*;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

/**
 *
 * @author javie
 */
public class Barco {
    private TipoNave tipo;
    private int longitud;
    private List<Posicion> posicionesOcupadas;
    private Set<Posicion> posicionesImpactadas;
    private EstadoNave estado;
    private Orientacion orientacion; // Orientación con la que se colocó
    private Posicion posicionInicio; // Posición inicial para referencia

    public Barco(TipoNave tipo, Posicion posicionInicio, Orientacion orientacion) {
        this.tipo = tipo;
        this.longitud = tipo.getLongitud();
        this.posicionInicio = posicionInicio;
        this.orientacion = orientacion;
        this.posicionesOcupadas = calcularPosicionesOcupadas(posicionInicio, longitud, orientacion);
        this.posicionesImpactadas = new HashSet<>();
        this.estado = EstadoNave.INTACTA;
    }

    // Constructor alternativo si las posiciones ya están calculadas
    public Barco(TipoNave tipo, List<Posicion> posicionesOcupadas, Orientacion orientacion) {
        this.tipo = tipo;
        this.longitud = tipo.getLongitud(); // O posicionesOcupadas.size() si se confía en ello
        this.posicionesOcupadas = new ArrayList<>(posicionesOcupadas);
        this.posicionInicio = !posicionesOcupadas.isEmpty() ? posicionesOcupadas.get(0) : null; // Asume la primera como inicio
        this.orientacion = orientacion;
        this.posicionesImpactadas = new HashSet<>();
        this.estado = EstadoNave.INTACTA;
    }


    public static List<Posicion> calcularPosicionesOcupadas(Posicion inicio, int longitud, Orientacion orientacion) {
        List<Posicion> posiciones = new ArrayList<>();
        for (int i = 0; i < longitud; i++) {
            if (orientacion == Orientacion.HORIZONTAL) {
                posiciones.add(new Posicion(inicio.getX() + i, inicio.getY()));
            } else { // VERTICAL
                posiciones.add(new Posicion(inicio.getX(), inicio.getY() + i));
            }
        }
        return posiciones;
    }

    public boolean registrarImpacto(Posicion pos) {
        if (posicionesOcupadas.contains(pos) && !posicionesImpactadas.contains(pos)) {
            posicionesImpactadas.add(pos);
            actualizarEstado();
            return true;
        }
        return false; // Ya impactada o no es parte del barco
    }

    private void actualizarEstado() {
        if (posicionesImpactadas.size() == longitud) {
            this.estado = EstadoNave.HUNDIDA;
        } else if (!posicionesImpactadas.isEmpty()) {
            this.estado = EstadoNave.AVERIADA;
        } else {
            this.estado = EstadoNave.INTACTA;
        }
    }

    public boolean estaHundido() {
        return estado == EstadoNave.HUNDIDA;
    }

    public boolean ocupaPosicion(Posicion pos) {
        return posicionesOcupadas.contains(pos);
    }

    // Getters
    public TipoNave getTipo() { return tipo; }
    public int getLongitud() { return longitud; }
    public List<Posicion> getPosicionesOcupadas() { return new ArrayList<>(posicionesOcupadas); } // Devuelve copia
    public Set<Posicion> getPosicionesImpactadas() { return new HashSet<>(posicionesImpactadas); } // Devuelve copia
    public EstadoNave getEstado() { return estado; }
    public Orientacion getOrientacion() { return orientacion; }
    public Posicion getPosicionInicio() { return posicionInicio; }

    // Setters (usar con cuidado, preferiblemente manejar estado vía métodos)
    public void setEstado(EstadoNave estado) { this.estado = estado; }
    // No se suelen poner setters para tipo, longitud, posicionesOcupadas una vez creado,
    // a menos que la lógica del juego lo requiera explícitamente (raro para un barco ya colocado).

    @Override
    public String toString() {
        return tipo + ": " + longitud + ", " + posicionesOcupadas.getFirst().getX()+","+ posicionesOcupadas.getFirst().getY() + ", " + orientacion;
    }
    
    
}
