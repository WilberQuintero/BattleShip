/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.blackboard;
import java.util.HashSet;
import java.util.Set;
/**
 *
 * @author Hector
 */
public class Sala {
      private String id;
    private Set<String> jugadores;

    public Sala(String id) {
        this.id = id;
        this.jugadores = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public void agregarJugador(String jugador) {
        jugadores.add(jugador);
    }

    public Set<String> getJugadores() {
        return jugadores;
    }
}
