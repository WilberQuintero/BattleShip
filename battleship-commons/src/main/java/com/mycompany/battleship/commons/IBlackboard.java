/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.battleship.commons;


/**
 *
 * @author caarl
 */
import dto.JugadorDTO;
import java.net.Socket;
import java.util.List; // Necesario para getClientesConectados
import java.util.Map;  // Necesario para los métodos de sala

/**
 * Interfaz que define el contrato público del BlackBoard.
 * Expone los métodos necesarios para que otros componentes (Server, KS, Controller)
 * interactúen con él sin conocer la implementación concreta.
 */
public interface IBlackboard {

    // --- Entrada de Eventos ---
    void enviarEventoBlackBoard(Socket cliente, Evento evento);

    // --- Gestión de Salas ---
    boolean existeSala(String idSala);
    void agregarSala(String id, Map<String, Object> datosSala);
    Map<String, Object> getDatosSala(String idSala);
    void actualizarDatosSala(String idSala, Map<String, Object> nuevosDatosSala);

    // --- Gestión de Clientes/Usuarios ---
    void agregarClienteSocket(Socket clienteSocket);
    void removerClienteSocket(Socket clienteSocket); // Maneja limpieza interna de mapas
    List<Socket> getClientesConectados();

    /**
     * Verifica si un nombre de usuario ya está registrado en el sistema.
     * Necesario para evitar nombres duplicados.
     * @param nombre El nombre a verificar.
     * @return true si el nombre ya está en uso, false en caso contrario.
     */
    boolean isNombreEnUso(String nombre); // <-- NUEVO

    /**
     * Registra la asociación entre un socket de cliente y un nombre de usuario.
     * Debe ser llamado por la KS después de validar el nombre.
     * @param cliente El socket del cliente.
     * @param nombre El nombre de usuario validado.
     */
     void registrarUsuario(Socket cliente, JugadorDTO jugadorDTO); // <-- NUEVO
JugadorDTO getJugadorDTO(Socket cliente); 
    /**
     * Obtiene el nombre de usuario asociado a un socket específico.
     * @param cliente El socket del cliente.
     * @return El nombre de usuario, o null si el socket no está registrado con un nombre.
     */
    String getNombreDeUsuario(Socket cliente); // <-- NUEVO (Útil)

    /**
     * Obtiene el socket asociado a un nombre de usuario específico.
     * @param nombre El nombre de usuario.
     * @return El Socket, o null si el nombre no está registrado.
     */
    Socket getSocketDeUsuario(String nombre); // <-- NUEVO (Útil)


    // --- Callback ---
    void respuestaFuenteC(Socket cliente, Evento eventoRespuesta);

}