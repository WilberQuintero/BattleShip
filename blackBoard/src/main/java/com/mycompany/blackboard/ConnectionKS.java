/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.blackboard;

/**
 *
 * @author caarl
 */


import com.mycompany.battleship.commons.Evento;
import com.mycompany.battleship.commons.IServer;

import java.net.Socket;

/**
 * Knowledge Source para manejar el evento de conexión inicial de un usuario.
 * Adaptada al estilo Dominó.
 */
public class ConnectionKS implements IKnowledgeSource {

    private final BlackBoard blackboard;
    private final IServer server; // Podría necesitar el server para enviar respuestas directas
    private final Controller controller; // Podría necesitar notificar cambios específicos

    public ConnectionKS(BlackBoard blackboard, IServer server, Controller controller) {
        this.blackboard = blackboard;
        this.server = server;
        this.controller = controller;
    }

    /**
     * Verifica si puede procesar el evento (solo "CONECTAR_USUARIO_SERVER").
     * @param evento El evento a evaluar.
     * @return true si es un evento de conexión, false en caso contrario.
     */
    @Override
    public boolean puedeProcesar(Evento evento) {
        return evento != null && "CONECTAR_USUARIO_SERVER".equals(evento.getTipo());
    }

    /**
     * Procesa el evento de conexión.
     * @param cliente El socket del cliente que se acaba de conectar.
     * @param evento El evento (tipo "CONECTAR_USUARIO_SERVER").
     */
    @Override
    public void procesarEvento(Socket cliente, Evento evento) {
        if (cliente == null) {
            System.err.println("ConnectionKS ERROR: Se recibió un socket nulo para procesar.");
            return;
        }
        System.out.println("ConnectionKS: Procesando evento '" + evento.getTipo() + "' para " + cliente.getInetAddress().getHostAddress());

        // Acción principal: Registrar el socket del cliente en el BlackBoard
        blackboard.agregarClienteSocket(cliente);

        // Acción secundaria: Notificar al BlackBoard que se completó (Estilo Dominó)
        // Podríamos crear un evento de respuesta si fuera necesario
        Evento eventoRespuesta = new Evento("CONEXION_PROCESADA");
        eventoRespuesta.agregarDato("clienteHost", cliente.getInetAddress().getHostAddress());
        blackboard.respuestaFuenteC(cliente, eventoRespuesta);

        // Acción terciaria (Opcional aquí, podría ir en el Controller al recibir
        // la notificación de "CLIENTE_CONECTADO_REGISTRADO"): Enviar bienvenida
        // try {
        //     server.enviarMensajeACliente(cliente, "EVENTO;TIPO=BIENVENIDA;MSG=Conectado al servidor de Batalla Naval!");
        // } catch (IOException e) {
        //     System.err.println("ConnectionKS ERROR: No se pudo enviar mensaje de bienvenida a " + cliente.getInetAddress().getHostAddress());
        // }
    }
}