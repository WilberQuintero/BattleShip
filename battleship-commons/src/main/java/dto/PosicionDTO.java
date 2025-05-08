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
// --- PosicionDTO ---
public class PosicionDTO implements Serializable {
    private int x;
    private int y;
    public PosicionDTO() {}
    public PosicionDTO(int x, int y) { this.x = x; this.y = y; }
    // Getters y Setters...
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    // Es buena idea implementar equals() y hashCode() si se usan en Sets o como claves de Map
    @Override public boolean equals(Object o) { /* ... */ return false; }
    @Override public int hashCode() { /* ... */ return 0; }
}