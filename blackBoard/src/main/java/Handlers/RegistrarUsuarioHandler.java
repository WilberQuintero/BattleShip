/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Handlers;
import dto.JugadorDTO; // Importa tu JugadorDTO
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

    private final IHandlerCommons blackboard;
    private final IServer server;
    private final Controller controller;

    public RegistrarUsuarioHandler(IHandlerCommons blackboard, IServer server, Controller controller) {
        // Verificar nulos si es necesario
        this.blackboard = blackboard;
        this.server = server;
        this.controller = controller;
    }

    @Override
    public boolean puedeProcesar(Evento evento) {
        return evento != null && "REGISTRAR_USUARIO".equalsIgnoreCase(evento.getTipo());
    }

    @Override
    public void procesarEvento(Socket cliente, Evento evento) {
        if (cliente == null || evento == null) { /* ... manejo error ... */ return; }

        // 1. Obtener y validar nombre
        Object nombreObj = evento.obtenerDato("nombre");
        String nombre = null;
        if (nombreObj instanceof String) {
            nombre = ((String) nombreObj).trim();
        }
        if (nombre == null || nombre.isBlank()) {
            enviarError(cliente, "Nombre de usuario inválido.");
            return;
        }

        System.out.println("RegistrarUsuarioHandler: Procesando registro para nombre '" + nombre + "'...");

        // 2. Verificar si el nombre ya está en uso por OTRO socket
        if (blackboard.isNombreEnUso(nombre)) {
            Socket socketExistente = blackboard.getSocketDeUsuario(nombre);
            if (socketExistente != null && !socketExistente.equals(cliente)) {
                enviarError(cliente, "El nombre de usuario '" + nombre + "' ya está en uso.");
                return;
            }
            // Si es el mismo socket, permite continuar (re-registro)
        }

        // 3. Crear el objeto JugadorDTO
        JugadorDTO nuevoJugadorDTO;
        try {
            nuevoJugadorDTO = new JugadorDTO(nombre);
            // Inicializar otros campos del DTO si es necesario aquí
            // nuevoJugadorDTO.setListoParaJugar(false); // Por defecto ya es false
            // Los tableros se asignarán más adelante cuando se configuren
            System.out.println("RegistrarUsuarioHandler: JugadorDTO creado: " + nuevoJugadorDTO.toString());
        } catch (Exception e) { // Captura errores de creación si los hubiera
            System.err.println("RegistrarUsuarioHandler: Error creando JugadorDTO: " + e.getMessage());
            enviarError(cliente, "Error interno al procesar registro.");
            return;
        }
        
        // (Opcional) Verificar si el SOCKET tenía otro JugadorDTO asociado
        JugadorDTO jugadorPrevioDTO = blackboard.getJugadorDTO(cliente); // Usa el nuevo método
        if(jugadorPrevioDTO != null && !jugadorPrevioDTO.getNombre().equals(nombre)) {
             System.out.println("RegistrarUsuarioHandler WARN: Socket tenía JugadorDTO previo '" + jugadorPrevioDTO.getNombre() + "', se sobrescribirá.");
        }

        // 4. Registrar en Blackboard (pasando el DTO)
        System.out.println("RegistrarUsuarioHandler: Registrando JugadorDTO '" + nombre + "' en Blackboard...");
        blackboard.registrarUsuario(cliente, nuevoJugadorDTO); // Llama al método MODIFICADO

        // 5. Enviar confirmación al cliente (solo el nombre es suficiente por ahora)
        Evento respuestaOk = new Evento("REGISTRO_OK");
        respuestaOk.agregarDato("nombre", nuevoJugadorDTO.getNombre());
        respuestaOk.agregarDato("mensaje", "Usuario '" + nombre + "' registrado.");
        server.enviarEventoACliente(cliente, respuestaOk);
        System.out.println("RegistrarUsuarioHandler: Confirmación REGISTRO_OK enviada a '" + nombre + "'.");

        // 6. Notificar al Controller (backend), si aplica
        // ...

        // 7. Informar al Blackboard sobre finalización
        blackboard.respuestaFuenteC(cliente, evento);
    }
    // Método helper para enviar errores específicos de este KS
    private void enviarError(Socket cliente, String mensajeError) {
        Evento respuesta = new Evento("ERROR_REGISTRO");
        respuesta.agregarDato("error", mensajeError);
        if (server != null) {
            server.enviarEventoACliente(cliente, respuesta);
        } else {
            System.err.println("RegistrarUsuarioHandler ERROR: IServer es null. No se puede enviar error.");
        }
    }
}