/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package handlers;

/**
 *
 * @author caarl
 */
import com.mycompany.battleship.commons.Evento;
import com.mycompany.battleship.commons.IServer;
import com.mycompany.blackboard.Controller;

import java.net.Socket;
import com.mycompany.blackboard.IHandler;
import com.mycompany.battleship.commons.IHandlerCommons;




/**
 * Knowledge Source encargada de procesar el registro de un nombre de usuario
 * para un cliente recién conectado.
 */
public class RegistrarUsuarioHandler implements IHandler { // O IHandler

    private final IHandlerCommons handlerCommons;
    private final IServer server;
    private final Controller controller;

    public RegistrarUsuarioHandler(IHandlerCommons blackboard, IServer server, Controller controller) {
        // Verificar nulos si es necesario
        this.handlerCommons = blackboard;
        this.server = server;
        this.controller = controller;
    }

    @Override
    public boolean puedeProcesar(Evento evento) {
        return evento != null && "REGISTRAR_USUARIO".equalsIgnoreCase(evento.getTipo());
    }

    @Override
    public void procesarEvento(Socket cliente, Evento evento) {
        if (cliente == null || evento == null) {
            System.err.println("REGISTRAR_USUARIO_KS: Cliente o Evento nulo.");
            return;
        }

        // 1. Obtener y validar nombre
        Object nombreObj = evento.obtenerDato("nombre");
        String nombre = null;
        if (nombreObj instanceof String) {
            nombre = ((String) nombreObj).trim();
        }

        if (nombre == null || nombre.isBlank()) {
            System.err.println("REGISTRAR_USUARIO_KS: Nombre de usuario inválido recibido: '" + nombreObj + "'");
            enviarError(cliente, "Nombre de usuario inválido.");
            // Considerar si se debe desconectar al cliente por enviar datos inválidos
            return;
        }

        System.out.println("REGISTRAR_USUARIO_KS: Procesando registro para nombre '" + nombre + "' desde socket " + cliente.getInetAddress().getHostAddress());

        // 2. Verificar si el nombre ya está en uso por OTRO socket
        // Usamos la interfaz IHandlerCommons para interactuar
        if (handlerCommons.isNombreEnUso(nombre)) {
            // Podríamos verificar si el socket asociado a ese nombre es el mismo que el actual,
            // lo que significaría un re-registro, pero por ahora lo tratamos como error.
             Socket socketExistente = handlerCommons.getSocketDeUsuario(nombre);
             if (socketExistente != null && !socketExistente.equals(cliente)) {
                 System.err.println("REGISTRAR_USUARIO_KS: Nombre '" + nombre + "' ya está en uso por otro cliente.");
                 enviarError(cliente, "El nombre de usuario '" + nombre + "' ya está en uso.");
                 return;
             } else if (socketExistente != null && socketExistente.equals(cliente)) {
                  System.out.println("REGISTRAR_USUARIO_KS: El cliente ya está registrado con este nombre. Permitiendo (o ignorando).");
                  // Podríamos simplemente enviar OK o ignorar si ya está registrado con ese nombre.
                  // Por ahora, continuaremos y sobrescribiremos por si acaso.
             } else {
                  // Caso raro: isNombreEnUso es true pero getSocketDeUsuario es null? Improbable con ConcurrentHashMap
                  System.err.println("REGISTRAR_USUARIO_KS: Inconsistencia detectada para nombre '" + nombre + "'.");
                  enviarError(cliente, "Error interno al verificar nombre.");
                  return;
             }
        }
        
        

        // 3. Verificar si el SOCKET ya tiene OTRO nombre (opcional, pero informativo)
        String nombrePrevioSocket = handlerCommons.getNombreDeUsuario(cliente);
        if (nombrePrevioSocket != null && !nombrePrevioSocket.equals(nombre)) {
             System.out.println("REGISTRAR_USUARIO_KS WARN: Socket " + cliente.getInetAddress().getHostAddress() +
                                " tenía nombre previo '" + nombrePrevioSocket + "', se sobrescribirá con '" + nombre + "'.");
             // No enviamos error, permitimos el cambio de nombre implícito aquí
        }

System.out.println("-----------------------------------------------------");
        System.out.println("REGISTRAR_USUARIO_KS: Preparando para registrar en Blackboard:");
        System.out.println("  -> Socket: " + (cliente != null ? cliente.getInetAddress().getHostAddress() + ":" + cliente.getPort() : "NULL"));
        System.out.println("  -> Nombre: '" + nombre + "'");
        System.out.println("  -> Tablero: null (valor inicial)");
        System.out.println("  -> EnTurno: false (valor inicial)"); 
        System.out.println("-----------------------------------------------------");
        
        
        // 4. Registrar en Blackboard
        System.out.println("REGISTRAR_USUARIO_KS: Registrando usuario '" + nombre + "' para socket " + cliente.getInetAddress().getHostAddress());
        handlerCommons.registrarUsuario(cliente, nombre); // Llama al método del blackboard

        // 5. Enviar confirmación al cliente
        Evento respuestaOk = new Evento("REGISTRO_OK");
        respuestaOk.agregarDato("nombre", nombre); // Devolver el nombre registrado
        respuestaOk.agregarDato("mensaje", "Usuario registrado exitosamente.");
        server.enviarEventoACliente(cliente, respuestaOk);
        System.out.println("REGISTRAR_USUARIO_KS: Confirmación REGISTRO_OK enviada a '" + nombre + "'.");

        // 6. Notificar al Controller (backend)
        if (controller != null) {
            controller.notificarCambio("USUARIO_REGISTRADO;" + nombre); // Incluir nombre en notificación
        }

        // 7. Informar al Blackboard sobre finalización
        handlerCommons.respuestaFuenteC(cliente, evento); // O usar respuestaOk
    }

    // Método helper para enviar errores específicos de este KS
    private void enviarError(Socket cliente, String mensajeError) {
        Evento respuesta = new Evento("ERROR_REGISTRO");
        respuesta.agregarDato("error", mensajeError);
        if (server != null) {
            server.enviarEventoACliente(cliente, respuesta);
        } else {
            System.err.println("REGISTRAR_USUARIO_KS ERROR: IServer es null. No se puede enviar error.");
        }
    }
}