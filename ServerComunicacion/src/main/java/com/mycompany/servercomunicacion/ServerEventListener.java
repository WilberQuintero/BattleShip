/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.servercomunicacion;

/**
 *
 * @author caarl
 */
import java.util.Map;

/**
 * Interfaz para notificar a la capa superior (Vista/Controlador del cliente)
 * sobre eventos asíncronos o datos recibidos desde la capa de comunicación
 * (ServerComunicacion) que interactúa con el servidor backend.
 *
 * La clase que maneja la lógica principal de la UI del cliente (generalmente
 * el Controlador en un patrón MVC) implementará esta interfaz.
 */
public interface ServerEventListener {

    /**
     * Llamado por ServerComunicacion cuando la conexión con el servidor
     * se ha establecido exitosamente.
     */
    void onConectado();

    /**
     * Llamado por ServerComunicacion cuando la conexión con el servidor
     * se ha perdido o cerrado (ya sea intencionalmente o por un error).
     * @param motivo Una breve descripción de por qué ocurrió la desconexión
     * (ej. "Error de red", "Servidor desconectado", "Desconexión manual").
     */
    void onDesconectado(String motivo);

    /**
     * Llamado por ServerComunicacion cuando ocurre un error general
     * relacionado con la comunicación (ej. no se pudo conectar, error al enviar/recibir)
     * o cuando el servidor envía un mensaje de error específico.
     * @param mensajeError Descripción del error ocurrido.
     */
    void onError(String mensajeError);

    /**
     * Método genérico llamado por ServerComunicacion cuando se recibe un mensaje
     * o evento estructurado del servidor que no tiene un método de callback dedicado.
     * La implementación de este método deberá inspeccionar el 'tipo' para determinar
     * cómo procesar los 'datos'.
     *
     * @param tipo Un String que identifica el tipo de mensaje/evento recibido del servidor
     * (ej. "SALA_CREADA_OK", "ESTADO_JUEGO", "TURNO_CONTRARIO", "CHAT_RECIBIDO", etc.).
     * @param datos Un Map que contiene los datos asociados a ese evento/mensaje,
     * donde las claves son Strings y los valores pueden ser de distintos tipos
     * (principalmente Strings si se parsea desde texto simple, pero podrían
     * ser otros si se usa una serialización más compleja como JSON).
     */
    void onMensajeServidor(String tipo, Map<String, Object> datos);

    /*
     * --- Alternativa / Complemento a onMensajeServidor ---
     * Si prefieres tener métodos más específicos para eventos comunes,
     * podrías descomentar o añadir métodos como los siguientes, y en
     * ServerComunicacion.procesarMensajeServidor llamarías a estos métodos
     * específicos en lugar del genérico onMensajeServidor para esos tipos.

    void onSalaCreada(String idSala, String mensaje);
    void onUnionSala(boolean exito, String idSala, String mensaje);
    void onListaSalasRecibida(List<SalaInfoDTO> salas); // Necesitarías SalaInfoDTO
    void onOponenteEncontrado(String idSala, String nombreOponente);
    void onPartidaIniciada(String idSala, TableroDTO miTablero, boolean esMiTurno); // Necesitarías TableroDTO
    void onResultadoDisparo(int x, int y, String resultado, boolean sigueMiTurno); // resultado="AGUA", "IMPACTO", "HUNDIDO:Portaaviones"
    void onDisparoRecibido(int x, int y); // Cuando el oponente dispara
    void onActualizacionTableroOponente(int x, int y, String resultado); // Para marcar en tu segundo tablero
    void onJuegoTerminado(boolean ganaste, String motivo);
    void onChatRecibido(String remitente, String mensaje);

     * La elección entre el método genérico y los específicos depende de tu preferencia
     * y la complejidad de la comunicación. El genérico es más flexible inicialmente,
     * los específicos pueden ser más claros para eventos muy definidos. Puedes usar una mezcla.
     */

}