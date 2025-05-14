/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ks; // O tu paquete de knowledge sources

import com.mycompany.battleship.commons.Evento;
import com.mycompany.battleship.commons.IBlackboard;
import com.mycompany.battleship.commons.IServer;
import com.mycompany.blackboard.Controller;
import com.mycompany.blackboard.IKnowledgeSource;
import dto.*;
import enums.*;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Knowledge Source para manejar el evento de un jugador uniéndose a una sala existente.
 */
public class UnirseSalaKS implements IKnowledgeSource { // Asegúrate que IKnowledgeSource esté definida como en tu proyecto

    private final IBlackboard blackboard;
    private final IServer server;
    private final Controller controller; // Podríamos necesitar notificar al controller

    // Constante para la capacidad máxima de la sala (Battleship = 2 jugadores)
    private static final int MAX_JUGADORES_SALA = 2;

    public UnirseSalaKS(IBlackboard blackboard, IServer server, Controller controller) {
        this.blackboard = blackboard;
        this.server = server;
        this.controller = controller;
    }

    @Override
    public boolean puedeProcesar(Evento evento) {
        return evento != null && "UNIRSE_SALA".equalsIgnoreCase(evento.getTipo());
    }


  @Override
    public void procesarEvento(Socket clienteRetador, Evento evento) {
        if (clienteRetador == null || evento == null) {
            System.err.println("UNIRSE_SALA_KS: Cliente o Evento nulo.");
            return;
        }

        String idSala = (String) evento.obtenerDato("idSala");
        if (idSala == null || idSala.isBlank()) {
            enviarRespuestaError(clienteRetador, "ID de sala no válido.");
            return;
        }
        idSala = idSala.trim();

        System.out.println("UNIRSE_SALA_KS: Cliente " + clienteRetador.getInetAddress().getHostAddress() +
                           " intentando unirse a sala '" + idSala + "'");

        // 1. Obtener el JugadorDTO del retador (ya debe estar registrado)
        JugadorDTO retadorDTO = blackboard.getJugadorDTO(clienteRetador);
        if (retadorDTO == null) {
            enviarRespuestaError(clienteRetador, "Error: No estás registrado. Regístrate primero.");
            return;
        }
        System.out.println("UNIRSE_SALA_KS: Retador es '" + retadorDTO.getNombre() + "'.");

        // 2. Obtener la PartidaDTO del Blackboard
        PartidaDTO partida = blackboard.getPartidaDTO(idSala);
        if (partida == null) {
            enviarRespuestaError(clienteRetador, "La sala '" + idSala + "' no existe.");
            return;
        }
        System.out.println("UNIRSE_SALA_KS: Partida '" + idSala + "' encontrada. Anfitrión: " +
                           (partida.getJugador1() != null ? partida.getJugador1().getNombre() : "N/A") +
                           ", Jugador2: " + (partida.getJugador2() != null ? partida.getJugador2().getNombre() : "N/A") +
                           ", Estado actual: " + partida.getEstado());

        // 3. Validaciones de la partida y del jugador que se une

        // Verificar si el retador ya es el anfitrión (jugador1)
        if (partida.getJugador1() != null && partida.getJugador1().getNombre().equals(retadorDTO.getNombre())) {
            System.out.println("UNIRSE_SALA_KS: Cliente " + retadorDTO.getNombre() + " ya es el anfitrión de esta sala.");
            enviarRespuesta(clienteRetador, "UNIDO_OK", Map.of(
                "mensaje", "Ya eres el anfitrión de esta sala.",
                "idSala", idSala,
                "rol", "ANFITRION"
            ));
            return;
        }

        // Verificar si el retador ya es el jugador2
        if (partida.getJugador2() != null && partida.getJugador2().getNombre().equals(retadorDTO.getNombre())) {
            System.out.println("UNIRSE_SALA_KS: Cliente " + retadorDTO.getNombre() + " ya se unió como retador a esta sala.");
            enviarRespuesta(clienteRetador, "UNIDO_OK", Map.of(
                "mensaje", "Ya te has unido a esta sala como retador.",
                "idSala", idSala,
                "rol", "RETADOR"
            ));
            return;
        }

        // Verificar si la sala está llena (es decir, si jugador2 ya está ocupado por OTRO jugador)
        if (partida.getJugador2() != null) {
            // Como ya validamos que el retador no es jugador2, si jugador2 no es null, la sala está llena.
            System.out.println("UNIRSE_SALA_KS: Sala '" + idSala + "' está llena. Jugador2 actual: " + partida.getJugador2().getNombre());
            enviarRespuestaError(clienteRetador, "La sala '" + idSala + "' está llena.");
            return;
        }

        // --- Si llegamos aquí, el jugador puede unirse como jugador2 ---

        // 4. Configurar el JugadorDTO del retador con sus tableros vacíos
        int dimensionTablero = 10; // Podría tomarse del partida.getJugador1().getTableroFlota().getDimension() si j1 existe
        if (partida.getJugador1() != null && partida.getJugador1().getTableroFlota() != null) {
            dimensionTablero = partida.getJugador1().getTableroFlota().getDimension();
        }

        if (retadorDTO.getTableroFlota() == null) {
            TableroFlotaDTO tfdto = new TableroFlotaDTO();
            tfdto.setDimension(dimensionTablero);
            retadorDTO.setTableroFlota(tfdto);
        } else { // Asegurar que la dimensión sea consistente
            retadorDTO.getTableroFlota().setDimension(dimensionTablero);
        }

        if (retadorDTO.getTableroSeguimiento() == null) {
            TableroSeguimientoDTO tsdto = new TableroSeguimientoDTO();
            tsdto.setDimension(dimensionTablero);
            retadorDTO.setTableroSeguimiento(tsdto);
        } else {
            retadorDTO.getTableroSeguimiento().setDimension(dimensionTablero);
        }
        retadorDTO.setHaConfirmadoTablero(false);

        // 5. Añadir al retador a la PartidaDTO y actualizar estado
        partida.setJugador2(retadorDTO);
        // El estado cambia a CONFIGURACION, indicando que ambos jugadores están y pueden proceder a colocar barcos.
        partida.setEstado(EstadoPartida.CONFIGURACION);
        System.out.println("UNIRSE_SALA_KS: Jugador '" + retadorDTO.getNombre() + "' asignado como jugador2 en PartidaDTO para sala '" + idSala + "'.");
        System.out.println("UNIRSE_SALA_KS: Estado de PartidaDTO actualizado a: " + partida.getEstado());

        // 6. Actualizar la PartidaDTO en el Blackboard
        if (blackboard.actualizarPartida(partida)) {
            System.out.println("UNIRSE_SALA_KS: PartidaDTO '" + idSala + "' actualizada en Blackboard.");

            // 7. Notificar al retador que se unió exitosamente
            enviarRespuesta(clienteRetador, "UNIDO_OK", Map.of(
                "mensaje", "Te has unido a la sala '" + idSala + "'. Prepárate para colocar barcos.",
                "idSala", idSala,
                "rol", "RETADOR",
                "nombreOponente", (partida.getJugador1() != null ? partida.getJugador1().getNombre() : "N/A")
            ));
            Socket socketAnfitrion = blackboard.getSocketDeUsuario(partida.getJugador1().getNombre());
           
            // 8. Notificar al anfitrión (jugador1) que un oponente se ha unido
            if (partida.getJugador1() != null) {
               
                if (socketAnfitrion != null) {
                    Evento notificacionAnfitrion = new Evento("NUEVO_JUGADOR_EN_SALA"); // Mantenemos el nombre del evento
                    notificacionAnfitrion.agregarDato("idSala", idSala);
                    // En tu código original era "jugadorInfo", "Jugador " + ip. Mantendremos nombre.
                    notificacionAnfitrion.agregarDato("jugadorInfo", retadorDTO.getNombre()); // Nombre del jugador que se unió
                    server.enviarEventoACliente(socketAnfitrion, notificacionAnfitrion);
                    System.out.println("UNIRSE_SALA_KS: Notificación NUEVO_JUGADOR_EN_SALA enviada al anfitrión: " + partida.getJugador1().getNombre());
                } else {
                     System.err.println("UNIRSE_SALA_KS: No se encontró socket para el anfitrión '" + partida.getJugador1().getNombre() + "' para notificar.");
                }
            }
                

            // 9. Lógica de "Sala Llena" - Disparar evento para iniciar colocación
            // (Esta parte ya estaba en tu código original y es correcta)
            // Ahora que hay dos jugadores (jugador1 y jugador2 no son null)
            System.out.println("UNIRSE_SALA_KS: ¡Sala '" + idSala + "' llena con 2 jugadores! Disparando evento INICIAR_FASE_COLOCACION.");

             Evento eventoInicioColocacion = new Evento("INICIAR_FASE_COLOCACION");
            eventoInicioColocacion.agregarDato("idSala", idSala);
            blackboard.enviarEventoBlackBoard(null, eventoInicioColocacion); // Evento sistémico, clienteOrigen null
            
            
            
            // Notificar al backend controller (si aún es parte de tu flujo)
            if (controller != null) {
                controller.notificarCambio("SALA_LLENA;" + idSala + ";jugador1=" + partida.getJugador1().getNombre() + ";jugador2=" + partida.getJugador2().getNombre());
            }
            
            Evento iniciarPartida = new Evento("INICIAR_PARTIDA_SALA");
            iniciarPartida.agregarDato("idSala", idSala);
            if (socketAnfitrion != null) { // Solo si el anfitrión necesita un evento especial
                 blackboard.enviarEventoBlackBoard(socketAnfitrion, iniciarPartida);
            }
            
       

        } else {
            System.err.println("UNIRSE_SALA_KS: Falló la actualización de PartidaDTO en Blackboard para sala '" + idSala + "'.");
            enviarRespuestaError(clienteRetador, "Error interno al unirse a la sala.");
        }

        blackboard.respuestaFuenteC(clienteRetador, evento);
    }
  

    private void enviarRespuestaError(Socket cliente, String mensajeError) {
        Evento respuesta = new Evento("ERROR_UNIRSE_SALA");
        respuesta.agregarDato("error", mensajeError);
        server.enviarEventoACliente(cliente, respuesta);
    }

    private void enviarRespuesta(Socket cliente, String tipoRespuesta, Map<String, Object> datos) {
        Evento respuesta = new Evento(tipoRespuesta);
        if (datos != null) {
            datos.forEach(respuesta::agregarDato);
        }
        server.enviarEventoACliente(cliente, respuesta);
    }
}