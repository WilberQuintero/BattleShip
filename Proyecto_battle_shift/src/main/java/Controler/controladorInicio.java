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
import View.PantallaPartida;
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
            // Si idSala es null, intenta obtener la del delegadoEspera si está activo Y TIENE SALA ASIGNADA
             if (this.controladorEsperaActual != null && this.controladorEsperaActual.getIdSala() != null) {
                 return partidasActivas.get(this.controladorEsperaActual.getIdSala());
             } else if (this.controladorDePartidaActual != null && this.controladorDePartidaActual.getPartidaActual() != null) {
                 // Si estamos en una partida activa, obtenerla de su controlador
                 return this.controladorDePartidaActual.getPartidaActual(); // Necesitarás un getter en controladorPartida
             }
            System.err.println("CONTROLLER [Inicio] WARN (getPartidaActual): idSala es null y no hay delegado de espera/partida activo con sala.");
            return null;
        }
        return partidasActivas.get(idSala);
    }

    public void setControladorDePartidaActual(controladorPartida ctrlPartida) {
        this.controladorDePartidaActual = ctrlPartida;
    }

    public void limpiarControladorDePartidaActual() {
        if (this.controladorDePartidaActual != null) {
            if (this.controladorDePartidaActual.getVistaPartida() != null && 
                this.controladorDePartidaActual.getVistaPartida().isDisplayable()) {
                this.controladorDePartidaActual.getVistaPartida().dispose();
            }
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
        limpiarControladorDePartidaActual(); // También limpiar controlador de partida si se vuelve a esta fase
        this.controladorCrearPartidaActual = controlador;
    }

    public void clearControladorCrearPartidaActual() {
        if (this.controladorCrearPartidaActual != null) {
            System.out.println("CONTROLLER [Inicio]: Limpiando controladorCrearPartidaActual.");
            // Si la vista de crear/unir tiene un método para cerrarse si el controlador se limpia:
            // if (this.controladorCrearPartidaActual.getVista() != null && this.controladorCrearPartidaActual.getVista().isDisplayable()) {
            // this.controladorCrearPartidaActual.getVista().dispose();
            // }
            this.controladorCrearPartidaActual = null;
        }
    }

    public void setControladorEsperaActual(controladorPartidaEspera controlador) {
        System.out.println("CONTROLLER [Inicio]: Estableciendo controladorEsperaActual: " + (controlador != null ? controlador.getClass().getSimpleName() : "null"));
        clearControladorCrearPartidaActual();
        limpiarControladorDePartidaActual();
        this.controladorEsperaActual = controlador;
    }

    public void clearControladorEsperaActual() {
        if (this.controladorEsperaActual != null) {
            System.out.println("CONTROLLER [Inicio]: Limpiando controladorEsperaActual.");
            // if (this.controladorEsperaActual.getVista() != null && this.controladorEsperaActual.getVista().isDisplayable()) {
            // this.controladorEsperaActual.getVista().dispose();
            // }
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
            // No llamar a solicitarDesconexion() aquí automáticamente, podría causar bucles si la conexión falla repetidamente.
            // El listener de ServerComunicacion (onError o onDesconectado) manejará la limpieza si la conexión se pierde.
        }
    }

    @Override
    public void onDesconectado(String motivo) {
        System.out.println("CONTROLLER [Inicio]: DESCONECTADO. Motivo: " + motivo);
        clearControladorCrearPartidaActual();
        clearControladorEsperaActual();
        limpiarControladorDePartidaActual();

        nombreUsuarioPendiente = null;
        nombreUsuarioRegistrado = null;
        partidasActivas.clear();
        System.out.println("CONTROLLER [Inicio]: Mapa de partidas activas limpiado tras desconexión.");

        SwingUtilities.invokeLater(() -> {
            if (this.vistaUnirseJugar != null && this.vistaUnirseJugar.isDisplayable()) { this.vistaUnirseJugar.dispose(); this.vistaUnirseJugar = null; }
            if (this.vistaPartidaEspera != null && this.vistaPartidaEspera.isDisplayable()) { this.vistaPartidaEspera.dispose(); this.vistaPartidaEspera = null; }
            // La vista de partida (PantallaPartida) es cerrada por limpiarControladorDePartidaActual()

            if (this.vistaInicio != null) {
                if (!this.vistaInicio.isDisplayable()) this.vistaInicio.setVisible(true);
                this.vistaInicio.mostrarEstadoDesconectado(motivo);
                this.vistaInicio.reactivarBotonPlay();
            } else {
                 JOptionPane.showMessageDialog(null, "Desconectado: " + motivo + "\nPor favor, reinicie la aplicación.", "Desconexión", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    @Override
    public void onError(String mensajeError) {
        System.err.println("CONTROLLER [Inicio]: ERROR DE COMUNICACIÓN recibido: " + mensajeError);
        // Un error de comunicación a menudo implica una desconexión o un fallo al conectar.
        // No necesariamente limpiamos nombreUsuarioRegistrado aquí, a menos que el error sea "no se pudo conectar".
        // Si el error es durante una partida, el controladorDePartidaActual debería manejarlo.
        SwingUtilities.invokeLater(() -> {
            Component vistaActiva = null;
            if (controladorDePartidaActual != null && controladorDePartidaActual.getVistaPartida() != null && controladorDePartidaActual.getVistaPartida().isDisplayable()) {
                vistaActiva = controladorDePartidaActual.getVistaPartida();
            } else if (vistaPartidaEspera != null && vistaPartidaEspera.isDisplayable()) {
                vistaActiva = vistaPartidaEspera;
            } else if (vistaUnirseJugar != null && vistaUnirseJugar.isDisplayable()) {
                vistaActiva = vistaUnirseJugar;
            } else {
                vistaActiva = vistaInicio;
            }

            if (vistaActiva instanceof PantallaPartida) {
                 ((PantallaPartida)vistaActiva).mostrarError("Error de comunicación: " + mensajeError, true); // true para error crítico en partida
            } else if (vistaActiva instanceof PartidaEspera) {
                ((PartidaEspera)vistaActiva).volverAPantallaAnterior("Error de comunicación: " + mensajeError);
            } else if (vistaActiva instanceof UnirseJugar) {
                ((UnirseJugar)vistaActiva).mostrarError("Error de comunicación: " + mensajeError);
                ((UnirseJugar)vistaActiva).reactivarBotones();
            } else if (vistaActiva instanceof PantallaInicio) {
                ((PantallaInicio)vistaActiva).mostrarError("Error de comunicación: " + mensajeError);
                ((PantallaInicio)vistaActiva).reactivarBotonPlay();
                 nombreUsuarioPendiente = null; // Resetear si falla la conexión/registro desde PantallaInicio
            } else {
                JOptionPane.showMessageDialog(null, "Error de comunicación: " + mensajeError, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
         // Si el error implica desconexión, ServerComunicacion debería llamar a onDesconectado eventualmente.
    }

    // --- HELPER PARA DETERMINAR QUÉ TIPO DE EVENTO ES ---
    private boolean esEventoDeJuegoEnCurso(String tipo) {
        return tipo != null && (
                tipo.equals("RESULTADO_DISPARO") ||
                tipo.equals("ERROR_DISPARO") ||
                tipo.equals("CAMBIO_DE_TURNO") || // Si el servidor envía este evento explícitamente
                tipo.equals("FIN_PARTIDA") ||
                tipo.equals("OPONENTE_ABANDONO_PARTIDA") // Un nuevo evento que podrías necesitar
        );
    }

    private boolean esMensajeParaEspera(String tipo) {
        return tipo != null && (
                tipo.equals("NUEVO_JUGADOR_EN_SALA") ||
                tipo.equals("ACTUALIZACION_SALA") ||
                tipo.equals("OPONENTE_LISTO") ||
                tipo.equals("AMBOS_LISTOS") ||
                tipo.equals("INICIAR_COLOCACION") ||
                // INICIAR_PARTIDA es obsoleto si INICIAR_COMBATE lo reemplaza
                tipo.equals("OPONENTE_SALIO") || // Oponente sale de la sala de espera
                tipo.equals("ERROR_SALA") ||
                // Eventos de la fase de colocación que son manejados por el delegado de espera/colocación
                tipo.equals("ESPERANDO_OPONENTE_FLOTA") ||
                tipo.equals("OPONENTE_CONFIRMO_FLOTA") ||
                tipo.equals("ERROR_CONFIRMAR_FLOTA")
        );
    }

    private boolean esMensajeParaCrearUnir(String tipo) {
        return tipo != null && (
                tipo.startsWith("SALA_CREADA") || // Abarca SALA_CREADA_OK
                tipo.startsWith("ERROR_CREAR_SALA") ||
                tipo.startsWith("UNIDO_") || // Abarca UNIDO_OK
                tipo.startsWith("ERROR_UNIRSE_SALA")
        );
    }
    // --- FIN HELPERS ---

    @Override
    @SuppressWarnings("unchecked") // Para el casteo de List<String>
    public void onMensajeServidor(String tipo, Map<String, Object> datos) {
      System.out.println("CONTROLLER [Inicio]: Mensaje del servidor recibido - Tipo: " + tipo + ", Datos: " + datos);

        final String finalTipo = tipo;
        final Map<String, Object> finalDatos = datos;

        SwingUtilities.invokeLater(() -> {
            System.out.println(">>> [EDT] Task START for " + finalTipo + 
                               " | controladorDePartidaActual: " + (this.controladorDePartidaActual != null) +
                               " | controladorEsperaActual: " + (this.controladorEsperaActual != null) +
                               " | controladorCrearPartidaActual: " + (this.controladorCrearPartidaActual != null));
            try {
                // --- PRIORIDAD 1: Evento de transición a la partida ---
                if (finalTipo.equals("INICIAR_COMBATE")) {
                    System.out.println("    [EDT] Procesando INICIAR_COMBATE...");
                    String idSalaCombate = (String) finalDatos.get("idSala");
                    String partidaJsonBase64 = (String) finalDatos.get("partidaJsonBase64");

                    if (partidaJsonBase64 != null && idSalaCombate != null) {
                        try {
                            byte[] decodedBytes = Base64.getDecoder().decode(partidaJsonBase64);
                            String partidaJson = new String(decodedBytes, StandardCharsets.UTF_8);
                            Gson gson = new Gson();
                            PartidaDTO partidaDTORecibida = gson.fromJson(partidaJson, PartidaDTO.class);

                            System.out.println("CLIENTE " + this.nombreUsuarioRegistrado + 
                                               " - [controladorInicio] INICIAR_COMBATE: PartidaDTO deserializada. Sala: " + partidaDTORecibida.getIdPartida() +
                                               ", Estado DTO: " + partidaDTORecibida.getEstado() +
                                               ", Turno DTO: " + partidaDTORecibida.getNombreJugadorEnTurno());

                            Partida entidadPartidaCliente = ModelConverter.toPartidaEntity(partidaDTORecibida);
                            if (entidadPartidaCliente == null || entidadPartidaCliente.getJugador1() == null || entidadPartidaCliente.getJugador2() == null || entidadPartidaCliente.obtenerJugadorEnTurno() == null) {
                                 System.err.println("    [EDT] ERROR CRÍTICO en INICIAR_COMBATE: Partida entidad incompleta tras conversión. J1=" + (entidadPartidaCliente != null ? entidadPartidaCliente.getJugador1() : "partidaNull") + ", J2=" + (entidadPartidaCliente != null ? entidadPartidaCliente.getJugador2() : "partidaNull") + ", Turno=" + (entidadPartidaCliente != null ? entidadPartidaCliente.obtenerJugadorEnTurno() : "partidaNull"));
                                 // Notificar error
                                 return;
                            }
                            partidasActivas.put(idSalaCombate, entidadPartidaCliente); // Guardar la partida del modelo
                            System.out.println("    [EDT] Entidad Partida '" + idSalaCombate + "' CREADA/ACTUALIZADA en modelo cliente.");

                            if (controladorEsperaActual != null && controladorEsperaActual.getVista() != null && controladorEsperaActual.getVista().isDisplayable()) {
                                controladorEsperaActual.getVista().dispose();
                            }
                            if (controladorCrearPartidaActual != null && controladorCrearPartidaActual.getVista() != null && controladorCrearPartidaActual.getVista().isDisplayable()) {
                                controladorCrearPartidaActual.getVista().dispose();
                            }
                            clearControladorEsperaActual();
                            clearControladorCrearPartidaActual();
                            
                            System.out.println("    [EDT] Instanciando controladorPartida para la partida: " + entidadPartidaCliente.getIdPartida());
                            this.controladorDePartidaActual = new controladorPartida(
                                this.serverComunicacion,
                                entidadPartidaCliente,
                                this.nombreUsuarioRegistrado
                            );
                            System.out.println("    [EDT] controladorPartida instanciado.");
                        
                        } catch (Exception e) {
                            System.err.println("    [EDT] Error CRÍTICO al procesar datos de INICIAR_COMBATE: " + e.getMessage());
                            e.printStackTrace();
                            // Notificar error a la vista activa
                        }
                    } else {
                        System.err.println("    [EDT] Evento INICIAR_COMBATE no contenía datos de partidaJsonBase64 o idSala.");
                    }
                // --- PRIORIDAD 2: Eventos durante una partida activa ---
                } else if (this.controladorDePartidaActual != null && esEventoDeJuegoEnCurso(finalTipo)) {
                    System.out.println("    [EDT] Delegando evento de juego '" + finalTipo + "' a controladorDePartidaActual.");
                    switch(finalTipo) {
                        case "RESULTADO_DISPARO":
                            controladorDePartidaActual.procesarResultadoDisparo(finalDatos);
                            break;
                        case "ERROR_DISPARO":
                             System.err.println("    [EDT] ERROR_DISPARO recibido del servidor: " + finalDatos.get("error"));
                             if (controladorDePartidaActual.getVistaPartida() != null) {
                                 controladorDePartidaActual.getVistaPartida().mostrarError("Info del juego: " + finalDatos.get("error"), false);
                             }
                            break;
                        case "FIN_PARTIDA":
                             System.out.println("    [EDT] Evento FIN_PARTIDA recibido. Delegando a controladorDePartidaActual...");
                             controladorDePartidaActual.procesarFinDePartida(finalDatos);
                            // La limpieza de controladorDePartidaActual se hará desde procesarFinDePartida si es necesario
                            break;
                        // Añadir case "CAMBIO_DE_TURNO": si el servidor lo envía explícitamente y no solo con RESULTADO_DISPARO
                        // case "CAMBIO_DE_TURNO":
                        //     controladorDePartidaActual.procesarCambioDeTurno(finalDatos);
                        //     break;
                        default:
                            System.out.println("    [EDT] Evento de juego '" + finalTipo + "' sin case explícito en controladorInicio para controladorDePartidaActual.");
                            break;
                    }
                // --- PRIORIDAD 3: Eventos de fases de espera/colocación ---
                } else if (this.controladorEsperaActual != null && esMensajeParaEspera(finalTipo)) {
                    System.out.println("    [EDT] Procesando para delegadoEspera tipo " + finalTipo);
                    // (Tu switch existente para delegadoEspera)
                     switch (finalTipo) {
                        case "NUEVO_JUGADOR_EN_SALA":
                        case "ACTUALIZACION_SALA":
                             this.controladorEsperaActual.procesarActualizacionSala(finalDatos);
                             String idSalaEvt = (String) finalDatos.get("idSala");
                             if (idSalaEvt == null && this.controladorEsperaActual.getIdSala() != null) idSalaEvt = this.controladorEsperaActual.getIdSala();
                             Object jugadoresObj = finalDatos.get("jugadores");
                             List<String> nombresJugadores = null;
                             if (jugadoresObj instanceof List) {
                                 try { nombresJugadores = (List<String>) jugadoresObj; } catch (ClassCastException e) {}
                             } else if (finalTipo.equals("NUEVO_JUGADOR_EN_SALA") && this.nombreUsuarioRegistrado != null && finalDatos.get("jugadorInfo") instanceof String) {
                                 String otroJugador = (String)finalDatos.get("jugadorInfo");
                                 if(!this.nombreUsuarioRegistrado.equals(otroJugador)) { 
                                      nombresJugadores = List.of(this.nombreUsuarioRegistrado, otroJugador);
                                 } else { nombresJugadores = List.of(this.nombreUsuarioRegistrado); }
                             }
                             if (idSalaEvt != null && nombresJugadores != null && nombresJugadores.size() == 2) {
                                 if (!partidasActivas.containsKey(idSalaEvt)) {
                                     String n1 = nombresJugadores.get(0); String n2 = nombresJugadores.get(1);
                                     if (n1 != null && !n1.isBlank() && n2 != null && !n2.isBlank() && !n1.equals(n2)) {
                                         Partida nuevaP = Partida.crearJuego(idSalaEvt, n1, n2, DIMENSION_TABLERO_DEFAULT);
                                         partidasActivas.put(idSalaEvt, nuevaP);
                                         System.out.println("    [EDT] MODELO Partida '" + idSalaEvt + "' CREADA ("+finalTipo+") con: " + n1 + " y " + n2);
                                     }
                                 }
                             }
                             break;
                        case "INICIAR_COLOCACION":
                             Partida pCol = getPartidaActual((String) finalDatos.get("idSala"));
                             if (pCol == null || pCol.getJugador1() == null || pCol.getJugador2() == null) {
                                 System.err.println("    [EDT] ERROR INICIAR_COLOCACION: Partida no lista en modelo.");
                                 this.controladorEsperaActual.procesarErrorSala(Map.of("error", "Error interno al iniciar colocación."));
                                 return;
                             }
                             this.controladorEsperaActual.procesarInicioColocacion(finalDatos);
                             break;
                        case "OPONENTE_LISTO": 
                        case "AMBOS_LISTOS":
                             this.controladorEsperaActual.procesarActualizacionSala(finalDatos);
                             break;
                        case "ESPERANDO_OPONENTE_FLOTA":
                             this.controladorEsperaActual.mostrarMensajeEsperaFlota((String)finalDatos.get("mensaje"));
                             break;
                        case "OPONENTE_CONFIRMO_FLOTA":
                             this.controladorEsperaActual.mostrarMensajeOponenteListo((String)finalDatos.get("mensaje"));
                             break;
                        case "ERROR_CONFIRMAR_FLOTA":
                             this.controladorEsperaActual.mostrarErrorColocacion((String)finalDatos.get("error"));
                             break;
                        case "OPONENTE_SALIO":
                             this.controladorEsperaActual.procesarSalidaOponente(finalDatos);
                             eliminarPartida((String) finalDatos.get("idSala"));
                             clearControladorEsperaActual();
                             break;
                        case "ERROR_SALA":
                             this.controladorEsperaActual.procesarErrorSala(finalDatos);
                             eliminarPartida((String) finalDatos.get("idSala"));
                             clearControladorEsperaActual();
                             break;
                        default:
                             System.out.println("   [EDT] Tipo de Espera/Colocacion [" + finalTipo + "] no reconocido por switch interno para delegadoEspera.");
                             break;
                     }
                } else if (this.controladorCrearPartidaActual != null && esMensajeParaCrearUnir(finalTipo)) {
                    System.out.println("    [EDT] Procesando para delegadoCrearUnir tipo " + finalTipo);
                    boolean exito = !(finalTipo.startsWith("ERROR_"));
                     if (finalTipo.equals("UNIDO_OK") && exito) {
                         String idSalaUnida = (String) finalDatos.get("idSala");
                         String nombreOponenteDelServidor = (String) finalDatos.get("nombreOponente");
                         String miNombreComoRetador = this.nombreUsuarioRegistrado;
                         if (idSalaUnida != null && nombreOponenteDelServidor != null && miNombreComoRetador != null && !miNombreComoRetador.equals(nombreOponenteDelServidor)) {
                             if (!partidasActivas.containsKey(idSalaUnida)) {
                                 // Para UNIDO_OK, el orden de jugadores en Partida.crearJuego debería ser (anfitrión, retador)
                                 // anfitrión = nombreOponenteDelServidor, retador = miNombreComoRetador
                                 Partida nuevaPartida = Partida.crearJuego(idSalaUnida, nombreOponenteDelServidor, miNombreComoRetador, DIMENSION_TABLERO_DEFAULT);
                                 partidasActivas.put(idSalaUnida, nuevaPartida);
                                 System.out.println("    [EDT] MODELO Partida '" + idSalaUnida + "' CREADA (UNIDO_OK retador) con J1(Host)=" + nombreOponenteDelServidor + ", J2(Retador)=" + miNombreComoRetador);
                             }
                         }
                     }
                    if (finalTipo.contains("CREAR_SALA")) {
                        this.controladorCrearPartidaActual.procesarRespuestaCrearSala(exito, finalDatos);
                    } else if (finalTipo.equals("UNIDO_OK") || finalTipo.equals("ERROR_UNIRSE_SALA")) { // Ya no necesitas contains("UNIRSE_SALA")
                        this.controladorCrearPartidaActual.procesarRespuestaUnirseSala(exito, finalDatos);
                    } else {
                         System.out.println("    [EDT] Tipo de Crear/Unir [" + finalTipo + "] no reconocido específicamente para delegación.");
                    }
                } else if (finalTipo.equals("REGISTRO_OK") || finalTipo.equals("ERROR_REGISTRO")) {
                    if (finalTipo.equals("REGISTRO_OK")) {
                        this.nombreUsuarioRegistrado = (String) finalDatos.get("nombre");
                        this.nombreUsuarioPendiente = null;
                        if (vistaInicio != null && vistaInicio.isDisplayable()) {
                            vistaInicio.navegarASiguientePantalla();
                        }
                    } else { 
                        this.nombreUsuarioPendiente = null;
                        if (vistaInicio != null) {
                            vistaInicio.mostrarError("Error de Registro: " + finalDatos.get("error"));
                            vistaInicio.reactivarBotonPlay();
                        }
                    }
                } else {
                    System.out.println("    [EDT] Mensaje tipo '" + finalTipo + "' NO MANEJADO FINALMENTE o todos los delegados pertinentes son null.");
                }
            } catch (Throwable t) {
                System.err.println("    [EDT] CATCH Throwable GENERAL en onMensajeServidor procesando " + finalTipo + ": " + t.getMessage());
                t.printStackTrace();
                // Tu lógica de notificación de error global...
            } finally {
                System.out.println("<<< [EDT] Task END for " + finalTipo);
            }
        });
    }
    
     // Fin onMensajeServidor

    
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
    
    
    
   
}