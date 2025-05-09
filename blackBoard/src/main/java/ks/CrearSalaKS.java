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

import dto.JugadorDTO;
import dto.TableroFlotaDTO;
import dto.TableroSeguimientoDTO;
import dto.PartidaDTO; // Asumiendo que tienes un PartidaDTO
import enums.EstadoPartida; // Asumiendo que tienes un enum para esto

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID; // Para generar un ID de partida si es necesario
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
    public void procesarEvento(Socket clienteHost, Evento evento) {
        if (evento == null || clienteHost == null) { /* ... error ... */ return; }

        String idSala = (String) evento.obtenerDato("idSala");
        if (idSala == null || idSala.isBlank()) {
            enviarRespuestaError(clienteHost, "Nombre de sala no válido.");
            return;
        }
        idSala = idSala.trim();

        System.out.println("CREAR_SALA_KS: Procesando para crear sala '" + idSala + "' por " + clienteHost.getInetAddress().getHostAddress());

        if (blackboard.existeSala(idSala)) {
            enviarRespuestaError(clienteHost, "La sala '" + idSala + "' ya existe.");
            return;
        }

        // 1. Obtener el JugadorDTO del anfitrión
        JugadorDTO anfitrionDTO = blackboard.getJugadorDTO(clienteHost);
        if (anfitrionDTO == null) {
            enviarRespuestaError(clienteHost, "Error: Usuario no registrado. No se puede crear sala.");
            return;
        }
        System.out.println("CREAR_SALA_KS: Anfitrión es '" + anfitrionDTO.getNombre() + "'.");

        // 2. (Opcional, pero buena práctica) Asignar/Reasignar tableros DTO vacíos al anfitriónDTO
        // Esto asegura que cualquier estado previo de tableros de otra partida no se mezcle.
        // Si el JugadorDTO ya se crea con tableros vacíos al registrarse, esto es solo para reafirmar.
        int dimensionTablero = 10; // O tu dimensión estándar
        if (anfitrionDTO.getTableroFlota() == null) {
            TableroFlotaDTO tfdto = new TableroFlotaDTO();
            tfdto.setDimension(dimensionTablero);
            anfitrionDTO.setTableroFlota(tfdto);
        }
        if (anfitrionDTO.getTableroSeguimiento() == null) {
            TableroSeguimientoDTO tsdto = new TableroSeguimientoDTO();
            tsdto.setDimension(dimensionTablero);
            anfitrionDTO.setTableroSeguimiento(tsdto);
        }
        anfitrionDTO.setHaConfirmadoTablero(false); // Aún no ha colocado barcos para esta nueva partida

        // 3. Crear el PartidaDTO
        PartidaDTO nuevaPartida = new PartidaDTO(idSala, anfitrionDTO, EstadoPartida.ESPERANDO_OPONENTE);
        // El constructor de PartidaDTO debería inicializar jugador2 a null y nombreJugadorEnTurno a null.

        System.out.println("CREAR_SALA_KS: PartidaDTO creada: " + nuevaPartida.toString());

        // 4. Agregar la PartidaDTO al Blackboard usando el nuevo método
        if (blackboard.agregarPartida(nuevaPartida)) {
            System.out.println("CREAR_SALA_KS: Partida '" + idSala + "' con anfitrión '" + anfitrionDTO.getNombre() + "' agregada al Blackboard.");

            // Enviar respuesta de éxito al cliente
            enviarRespuesta(clienteHost, "SALA_CREADA_OK", Map.of(
                "mensaje", "Sala '" + idSala + "' creada. Esperando oponente.",
                "idSala", idSala,
                "tuNombre", anfitrionDTO.getNombre()
            ));

            if (controller != null) {
                controller.notificarCambio("NUEVA_SALA_CREADA;" + idSala + ";anfitrion=" + anfitrionDTO.getNombre());
            }
        } else {
            // Esto no debería pasar si existeSala() funcionó, pero es un buen control.
            System.err.println("CREAR_SALA_KS: Falló agregarPartida al Blackboard, la sala podría haber sido creada concurrentemente.");
            enviarRespuestaError(clienteHost, "Error interno al crear la sala. Intenta de nuevo.");
        }

        blackboard.respuestaFuenteC(clienteHost, evento);
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