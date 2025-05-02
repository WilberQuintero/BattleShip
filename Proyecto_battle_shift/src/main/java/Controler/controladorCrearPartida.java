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
import javax.swing.SwingUtilities;

/**
 * Controlador específico para la pantalla/lógica de Crear y Unirse a una Sala (Partida).
 * Recibe la instancia de ServerComunicacion del controlador principal (controladorInicio).
 */
public class controladorCrearPartida {

    private final ServerComunicacion serverComunicacion; // Referencia compartida, final
    private UnirseJugar vistaUnirseJugar; // Referencia a la vista que maneja

    /**
     * Constructor. Se le debe pasar la instancia activa de ServerComunicacion.
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
     * Es importante que la Vista llame a este método después de crear el controlador.
     * @param vista La instancia de UnirseJugar.
     */
    public void setVista(UnirseJugar vista) {
        this.vistaUnirseJugar = vista;
    }

    // --- Métodos llamados por la Vista (UnirseJugar) ---

    /**
     * Le pide a ServerComunicacion que envíe la solicitud para crear una sala.
     * @param idSala El nombre/ID deseado para la sala.
     */
    public void solicitarCreacionSala(String idSala) {
        System.out.println("CONTROLLER [CrearPartida]: Solicitud de Vista para crear sala: " + idSala);
        if (!serverComunicacion.isConectado()) {
             System.err.println("CONTROLLER [CrearPartida]: No conectado. No se puede crear sala.");
             if(vistaUnirseJugar != null) {
                 SwingUtilities.invokeLater(() -> vistaUnirseJugar.mostrarError("No estás conectado al servidor."));
             }
             return;
        }
        // Llama al método correspondiente en la capa de comunicación
        serverComunicacion.crearSala(idSala);
        System.out.println("CONTROLLER [CrearPartida]: Solicitud CREAR_SALA enviada a ServerComunicacion.");
        // La respuesta llegará a controladorInicio.onMensajeServidor y será delegada aquí.
    }

    /**
     * Le pide a ServerComunicacion que envíe la solicitud para unirse a una sala.
     * @param idSala El ID de la sala a la que unirse.
     */
    public void solicitarUnirseSala(String idSala) {
        System.out.println("CONTROLLER [CrearPartida]: Solicitud de Vista para unirse a sala: " + idSala);
         if (!serverComunicacion.isConectado()) {
             System.err.println("CONTROLLER [CrearPartida]: No conectado. No se puede unir a sala.");
             if(vistaUnirseJugar != null) {
                 SwingUtilities.invokeLater(() -> vistaUnirseJugar.mostrarError("No estás conectado al servidor."));
             }
             return;
        }
        serverComunicacion.unirseASala(idSala);
        System.out.println("CONTROLLER [CrearPartida]: Solicitud UNIRSE_SALA enviada a ServerComunicacion.");
        // La respuesta llegará a controladorInicio.onMensajeServidor y será delegada aquí.
    }


   public void procesarRespuestaCrearSala(boolean exito, Map<String, Object> datos) {
        // --- PRIMERO: Añade este log para confirmar que se entra al método ---
        System.out.println("DEBUG [CrearPartida]: Ingresando a procesarRespuestaCrearSala. Éxito=" + exito);
        // --- FIN LOG ---

        if (vistaUnirseJugar == null) {
             System.err.println("CONTROLLER [CrearPartida]: ERROR - Vista no asignada.");
             return;
        }

        // --- Ya estamos en el Hilo de Swing gracias al invokeLater en controladorInicio ---
        // --- Llamamos a los métodos de la vista directamente ---
        if (exito) {
            String idSalaCreada = (String) datos.get("idSala");
            System.out.println("CONTROLLER [CrearPartida]: Sala '" + idSalaCreada + "' creada OK. Llamando a vistaUnirseJugar.navegarAPantallaEspera...");
            vistaUnirseJugar.navegarAPantallaEspera(idSalaCreada); // Llamada directa
        } else {
            String errorMsg = (String) datos.getOrDefault("error", "Error desconocido al crear sala.");
            System.err.println("CONTROLLER [CrearPartida]: Error al crear sala: " + errorMsg);
            vistaUnirseJugar.mostrarError("Error al crear sala: " + errorMsg); // Llamada directa
            vistaUnirseJugar.reactivarBotonCrear(); // Llamada directa
        }
        System.out.println("DEBUG [CrearPartida]: Fin de procesarRespuestaCrearSala."); // Log al final
    }

    public void procesarRespuestaUnirseSala(boolean exito, Map<String, Object> datos) {
         // --- PRIMERO: Añade este log para confirmar que se entra al método ---
         System.out.println("DEBUG [CrearPartida]: Ingresando a procesarRespuestaUnirseSala. Éxito=" + exito);
         // --- FIN LOG ---

         if (vistaUnirseJugar == null) { /*...*/ return; } // Error si no hay vista

         // --- Ya estamos en el Hilo de Swing ---
         if (exito) {
             String idSalaUnida = (String) datos.get("idSala");
             System.out.println("CONTROLLER [CrearPartida]: Unión a sala '" + idSalaUnida + "' OK. Llamando a vistaUnirseJugar.navegarAPantallaEspera...");
             vistaUnirseJugar.navegarAPantallaEspera(idSalaUnida); // Llamada directa
         } else {
             String errorMsg = (String) datos.getOrDefault("error", "Error desconocido al unirse.");
             System.err.println("CONTROLLER [CrearPartida]: Error al unirse a sala: " + errorMsg);
             vistaUnirseJugar.mostrarError("Error al unirse: " + errorMsg); // Llamada directa
             vistaUnirseJugar.reactivarBotonUnirse(); // Llamada directa
         }
         System.out.println("DEBUG [CrearPartida]: Fin de procesarRespuestaUnirseSala."); // Log al final
    }
}