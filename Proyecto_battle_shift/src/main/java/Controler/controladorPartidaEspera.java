/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controler; // O tu paquete de controladores

// --- Imports ---
import View.PartidaEspera; // La Vista que este controlador maneja
import com.mycompany.servercomunicacion.ServerComunicacion;
import java.util.List;    // Para procesar lista de jugadores
import java.util.Map;     // Para procesar datos de eventos
import javax.swing.SwingUtilities; // Para asegurar actualizaciones de UI en el hilo correcto

/**
 * Controlador específico para la lógica de la Pantalla de Espera (Lobby).
 * El juego inicia automáticamente cuando la sala tiene 2 jugadores.
 */
public class controladorPartidaEspera {

    // --- Atributos ---
    private final ServerComunicacion serverComunicacion; // Comunicación con el servidor
    private final String idSala;                         // ID de la sala que maneja
    private PartidaEspera vistaEspera;                   // Referencia a la vista asociada

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
        this.vistaEspera = vista; // Guardar referencia a la vista
        System.out.println("CONTROLLER [Espera]: Controlador inicializado y vista asignada.");
    }
    
    

    // El método setVista no es estrictamente necesario si siempre se pasa en el constructor,
    // pero puede ser útil si se reasigna la vista (poco común).
    // public void setVista(PartidaEspera vista) { this.vistaEspera = vista; }


    // --- Métodos llamados por la Vista (PartidaEspera) ---

    /** Eliminado porque ya no hay botón Listo */
    // public void jugadorListo() { ... }

    /**
     * Envía el evento 'SALIR_SALA' al servidor cuando el usuario quiere abandonar.
     */
    public void salirDeSala() {
         System.out.println("CONTROLLER [Espera]: Jugador local saliendo de sala " + idSala + ". Enviando evento...");
         if (!handleNotConnectedError("salir de sala")) return; // Verificar conexión

         // --- CORRECCIÓN: Llamar al método público específico en ServerComunicacion ---
         serverComunicacion.desconectar();
         // --- FIN CORRECCIÓN ---

         System.out.println("CONTROLLER [Espera]: Evento SALIR_SALA enviado.");
         // La vista llamará a dispose() después de esto.
    }

    /**
     * Podría usarse para pedir explícitamente el estado actual de la sala al servidor.
     * (Requiere implementar el método en ServerComunicacion y la lógica en el servidor).
     */
    public void solicitarActualizacionEstadoSala() {
         System.out.println("CONTROLLER [Espera]: Solicitando actualización de estado para sala " + idSala);
         if (!handleNotConnectedError("actualizar estado")) return;
         // TODO: Implementar si es necesario:
         // String mensaje = "EVENTO;TIPO=PEDIR_ACTUALIZACION_SALA;idSala=" + idSala;
         // serverComunicacion.enviarMensaje(mensaje); // O un método específico si lo creas
         System.out.println("CONTROLLER [Espera]: Funcionalidad PEDIR_ACTUALIZACION_SALA no implementada en ServerComunicacion (o comentada).");
    }


    // --- Métodos llamados por controladorInicio para DELEGAR respuestas ---

    /**
     * Procesa actualizaciones del estado de la sala (ej. llega el otro jugador).
     * Llamado por controladorInicio al recibir "ACTUALIZACION_SALA".
     * @param datos Los datos del evento (esperamos una clave "jugadores" con List<String>).
     */
    @SuppressWarnings("unchecked") // Necesario por el casting de List<String>
    public void procesarActualizacionSala(Map<String, Object> datos) {
        System.out.println("CONTROLLER [Espera]: Procesando actualización de sala delegada: " + datos);
        if (vistaEspera == null) {
            System.err.println("CONTROLLER [Espera] ERROR: Vista es null en procesarActualizacionSala.");
            return;
        }

        // Extraer y actualizar lista de jugadores
        if (datos != null && datos.containsKey("jugadores")) {
            Object jugObj = datos.get("jugadores");
            if (jugObj instanceof List) {
                try {
                     // Asumimos que la lista contiene nombres (String)
                     List<String> nombresJugadores = (List<String>) jugObj;
                     // Llamar al método de la vista para actualizar la UI
                     vistaEspera.actualizarListaJugadores(nombresJugadores);
                } catch (ClassCastException e) {
                    System.err.println("CONTROLLER [Espera] ERROR: Datos de jugadores no son List<String> en ACTUALIZACION_SALA.");
                } catch (Exception e) {
                     System.err.println("CONTROLLER [Espera] ERROR inesperado procesando lista jugadores: " + e.getMessage());
                }
            } else {
                 System.err.println("CONTROLLER [Espera]: 'jugadores' recibido pero no es una Lista.");
            }
        } else {
             System.out.println("CONTROLLER [Espera]: Mensaje ACTUALIZACION_SALA no contenía 'jugadores'.");
        }
        // Ya no se procesan estados de "listo" aquí.
    }

    /**
     * Procesa el evento que indica el inicio de la fase de colocación de barcos.
     * Llamado por controladorInicio al recibir "INICIAR_COLOCACION".
     * @param datos Contiene el idSala y la 'flota' a colocar.
     */
    // Dentro de controladorPartidaEspera.java

    public void procesarInicioColocacion(Map<String, Object> datos) {
        // ---- TRY-CATCH ENVOLVENTE ----
        try {
            // Log de entrada (Intenta ejecutar esto)
            System.out.println("CONTROLLER [Espera]: Procesando inicio de colocación delegado: " + datos);

            if (vistaEspera == null) {
                System.err.println("CONTROLLER [Espera] ERROR: Vista es null en procesarInicioColocacion.");
                return; // Salir si no hay vista
            }
            System.out.println("DEBUG [Espera]: Referencia a vistaEspera OK."); // Log extra

            String idSalaRecibido = null;
            String flotaString = null;

            // Extraer datos con más cuidado
             if (datos != null) {
                  Object idObj = datos.get("idSala");
                  Object flotaObj = datos.get("flota");
                  if (idObj instanceof String) idSalaRecibido = (String) idObj;
                  if (flotaObj instanceof String) flotaString = (String) flotaObj;
             }
            System.out.println("DEBUG [Espera]: Datos extraídos -> idSala=" + idSalaRecibido + ", flota=" + (flotaString != null && flotaString.length() > 50 ? flotaString.substring(0,50)+"..." : flotaString) );


            // Validar datos
            if (idSalaRecibido == null || !idSalaRecibido.equals(this.idSala)) {
                 System.err.println("CONTROLLER [Espera] ERROR: ID de sala ("+idSalaRecibido+") no coincide en INICIAR_COLOCACION.");
                 vistaEspera.mostrarError("Error interno: ID de sala no coincide.", true);
                 return;
            }
            if (flotaString == null || flotaString.isBlank()){
                 System.err.println("CONTROLLER [Espera] ERROR: Flota no recibida en INICIAR_COLOCACION.");
                 vistaEspera.volverAPantallaAnterior("Error iniciando colocación: No se recibió la flota.");
                 return;
            }

            // Llamar a la navegación
            System.out.println("CONTROLLER [Espera]: Llamando a vista para navegar a pantalla de colocación...");
            vistaEspera.navegarAPantallaColocacion(idSala, flotaString);
            System.out.println("DEBUG [Espera]: Llamada a navegarAPantallaColocacion completada.");

        } catch (Throwable t) {
             // --- CAPTURAR CUALQUIER ERROR AQUÍ ---
             System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
             System.err.println("!!!!! ERROR GRAVE DENTRO DE procesarInicioColocacion !!!!!");
             System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
             t.printStackTrace(); // Imprimir el stack trace COMPLETO
              // Intentar mostrar un error en la UI como último recurso
              if(vistaEspera != null) {
                  final String errorFatalMsg = "Error fatal procesando inicio de colocación:\n" + t.toString();
                   SwingUtilities.invokeLater(() -> vistaEspera.mostrarError(errorFatalMsg, true));
              }
        } finally {
             // Log de salida del método (si no hubo excepción grave antes)
              System.out.println("DEBUG [Espera]: Fin de procesarInicioColocacion (dentro de finally).");
        }
    }

    /**
     * Procesa el evento de inicio de partida (fase de disparos).
     * Llamado por controladorInicio al recibir "INICIAR_PARTIDA".
     * @param datos Datos relevantes para iniciar la partida.
     */
    public void procesarInicioPartida(Map<String, Object> datos) {
        System.out.println("CONTROLLER [Espera]: Procesando inicio de partida delegado: " + datos);
        if (vistaEspera == null) { return; }
        System.out.println("CONTROLLER [Espera]: Llamando a vista para navegar a pantalla de juego...");
        vistaEspera.navegarAPantallaJuego(datos);
    }

    /**
     * Procesa la notificación de que el oponente salió de la sala.
     * Llamado por controladorInicio al recibir "OPONENTE_SALIO".
     * @param datos Información sobre la salida (opcional).
     */
    public void procesarSalidaOponente(Map<String, Object> datos) {
        System.out.println("CONTROLLER [Espera]: Procesando salida de oponente delegada.");
        if (vistaEspera == null) { return; }
        System.out.println("CONTROLLER [Espera]: Llamando a vista para volver a pantalla anterior...");
        vistaEspera.volverAPantallaAnterior("El oponente ha salido de la sala.");
    }

    public void mostrarMensajeEsperaFlota(String mensaje) {
    if (vistaEspera != null) {
        SwingUtilities.invokeLater(() -> vistaEspera.mostrarMensajeDeEstado(mensaje));
    }
}

public void mostrarMensajeOponenteListo(String mensaje) {
     if (vistaEspera != null) {
        SwingUtilities.invokeLater(() -> vistaEspera.mostrarMensajeDeEstado(mensaje));
    }
}

public void mostrarErrorColocacion(String mensajeError) {
    if (vistaEspera != null) {
        // El método mostrarError en tu PartidaEspera toma (mensaje, esCriticoQueCierraVista)
        SwingUtilities.invokeLater(() -> vistaEspera.mostrarError(mensajeError, false)); // false para no cerrar la vista
    }
}

// Para cerrar la vista cuando se inicia el combate o hay error fatal
public void cerrarVista() {
    if (vistaEspera != null) {
        SwingUtilities.invokeLater(() -> vistaEspera.dispose());
         System.out.println("CONTROLLER [Espera]: Vista PartidaEspera cerrada.");
    }
}

public boolean isVistaActiva() {
    return vistaEspera != null && vistaEspera.isDisplayable();
}
    
    
    /**
     * Procesa un error específico de esta sala recibido del servidor.
     * Llamado por controladorInicio al recibir "ERROR_SALA".
     * @param datos Contiene el mensaje de error.
     */
     public void procesarErrorSala(Map<String, Object> datos) {
        System.err.println("CONTROLLER [Espera]: Procesando error específico de sala delegado: " + datos);
        if (vistaEspera == null) { return; }
        String errorMsg = "Error desconocido en la sala.";
        if (datos != null && datos.get("error") instanceof String) {
            errorMsg = (String) datos.get("error");
        }
        System.out.println("CONTROLLER [Espera]: Llamando a vista para volver a pantalla anterior debido a error...");
        vistaEspera.volverAPantallaAnterior("Error en la sala: " + errorMsg);
    }

    // --- Métodos de UI eliminados (mostrarError, mostrarMensaje) ---
    // Estos métodos deben estar en la clase de la Vista (PartidaEspera)

    /**
     * Helper interno para verificar conexión y notificar error a la vista.
     * @param accion Descripción de la acción que requiere conexión.
     * @return true si está conectado, false si no.
     */
    private boolean handleNotConnectedError(String accion) {
        if (!serverComunicacion.isConectado()) {
             System.err.println("CONTROLLER [Espera]: No conectado. No se puede " + accion + ".");
             if(vistaEspera != null) {
                 // --- CORRECCIÓN: Llamar a mostrarError con dos argumentos ---
                 SwingUtilities.invokeLater(() -> vistaEspera.mostrarError("No estás conectado al servidor.", true));
             }
             return false;
        }
        return true;
    }
    
    public String getIdSala() {
        return this.idSala;
    }
    
}