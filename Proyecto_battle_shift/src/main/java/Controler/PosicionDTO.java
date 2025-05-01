/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controler;

import Model.Posicion;

/**
 *
 * @author javie
 */
class PosicionDTO {
    private int x;
    private int y;

    public PosicionDTO() {
    }

    public PosicionDTO(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public PosicionDTO(PosicionDTO posicionDTO) {
        this.x = posicionDTO.getX();
        this.y = posicionDTO.getY();
    }
    
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
    
    public static Posicion convertir(PosicionDTO posicionDTO){
        Posicion posicion=new Posicion(posicionDTO.getX(),posicionDTO.getY());
        return posicion;
    }
    
    public static PosicionDTO convertir(Posicion posicion){
        PosicionDTO posicionDTO=new PosicionDTO(posicion.getX(),posicion.getY());
        return posicionDTO;
    }
}
