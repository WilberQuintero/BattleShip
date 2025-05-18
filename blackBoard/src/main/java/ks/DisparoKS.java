/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author caarl
 */
package ks;

import com.mycompany.battleship.commons.Evento;
import com.mycompany.battleship.commons.IBlackboard;
import com.mycompany.battleship.commons.IServer;
import com.mycompany.blackboard.Controller; // Si necesitas notificar al controller del backend
import com.mycompany.blackboard.IKnowledgeSource;

import dto.*; // Tus DTOs: PartidaDTO, JugadorDTO, BarcoDTO, PosicionDTO, TableroFlotaDTO, TableroSeguimientoDTO
import enums.*; // Tus Enums: ResultadoDisparo, EstadoPartida, EstadoNave, TipoNave

import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects; // Para Objects.equals

public class DisparoKS implements IKnowledgeSource {

    private final IBlackboard blackboard;
    private final IServer server;
    // private final Controller controller; // Opcional, si la KS necesita interactuar con él

    public DisparoKS(IBlackboard blackboard, IServer server, Controller controller) {
        this.blackboard = blackboard;
        this.server = server;
        // this.controller = controller;
    }

    @Override
    public boolean puedeProcesar(Evento evento) {
        return evento != null && "REALIZAR_DISPARO".equalsIgnoreCase(evento.getTipo());
    }

    @Override
    public void procesarEvento(Socket clienteQueDispara, Evento evento) {
        String idSala = (String) evento.obtenerDato("idSala");
        String nombreJugadorAtacante = (String) evento.obtenerDato("nombreJugador");
        // Las coordenadas vienen como String del evento parseado, convertir a int
        int fila = -1, columna = -1;
        try {
            fila = Integer.parseInt((String) evento.obtenerDato("fila"));
            columna = Integer.parseInt((String) evento.obtenerDato("columna"));
        } catch (NumberFormatException e) {
            System.err.println("DISPARO_KS: Error al parsear fila/columna. Datos: " + evento.getDatos());
            enviarErrorAlCliente(clienteQueDispara, idSala, "Coordenadas de disparo inválidas.");
            return;
        }

        System.out.println("DISPARO_KS: Procesando disparo de '" + nombreJugadorAtacante + "' en sala '" + idSala + "' a (" + fila + "," + columna + ")");

        PartidaDTO partida = blackboard.getPartidaDTO(idSala);

        // --- 1. Validaciones ---
        if (!validacionesPrevias(clienteQueDispara, idSala, nombreJugadorAtacante, partida, fila, columna)) {
            return; // Las validaciones envían el error si es necesario
        }

        JugadorDTO atacanteDTO = partida.getJugador1().getNombre().equals(nombreJugadorAtacante) ? partida.getJugador1() : partida.getJugador2();
        JugadorDTO defensorDTO = partida.getJugador1().getNombre().equals(nombreJugadorAtacante) ? partida.getJugador2() : partida.getJugador1();

        PosicionDTO posDisparoDTO = new PosicionDTO(columna, fila); // (X, Y)

        // (Opcional) Validar si ya disparó ahí en su TableroSeguimientoDTO
        // if (atacanteDTO.getTableroSeguimiento().getRegistrosDisparos().containsKey(posDisparoDTO)) {
        //     enviarErrorAlCliente(clienteQueDispara, idSala, "Ya has disparado en esa casilla.");
        //     return;
        // }

        // --- 2. Procesar el Disparo en el TableroFlotaDTO del Defensor ---
        ResultadoDisparo resultadoDisparo = ResultadoDisparo.AGUA; // Por defecto
        BarcoDTO barcoImpactado = null;
        boolean barcoHundidoEsteTurno = false;
        TipoNave tipoNaveHundida = null;

        TableroFlotaDTO tableroFlotaDefensor = defensorDTO.getTableroFlota();
        if (tableroFlotaDefensor != null && tableroFlotaDefensor.getBarcos() != null) {
            for (BarcoDTO barco : tableroFlotaDefensor.getBarcos()) {
                if (barco.getEstado() == EstadoNave.HUNDIDA) continue; // No se puede impactar un barco ya hundido

                for (PosicionDTO posBarco : barco.getPosicionesOcupadas()) {
                    if (posBarco.equals(posDisparoDTO)) { // Necesitas un equals() correcto en PosicionDTO
                        // ¡Impacto!
                        if (!barco.getPosicionesImpactadas().contains(posDisparoDTO)) {
                            barco.getPosicionesImpactadas().add(posDisparoDTO); // Marcar impacto en el DTO
                            barcoImpactado = barco;
                            
                            if (barco.getPosicionesImpactadas().size() >= barco.getLongitud()) {
                                barco.setEstado(EstadoNave.HUNDIDA);
                                resultadoDisparo = ResultadoDisparo.HUNDIDO;
                                barcoHundidoEsteTurno = true;
                                tipoNaveHundida = barco.getTipo();
                            } else {
                                barco.setEstado(EstadoNave.AVERIADA);
                                resultadoDisparo = ResultadoDisparo.IMPACTO;
                            }
                        } else {
                            // Ya había sido impactada esta parte del barco.
                            // Mantener el estado actual del barco (podría ya estar AVERIADA o HUNDIDA).
                            // El resultado del disparo sigue siendo el estado actual de la casilla/barco.
                            resultadoDisparo = (barco.getEstado() == EstadoNave.HUNDIDA) ? ResultadoDisparo.HUNDIDO : ResultadoDisparo.IMPACTO;
                             if (barco.getEstado() == EstadoNave.HUNDIDA) tipoNaveHundida = barco.getTipo(); // Si ya estaba hundido y le vuelven a dar
                            System.out.println("DISPARO_KS: Disparo a casilla ya impactada de un barco " + barco.getEstado());
                        }
                        break; // Salir del bucle de posiciones del barco
                    }
                }
                if (barcoImpactado != null) break; // Salir del bucle de barcos
            }
        }
        System.out.println("DISPARO_KS: Resultado del disparo: " + resultadoDisparo);

        // --- 3. Actualizar TableroSeguimientoDTO del Atacante ---
        if (atacanteDTO.getTableroSeguimiento() != null) {
            // Si tu TableroSeguimientoDTO usa un Map<PosicionDTO, ResultadoDisparo> para registrosDisparos:
            Map<PosicionDTO, ResultadoDisparo> registros = atacanteDTO.getTableroSeguimiento().getRegistrosDisparos();
            if (registros == null) registros = new HashMap<>(); // Asegurar que no sea null
            registros.put(posDisparoDTO, resultadoDisparo);
            atacanteDTO.getTableroSeguimiento().setRegistrosDisparos(registros);
        }

        // --- 4. Determinar Siguiente Turno y Verificar Fin de Partida ---
        boolean partidaTerminada = false;
        String nombreGanador = null;
        String mensajeFin = "";

        if (barcoHundidoEsteTurno) {
            partidaTerminada = verificarSiTodosHundidos(tableroFlotaDefensor);
            if (partidaTerminada) {
                nombreGanador = atacanteDTO.getNombre();
                partida.setEstado(EstadoPartida.FINALIZADA); // O un estado específico para ganador
                partida.setNombreJugadorEnTurno(null); // Nadie más juega
                mensajeFin = "¡" + nombreGanador + " ha ganado la partida! Todos los barcos de " + defensorDTO.getNombre() + " hundidos.";
                System.out.println("DISPARO_KS: Partida finalizada. Ganador: " + nombreGanador);
            }
        }

        if (!partidaTerminada) {
            if (resultadoDisparo == ResultadoDisparo.AGUA) {
                partida.setNombreJugadorEnTurno(defensorDTO.getNombre()); // Cambia el turno
            } else {
                // Si fue IMPACTO o HUNDIDO (y la partida no terminó), el atacante tiene otro turno.
                partida.setNombreJugadorEnTurno(atacanteDTO.getNombre());
            }
        }
        System.out.println("DISPARO_KS: Siguiente turno para: " + partida.getNombreJugadorEnTurno());
        
        // --- 5. Actualizar PartidaDTO en Blackboard ---
        blackboard.actualizarPartida(partida);

        // --- 6. Enviar Respuesta a los Clientes ---
        Evento respuestaEvento = new Evento("RESULTADO_DISPARO");
        respuestaEvento.agregarDato("idSala", idSala);
        respuestaEvento.agregarDato("fila", String.valueOf(fila));
        respuestaEvento.agregarDato("columna", String.valueOf(columna));
        respuestaEvento.agregarDato("resultado", resultadoDisparo.name());
        respuestaEvento.agregarDato("nombreJugadorQueDisparo", atacanteDTO.getNombre());
        respuestaEvento.agregarDato("nombreJugadorImpactado", defensorDTO.getNombre());
        respuestaEvento.agregarDato("turnoActualizado", partida.getNombreJugadorEnTurno()); // Quién tiene el siguiente turno

        if (barcoImpactado != null) {
            respuestaEvento.agregarDato("estadoNaveImpactada", barcoImpactado.getEstado().name());
            if (resultadoDisparo == ResultadoDisparo.HUNDIDO && tipoNaveHundida != null) {
                respuestaEvento.agregarDato("tipoBarcoHundido", tipoNaveHundida.name());
            }
        }
        respuestaEvento.agregarDato("partidaTerminada", String.valueOf(partidaTerminada));
        if (partidaTerminada) {
            respuestaEvento.agregarDato("ganador", nombreGanador);
            respuestaEvento.agregarDato("mensajeFin", mensajeFin);
        }

        // Enviar a ambos jugadores
        Socket socketAtacante = clienteQueDispara; // Ya lo tenemos
        Socket socketDefensor = blackboard.getSocketDeUsuario(defensorDTO.getNombre());

        if (socketAtacante != null) {
            server.enviarEventoACliente(socketAtacante, respuestaEvento);
        }
        if (socketDefensor != null && !socketDefensor.equals(socketAtacante)) { // No enviar dos veces si es el mismo por alguna razón
            server.enviarEventoACliente(socketDefensor, respuestaEvento);
        }
        System.out.println("DISPARO_KS: Evento RESULTADO_DISPARO enviado a los clientes.");
        
        blackboard.respuestaFuenteC(clienteQueDispara, evento); // Notificar al blackboard que la KS terminó
    }

    private boolean validacionesPrevias(Socket clienteQueDispara, String idSala, String nombreJugadorAtacante, PartidaDTO partida, int fila, int col) {
        if (partida == null) {
            enviarErrorAlCliente(clienteQueDispara, idSala, "Error: La sala/partida no existe.");
            return false;
        }
        if (partida.getEstado() != EstadoPartida.EN_CURSO) {
            enviarErrorAlCliente(clienteQueDispara, idSala, "Error: La partida no está en curso.");
            return false;
        }
        if (partida.getNombreJugadorEnTurno() == null || !partida.getNombreJugadorEnTurno().equals(nombreJugadorAtacante)) {
            enviarErrorAlCliente(clienteQueDispara, idSala, "Error: No es tu turno.");
            return false;
        }
        // Validar coordenadas (asumiendo que TableroFlotaDTO tiene la dimensión)
        int dimension = (partida.getJugador1() != null && partida.getJugador1().getTableroFlota() != null) ?
                         partida.getJugador1().getTableroFlota().getDimension() : 10; // O tomar de PartidaDTO si la tiene
        if (fila < 0 || fila >= dimension || col < 0 || col >= dimension) {
             enviarErrorAlCliente(clienteQueDispara, idSala, "Error: Coordenadas fuera del tablero.");
            return false;
        }
        return true;
    }

    private boolean verificarSiTodosHundidos(TableroFlotaDTO tableroFlota) {
        if (tableroFlota == null || tableroFlota.getBarcos() == null || tableroFlota.getBarcos().isEmpty()) {
            return true; // No hay barcos, o ya no quedan barcos para hundir (partida ya ganada)
        }
        for (BarcoDTO barco : tableroFlota.getBarcos()) {
            if (barco.getEstado() != EstadoNave.HUNDIDA) {
                return false; // Al menos un barco no está hundido
            }
        }
        return true; // Todos los barcos están hundidos
    }

    private void enviarErrorAlCliente(Socket cliente, String idSala, String mensajeError) {
        Evento errorEvento = new Evento("ERROR_DISPARO"); // O un tipo de error más genérico
        errorEvento.agregarDato("idSala", idSala);
        errorEvento.agregarDato("error", mensajeError);
        server.enviarEventoACliente(cliente, errorEvento);
        System.err.println("DISPARO_KS: Error enviado a cliente: " + mensajeError);
    }
}