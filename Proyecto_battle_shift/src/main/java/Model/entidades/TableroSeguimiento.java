/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model.entidades;
import enums.*;
import java.util.Map;
import java.util.HashMap;
/**
 *
 * @author Hector
 */
public class TableroSeguimiento {
    private Map<Posicion, ResultadoDisparo> registrosDisparos;
    private final int dimension;

    public TableroSeguimiento(int dimension) {
        this.dimension = dimension;
        this.registrosDisparos = new HashMap<>();
    }

    public void marcarDisparo(Posicion pos, ResultadoDisparo resultado) {
        if (pos.getX() >= 0 && pos.getX() < dimension && pos.getY() >= 0 && pos.getY() < dimension) {
            registrosDisparos.put(pos, resultado);
        }
    }

    public boolean yaSeDisparoEn(Posicion pos) {
        return registrosDisparos.containsKey(pos);
    }

    public ResultadoDisparo obtenerResultadoEn(Posicion pos) {
        return registrosDisparos.get(pos); // Devuelve null si no hay registro
    }

    // Getters
    public Map<Posicion, ResultadoDisparo> getRegistrosDisparos() { return new HashMap<>(registrosDisparos); } // Devuelve copia
    public int getDimension() { return dimension; }
}