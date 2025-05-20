/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.blackboard;

import com.mycompany.battleship.commons.Evento;
import java.net.Socket;

/**
 *
 * @author caarl
 */

/**
 * Interfaz que define el contrato para handlers
 * dentro del patrón CHAIN-RESPONSABILITY.
 */
public interface IHandler {

    /**
     * Determina si esta fuente de conocimiento puede procesar el evento dado.
     * @param evento El evento a evaluar.
     * @return true si puede procesarlo, false en caso contrario.
     */
    boolean puedeProcesar(Evento evento);

    /**
     * Procesa el evento específico.
     * @param cliente El socket del cliente que originó la acción o está asociado al evento.
     * @param evento El evento a procesar.
     */
    void procesarEvento(Socket cliente, Evento evento);
}