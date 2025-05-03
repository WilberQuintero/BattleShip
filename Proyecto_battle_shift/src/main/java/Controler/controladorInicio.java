/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controler;


import View.PantallaInicio;
import View.UnirseJugar;
import com.mycompany.servercomunicacion.ServerComunicacion;
import com.mycompany.servercomunicacion.ServerEventListener;
import java.awt.Component;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Controlador principal para el flujo inicial de la aplicación cliente.
 * Responsable de manejar la conexión inicial y el registro del usuario.
 * Actúa como el listener principal para ServerComunicacion y delega
 * eventos específicos a otros controladores cuando sea apropiado.
 */
public class controladorInicio implements ServerEventListener {

    // --- Atributos ---
    private final ServerComunicacion serverComunicacion; // Instancia única de comunicación
    private PantallaInicio vistaInicio;         // Referencia a la vista de inicio
    private UnirseJugar vistaUnirseJugar;       // Referencia a la vista de unirse/crear (se asigna al navegar)

    // Referencia al controlador secundario que está activo (si lo hay)
    private controladorCrearPartida controladorCrearPartidaActual;

    // Estado temporal
    private String nombreUsuarioPendiente = null; // Nombre guardado mientras conecta/registra
    private String nombreUsuarioRegistrado = null; // Nombre confirmado por el servidor

    /**
     * Constructor del Controlador de Inicio.
     * Inicializa la capa de comunicación y se establece como su listener.
     */
    public controladorInicio() {
        System.out.println("CONTROLLER [Inicio]: Inicializando...");
        String hostServidor = "localhost"; // O IP/DNS del servidor
        int puertoServidor = 5000;       // Puerto del servidor
        // Instanciar ServerComunicacion (manejar posible error)
        ServerComunicacion tempCom = null;
        try {
            tempCom = new ServerComunicacion(hostServidor, puertoServidor);
            tempCom.setListener(this); // Este controlador escuchará todos los eventos
            System.out.println("CONTROLLER [Inicio]: ServerComunicacion creado y listener asignado.");
        } catch (Exception e) {
             System.err.println("CONTROLLER [Inicio]: ERROR CRÍTICO al inicializar ServerComunicacion: " + e.getMessage());
             // Podríamos mostrar un error fatal aquí o dejar serverComunicacion como null
        }
        this.serverComunicacion = tempCom; // Asignar la instancia (puede ser null si falló)
    }

    // --- Setters para Vistas ---

    /**
     * Establece la referencia a la vista de inicio.
     */
    public void setVistaInicio(PantallaInicio vista) {
        this.vistaInicio = vista;
    }

    /**
     * Establece la referencia a la vista de unirse/jugar.
     * Útil si necesitamos interactuar con ella directamente más tarde.
     */
    public void setVistaUnirseJugar(UnirseJugar vista) {
        this.vistaUnirseJugar = vista;
        // Podríamos limpiar la referencia a vistaInicio aquí si ya no se usa
        // this.vistaInicio = null;
    }

   /**
 * Devuelve el nombre de usuario confirmado por el servidor.
 * @return El nombre del usuario registrado, o null si aún no se ha registrado.
 */
public String getNombreUsuarioRegistrado() {
    return nombreUsuarioRegistrado;
}

    /**
     * Devuelve la instancia compartida de ServerComunicacion.
     * Necesario para que otras partes (como la Vista al crear otros controladores)
     * puedan acceder a ella.
     * @return La instancia de ServerComunicacion, o null si falló la inicialización.
     */
    public ServerComunicacion getServerComunicacion() {
        return serverComunicacion;
    }

    // --- Métodos llamados por la Vista de Inicio ---

    /**
     * Inicia el proceso de conexión al servidor y guarda el nombre
     * para el registro posterior. Llamado desde PantallaInicio.
     * @param nombreUsuario El nombre ingresado.
     */
    public void intentarConectarYRegistrar(String nombreUsuario) {
        System.out.println("CONTROLLER [Inicio]: Solicitud para conectar y registrar usuario: " + nombreUsuario);

        if (serverComunicacion == null) {
             System.err.println("CONTROLLER [Inicio]: ServerComunicacion no inicializado.");
             if (vistaInicio != null) vistaInicio.mostrarError("Error interno: Comunicación no disponible.");
             return;
        }
        if (serverComunicacion.isConectado()) {
             System.out.println("CONTROLLER [Inicio]: Ya está conectado.");
             // Si ya está conectado pero no registrado, intentar registrar
             if (this.nombreUsuarioRegistrado == null) {
                  System.out.println("CONTROLLER [Inicio]: Ya conectado, intentando registrar nombre: " + nombreUsuario);
                  this.nombreUsuarioPendiente = nombreUsuario;
                  // Llamar directamente al envío (onConectado no se llamará de nuevo)
                   if (nombreUsuarioPendiente != null && serverComunicacion.isConectado()) {
                       serverComunicacion.enviarRegistroUsuario(nombreUsuarioPendiente);
                   } else {
                        // Mostrar error si el estado es inconsistente
                         if (vistaInicio != null) vistaInicio.mostrarError("Error de estado al intentar registrar.");
                   }
             } else {
                  // Ya conectado Y registrado, quizás navegar directamente?
                  System.out.println("CONTROLLER [Inicio]: Ya conectado y registrado como " + this.nombreUsuarioRegistrado);
                  if (vistaInicio != null) vistaInicio.navegarASiguientePantalla();
             }
             return;
        }
        // Si no está conectado, iniciar conexión
        this.nombreUsuarioPendiente = nombreUsuario;
        System.out.println("CONTROLLER [Inicio]: Nombre guardado. Solicitando conexión a ServerComunicacion...");
        serverComunicacion.conectar(); // Inicia la conexión asíncrona
    }

    /**
     * Solicita el cierre de la conexión. Llamado desde la UI.
     */
     public void solicitarDesconexion() {
         System.out.println("CONTROLLER [Inicio]: Solicitando desconexión...");
         if (serverComunicacion != null) {
              serverComunicacion.desconectar();
         }
     }

    // --- Gestión del Controlador Secundario ---

    /**
     * Guarda la referencia al controlador que maneja la pantalla de Crear/Unirse Sala.
     * @param controlador El controlador secundario activo.
     */
    public void setControladorCrearPartidaActual(controladorCrearPartida controlador) {
         System.out.println("CONTROLLER [Inicio]: Estableciendo controladorCrearPartidaActual: " + (controlador != null ? controlador.getClass().getSimpleName() : "null"));
         this.controladorCrearPartidaActual = controlador;
    }

    /**
     * Limpia la referencia al controlador secundario.
     */
    public void clearControladorCrearPartidaActual() {
         System.out.println("CONTROLLER [Inicio]: Limpiando controladorCrearPartidaActual.");
         this.controladorCrearPartidaActual = null;
    }

    // --- Implementación de ServerEventListener ---

    @Override
    public void onConectado() {
        System.out.println("CONTROLLER [Inicio]: ¡CONECTADO! Callback onConectado recibido.");
        // Ahora que conectó, enviar el registro si hay un nombre pendiente
        if (nombreUsuarioPendiente != null && serverComunicacion != null && serverComunicacion.isConectado()) {
            System.out.println("CONTROLLER [Inicio][onConectado]: Estado verificado OK. Enviando registro para: " + nombreUsuarioPendiente);
            serverComunicacion.enviarRegistroUsuario(nombreUsuarioPendiente);
            // NO NAVEGAR AÚN - Esperar a REGISTRO_OK
        } else {
             System.err.println("CONTROLLER [Inicio][onConectado]: ERROR DE ESTADO - Conectado pero no se puede enviar registro.");
             nombreUsuarioPendiente = null; // Limpiar
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
        clearControladorCrearPartidaActual(); // Limpiar referencia al secundario
        nombreUsuarioPendiente = null;
        nombreUsuarioRegistrado = null; // Limpiar nombre registrado

        // Aseguramos actualizar la UI desde el hilo de Swing
        SwingUtilities.invokeLater(() -> {
            // 1. Cerrar o resetear la vista secundaria si estaba abierta
            if (vistaUnirseJugar != null && vistaUnirseJugar.isDisplayable()) {
                 System.out.println("CONTROLLER [Inicio][onDesconectado]: Cerrando vista UnirseJugar.");
                 vistaUnirseJugar.setVisible(false);
                 vistaUnirseJugar.dispose(); // Liberar recursos
                 this.vistaUnirseJugar = null; // Limpiar referencia
            }

            // 2. Actualizar la vista de inicio si todavía existe y es visible
            //    (o volver a mostrarla si la lógica de la app lo requiere)
            if (vistaInicio != null) {
                // Si no está visible, quizás queramos mostrarla de nuevo
                if (!vistaInicio.isDisplayable()) {
                    System.out.println("CONTROLLER [Inicio][onDesconectado]: Haciendo visible PantallaInicio.");
                    vistaInicio.setVisible(true);
                }
                // Actualizar su estado para reflejar desconexión
                System.out.println("CONTROLLER [Inicio][onDesconectado]: Actualizando estado de PantallaInicio.");
                vistaInicio.mostrarEstadoDesconectado(motivo); // Método en PantallaInicio
                vistaInicio.reactivarBotonPlay();           // Método en PantallaInicio
            } else {
                // Si no hay referencia a vistaInicio (quizás se cerró antes),
                // mostramos un mensaje genérico. No intentamos recrearla aquí.
                System.out.println("CONTROLLER [Inicio][onDesconectado]: No hay referencia a vistaInicio. Mostrando JOptionPane.");
                JOptionPane.showMessageDialog(null, "Desconectado: " + motivo, "Desconexión", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    @Override
    public void onError(String mensajeError) {
        System.err.println("CONTROLLER [Inicio]: ERROR recibido: " + mensajeError);
        // No limpiar controlador secundario necesariamente, podría ser temporal
        nombreUsuarioPendiente = null; // Limpiar si el error ocurrió durante registro/conexión
        SwingUtilities.invokeLater(() -> {
             // Mostrar error en la vista activa
             Component vistaActiva = vistaUnirseJugar != null && vistaUnirseJugar.isDisplayable() ? vistaUnirseJugar : vistaInicio;
             if (vistaActiva instanceof UnirseJugar) {
                 ((UnirseJugar)vistaActiva).mostrarError(mensajeError);
                 ((UnirseJugar)vistaActiva).reactivarBotones(); // Método hipotético
             } else if (vistaActiva instanceof PantallaInicio) {
                 ((PantallaInicio)vistaActiva).mostrarError(mensajeError);
                 ((PantallaInicio)vistaActiva).reactivarBotonPlay();
             } else {
                  JOptionPane.showMessageDialog(null, mensajeError, "Error", JOptionPane.ERROR_MESSAGE);
             }
        });
    }

    @Override
    public void onMensajeServidor(String tipo, Map<String, Object> datos) {
        System.out.println("CONTROLLER [Inicio]: Mensaje genérico recibido - Tipo: " + tipo + ", Datos: " + datos);

        // Delegar a controlador secundario si existe y el tipo de mensaje es relevante para él
        if (controladorCrearPartidaActual != null &&
            (tipo.equals("SALA_CREADA_OK") || tipo.equals("ERROR_CREAR_SALA") ||
             tipo.equals("UNIDO_OK") || tipo.equals("ERROR_UNIRSE_SALA")
             /* || otros tipos relevantes para Crear/Unir */ ))
        {
             System.out.println("CONTROLLER [Inicio]: Delegando mensaje tipo '" + tipo + "' a controladorCrearPartidaActual.");
             boolean exito = !(tipo.startsWith("ERROR_")); // Simplificación: true si no empieza con ERROR
             // Llamar al método apropiado en el controlador secundario (Asegúrate que existan)
              if (tipo.contains("CREAR")) {
                  controladorCrearPartidaActual.procesarRespuestaCrearSala(exito, datos);
              } else if (tipo.contains("UNIRSE")) {
                   controladorCrearPartidaActual.procesarRespuestaUnirseSala(exito, datos);
              }
              // Añadir más delegaciones si es necesario (ej. lista de salas)

        } else {
            // Si no hay delegado o el mensaje es para este controlador, procesarlo aquí
            System.out.println("CONTROLLER [Inicio]: Procesando mensaje tipo '" + tipo + "' localmente.");
            SwingUtilities.invokeLater(() -> {
                 switch(tipo) {
                     case "REGISTRO_OK":
                         System.out.println("CONTROLLER [Inicio]: Servidor confirma REGISTRO_OK para " + datos.get("nombre"));
                         this.nombreUsuarioRegistrado = (String) datos.get("nombre");
                         nombreUsuarioPendiente = null; // Limpiar pendiente
                         // Navegar a la pantalla UnirseJugar
                         if (vistaInicio != null) {
                              System.out.println("CONTROLLER [Inicio]: Registro exitoso. Navegando a Unirse/Jugar...");
                              vistaInicio.navegarASiguientePantalla(); // Este método crea UnirseJugar y los controladores
                         }
                         break;

                     case "ERROR_REGISTRO":
                         System.err.println("CONTROLLER [Inicio]: Servidor reporta ERROR_REGISTRO: " + datos.get("error"));
                         nombreUsuarioPendiente = null;
                         if (vistaInicio != null) {
                              vistaInicio.mostrarError("Error de Registro: " + datos.get("error"));
                              vistaInicio.reactivarBotonPlay();
                         }
                          solicitarDesconexion(); // Forzar desconexión si el registro falló
                         break;

                     // Otros mensajes que SÍ maneja este controlador principal:
                     // case "LISTA_JUGADORES_GLOBAL": ...
                     // case "MENSAJE_BROADCAST": ...

                     default:
                         System.out.println("CONTROLLER [Inicio]: Mensaje tipo '" + tipo + "' no manejado.");
                         break;
                 }
             });
        } // Fin else (manejo local)
    } // Fin onMensajeServidor
}