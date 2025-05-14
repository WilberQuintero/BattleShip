/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controler;

import Model.entidades.Partida;
import Model.entidades.Barco;
import Model.entidades.Jugador;
import Model.entidades.Posicion;
import Model.entidades.TableroFlota;
import Model.entidades.mappers.ModelConverter;
import com.mycompany.servercomunicacion.ServerComunicacion;

/**
 *
 * @author caarl
 */

import View.TableroJuego; // La vista que maneja
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.TableroFlotaDTO;
import enums.Orientacion;
import enums.TipoNave;
import java.util.List;
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
    private controladorInicio ctrlInicioRef; // Referencia a controladorInicio


   public controladorTablero(ServerComunicacion serverComunicacion, String idSala, String miNombre, TableroJuego vista) {
        System.out.println("CONTROLLER [Tablero]: Inicializando para sala " + idSala + ", jugador " + miNombre);
        if (serverComunicacion == null || idSala == null || miNombre == null || vista == null) {
            throw new IllegalArgumentException("Dependencias nulas para controladorTablero: serverCom, idSala, miNombre o vista.");
        }
        this.serverComunicacion = serverComunicacion;
        this.idSala = idSala;
        this.miNombreUsuario = miNombre;
        this.vistaTablero = vista;

        if (this.vistaTablero.getControladorPrincipal() != null) {
            this.ctrlInicioRef = this.vistaTablero.getControladorPrincipal();
            System.out.println("CONTROLLER [Tablero]: Referencia a controladorInicio obtenida correctamente.");
        } else {
            System.err.println("CONTROLLER [Tablero] ERROR CRÍTICO: No se pudo obtener referencia a controladorInicio desde la vistaTablero.");
            // Considera un manejo de error más robusto si esto es un estado inválido
        }
    }
   
   
   /**
     * Obtiene el objeto Jugador actual desde controladorInicio a través de la Partida.
     */
    private Jugador obtenerJugadorActual() {
        if (this.ctrlInicioRef == null) {
            System.err.println("CONTROLLER [Tablero] ERROR (obtenerJugadorActual): ctrlInicioRef es nulo.");
            return null;
        }
        Partida partidaActual = this.ctrlInicioRef.getPartidaActual(this.idSala);
        if (partidaActual == null) {
            System.err.println("CONTROLLER [Tablero] ERROR (obtenerJugadorActual): No se encontró partida activa para la sala '" + this.idSala + "'.");
            return null;
        }
        
        Jugador jugador = null;
        if (partidaActual.getJugador1() != null && partidaActual.getJugador1().getNombre().equals(this.miNombreUsuario)) {
            jugador = partidaActual.getJugador1();
        } else if (partidaActual.getJugador2() != null && partidaActual.getJugador2().getNombre().equals(this.miNombreUsuario)) {
            jugador = partidaActual.getJugador2();
        }

        if (jugador == null) {
             System.err.println("CONTROLLER [Tablero] ERROR (obtenerJugadorActual): Jugador '" + this.miNombreUsuario + "' no encontrado en partida '" + this.idSala + "'.");
             return null;
        }

        // Asegurar que el jugador tenga un TableroFlota
        if (jugador.getTableroFlota() == null) {
            System.out.println("CONTROLLER [Tablero] (obtenerJugadorActual): TableroFlota es null para " + this.miNombreUsuario + ". Creando uno nuevo.");
            // Usar la dimensión de la partida o una por defecto
            int dimension = (this.ctrlInicioRef != null && this.ctrlInicioRef.getPartidaActual(this.idSala) != null) ?
                            this.ctrlInicioRef.getPartidaActual(this.idSala).getDimensionTablero() : 10; // Asume 10 si no puede obtenerla
            jugador.setTableroFlota(new TableroFlota(dimension));
        }
        return jugador;
    }
    
    
     /**
     * NUEVO MÉTODO: Toma los datos de la UI (desde vistaTablero.getBarcosColocadosEnUI())
     * y actualiza el TableroFlota del Jugador en el modelo.
     */
    public void poblarModeloFlotaDesdeVista() {
        Jugador jugadorActual = obtenerJugadorActual();
        if (jugadorActual == null) {
            System.err.println("CONTROLLER [Tablero] ERROR (poblarModelo): No se pudo obtener el jugador actual.");
            if (vistaTablero != null) vistaTablero.mostrarError("Error interno: No se pudo guardar la flota.");
            return;
        }
        if (vistaTablero == null) {
            System.err.println("CONTROLLER [Tablero] ERROR (poblarModelo): vistaTablero es null.");
            return;
        }

        List<Map<String, Object>> barcosColocadosUI = vistaTablero.getBarcosColocadosEnUI();
        if (barcosColocadosUI == null) {
             System.err.println("CONTROLLER [Tablero] ERROR (poblarModelo): Lista de barcos de la UI es null.");
            if (vistaTablero != null) vistaTablero.mostrarError("Error: No se pudo obtener la información de los barcos colocados.");
            return;
        }

        // Crear un nuevo TableroFlota para el jugador para asegurar que esté limpio antes de poblar
        // Esto usa el setter que ya tienes en Jugador.java y la dimensión del tablero.
        int dimensionTablero = jugadorActual.getTableroFlota() != null ? jugadorActual.getTableroFlota().getDimension() : 10; // Mantener dimensión o default
        jugadorActual.setTableroFlota(new TableroFlota(dimensionTablero));
        TableroFlota tableroFlotaModelo = jugadorActual.getTableroFlota(); // Obtener la nueva referencia

        System.out.println("CONTROLLER [Tablero] (poblarModelo): Poblando TableroFlota del modelo para " + jugadorActual.getNombre() + ". Barcos desde UI: " + barcosColocadosUI.size());

        for (Map<String, Object> datosBarcoUI : barcosColocadosUI) {
            try {
                int fila = (Integer) datosBarcoUI.get("fila");
                int columna = (Integer) datosBarcoUI.get("columna");
                int tamanoLongitud = (Integer) datosBarcoUI.get("tamaño"); // Esta es la longitud del barco
                
                // Determinar orientación. Es crucial que el Map 'datosBarcoUI' contenga esta información.
                boolean esVertical = false; // Valor por defecto
                if (datosBarcoUI.containsKey("isRotado") && datosBarcoUI.get("isRotado") instanceof Boolean) {
                    esVertical = (Boolean) datosBarcoUI.get("isRotado");
                } else {
                    System.err.println("CONTROLLER [Tablero] (poblarModelo) ADVERTENCIA: Clave 'isRotado' no encontrada o no es booleana en datosBarcoUI para barco en ("+fila+","+columna+"). Asumiendo HORIZONTAL.");
                }
                Orientacion orientacionEnum = esVertical ? Orientacion.VERTICAL : Orientacion.HORIZONTAL;

                TipoNave tipoNaveEnum = null;
                for (TipoNave tn : TipoNave.values()) {
                    if (tn.getLongitud() == tamanoLongitud) {
                        tipoNaveEnum = tn;
                        break;
                    }
                }

                if (tipoNaveEnum == null) {
                    System.err.println("CONTROLLER [Tablero] (poblarModelo): No se encontró TipoNave para longitud " + tamanoLongitud);
                    continue; // Saltar este barco si no se reconoce el tipo por su longitud
                }

                Posicion posInicio = new Posicion(columna, fila); // Asume X=columna, Y=fila
                Barco barcoEntidad = new Barco(tipoNaveEnum, posInicio, orientacionEnum);
                
                if (!tableroFlotaModelo.agregarBarco(barcoEntidad)) {
                    System.err.println("CONTROLLER [Tablero] (poblarModelo) ERROR: No se pudo agregar barco " + tipoNaveEnum + " al modelo. Posiblemente inválido.");
                     if(vistaTablero!=null) vistaTablero.mostrarError("Error al validar la flota para: "+ tipoNaveEnum + ". Revise la consola.");
                } else {
                    System.out.println("CONTROLLER [Tablero] (poblarModelo): Barco " + tipoNaveEnum + " ("+orientacionEnum+") agregado al modelo en ["+columna+","+fila+"]");
                }
            } catch (Exception e) {
                System.err.println("CONTROLLER [Tablero] (poblarModelo) ERROR CRÍTICO procesando datos de barco desde UI: " + e.getMessage());
                e.printStackTrace();
                 if(vistaTablero!=null) vistaTablero.mostrarError("Error procesando un barco: " + e.getMessage());
            }
        }
        jugadorActual.setHaConfirmadoTablero(true); // Marcar que el jugador ha confirmado su tablero
        System.out.println("CONTROLLER [Tablero] (poblarModelo): Población del modelo finalizada. Jugador " + jugadorActual.getNombre() + " ha confirmado tablero.");
    }

    /**
     * Construye y envía el evento de jugador listo con su flota al servidor.
     * Asume que poblarModeloFlotaDesdeVista() ya fue llamado y el modelo está actualizado.
     */
    public void enviarEventoJugadorListoConFlota() {
        System.out.println("CONTROLLER [Tablero]: Jugador '" + this.miNombreUsuario + "' LISTO. Preparando evento con flota del modelo...");

        if (serverComunicacion == null || !serverComunicacion.isConectado()) {
            if(vistaTablero != null) vistaTablero.mostrarError("Error: No conectado al servidor. No se puede enviar la flota.");
            return;
        }

        Jugador jugadorActual = obtenerJugadorActual(); 
        if (jugadorActual == null || jugadorActual.getTableroFlota() == null) {
            if(vistaTablero != null) vistaTablero.mostrarError("Error interno: No se pudo obtener la información del jugador/tablero para enviar.");
            return;
        }
         if (!jugadorActual.haConfirmadoTablero()) { // Chequeo adicional
            if(vistaTablero != null) vistaTablero.mostrarError("Error interno: El tablero del jugador no parece estar confirmado en el modelo.");
            return;
        }

        TableroFlotaDTO tableroFlotaDTO = ModelConverter.toTableroFlotaDTO(jugadorActual.getTableroFlota());

        if (tableroFlotaDTO == null || tableroFlotaDTO.getBarcos() == null || tableroFlotaDTO.getBarcos().isEmpty()) {
            if(vistaTablero != null) vistaTablero.mostrarError("No hay barcos en el modelo para enviar. Por favor, colócalos primero.");
            return; 
        }

        String flotaJson;
        try {
            Gson gson = new GsonBuilder().create(); 
            // Vamos a enviar la lista de BarcoDTO, ya que TableroFlotaDTO incluye dimensión que quizás no es necesaria para este evento.
            flotaJson = gson.toJson(tableroFlotaDTO.getBarcos()); 
        } catch (Exception e) {
            System.err.println("CONTROLLER [Tablero] ERROR: Serialización de flota (BarcoDTO list) a JSON falló: " + e.getMessage());
            if (vistaTablero != null) vistaTablero.mostrarError("Error interno al preparar datos de la flota.");
            return;
        }
        System.out.println("JSON de la flota (desde el modelo): " + flotaJson);

        String mensajeJugadorListo = "Jugador " + this.miNombreUsuario + " Listo";
        // Usar el método genérico enviarMensaje de tu ServerComunicacion
        String eventoCompleto = String.format("EVENTO;TIPO=JUGADOR_FLOTA_LISTA;idSala=%s;nombreJugador=%s;mensaje=%s;flotaJson=%s",
                                              this.idSala,
                                              this.miNombreUsuario,
                                              mensajeJugadorListo,
                                              flotaJson);

        System.out.println("CONTROLLER [Tablero]: Enviando evento: " + eventoCompleto);
        serverComunicacion.enviarEventoJuego(eventoCompleto); // <--- USANDO TU MÉTODO enviarMensaje

        if(vistaTablero != null) {
            vistaTablero.deshabilitarColocacion("¡Flota enviada! Esperando al oponente...");
        }
    }


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