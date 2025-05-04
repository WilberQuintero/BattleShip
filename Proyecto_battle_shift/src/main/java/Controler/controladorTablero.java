/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controler;


import com.mycompany.servercomunicacion.ServerComunicacion;

/**
 *
 * @author caarl
 */

import View.TableroJuego; // La vista que maneja
import java.util.Map; // Para procesar respuestas del servidor


/**
 * Controlador para la pantalla principal del juego (Tablero).
 * Maneja la colocación de barcos y la lógica de disparos.
 */
public class controladorTablero {

    private final ServerComunicacion serverComunicacion;
    private final String idSala;
    private final String miNombreUsuario;
    private TableroJuego vistaTablero;
    // Podrías tener aquí una representación lógica del tablero del jugador
    // private Model.Tablero miTableroLogico;

    public controladorTablero(ServerComunicacion serverComunicacion, String idSala, String miNombre, TableroJuego vista) {
        System.out.println("CONTROLLER [Tablero]: Inicializando para sala " + idSala + ", jugador " + miNombre);
        if (serverComunicacion == null || idSala == null || miNombre == null || vista == null) {
            throw new IllegalArgumentException("Dependencias nulas para controladorTablero.");
        }
        this.serverComunicacion = serverComunicacion;
        this.idSala = idSala;
        this.miNombreUsuario = miNombre;
        this.vistaTablero = vista;
        // this.miTableroLogico = new Model.Tablero(); // Inicializar tablero lógico
        System.out.println("CONTROLLER [Tablero]: Controlador inicializado y vista asignada.");
    }

    // --- Métodos llamados por la Vista (TableroJuego) ---

    /**
     * Llamado cuando el jugador confirma que ha terminado de colocar sus barcos.
     * @param tableroJson Representación JSON del tablero con los barcos colocados.
     */
//    public void enviarColocacionLista(String tableroJson) {
//        System.out.println("CONTROLLER [Tablero]: Colocación lista. Enviando tablero JSON para sala " + idSala);
//        if (!serverComunicacion.isConectado()) {
//            if(vistaTablero != null) vistaTablero.mostrarError("Error: No conectado al servidor.");
//            return;
//        }
//        if (tableroJson == null || tableroJson.isBlank()) {
//             System.err.println("CONTROLLER [Tablero]: Error - Intento de enviar tablero JSON vacío.");
//             if(vistaTablero != null) vistaTablero.mostrarError("Error interno al generar la disposición de barcos.");
//             return;
//        }
//
//        // Crear y enviar evento COLOCACION_LISTA
//        String mensaje = String.format("EVENTO;TIPO=COLOCACION_LISTA;idSala=%s;tableroJson=%s",
//                                      idSala, tableroJson);
//        serverComunicacion.enviarEventoListo(mensaje);
//        System.out.println("CONTROLLER [Tablero]: Evento COLOCACION_LISTA enviado.");
//        // La vista debería mostrar "Esperando al oponente..."
//    }
//
//    /**
//     * Llamado cuando el jugador hace clic en una celda para disparar.
//     * @param x Coordenada X
//     * @param y Coordenada Y
//     */
//    public void realizarDisparo(int x, int y) {
//        System.out.println("CONTROLLER [Tablero]: Solicitud de disparo a [" + x + "," + y + "] en sala " + idSala);
//        if (!serverComunicacion.isConectado()) {
//             if(vistaTablero != null) vistaTablero.mostrarError("Error: No conectado al servidor.");
//            return;
//        }
//        // Crear y enviar evento DISPARAR
//         String mensaje = String.format("EVENTO;TIPO=DISPARAR;idSala=%s;x=%d;y=%d",
//                                      idSala, x, y);
//        serverComunicacion.enviarEventoListo(mensaje);
//         System.out.println("CONTROLLER [Tablero]: Evento DISPARAR enviado.");
//         // La vista debería deshabilitar el tablero enemigo hasta recibir respuesta
//    }
//
//    /**
//     * Llamado si el jugador decide abandonar la partida en curso.
//     */
//    public void salirDePartida() {
//         System.out.println("CONTROLLER [Tablero]: Jugador saliendo de partida en sala " + idSala);
//         if (!serverComunicacion.isConectado()) { return; } // Salir silenciosamente si no está conectado
//         // Crear y enviar evento ABANDONAR_PARTIDA
//          String mensaje = String.format("EVENTO;TIPO=ABANDONAR_PARTIDA;idSala=%s", idSala);
//          serverComunicacion.enviarEventoListo(mensaje);
//          System.out.println("CONTROLLER [Tablero]: Evento ABANDONAR_PARTIDA enviado.");
//          // La vista se cerrará o mostrará un mensaje. El servidor notificará al otro jugador.
//    }
//
//
//    // --- Métodos llamados por controladorInicio para DELEGAR respuestas ---
//
//    /**
//     * Procesa la respuesta a un evento de colocación lista (ej. si fue válida o no).
//     * @param datos Datos recibidos del servidor.
//     */
//    public void procesarRespuestaColocacion(Map<String, Object> datos) {
//        System.out.println("CONTROLLER [Tablero]: Procesando respuesta COLOCACION_LISTA: " + datos);
//        if (vistaTablero == null) return;
//        // Ejemplo: El servidor podría enviar un TIPO="ERROR_COLOCACION" o "COLOCACION_RECIBIDA"
//        // String mensaje = (String) datos.getOrDefault("mensaje", "Esperando oponente...");
//        // boolean error = "true".equalsIgnoreCase(String.valueOf(datos.getOrDefault("error", "false")));
//        // vistaTablero.mostrarMensaje(mensaje, error);
//    }
//
//    /**
//     * Procesa la respuesta a un disparo realizado.
//     * @param datos Datos como {x:int, y:int, resultado:"AGUA"|"IMPACTO"|"HUNDIDO:Crucero", turno:boolean}
//     */
//    public void procesarResultadoDisparo(Map<String, Object> datos) {
//         System.out.println("CONTROLLER [Tablero]: Procesando resultado de disparo: " + datos);
//         if (vistaTablero == null) return;
//         // TODO: Extraer x, y, resultado, esMiTurno de 'datos'
//         // int x = Integer.parseInt(String.valueOf(datos.get("x")));
//         // String resultado = (String) datos.get("resultado");
//         // boolean miTurno = "true".equalsIgnoreCase(String.valueOf(datos.get("turno")));
//         // vistaTablero.actualizarTableroEnemigo(x, y, resultado); // Actualiza UI del tablero enemigo
//         // vistaTablero.establecerTurno(miTurno); // Habilita/deshabilita tablero enemigo
//    }
//
//     /**
//     * Procesa un disparo recibido del oponente.
//     * @param datos Datos como {x:int, y:int}
//     */
//    public void procesarDisparoOponente(Map<String, Object> datos) {
//         System.out.println("CONTROLLER [Tablero]: Procesando disparo oponente: " + datos);
//         if (vistaTablero == null) return;
//         // TODO: Extraer x, y de 'datos'
//         // int x = ...; int y = ...;
//         // TODO: Verificar en miTableroLogico si fue AGUA, IMPACTO o HUNDIDO
//         // String resultado = miTableroLogico.verificarDisparo(x, y);
//         // TODO: Enviar respuesta al servidor EVENTO;TIPO=RESPUESTA_DISPARO;idSala=...;x=...;y=...;resultado=...
//         // serverComunicacion.enviarRespuestaDisparo(idSala, x, y, resultado);
//         // TODO: Actualizar mi propio tablero en la UI
//         // vistaTablero.actualizarMiTablero(x, y, resultado);
//         // TODO: Indicar en la UI que es el turno del oponente
//         // vistaTablero.establecerTurno(false);
//    }

    /**
     * Procesa el evento de fin de juego.
     * @param datos Datos como {ganador:boolean, motivo:String}
     */
    public void procesarFinDeJuego(Map<String, Object> datos) {
        System.out.println("CONTROLLER [Tablero]: Procesando fin de juego: " + datos);
        if (vistaTablero == null) return;
        // TODO: Extraer datos
        // boolean ganaste = "true".equalsIgnoreCase(String.valueOf(datos.getOrDefault("ganador", "false")));
        // String motivo = (String) datos.getOrDefault("motivo", "");
        // vistaTablero.mostrarFinDeJuego(ganaste, motivo); // Muestra resultado y opción de salir/revancha
    }

     /**
     * Procesa un error específico de la partida en curso.
     * @param datos Contiene el mensaje de error.
     */
     public void procesarErrorPartida(Map<String, Object> datos) {
        System.err.println("CONTROLLER [Tablero]: Procesando error específico de partida: " + datos);
        if (vistaTablero == null) return;
        String errorMsg = (String) datos.getOrDefault("error", "Error desconocido en la partida.");
        vistaTablero.mostrarError(errorMsg); // Muestra el error en la pantalla de juego
        // Podría necesitar deshabilitar controles o forzar salida
    }

}