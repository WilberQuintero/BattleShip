/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controler;

import View.PantallaInicio;
import View.UnirseJugar;
import View.PartidaEspera;
import com.mycompany.servercomunicacion.ServerComunicacion;
import com.mycompany.servercomunicacion.ServerEventListener;
import Model.entidades.Jugador;
import Model.entidades.Partida;
import Model.entidades.mappers.ModelConverter;
import com.google.gson.Gson;
import dto.PartidaDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Component;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

public class controladorInicio implements ServerEventListener {

    private final ServerComunicacion serverComunicacion;
    private PantallaInicio vistaInicio;
    private UnirseJugar vistaUnirseJugar;
    private PartidaEspera vistaPartidaEspera;
    private controladorCrearPartida controladorCrearPartidaActual;
    private controladorPartidaEspera controladorEsperaActual;
    private controladorTablero controladorTableroPartida;
    private String nombreUsuarioPendiente = null;
    private String nombreUsuarioRegistrado = null;
    private Map<String, Partida> partidasActivas;
    private final int DIMENSION_TABLERO_DEFAULT = 10;
   // Atributo para mantener la referencia al controlador de la partida activa
    private controladorPartida controladorDePartidaActual;

    public controladorInicio() {
        System.out.println("CONTROLLER [Inicio]: Inicializando...");
        String hostServidor = "localhost";
        int puertoServidor = 5000;
        ServerComunicacion tempCom = null;
        try {
            tempCom = new ServerComunicacion(hostServidor, puertoServidor);
            tempCom.setListener(this);
            System.out.println("CONTROLLER [Inicio]: ServerComunicacion creado y listener asignado.");
        } catch (Exception e) {
            System.err.println("CONTROLLER [Inicio]: ERROR CRÍTICO al inicializar ServerComunicacion: " + e.getMessage());
        }
        this.serverComunicacion = tempCom;
        this.partidasActivas = new HashMap<>();
        System.out.println("CONTROLLER [Inicio]: Mapa de partidas activas inicializado.");
    }

    public controladorInicio getControladorPrincipal() {
        return this;
    }

    public void setVistaInicio(PantallaInicio vista) { this.vistaInicio = vista; }
    public void setVistaUnirseJugar(UnirseJugar vista) { this.vistaUnirseJugar = vista; }
    public void setVistaPartidaEspera(PartidaEspera vista) { this.vistaPartidaEspera = vista; }
    public ServerComunicacion getServerComunicacion() { return serverComunicacion; }
    public String getNombreUsuarioRegistrado() { return nombreUsuarioRegistrado; }

     public Partida getPartidaActual(String idSala) {
        if (idSala == null) {
            // Si idSala es null, intenta obtener la del delegadoEspera si está activo
            if (this.controladorEsperaActual != null && this.controladorEsperaActual.getIdSala() != null) {
                return partidasActivas.get(this.controladorEsperaActual.getIdSala());
            }
            // Si no, no podemos determinar una partida actual.
            System.err.println("MODELO_CLIENTE: getPartidaActual llamado con idSala null y sin delegado de espera activo con sala.");
            return null;
        }
        return partidasActivas.get(idSala);
    }

 // Método para establecer y limpiar el controlador de partida actual (ya lo teníamos)
    public void setControladorDePartidaActual(controladorPartida ctrlPartida) {
        this.controladorDePartidaActual = ctrlPartida;
    }

    public void limpiarControladorDePartidaActual() {
        if (this.controladorDePartidaActual != null) {
            // Pedirle al controlador de partida que cierre su vista
            this.controladorDePartidaActual.cerrarVistaPartida();
            this.controladorDePartidaActual = null;
            System.out.println("CONTROLLER [Inicio]: Controlador de partida actual limpiado.");
        }
    }
    public void eliminarPartida(String idSala) {
        Partida p = partidasActivas.remove(idSala);
        if (p != null) {
            System.out.println("CONTROLLER [Inicio]: Partida eliminada de la gestión interna: " + idSala);
        }
    }

    public void intentarConectarYRegistrar(String nombreUsuario) {
        System.out.println("CONTROLLER [Inicio]: Solicitud para conectar y registrar usuario: " + nombreUsuario);
        if (serverComunicacion == null) {
            if (vistaInicio != null) vistaInicio.mostrarError("Error interno: Comunicación no disponible.");
            return;
        }
        if (serverComunicacion.isConectado()) {
            System.out.println("CONTROLLER [Inicio]: Ya conectado.");
            if (this.nombreUsuarioRegistrado == null) {
                System.out.println("CONTROLLER [Inicio]: Intentando registrar nombre: " + nombreUsuario);
                this.nombreUsuarioPendiente = nombreUsuario;
                if (nombreUsuarioPendiente != null) {
                    serverComunicacion.enviarRegistroUsuario(nombreUsuarioPendiente);
                }
            } else {
                System.out.println("CONTROLLER [Inicio]: Ya conectado y registrado como " + this.nombreUsuarioRegistrado);
                if (vistaInicio != null) vistaInicio.navegarASiguientePantalla();
            }
            return;
        }
        this.nombreUsuarioPendiente = nombreUsuario;
        System.out.println("CONTROLLER [Inicio]: Nombre guardado. Solicitando conexión...");
        serverComunicacion.conectar();
    }

    public void solicitarDesconexion() {
        System.out.println("CONTROLLER [Inicio]: Solicitando desconexión...");
        if (serverComunicacion != null) {
            serverComunicacion.desconectar();
        }
    }

    public void setControladorCrearPartidaActual(controladorCrearPartida controlador) {
        System.out.println("CONTROLLER [Inicio]: Estableciendo controladorCrearPartidaActual: " + (controlador != null ? controlador.getClass().getSimpleName() : "null"));
        clearControladorEsperaActual();
        this.controladorCrearPartidaActual = controlador;
    }

    public void clearControladorCrearPartidaActual() {
        if (this.controladorCrearPartidaActual != null) {
            System.out.println("CONTROLLER [Inicio]: Limpiando controladorCrearPartidaActual.");
            this.controladorCrearPartidaActual = null;
        }
    }

    public void setControladorEsperaActual(controladorPartidaEspera controlador) {
        System.out.println("CONTROLLER [Inicio]: Estableciendo controladorEsperaActual: " + (controlador != null ? controlador.getClass().getSimpleName() : "null"));
        clearControladorCrearPartidaActual();
        this.controladorEsperaActual = controlador;
    }

    public void clearControladorEsperaActual() {
        if (this.controladorEsperaActual != null) {
            System.out.println("CONTROLLER [Inicio]: Limpiando controladorEsperaActual.");
            this.controladorEsperaActual = null;
        }
    }

    @Override
    public void onConectado() {
        System.out.println("CONTROLLER [Inicio]: ¡CONECTADO! Callback onConectado recibido.");
        if (nombreUsuarioPendiente != null && serverComunicacion != null && serverComunicacion.isConectado()) {
            System.out.println("CONTROLLER [Inicio][onConectado]: Estado verificado OK. Enviando registro para: " + nombreUsuarioPendiente);
            serverComunicacion.enviarRegistroUsuario(nombreUsuarioPendiente);
        } else {
            System.err.println("CONTROLLER [Inicio][onConectado]: ERROR DE ESTADO INESPERADO al intentar registrar.");
            nombreUsuarioPendiente = null;
            if (vistaInicio != null) {
                SwingUtilities.invokeLater(() -> {
                    vistaInicio.mostrarError("Error interno post-conexión.");
                    vistaInicio.reactivarBotonPlay();
                });
            }
            solicitarDesconexion();
        }
    }
  // En onDesconectado, asegúrate de llamar a limpiarControladorDePartidaActual()
    @Override
    public void onDesconectado(String motivo) {
        System.out.println("CONTROLLER [Inicio]: DESCONECTADO. Motivo: " + motivo);
        clearControladorCrearPartidaActual(); // Ya lo tienes
        clearControladorEsperaActual();     // Ya lo tienes
        limpiarControladorDePartidaActual(); // Limpiar el controlador de juego también

        nombreUsuarioPendiente = null;
        nombreUsuarioRegistrado = null;
        partidasActivas.clear(); 

        SwingUtilities.invokeLater(() -> {
            // ... (tu lógica para cerrar vistas y volver a PantallaInicio) ...
             if (vistaUnirseJugar != null && vistaUnirseJugar.isDisplayable()) { vistaUnirseJugar.dispose(); this.vistaUnirseJugar = null; }
             if (vistaPartidaEspera != null && vistaPartidaEspera != null && vistaPartidaEspera.isDisplayable()) { // Asumiendo que controladorEsperaActual tiene getVistaEspera()
                vistaPartidaEspera.dispose(); 
                this.vistaPartidaEspera = null; // Si guardas la vista directamente en controladorInicio
             }
             // La vista de partida se cierra a través de limpiarControladorDePartidaActual()

            if (vistaInicio != null) {
                if (!vistaInicio.isDisplayable()) vistaInicio.setVisible(true);
                vistaInicio.mostrarEstadoDesconectado(motivo);
                vistaInicio.reactivarBotonPlay();
            } else {
                 JOptionPane.showMessageDialog(null, "Desconectado: " + motivo, "Desconexión", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

 
    @Override
    public void onError(String mensajeError) {
        System.err.println("CONTROLLER [Inicio]: ERROR recibido: " + mensajeError);
        nombreUsuarioPendiente = null; // Resetear nombre pendiente en caso de error de conexión/registro
        SwingUtilities.invokeLater(() -> {
            Component vistaActiva = null;
            if (vistaPartidaEspera != null && vistaPartidaEspera.isDisplayable()) vistaActiva = vistaPartidaEspera;
            else if (vistaUnirseJugar != null && vistaUnirseJugar.isDisplayable()) vistaActiva = vistaUnirseJugar;
            else vistaActiva = vistaInicio;

            if (vistaActiva instanceof PartidaEspera) {
                ((PartidaEspera) vistaActiva).volverAPantallaAnterior(mensajeError);
            } else if (vistaActiva instanceof UnirseJugar) {
                ((UnirseJugar) vistaActiva).mostrarError(mensajeError);
                ((UnirseJugar) vistaActiva).reactivarBotones();
            } else if (vistaActiva instanceof PantallaInicio) {
                ((PantallaInicio) vistaActiva).mostrarError(mensajeError);
                ((PantallaInicio) vistaActiva).reactivarBotonPlay();
            } else {
                JOptionPane.showMessageDialog(null, mensajeError, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked") // Para el casteo de List<String>
    public void onMensajeServidor(String tipo, Map<String, Object> datos) {
        System.out.println("CONTROLLER [Inicio]: Mensaje del servidor recibido - Tipo: " + tipo + ", Datos: " + datos);

        final controladorCrearPartida delegadoCrearUnir = this.controladorCrearPartidaActual;
        final controladorPartidaEspera delegadoEspera = this.controladorEsperaActual;
        final controladorTablero delegadoTablero = this.controladorTableroPartida;
        // Podrías necesitar una referencia al controlador de la pantalla de colocación si es diferente de delegadoEspera
        // final ControladorColocacionBarcos delegadoColocacion = this.controladorColocacionActual;

        final String finalTipo = tipo;
        final Map<String, Object> finalDatos = datos;

        final boolean esParaEsperaOColocacion = esMensajeParaEsperaOColocacion(finalTipo);
        final boolean esParaCrearUnir = esMensajeParaCrearUnir(finalTipo);
        final boolean esParaEspera = esMensajeParaEspera(finalTipo);
        final boolean esRegistro = finalTipo.equals("REGISTRO_OK") || finalTipo.equals("ERROR_REGISTRO");
        final boolean esIniciarCombate = finalTipo.equals("INICIAR_COMBATE");
                final boolean esEventoDeJuego = esEventoDeJuegoEnCurso(finalTipo); // Tu helper
        
        
        SwingUtilities.invokeLater(() -> {
            System.out.println(">>> [EDT] Task START for " + finalTipo);
            try {
                if (esIniciarCombate) {
                      System.out.println("   [EDT] Procesando INICIAR_COMBATE...");
                    String idSalaCombate = (String) finalDatos.get("idSala");
                    String partidaJsonBase64 = (String) finalDatos.get("partidaJsonBase64");
                    // String mensajeServidor = (String) finalDatos.get("mensaje"); // Para el JOptionPane

                    if (partidaJsonBase64 != null && idSalaCombate != null) {
                        try {
                            byte[] decodedBytes = Base64.getDecoder().decode(partidaJsonBase64);
                            String partidaJson = new String(decodedBytes, StandardCharsets.UTF_8);
                            Gson gson = new Gson();
                            PartidaDTO partidaDTORecibida = gson.fromJson(partidaJson, PartidaDTO.class);

                            System.out.println("   [EDT] PartidaDTO deserializada para INICIAR_COMBATE: " + partidaDTORecibida.getIdPartida() +
                                               ", Estado: " + partidaDTORecibida.getEstado() +
                                               ", Turno de: " + partidaDTORecibida.getNombreJugadorEnTurno());

                            Partida entidadPartidaCliente = ModelConverter.toPartidaEntity(partidaDTORecibida);
                            if (entidadPartidaCliente == null) {
                                throw new IllegalStateException("ModelConverter.toPartidaEntity devolvió null para INICIAR_COMBATE.");
                            }
                            partidasActivas.put(idSalaCombate, entidadPartidaCliente);
                            System.out.println("   [EDT] Entidad Partida '" + idSalaCombate + "' CREADA/ACTUALIZADA en modelo cliente.");

                            // Limpiar vistas y controladores de fases anteriores
                            if (delegadoEspera != null && delegadoEspera.getVista() != null && delegadoEspera.getVista().isDisplayable()) {
                                delegadoEspera.getVista().dispose();
                                System.out.println("   [EDT] Vista de Espera/Colocación cerrada.");
                            }
                            if (delegadoCrearUnir != null && delegadoCrearUnir.getVista() != null && delegadoCrearUnir.getVista().isDisplayable()) {
                                delegadoCrearUnir.getVista().dispose();
                                System.out.println("   [EDT] Vista de Unirse/Jugar cerrada.");
                            }
                            clearControladorEsperaActual();
                            clearControladorCrearPartidaActual();
                            
                            // --- ¡AQUÍ SE INSTANCIA Y USA TU controladorPartida! ---
                            System.out.println("   [EDT] Instanciando controladorPartida para la partida: " + entidadPartidaCliente.getIdPartida());
                            // Se le pasa el serverComunicacion, la entidadPartida ya convertida, y el nombre del jugador local
                            this.controladorDePartidaActual = new controladorPartida(
                                this.serverComunicacion,
                                entidadPartidaCliente,
                                this.nombreUsuarioRegistrado
                            );
                            // El constructor de controladorPartida ya crea y muestra su PantallaPartida.
                            System.out.println("   [EDT] controladorPartida instanciado. La vista de partida debería estar ahora visible.");
                            
                            // Puedes quitar el JOptionPane si la pantalla de partida ya muestra la info de turno.
                            // JOptionPane.showMessageDialog(null, mensajeServidor != null ? mensajeServidor : "¡El combate ha comenzado!", "Partida Iniciada", JOptionPane.INFORMATION_MESSAGE);

                        
                        } catch (Exception e) {
                            System.err.println("   [EDT] Error al procesar datos de INICIAR_COMBATE: " + e.getMessage());
                            e.printStackTrace();
                            // Mostrar error en la vista que esté activa (podría ser la de espera/colocación)
                             if (delegadoEspera != null && delegadoEspera.isVistaActiva()) {
                                delegadoEspera.mostrarErrorColocacion("Error al iniciar la partida: " + e.getMessage());
                            } else if (vistaUnirseJugar != null && vistaUnirseJugar.isDisplayable()) { // Fallback
                                vistaUnirseJugar.mostrarError("Error al iniciar la partida: " + e.getMessage());
                            } else {
                                JOptionPane.showMessageDialog(null, "Error al iniciar la partida: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } else {
                        System.err.println("   [EDT] Evento INICIAR_COMBATE no contenía datos de partidaJsonBase64 o idSala.");
                         if (delegadoEspera != null && delegadoEspera.isVistaActiva()) {
                                delegadoEspera.mostrarErrorColocacion("Error: Datos incompletos del servidor para iniciar partida.");
                         } else {
                             JOptionPane.showMessageDialog(null, "Datos incompletos del servidor para iniciar partida.", "Error", JOptionPane.ERROR_MESSAGE);
                         }
                    }

                }else if (this.controladorDePartidaActual != null && esEventoDeJuego) {
                    // --- DELEGACIÓN DE EVENTOS DE JUEGO AL CONTROLADOR DE PARTIDA ACTIVO ---
                    System.out.println("   [EDT] Delegando evento de juego '" + finalTipo + "' a controladorDePartidaActual.");
                    switch(finalTipo) {
                        case "RESULTADO_DISPARO":
                            controladorDePartidaActual.procesarResultadoDisparo(finalDatos);
                            break;
                        case "CAMBIO_DE_TURNO":
                            controladorDePartidaActual.procesarCambioDeTurno(finalDatos);
                            break;
                        case "FIN_PARTIDA":
                            controladorDePartidaActual.procesarFinDePartida(finalDatos);
                            // Una vez que la partida termina, limpiamos la referencia y su vista
                            limpiarControladorDePartidaActual();
                            // Aquí podrías navegar a una pantalla de "Fin de Juego" o volver al menú (UnirseJugar)
                            // Ejemplo:
                            // if (vistaUnirseJugar == null || !vistaUnirseJugar.isDisplayable()) {
                            //     vistaUnirseJugar = new UnirseJugar();
                            //     controladorCrearPartidaActual = new controladorCrearPartida(serverComunicacion, vistaUnirseJugar, this);
                            //     setControladorCrearPartidaActual(controladorCrearPartidaActual); // Registrar nuevo delegado
                            //     vistaUnirseJugar.setControlador(controladorCrearPartidaActual);
                            //     vistaUnirseJugar.setVisible(true);
                            // } else {
                            //    vistaUnirseJugar.setVisible(true);
                            //    vistaUnirseJugar.toFront();
                            // }
                            break;
                        default:
                            System.out.println("   [EDT] Evento de juego '" + finalTipo + "' no manejado explícitamente por switch en controladorInicio (delegado a controladorPartida).");
                            // El controladorPartida podría tener su propio switch o manejo para otros eventos de juego
                            if (controladorDePartidaActual.getVistaPartida() != null && finalDatos.containsKey("mensaje")) {
                               controladorDePartidaActual.getVistaPartida().mostrarMensajeGeneral((String)finalDatos.get("mensaje"));
                            }
                            break;
                    }
                  }                           
                else if (delegadoEspera != null && esParaEspera) {
                    System.out.println("   [EDT] Procesando para delegado ESPERA/COLOCACION tipo " + finalTipo);
                    switch (finalTipo) {
                        case "NUEVO_JUGADOR_EN_SALA":
                            
                             System.out.println("    [EDT] Procesando NUEVO_JUGADOR_EN_SALA...");
                            String idSalaNuevoJugador = (String) finalDatos.get("idSala");
                            String nombreJugadorInfo = (String) finalDatos.get("jugadorInfo");

                            if (idSalaNuevoJugador != null && nombreJugadorInfo != null && this.nombreUsuarioRegistrado != null) {
                                String jugadorActualCliente = this.nombreUsuarioRegistrado;
                                String otroJugadorInfo = nombreJugadorInfo;

                                if (jugadorActualCliente.equals(otroJugadorInfo)) {
                                    System.out.println("    [EDT] NUEVO_JUGADOR_EN_SALA informa sobre el jugador actual (" + jugadorActualCliente + "). No se crea Partida aquí. Esperando ACTUALIZACION_SALA con ambos nombres distintos.");
                                    if (delegadoEspera != null) {
                                         // Solo actualizar UI con el jugador conocido
                                         Map<String, Object> datosUiUnJugador = new HashMap<>(finalDatos);
                                         datosUiUnJugador.put("jugadores", List.of(jugadorActualCliente)); // Lista con un solo jugador
                                         delegadoEspera.procesarActualizacionSala(datosUiUnJugador);
                                    }
                                } else {
                                    // Tenemos dos nombres distintos: jugadorActualCliente y otroJugadorInfo
                                    if (!partidasActivas.containsKey(idSalaNuevoJugador)) {
                                        Partida nuevaPartida = Partida.crearJuego(idSalaNuevoJugador, jugadorActualCliente, otroJugadorInfo, DIMENSION_TABLERO_DEFAULT);
                                        partidasActivas.put(idSalaNuevoJugador, nuevaPartida);
                                        System.out.println("    [EDT] MODELO Partida '" + idSalaNuevoJugador + "' CREADA (NUEVO_JUGADOR_EN_SALA) con: " + jugadorActualCliente + " y " + otroJugadorInfo);
                                    } else {
                                        System.out.println("    [EDT] MODELO Partida '" + idSalaNuevoJugador + "' ya existía (verificado en NUEVO_JUGADOR_EN_SALA).");
                                    }
                                    if (delegadoEspera != null) {
                                        Map<String, Object> datosUiAmbosJugadores = new HashMap<>(finalDatos);
                                        datosUiAmbosJugadores.put("jugadores", List.of(jugadorActualCliente, otroJugadorInfo));
                                        delegadoEspera.procesarActualizacionSala(datosUiAmbosJugadores);
                                    }
                                }
                            } else {
                                System.err.println("    [EDT] NUEVO_JUGADOR_EN_SALA: Datos insuficientes (idSala, jugadorInfo, o nombreUsuarioRegistrado es null).");
                            }
                            break;
                        case "ACTUALIZACION_SALA":
                            
                               System.out.println("    [EDT] Procesando ACTUALIZACION_SALA...");
                            if (delegadoEspera != null) { // delegadoEspera ya fue chequeado por esParaEspera
                                delegadoEspera.procesarActualizacionSala(finalDatos); // UI Update

                                String idSalaActualizacion = (String) finalDatos.get("idSala");
                                if (idSalaActualizacion == null && delegadoEspera.getIdSala() != null) {
                                     idSalaActualizacion = delegadoEspera.getIdSala();
                                }

                                Object jugadoresObj = finalDatos.get("jugadores");
                                if (idSalaActualizacion != null && jugadoresObj instanceof List) {
                                    List<String> nombresJugadores = (List<String>) jugadoresObj;
                                    System.out.println("    [EDT] ACTUALIZACION_SALA para '" + idSalaActualizacion + "'. Jugadores en datos: " + nombresJugadores);
                                    if (nombresJugadores.size() == 2) {
                                        if (!partidasActivas.containsKey(idSalaActualizacion)) {
                                            String nombreJ1 = nombresJugadores.get(0);
                                            String nombreJ2 = nombresJugadores.get(1);
                                            if (nombreJ1 != null && !nombreJ1.isBlank() && nombreJ2 != null && !nombreJ2.isBlank() && !nombreJ1.equals(nombreJ2)) {
                                                Partida nuevaPartida = Partida.crearJuego(idSalaActualizacion, nombreJ1, nombreJ2, DIMENSION_TABLERO_DEFAULT);
                                                partidasActivas.put(idSalaActualizacion, nuevaPartida);
                                                System.out.println("    [EDT] MODELO Partida '" + idSalaActualizacion + "' CREADA (desde ACTUALIZACION_SALA) con: " + nombreJ1 + ", " + nombreJ2);
                                            } else {
                                                 System.err.println("    [EDT] ACTUALIZACION_SALA: Nombres de jugador inválidos/duplicados. No se crea Partida. Nombres: " + nombresJugadores);
                                            }
                                        } else {
                                            System.out.println("    [EDT] MODELO Partida '" + idSalaActualizacion + "' ya existía (verificado por ACTUALIZACION_SALA).");
                                        }
                                    } else {
                                         System.out.println("    [EDT] ACTUALIZACION_SALA: No hay 2 jugadores distintos aún (" + nombresJugadores.size() + ") para formalizar Partida modelo.");
                                    }
                                } else {
                                     System.out.println("    [EDT] ACTUALIZACION_SALA: Datos insuficientes para crear/verificar Partida (idSala o lista de jugadores).");
                                }
                            }
                            break;
                        case "OPONENTE_LISTO": // Puede ser útil para UI de espera de colocación
                        case "AMBOS_LISTOS":   // Puede ser útil para UI de espera de colocación
                            delegadoEspera.procesarActualizacionSala(finalDatos);
                            // La lógica de crear Partida entidad que tenías aquí para NUEVO_JUGADOR_EN_SALA
                            // y ACTUALIZACION_SALA es buena si necesitas el modelo local antes de INICIAR_COMBATE.
                            // Si INICIAR_COMBATE siempre envía el PartidaDTO completo, esa creación local
                            // podría simplificarse o hacerse solo cuando llega el DTO completo.
                            // Por ahora, mantendré tu lógica original de creación de Partida en el cliente para estos eventos.
                            // (Tu lógica de creación de Partida en NUEVO_JUGADOR_EN_SALA y ACTUALIZACION_SALA va aquí)
                            String idSalaEvt = (String) finalDatos.get("idSala");
                             if (idSalaEvt == null && delegadoEspera.getIdSala() != null) idSalaEvt = delegadoEspera.getIdSala();

                             if (idSalaEvt != null) {
                                 Object jugadoresObj = finalDatos.get("jugadores"); // Si el servidor envía esto
                                 List<String> nombresJugadores = null;
                                 if (jugadoresObj instanceof List) {
                                     try { nombresJugadores = (List<String>) jugadoresObj; } catch (ClassCastException e) {}
                                 }
                                 if (nombresJugadores == null && finalTipo.equals("NUEVO_JUGADOR_EN_SALA")) {
                                     // Reconstruir lista de jugadores para el modelo local
                                     String jugadorInfo = (String) finalDatos.get("jugadorInfo");
                                     if (this.nombreUsuarioRegistrado != null && jugadorInfo != null && !this.nombreUsuarioRegistrado.equals(jugadorInfo)) {
                                         nombresJugadores = List.of(this.nombreUsuarioRegistrado, jugadorInfo);
                                     } else if (this.nombreUsuarioRegistrado != null) {
                                         nombresJugadores = List.of(this.nombreUsuarioRegistrado);
                                     }
                                 }

                                 if (nombresJugadores != null && nombresJugadores.size() == 2) {
                                     if (!partidasActivas.containsKey(idSalaEvt)) {
                                         Partida pExistente = getPartidaActual(idSalaEvt);
                                         if (pExistente == null){
                                            Partida nuevaPartida = Partida.crearJuego(idSalaEvt, nombresJugadores.get(0), nombresJugadores.get(1), DIMENSION_TABLERO_DEFAULT);
                                            partidasActivas.put(idSalaEvt, nuevaPartida);
                                            System.out.println("    [EDT] MODELO Partida '" + idSalaEvt + "' CREADA ("+finalTipo+") con: " + nombresJugadores.get(0) + " y " + nombresJugadores.get(1));
                                         } else {
                                             System.out.println("    [EDT] MODELO Partida '" + idSalaEvt + "' YA EXISTÍA. ("+finalTipo+")");
                                         }
                                     }
                                 }
                             }
                            break;
                        case "INICIAR_COLOCACION":
                              System.out.println("    [EDT] Procesando INICIAR_COLOCACION...");
                            String idSalaColocacion = (String) finalDatos.get("idSala");
                            Partida p = getPartidaActual(idSalaColocacion);

                            if (p == null || p.getJugador1() == null || p.getJugador2() == null) {
                                System.err.println("    [EDT] ERROR CRÍTICO: INICIAR_COLOCACION para sala '" + idSalaColocacion + "' pero Partida (" + p + ") o Jugadores no están listos en el modelo.");
                                System.err.println("    Causa probable: Un evento anterior (NUEVO_JUGADOR_EN_SALA o ACTUALIZACION_SALA) no creó/completó la Partida en 'partidasActivas' con ambos jugadores.");
                                if (delegadoEspera != null) {
                                    delegadoEspera.procesarErrorSala(Map.of("error", "Error de sincronización de partida interna (faltan datos de jugadores)."));
                                }
                                return;
                            }
                            System.out.println("    [EDT] Partida para INICIAR_COLOCACION encontrada. J1: " + p.getJugador1().getNombre() + ", J2: " + p.getJugador2().getNombre());
                            delegadoEspera.procesarInicioColocacion(finalDatos);
                            break;
                        // Los eventos de "IniciarCombateKS" llegan aquí si delegadoEspera es el activo
                        case "ESPERANDO_OPONENTE_FLOTA":
                            System.out.println("   [EDT] Recibido ESPERANDO_OPONENTE_FLOTA.");
                            if (delegadoEspera != null) { // O el controlador de la pantalla de colocación
                                delegadoEspera.mostrarMensajeEsperaFlota((String)finalDatos.get("mensaje"));
                            }
                            break;
                        case "OPONENTE_CONFIRMO_FLOTA":
                             System.out.println("   [EDT] Recibido OPONENTE_CONFIRMO_FLOTA.");
                             if (delegadoEspera != null) { // O el controlador de la pantalla de colocación
                                delegadoEspera.mostrarMensajeOponenteListo((String)finalDatos.get("mensaje"));
                            }
                            break;
                        case "ERROR_CONFIRMAR_FLOTA":
                            System.err.println("   [EDT] Recibido ERROR_CONFIRMAR_FLOTA: " + finalDatos.get("error"));
                            if (delegadoEspera != null) { // O el controlador de la pantalla de colocación
                                delegadoEspera.mostrarErrorColocacion((String)finalDatos.get("error"));
                            }
                            break;
                        // ... (tus otros cases para delegadoEspera: INICIAR_PARTIDA (obsoleto aquí?), OPONENTE_SALIO, ERROR_SALA)
                        case "INICIAR_PARTIDA": // Este probablemente será reemplazado por INICIAR_COMBATE
                            System.out.println("   [EDT] ADVERTENCIA: Evento INICIAR_PARTIDA recibido, pero INICIAR_COMBATE es el preferido.");
                            if (delegadoEspera != null) {
                                delegadoEspera.procesarInicioPartida(finalDatos); // Podría llamar a la lógica de INICIAR_COMBATE
                                clearControladorEsperaActual();
                            }
                            break;
                         case "OPONENTE_SALIO":
                             if (delegadoEspera != null) delegadoEspera.procesarSalidaOponente(finalDatos);
                             eliminarPartida((String) finalDatos.get("idSala"));
                             clearControladorEsperaActual();
                             break;
                         case "ERROR_SALA":
                             if (delegadoEspera != null) delegadoEspera.procesarErrorSala(finalDatos);
                             eliminarPartida((String) finalDatos.get("idSala"));
                             clearControladorEsperaActual();
                             break;
                        default:
                            System.out.println("   [EDT] Tipo de Espera/Colocacion [" + finalTipo + "] no reconocido en switch interno.");
                            break;
                    }
                } else if (delegadoCrearUnir != null && esParaCrearUnir) {
                    // ... (tu lógica existente para delegadoCrearUnir: procesarRespuestaCrearSala, procesarRespuestaUnirseSala)
                    // Asegúrate que tu lógica de UNIDO_OK para el retador también cree la Partida local si es necesario.
                     boolean exito = !(finalTipo.startsWith("ERROR_"));
                     System.out.println("    [EDT] Evento para Crear/Unir (" + finalTipo + ") detectado.");
                     if (finalTipo.equals("UNIDO_OK") && exito) {
                         String idSalaUnida = (String) finalDatos.get("idSala");
                         String nombreOponenteDelServidor = (String) finalDatos.get("nombreOponente");
                         String miNombreComoRetador = this.nombreUsuarioRegistrado;

                         if (idSalaUnida != null && nombreOponenteDelServidor != null && miNombreComoRetador != null &&
                             !miNombreComoRetador.equals(nombreOponenteDelServidor)) {
                             if (!partidasActivas.containsKey(idSalaUnida)) {
                                 Partida pExistente = getPartidaActual(idSalaUnida);
                                 if(pExistente == null) {
                                    Partida nuevaPartida = Partida.crearJuego(idSalaUnida, nombreOponenteDelServidor, miNombreComoRetador, DIMENSION_TABLERO_DEFAULT);
                                    partidasActivas.put(idSalaUnida, nuevaPartida);
                                    System.out.println("    [EDT] MODELO Partida '" + idSalaUnida + "' CREADA (desde UNIDO_OK por retador) con J1=" + nombreOponenteDelServidor + ", J2=" + miNombreComoRetador);
                                 } else {
                                      System.out.println("    [EDT] MODELO Partida '" + idSalaUnida + "' YA EXISTÍA (UNIDO_OK).");
                                 }
                             }
                         }
                     }
                     if (finalTipo.contains("CREAR_SALA")) {
                         delegadoCrearUnir.procesarRespuestaCrearSala(exito, finalDatos);
                     } else if (finalTipo.equals("UNIDO_OK") || finalTipo.equals("ERROR_UNIRSE_SALA") || finalTipo.contains("UNIRSE_SALA")) {
                         delegadoCrearUnir.procesarRespuestaUnirseSala(exito, finalDatos);
                     } else {
                         System.out.println("    [EDT] Tipo de Crear/Unir [" + finalTipo + "] no reconocido específicamente para delegación.");
                     }

                } else if (esRegistro) {
                    // ... (tu lógica existente para REGISTRO_OK y ERROR_REGISTRO) ...
                     if (finalTipo.equals("REGISTRO_OK")) {
                         this.nombreUsuarioRegistrado = (String) finalDatos.get("nombre");
                         this.nombreUsuarioPendiente = null;
                         if (vistaInicio != null && vistaInicio.isDisplayable()) {
                             vistaInicio.navegarASiguientePantalla();
                         }
                     } else { // ERROR_REGISTRO
                         this.nombreUsuarioPendiente = null;
                         if (vistaInicio != null) {
                             vistaInicio.mostrarError("Error de Registro: " + finalDatos.get("error"));
                             vistaInicio.reactivarBotonPlay();
                         }
                         solicitarDesconexion();
                     }
                } else {
                    System.out.println("   [EDT] Mensaje tipo '" + finalTipo + "' NO MANEJADO o delegado es null.");
                }
            } catch (Throwable t) {
                // ... (tu manejo de errores Throwable existente) ...
                 System.err.println("    [EDT] CATCH Throwable durante procesamiento de " + finalTipo + ": " + t.getMessage());
                 t.printStackTrace();
                 Component vistaActiva = null;
                 if (vistaPartidaEspera != null && vistaPartidaEspera.isDisplayable()) vistaActiva = vistaPartidaEspera;
                 else if (vistaUnirseJugar != null && vistaUnirseJugar.isDisplayable()) vistaActiva = vistaUnirseJugar;
                 else vistaActiva = vistaInicio;
                 final String errorMsg = "Error interno procesando respuesta: " + t.getMessage();
                 if (vistaActiva instanceof PartidaEspera) { ((PartidaEspera)vistaActiva).mostrarError(errorMsg, true); }
                 else if (vistaActiva instanceof UnirseJugar) { ((UnirseJugar)vistaActiva).mostrarError(errorMsg); ((UnirseJugar)vistaActiva).reactivarBotones(); }
                 else if (vistaActiva instanceof PantallaInicio) { ((PantallaInicio)vistaActiva).mostrarError(errorMsg); ((PantallaInicio)vistaActiva).reactivarBotonPlay(); }
                 else { JOptionPane.showMessageDialog(null, errorMsg, "Error Crítico", JOptionPane.ERROR_MESSAGE); }
            } finally {
                System.out.println("<<< [EDT] Task END for " + finalTipo);
            }
        }); // Fin invokeLater
    } // Fin onMensajeServidor

    // --- Métodos privados Helper ---
    private boolean esMensajeParaCrearUnir(String tipo) {
        return tipo != null && (tipo.startsWith("SALA_CREADA") || tipo.startsWith("ERROR_CREAR_SALA") || // Abarca SALA_CREADA_OK
                                tipo.startsWith("UNIDO_") || tipo.startsWith("ERROR_UNIRSE_SALA"));    // Abarca UNIDO_OK
    }

    
    private boolean esMensajeParaEsperaOColocacion(String tipo) { // Renombrado y ampliado
        return tipo != null && (
            tipo.equals("ACTUALIZACION_SALA") || tipo.equals("OPONENTE_LISTO") || // Ya los tenías
            tipo.equals("AMBOS_LISTOS") || tipo.equals("INICIAR_COLOCACION") ||
            tipo.equals("OPONENTE_SALIO") || tipo.equals("ERROR_SALA") ||
            // Nuevos eventos de IniciarCombateKS (o la KS que maneja JUGADOR_FLOTA_LISTA)
            tipo.equals("ESPERANDO_OPONENTE_FLOTA") ||
            tipo.equals("OPONENTE_CONFIRMO_FLOTA") ||
            tipo.equals("ERROR_CONFIRMAR_FLOTA")
            // INICIAR_COMBATE se manejará como un caso especial para transición
        );
    }
    
     private boolean esEventoDeJuegoEnCurso(String tipo) {
        return tipo != null && (
                tipo.equals("INICIAR_COMBATE")  // <<<--- AÑADIDO AQUÍ
               
        );
    }
    
    private boolean esMensajeParaEspera(String tipo) {
        return tipo != null && (
                tipo.equals("NUEVO_JUGADOR_EN_SALA") || // <<<--- AÑADIDO AQUÍ
                tipo.equals("ACTUALIZACION_SALA") ||
                tipo.equals("OPONENTE_LISTO") ||
                tipo.equals("AMBOS_LISTOS") ||
                tipo.equals("INICIAR_COLOCACION") ||
                tipo.equals("INICIAR_PARTIDA") ||
                tipo.equals("OPONENTE_SALIO") ||
                tipo.equals("ERROR_SALA")
        );
    }
}