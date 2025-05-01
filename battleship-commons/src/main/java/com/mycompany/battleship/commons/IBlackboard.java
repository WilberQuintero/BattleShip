/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.battleship.commons;


/**
 *
 * @author caarl
 */
import java.net.Socket;
import java.util.List; // Necesario para getClientesConectados
import java.util.Map;  // Necesario para los métodos de sala

/**
 * Interfaz que define el contrato público del BlackBoard.
 * Expone los métodos necesarios para que otros componentes (Server, KS, Controller)
 * interactúen con él sin conocer la implementación concreta.
 */
public interface IBlackboard {

    // --- Métodos de Interacción Principal (Entrada de Eventos) ---

    /**
     * Método principal que el Server (u otros) usa para enviar un evento
     * al Blackboard para su procesamiento por las Knowledge Sources.
     * @param cliente El socket asociado al origen del evento (puede ser null).
     * @param evento El evento a procesar.
     */
    void enviarEventoBlackBoard(Socket cliente, Evento evento);


    // --- Métodos para Gestión de Salas ---

    /**
     * Verifica si una sala con el ID especificado existe en el Blackboard.
     * @param idSala El ID de la sala a verificar.
     * @return true si la sala existe, false en caso contrario.
     */
    boolean existeSala(String idSala);

    /**
     * Registra una nueva sala en el Blackboard.
     * Llamado típicamente por una CrearSalaKS.
     * @param id El ID único para la nueva sala.
     * @param datosSala Un mapa que contiene la información inicial de la sala (ej. host, lista de jugadores vacía).
     */
    void agregarSala(String id, Map<String, Object> datosSala);

    /**
     * Obtiene los datos asociados a una sala específica.
     * Devuelve una copia o una vista inmutable para proteger el estado interno.
     * @param idSala El ID de la sala a consultar.
     * @return Un Map con los datos de la sala, o null si la sala no existe.
     */
    Map<String, Object> getDatosSala(String idSala); // <-- MÉTODO NUEVO

    /**
     * Actualiza (sobrescribe) los datos de una sala existente en el Blackboard.
     * Llamado por KSs después de modificar el estado de una sala (ej. añadir jugador).
     * @param idSala El ID de la sala a actualizar.
     * @param nuevosDatosSala El Map con los datos actualizados de la sala.
     */
    void actualizarDatosSala(String idSala, Map<String, Object> nuevosDatosSala); // <-- MÉTODO NUEVO


    // --- Métodos para Gestión de Clientes/Jugadores (Estado General) ---

    /**
     * Añade un socket a la lista interna de clientes cuya conexión fue
     * validada y procesada por la KS correspondiente.
     * ¡Nota: Generalmente llamado DESDE la KS, no HACIA la KS! Podría
     * considerarse un detalle de implementación y no ir en la interfaz,
     * pero lo incluimos si otras KS necesitan llamar a esto explícitamente.
     * Si solo ConnectionKS lo llama, podría quitarse de la interfaz.
     * @param clienteSocket El socket del cliente a registrar como activo.
     */
    void agregarClienteSocket(Socket clienteSocket);

     /**
      * Elimina un socket de la lista interna de clientes activos.
      * Útil para KS que manejan desconexiones.
      * @param clienteSocket El socket a eliminar.
      */
     void removerClienteSocket(Socket clienteSocket); // <-- MÉTODO NUEVO (para KS de desconexión)

    /**
     * Obtiene una lista (preferiblemente una copia o inmutable) de los sockets
     * de los clientes actualmente considerados activos/conectados por el Blackboard.
     * @return Una lista de Sockets.
     */
    List<Socket> getClientesConectados(); // <-- MÉTODO NUEVO (útil para broadcasting, etc.)


    // --- Métodos de Callback (Opcional en interfaz) ---

    /**
     * Método de callback (estilo Dominó) que una KS puede llamar para
     * señalar que ha terminado de procesar un evento. Su utilidad en la
     * interfaz pública es debatible, podría ser un método interno.
     * @param cliente Socket asociado.
     * @param eventoRespuesta Evento que representa la respuesta o finalización.
     */
    void respuestaFuenteC(Socket cliente, Evento eventoRespuesta);


    // --- NO incluimos getServer() ni getController() ---
    // porque esas dependencias se inyectan directamente en las KS
    // durante su construcción por el BlackBoard concreto, no necesitan
    // pasar a través de la interfaz IBlackboard para obtenerlas.

}
