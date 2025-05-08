/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;
import enums.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Set; // O List para JSON más simple si Set da problemas con alguna librería
import java.util.Map; // O una estructura de lista para JSON
/**
 *
 * @author Hector
 */
// --- TableroSeguimientoDTO ---
public class TableroSeguimientoDTO implements Serializable {
     private Map<PosicionDTO, ResultadoDisparo> registrosDisparos;
    private int dimension;

    public TableroSeguimientoDTO() {}
    // Constructor, Getters y Setters...
  public TableroSeguimientoDTO(int dimension) {
        this.dimension = dimension;
        this.registrosDisparos = new HashMap<>();
    }

    public Map<PosicionDTO, ResultadoDisparo> getRegistrosDisparos() {
        return registrosDisparos;
    }

    public void setRegistrosDisparos(Map<PosicionDTO, ResultadoDisparo> registrosDisparos) {
        this.registrosDisparos = registrosDisparos;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }
    
}