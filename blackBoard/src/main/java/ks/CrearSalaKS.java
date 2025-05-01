/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ks;
import com.mycompany.battleship.commons.Evento;
import com.mycompany.battleship.commons.IBlackboard;
import com.mycompany.battleship.commons.IServer;
import com.mycompany.blackboard.Controller;
import com.mycompany.blackboard.IKnowledgeSource;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 *
 * @author Hector
 */


// Asegúrate que implemente tu interfaz IKnowledgeSource correctamente
public class CrearSalaKS implements IKnowledgeSource {

    // Dependencias (final hace que deban asignarse en el constructor)
    private final IServer server;
    private final IBlackboard blackboard;
    private final Controller controller; // Hacerlo final si siempre se requiere

    // --- CORRECCIÓN/RECOMENDACIÓN 1: Unificar Constructores ---
    // Es mejor tener un único constructor que inicialice todas las dependencias
    // requeridas consistentemente. Si la dependencia del Controller es opcional,
    // la lógica que la usa debe manejar el caso null de forma más explícita,
    // pero aquí parece que sí se usa (en la notificación).
    // Recomiendo eliminar el constructor que deja 'controller' como null.

    /**
     * Constructor recomendado que inicializa todas las dependencias necesarias.
     * @param blackboard Instancia de IBlackboard.
     * @param server Instancia de IServer.
     * @param controller Instancia de Controller.
     */
    public CrearSalaKS(IBlackboard blackboard, IServer server, Controller controller) {
        // Es buena práctica verificar que las dependencias no sean nulas
        if (blackboard == null || server == null || controller == null) {
            throw new IllegalArgumentException("Las dependencias (Blackboard, Server, Controller) no pueden ser nulas.");
        }
        this.blackboard = blackboard;
        this.server = server;
        this.controller = controller;
    }

    // Se recomienda eliminar este constructor si siempre necesitas el controller:
    /*
    public CrearSalaKS(IServer server, IBlackboard blackboard) {
        this.server = server;
        this.blackboard = blackboard;
        this.controller = null; // Esto puede llevar a NullPointerException si se usa controller
    }
    */

    @Override
    public boolean puedeProcesar(Evento evento) {
        // Correcto: verifica el tipo de evento ignorando mayúsculas/minúsculas.
        return evento != null && "CREAR_SALA".equalsIgnoreCase(evento.getTipo());
    }

    @Override
    public void procesarEvento(Socket cliente, Evento evento) {
        // Verificación inicial de nulos. Correcto.
       if (evento == null || cliente == null) {
            System.err.println("CREAR_SALA_KS: Evento o cliente nulo.");
            return;
        }

       // --- Obtención y Validación del ID de Sala ---
       Object idSalaObj = evento.obtenerDato("idSala");
       System.out.println("DEBUG [CrearSalaKS]: Intentando obtener 'idSala'. Valor obtenido: " + idSalaObj +
                          " (Tipo: " + (idSalaObj != null ? idSalaObj.getClass().getName() : "null") + ")");

       String idSala = null;
       if (idSalaObj instanceof String) {
            String idSalaTemp = (String) idSalaObj;
            idSala = idSalaTemp.trim(); // Correcto: aplica trim()
       }

       // Validación. Correcto.
       if (idSala == null || idSala.isBlank()) {
           System.err.println("CREAR_SALA_KS: ID de sala inválido después de procesar (null, vacío o solo espacios). Valor original obj: '" + idSalaObj + "'");
           enviarRespuestaError(cliente, "Nombre de sala no válido o no proporcionado.");
           return;
       }

       System.out.println("CREAR_SALA_KS: Procesando solicitud para crear sala válida '" + idSala + "' por " + cliente.getInetAddress().getHostAddress());

       // --- Lógica de Creación de Sala ---

       // Verificar si la sala ya existe. Correcto.
       if (blackboard.existeSala(idSala)) {
           System.out.println("CREAR_SALA_KS: Sala '" + idSala + "' ya existe. No se creó.");
           enviarRespuestaError(cliente, "La sala '" + idSala + "' ya existe.");
           return;
       }

       // Crear datos de la nueva sala. Correcto.
       Map<String, Object> datosSala = new HashMap<>();
       List<Socket> jugadores = new ArrayList<>();
       jugadores.add(cliente); // Correcto: Añadir host a la lista.

       datosSala.put("host", cliente); // Guardar host.
       datosSala.put("jugadores", jugadores); // Guardar lista con 1 jugador.
       datosSala.put("estado", "ESPERANDO"); // Estado inicial.
       datosSala.put("maxJugadores", 2);     // Capacidad.

       // Log del estado inicial. Correcto.
       System.out.println("CREAR_SALA_KS: Preparando datos para sala '" + idSala + "'. Jugadores iniciales: "
                          + jugadores.size() + " [" + cliente.getInetAddress().getHostAddress() + "]");

       // Agregar la sala al Blackboard. Correcto.
       blackboard.agregarSala(idSala, datosSala);

       // Enviar respuesta de éxito al cliente. Correcto.
       enviarRespuesta(cliente, "SALA_CREADA_OK", Map.of("mensaje", "Sala '" + idSala + "' creada.", "idSala", idSala));

       // Notificar al Controller (maneja el caso null internamente, pero es mejor evitarlo con el constructor único). Correcto.
       if (controller != null) {
           controller.notificarCambio("NUEVA_SALA;" + idSala);
       } else {
            System.out.println("CREAR_SALA_KS WARN: Controller es null, no se puede notificar cambio NUEVA_SALA.");
       }

       // Indicar finalización al blackboard. Correcto.
       blackboard.respuestaFuenteC(cliente, evento);
    }

    // --- Métodos de ayuda para enviar respuestas ---
    // Estos métodos lucen correctos. Verifican si 'server' es null antes de usarlo.
    private void enviarRespuestaError(Socket cliente, String mensajeError) {
        Evento respuesta = new Evento("ERROR_CREAR_SALA");
        respuesta.agregarDato("error", mensajeError);
        if (server != null) {
             server.enviarEventoACliente(cliente, respuesta);
        } else {
             System.err.println("CREAR_SALA_KS ERROR: Referencia a IServer es null. No se puede enviar respuesta de error.");
        }
    }

    private void enviarRespuesta(Socket cliente, String tipoRespuesta, Map<String, Object> datos) {
        Evento respuesta = new Evento(tipoRespuesta);
        if (datos != null) {
            datos.forEach(respuesta::agregarDato);
        }
        if (server != null) {
            server.enviarEventoACliente(cliente, respuesta);
        } else {
            System.err.println("CREAR_SALA_KS ERROR: Referencia a IServer es null. No se puede enviar respuesta.");
        }
    }
}