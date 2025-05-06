/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package handlers;

/**
 *
 * @author caarl
 */
//olas

import com.mycompany.battleship.commons.Evento;
import com.mycompany.battleship.commons.IServer;
import com.mycompany.blackboard.HandlerChain;
import com.mycompany.blackboard.Controller;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import com.mycompany.blackboard.IHandler;

/**
 * Knowledge Source para manejar el evento de conexión inicial de un usuario.
 * Adaptada al estilo Dominó.
 */
public class ConnectionHandler implements IHandler {

    private final HandlerChain HandlerChain;
    private final IServer server; // Podría necesitar el server para enviar respuestas directas
    private final Controller controller; // Podría necesitar notificar cambios específicos

    public ConnectionHandler(HandlerChain blackboard, IServer server, Controller controller) {
        this.HandlerChain = blackboard;
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
        // Debe retornar true SOLAMENTE para el evento de conexión inicial.
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
    
    // Registrar el socket del cliente en el HandlerChain (acción principal)
    HandlerChain.agregarClienteSocket(cliente);

    // Procesar diferentes tipos de eventos
    switch (evento.getTipo()) {
        case "CREAR_SALA":
            System.out.println("KS: Procesando solicitud de creación de sala.");
            
            String idSala = (String) evento.obtenerDato("idSala");
            String jugador = (String) evento.obtenerDato("jugador");

            if (idSala != null && jugador != null) {
                // Crear objeto de sala
                Map<String, Object> nuevaSala = new HashMap<>();
                nuevaSala.put("id", idSala);
                nuevaSala.put("jugadorCreador", jugador);
                nuevaSala.put("socket", cliente); // Guardamos el socket del creador

                // Guardar en el Blackboard
                HandlerChain.agregarSala(idSala, nuevaSala);

                // Notificar al cliente
                server.enviarMensajeACliente(cliente, "SALA_CREADAConecctionKS;id=" + idSala);
            } else {
                server.enviarMensajeACliente(cliente, "ERROR;detalle=Faltan datos para crear la sala");
            }
            break;
            
        case "CONECTAR_USUARIO_SERVER":  // Manejo de conexión básica (tu lógica original)
            // Acción secundaria: Notificar al HandlerChain
            Evento eventoRespuesta = new Evento("CONEXION_PROCESADA");
            eventoRespuesta.agregarDato("clienteHost", cliente.getInetAddress().getHostAddress());
            HandlerChain.respuestaFuenteC(cliente, eventoRespuesta);
            break;
            
        default:
            System.err.println("ConnectionKS ERROR: Tipo de evento no reconocido: " + evento.getTipo());
            break;
    }
}
}