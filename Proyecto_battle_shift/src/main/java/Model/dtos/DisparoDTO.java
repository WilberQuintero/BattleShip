/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model.dtos;

import Model.entidades.Disparo;
import Model.entidades.Posicion;

/**
 *
 * @author javie
 */
public class DisparoDTO {
    private PosicionDTO posicion;
    private boolean impacto;

    public DisparoDTO() {
    }

    public DisparoDTO(PosicionDTO posicion, boolean impacto) {
        this.posicion = posicion;
        this.impacto = impacto;
    }

    public PosicionDTO getPosicion() {
        return posicion;
    }

    public void setPosicion(PosicionDTO posicion) {
        this.posicion = posicion;
    }

    public boolean isImpacto() {
        return impacto;
    }

    public void setImpacto(boolean impacto) {
        this.impacto = impacto;
    }
    
    
    public static DisparoDTO convertir(Disparo disparo) {
        PosicionDTO posicionDTO = PosicionDTO.convertir(disparo.getPosicion());
        return new DisparoDTO(posicionDTO, disparo.isImpacto());
    }
    
    
    public static Disparo convertir(DisparoDTO disparoDTO) {
        Posicion posicion = PosicionDTO.convertir(disparoDTO.getPosicion());
        return new Disparo(posicion, disparoDTO.isImpacto());
    }
    
}
