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
// --- TableroFlotaDTO ---
public class TableroFlotaDTO implements Serializable {
    private List<BarcoDTO> barcos;
    private int dimension;
    // Para el oponente, la lista de barcos podría ser filtrada (solo mostrar barcos impactados/hundidos sin revelar todas sus posiciones)

    public TableroFlotaDTO() {}
    // Constructor, Getters y Setters...

    public List<BarcoDTO> getBarcos() {
        return barcos;
    }

    public void setBarcos(List<BarcoDTO> barcos) {
        this.barcos = barcos;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }
    
    
}
