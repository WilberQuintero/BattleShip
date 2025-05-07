/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controler;

/**
 *
 * @author caarl
 */

// --- Imports ---
import View.UnirseJugar; // La Vista que este controlador maneja
import com.mycompany.servercomunicacion.ServerComunicacion;
import java.util.Map;
import javax.swing.SwingUtilities; // Para asegurar actualizaciones de UI en el hilo correcto
import javax.swing.JOptionPane;   // Para mostrar mensajes de error

/**
 * Controlador específico para la pantalla/lógica de Crear y Unirse a una Sala (Partida).
 * Navega para Crear Sala de forma optimista, pero ESPERA confirmación para Unirse a Sala.
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
     * (Mantenemos optimista esta acción si así lo deseas).
     * @param idSala El nombre/ID deseado para la sala.
     */
    public void solicitarCreacionSala(String idSala) {
        System.out.println("CONTROLLER [CrearPartida]: Solicitud de Vista para crear sala: " + idSala);
        if (!handleNotConnectedError("crear sala")) return;

        // 1. Enviar el evento al servidor
        serverComunicacion.crearSala(idSala);
        System.out.println("CONTROLLER [CrearPartida]: Solicitud CREAR_SALA enviada a ServerComunicacion.");

        // 2. Navegar INMEDIATAMENTE (Optimista)
        navegarOptimistaAEspera(idSala, "Crear");
    }

    /**
     * Envía la solicitud para unirse a una sala existente al servidor.
     * **YA NO NAVEGA INMEDIATAMENTE**. Espera la respuesta del servidor.
     * @param idSala El ID de la sala a la que unirse.
     */
    public void solicitarUnirseSala(String idSala) {
        System.out.println("CONTROLLER [CrearPartida]: Solicitud de Vista para unirse a sala: " + idSala);
         if (!handleNotConnectedError("unirse a sala")) return;

        // 1. Enviar el evento al servidor
        serverComunicacion.unirseASala(idSala);
        System.out.println("CONTROLLER [CrearPartida]: Solicitud UNIRSE_SALA enviada. Esperando respuesta...");

        // --- NAVEGACIÓN OPTIMISTA ELIMINADA DE AQUÍ ---
    }

    // --- Métodos Helpers Internos ---

    /**
     * Realiza la navegación optimista a la pantalla de espera (usado solo por Crear Sala ahora).
     */
    private void navegarOptimistaAEspera(String idSala, String accion) {
        if (vistaUnirseJugar != null) {
             System.out.println("CONTROLLER [CrearPartida]: Navegando a pantalla de espera (Optimista para " + accion + ")...");
             SwingUtilities.invokeLater(() -> {
                vistaUnirseJugar.navegarAPantallaEspera(idSala);
             });
        } else { /* ... (manejo error si vista es null) ... */ }
    }

     /**
     * Helper interno para verificar conexión y notificar error a la vista.
     */
    private boolean handleNotConnectedError(String accion) {
        if (!serverComunicacion.isConectado()) {
             System.err.println("CONTROLLER [CrearPartida]: No conectado. No se puede " + accion + ".");
             if(vistaUnirseJugar != null) {
                 SwingUtilities.invokeLater(() -> {
                     vistaUnirseJugar.mostrarError("No estás conectado al servidor.");
                     // Reactivar botones correspondientes
                     if (accion.contains("crear")) vistaUnirseJugar.reactivarBotonCrear();
                     if (accion.contains("unirse")) vistaUnirseJugar.reactivarBotonUnirse();
                 });
             }
             return false;
        }
        return true;
     }


    // --- Métodos llamados por controladorInicio para DELEGAR respuestas ---

    /**
     * Procesa la respuesta del servidor para la creación de sala.
     * SOLO loguea o maneja el caso de ERROR (porque ya navegamos optimisticamente).
     */
    public void procesarRespuestaCrearSala(boolean exito, Map<String, Object> datos) {
        System.out.println("DEBUG [CrearPartida]: Ingresando a procesarRespuestaCrearSala (Respuesta recibida). Éxito=" + exito);
        if (vistaUnirseJugar == null && !exito) { // Si falló y ya no tenemos vista, mostramos diálogo global
             String errorMsg = (String) datos.getOrDefault("error", "Error desconocido al crear sala.");
             System.err.println("CONTROLLER [CrearPartida]: ERROR POST-NAVEGACIÓN (Crear): Vista es null. Error del servidor: " + errorMsg);
             final String finalErrorMsg = errorMsg;
             SwingUtilities.invokeLater(() -> {
                   JOptionPane.showMessageDialog(null,"ERROR: Falló la creación de la sala:\n" + finalErrorMsg, "Error Creación", JOptionPane.ERROR_MESSAGE);
             });
             return;
        } else if (vistaUnirseJugar == null && exito) {
             // Éxito pero la vista ya no está (raro, pero posible si hubo error antes)
             System.out.println("CONTROLLER [CrearPartida]: Confirmación de sala creada recibida, pero la vista UnirseJugar ya no existe.");
             return;
        }

        // Procesamiento normal si la vista aún existe (principalmente para manejar errores post-navegación)
         if (exito) {
             String idSalaCreada = "";
             try{ idSalaCreada = (String) datos.getOrDefault("idSala", "?"); } catch(Exception e){}
             System.out.println("CONTROLLER [CrearPartida]: Confirmación de sala '" + idSalaCreada + "' creada recibida. (Ya se navegó optimisticamente).");
             // No se necesita acción de UI adicional aquí en caso de éxito
         } else {
             // ¡ERROR! Navegamos, pero el servidor dijo que falló.
             String errorMsg = (String) datos.getOrDefault("error", "Error desconocido al crear sala.");
             System.err.println("CONTROLLER [CrearPartida]: ERROR POST-NAVEGACIÓN (Crear) - Servidor reportó error: " + errorMsg);
             // Mostramos diálogo porque la pantalla activa ahora es PartidaEspera
             SwingUtilities.invokeLater(() -> {
                  JOptionPane.showMessageDialog(null,
                      "ERROR: Falló la creación de la sala en el servidor:\n" + errorMsg +
                      "\n\nSerás devuelto a la pantalla anterior.",
                      "Error de Creación de Sala", JOptionPane.ERROR_MESSAGE);
                  // TODO: Implementar lógica para cerrar PartidaEspera y volver a UnirseJugar
                  // Esto requiere que PartidaEspera o su controlador tengan forma de volver.
             });
         }
         System.out.println("DEBUG [CrearPartida]: Fin de procesarRespuestaCrearSala.");
    }

     /**
     * Procesa la respuesta del servidor para unirse a sala.
     * **AHORA inicia la navegación si la unión fue exitosa.**
     * @param exito true si se unió, false si hubo error.
     * @param datos Los datos recibidos del servidor.
     */
    public void procesarRespuestaUnirseSala(boolean exito, Map<String, Object> datos) {

         if (vistaUnirseJugar == null) {
             System.err.println("CONTROLLER [CrearPartida]: ERROR - Vista (UnirseJugar) no asignada para procesar respuesta UnirseSala.");
             return; // Salir si no hay vista
         }
         System.out.println("DEBUG [CrearPartida]: Referencia a vistaUnirseJugar OK."); // Log extra

         // Ya estamos en el hilo de Swing (viene de invokeLater en controladorInicio)

         if (exito) {
            String idSalaUnida = null; // Inicializar a null
             try {
                 // Extraer ID de sala
                 Object idObj = datos.get("idSala");
                 System.out.println("DEBUG [CrearPartida]: Objeto 'idSala' extraído (Unirse): " + idObj + (idObj != null ? " (Tipo: " + idObj.getClass().getName() + ")" : ""));
                 if (idObj instanceof String) idSalaUnida = (String) idObj;

                 // Validar ID
                 if (idSalaUnida == null || idSalaUnida.isBlank()) {
                     throw new Exception("ID de sala inválido en respuesta UNIDO_OK del servidor.");
                 }

                 System.out.println("CONTROLLER [CrearPartida]: Unión a sala '" + idSalaUnida + "' OK. Llamando a vistaUnirseJugar.navegarAPantallaEspera...");

                 // --- ¡NAVEGACIÓN AQUÍ! ---
                 vistaUnirseJugar.navegarAPantallaTablero(idSalaUnida);
                 // -------------------------
                 System.out.println("DEBUG [CrearPartida]: Llamada a navegarAPantallaEspera completada.");


             } catch (Exception e) {
                  // Error al procesar los datos de éxito o al navegar
                  System.err.println("CONTROLLER [CrearPartida] ERROR: Excepción procesando UNIDO_OK o durante navegación: " + e.getMessage());
                  e.printStackTrace();
                  // Mostrar error y reactivar botón en UnirseJugar
                  vistaUnirseJugar.mostrarError("Respuesta inesperada del servidor al unirse.");
                  vistaUnirseJugar.reactivarBotonUnirse();
             }
         } else { // Manejar ERROR_UNIRSE_SALA
             String errorMsg = "Error desconocido al unirse.";
             try{ // Extraer mensaje de error de forma segura
                  Object errObj = datos.get("error");
                   System.out.println("DEBUG [CrearPartida]: Objeto 'error' extraído (Unirse): " + errObj + (errObj != null ? " (Tipo: " + errObj.getClass().getName() + ")" : ""));
                  if(errObj instanceof String) errorMsg = (String) errObj;
                  else if (errObj != null) errorMsg = errObj.toString();
             } catch (Exception e) { System.err.println("CONTROLLER [CrearPartida] WARN: Excepción al obtener mensaje de error: " + e.getMessage());}

             System.err.println("CONTROLLER [CrearPartida]: Error al unirse a sala: " + errorMsg);
             // Mostrar error y reactivar botón en UnirseJugar
             try {
                 vistaUnirseJugar.mostrarError("Error al unirse: " + errorMsg);
                 vistaUnirseJugar.reactivarBotonUnirse();
                 System.out.println("DEBUG [CrearPartida]: Llamadas a mostrarError/reactivarBotonUnirse completadas.");
             } catch (Exception eMostrarError) {
                 System.err.println("CONTROLLER [CrearPartida] ERROR: Excepción dentro de vistaUnirseJugar.mostrarError/reactivar: " + eMostrarError.getMessage());
                 eMostrarError.printStackTrace();
             }
         }
         System.out.println("DEBUG [CrearPartida]: Fin de procesarRespuestaUnirseSala.");
    }
    
    
    // Dentro de la clase controladorCrearPartida.java

public void metodoDePruebaSuperSimple(String mensajeEntrada) {
    // Log muy distintivo para que sea fácil de ver
    System.out.println("*******************************************************************");
    System.out.println("***** MÉTODO DE PRUEBA SUPER SIMPLE EN controladorCrearPartida EJECUTADO *****");
    System.out.println("***** Mensaje recibido: " + mensajeEntrada);
    System.out.println("*******************************************************************");
    System.out.flush(); // Forzar que la consola muestre esto inmediatamente
}

// ... El resto de tus métodos (procesarRespuestaCrearSala, procesarRespuestaUnirseSala, etc.)
// pueden quedar como estaban o simplificados si aún lo están. Para esta prueba,
// solo nos importa si metodoDePruebaSuperSimple se ejecuta.

    
} // Fin de la clase