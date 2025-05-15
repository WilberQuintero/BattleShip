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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Component;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

public class controladorInicio implements ServerEventListener {

    private final ServerComunicacion serverComunicacion;
    private PantallaInicio vistaInicio;
    private UnirseJugar vistaUnirseJugar;
    private PartidaEspera vistaPartidaEspera;
    private controladorCrearPartida controladorCrearPartidaActual;
    private controladorPartidaEspera controladorEsperaActual;
    private String nombreUsuarioPendiente = null;
    private String nombreUsuarioRegistrado = null;
    private Map<String, Partida> partidasActivas;
    private final int DIMENSION_TABLERO_DEFAULT = 10;

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
        return partidasActivas.get(idSala);
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

    @Override
    public void onDesconectado(String motivo) {
        System.out.println("CONTROLLER [Inicio]: DESCONECTADO. Motivo: " + motivo);
        clearControladorCrearPartidaActual();
        clearControladorEsperaActual();
        nombreUsuarioPendiente = null;
        nombreUsuarioRegistrado = null;
        partidasActivas.clear();
        System.out.println("CONTROLLER [Inicio]: Mapa de partidas activas limpiado tras desconexión.");
        SwingUtilities.invokeLater(() -> {
            if (vistaUnirseJugar != null && vistaUnirseJugar.isDisplayable()) { vistaUnirseJugar.dispose(); this.vistaUnirseJugar = null; }
            if (vistaPartidaEspera != null && vistaPartidaEspera.isDisplayable()) { vistaPartidaEspera.dispose(); this.vistaPartidaEspera = null; }
            if (vistaInicio != null) {
                if (!vistaInicio.isDisplayable()) vistaInicio.setVisible(true);
                vistaInicio.mostrarEstadoDesconectado(motivo);
                vistaInicio.reactivarBotonPlay();
            } else {
                System.out.println("CONTROLLER [Inicio][onDesconectado]: No hay vista de inicio para actualizar.");
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
        final String finalTipo = tipo;
        final Map<String, Object> finalDatos = datos;
        // >>> CORRECCIÓN: esParaEspera ahora incluye NUEVO_JUGADOR_EN_SALA <<<
        final boolean esParaEspera = esMensajeParaEspera(finalTipo);
        final boolean esParaCrearUnir = esMensajeParaCrearUnir(finalTipo);
        final boolean esRegistro = finalTipo.equals("REGISTRO_OK") || finalTipo.equals("ERROR_REGISTRO");

        SwingUtilities.invokeLater(() -> {
            System.out.println(">>> [EDT] Task START for " + finalTipo);
            try {
                if (delegadoEspera != null && esParaEspera) {
                    System.out.println("    [EDT] Procesando para delegado ESPERA tipo " + finalTipo);
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

                        case "OPONENTE_LISTO":
                        case "AMBOS_LISTOS":
                             if (delegadoEspera != null) delegadoEspera.procesarActualizacionSala(finalDatos);
                             break;
                        case "INICIAR_PARTIDA":
                             if (delegadoEspera != null) {
                                 delegadoEspera.procesarInicioPartida(finalDatos);
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
                            System.out.println("    [EDT] Tipo de Espera [" + finalTipo + "] no reconocido en switch interno.");
                            break;
                    }
} else if (delegadoCrearUnir != null && esParaCrearUnir) {
    boolean exito = !(finalTipo.startsWith("ERROR_"));
    System.out.println("    [EDT] Evento para Crear/Unir (" + finalTipo + ") detectado.");

    // >>> INICIO LÓGICA ADITIVA PARA CREAR PARTIDA en UNIDO_OK <<<
    if (finalTipo.equals("UNIDO_OK") && exito) {
        String idSalaUnida = (String) finalDatos.get("idSala");
        String nombreOponenteDelServidor = (String) finalDatos.get("nombreOponente");
        String miNombreComoRetador = this.nombreUsuarioRegistrado; // El que se está uniendo

        if (idSalaUnida != null && nombreOponenteDelServidor != null && miNombreComoRetador != null &&
            !miNombreComoRetador.equals(nombreOponenteDelServidor)) { // Asegurar que los nombres sean distintos

            if (!partidasActivas.containsKey(idSalaUnida)) {
                // El servidor indica el rol. Si soy RETADOR, el 'nombreOponenteDelServidor' es el ANFITRION (J1).
                // Tu Partida.crearJuego(id, nombreJ1, nombreJ2, dim)
                String nombreJ1 = nombreOponenteDelServidor; // El anfitrión
                String nombreJ2 = miNombreComoRetador;      // Yo, el retador

                Partida nuevaPartida = Partida.crearJuego(idSalaUnida, nombreJ1, nombreJ2, DIMENSION_TABLERO_DEFAULT);
                partidasActivas.put(idSalaUnida, nuevaPartida);
                System.out.println("    [EDT] MODELO Partida '" + idSalaUnida + "' CREADA (desde UNIDO_OK por retador) con J1=" + nombreJ1 + ", J2=" + nombreJ2);
            } else {
                System.out.println("    [EDT] MODELO Partida '" + idSalaUnida + "' ya existía (verificado en UNIDO_OK del retador).");
                // Aquí se podría actualizar el estado de la partida si es necesario,
                // por ejemplo, si el anfitrión la creó solo con J1 y ahora se añade J2.
                // Pero tu Partida.crearJuego crea con ambos.
            }
        } else {
            System.err.println("    [EDT] UNIDO_OK (retador): Datos insuficientes o nombres iguales para crear Partida. MiNombre: " + miNombreComoRetador + ", Oponente: " + nombreOponenteDelServidor);
        }
    }
    // >>> FIN LÓGICA ADITIVA <<<

    // Delegar a controladorCrearPartida para la navegación y otros procesamientos de UI
    if (finalTipo.contains("CREAR_SALA")) { 
        delegadoCrearUnir.procesarRespuestaCrearSala(exito, finalDatos);
    } else if (finalTipo.equals("UNIDO_OK") || finalTipo.equals("ERROR_UNIRSE_SALA") || finalTipo.contains("UNIRSE_SALA")) {
        delegadoCrearUnir.procesarRespuestaUnirseSala(exito, finalDatos);
    } else {
        System.out.println("    [EDT] Tipo de Crear/Unir [" + finalTipo + "] no reconocido específicamente para delegación.");
    }
                } else if (esRegistro) {
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
                    System.out.println("    [EDT] Mensaje tipo '" + finalTipo + "' NO MANEJADO o delegado es null.");
                }
            } catch (Throwable t) {
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
        });
    }

    // --- Métodos privados Helper ---
    private boolean esMensajeParaCrearUnir(String tipo) {
        return tipo != null && (tipo.startsWith("SALA_CREADA") || tipo.startsWith("ERROR_CREAR_SALA") || // Abarca SALA_CREADA_OK
                                tipo.startsWith("UNIDO_") || tipo.startsWith("ERROR_UNIRSE_SALA"));    // Abarca UNIDO_OK
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