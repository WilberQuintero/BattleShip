/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.blackboard;
import com.mycompany.battleship.commons.Evento;
import com.mycompany.battleship.commons.IBlackboard;
import com.mycompany.battleship.commons.IServer;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author Hector
 */
public class CrearSalaKS  implements IKnowledgeSource{
 
    private final IServer server;
    private final IBlackboard blackboard;
    

    public CrearSalaKS(IServer server, IBlackboard blackboard) {
        this.server = server;
        this.blackboard = blackboard;
      
    }

    @Override
    public boolean puedeProcesar(Evento evento) {
        return "CREAR_SALA".equalsIgnoreCase(evento.getTipo());
    }

    @Override
    public void procesarEvento(Socket cliente, Evento evento) {
       if (evento == null || cliente == null) {
            System.err.println("CREAR_SALA_KS: Evento o cliente nulo.");
            return;
        }

        String idSala = (String) evento.getDatos().get("sala");

        if (idSala == null || idSala.isBlank()) {
            System.err.println("CREAR_SALA_KS: ID de sala inválido.");
            return;
        }

        if (!blackboard.existeSala(idSala)) {
            Map<String, Object> datosSala = new HashMap<>();
            datosSala.put("host", cliente);
            datosSala.put("jugadores", new ArrayList<Socket>()); // Lista vacía por ahora
            blackboard.agregarSala(idSala, datosSala);

            System.out.println("CREAR_SALA_KS: Sala '" + idSala + "' creada por " + cliente.getInetAddress().getHostAddress());
           
        } else {
            System.out.println("CREAR_SALA_KS: Sala '" + idSala + "' ya existe. No se creó.");
            // Opcional: enviar respuesta de error al cliente
        }
    }
    }


