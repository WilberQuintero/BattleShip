/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controler;

/**
 *
 * @author caarl
 */


import View.UnirseJugar; // Asume que esta es tu pantalla de Crear/Unirse
import com.mycompany.servercomunicacion.ServerComunicacion;
import java.util.Map; // Para procesar respuestas
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Controlador específico para la pantalla/lógica de Crear y Unirse a una Sala (Partida).
 * Recibe la instancia de ServerComunicacion del controlador principal (controladorInicio).
 */
public class controladorCrearPartida {

    private final ServerComunicacion serverComunicacion; // Referencia compartida
    private UnirseJugar vistaUnirseJugar; // Referencia a la vista que maneja

    /**
     * Constructor.
     * @param serverComunicacion La instancia compartida para comunicarse con el servidor.
     */
    public controladorCrearPartida(ServerComunicacion serverComunicacion) {
        System.out.println("CONTROLLER [CrearPartida]: Inicializando...");
        if (serverComunicacion == null) {
            throw new IllegalArgumentException("ServerComunicacion no puede ser nulo.");
        }
        this.serverComunicacion = serverComunicacion;
        System.out.println("CONTROLLER [CrearPartida]: Referencia a ServerComunicacion asignada.");
    }

    /**
     * Asigna la vista que este controlador gestionará.
     * @param vista La instancia de UnirseJugar.
     */
    public void setVista(UnirseJugar vista) {
        this.vistaUnirseJugar = vista;
    }

    // --- Métodos llamados por la Vista (UnirseJugar) ---

    /**
     * Envía la solicitud para crear una nueva sala al servidor Y NAVEGA INMEDIATAMENTE.
     * @param idSala El nombre/ID deseado para la sala.
     */
    public void solicitarCreacionSala(String idSala) {
        System.out.println("CONTROLLER [CrearPartida]: Solicitud de Vista para crear sala: " + idSala);
        if (!serverComunicacion.isConectado()) {
             handleNotConnectedError("crear sala");
             return;
        }

        // 1. Enviar el evento al servidor
        serverComunicacion.crearSala(idSala);
        System.out.println("CONTROLLER [CrearPartida]: Solicitud CREAR_SALA enviada a ServerComunicacion.");

        // 2. Navegar INMEDIATAMENTE (Optimista)
        navegarOptimistaAEspera(idSala, "Crear"); // Llama al método helper
    }

    /**
     * Envía la solicitud para unirse a una sala existente al servidor Y NAVEGA INMEDIATAMENTE.
     * @param idSala El ID de la sala a la que unirse.
     */
    public void solicitarUnirseSala(String idSala) {
        System.out.println("CONTROLLER [CrearPartida]: Solicitud de Vista para unirse a sala: " + idSala);
         if (!serverComunicacion.isConectado()) {
             handleNotConnectedError("unirse a sala");
             return;
        }

        // 1. Enviar el evento al servidor
        serverComunicacion.unirseASala(idSala);
        System.out.println("CONTROLLER [CrearPartida]: Solicitud UNIRSE_SALA enviada a ServerComunicacion.");

        // 2. Navegar INMEDIATAMENTE (Optimista)
        navegarOptimistaAEspera(idSala, "Unirse"); // Llama al método helper
    }

    // --- Métodos Helpers Internos ---

    /**
     * Maneja el error común de no estar conectado.
     * @param accion Descripción de la acción fallida.
     */
    private void handleNotConnectedError(String accion) {
        System.err.println("CONTROLLER [CrearPartida]: No conectado. No se puede " + accion + ".");
        if(vistaUnirseJugar != null) {
            SwingUtilities.invokeLater(() -> {
                vistaUnirseJugar.mostrarError("No estás conectado al servidor.");
                // Reactivar botones correspondientes si es necesario
                if (accion.contains("crear")) vistaUnirseJugar.reactivarBotonCrear();
                if (accion.contains("unirse")) vistaUnirseJugar.reactivarBotonUnirse();
            });
        }
    }

    /**
     * Realiza la navegación optimista a la pantalla de espera.
     * @param idSala El ID de la sala a la que se intenta entrar.
     * @param accion "Crear" o "Unirse" para logging.
     */
    private void navegarOptimistaAEspera(String idSala, String accion) {
        if (vistaUnirseJugar != null) {
             System.out.println("CONTROLLER [CrearPartida]: Navegando a pantalla de espera (Optimista para " + accion + ")...");
             // Asegurarse de que la navegación ocurra en el hilo de Swing
             SwingUtilities.invokeLater(() -> {
                 // Pasamos el ID de sala que *intentamos* crear/unir
                vistaUnirseJugar.navegarAPantallaEspera(idSala);
             });
        } else {
             System.err.println("CONTROLLER [CrearPartida]: ERROR - Vista es null, no se puede navegar optimisticamente para " + accion + ".");
             JOptionPane.showMessageDialog(null, "Error interno al procesar la solicitud.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    // --- Métodos llamados por controladorInicio para DELEGAR respuestas ---

    /**
     * Procesa la respuesta del servidor para la creación de sala.
     * SOLO loguea o maneja el caso de ERROR (porque ya navegamos).
     */
    public void procesarRespuestaCrearSala(boolean exito, Map<String, Object> datos) {
        System.out.println("CONTROLLER [CrearPartida]: Respuesta RECIBIDA para creación de sala. Éxito=" + exito);

        if (exito) {
            String idSalaCreada = (String) datos.getOrDefault("idSala", "?");
            System.out.println("CONTROLLER [CrearPartida]: Confirmación de sala '" + idSalaCreada + "' creada recibida. (Ya se navegó).");
            // No hay acción de UI aquí en caso de éxito.
        } else {
            // ¡ERROR! Navegamos, pero el servidor dijo que falló.
            String errorMsg = (String) datos.getOrDefault("error", "Error desconocido al crear sala.");
            System.err.println("CONTROLLER [CrearPartida]: ERROR POST-NAVEGACIÓN - Servidor reportó error al crear sala: " + errorMsg);
            // Mostrar error al usuario (ya está en otra pantalla)
            SwingUtilities.invokeLater(() -> {
                 JOptionPane.showMessageDialog(null, // null como parent para que sea global
                     "ERROR: Falló la creación de la sala en el servidor:\n" + errorMsg +
                     "\n\nSerás devuelto a la pantalla anterior.",
                     "Error de Creación de Sala", JOptionPane.ERROR_MESSAGE);
                 // TODO: Implementar lógica para cerrar la pantalla de espera y volver a UnirseJugar
            });
        }
    }

     /**
     * Procesa la respuesta del servidor para unirse a sala.
     * SOLO loguea o maneja el caso de ERROR (porque ya navegamos).
     */
    public void procesarRespuestaUnirseSala(boolean exito, Map<String, Object> datos) {
         System.out.println("CONTROLLER [CrearPartida]: Respuesta RECIBIDA para unirse a sala. Éxito=" + exito);

         if (exito) {
            String idSalaUnida = (String) datos.getOrDefault("idSala", "?");
            System.out.println("CONTROLLER [CrearPartida]: Confirmación de unión a sala '" + idSalaUnida + "' recibida. (Ya se navegó).");
            // No hay acción de UI aquí en caso de éxito.
         } else {
             // ¡ERROR! Navegamos, pero el servidor dijo que falló.
             String errorMsg = (String) datos.getOrDefault("error", "Error desconocido al unirse.");
             System.err.println("CONTROLLER [CrearPartida]: ERROR POST-NAVEGACIÓN - Servidor reportó error al unirse a sala: " + errorMsg);
            // Mostrar error al usuario (ya está en otra pantalla)
             SwingUtilities.invokeLater(() -> {
                  JOptionPane.showMessageDialog(null, // null como parent
                      "ERROR: Falló la unión a la sala en el servidor:\n" + errorMsg +
                      "\n\nSerás devuelto a la pantalla anterior.",
                      "Error al Unirse a Sala", JOptionPane.ERROR_MESSAGE);
                  // TODO: Implementar lógica para cerrar la pantalla de espera y volver a UnirseJugar
             });
         }
    }
}