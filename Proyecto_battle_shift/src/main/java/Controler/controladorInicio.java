/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controler;

// --- Imports ---

import View.PantallaInicio;
import View.UnirseJugar;
import View.PartidaEspera; // Necesario si se maneja su cierre aquí
import com.mycompany.servercomunicacion.ServerComunicacion;
import com.mycompany.servercomunicacion.ServerEventListener;
// Utilidades
import java.awt.Component; // Para saber qué vista está activa en onError
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
    private final ServerComunicacion serverComunicacion; // Instancia única de comunicación
    private PantallaInicio vistaInicio;         // Referencia a la vista de inicio
    private UnirseJugar vistaUnirseJugar;       // Referencia a la vista de unirse/crear
    private PartidaEspera vistaPartidaEspera;   // Referencia a la vista de espera

    // Referencias a los controladores secundarios que pueden estar activos
    private controladorCrearPartida controladorCrearPartidaActual;
    private controladorPartidaEspera controladorEsperaActual;

    // Estado temporal
    private String nombreUsuarioPendiente = null; // Nombre guardado mientras conecta/registra
    private String nombreUsuarioRegistrado = null; // Nombre confirmado por el servidor

    /**
     * Constructor del Controlador de Inicio.
     * Inicializa la capa de comunicación y se establece como su listener.
     */
    public controladorInicio() {
        System.out.println("CONTROLLER [Inicio]: Inicializando...");
        String hostServidor = "localhost";
        int puertoServidor = 5000;
        ServerComunicacion tempCom = null;
        try {
            tempCom = new ServerComunicacion(hostServidor, puertoServidor);
            tempCom.setListener(this); // Este controlador escuchará todos los eventos
            System.out.println("CONTROLLER [Inicio]: ServerComunicacion creado y listener asignado.");
        } catch (Exception e) {
             System.err.println("CONTROLLER [Inicio]: ERROR CRÍTICO al inicializar ServerComunicacion: " + e.getMessage());
        }
        this.serverComunicacion = tempCom; // Asignar la instancia
    }

    // --- Setters para Vistas ---
    // Estos permiten a las vistas registrarse con este controlador si es necesario,
    // aunque principalmente es este controlador el que necesita referencias a ellas.
    public void setVistaInicio(PantallaInicio vista) {
        this.vistaInicio = vista;
    }
    public void setVistaUnirseJugar(UnirseJugar vista) {
        this.vistaUnirseJugar = vista;
    }
     public void setVistaPartidaEspera(PartidaEspera vista) {
        this.vistaPartidaEspera = vista;
    }

    // --- Getters para dependencias compartidas ---

    /**
     * Devuelve la instancia compartida de ServerComunicacion.
     */
    public ServerComunicacion getServerComunicacion() {
        return serverComunicacion;
    }

    /**
     * Devuelve el nombre de usuario confirmado por el servidor.
     */
    public String getNombreUsuarioRegistrado() {
        return nombreUsuarioRegistrado;
    }

    // --- Métodos llamados por la Vista de Inicio ---

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





 @Override
    public void onMensajeServidor(String tipo, Map<String, Object> datos) {
        System.out.println("CONTROLLER [Inicio]: Mensaje genérico recibido - Tipo: " + tipo + ", Datos: " + datos);

        // Usar referencias finales para el lambda
        final controladorCrearPartida delegadoCrearUnir = this.controladorCrearPartidaActual;
        final controladorPartidaEspera delegadoEspera = this.controladorEsperaActual;
        final String finalTipo = tipo;
        final Map<String, Object> finalDatos = datos;
        final boolean esParaCrearUnir = esMensajeParaCrearUnir(finalTipo);
        final boolean esParaEspera = esMensajeParaEspera(finalTipo);
        final boolean esRegistro = finalTipo.equals("REGISTRO_OK") || finalTipo.equals("ERROR_REGISTRO");

        // Ejecutar en el EDT
        SwingUtilities.invokeLater(() -> {
            System.out.println(">>> EDT Task START for " + finalTipo);
            try {
                // 1. Intentar delegar a Controlador de Espera
                if (delegadoEspera != null && esParaEspera) {
                     System.out.println("    EDT Task: Delegado ESPERA Ref Check: " + delegadoEspera.getClass().getName() + "@" + delegadoEspera.hashCode());
                     System.out.println("    EDT Task: Procesando para delegado ESPERA tipo " + finalTipo);

                     switch(finalTipo) {
                          case "ACTUALIZACION_SALA":
                          case "OPONENTE_LISTO":
                          case "AMBOS_LISTOS":
                               delegadoEspera.procesarActualizacionSala(finalDatos);
                               break;
                          case "INICIAR_COLOCACION":
                               // --- DELAY DE DIAGNÓSTICO ---
                               try {
                                    System.out.println("    EDT Task: >>> INICIANDO PAUSA DIAGNÓSTICA (500ms) ANTES de delegar INICIAR_COLOCACION <<<");
                                    Thread.sleep(500); // Pausa de medio segundo
                                    System.out.println("    EDT Task: >>> PAUSA FINALIZADA <<<");
                               } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    System.err.println("    EDT Task: Hilo interrumpido durante pausa diagnóstica.");
                               }
                               // --- FIN DELAY ---
                               System.out.println("    EDT Task: Llamando a delegadoEspera.procesarInicioColocacion...");
                               delegadoEspera.procesarInicioColocacion(finalDatos);
                               break;
                          case "INICIAR_PARTIDA":
                               System.out.println("    EDT Task: Llamando a delegadoEspera.procesarInicioPartida...");
                               delegadoEspera.procesarInicioPartida(finalDatos);
                               clearControladorEsperaActual();
                               break;
                           case "OPONENTE_SALIO":
                                System.out.println("    EDT Task: Llamando a delegadoEspera.procesarSalidaOponente...");
                                delegadoEspera.procesarSalidaOponente(finalDatos);
                                clearControladorEsperaActual();
                                break;
                           case "ERROR_SALA":
                                System.out.println("    EDT Task: Llamando a delegadoEspera.procesarErrorSala...");
                                delegadoEspera.procesarErrorSala(finalDatos);
                                clearControladorEsperaActual();
                                break;
                           default:
                                System.out.println("    EDT Task: Tipo de Espera [" + finalTipo + "] no reconocido en switch interno.");
                                break;
                     }
                     System.out.println("    EDT Task: Llamada a delegado ESPERA retornó.");

                // 2. Intentar delegar a Controlador de Crear/Unir
                } else if (delegadoCrearUnir != null && esParaCrearUnir) {
                    System.out.println("    EDT Task: Delegado CREAR/UNIR Ref Check: " + delegadoCrearUnir.getClass().getName() + "@" + delegadoCrearUnir.hashCode());
                    System.out.println("    EDT Task: Llamando a delegado CREAR/UNIR para tipo " + finalTipo);
                    boolean exito = !(finalTipo.startsWith("ERROR_"));

                    // --- AÑADIR DELAY AQUÍ TAMBIÉN SI QUIERES PROBAR PARA UNIDO_OK ---
                    
                    try {
                         System.out.println("    EDT Task: >>> INICIANDO PAUSA DE 500ms ANTES de delegar " + finalTipo + " <<<");
                         Thread.sleep(500);
                         System.out.println("    EDT Task: >>> PAUSA FINALIZADA <<<");
                    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    
                    // --- FIN DELAY ---

                    if (finalTipo.contains("CREAR")) {
                        delegadoCrearUnir.procesarRespuestaCrearSala(exito, finalDatos);
                    } else if (finalTipo.contains("UNIRSE")) {
                        delegadoCrearUnir.procesarRespuestaUnirseSala(exito, finalDatos);
                    }
                    System.out.println("    EDT Task: Llamada a delegado CREAR/UNIR retornó.");


                // 3. Si no, manejar localmente (Eventos de Registro)
                } else if (esRegistro) {
                    System.out.println("    EDT Task: Procesando REGISTRO localmente.");
                    if (finalTipo.equals("REGISTRO_OK")) {
                         System.out.println("    EDT Task: Servidor confirma REGISTRO_OK para " + finalDatos.get("nombre"));
                         // Guardar nombre confirmado
                         this.nombreUsuarioRegistrado = (String) finalDatos.get("nombre");
                         nombreUsuarioPendiente = null; // Limpiar el pendiente
                         // Navegar a la siguiente pantalla si la vista inicial existe y está visible
                         if (vistaInicio != null && vistaInicio.isDisplayable()) {
                              System.out.println("    EDT Task: Registro exitoso. Navegando a Unirse/Jugar...");
                              vistaInicio.navegarASiguientePantalla(); // Llama al método que crea UnirseJugar y los controllers
                         } else {
                              System.out.println("    EDT Task: Registro OK, pero vistaInicio no disponible para navegar.");
                         }
                    } else { // ERROR_REGISTRO
                         System.err.println("    EDT Task: Servidor reporta ERROR_REGISTRO: " + finalDatos.get("error"));
                         nombreUsuarioPendiente = null;
                         // Mostrar error en la vista inicial y reactivar
                         if (vistaInicio != null) {
                              vistaInicio.mostrarError("Error de Registro: " + finalDatos.get("error"));
                              vistaInicio.reactivarBotonPlay();
                         }
                          // Forzar desconexión en error de registro, ya que no se puede continuar
                          solicitarDesconexion();
                    }
                // 4. Mensaje no manejado o delegado incorrecto
                } else {
                     System.out.println("    EDT Task: Mensaje tipo '" + finalTipo + "' NO MANEJADO.");
                     // Añadir logs para diagnosticar por qué no se manejó si debería haberse hecho
                     if(esParaEspera && delegadoEspera == null) {
                          System.err.println("    EDT Task: Era mensaje para Espera pero delegadoEspera era NULL.");
                     }
                     if(esParaCrearUnir && delegadoCrearUnir == null) {
                          System.err.println("    EDT Task: Era mensaje para Crear/Unir pero delegadoCrearUnir era NULL.");
                     }
                }

            } catch (Throwable t) { // Capturar CUALQUIER error/excepción durante procesamiento EDT
                 System.err.println("    EDT Task: CATCH Throwable durante procesamiento de " + finalTipo + ":");
                 t.printStackTrace(); // Imprimir stack trace completo
                 // Intentar notificar a la UI genéricamente si es posible
                  Component vistaActiva = null; // Determinar vista activa para mostrar error
                  if (vistaPartidaEspera != null && vistaPartidaEspera.isDisplayable()) vistaActiva = vistaPartidaEspera;
                  else if (vistaUnirseJugar != null && vistaUnirseJugar.isDisplayable()) vistaActiva = vistaUnirseJugar;
                  else vistaActiva = vistaInicio;

                  final String errorMsg = "Error interno procesando respuesta del servidor: " + t.getMessage();
                  // Mostrar error en la vista activa
                  if (vistaActiva instanceof PartidaEspera) { ((PartidaEspera)vistaActiva).mostrarError(errorMsg, true); /*Considerar volver atrás*/ }
                  else if (vistaActiva instanceof UnirseJugar) { ((UnirseJugar)vistaActiva).mostrarError(errorMsg); ((UnirseJugar)vistaActiva).reactivarBotones(); }
                  else if (vistaActiva instanceof PantallaInicio) { ((PantallaInicio)vistaActiva).mostrarError(errorMsg); ((PantallaInicio)vistaActiva).reactivarBotonPlay(); }
                  else { JOptionPane.showMessageDialog(null, errorMsg, "Error Crítico", JOptionPane.ERROR_MESSAGE); }
                  // Considerar desconectar si ocurre un error grave aquí
                  // solicitarDesconexion();
            } finally {
                 System.out.println("<<< EDT Task END for " + finalTipo); // Log fin tarea EDT
            }
        }); // Fin invokeLater
    } // Fin onMensajeServidor


    // --- Métodos privados Helper para clasificación de mensajes ---
    // Asegúrate de tener estos métodos en tu clase controladorInicio
    private boolean esMensajeParaCrearUnir(String tipo) {
         return tipo != null && (tipo.equals("SALA_CREADA_OK") || tipo.equals("ERROR_CREAR_SALA") ||
                                 tipo.equals("UNIDO_OK") || tipo.equals("ERROR_UNIRSE_SALA"));
                // Añadir LISTA_SALAS_RESPUESTA si se maneja en CrearPartida
    }

    private boolean esMensajeParaEspera(String tipo) {
         return tipo != null && (tipo.equals("ACTUALIZACION_SALA") || tipo.equals("OPONENTE_LISTO") ||
                                 tipo.equals("AMBOS_LISTOS") || tipo.equals("INICIAR_COLOCACION") ||
                                 tipo.equals("INICIAR_PARTIDA") || tipo.equals("OPONENTE_SALIO") ||
                                 tipo.equals("ERROR_SALA"));
                // Añadir CHAT_SALA si se maneja en Espera
    }


   


} 