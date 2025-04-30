/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.battleship.commons;

import java.net.Socket;
import java.util.Map;

/**
 *
 * @author caarl
 */

public interface IBlackboard {
    // Método principal que el Server usará:
    void enviarEventoBlackBoard(Socket cliente, Evento evento);
    boolean existeSala(String idSala);
    void agregarSala(String id, Map<String, Object> datosSala);
    // Añade otros métodos si el Server necesita interactuar más con el Blackboard
}
