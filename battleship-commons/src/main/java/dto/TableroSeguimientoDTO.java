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
// --- TableroSeguimientoDTO ---
public class TableroSeguimientoDTO implements Serializable {
    private List<RegistroDisparoSeguimientoDTO> registrosDisparos; // Más JSON-friendly que Map<PosicionDTO, ...>
    private int dimension;

    public TableroSeguimientoDTO() {}
    // Constructor, Getters y Setters...

    public List<RegistroDisparoSeguimientoDTO> getRegistrosDisparos() {
        return registrosDisparos;
    }

    public void setRegistrosDisparos(List<RegistroDisparoSeguimientoDTO> registrosDisparos) {
        this.registrosDisparos = registrosDisparos;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }
    
}