/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controler;

// --- Imports ---

import Model.entidades.Partida;
import View.PantallaInicio;
import View.UnirseJugar;
import View.PartidaEspera; // Necesario si se maneja su cierre aquí
import com.mycompany.servercomunicacion.ServerComunicacion;
import com.mycompany.servercomunicacion.ServerEventListener;
// Utilidades
import java.awt.Component; // Para saber qué vista está activa en onError
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities; // Para actualizaciones de UI seguras
import javax.swing.JOptionPane;   // Para mostrar mensajes simples

/**
 * Controlador principal para el flujo inicial de la aplicación cliente.
 * Responsable de manejar la conexión inicial y el registro del usuario.
 * Actúa como el listener principal para ServerComunicacion y delega
 * eventos específicos a otros controladores (controladorCrearPartida, controladorPartidaEspera).
 */
public class controladorInicio implements ServerEventListener {
    // --- Atributos ---
    private final ServerComunicacion serverComunicacion;
    private PantallaInicio vistaInicio;
    private UnirseJugar vistaUnirseJugar;
    private PartidaEspera vistaPartidaEspera;

    private controladorCrearPartida controladorCrearPartidaActual;
    private controladorPartidaEspera controladorEsperaActual;

    private String nombreUsuarioPendiente = null;
    private String nombreUsuarioRegistrado = null;

    // >>> NUEVO ATRIBUTO PARA GESTIONAR PARTIDAS ACTIVAS <<<
    private Map<String, Partida> partidasActivas;
    private final int DIMENSION_TABLERO_DEFAULT = 10; // Para crear Jugadores y Partidas

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
        
        // >>> INICIALIZAR EL MAPA DE PARTIDAS <<<
        this.partidasActivas = new HashMap<>();
        System.out.println("CONTROLLER [Inicio]: Mapa de partidas activas inicializado.");
    }
    
    /**
     * CORRECCIÓN: Devuelve esta misma instancia.
     */
    public controladorInicio getControladorPrincipal() {
        return this; // Devuelve la instancia actual
    }

    // --- Setters para Vistas ---
    public void setVistaInicio(PantallaInicio vista) { this.vistaInicio = vista; }
    public void setVistaUnirseJugar(UnirseJugar vista) { this.vistaUnirseJugar = vista; }
    public void setVistaPartidaEspera(PartidaEspera vista) { this.vistaPartidaEspera = vista; }

    // --- Getters ---
    public ServerComunicacion getServerComunicacion() { return serverComunicacion; }
    public String getNombreUsuarioRegistrado() { return nombreUsuarioRegistrado; }

    // --- Métodos para gestionar Partidas (NUEVOS/MODIFICADOS) ---

    /**
     * Obtiene una partida activa por su ID.
     * @param idSala ID de la sala/partida.
     * @return la Partida si existe, o null.
     */
    public Partida getPartidaActual(String idSala) {
        return partidasActivas.get(idSala);
    }

    /**
     * Elimina una partida del seguimiento. Útil cuando una partida termina o un jugador sale.
     * @param idSala ID de la sala/partida a eliminar.
     */
    public void eliminarPartida(String idSala) {
        Partida p = partidasActivas.remove(idSala);
        if (p != null) {
            System.out.println("CONTROLLER [Inicio]: Partida eliminada de la gestión interna: " + idSala);
        }
    }

    /**
     * Inicia el proceso de conexión al servidor y guarda el nombre para registrarlo después.
     */
    public void intentarConectarYRegistrar(String nombreUsuario) {
        System.out.println("CONTROLLER [Inicio]: Solicitud para conectar y registrar usuario: " + nombreUsuario);
        if (serverComunicacion == null) {
             if (vistaInicio != null) vistaInicio.mostrarError("Error interno: Comunicación no disponible.");
             return;
        }
        if (serverComunicacion.isConectado()) {
             System.out.println("CONTROLLER [Inicio]: Ya conectado.");
             if (this.nombreUsuarioRegistrado == null) { // Si conectado pero no registrado
                  System.out.println("CONTROLLER [Inicio]: Intentando registrar nombre: " + nombreUsuario);
                  this.nombreUsuarioPendiente = nombreUsuario;
                   if (nombreUsuarioPendiente != null) { // Enviar registro directamente
                       serverComunicacion.enviarRegistroUsuario(nombreUsuarioPendiente);
                   }
             } else { // Ya conectado y registrado
                  System.out.println("CONTROLLER [Inicio]: Ya conectado y registrado como " + this.nombreUsuarioRegistrado);
                  if (vistaInicio != null) vistaInicio.navegarASiguientePantalla(); // Navegar directamente
             }
             return;
        }
        // Iniciar conexión si no está conectado
        this.nombreUsuarioPendiente = nombreUsuario;
        System.out.println("CONTROLLER [Inicio]: Nombre guardado. Solicitando conexión...");
        serverComunicacion.conectar();
    }

    /**
     * Solicita el cierre de la conexión.
     */
     public void solicitarDesconexion() {
         System.out.println("CONTROLLER [Inicio]: Solicitando desconexión...");
         if (serverComunicacion != null) {
              serverComunicacion.desconectar(); // El callback onDesconectado manejará la limpieza
         }
     }

    // --- Gestión de Controladores Secundarios ---

    /** Guarda referencia al controlador de Crear/Unir activo */
    public void setControladorCrearPartidaActual(controladorCrearPartida controlador) {
         System.out.println("CONTROLLER [Inicio]: Estableciendo controladorCrearPartidaActual: " + (controlador != null ? controlador.getClass().getSimpleName() : "null"));
         clearControladorEsperaActual(); // Asegura que solo uno esté activo
         this.controladorCrearPartidaActual = controlador;
    }
    /** Limpia la referencia al controlador de Crear/Unir */
    public void clearControladorCrearPartidaActual() {
         if (this.controladorCrearPartidaActual != null) {
              System.out.println("CONTROLLER [Inicio]: Limpiando controladorCrearPartidaActual.");
              this.controladorCrearPartidaActual = null;
         }
    }
    /** Guarda referencia al controlador de Espera activo */
     public void setControladorEsperaActual(controladorPartidaEspera controlador) {
         System.out.println("CONTROLLER [Inicio]: Estableciendo controladorEsperaActual: " + (controlador != null ? controlador.getClass().getSimpleName() : "null"));
         clearControladorCrearPartidaActual(); // Asegura que solo uno esté activo
         this.controladorEsperaActual = controlador;
    }
    /** Limpia la referencia al controlador de Espera */
    public void clearControladorEsperaActual() {
         if (this.controladorEsperaActual != null) {
              System.out.println("CONTROLLER [Inicio]: Limpiando controladorEsperaActual.");
              this.controladorEsperaActual = null;
         }
    }

    // --- Implementación de ServerEventListener ---

    @Override
    public void onConectado() {
        System.out.println("CONTROLLER [Inicio]: ¡CONECTADO! Callback onConectado recibido.");
        // Intentar enviar registro si hay nombre pendiente y estamos conectados
        if (nombreUsuarioPendiente != null && serverComunicacion != null && serverComunicacion.isConectado()) {
            System.out.println("CONTROLLER [Inicio][onConectado]: Estado verificado OK. Enviando registro para: " + nombreUsuarioPendiente);
            serverComunicacion.enviarRegistroUsuario(nombreUsuarioPendiente);
            // NO NAVEGAR AQUÍ - Esperar a REGISTRO_OK
        } else {
             System.err.println("CONTROLLER [Inicio][onConectado]: ERROR DE ESTADO INESPERADO al intentar registrar.");
             nombreUsuarioPendiente = null;
             if (vistaInicio != null) {
                 SwingUtilities.invokeLater(() -> {
                      vistaInicio.mostrarError("Error interno post-conexión.");
                      vistaInicio.reactivarBotonPlay();
                 });
             }
             solicitarDesconexion(); // Forzar desconexión si el estado es malo
        }
    }

    @Override
    public void onDesconectado(String motivo) {
        System.out.println("CONTROLLER [Inicio]: DESCONECTADO. Motivo: " + motivo);
        // Limpiar todo el estado relevante
        clearControladorCrearPartidaActual();
        clearControladorEsperaActual();
        nombreUsuarioPendiente = null;
        nombreUsuarioRegistrado = null;

        // Volver a la pantalla inicial o mostrar estado desconectado
        SwingUtilities.invokeLater(() -> {
             // Cerrar vistas secundarias si existen y están visibles
             if (vistaUnirseJugar != null && vistaUnirseJugar.isDisplayable()) { vistaUnirseJugar.dispose(); this.vistaUnirseJugar = null; }
             if (vistaPartidaEspera != null && vistaPartidaEspera.isDisplayable()) { vistaPartidaEspera.dispose(); this.vistaPartidaEspera = null; }

             // Asegurar que la vista inicial esté visible y actualizada
             if (vistaInicio != null) {
                 if (!vistaInicio.isDisplayable()) vistaInicio.setVisible(true);
                 vistaInicio.mostrarEstadoDesconectado(motivo);
                 vistaInicio.reactivarBotonPlay();
             } else {
                 // Si incluso la vista inicial se cerró, no podemos hacer mucho más que loguear
                 System.out.println("CONTROLLER [Inicio][onDesconectado]: No hay vista de inicio para actualizar.");
                 // Podría mostrar un JOptionPane global como último recurso
                 // JOptionPane.showMessageDialog(null, "Desconectado: " + motivo, "Desconexión", JOptionPane.INFORMATION_MESSAGE);
             }
        });
    }

    // Dentro de controladorInicio.java

    @Override
    public void onError(String mensajeError) {
        System.err.println("CONTROLLER [Inicio]: ERROR recibido: " + mensajeError);
        nombreUsuarioPendiente = null;
        SwingUtilities.invokeLater(() -> {
             Component vistaActiva = null;
             // Determinar qué vista está activa (puede necesitar lógica más robusta)
             if (vistaPartidaEspera != null && vistaPartidaEspera.isDisplayable()) vistaActiva = vistaPartidaEspera;
             else if (vistaUnirseJugar != null && vistaUnirseJugar.isDisplayable()) vistaActiva = vistaUnirseJugar;
             else vistaActiva = vistaInicio;

             // Llamar al método mostrarError correcto en cada vista
             if (vistaActiva instanceof PartidaEspera) {
                 
                   ((PartidaEspera)vistaActiva).volverAPantallaAnterior(mensajeError); 
                   
                   
             } else if (vistaActiva instanceof UnirseJugar) {
                  ((UnirseJugar)vistaActiva).mostrarError(mensajeError);
                  ((UnirseJugar)vistaActiva).reactivarBotones(); // Asumiendo que existe este método
             } else if (vistaActiva instanceof PantallaInicio) {
                  ((PantallaInicio)vistaActiva).mostrarError(mensajeError);
                  ((PantallaInicio)vistaActiva).reactivarBotonPlay();
             } else {
                  // Fallback si no hay vista conocida
                  JOptionPane.showMessageDialog(null, mensajeError, "Error", JOptionPane.ERROR_MESSAGE);
             }
        });
    }





 // En Controler.controladorInicio.java

@Override
@SuppressWarnings("unchecked") // Para el casteo de List<String> de datos.get("jugadores")
public void onMensajeServidor(String tipo, Map<String, Object> datos) {
    System.out.println("CONTROLLER [Inicio]: Mensaje del servidor recibido - Tipo: " + tipo + ", Datos: " + datos);

    final controladorCrearPartida delegadoCrearUnir = this.controladorCrearPartidaActual;
    final controladorPartidaEspera delegadoEspera = this.controladorEsperaActual;
    final String finalTipo = tipo;
    final Map<String, Object> finalDatos = datos;
    final boolean esParaCrearUnir = esMensajeParaCrearUnir(finalTipo);
    final boolean esParaEspera = esMensajeParaEspera(finalTipo);
    final boolean esRegistro = finalTipo.equals("REGISTRO_OK") || finalTipo.equals("ERROR_REGISTRO");

    SwingUtilities.invokeLater(() -> {
        System.out.println(">>> [EDT] Task START for " + finalTipo);
        try {
            if (delegadoEspera != null && esParaEspera) {
                System.out.println("    [EDT] Procesando para delegado ESPERA tipo " + finalTipo);
                switch (finalTipo) {
                    case "NUEVO_JUGADOR_EN_SALA":
                        System.out.println("    [EDT] Procesando NUEVO_JUGADOR_EN_SALA. Se delega a Espera para UI.");
                        // Este evento puede ser solo para que la vista de espera actualice la UI.
                        // La creación de la Partida se hará de forma más robusta con ACTUALIZACION_SALA
                        // si esta última confirma la lista completa de 2 jugadores.
                        if (delegadoEspera != null) {
                            // Pasamos los datos para que la vista se actualice si lo necesita.
                            // controladorPartidaEspera.procesarActualizacionSala podría necesitar adaptarse
                            // para manejar 'jugadorInfo' si 'jugadores' (lista) no está presente.
                            delegadoEspera.procesarActualizacionSala(finalDatos);
                        }
                        break;

                    case "ACTUALIZACION_SALA":
                        System.out.println("    [EDT] Procesando ACTUALIZACION_SALA...");
                        if (delegadoEspera != null) {
                            // Dejar que la vista se actualice primero con la información
                            delegadoEspera.procesarActualizacionSala(finalDatos);

                            String idSalaActualizacion = (String) finalDatos.get("idSala");
                            if (idSalaActualizacion == null && delegadoEspera.getIdSala() != null) {
                                idSalaActualizacion = delegadoEspera.getIdSala();
                            }

                            Object jugadoresObj = finalDatos.get("jugadores");
                            if (idSalaActualizacion != null && jugadoresObj instanceof List) {
                                List<String> nombresJugadores = (List<String>) jugadoresObj;
                                System.out.println("    [EDT] ACTUALIZACION_SALA para '" + idSalaActualizacion + "'. Jugadores en datos: " + nombresJugadores);

                                // --- PUNTO CLAVE PARA CREAR LA PARTIDA ---
                                if (nombresJugadores.size() == 2) {
                                    if (!partidasActivas.containsKey(idSalaActualizacion)) {
                                        String nombreJ1 = nombresJugadores.get(0);
                                        String nombreJ2 = nombresJugadores.get(1);

                                        // Asegurarse que los nombres no sean nulos o vacíos y sean distintos
                                        if (nombreJ1 != null && !nombreJ1.isBlank() &&
                                            nombreJ2 != null && !nombreJ2.isBlank() &&
                                            !nombreJ1.equals(nombreJ2)) {
                                            
                                            Partida nuevaPartida = Partida.crearJuego(idSalaActualizacion, nombreJ1, nombreJ2, DIMENSION_TABLERO_DEFAULT);
                                            partidasActivas.put(idSalaActualizacion, nuevaPartida);
                                            System.out.println("    [EDT] MODELO Partida '" + idSalaActualizacion + "' CREADA (desde ACTUALIZACION_SALA) con: " + nombreJ1 + ", " + nombreJ2);
                                        } else {
                                            System.err.println("    [EDT] ACTUALIZACION_SALA: Nombres de jugador inválidos o duplicados. No se crea Partida. Nombres: " + nombresJugadores);
                                        }
                                    } else {
                                        System.out.println("    [EDT] MODELO Partida '" + idSalaActualizacion + "' ya existía (verificado por ACTUALIZACION_SALA).");
                                        // Opcional: verificar si los jugadores en la partida activa coinciden con nombresJugadores y actualizar si es necesario.
                                    }
                                } else {
                                     System.out.println("    [EDT] ACTUALIZACION_SALA: No hay 2 jugadores distintos aún (" + nombresJugadores.size() + ") para formalizar Partida modelo.");
                                }
                            } else {
                                 System.out.println("    [EDT] ACTUALIZACION_SALA: Datos insuficientes para crear/verificar Partida modelo (idSala o lista de jugadores).");
                            }
                        } else {
                             System.out.println("    [EDT] ACTUALIZACION_SALA recibida, pero delegadoEspera es null.");
                        }
                        break;

                    case "INICIAR_COLOCACION":
                        System.out.println("    [EDT] Procesando INICIAR_COLOCACION...");
                        String idSalaColocacion = (String) finalDatos.get("idSala");
                        Partida p = getPartidaActual(idSalaColocacion); // Usa el getter de esta clase

                        if (p == null || p.getJugador1() == null || p.getJugador2() == null) {
                            System.err.println("    [EDT] ERROR CRÍTICO: INICIAR_COLOCACION para sala '" + idSalaColocacion + "' pero Partida ("+p+") o Jugadores no están listos en el modelo.");
                            System.err.println("    Causa probable: ACTUALIZACION_SALA (con ambos nombres de jugador distintos) no se procesó correctamente para crear la Partida en 'partidasActivas'.");
                            if(delegadoEspera != null) {
                                // Usar el mensaje de error que ya tenías y que se muestra en la imagen
                                delegadoEspera.procesarErrorSala(Map.of("error", "Error de sincronización de partida interna (faltan datos de jugadores)."));
                            }
                            return; 
                        }
                        System.out.println("    [EDT] Partida para INICIAR_COLOCACION encontrada. J1: " + p.getJugador1().getNombre() + ", J2: " + p.getJugador2().getNombre());
                        delegadoEspera.procesarInicioColocacion(finalDatos);
                        break;
                    
                    // ... (Tu lógica existente para otros cases como OPONENTE_LISTO, AMBOS_LISTOS, INICIAR_PARTIDA, OPONENTE_SALIO, ERROR_SALA)
                    // Asegúrate que OPONENTE_SALIO y ERROR_SALA llamen a this.eliminarPartida(idSala);
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
                        System.out.println("    [EDT] Tipo de Espera [" + finalTipo + "] no reconocido o ya manejado por otros bloques.");
                        break;
                }
            } else if (delegadoCrearUnir != null && esParaCrearUnir) {
                // ... (Tu lógica para SALA_CREADA_OK, UNIDO_OK, etc., se mantiene)
                // Estos eventos NO crean la Partida en 'partidasActivas' directamente
                // si Partida.crearJuego necesita ambos nombres. Se espera a ACTUALIZACION_SALA.
                boolean exito = !(finalTipo.startsWith("ERROR_"));
                if (finalTipo.contains("CREAR_SALA")) {
                   System.out.println("    [EDT] Evento CREAR_SALA (" + finalTipo + ") procesado por delegadoCrearUnir.");
                   delegadoCrearUnir.procesarRespuestaCrearSala(exito, finalDatos);
                } else if (finalTipo.contains("UNIRSE_SALA") || finalTipo.equals("UNIDO_OK") || finalTipo.equals("ERROR_UNIRSE_SALA")) {
                   System.out.println("    [EDT] Evento UNIRSE_SALA (" + finalTipo + ") procesado por delegadoCrearUnir.");
                   delegadoCrearUnir.procesarRespuestaUnirseSala(exito, finalDatos);
                } else {
                    System.out.println("    [EDT] Tipo de Crear/Unir [" + finalTipo + "] no reconocido específicamente en este bloque.");
                }

            } else if (esRegistro) {
                // ... (Tu lógica de registro se mantiene)
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
            // ... (tu lógica de manejo de error global existente)
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



// --- Métodos privados Helper (asegúrate que existan en tu clase) ---
private boolean esMensajeParaCrearUnir(String tipo) {
    return tipo != null && (tipo.equals("SALA_CREADA_OK") || tipo.equals("ERROR_CREAR_SALA") ||
                            tipo.equals("UNIDO_OK") || tipo.equals("ERROR_UNIRSE_SALA"));
}



private boolean esMensajeParaEspera(String tipo) {
    return tipo != null && (tipo.equals("ACTUALIZACION_SALA") || tipo.equals("OPONENTE_LISTO") ||
                            tipo.equals("AMBOS_LISTOS") || tipo.equals("INICIAR_COLOCACION") ||
                            tipo.equals("INICIAR_PARTIDA") || tipo.equals("OPONENTE_SALIO") ||
                            tipo.equals("ERROR_SALA"));
}


} 