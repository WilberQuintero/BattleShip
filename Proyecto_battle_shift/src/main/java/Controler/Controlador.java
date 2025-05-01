/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controler;

import Model.Jugador;
import Model.Partida;
import java.io.IOException;
import server.Server;

/**
 *
 * @author javie
 */
public class Controlador {
    JugadorDTO jugadorDTOActual;
    
    public Controlador() {
        jugadorDTOActual=new JugadorDTO();
    }
        
    public void crearServidor(JugadorDTO jugadorDTO, int ip) throws IOException{
        try {
            Jugador jugador = JugadorDTO.convertir(jugadorDTO);
            Server server=jugador.crearServidor(ip);
            jugadorDTOActual.setServidor(server);
            jugadorDTOActual=jugadorDTO;
        } catch (IOException iOException) {
            System.out.println(iOException.getMessage());
        }
    }
    
    public boolean crearPartida(){
        Jugador jugador = JugadorDTO.convertir(jugadorDTOActual);
        jugador.setEnTurno(true);
        Partida partida=new Partida();
        return partida.crearPartida(jugador);
    }
    
    public boolean unirsePartida(int ipPartida){
        Jugador jugador = JugadorDTO.convertir(jugadorDTOActual);
        Partida partida=new Partida();
        return partida.unirsePartida(jugador, ipPartida);
    }
    
    
}
