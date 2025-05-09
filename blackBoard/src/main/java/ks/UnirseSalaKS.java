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
    @SuppressWarnings("unchecked") // Si necesitas castear alguna lista genérica que venga del blackboard antiguo
    public void procesarEvento(Socket clienteRetador, Evento evento) {
        if (clienteRetador == null || evento == null) { /* ... error ... */ return; }

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
        System.out.println("UNIRSE_SALA_KS: Partida '" + idSala + "' encontrada. Estado actual: " + partida.getEstado());

        // 3. Validaciones de la partida
        if (partida.getJugador2() != null) {
            // La sala ya tiene dos jugadores.
            // ¿Es el mismo jugador intentando unirse de nuevo?
            if (partida.getJugador1() != null && partida.getJugador1().getNombre().equals(retadorDTO.getNombre())) {
                 enviarRespuesta(clienteRetador, "UNIDO_OK", Map.of("mensaje", "Ya eres el anfitrión de esta sala.", "idSala", idSala, "rol", "ANFITRION"));
                 return;
            } else if (partida.getJugador2().getNombre().equals(retadorDTO.getNombre())) {
                enviarRespuesta(clienteRetador, "UNIDO_OK", Map.of("mensaje", "Ya te has unido a esta sala como retador.", "idSala", idSala, "rol", "RETADOR"));
                return;
            } else {
                enviarRespuestaError(clienteRetador, "La sala '" + idSala + "' está llena.");
                return;
            }
        }

        // Verificar que el retador no sea el mismo que el anfitrión
        if (partida.getJugador1() != null && partida.getJugador1().getNombre().equals(retadorDTO.getNombre())) {
            enviarRespuestaError(clienteRetador, "No puedes unirte a tu propia sala como oponente.");
            // Podrías enviar un UNIDO_OK si quieres que el anfitrión pueda "re-entrar" a su lobby.
            // Pero la lógica actual asume que unirse es para un segundo jugador distinto.
            return;
        }

        // 4. El retador puede unirse. Configurar el JugadorDTO del retador.
        int dimensionTablero = 10; // O obtener de la partida.getJugador1().getTableroFlota().getDimension()
        if (retadorDTO.getTableroFlota() == null) {
            TableroFlotaDTO tfdto = new TableroFlotaDTO();
            tfdto.setDimension(dimensionTablero);
            retadorDTO.setTableroFlota(tfdto);
        }
        if (retadorDTO.getTableroSeguimiento() == null) {
            TableroSeguimientoDTO tsdto = new TableroSeguimientoDTO();
            tsdto.setDimension(dimensionTablero);
            retadorDTO.setTableroSeguimiento(tsdto);
        }
        retadorDTO.setHaConfirmadoTablero(false); // Aún no ha colocado barcos

        // 5. Añadir al retador a la PartidaDTO y actualizar estado
        partida.setJugador2(retadorDTO);
        partida.setEstado(EstadoPartida.CONFIGURACION); // Ambos jugadores están, listos para configurar barcos
        System.out.println("UNIRSE_SALA_KS: Jugador '" + retadorDTO.getNombre() + "' asignado como jugador2 en PartidaDTO.");
        System.out.println("UNIRSE_SALA_KS: Estado de PartidaDTO actualizado a: " + partida.getEstado());

        // 6. Actualizar la PartidaDTO en el Blackboard
        if (blackboard.actualizarPartida(partida)) {
            System.out.println("UNIRSE_SALA_KS: PartidaDTO '" + idSala + "' actualizada en Blackboard con el nuevo jugador.");

            // 7. Notificar al retador que se unió exitosamente
            enviarRespuesta(clienteRetador, "UNIDO_OK", Map.of(
                "mensaje", "Te has unido a la sala '" + idSala + "'. Esperando para colocar barcos.",
                "idSala", idSala,
                "rol", "RETADOR",
                // Opcional: Enviar el PartidaDTO completo para que el cliente actualice su estado.
                // "partidaDTOJson": new Gson().toJson(partida) // (Necesitarías Gson y Base64 si lo envías así)
                // Por ahora, el cliente esperará un evento de ACTUALIZACION_PARTIDA o INICIAR_COLOCACION
                "nombreOponente", partida.getJugador1().getNombre()
            ));

            // 8. Notificar al anfitrión (jugador1) que un oponente se ha unido
            Socket socketAnfitrion = blackboard.getSocketDeUsuario(partida.getJugador1().getNombre());
            if (socketAnfitrion != null) {
                Evento notificacionAnfitrion = new Evento("NUEVO_JUGADOR_EN_SALA");
                notificacionAnfitrion.agregarDato("idSala", idSala);
                notificacionAnfitrion.agregarDato("nombreOponente", retadorDTO.getNombre());
                // Opcional: Enviar el PartidaDTO completo también al anfitrión.
                // notificacionAnfitrion.agregarDato("partidaDTOJson", new Gson().toJson(partida));
                server.enviarEventoACliente(socketAnfitrion, notificacionAnfitrion);
                System.out.println("UNIRSE_SALA_KS: Notificación NUEVO_JUGADOR_EN_SALA enviada al anfitrión: " + partida.getJugador1().getNombre());
            }

            // 9. (Importante) Ahora que ambos jugadores están, generar evento para iniciar la fase de colocación
            System.out.println("UNIRSE_SALA_KS: Ambos jugadores presentes en sala '" + idSala + "'. Disparando evento INICIAR_FASE_COLOCACION.");
            Evento eventoInicioColocacion = new Evento("INICIAR_FASE_COLOCACION");
            eventoInicioColocacion.agregarDato("idSala", idSala);
            // Este evento es interno del servidor, el cliente origen podría ser null o el retador.
            // Lo procesará otra KS (IniciarColocacionKS).
            blackboard.enviarEventoBlackBoard(null, eventoInicioColocacion); // Cliente origen null para eventos sistémicos

            if (controller != null) {
                controller.notificarCambio("SALA_LLENA;" + idSala + ";jugador1=" + partida.getJugador1().getNombre() + ";jugador2=" + partida.getJugador2().getNombre());
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