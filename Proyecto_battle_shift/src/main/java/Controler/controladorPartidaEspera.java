/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controler;

import View.PartidaEspera;
import com.mycompany.servercomunicacion.ServerComunicacion;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;

/**
 *
 * @author caarl
 */
/**
 * Controlador para la lógica de la Pantalla de Espera (Lobby).
 */
public class controladorPartidaEspera {

    private final ServerComunicacion serverComunicacion;
    private final String idSala;
    private PartidaEspera vistaEspera; // Referencia a la vista

    /**
     * Constructor.
     * @param serverComunicacion Instancia compartida de comunicación.
     * @param idSala ID de la sala actual.
     * @param vista La instancia de la vista PartidaEspera.
     */
    public controladorPartidaEspera(ServerComunicacion serverComunicacion, String idSala, PartidaEspera vista) {
        System.out.println("CONTROLLER [Espera]: Inicializando para sala: " + idSala);
        if (serverComunicacion == null || idSala == null || vista == null) {
            throw new IllegalArgumentException("Dependencias nulas para controladorPartidaEspera.");
        }
        this.serverComunicacion = serverComunicacion;
        this.idSala = idSala;
        this.vistaEspera = vista;
        // Podríamos solicitar estado inicial aquí
        // solicitarActualizacionEstadoSala();
    }

    // --- Métodos llamados por la Vista (PartidaEspera) ---

    /**
     * Envía el evento 'JUGADOR_LISTO' al servidor.
     */
    public void jugadorListo() {
        System.out.println("CONTROLLER [Espera]: Jugador local listo en sala " + idSala + ". Enviando evento...");
        if (!handleNotConnectedError("indicar que estás listo")) return;

        // Enviar evento JUGADOR_LISTO
        String mensaje = "EVENTO;TIPO=JUGADOR_LISTO;idSala=" + idSala;
        serverComunicacion.enviarMensaje(mensaje);
        System.out.println("CONTROLLER [Espera]: Evento JUGADOR_LISTO enviado.");
    }

    /**
     * Envía el evento 'SALIR_SALA' al servidor. La vista se cierra sola.
     */
    public void salirDeSala() {
         System.out.println("CONTROLLER [Espera]: Jugador local saliendo de sala " + idSala + ". Enviando evento...");
         if (!handleNotConnectedError("salir de sala")) return; // Igual verificar conexión

         String mensaje = "EVENTO;TIPO=SALIR_SALA;idSala=" + idSala;
         serverComunicacion.enviarMensaje(mensaje);
         System.out.println("CONTROLLER [Espera]: Evento SALIR_SALA enviado.");
         // La vista llamará a dispose() y notificará al controladorPrincipal
    }

    /**
     * Podría usarse para pedir explícitamente el estado actual de la sala.
     */
    public void solicitarActualizacionEstadoSala() {
         System.out.println("CONTROLLER [Espera]: Solicitando actualización de estado para sala " + idSala);
         if (!handleNotConnectedError("actualizar estado")) return;
         String mensaje = "EVENTO;TIPO=PEDIR_ACTUALIZACION_SALA;idSala=" + idSala;
         serverComunicacion.enviarMensaje(mensaje);
    }


    // --- Métodos llamados por controladorInicio para DELEGAR respuestas ---

    /**
     * Procesa actualizaciones del estado de la sala (ej. nuevo jugador, jugador listo).
     */
    @SuppressWarnings("unchecked") // Por el casting de la lista de jugadores
    public void procesarActualizacionSala(Map<String, Object> datos) {
        System.out.println("CONTROLLER [Espera]: Procesando actualización de sala delegada: " + datos);
        if (vistaEspera == null) return;

        // Actualizar lista de jugadores si viene en los datos
        if (datos.containsKey("jugadores")) {
            Object jugObj = datos.get("jugadores");
            if (jugObj instanceof List) {
                try {
                     // Asumimos que la lista contiene nombres (String)
                     List<String> nombresJugadores = (List<String>) jugObj;
                     vistaEspera.actualizarListaJugadores(nombresJugadores); // Llama al método de la vista
                } catch (ClassCastException e) {
                    System.err.println("CONTROLLER [Espera]: Error al castear lista de jugadores: " + e);
                }
            } else {
                 System.err.println("CONTROLLER [Espera]: 'jugadores' recibido pero no es una Lista.");
            }
        } else {
             System.out.println("CONTROLLER [Espera]: Actualización no contenía lista de 'jugadores'.");
        }

        // Verificar si el oponente está listo (el servidor debe enviar este estado)
        boolean oponenteEstaListo = "true".equalsIgnoreCase(String.valueOf(datos.getOrDefault("oponenteListo", "false")));
        if (oponenteEstaListo) {
             vistaEspera.mostrarMensaje("¡Oponente está listo!", false);
        }

        // Verificar si ambos están listos para habilitar inicio
        boolean ambosListos = "true".equalsIgnoreCase(String.valueOf(datos.getOrDefault("ambosListos", "false")));
        if (ambosListos) {
             vistaEspera.habilitarInicioJuego("¡Ambos listos! Esperando inicio...");
        }
    }

    /**
     * Procesa el evento de inicio de partida.
     */
    public void procesarInicioPartida(Map<String, Object> datos) {
        System.out.println("CONTROLLER [Espera]: Procesando inicio de partida delegado: " + datos);
        if (vistaEspera == null) return;
        vistaEspera.navegarAPantallaJuego(datos); // Llama al método de la vista
    }

    /**
     * Procesa la notificación de que el oponente salió.
     */
    public void procesarSalidaOponente(Map<String, Object> datos) {
        System.out.println("CONTROLLER [Espera]: Procesando salida de oponente delegada.");
        if (vistaEspera == null) return;
        // Llama al método de la vista para volver atrás con mensaje
        vistaEspera.volverAPantallaAnterior("El oponente ha salido de la sala.");
    }

    /**
     * Procesa un error específico de esta sala.
     */
     public void procesarErrorSala(Map<String, Object> datos) {
        System.err.println("CONTROLLER [Espera]: Procesando error específico de sala delegado: " + datos);
        if (vistaEspera == null) return;
        String errorMsg = (String) datos.getOrDefault("error", "Error desconocido en la sala.");
        // Llama al método de la vista para volver atrás con mensaje
        vistaEspera.volverAPantallaAnterior("Error en la sala: " + errorMsg);
    }

     /**
     * Helper para verificar conexión y mostrar error en la vista.
     * @return true si está conectado, false si no.
     */
    private boolean handleNotConnectedError(String accion) {
        if (!serverComunicacion.isConectado()) {
             System.err.println("CONTROLLER [Espera]: No conectado. No se puede " + accion + ".");
             if(vistaEspera != null) {
                 // Usar invokeLater por si se llama desde fuera del EDT, aunque es menos probable aquí
                 SwingUtilities.invokeLater(() -> vistaEspera.mostrarError("No estás conectado al servidor.", true));
             }
             return false;
        }
        return true;
    }
}
