package Controler;

import View.PantallaPartida; // Tu vista de la partida
import Model.entidades.*;    // Tus entidades: Partida, Jugador, TableroFlota, etc.
import enums.*;              // Tus enums: ResultadoDisparo, etc.
import com.mycompany.servercomunicacion.ServerComunicacion; // Para comunicarse con el servidor
import com.google.gson.Gson; // Si necesitas serializar algo (aunque para enviar disparos no es JSON completo)
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import javax.swing.SwingUtilities;
import java.util.Map; // Para procesar datos de eventos del servidor
import javax.swing.JButton;

public class controladorPartida {

    private PantallaPartida vistaPartida;
    private Partida partidaActual;
    private ServerComunicacion serverComunicacion;
    private String nombreJugadorLocal;
    private Jugador jugadorLocalEntidad;
    private Jugador oponenteEntidad;
    private final Gson gson; // Útil para deserializar datos complejos si el servidor los envía

    public static final int DIMENSION_TABLERO = 10; // Si es una constante global

    /**
     * Constructor para el controlador de una partida en curso.
     * @param serverCom La instancia para comunicarse con el servidor.
     * @param partidaEntidad La entidad Partida ya procesada (convertida desde DTO).
     * @param nombreJugadorLocal El nombre del jugador que está usando esta instancia del cliente.
     */
    public controladorPartida(ServerComunicacion serverCom, Partida partidaEntidad, String nombreJugadorLocal) {
        this.serverComunicacion = serverCom;
        this.partidaActual = partidaEntidad;
        this.nombreJugadorLocal = nombreJugadorLocal;
        this.gson = new Gson();

        // Identificar jugador local y oponente dentro de la entidad Partida
        if (partidaActual.getJugador1() != null && partidaActual.getJugador1().getNombre().equals(nombreJugadorLocal)) {
            this.jugadorLocalEntidad = partidaActual.getJugador1();
            this.oponenteEntidad = partidaActual.getJugador2();
        } else if (partidaActual.getJugador2() != null && partidaActual.getJugador2().getNombre().equals(nombreJugadorLocal)) {
            this.jugadorLocalEntidad = partidaActual.getJugador2();
            this.oponenteEntidad = partidaActual.getJugador1();
        } else {
            System.err.println("CONTROLADOR_PARTIDA FATAL: Jugador local '" + nombreJugadorLocal + "' no encontrado en la Partida.");
            // Aquí deberías manejar este error crítico, quizás mostrando un mensaje y volviendo.
            return;
        }

        this.vistaPartida = new PantallaPartida(); // Crear la instancia de la UI de la partida
        // Configurar la vista con los datos iniciales de la partida
        configurarVistaInicial();
        configurarListenersDeDisparo(); // Configurar para que la UI notifique los disparos
    }

    /**
     * Configura la vista con la información inicial de la partida.
     * Este método reemplaza a tu antiguo "IniciarCombate" en términos de configuración de UI.
     */
    private void configurarVistaInicial() {
        if (vistaPartida == null || partidaActual == null || jugadorLocalEntidad == null || oponenteEntidad == null) {
            System.err.println("CONTROLADOR_PARTIDA: Error al configurar vista, datos incompletos.");
            return;
        }
        System.out.println("CONTROLADOR_PARTIDA: Configurando vista para " + nombreJugadorLocal);

        vistaPartida.setTitle("Batalla Naval - " + nombreJugadorLocal + " vs " + oponenteEntidad.getNombre());

        // Dibujar el tablero de flota del jugador local
        vistaPartida.dibujarTableroFlotaPropio(jugadorLocalEntidad.getTableroFlota());

        // Dibujar el tablero de seguimiento del jugador local (inicialmente vacío de impactos)
        vistaPartida.dibujarTableroSeguimiento(jugadorLocalEntidad.getTableroSeguimiento());

        // Indicar de quién es el turno
        actualizarInformacionDeTurno();

        vistaPartida.setVisible(true);
        System.out.println("CONTROLADOR_PARTIDA: Vista de partida configurada y visible.");
    }

    /**
     * Configura los listeners en la vista para cuando el jugador haga clic en el tablero de seguimiento.
     */
    private void configurarListenersDeDisparo() {
        if (vistaPartida == null) return;

        vistaPartida.setTableroListener((fila, columna, celdaBoton) -> {
            System.out.println("CONTROLADOR_PARTIDA: Clic en tablero de seguimiento: Fila=" + fila + ", Columna=" + columna);

            if (partidaActual.getEstado() != EstadoPartida.EN_CURSO) {
                vistaPartida.mostrarMensajeGeneral("La partida no está en curso.");
                return;
            }

            if (partidaActual.obtenerJugadorEnTurno() == null || !partidaActual.obtenerJugadorEnTurno().getNombre().equals(nombreJugadorLocal)) {
                vistaPartida.mostrarMensajeGeneral("No es tu turno.");
                return;
            }

            // Verificar si ya se disparó en esa casilla (usando la entidad local)
            if (jugadorLocalEntidad.getTableroSeguimiento().yaSeDisparoEn(new Posicion(columna, fila))) { // Asumiendo X=columna, Y=fila
                vistaPartida.mostrarMensajeGeneral("Ya has disparado en esta casilla.");
                return;
            }

            // Enviar evento de disparo al servidor
            System.out.println("CONTROLADOR_PARTIDA: Enviando disparo a (" + fila + "," + columna + ") para partida " + partidaActual.getIdPartida());
            String eventoDisparo = String.format("EVENTO;TIPO=REALIZAR_DISPARO;idSala=%s;nombreJugador=%s;fila=%d;columna=%d",
                                                 partidaActual.getIdPartida(),
                                                 nombreJugadorLocal,
                                                 fila,
                                                 columna);
            serverComunicacion.enviarEventoJuego(eventoDisparo); // Necesitas este método en ServerComunicacion

            // Opcional: Deshabilitar la celda en la UI inmediatamente para evitar doble clic
            // celdaBoton.setEnabled(false); // O marcarla como "pendiente"
            vistaPartida.marcarCasillaSeguimientoComoPendiente(fila, columna); // Necesitas este método en PantallaPartida
        });
    }

    /**
     * Actualiza la información de turno en la vista.
     */
    private void actualizarInformacionDeTurno() {
        if (vistaPartida == null || partidaActual == null || partidaActual.obtenerJugadorEnTurno() == null) return;

        String nombreTurno = partidaActual.obtenerJugadorEnTurno().getNombre();
        boolean esMiTurno = nombreTurno.equals(nombreJugadorLocal);
        vistaPartida.actualizarEstadoTurno("Turno de: " + nombreTurno + (esMiTurno ? " (¡TÚ!)" : ""), esMiTurno);
        // El segundo parámetro 'esMiTurno' en actualizarEstadoTurno podría habilitar/deshabilitar
        // la interacción con el tablero de seguimiento en la vista.
    }


    // --- Métodos para ser llamados por controladorInicio cuando llegan eventos del servidor ---

    /**
     * Procesa el resultado de un disparo recibido del servidor.
     * @param datos Los datos del evento RESULTADO_DISPARO.
     */
    // En Controler.controladorPartida.java

public void procesarResultadoDisparo(Map<String, Object> datos) {
    System.out.println("CONTROLADOR_PARTIDA: Procesando RESULTADO_DISPARO: " + datos);
    if (vistaPartida == null || partidaActual == null || jugadorLocalEntidad == null) {
        System.err.println("CONTROLADOR_PARTIDA ERROR: Vista o modelo nulo en procesarResultadoDisparo.");
        return;
    }

    String idSalaEvento = (String) datos.get("idSala");
    if (!partidaActual.getIdPartida().equals(idSalaEvento)) {
        System.err.println("CONTROLADOR_PARTIDA: Resultado de disparo para una sala incorrecta ("+idSalaEvento+"). Esta sala es "+partidaActual.getIdPartida()+". Ignorando.");
        return;
    }

    try {
        int fila = Integer.parseInt((String) datos.get("fila"));
        int columna = Integer.parseInt((String) datos.get("columna"));
        ResultadoDisparo resultado = ResultadoDisparo.valueOf(((String) datos.get("resultado")).toUpperCase());
        String nombreAtacante = (String) datos.get("nombreJugadorQueDisparo");
        // String nombreDefensor = (String) datos.get("nombreJugadorImpactado"); // El que recibió
        String turnoActualizado = (String) datos.get("turnoActualizado");
        String tipoBarcoHundido = (String) datos.get("tipoBarcoHundido"); // Puede ser null
        String estadoNaveImpactadaStr = (String) datos.get("estadoNaveImpactada"); // Puede ser null
        boolean partidaTerminada = "true".equalsIgnoreCase((String) datos.get("partidaTerminada"));
        String ganador = (String) datos.get("ganador");
        String mensajeFin = (String) datos.get("mensajeFin");


        Posicion posDisparo = new Posicion(columna, fila);

        if (nombreAtacante.equals(this.nombreJugadorLocal)) {
            // Es el resultado de MI disparo
            System.out.println("CONTROLADOR_PARTIDA: Es resultado de MI disparo en (" + fila + "," + columna + ")");
            jugadorLocalEntidad.getTableroSeguimiento().marcarDisparo(posDisparo, resultado);
            vistaPartida.actualizarCasillaSeguimiento(fila, columna, resultado, false); // El 'false' final no se usa
            
            String mensajeUi = "Disparo en ("+fila+","+columna+"): " + resultado;
            if(resultado == ResultadoDisparo.HUNDIDO && tipoBarcoHundido != null) {
                mensajeUi += " - ¡Hundiste un " + tipoBarcoHundido + "!";
            }
            vistaPartida.mostrarMensajeGeneral(mensajeUi);

        } else {
            // Es un disparo del oponente en MI tablero
            System.out.println("CONTROLADOR_PARTIDA: Es resultado de un disparo del OPONENTE en (" + fila + "," + columna + ") de mi tablero.");
            // El modelo TableroFlota local se actualiza aquí para reflejar el impacto
            // (asumiendo que el servidor es la fuente de verdad y nos dice el resultado en nuestro barco)
            // Necesitamos encontrar el barco en nuestro TableroFlota y registrar el impacto
            Barco barcoMioImpactado = jugadorLocalEntidad.getTableroFlota().getBarcoEn(posDisparo);
            if (barcoMioImpactado != null) {
                boolean fueImpactoNuevo = barcoMioImpactado.registrarImpacto(posDisparo); // Esto actualiza el estado del barco
                 System.out.println("CONTROLADOR_PARTIDA: Barco propio " + barcoMioImpactado.getTipo() + " impactado. Nuevo estado: " + barcoMioImpactado.getEstado());
            }
            vistaPartida.actualizarCasillaFlotaPropia(fila, columna, resultado);
            
            String mensajeUiOponente = "El oponente disparó en ("+fila+","+columna+"): " + resultado;
             if(resultado == ResultadoDisparo.HUNDIDO && tipoBarcoHundido != null) {
                mensajeUiOponente += " - ¡Tu " + tipoBarcoHundido + " ha sido hundido!";
            }
            // Considera mostrar este mensaje de forma diferente, quizás en un log de eventos de la partida en la UI
            // JOptionPane puede ser muy intrusivo para cada disparo del oponente.
            // vistaPartida.mostrarMensajeGeneral(mensajeUiOponente); 
            System.out.println("INFO PARA UI: " + mensajeUiOponente); // Log para que luego decidas cómo mostrarlo
        }

        // Actualizar el turno para AMBOS jugadores
        if (turnoActualizado != null && partidaActual.getJugadorPorNombre(turnoActualizado) != null) {
            partidaActual.setJugadorEnTurno(partidaActual.getJugadorPorNombre(turnoActualizado));
            System.out.println("CONTROLADOR_PARTIDA: Turno del modelo Partida actualizado a: " + partidaActual.obtenerJugadorEnTurno().getNombre());
            actualizarInformacionDeTurno(); // Esto llamará a vistaPartida.actualizarEstadoTurno(...)
        } else {
            System.err.println("CONTROLADOR_PARTIDA: No se pudo actualizar el turno, 'turnoActualizado' es nulo o jugador no encontrado: " + turnoActualizado);
            // Si el turno no se actualiza, la UI podría quedarse bloqueada o permitir disparos incorrectos.
            // Considera solicitar estado al servidor o manejar este error.
        }

        // Verificar si la partida terminó (basado en la info del servidor)
        if (partidaTerminada) {
            System.out.println("CONTROLADOR_PARTIDA: La partida ha terminado. Ganador: " + ganador);
            partidaActual.setEstado(EstadoPartida.FINALIZADA); // O un estado más específico si lo tienes
            vistaPartida.mostrarFinDePartida(mensajeFin != null ? mensajeFin : "La partida ha terminado. Ganador: " + ganador);
        }

    } catch (Exception e) {
        System.err.println("CONTROLADOR_PARTIDA ERROR: Excepción procesando ResultadoDisparo: " + e.getMessage());
        e.printStackTrace();
        if (vistaPartida != null) vistaPartida.mostrarError("Error procesando resultado del disparo: " + e.getMessage(), false);
    }
}

    /**
     * Procesa un cambio de turno general (si no viene con RESULTADO_DISPARO).
     * @param datos Datos del evento, esperando "nombreJugadorEnTurno".
     */
    public void procesarCambioDeTurno(Map<String, Object> datos) {
        System.out.println("CONTROLADOR_PARTIDA: Procesando CAMBIO_DE_TURNO: " + datos);
        if (partidaActual == null) return;

        String nombreNuevoTurno = (String) datos.get("nombreJugadorEnTurno");
        if (nombreNuevoTurno != null) {
             if (partidaActual.getJugador1() != null && partidaActual.getJugador1().getNombre().equals(nombreNuevoTurno)) {
                partidaActual.setJugadorEnTurno(partidaActual.getJugador1());
            } else if (partidaActual.getJugador2() != null && partidaActual.getJugador2().getNombre().equals(nombreNuevoTurno)) {
                partidaActual.setJugadorEnTurno(partidaActual.getJugador2());
            }
            actualizarInformacionDeTurno();
        }
    }
    
    /**
     * Procesa un evento de fin de partida.
     * @param datos Datos del evento, esperando "ganador" y "motivo".
     */
    public void procesarFinDePartida(Map<String, Object> datos) {
        System.out.println("CONTROLADOR_PARTIDA: Procesando FIN_DE_PARTIDA: " + datos);
        if (vistaPartida == null || partidaActual == null) return;

        String ganador = (String) datos.get("ganador");
        String motivo = (String) datos.get("motivo"); // Ej: "Todos los barcos hundidos", "Oponente abandonó"

        if (ganador != null) {
            partidaActual.setEstado(EstadoPartida.FINALIZADA_GANA_J1); // Ajustar si necesitas saber quién es J1/J2
            vistaPartida.mostrarFinDePartida("¡Partida Terminada! Ganador: " + ganador + ". Motivo: " + motivo);
        } else {
            partidaActual.setEstado(EstadoPartida.ABANDONADA); // O algún otro estado final
            vistaPartida.mostrarFinDePartida("Partida Terminada. Motivo: " + motivo);
        }
        // Aquí podrías deshabilitar tableros, mostrar botón de "Volver al menú", etc.
    }
    
     // Este método se llama cuando el jugador hace clic en una celda del tablero del oponente
    
    public void onCeldaSeleccionada(int fila, int columna, JButton celdaBoton) {
        System.out.println("CONTROLADOR_PARTIDA: Clic en tablero de seguimiento: Fila=" + fila + ", Columna=" + columna + " por " + nombreJugadorLocal);

        if (partidaActual == null || jugadorLocalEntidad == null) {
            vistaPartida.mostrarError("Error crítico: Datos de partida o jugador no inicializados.", true);
            return;
        }
        
        if (partidaActual.getEstado() != EstadoPartida.EN_CURSO) {
            vistaPartida.mostrarMensajeGeneral("La partida no está en curso o ya ha finalizado.");
            return;
        }

        // 1. Validar si es el turno del jugador local
        Jugador jugadorEnTurnoModelo = partidaActual.obtenerJugadorEnTurno();
        if (jugadorEnTurnoModelo == null || !jugadorEnTurnoModelo.getNombre().equals(this.nombreJugadorLocal)) {
            vistaPartida.mostrarMensajeGeneral("No es tu turno.");
            System.out.println("CONTROLADOR_PARTIDA: Intento de disparo fuera de turno por " + this.nombreJugadorLocal +
                               ". Turno actual de: " + (jugadorEnTurnoModelo != null ? jugadorEnTurnoModelo.getNombre() : "Nadie"));
            return;
        }

        // 2. Validar si ya se disparó en esa casilla usando el TableroSeguimiento del jugador local
        Posicion posicionDisparo = new Posicion(columna, fila); // Tu clase Posicion (X, Y)
        TableroSeguimiento miTableroSeguimiento = jugadorLocalEntidad.getTableroSeguimiento();

        if (miTableroSeguimiento == null) {
            vistaPartida.mostrarError("Error interno: Tablero de seguimiento no encontrado.", true);
            return;
        }

        if (miTableroSeguimiento.yaSeDisparoEn(posicionDisparo)) {
            vistaPartida.mostrarMensajeGeneral("Ya has disparado en la casilla (" + fila + "," + columna + "). Elige otra.");
            System.out.println("CONTROLADOR_PARTIDA: Intento de disparar a casilla ya atacada: " + posicionDisparo);
            return;
        }

        // 3. Si las validaciones pasan, construir y enviar el evento
        if (serverComunicacion != null && serverComunicacion.isConectado()) {
            // Marcar la casilla en la UI como "pendiente" para feedback inmediato
            if (vistaPartida != null) {
                vistaPartida.marcarCasillaSeguimientoComoPendiente(fila, columna);
            }

            // Formato del evento: EVENTO;TIPO=REALIZAR_DISPARO;idSala=X;nombreJugador=Y;fila=R;columna=C
            String eventoDisparoStr = String.format("EVENTO;TIPO=REALIZAR_DISPARO;idSala=%s;nombreJugador=%s;fila=%d;columna=%d",
                                                 this.partidaActual.getIdPartida(), // Obtener idSala de partidaActual
                                                 this.nombreJugadorLocal,
                                                 fila,
                                                 columna);
            
            System.out.println("CONTROLADOR_PARTIDA: Enviando evento de disparo: " + eventoDisparoStr);
            serverComunicacion.enviarEventoJuego(eventoDisparoStr); // Usando el método público de ServerComunicacion

            // Opcional: Deshabilitar el tablero del oponente temporalmente hasta recibir respuesta
            // vistaPartida.actualizarEstadoTurno("Disparo realizado. Esperando resultado del servidor...", false);

        } else {
            vistaPartida.mostrarError("No estás conectado al servidor. No se pudo realizar el disparo.", false);
        }
    }


    // Método para cerrar la vista de partida (llamado por controladorInicio si hay desconexión general)
    public void cerrarVistaPartida() {
        if (vistaPartida != null && vistaPartida.isDisplayable()) {
            vistaPartida.dispose();
            System.out.println("CONTROLADOR_PARTIDA: Vista de partida cerrada.");
        }
    }
    
    // Getter para la vista, por si controladorInicio necesita accederla (ej. para cerrarla)
    public PantallaPartida getVistaPartida() {
        return vistaPartida;
    }
}