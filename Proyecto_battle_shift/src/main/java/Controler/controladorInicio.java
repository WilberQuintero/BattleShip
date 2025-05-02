/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controler; // Asegúrate que este sea tu paquete de controladores

// --- Dependencias ---
// Importacion de la capa de comunicación y su listener
import com.mycompany.servercomunicacion.ServerComunicacion;
import com.mycompany.servercomunicacion.ServerEventListener;
// Importacion de la Vista para poder interactuar con ella (opcional pero útil)
import View.PantallaInicio;

// Importacion de Map si vas a usar onMensajeServidor detalladamente
import java.util.Map;
// Importacion de SwingUtilities si necesitas actualizar la UI desde los callbacks
import javax.swing.SwingUtilities;


/**
 * Controlador para la pantalla de inicio. Maneja la lógica inicial,
 * como la conexión al servidor a través de ServerComunicacion.
 * Implementa ServerEventListener para recibir respuestas/eventos del servidor.
 */
public class controladorInicio implements ServerEventListener {

    private ServerComunicacion serverComunicacion;
    private PantallaInicio vistaInicio; // Referencia a la vista que controla

    /**
     * Constructor. Inicializa la capa de comunicación.
     */
    public controladorInicio() {
        System.out.println("CONTROLLER [controladorInicio]: Inicializando...");
        // Define aquí el host y puerto de tu servidor backend
        String hostServidor = "localhost"; // O la IP donde corre tu ServerTest
        int puertoServidor = 5000;       // El puerto que usa ServerTest
        try {
            this.serverComunicacion = new ServerComunicacion(hostServidor, puertoServidor);
            this.serverComunicacion.setListener(this); // Este controlador escuchará los eventos
            System.out.println("CONTROLLER [controladorInicio]: ServerComunicacion creado y listener asignado.");
        } catch (Exception e) {
             // Captura errores potenciales al crear ServerComunicacion (aunque no debería haberlos)
             System.err.println("CONTROLLER [controladorInicio]: Error Crítico al inicializar ServerComunicacion: " + e.getMessage());
             // Podrías querer lanzar una excepción o manejar esto de forma más robusta
             this.serverComunicacion = null; // Asegura que sea null si falla
        }
    }

    /**
     * Establece la referencia a la vista que este controlador maneja.
     * @param vista La instancia de PantallaInicio.
     */
    public void setVista(PantallaInicio vista) {
        this.vistaInicio = vista;
    }

    // --- Métodos llamados por la Vista ---

    /**
     * Método llamado por PantallaInicio cuando se presiona el botón 'Jugar'.
     * Inicia el intento de conexión.
     */
    public void botonJugarPresionado() {
        System.out.println("CONTROLLER [controladorInicio]: Botón Jugar presionado detectado.");
        if (serverComunicacion != null && !serverComunicacion.isConectado()) {
             System.out.println("CONTROLLER [controladorInicio]: Solicitando conexión a ServerComunicacion...");
             serverComunicacion.conectar(); // Inicia la conexión (asíncrona)
        } else if (serverComunicacion == null) {
             System.err.println("CONTROLLER [controladorInicio]: ServerComunicacion no está inicializado.");
             // Notificar a la vista sobre el error interno
             if (vistaInicio != null) {
                 vistaInicio.mostrarError("Error interno: No se pudo inicializar la comunicación.");
             }
        } else {
             System.out.println("CONTROLLER [controladorInicio]: Ya se está conectado o conectando.");
             // Podrías querer manejar este caso (quizás ya navegar si está conectado)
             if (serverComunicacion.isConectado() && vistaInicio != null) {
                  vistaInicio.navegarASiguientePantalla(); // Navega si ya estaba conectado
             }
        }
    }

    /**
     * Método llamado por la Vista si el usuario cierra la ventana o quiere desconectar.
     */
    public void solicitarDesconexion() {
         System.out.println("CONTROLLER [controladorInicio]: Solicitando desconexión a ServerComunicacion...");
         if (serverComunicacion != null) {
              serverComunicacion.desconectar();
         }
    }


    // --- Implementación de ServerEventListener ---
    // Estos métodos son llamados por ServerComunicacion en respuesta a eventos de red/servidor

    @Override
    public void onConectado() {
        // Se ejecuta en el hilo del listener de ServerComunicacion
        System.out.println("CONTROLLER [controladorInicio]: ¡CONECTADO! Notificación recibida.");
        // Ahora que estamos conectados, podemos navegar a la siguiente pantalla
        // IMPORTANTE: Actualizar la UI de Swing desde el hilo correcto (EDT)
        if (vistaInicio != null) {
            SwingUtilities.invokeLater(() -> {
                vistaInicio.navegarASiguientePantalla();
            });
        }
    }

    @Override
    public void onDesconectado(String motivo) {
        // Se ejecuta en el hilo del listener de ServerComunicacion
        System.out.println("CONTROLLER [controladorInicio]: DESCONECTADO. Motivo: " + motivo);
        // Actualizar la UI para reflejar esto (ej. reactivar botón 'Jugar')
        if (vistaInicio != null) {
            SwingUtilities.invokeLater(() -> {
                vistaInicio.mostrarEstadoDesconectado(motivo);
                vistaInicio.reactivarBotonPlay(); // Necesitas este método en la vista
            });
        }
    }

    @Override
    public void onError(String mensajeError) {
         // Se ejecuta en el hilo del listener de ServerComunicacion
        System.err.println("CONTROLLER [controladorInicio]: ERROR recibido: " + mensajeError);
        // Mostrar error al usuario y reactivar el botón de jugar
        if (vistaInicio != null) {
             SwingUtilities.invokeLater(() -> {
                 vistaInicio.mostrarError(mensajeError);
                 vistaInicio.reactivarBotonPlay(); // Necesitas este método en la vista
             });
        }
    }

    @Override
    public void onMensajeServidor(String tipo, Map<String, Object> datos) {
        // Se ejecuta en el hilo del listener de ServerComunicacion
        System.out.println("CONTROLLER [controladorInicio]: Mensaje genérico del servidor recibido - Tipo: " + tipo + ", Datos: " + datos);
        // Por ahora, para la conexión inicial, no esperamos mensajes específicos aquí.
        // Más adelante, aquí manejarías respuestas como SALA_CREADA_OK, UNIDO_OK, etc.
        // Siempre recuerda usar SwingUtilities.invokeLater si actualizas la UI desde aquí.
    }

     // --- Métodos para pasar datos (si es necesario) ---
     // Ejemplo: Si necesitas guardar el nombre de usuario antes de conectar
     /*
     private String nombreUsuario;
     public void setNombreUsuario(String nombre) {
         this.nombreUsuario = nombre;
         System.out.println("CONTROLLER [controladorInicio]: Nombre de usuario establecido a: " + nombre);
     }
     */
}