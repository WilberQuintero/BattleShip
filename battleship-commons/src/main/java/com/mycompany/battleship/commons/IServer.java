/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.battleship.commons;

/**
 *
 * @author caarl
 */

import java.net.Socket;

public interface IServer {
    // Métodos que el Controller o KS podrían necesitar:
    void enviarMensajeACliente(Socket cliente, String mensaje);
    void enviarMensajeATodos(String mensaje);
    void enviarEventoACliente(Socket cliente, Evento evento); // Si conviertes evento a string en Server
    void clienteDesconectado(Socket cliente); // Para notificar al server sobre desconexiones detectadas
    // Añade otros métodos que necesites exponer del Server al Blackboard/Controller/KS
}