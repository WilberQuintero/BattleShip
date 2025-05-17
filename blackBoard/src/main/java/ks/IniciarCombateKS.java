// En ks.IniciarCombateKS.java (Servidor)
package ks;

import com.mycompany.battleship.commons.Evento;
import com.mycompany.battleship.commons.IBlackboard;
import com.mycompany.battleship.commons.IServer;
import com.mycompany.blackboard.Controller; // Si lo usas
import com.mycompany.blackboard.IKnowledgeSource;

// Importar DTOs, Enums, Gson, etc.
import dto.BarcoDTO;
import dto.JugadorDTO;
import dto.PartidaDTO;
import dto.TableroFlotaDTO; // Necesario para validar la dimensión y asignar barcos
import enums.EstadoPartida;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken; // Para deserializar List<BarcoDTO>

import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class IniciarCombateKS implements IKnowledgeSource {
    private final IBlackboard blackboard;
    private final IServer server;
    private final Controller controller; // Opcional
    private final Gson gson; // Para deserializar JSON

    public IniciarCombateKS(IBlackboard blackboard, IServer server, Controller controller) {
        this.blackboard = blackboard;
        this.server = server;
        this.controller = controller;
        this.gson = new Gson(); // Crear una instancia de Gson
    }

    @Override
    public boolean puedeProcesar(Evento evento) {
        return evento != null && "JUGADOR_FLOTA_LISTA".equalsIgnoreCase(evento.getTipo());
    }

    @Override
    public void procesarEvento(Socket clienteQueEnvia, Evento evento) {
        if (clienteQueEnvia == null || evento == null) {
            System.err.println("INICIAR_COMBATE_KS: Cliente o Evento nulo.");
            return;
        }

        String idSala = (String) evento.obtenerDato("idSala");
        String nombreJugadorQueEnvia = (String) evento.obtenerDato("nombreJugador");
        String flotaJson = (String) evento.obtenerDato("flotaJson"); // JSON de List<BarcoDTO>

        if (idSala == null || idSala.isBlank() ||
            nombreJugadorQueEnvia == null || nombreJugadorQueEnvia.isBlank() ||
            flotaJson == null || flotaJson.isBlank()) {
            enviarError(clienteQueEnvia, "Datos incompletos para confirmar flota (sala, nombre o flota faltante).", idSala);
            return;
        }
        idSala = idSala.trim();
        nombreJugadorQueEnvia = nombreJugadorQueEnvia.trim();

        System.out.println("INICIAR_COMBATE_KS: Recibida flota de '" + nombreJugadorQueEnvia + "' para sala '" + idSala + "'.");

        // 1. Validar que el jugador que envía es quien dice ser y está asociado a este socket
        JugadorDTO jugadorActualDTO = blackboard.getJugadorDTO(clienteQueEnvia);
        if (jugadorActualDTO == null || !jugadorActualDTO.getNombre().equals(nombreJugadorQueEnvia)) {
            System.err.println("INICIAR_COMBATE_KS: Discrepancia entre socket y nombre de jugador reportado. Socket: " +
                               clienteQueEnvia.getInetAddress().getHostAddress() + ", Nombre en evento: " + nombreJugadorQueEnvia +
                               ", Nombre en BB para socket: " + (jugadorActualDTO != null ? jugadorActualDTO.getNombre() : "N/A"));
            enviarError(clienteQueEnvia, "Error de autenticación al enviar flota.", idSala);
            return;
        }

        // 2. Obtener la PartidaDTO del Blackboard
        PartidaDTO partida = blackboard.getPartidaDTO(idSala);
        if (partida == null) {
            enviarError(clienteQueEnvia, "La sala '" + idSala + "' no existe.", idSala);
            return;
        }

        // 3. Verificar que la partida esté en estado CONFIGURACION (o el estado previo a EN_CURSO)
        if (partida.getEstado() != EstadoPartida.CONFIGURACION && partida.getEstado() != EstadoPartida.ESPERANDO_OPONENTE) {
             // Si ya está EN_CURSO o FINALIZADA, no se debería procesar esto.
             // ESPERANDO_OPONENTE podría ser válido si la lógica permite configurar antes de que el 2do jugador confirme.
             // Para este caso, asumimos que es CONFIGURACION (ambos jugadores unidos, listos para colocar).
            System.err.println("INICIAR_COMBATE_KS: La partida '" + idSala + "' no está en estado de configuración. Estado actual: " + partida.getEstado());
            enviarError(clienteQueEnvia, "La partida no está en fase de configuración de flota.", idSala);
            return;
        }

        // 4. Identificar si el jugador actual es J1 o J2 en la PartidaDTO
        JugadorDTO jugadorQueConfirma = null;
        if (partida.getJugador1() != null && partida.getJugador1().getNombre().equals(nombreJugadorQueEnvia)) {
            jugadorQueConfirma = partida.getJugador1();
        } else if (partida.getJugador2() != null && partida.getJugador2().getNombre().equals(nombreJugadorQueEnvia)) {
            jugadorQueConfirma = partida.getJugador2();
        }

        if (jugadorQueConfirma == null) {
            enviarError(clienteQueEnvia, "No estás registrado en esta partida '" + idSala + "'.", idSala);
            return;
        }

        if (jugadorQueConfirma.isHaConfirmadoTablero()) {
            System.out.println("INICIAR_COMBATE_KS: Jugador '" + nombreJugadorQueEnvia + "' ya había confirmado su tablero.");
            // Podrías reenviar "esperando oponente" o un error si es un envío duplicado no deseado.
            // Por ahora, lo permitimos pero no reprocesaremos si ya está listo para combate.
             if (partida.getEstado() == EstadoPartida.EN_CURSO) {
                 enviarError(clienteQueEnvia, "La partida ya ha comenzado.", idSala);
                 return;
             }
            // Si solo él ha confirmado, enviar mensaje de espera.
            JugadorDTO oponente = (jugadorQueConfirma == partida.getJugador1()) ? partida.getJugador2() : partida.getJugador1();
            if (oponente == null || !oponente.isHaConfirmadoTablero()) {
                 enviarRespuesta(clienteQueEnvia, "ESPERANDO_OPONENTE_FLOTA", Map.of(
                    "mensaje", "Tu flota está confirmada. Esperando al oponente...",
                    "idSala", idSala
                ));
            }
            return; // Ya confirmó, no hacer más aquí.
        }

        // 5. Deserializar y Validar la flotaJson
        List<BarcoDTO> flotaRecibida;
        try {
            Type tipoListaBarcoDTO = new TypeToken<ArrayList<BarcoDTO>>() {}.getType();
            flotaRecibida = gson.fromJson(flotaJson, tipoListaBarcoDTO);
        } catch (Exception e) {
            System.err.println("INICIAR_COMBATE_KS: Error al deserializar flotaJson para '" + nombreJugadorQueEnvia + "': " + e.getMessage());
            enviarError(clienteQueEnvia, "Formato de flota inválido.", idSala);
            return;
        }

        // *** Aquí va la LÓGICA DE VALIDACIÓN DE LA FLOTA ***
        // - ¿Número correcto de barcos? (2 Portaaviones, 2 Cruceros, 4 Submarinos, 3 Barcos Patrulla)
        // - ¿Longitudes correctas para cada tipo? (Ya debería estar en el DTO, pero verificar)
        // - ¿Posiciones dentro del tablero (ej. 0-9 para 10x10)?
        // - ¿Barcos no se superponen?
        // - ¿Barcos no están pegados (si es regla)?
        // Esta validación es crucial y puede ser compleja. Por ahora, asumimos que es válida.
        boolean flotaValida = validarFlota(flotaRecibida, jugadorQueConfirma.getTableroFlota().getDimension());
        if (!flotaValida) {
            System.err.println("INICIAR_COMBATE_KS: Flota recibida de '" + nombreJugadorQueEnvia + "' no es válida.");
            enviarError(clienteQueEnvia, "La configuración de tu flota no es válida. Por favor, revísala.", idSala);
            return;
        }
        System.out.println("INICIAR_COMBATE_KS: Flota de '" + nombreJugadorQueEnvia + "' validada exitosamente.");

        // 6. Asignar la flota al TableroFlotaDTO del jugador y marcar como confirmado
        if (jugadorQueConfirma.getTableroFlota() == null) { // No debería ser null si se inicializó en Crear/UnirseSala
            TableroFlotaDTO tfdto = new TableroFlotaDTO();
            tfdto.setDimension(10); // Asumir o tomar de algún lado
            jugadorQueConfirma.setTableroFlota(tfdto);
        }
        jugadorQueConfirma.getTableroFlota().setBarcos(flotaRecibida);
        jugadorQueConfirma.setHaConfirmadoTablero(true);
        System.out.println("INICIAR_COMBATE_KS: Flota asignada y jugador '" + nombreJugadorQueEnvia + "' marcado como confirmado.");

        // 7. Actualizar la PartidaDTO en el Blackboard
        blackboard.actualizarPartida(partida); // Guardar el estado del jugador que confirmó

        // 8. Verificar si ambos jugadores están listos
        JugadorDTO jugador1 = partida.getJugador1();
        JugadorDTO jugador2 = partida.getJugador2();

        if (jugador1 != null && jugador1.isHaConfirmadoTablero() &&
            jugador2 != null && jugador2.isHaConfirmadoTablero()) {
            
            System.out.println("INICIAR_COMBATE_KS: ¡Ambos jugadores listos en sala '" + idSala + "'! Iniciando combate.");

            // AMBOS LISTOS: Iniciar el combate
            partida.setEstado(EstadoPartida.EN_CURSO);

            // Asignar turno inicial aleatoriamente
            Random random = new Random();
            JugadorDTO jugadorConTurno = random.nextBoolean() ? jugador1 : jugador2;
            partida.setNombreJugadorEnTurno(jugadorConTurno.getNombre());
            System.out.println("INICIAR_COMBATE_KS: Turno inicial para: " + jugadorConTurno.getNombre());

            // Actualizar la partida final en el Blackboard
            blackboard.actualizarPartida(partida);

            // Preparar evento INICIAR_COMBATE para ambos clientes
            Evento eventoIniciarCombate = new Evento("INICIAR_COMBATE");
            eventoIniciarCombate.agregarDato("idSala", partida.getIdPartida());
            eventoIniciarCombate.agregarDato("mensaje", "¡El combate ha comenzado! Turno de " + jugadorConTurno.getNombre());

            // Serializar el PartidaDTO completo (podría necesitar sanitización por jugador)
            // Por ahora, enviamos el mismo PartidaDTO a ambos.
            String partidaCompletaJson = gson.toJson(partida);
            String partidaCompletaJsonBase64 = Base64.getEncoder().encodeToString(partidaCompletaJson.getBytes(StandardCharsets.UTF_8));
            eventoIniciarCombate.agregarDato("partidaJsonBase64", partidaCompletaJsonBase64);

            // Enviar a ambos jugadores
            Socket socketJ1 = blackboard.getSocketDeUsuario(jugador1.getNombre());
            Socket socketJ2 = blackboard.getSocketDeUsuario(jugador2.getNombre());

            if (socketJ1 != null) {
                server.enviarEventoACliente(socketJ1, eventoIniciarCombate);
                System.out.println("INICIAR_COMBATE_KS: Enviado INICIAR_COMBATE a J1: " + jugador1.getNombre());
            }
            if (socketJ2 != null) {
                server.enviarEventoACliente(socketJ2, eventoIniciarCombate);
                System.out.println("INICIAR_COMBATE_KS: Enviado INICIAR_COMBATE a J2: " + jugador2.getNombre());
            }
            
            if (controller != null) { // Notificar al controller del backend
                 controller.notificarCambio("PARTIDA_INICIADA;" + idSala + ";turno=" + jugadorConTurno.getNombre());
            }

        } else {
            // Uno de los jugadores aún no está listo
            System.out.println("INICIAR_COMBATE_KS: Jugador '" + nombreJugadorQueEnvia + "' listo. Esperando al oponente en sala '" + idSala + "'.");
            enviarRespuesta(clienteQueEnvia, "ESPERANDO_OPONENTE_FLOTA", Map.of(
                "mensaje", "Tu flota está confirmada. Esperando configuración del oponente...",
                "idSala", idSala
            ));
            
            // Opcional: Notificar al otro jugador que su oponente ya está listo (si está conectado)
            JugadorDTO oponente = (jugadorQueConfirma == jugador1) ? jugador2 : jugador1;
            if (oponente != null && !oponente.isHaConfirmadoTablero()) { // y el oponente no ha confirmado aún
                Socket socketOponente = blackboard.getSocketDeUsuario(oponente.getNombre());
                if (socketOponente != null) {
                    Evento notificacionOponente = new Evento("OPONENTE_CONFIRMO_FLOTA");
                    notificacionOponente.agregarDato("idSala", idSala);
                    notificacionOponente.agregarDato("mensaje", "Tu oponente (" + jugadorQueConfirma.getNombre() + ") ha configurado su flota.");
                    server.enviarEventoACliente(socketOponente, notificacionOponente);
                }
            }
        }

        blackboard.respuestaFuenteC(clienteQueEnvia, evento);
    }

    /**
     * TODO: Implementar lógica de validación de flota completa.
     * - Número correcto de barcos de cada tipo.
     * - Longitudes correctas.
     * - Posiciones dentro del tablero.
     * - Sin superposición.
     * - Sin estar pegados (si es regla).
     * @param flota La lista de BarcoDTO a validar.
     * @param dimension La dimensión del tablero.
     * @return true si la flota es válida, false en caso contrario.
     */
    private boolean validarFlota(List<BarcoDTO> flota, int dimension) {
        if (flota == null) return false;
        // Implementa aquí tus reglas de validación detalladas.
        // Esto es solo un placeholder.
        // Ejemplo: Verificar cantidad total de barcos (2+2+4+3 = 11 barcos)
        if (flota.size() != 11) { // Ajusta este número a tu total de barcos
            System.err.println("VALIDAR_FLOTA: Número incorrecto de barcos. Esperado: 11, Recibido: " + flota.size());
            return false;
        }
        // Aquí irían muchas más validaciones...
        System.out.println("VALIDAR_FLOTA: Validación básica pasada (cantidad de barcos). Implementar validación completa.");
        return true;
    }

    private void enviarError(Socket cliente, String mensajeError, String idSala) {
        Evento respuesta = new Evento("ERROR_CONFIRMAR_FLOTA"); // Nuevo tipo de error
        respuesta.agregarDato("error", mensajeError);
        if (idSala != null) respuesta.agregarDato("idSala", idSala);
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