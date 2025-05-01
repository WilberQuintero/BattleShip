/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controler;

import Model.Barco;
import Model.Disparo;
import Model.Tablero;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author javie
 */
public class TableroDTO {
    private List<BarcoDTO> barcosDTO;
    private List<DisparoDTO> disparosRecibidosDTO;
    
    public TableroDTO(){
        
    }

    public TableroDTO(List<BarcoDTO> barcos, List<DisparoDTO> disparosRecibidos) {
        this.barcosDTO = barcos;
        this.disparosRecibidosDTO = disparosRecibidos;
    }
    
    

    public List<BarcoDTO> getBarcos() {
        return barcosDTO;
    }

    public void setBarcos(List<BarcoDTO> barcosDTO) {
        this.barcosDTO = barcosDTO;
    }

    public List<DisparoDTO> getDisparosRecibidos() {
        return disparosRecibidosDTO;
    }

    public void setDisparosRecibidos(List<DisparoDTO> disparosRecibidosDTO) {
        this.disparosRecibidosDTO = disparosRecibidosDTO;
    }
    
   public static TableroDTO convertir(Tablero tablero) {
        List<BarcoDTO> barcosDTO = new ArrayList<>();
        List<DisparoDTO> disparosDTO = new ArrayList<>();

        if (tablero != null) {
            if (tablero.getBarcos() != null && !tablero.getBarcos().isEmpty()) {
                for (Barco barco : tablero.getBarcos()) {
                    if (barco != null) {
                        barcosDTO.add(BarcoDTO.convertir(barco));
                    }
                }
            }

            if (tablero.getDisparosRecibidos() != null && !tablero.getDisparosRecibidos().isEmpty()) {
                for (Disparo disparo : tablero.getDisparosRecibidos()) {
                    if (disparo != null) {
                        disparosDTO.add(DisparoDTO.convertir(disparo));
                    }
                }
            }
        }

        return new TableroDTO(barcosDTO, disparosDTO);
    }
   
   public static Tablero convertir(TableroDTO tableroDTO) {
        List<Barco> barcos = new ArrayList<>();
        List<Disparo> disparos = new ArrayList<>();

        if (tableroDTO != null) {
            if (tableroDTO.getBarcos() != null) {
                for (BarcoDTO barcoDTO : tableroDTO.getBarcos()) {
                    if (barcoDTO != null) {
                        barcos.add(BarcoDTO.convertir(barcoDTO));
                    }
                }
            }

            if (tableroDTO.getDisparosRecibidos() != null) {
                for (DisparoDTO disparoDTO : tableroDTO.getDisparosRecibidos()) {
                    if (disparoDTO != null) {
                        disparos.add(DisparoDTO.convertir(disparoDTO));
                    }
                }
            }
        }

        Tablero tablero = new Tablero();
        tablero.setBarcos(barcos);
        tablero.setDisparosRecibidos(disparos);
        return tablero;
    }
    
    
}
