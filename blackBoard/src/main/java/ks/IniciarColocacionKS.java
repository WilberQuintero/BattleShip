/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ks;

/**
 *
 * @author caarl
 */
import com.mycompany.battleship.commons.Evento;
import com.mycompany.battleship.commons.IBlackboard;
import com.mycompany.battleship.commons.IServer;
import com.mycompany.blackboard.Controller; // Controller del backend
import com.mycompany.blackboard.IKnowledgeSource;

import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.ArrayList; // Para crear la lista de flota


import dto.PartidaDTO;
import dto.JugadorDTO;
import enums.EstadoPartida; // Asegúrate que la ruta a tu enum sea correcta
import enums.TipoNave;  

/**
 * Knowledge Source que se activa cuando una sala está llena
 * para iniciar la fase de colocación de barcos por parte de los jugadores.
 */
public class IniciarColocacionKS implements IKnowledgeSource { // O IKnowledgeSource

    private final IBlackboard blackboard;
    private final IServer server;
    private final Controller controller;

    public IniciarColocacionKS(IBlackboard blackboard, IServer server, Controller controller) {
        this.blackboard = blackboard;
        this.server = server;
        this.controller = controller;
    }

    @Override
    public boolean puedeProcesar(Evento evento) {
        return evento != null && "INICIAR_FASE_COLOCACION".equalsIgnoreCase(evento.getTipo());
    }

    @Override
    @SuppressWarnings("unchecked") // Por el casting de la lista de jugadores del Map
    public void procesarEvento(Socket clienteOrigenIgnorado, Evento evento) { // clienteOrigenIgnorado suele ser null para eventos sistémicos
        String idSala = (String) evento.obtenerDato("idSala");
        if (idSala == null || idSala.isBlank()) {
            System.err.println("INICIAR_COLOCACION_KS: Evento no contenía idSala válido.");
            return;
        }
        idSala = idSala.trim();

        System.out.println("INICIAR_COLOCACION_KS: Procesando inicio de colocación para sala: " + idSala);

        // 1. Obtener la PartidaDTO del Blackboard
        PartidaDTO partida = blackboard.getPartidaDTO(idSala);
        if (partida == null) {
            System.err.println("INICIAR_COLOCACION_KS: No se encontraron datos de PartidaDTO para la sala " + idSala);
            return;
        }

        // 2. Verificar jugadores
        JugadorDTO j1 = partida.getJugador1();
        JugadorDTO j2 = partida.getJugador2();

        if (j1 == null || j2 == null) {
            System.err.println("INICIAR_COLOCACION_KS: La partida '" + idSala + "' no tiene dos jugadores asignados. J1: " + (j1!=null) + ", J2: " + (j2!=null));
            // Podría ser que un jugador se desconectó justo antes. Considerar limpiar la sala o enviar error.
            return;
        }
        System.out.println("INICIAR_COLOCACION_KS: Jugador 1: " + j1.getNombre() + ", Jugador 2: " + j2.getNombre());


        // 3. Actualizar Estado de la PartidaDTO en Blackboard
        // Asegurarse que los flags de confirmación de tablero estén en false
        j1.setHaConfirmadoTablero(false);
        j2.setHaConfirmadoTablero(false);
        partida.setEstado(EstadoPartida.CONFIGURACION); // O un estado específico como "COLOCANDO_BARCOS"
        // Los placeholders "tableroJsonJugador1/2" ya no son necesarios en PartidaDTO si los TableroFlotaDTO
        // se envían después por los clientes y se guardan en los JugadorDTO respectivos.

        if (blackboard.actualizarPartida(partida)) {
            System.out.println("INICIAR_COLOCACION_KS: Estado de partida '" + idSala + "' actualizado a " + partida.getEstado() + " en Blackboard.");
        } else {
            System.err.println("INICIAR_COLOCACION_KS: Error al actualizar partida '" + idSala + "' en Blackboard.");
            // Considerar cómo manejar este error. ¿Reintentar? ¿Notificar?
            return;
        }

        // 4. Definir la Flota Estándar a colocar
        // Esta información es la que el cliente usará para saber qué barcos debe permitir colocar.
        // Podrías tener esto en constantes o una clase de configuración.
        // Formato: Lista de objetos sería mejor que un string, pero adaptamos a tu string actual:
        List<String> definicionFlota = new ArrayList<>();
        // Usando el enum TipoNave para obtener los datos
        for (TipoNave tipo : TipoNave.values()) { // Asumiendo que TipoNave tiene los tipos y sus longitudes
             // La cantidad de cada barco es según las reglas del juego (ej. 2 Portaaviones, etc.)
             // Aquí simplifico y asumo 1 de cada para el ejemplo de la flota string.
             // Debes ajustar esto a las reglas reales:
             // 2 Porta aviones (4 casillas) -> "Portaaviones:4" (repetir 2 veces o enviar cantidad)
             // 2 Cruceros (3 casillas)
             // 4 Submarinos (2 casillas)
             // 3 Barcos (1 casilla) -> BARCO_PATRULLA en tu enum
            
            // Ejemplo simplificado de 1 de cada para el string:
            // definicionFlota.add(tipo.name() + ":" + tipo.getLongitud());

            // Ejemplo más acorde a tus reglas (necesitarás un bucle para las cantidades):
            if (tipo == TipoNave.PORTAAVIONES) { for(int i=0; i<2; i++) definicionFlota.add(tipo.name() + ":" + tipo.getLongitud()); }
            if (tipo == TipoNave.CRUCERO) { for(int i=0; i<2; i++) definicionFlota.add(tipo.name() + ":" + tipo.getLongitud()); }
            if (tipo == TipoNave.SUBMARINO) { for(int i=0; i<4; i++) definicionFlota.add(tipo.name() + ":" + tipo.getLongitud()); }
            if (tipo == TipoNave.BARCO_PATRULLA) { for(int i=0; i<3; i++) definicionFlota.add(tipo.name() + ":" + tipo.getLongitud()); }
        }
        String flotaStringParaEnviar = String.join(";", definicionFlota.toString()); // Usar ';' como delimitador si es más fácil de parsear que ','

        // 5. Enviar evento "INICIAR_COLOCACION" a ambos clientes
        Evento eventoCliente = new Evento("INICIAR_COLOCACION"); // El cliente escucha este tipo de evento
        eventoCliente.agregarDato("idSala", idSala);
        eventoCliente.agregarDato("flota", flotaStringParaEnviar); // "PORTAAVIONES:4;PORTAAVIONES:4;CRUCERO:3;..."
        // También es buena idea enviarles el PartidaDTO actualizado para que tengan el estado más reciente
        // String partidaJson = new com.google.gson.Gson().toJson(partida);
        // String partidaJsonBase64 = java.util.Base64.getEncoder().encodeToString(partidaJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        // eventoCliente.agregarDato("partidaActualizadaJsonBase64", partidaJsonBase64);

        Socket socketJ1 = blackboard.getSocketDeUsuario(j1.getNombre());
        Socket socketJ2 = blackboard.getSocketDeUsuario(j2.getNombre());

        if (socketJ1 != null) {
            server.enviarEventoACliente(socketJ1, eventoCliente);
            System.out.println("INICIAR_COLOCACION_KS: Enviado INICIAR_COLOCACION a J1 (" + j1.getNombre() + ") para sala " + idSala);
        } else {
            System.err.println("INICIAR_COLOCACION_KS: No se encontró socket para J1 (" + j1.getNombre() + ").");
        }

        if (socketJ2 != null) {
            server.enviarEventoACliente(socketJ2, eventoCliente);
            System.out.println("INICIAR_COLOCACION_KS: Enviado INICIAR_COLOCACION a J2 (" + j2.getNombre() + ") para sala " + idSala);
        } else {
            System.err.println("INICIAR_COLOCACION_KS: No se encontró socket para J2 (" + j2.getNombre() + ").");
        }

        System.out.println("INICIAR_COLOCACION_KS: Proceso para sala " + idSala + " completado.");
        // No se llama a blackboard.respuestaFuenteC porque este evento fue originado internamente por el servidor
        // (disparado por UnirseSalaKS).
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