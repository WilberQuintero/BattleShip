/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View; // O tu paquete de Vistas

// --- Imports ---
import Controler.controladorInicio;
import Controler.controladorPartidaEspera;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.util.Map; // Para navegarAPantallaJuego

/**
 * Vista que muestra la sala de espera. El juego inicia automáticamente
 * cuando se une el segundo jugador, pasando a la fase de colocación.
 * (Versión integrada con UI existente del usuario)
 */
public class PartidaEspera extends javax.swing.JFrame {

    // --- Atributos ---
    private final controladorInicio controladorPrincipal;
    private final controladorPartidaEspera controlador;
    private final String idSala;
    private final String miNombreUsuario;
    private DefaultListModel<String> listModelJugadores;

    // Variables para tu animación
    private javax.swing.Timer tiempoAnimacion; // Renombrada desde 'tiempo'
    private int posicionAnimacion = 0;        // Renombrada desde 'posicion'
private javax.swing.JList<String> listaJugadores;

    /**
     * Constructor de la Pantalla de Espera.
     * @param ctrlPrincipal Controlador principal.
     * @param idSala ID de la sala.
     * @param miNombre Nombre del jugador local.
     */
    public PartidaEspera(controladorInicio ctrlPrincipal, String idSala, String miNombre) {
        initComponents(); // Crea componentes Swing (generado por NetBeans)
        this.setLocationRelativeTo(null);
        this.setTitle("Sala: " + idSala + " | Esperando Oponente - Battleship");

        this.controladorPrincipal = ctrlPrincipal;
        this.idSala = idSala;
        this.miNombreUsuario = miNombre;

        // Crear controlador específico
        controladorPartidaEspera ctrlEspera = null;
        if (ctrlPrincipal != null && ctrlPrincipal.getServerComunicacion() != null) {
            System.out.println("VIEW [PartidaEspera]: Creando controladorPartidaEspera para sala: " + idSala);
            ctrlEspera = new controladorPartidaEspera(ctrlPrincipal.getServerComunicacion(), idSala, this);
            ctrlPrincipal.setControladorEsperaActual(ctrlEspera);
             System.out.println("VIEW [PartidaEspera]: Controlador específico creado y asignado.");
        } else {
             System.err.println("VIEW [PartidaEspera] ERROR CRÍTICO: No se pudo obtener ServerComunicacion.");
             mostrarError("Error crítico de inicialización.", true);
             ctrlEspera = null;
             // Deshabilitar botón Salir si hay error grave
             if(btnSalir != null) btnSalir.setEnabled(false);
        }
        this.controlador = ctrlEspera;

        // Configurar la UI inicial
        configurarUIInicial();

        // Iniciar tu animación de carga
        iniciarAnimacionCarga(); // Llama al método para iniciar el Timer
    }
    public void mostrarMensajeDeEstado(String mensaje) {
    // Actualiza un JLabel en tu UI para mostrar este mensaje
    // Ejemplo: lblEstadoActual.setText(mensaje);
    System.out.println("VISTA [PartidaEspera]: Mostrando mensaje de estado: " + mensaje);
    JOptionPane.showMessageDialog(this, mensaje, "Información de Partida", JOptionPane.INFORMATION_MESSAGE); // O un JLabel
}

public void mostrarErrorColocacion(String mensajeError) {
    // Podría ser similar a mostrarError, pero específico para la fase de colocación
    mostrarError("Error en Flota: " + mensajeError, false); // false si no es crítico para cerrar
}
    /**
     * Muestra un mensaje de estado o error en el JLabel jLabelInfoOponente.
     * @param mensaje El mensaje a mostrar.
     * @param esError Indica si es un error (cambia color y muestra diálogo).
     */
    public void mostrarError(String mensaje, boolean esError) { // Recibe boolean
         SwingUtilities.invokeLater(() -> {
             System.out.println("VIEW [PartidaEspera]: Mostrando mensaje (Error=" + esError + "): " + mensaje);
             // Mostrar un diálogo extra si es un error para más visibilidad
             if (esError) {
                  JOptionPane.showMessageDialog(this, mensaje, "Error en Sala", JOptionPane.WARNING_MESSAGE);
             }
         });
    }
    // Sobrecarga opcional si a veces solo quieres mostrar info sin diálogo
     public void mostrarMensaje(String mensaje) {
         mostrarError(mensaje, false); // Llama al otro método indicando que NO es error
     }

    /**
     * Configura el estado inicial de los componentes de la UI.
     */
    private void configurarUIInicial() {
        if(lblNombreSala != null) {
             lblNombreSala.setText("Sala: " + idSala);
        } else { System.err.println("VIEW [PartidaEspera] WARN: JLabel 'lblNombreSala' no encontrado."); }

        // Configurar el modelo para el JList 'listaJugadores' (DEBES AÑADIRLO EN NETBEANS)
        listModelJugadores = new DefaultListModel<>();
        if(listaJugadores != null) {
            listaJugadores.setModel(listModelJugadores);
        } else { System.err.println("VIEW [PartidaEspera] WARN: JList 'listaJugadores' no encontrado."); }

        // Añadir al jugador local a la lista
        if (this.miNombreUsuario != null && listModelJugadores != null) {
            listModelJugadores.addElement(this.miNombreUsuario + " (Tú)");
        }

        // Usar jLabel3 para mensajes de estado
        if (jLabel3 != null) {
            jLabel3.setText("Esperando oponente...");
            jLabel3.setForeground(Color.BLACK); // Color por defecto
        } else { System.err.println("VIEW [PartidaEspera] WARN: JLabel 'jLabel3' no encontrado."); }

         // Botón Salir habilitado
         if(btnSalir != null) btnSalir.setEnabled(true);
         // El botón Listo (jButton2/btnListo) debe ser eliminado del diseño
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        panelAnimacion = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        btnSalir = new javax.swing.JButton();
        lblNombreSala = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(32, 51, 75));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(36, 37, 56));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelAnimacion.setBackground(new java.awt.Color(250, 250, 250));

        javax.swing.GroupLayout panelAnimacionLayout = new javax.swing.GroupLayout(panelAnimacion);
        panelAnimacion.setLayout(panelAnimacionLayout);
        panelAnimacionLayout.setHorizontalGroup(
            panelAnimacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );
        panelAnimacionLayout.setVerticalGroup(
            panelAnimacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );

        jPanel2.add(panelAnimacion, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 40, 30));

        jPanel3.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 170, 400, 50));

        jLabel2.setFont(new java.awt.Font("Sitka Display", 1, 36)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Espera que la partida empiece");
        jPanel3.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(98, 90, -1, -1));

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 80, 680, 330));

        btnSalir.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        btnSalir.setForeground(new java.awt.Color(255, 255, 255));
        btnSalir.setText("<");
        btnSalir.setBorderPainted(false);
        btnSalir.setContentAreaFilled(false);
        btnSalir.setFocusPainted(false);
        btnSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalirActionPerformed(evt);
            }
        });
        jPanel1.add(btnSalir, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, -1, -1));

        lblNombreSala.setFont(new java.awt.Font("Sitka Display", 1, 36)); // NOI18N
        lblNombreSala.setForeground(new java.awt.Color(255, 255, 255));
        lblNombreSala.setText("SALA");
        jPanel1.add(lblNombreSala, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 20, -1, -1));

        jLabel3.setFont(new java.awt.Font("Sitka Display", 1, 36)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("SALA: ");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 20, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 883, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 489, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
        // TODO add your handling code here:

        this.dispose();
    }//GEN-LAST:event_btnSalirActionPerformed
// Acción al intentar cerrar ventana con 'X'
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
         System.out.println("VIEW [PartidaEspera]: Intento de cerrar ventana detectado.");
         salirDeSala(); // Ejecutar la misma lógica que el botón Salir
    }//GEN-LAST:event_formWindowClosing

    // ELIMINADO: Ya no existe el botón Listo (jButton2)
    // private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) { ... }


    /**
     * Lógica centralizada para el proceso de salir de la sala.
     * Confirma con el usuario y notifica al controlador.
     */
    private void salirDeSala() {
         System.out.println("VIEW [PartidaEspera]: Iniciando secuencia para salir/cerrar sala.");
         int opcion = JOptionPane.showConfirmDialog(this,
                 "¿Seguro que quieres salir de esta sala?\nVolverás a la pantalla anterior.", // Mensaje más claro
                 "Confirmar Salida",
                 JOptionPane.YES_NO_OPTION,
                 JOptionPane.QUESTION_MESSAGE);

         if (opcion == JOptionPane.YES_OPTION) {
             System.out.println("VIEW [PartidaEspera]: Salida confirmada.");
             if (controlador != null) {
                 System.out.println("VIEW [PartidaEspera]: Llamando a controlador.salirDeSala().");
                 controlador.salirDeSala(); // Notifica al controlador específico de esta pantalla
             } else {
                  System.err.println("VIEW [PartidaEspera]: Controlador es null al intentar salir.");
             }
             // Limpiar referencias en el controlador principal ANTES de cerrar
             if (controladorPrincipal != null) {
                  System.out.println("VIEW [PartidaEspera]: Limpiando controladorEsperaActual en controladorInicio.");
                  controladorPrincipal.clearControladorEsperaActual();
                  // Mostrar de nuevo UnirseJugar
                  // new UnirseJugar(controladorPrincipal).setVisible(true); // Opcional: volver automáticamente
             }
              detenerAnimacionCarga(); // Detener animación al salir
             this.dispose(); // Cierra esta ventana
             // Considera si quieres reabrir UnirseJugar automáticamente aquí
             new UnirseJugar(controladorPrincipal).setVisible(true);

         } else {
              System.out.println("VIEW [PartidaEspera]: Salida cancelada por el usuario.");
         }
    }

    // --- Métodos llamados por el Controlador para actualizar UI (Implementados) ---

    /**
     * Actualiza el JList con los nombres de los jugadores y el label de estado.
     * @param nombres Lista de nombres de usuario.
     */
    public void actualizarListaJugadores(List<String> nombres) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("VIEW [PartidaEspera]: Actualizando lista UI jugadores: " + nombres);
            if (listModelJugadores == null || listaJugadores == null) {
                 System.err.println("VIEW [PartidaEspera] ERROR: JList o su modelo no están inicializados.");
                 return;
            }
            listModelJugadores.clear();
            int contadorJugadores = 0;
            if (nombres != null) {
                for (String nombre : nombres) {
                    if (nombre == null || nombre.isBlank()) continue;
                    String etiqueta = nombre;
                    if (nombre.equals(this.miNombreUsuario)) {
                         etiqueta += " (Tú)";
                    }
                    listModelJugadores.addElement(etiqueta);
                    contadorJugadores++;
                }
            }
            // Actualizar label de estado (usando jLabel3)
            if (jLabel3 != null) {
                 if (contadorJugadores >= 2) {
                      jLabel3.setText("¡Oponente [" + obtenerNombreOponente(nombres) + "] conectado! Iniciando colocación...");
                      // No hay botón "Listo" que habilitar/deshabilitar
                 } else {
                      jLabel3.setText("Esperando oponente...");
                 }
            }
        });
    }

    /** Helper para obtener nombre oponente (sin cambios) */
    private String obtenerNombreOponente(List<String> nombres) {
        if (nombres == null) return "?";
        for (String n : nombres) {
            if (n != null && !n.equals(this.miNombreUsuario)) {
                return n;
            }
        }
        return "?";
    }


    /**
     * Muestra un mensaje de estado o error en el JLabel jLabel3.
     * @param mensaje El mensaje a mostrar.
     * @param esError Indica si es un error (cambia color y muestra diálogo).
     */
    public void mostrarMensaje(String mensaje, boolean esError) {
         SwingUtilities.invokeLater(() -> {
             System.out.println("VIEW [PartidaEspera]: Mostrando mensaje (Error=" + esError + "): " + mensaje);
              if (jLabel3 != null) { // Usar jLabel3 para mensajes
                    jLabel3.setText(mensaje);
                    jLabel3.setForeground(esError ? Color.RED : Color.DARK_GRAY); // Rojo para error, oscuro para info
              }
             // Mostrar un diálogo extra sólo para errores importantes
             if (esError) {
                  JOptionPane.showMessageDialog(this, mensaje, "Aviso de Sala", JOptionPane.WARNING_MESSAGE);
             }
         });
    }

     /** ELIMINADO - Ya no es necesario, la navegación la dispara INICIAR_COLOCACION */
     // public void habilitarInicioJuego(String mensajeEstado) { ... }


    /**
     * Cierra esta ventana y abre la pantalla de colocación de barcos.
     * Llamado por el controlador cuando recibe el evento INICIAR_COLOCACION.
     * @param idSala El ID de la sala actual.
     * @param flotaString La descripción de la flota a colocar.
     */
    public void navegarAPantallaColocacion(String idSala, String flotaString) {
         SwingUtilities.invokeLater(() -> {
             System.out.println("VIEW [PartidaEspera]: Navegando a PantallaColocarBarcos para sala: " + idSala);

             // Crear e instanciar tu JFrame para colocar barcos
             // ASEGÚRATE de que la clase PantallaColocarBarcos exista y tenga este constructor
             TableroJuego pantallaColocar = new TableroJuego(this.controladorPrincipal, idSala, this.miNombreUsuario, flotaString);
             pantallaColocar.setVisible(true);

             if (controladorPrincipal != null) {
                  // Limpiar referencia a este controlador de espera
                  controladorPrincipal.clearControladorEsperaActual();
             }
             detenerAnimacionCarga(); // Detener animación antes de cerrar
             this.dispose(); // Cierra esta ventana de espera
         });
    }

    /** Navega a la pantalla de JUEGO (tableros) - sin cambios */
    public void navegarAPantallaJuego(Map<String, Object> datosPartida) {
         SwingUtilities.invokeLater(() -> {
             System.out.println("VIEW [PartidaEspera]: Navegando a PantallaJuego...");
             // TODO: Crear e instanciar tu JFrame del juego principal (TableroJuego?)
             TableroJuego pantallaJuego = new TableroJuego( controladorPrincipal, idSala,  miNombreUsuario,"none");
             pantallaJuego.setVisible(true);

             if (controladorPrincipal != null) {
                  controladorPrincipal.clearControladorEsperaActual();
             }
             detenerAnimacionCarga();
             this.dispose();
         });
     }

    /** Vuelve a UnirseJugar (sin cambios) */
     public void volverAPantallaAnterior(String mensajeError) {
          SwingUtilities.invokeLater(() -> {
               System.out.println("VIEW [PartidaEspera]: Volviendo a pantalla anterior (UnirseJugar). Motivo: " + mensajeError);
               JOptionPane.showMessageDialog(this, mensajeError, "Sala Terminada", JOptionPane.WARNING_MESSAGE);
               detenerAnimacionCarga();
               if (controladorPrincipal != null) {
                    controladorPrincipal.clearControladorEsperaActual();
                    // Reabrir UnirseJugar
                    new UnirseJugar(controladorPrincipal).setVisible(true);
               }
                this.dispose();
          });
     }

     // --- Tus Métodos de Animación (Existentes y Helper) ---
     private void iniciarAnimacionCarga() {
         // Llama a tu método 'tiempo' para iniciar el Timer
          if (panelAnimacion != null && tiempoAnimacion == null) {
               tiempo(); // Llama a tu método existente
          }
     }

     private void tiempo() { // Tu método existente
         if (tiempoAnimacion == null) {
              tiempoAnimacion = new javax.swing.Timer(100, e -> barraCarga());
              tiempoAnimacion.start();
              System.out.println("VIEW [PartidaEspera]: Timer de animación iniciado.");
         }
     }

     private void barraCarga() { // Tu método existente
         // Usar panelAnimacion en lugar de jPanel4
         if (panelAnimacion == null) {
              detenerAnimacionCarga();
              return;
         }
         posicionAnimacion += 10;
         // Usar el ancho del panel padre (jPanel1) como referencia para el límite
         int maxWidth = jPanel1.getWidth() - (panelAnimacion.getX() * 2); // Ajustar margen
         if (maxWidth <= 20) maxWidth = 380; // Valor por defecto si el layout aún no está listo

         if (posicionAnimacion > maxWidth) {
             posicionAnimacion = 0;
         }
         panelAnimacion.setSize(posicionAnimacion, panelAnimacion.getHeight());
         // Forzar repintado puede ser necesario
         panelAnimacion.repaint();
     }

     private void detenerAnimacionCarga() {
          if (tiempoAnimacion != null) {
               tiempoAnimacion.stop();
               tiempoAnimacion = null;
               System.out.println("VIEW [PartidaEspera]: Timer de animación detenido.");
               // Podrías resetear la barra aquí si quieres
               // if(panelAnimacion != null) panelAnimacion.setSize(0, panelAnimacion.getHeight());
          }
     }
     
     

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSalir;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel lblNombreSala;
    private javax.swing.JPanel panelAnimacion;
    // End of variables declaration//GEN-END:variables
}
