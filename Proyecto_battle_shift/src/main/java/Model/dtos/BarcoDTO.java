/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model.dtos;

import Model.entidades.Barco;
import Model.entidades.Posicion;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author javie
 */
public class BarcoDTO {
    private String tipo;
    private int tamanio;
    private List<PosicionDTO> posicionesDTO;
    private List<PosicionDTO> posicionesImpactadasDTO;

    public BarcoDTO() {
    }

    
    public BarcoDTO(String tipo, int tamanio, List<PosicionDTO> posicionesDTO, List<PosicionDTO> posicionesImpactadasDTO) {
        this.tipo = tipo;
        this.tamanio = tamanio;
        this.posicionesDTO = posicionesDTO;
        this.posicionesImpactadasDTO = posicionesImpactadasDTO;
    }
    
    public BarcoDTO(BarcoDTO barcoDTO) {
        this.tipo = barcoDTO.getTipo();
        this.tamanio = barcoDTO.getTamanio();
        this.posicionesDTO = barcoDTO.getPosicionesDTO();
        this.posicionesImpactadasDTO = barcoDTO.getPosicionesImpactadasDTO();
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

    public List<PosicionDTO> getPosicionesDTO() {
        return posicionesDTO;
    }

    public void setPosicionesDTO(List<PosicionDTO> posicionesDTO) {
        this.posicionesDTO = posicionesDTO;
    }

    public List<PosicionDTO> getPosicionesImpactadasDTO() {
        return posicionesImpactadasDTO;
    }

    public void setPosicionesImpactadasDTO(List<PosicionDTO> posicionesImpactadasDTO) {
        this.posicionesImpactadasDTO = posicionesImpactadasDTO;
    }
    
    public static BarcoDTO convertir(Barco barco) {
        List<PosicionDTO> posicionesDTO = new ArrayList<>();
        List<PosicionDTO> posicionesImpactadasDTO = new ArrayList<>();

        if (barco != null) {
            if (barco.getPosiciones() != null) {
                for (Posicion posicion : barco.getPosiciones()) {
                    if (posicion != null) {
                        posicionesDTO.add(PosicionDTO.convertir(posicion));
                    }
                }
            }

            if (barco.getPosicionesImpactadas() != null) {
                for (Posicion posicion : barco.getPosicionesImpactadas()) {
                    if (posicion != null) {
                        posicionesImpactadasDTO.add(PosicionDTO.convertir(posicion));
                    }
                }
            }

            return new BarcoDTO(
                barco.getTipo(),
                barco.getTamanio(),
                posicionesDTO,
                posicionesImpactadasDTO
            );
        }

        return null;
    }
    
    public static Barco convertir(BarcoDTO barcoDTO) {
        List<Posicion> posiciones = new ArrayList<>();
        List<Posicion> posicionesImpactadas = new ArrayList<>();

        if (barcoDTO != null) {
            if (barcoDTO.getPosicionesDTO() != null) {
                for (PosicionDTO posicionDTO : barcoDTO.getPosicionesDTO()) {
                    if (posicionDTO != null) {
                        posiciones.add(PosicionDTO.convertir(posicionDTO));
                    }
                }
            }

            if (barcoDTO.getPosicionesImpactadasDTO() != null) {
                for (PosicionDTO posicionDTO : barcoDTO.getPosicionesImpactadasDTO()) {
                    if (posicionDTO != null) {
                        posicionesImpactadas.add(PosicionDTO.convertir(posicionDTO));
                    }
                }
            }

            return new Barco(
                barcoDTO.getTipo(),
                barcoDTO.getTamanio(),
                posiciones,
                posicionesImpactadas
            );
        }

        return null;
    }
    
    
}
