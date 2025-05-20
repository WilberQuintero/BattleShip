/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package enums;

/**
 *
 * @author Hector
 */
public enum TipoNave {
    PORTAAVIONES(4), CRUCERO(3), SUBMARINO(2), BARCO_PATRULLA(1);
    public final int longitud;
    TipoNave(int longitud) { this.longitud = longitud; }
    public int getLongitud() { return longitud; }
}