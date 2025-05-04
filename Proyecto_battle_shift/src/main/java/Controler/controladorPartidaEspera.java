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
import View.PartidaEspera; // La Vista que este controlador maneja
import com.mycompany.servercomunicacion.ServerComunicacion;
import java.util.List;    // Para procesar lista de jugadores
import java.util.Map;     // Para procesar datos de eventos
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities; // Para asegurar actualizaciones de UI en el hilo correcto

/**
 * Controlador específico para la lógica de la Pantalla de Espera (Lobby).
 * Maneja acciones del usuario como "Listo" o "Salir" y procesa
 * actualizaciones del estado de la sala recibidas del servidor (delegadas
 * por controladorInicio).
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
        // Validar dependencias
        if (serverComunicacion == null || idSala == null || vista == null) {
            throw new IllegalArgumentException("Dependencias (ServerComunicacion, idSala, Vista) no pueden ser nulas para controladorPartidaEspera.");
        }
        this.serverComunicacion = serverComunicacion;
        this.idSala = idSala;
        this.vistaEspera = vista; // Guardar referencia a la vista
        System.out.println("CONTROLLER [Espera]: Controlador inicializado y vista asignada.");
        // Opcional: Solicitar estado inicial al entrar a la sala
        // solicitarActualizacionEstadoSala();
    }

    // --- Métodos llamados por la Vista (PartidaEspera) ---

   

    

    public void salirDeSala() {
         System.out.println("CONTROLLER [Espera]: Jugador local saliendo de sala " + idSala + ". Enviando evento...");
         if (!handleNotConnectedError("salir de sala")) return;
         // --- CORRECCIÓN: Llamar a método público ---
         serverComunicacion.desconectar();
         // --- FIN CORRECCIÓN ---
         System.out.println("CONTROLLER [Espera]: Evento SALIR_SALA enviado.");
    }

    /**
     * Podría usarse para pedir explícitamente el estado actual de la sala al servidor.
     */
    public void solicitarActualizacionEstadoSala() {
         System.out.println("CONTROLLER [Espera]: Solicitando actualización de estado para sala " + idSala);
         if (!handleNotConnectedError("actualizar estado")) return;
         // --- CORRECCIÓN: Crear método público si se necesita ---
         // No creamos un método público para esto antes, pero si lo necesitas:
         // serverComunicacion.pedirActualizacionSala(idSala);
         // O enviar uno genérico si ServerComunicacion tuviera un método para eso:
         // Map<String,Object> data = new HashMap<>(); data.put("idSala", idSala);
         // serverComunicacion.enviarEventoGenerico("PEDIR_ACTUALIZACION_SALA", data);
         // Por ahora, lo comentamos si no existe el método en ServerComunicacion:
         System.out.println("CONTROLLER [Espera]: Funcionalidad PEDIR_ACTUALIZACION_SALA no implementada en ServerComunicacion.");
    }

    // --- Métodos llamados por controladorInicio para DELEGAR respuestas ---
    // Estos métodos AHORA existen y tienen una implementación básica (logging + llamada a la vista).

    /**
     * Procesa actualizaciones del estado de la sala (ej. nuevo jugador, jugador listo).
     * Llamado por controladorInicio cuando llega un evento relevante como "ACTUALIZACION_SALA".
     * @param datos Los datos del evento (ej. lista de jugadores, quién está listo).
     */
    @SuppressWarnings("unchecked") // Necesario por el casting de List<String>
    public void procesarActualizacionSala(Map<String, Object> datos) {
        System.out.println("CONTROLLER [Espera]: Procesando actualización de sala delegada: " + datos);
        if (vistaEspera == null) {
            System.err.println("CONTROLLER [Espera] ERROR: Vista es null en procesarActualizacionSala.");
            return;
        }

        // Extraer y actualizar lista de jugadores (ejemplo)
        if (datos != null && datos.containsKey("jugadores")) {
            Object jugObj = datos.get("jugadores");
            // Asumimos que el servidor envía una lista de Strings (nombres)
            if (jugObj instanceof List) {
                try {
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

        // TODO: Procesar otros datos que puedan venir en la actualización
        // como quién está listo, etc., y llamar a los métodos correspondientes de vistaEspera.
        // Ejemplo:
        // boolean oponenteListo = "true".equalsIgnoreCase(String.valueOf(datos.getOrDefault("oponenteListo", "false")));
        // if (oponenteListo) vistaEspera.mostrarMensaje("Oponente listo!", false);
        // boolean ambosListos = "true".equalsIgnoreCase(String.valueOf(datos.getOrDefault("ambosListos", "false")));
        // if (ambosListos) vistaEspera.habilitarInicioJuego("Ambos listos!");

    }

    /**
     * Procesa el evento que indica el inicio de la fase de colocación de barcos.
     * Llamado por controladorInicio al recibir "INICIAR_COLOCACION".
     * @param datos Contiene el idSala y la 'flota' a colocar.
     */
    public void procesarInicioColocacion(Map<String, Object> datos) {
        System.out.println("CONTROLLER [Espera]: Procesando inicio de colocación delegado: " + datos);
        if (vistaEspera == null) {
            System.err.println("CONTROLLER [Espera] ERROR: Vista es null en procesarInicioColocacion.");
            return;
        }

        String idSalaRecibido = (String) datos.get("idSala");
        String flotaString = (String) datos.get("flota");

        // Validar ID de sala (opcional pero recomendado)
        if (idSalaRecibido == null || !idSalaRecibido.equals(this.idSala)) {
             System.err.println("CONTROLLER [Espera] ERROR: ID de sala ("+idSalaRecibido+") no coincide en INICIAR_COLOCACION.");
             return;
        }
        if (flotaString == null || flotaString.isBlank()){
             System.err.println("CONTROLLER [Espera] ERROR: Flota no recibida en INICIAR_COLOCACION.");
             vistaEspera.volverAPantallaAnterior("Error interno: Flota no recibida.");
             return;
        }
        // Llamar a la vista para que navegue a la pantalla de colocación
        System.out.println("CONTROLLER [Espera]: Llamando a vista para navegar a pantalla de colocación...");
        vistaEspera.navegarAPantallaColocacion(idSala, flotaString);
    }

    /**
     * Procesa el evento de inicio de partida (fase de disparos).
     * Llamado por controladorInicio al recibir "INICIAR_PARTIDA".
     * @param datos Datos relevantes para iniciar la partida (ej. tablero inicial, turno).
     */
    public void procesarInicioPartida(Map<String, Object> datos) {
        System.out.println("CONTROLLER [Espera]: Procesando inicio de partida delegado: " + datos);
        if (vistaEspera == null) {
             System.err.println("CONTROLLER [Espera] ERROR: Vista es null en procesarInicioPartida.");
             return;
        }
        // Llama a la vista para navegar a la pantalla de juego principal
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
        if (vistaEspera == null) {
            System.err.println("CONTROLLER [Espera] ERROR: Vista es null en procesarSalidaOponente.");
            return;
        }
        // Llama a la vista para volver atrás con un mensaje
        System.out.println("CONTROLLER [Espera]: Llamando a vista para volver a pantalla anterior...");
        vistaEspera.volverAPantallaAnterior("El oponente ha salido de la sala.");
    }

    /**
     * Procesa un error específico de esta sala recibido del servidor.
     * Llamado por controladorInicio al recibir "ERROR_SALA".
     * @param datos Contiene el mensaje de error.
     */
     public void procesarErrorSala(Map<String, Object> datos) {
        System.err.println("CONTROLLER [Espera]: Procesando error específico de sala delegado: " + datos);
        if (vistaEspera == null) {
            System.err.println("CONTROLLER [Espera] ERROR: Vista es null en procesarErrorSala.");
            return;
        }
        String errorMsg = (String) datos.getOrDefault("error", "Error desconocido en la sala.");
        // Llama a la vista para volver atrás con un mensaje de error
        System.out.println("CONTROLLER [Espera]: Llamando a vista para volver a pantalla anterior debido a error...");
        vistaEspera.volverAPantallaAnterior("Error en la sala: " + errorMsg);
    }
     
     /**
     * Muestra un mensaje de estado o error en el JLabel jLabelInfoOponente.
     * @param mensaje El mensaje a mostrar.
     * @param esError Indica si es un error (cambia color y muestra diálogo).
     */
    public void mostrarError(String mensaje, boolean esError) { // Recibe boolean
         SwingUtilities.invokeLater(() -> {
             System.out.println("VIEW [PartidaEspera]: Mostrando mensaje (Error=" + esError + "): " + mensaje);
             
              
             // Mostrar un diálogo extra si es un error para más visibilidad
             if (esError) {
                  JOptionPane.showMessageDialog(this.vistaEspera, mensaje, "Error en Sala", JOptionPane.WARNING_MESSAGE);
             }
         });
    }
    // Sobrecarga opcional si a veces solo quieres mostrar info sin diálogo
     public void mostrarMensaje(String mensaje) {
         mostrarError(mensaje, false); // Llama al otro método indicando que NO es error
     }


     /**
     * Helper para verificar conexión y mostrar error en la vista asociada.
     * @param accion Descripción de la acción que requiere conexión.
     * @return true si está conectado, false si no.
     */
    // --- Helper (Corregir llamada a mostrarError) ---
    private boolean handleNotConnectedError(String accion) {
        if (!serverComunicacion.isConectado()) {
             System.err.println("CONTROLLER [Espera]: No conectado. No se puede " + accion + ".");
             if(vistaEspera != null) {
                 // --- CORRECCIÓN: Usar método con 2 args ---
                 SwingUtilities.invokeLater(() -> vistaEspera.mostrarMensaje("No estás conectado al servidor.", true));
             }
             return false;
        }
        return true;
    }
    
}
