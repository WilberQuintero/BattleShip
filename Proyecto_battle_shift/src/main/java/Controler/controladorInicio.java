/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controler;


import View.PantallaInicio;
import com.mycompany.servercomunicacion.ServerComunicacion;
import com.mycompany.servercomunicacion.ServerEventListener;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane; // Para mostrar mensajes

/**
 * Controlador para la pantalla de inicio (CORREGIDO).
 * Maneja la conexión y el registro del usuario.
 * Implementa ServerEventListener para recibir respuestas/eventos del servidor.
 */
public class controladorInicio implements ServerEventListener {

    private ServerComunicacion serverComunicacion;
    private PantallaInicio vistaInicio;
    private String nombreUsuarioPendiente = null; // Nombre guardado mientras conecta

    /**
     * Constructor.
     */
    public controladorInicio() {
        System.out.println("CONTROLLER [controladorInicio]: Inicializando...");
        String hostServidor = "localhost";
        int puertoServidor = 5000;
        try {
            this.serverComunicacion = new ServerComunicacion(hostServidor, puertoServidor);
            this.serverComunicacion.setListener(this);
            System.out.println("CONTROLLER [controladorInicio]: ServerComunicacion creado y listener asignado.");
        } catch (Exception e) {
             System.err.println("CONTROLLER [controladorInicio]: Error Crítico al inicializar ServerComunicacion: " + e.getMessage());
             this.serverComunicacion = null;
        }
    }

    /**
     * Establece la referencia a la vista.
     */
    public void setVista(PantallaInicio vista) {
        this.vistaInicio = vista;
    }

    // --- Métodos llamados por la Vista ---

    /**
     * Intenta conectar y guarda el nombre para registrarlo DESPUÉS de conectar.
     */
    public void intentarConectarYRegistrar(String nombreUsuario) {
        System.out.println("CONTROLLER [controladorInicio]: Solicitud para conectar y registrar usuario: " + nombreUsuario);
        // Solo intenta conectar si no está ya conectado o intentándolo
        if (serverComunicacion != null && !serverComunicacion.isConectado() /*&& !serverComunicacion.isIntentandoConectar() // <-- Necesitarías añadir isIntentandoConectar() a ServerComunicacion */ ) {
             this.nombreUsuarioPendiente = nombreUsuario; // Guarda el nombre
             System.out.println("CONTROLLER [controladorInicio]: Nombre guardado. Solicitando conexión a ServerComunicacion...");
             serverComunicacion.conectar(); // Inicia la conexión (asíncrona)
        } else if (serverComunicacion == null) {
             System.err.println("CONTROLLER [controladorInicio]: ServerComunicacion no está inicializado.");
             if (vistaInicio != null) vistaInicio.mostrarError("Error interno: Comunicación no disponible.");
             // Reactivar botón en la vista si falla inmediatamente
             if (vistaInicio != null) vistaInicio.reactivarBotonPlay();
        } else {
             System.out.println("CONTROLLER [controladorInicio]: Ya conectado o en proceso.");
             // No hacemos nada si ya está conectado/conectando, esperamos callbacks
             // Podríamos opcionalmente mostrar un mensaje "Ya conectando..."
             // if (vistaInicio != null) vistaInicio.mostrarMensaje("Ya se está intentando conectar...");
        }
    }

    /**
     * Cierra la conexión con el servidor.
     */
     public void solicitarDesconexion() {
         System.out.println("CONTROLLER [controladorInicio]: Solicitando desconexión a ServerComunicacion...");
         if (serverComunicacion != null) {
              serverComunicacion.desconectar(); // desconectar() debería llamar a onDesconectado eventualmente
         }
     }


    // --- Implementación de ServerEventListener ---

    @Override
    public void onConectado() {
        // SOLO se llama cuando ServerComunicacion confirma conexión de socket exitosa
        System.out.println("CONTROLLER [controladorInicio]: ¡CONECTADO! Callback onConectado recibido.");

        // Ahora que estamos conectados, INTENTAMOS enviar el registro.
        if (nombreUsuarioPendiente != null && serverComunicacion != null && serverComunicacion.isConectado()) {
            System.out.println("CONTROLLER [onConectado]: Estado verificado OK. Llamando a serverComunicacion.enviarRegistroUsuario para: " + nombreUsuarioPendiente);
            serverComunicacion.enviarRegistroUsuario(nombreUsuarioPendiente);
            // IMPORTANTE: YA NO NAVEGAMOS AQUÍ. Esperamos a REGISTRO_OK.
        } else {
             // Si llegamos a onConectado pero el estado es inconsistente (¡muy raro!)
             System.err.println("CONTROLLER [onConectado]: ERROR DE ESTADO INESPERADO. Conectado pero no se puede enviar registro.");
             nombreUsuarioPendiente = null; // Limpiar
             if (vistaInicio != null) {
                  SwingUtilities.invokeLater(() -> {
                      vistaInicio.mostrarError("Error interno tras conectar. No se pudo registrar.");
                      vistaInicio.reactivarBotonPlay();
                  });
             }
             if (serverComunicacion != null) serverComunicacion.desconectar(); // Desconectar si hay error
        }
    }

    @Override
    public void onDesconectado(String motivo) {
        System.out.println("CONTROLLER [controladorInicio]: DESCONECTADO. Motivo: " + motivo);
        nombreUsuarioPendiente = null; // Limpiar nombre pendiente
        // Aseguramos actualizar la UI desde el hilo de Swing
        if (vistaInicio != null) {
            SwingUtilities.invokeLater(() -> {
                vistaInicio.mostrarEstadoDesconectado(motivo); // Podría mostrar un mensaje en la UI
                vistaInicio.reactivarBotonPlay(); // Reactivar el botón
            });
        }
    }

    @Override
    public void onError(String mensajeError) {
        System.err.println("CONTROLLER [controladorInicio]: ERROR recibido: " + mensajeError);
        nombreUsuarioPendiente = null; // Limpiar nombre pendiente
        // Aseguramos actualizar la UI desde el hilo de Swing
        if (vistaInicio != null) {
             SwingUtilities.invokeLater(() -> {
                 vistaInicio.mostrarError(mensajeError); // Mostrar el error
                 vistaInicio.reactivarBotonPlay(); // Reactivar el botón
             });
        }
    }

    @Override
    public void onMensajeServidor(String tipo, Map<String, Object> datos) {
        System.out.println("CONTROLLER [controladorInicio]: Mensaje genérico del servidor - Tipo: " + tipo + ", Datos: " + datos);
        // Ejecutar lógica de UI en el hilo de Swing
        SwingUtilities.invokeLater(() -> {
             switch(tipo) {
                 case "REGISTRO_OK":
                     System.out.println("CONTROLLER: Servidor confirma REGISTRO_OK para " + datos.get("nombre"));
                     // --- ¡AHORA SÍ NAVEGAMOS! ---
                     if (vistaInicio != null) {
                          System.out.println("CONTROLLER: Registro exitoso. Navegando a la siguiente pantalla...");
                          vistaInicio.navegarASiguientePantalla();
                     }
                     // Limpiar el nombre pendiente si se registró con éxito
                     // nombreUsuarioPendiente = null;
                     break;

                 case "ERROR_REGISTRO":
                     System.err.println("CONTROLLER: Servidor reporta ERROR_REGISTRO: " + datos.get("error"));
                     nombreUsuarioPendiente = null; // Limpiar nombre pendiente
                     if (vistaInicio != null) {
                          vistaInicio.mostrarError("Error de Registro: " + datos.get("error"));
                          vistaInicio.reactivarBotonPlay(); // Permitir reintentar
                     }
                      // Considerar si desconectar aquí o permitir reintentar con otro nombre
                      // solicitarDesconexion();
                     break;

                 // Aquí manejarías otros mensajes del servidor (SALA_CREADA_OK, UNIDO_OK, etc.)
                 // cuando implementes esas funcionalidades.

                 default:
                     System.out.println("CONTROLLER: Mensaje tipo '" + tipo + "' no manejado específicamente.");
                     break;
             }
         });
    }
}